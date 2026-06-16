package com.cat.csc.core.schedulers;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;

import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.*;

import javax.jcr.*;
import javax.jcr.security.*;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jackrabbit.api.security.user.*;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;

import com.day.cq.search.QueryBuilder;
import com.day.cq.search.Query;
import com.day.cq.search.result.SearchResult;
import com.day.cq.search.result.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.search.PredicateGroup;

@Component(service = Runnable.class, immediate = true)
@Designate(ocd = PrivateFolderAclSyncSchedulerConfiguration.class)
public class PrivateFolderAclSyncScheduler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateFolderAclSyncScheduler.class);

    @Reference
    private Scheduler scheduler;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    private PrivateFolderAclSyncSchedulerConfiguration config;

    private String jobName = PrivateFolderAclSyncScheduler.class.getName();
    private AtomicBoolean running = new AtomicBoolean(false);


    @Activate
    @Modified
    protected void activate(final PrivateFolderAclSyncSchedulerConfiguration config) {

        addScheduler(config);
    }

    private void addScheduler(PrivateFolderAclSyncSchedulerConfiguration config) {

        this.config = config;
        if (!config.enable_scheduler()) return;

        ScheduleOptions options = scheduler.EXPR(config.scheduler_expression());
        options.name(config.scheduler_name());

        options.canRunConcurrently(config.concurrent_scheduler());

        scheduler.schedule(this, options);
        LOGGER.info(" >>>>  Scheduler added successfully name='{}'", config.scheduler_name());
    }


    // Custom method to deactivate or unschedule scheduler
    public void removeScheduler(PrivateFolderAclSyncSchedulerConfiguration config) {
        scheduler.unschedule(config.scheduler_name());
    }

    // On deactivate component it will unschedule scheduler
    @Deactivate
    protected void deactivate(PrivateFolderAclSyncSchedulerConfiguration config) {
        removeScheduler(config);
    }

    // On component modification change status will remove and add scheduler
    @Modified
    protected void modified(PrivateFolderAclSyncSchedulerConfiguration config) {
        removeScheduler(config);
        addScheduler(config);
    }

    @Override
    public void run() {

        LOGGER.info(">>>>>>>>>>> SCHEDULE RUN >>>>>>>>>>>");

        if(!config.enable_scheduler()){
            return;
        }

        Map<String, Object> param = Collections.singletonMap(
                ResourceResolverFactory.SUBSERVICE, "private-folder-service"
        );

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(param)) {
            Session session = resolver.adaptTo(Session.class);
            if (session == null) {
                return;
            }

            processPrivateFolders(session, resolver);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            running.set(false);
        }
    }

    // ---------------------------------------------------------
    // STEP 1: Find private folders using Query Builder
    // ---------------------------------------------------------
    private void processPrivateFolders(Session session, ResourceResolver resolver)
            throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("path", config.rootPath());
        params.put("type", "sling:Folder");
        params.put("nodename", "*");
        params.put("property", "rep:policy/deny/rep:principalName");
        params.put("property.value", "everyone");
        params.put("p.limit", "-1");

        PredicateGroup predicates = PredicateGroup.create(params);
        Query query = queryBuilder.createQuery(predicates, session);

        SearchResult result = query.getResult();

        for (Hit hit : result.getHits()) {
            String folderPath = hit.getPath();
            processFolder(session, resolver, folderPath);
        }
    }

    // ---------------------------------------------------------
    // STEP 2–4: Process ACLs, expand groups, map users, copy ACEs
    // ---------------------------------------------------------
    private void processFolder(Session session, ResourceResolver resolver, String folderPath)
            throws RepositoryException {

        AccessControlManager acm = session.getAccessControlManager();
        AccessControlPolicy[] policies = acm.getPolicies(folderPath);

        JackrabbitAccessControlList acl = null;
        for (AccessControlPolicy p : policies) {
            if (p instanceof JackrabbitAccessControlList) {
                acl = (JackrabbitAccessControlList) p;
                break;
            }
        }
        if (acl == null) return;

        UserManager userManager = resolver.adaptTo(UserManager.class);
        PrincipalManager principalManager =
                ((JackrabbitSession) session).getPrincipalManager();

        for (AccessControlEntry entry : acl.getAccessControlEntries()) {

            if (!(entry instanceof JackrabbitAccessControlEntry)) {
                continue;
            }

            JackrabbitAccessControlEntry jace =
                    (JackrabbitAccessControlEntry) entry;

            Principal principal = jace.getPrincipal();
            String principalName = principal.getName();

            // Skip system principals
            if ("everyone".equals(principalName)
                    || "rep:anonymous".equals(principalName)
                    || "rep:authenticated".equals(principalName)) {
                continue;
            }

            Authorizable authA = userManager.getAuthorizable(principalName);
            if (authA == null) {
                continue; // external/system principal
            }

            // CASE 1: principal is a GROUP → sync membership for Workfront IDs
            if (authA.isGroup()) {
                Group groupA = (Group) authA;

                // For each AEM ID in the group, ensure Workfront ID is in the group
                List<Authorizable> members = expandGroupMembers(groupA);
                for (Authorizable member : members) {
                    if (member.isGroup()) {
                        continue;
                    }

                    String aemEmail = getEmailFromAuthorizable(member);
                    if (aemEmail == null) {
                        continue;
                    }

                    Authorizable workfront = findWorkfrontIdByEmail(userManager, aemEmail);
                    if (workfront != null && !workfront.isGroup()) {
                        groupA.addMember(workfront);
                    }
                }

                // Sync removal: remove Workfront IDs without matching AEM IDs
                syncGroupMembership(groupA, userManager);

                session.save();
                continue;
            }

            // CASE 2: principal is an AEM ID → copy ACE and sync add/update/remove
            processAemIdPrincipal(session, resolver, acl, jace, authA, principalManager);
        }


        removeWorkfrontAcesForMissingAemIds(acl, userManager);

        acm.setPolicy(folderPath, acl);
        session.save();
    }

    // ---------------------------------------------------------
    // STEP 3 + STEP 4: Map aemID → userB and copy ACE
    // ---------------------------------------------------------
    private void processAemIdPrincipal(Session session,
                                       ResourceResolver resolver,
                                       JackrabbitAccessControlList acl,
                                       JackrabbitAccessControlEntry jace,
                                       Authorizable aemId,
                                       PrincipalManager principalManager) throws RepositoryException {

        UserManager userManager = resolver.adaptTo(UserManager.class);

        String aemEmail = getEmailFromAuthorizable(aemId);
        if (aemEmail == null) {
            return;
        }

        Authorizable workfront = findWorkfrontIdByEmail(userManager, aemEmail);
        if (workfront == null || workfront.isGroup()) return;

        final String workfrontId = workfront.getID();

        Principal workfrontPrincipal = principalManager.getPrincipal(workfrontId);
        if (workfrontPrincipal == null) {
            workfrontPrincipal = new Principal() {
                @Override
                public String getName() {
                    return workfrontId;
                }
            };
        }

        // Ensure ACE exists for Workfront ID
        addAceIfMissing(acl, workfrontPrincipal, jace.getPrivileges(), jace.isAllow());

        // Sync removal and updates for all ACEs of AEM ID → Workfront ID
        syncWorkfrontAce(acl, jace.getPrincipal(), workfrontPrincipal);
    }

    private String getEmailFromAuthorizable(Authorizable auth) throws RepositoryException {
        Value[] emailValues = auth.getProperty("profile/email");
        if (emailValues != null && emailValues.length > 0 && emailValues[0] != null) {
            return emailValues[0].getString();
        }
        return null;
    }


    // Workfront ID: userId = "wf-" + email
    private Authorizable findWorkfrontIdByEmail(UserManager um, String email) throws RepositoryException {
        if (email == null || email.isEmpty()) {
            return null;
        }
        String expectedUserId = "wf-" + email;
        Authorizable workfront = um.getAuthorizable(expectedUserId);
        if (workfront != null && !workfront.isGroup()) {
            return workfront;
        }
        return null;
    }

    // ---------------------------------------------------------
    // Expand group → all user members (recursive)
    // ---------------------------------------------------------
    private List<Authorizable> expandGroupMembers(Group group) throws RepositoryException {
        List<Authorizable> users = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        collectMembers(group, users, visited);
        return users;
    }

    private void collectMembers(Authorizable auth,
                                List<Authorizable> users,
                                Set<String> visited) throws RepositoryException {

        if (auth == null) return;

        if (!visited.add(auth.getID())) return;

        if (!auth.isGroup()) {
            users.add(auth);
            return;
        }

        Group group = (Group) auth;
        Iterator<Authorizable> it = group.getMembers();

        while (it.hasNext()) {
            collectMembers(it.next(), users, visited);
        }
    }

    // ---------------------------------------------------------
    // Add ACE only if not already present
    // ---------------------------------------------------------
    private void addAceIfMissing(JackrabbitAccessControlList acl,
                                 Principal principal,
                                 Privilege[] privileges,
                                 boolean allow) throws RepositoryException {

        for (AccessControlEntry entry : acl.getAccessControlEntries()) {
            if (entry instanceof JackrabbitAccessControlEntry) {
                JackrabbitAccessControlEntry jace =
                        (JackrabbitAccessControlEntry) entry;

                boolean samePrincipal = jace.getPrincipal().getName().equals(principal.getName());
                boolean sameAllowDeny = jace.isAllow() == allow;
                boolean samePrivileges = samePrivileges(jace.getPrivileges(), privileges);

                if (samePrincipal && sameAllowDeny && samePrivileges) {
                    return; // ACE already exists
                }
            }
        }

        acl.addEntry(principal, privileges, allow);
    }

    private boolean samePrivileges(Privilege[] a, Privilege[] b) {
        if (a.length != b.length) return false;

        Set<String> names = new HashSet<>();
        for (Privilege p : a) names.add(p.getName());

        for (Privilege p : b) {
            if (!names.contains(p.getName())) return false;
        }

        return true;
    }

    // Sync ACEs: Workfront ID should mirror AEM ID (add/update/remove)
    private void syncWorkfrontAce(JackrabbitAccessControlList acl,
                                  Principal aemPrincipal,
                                  Principal workfrontPrincipal) throws RepositoryException {

        List<JackrabbitAccessControlEntry> aemEntries = new ArrayList<>();
        List<JackrabbitAccessControlEntry> workfrontEntries = new ArrayList<>();

        for (AccessControlEntry entry : acl.getAccessControlEntries()) {
            if (!(entry instanceof JackrabbitAccessControlEntry)) continue;

            JackrabbitAccessControlEntry jace = (JackrabbitAccessControlEntry) entry;

            if (jace.getPrincipal().getName().equals(aemPrincipal.getName())) {
                aemEntries.add(jace);
            }

            if (jace.getPrincipal().getName().equals(workfrontPrincipal.getName())) {
                workfrontEntries.add(jace);
            }
        }

        // Remove Workfront ACEs that AEM ID no longer has
        for (JackrabbitAccessControlEntry bEntry : workfrontEntries) {
            boolean existsInA = false;

            for (JackrabbitAccessControlEntry aEntry : aemEntries) {
                if (samePrivileges(aEntry.getPrivileges(), bEntry.getPrivileges())
                        && aEntry.isAllow() == bEntry.isAllow()) {
                    existsInA = true;
                    break;
                }
            }

            if (!existsInA) {
                acl.removeAccessControlEntry(bEntry);
            }
        }

        // Ensure all AEM ACEs exist for Workfront ID
        for (JackrabbitAccessControlEntry aEntry : aemEntries) {
            addAceIfMissing(acl, workfrontPrincipal, aEntry.getPrivileges(), aEntry.isAllow());
        }
    }

    // Sync group membership: Workfront IDs should exist in group only if corresponding AEM IDs exist
    private void syncGroupMembership(Group groupA, UserManager um) throws RepositoryException {

        Set<String> aemEmails = new HashSet<>();
        Iterator<Authorizable> members = groupA.getMembers();

        while (members.hasNext()) {
            Authorizable a = members.next();
            if (!a.isGroup()) {
                String email = getEmailFromAuthorizable(a);
                if (email != null) {
                    aemEmails.add(email);
                }
            }
        }

        members = groupA.getMembers();
        while (members.hasNext()) {
            Authorizable a = members.next();
            if (a.isGroup()) {
                continue;
            }

            String id = a.getID();
            if (id != null && id.startsWith("wf-")) {
                String email = id.substring("wf-".length());

                if (!aemEmails.contains(email)) {
                    groupA.removeMember(a);
                }
            }
        }
    }

    private void removeWorkfrontAcesForMissingAemIds(JackrabbitAccessControlList acl,
                                                     UserManager um) throws RepositoryException {

        Set<String> aemEmails = new HashSet<>();
        Set<JackrabbitAccessControlEntry> workfrontEntries = new HashSet<>();

        // Collect AEM ID emails and Workfront ACEs
        for (AccessControlEntry entry : acl.getAccessControlEntries()) {
            if (!(entry instanceof JackrabbitAccessControlEntry)) continue;

            JackrabbitAccessControlEntry jace = (JackrabbitAccessControlEntry) entry;
            String principalName = jace.getPrincipal().getName();

            if (principalName.startsWith("wf-")) {
                workfrontEntries.add(jace);
            } else {
                // Try to resolve AEM ID email
                Authorizable aemId = um.getAuthorizable(principalName);
                if (aemId != null && !aemId.isGroup()) {
                    String email = getEmailFromAuthorizable(aemId);
                    if (email != null) {
                        aemEmails.add(email);
                    }
                }
            }
        }

        // Remove Workfront ACEs whose AEM ID no longer exists
        for (JackrabbitAccessControlEntry wfEntry : workfrontEntries) {
            String wfId = wfEntry.getPrincipal().getName();
            String email = wfId.substring("wf-".length());

            if (!aemEmails.contains(email)) {
                acl.removeAccessControlEntry(wfEntry);
            }
        }
    }

}
