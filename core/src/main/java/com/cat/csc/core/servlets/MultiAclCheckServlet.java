package com.cat.csc.core.servlets;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import javax.servlet.Servlet;
import java.io.IOException;
import javax.jcr.Session;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/catcsc/check-multi-acl",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class MultiAclCheckServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        ResourceResolver resolver = request.getResourceResolver();

        // Get selected items from request
        String[] paths = request.getParameterValues("item");
        boolean allowed = true;

        try {
            Session session = resolver.adaptTo(Session.class);
            AccessControlManager acm = session.getAccessControlManager();
            Privilege modify = acm.privilegeFromName("jcr:modifyAccessControl");
            Privilege readAcl = acm.privilegeFromName("jcr:readAccessControl");
            Privilege repWrite = acm.privilegeFromName("rep:write");
            Privilege allPriv = acm.privilegeFromName("jcr:all");

            for (String path : paths) {
                Privilege[] userPrivs = acm.getPrivileges(path);

                // Must be a folder
                Resource r = resolver.getResource(path);
                if (r == null || !"sling:Folder".equals(r.getValueMap().get("jcr:primaryType"))) {
                    allowed = false;
                    break;
                }

                // If user has jcr:all, allow immediately
                boolean hasAll = false;
                for (Privilege p : userPrivs) {
                    if (p.equals(allPriv)) {
                        hasAll = true;
                        break;
                    }
                }
                if (hasAll) {
                    continue; // skip other checks
                }


                // Check privileges
                Privilege[] required = new Privilege[]{
                        acm.privilegeFromName("jcr:modifyAccessControl"),
                        acm.privilegeFromName("jcr:readAccessControl"),
                        acm.privilegeFromName("rep:write")
                };

                boolean hasModify = false;
                boolean hasReadAcl = false;
                boolean hasRepWrite = false;

                for (Privilege p : userPrivs) {
                    if (p.equals(modify)) hasModify = true;
                    if (p.equals(readAcl)) hasReadAcl = true;
                    if (p.equals(repWrite)) hasRepWrite = true;
                }

                if (!hasModify || !hasReadAcl || !hasRepWrite) {
                    allowed = false;
                    break;
                }
            }
        } catch (Exception e) {
            allowed = false;
        }

        response.setContentType("application/json");
        response.getWriter().write("{\"allowed\": " + allowed + "}");
    }
}
