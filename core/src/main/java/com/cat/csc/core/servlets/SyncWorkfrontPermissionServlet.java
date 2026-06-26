package com.cat.csc.core.servlets;

import com.cat.csc.core.service.PermissionSyncService;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.json.JSONException;
import org.osgi.service.component.annotations.Component;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.osgi.service.component.annotations.Reference;
import javax.jcr.Session;
import javax.servlet.ServletException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/catcsc/syncWorkfrontPermissions",
                "sling.servlet.methods=" + HttpConstants.METHOD_POST
        }
)
public class SyncWorkfrontPermissionServlet extends SlingAllMethodsServlet {

    @Reference
    private PermissionSyncService permissionSyncService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        ResourceResolver resourceResolver = request.getResourceResolver();
        Session session = resourceResolver.adaptTo(Session.class);

        String[] folders = request.getParameterValues("folders");
        JSONObject json = new JSONObject();

        try {
            if (folders == null || folders.length == 0) {
                json.put("message", "No folders received.");
                response.getWriter().write(json.toString());
                return;
            }

            // Filter private folders using helper method
            List<String> privateFolders = filterPrivateFolders(folders, session);

            if (privateFolders.isEmpty()) {
                json.put("message", "No private folders found in selection, or you don't have permission on them.");
                response.getWriter().write(json.toString());
                return;
            }
            else {
                // Loop through folders
                int successCount = 0;
                for (String folderPath : privateFolders) {
                    try {
                        permissionSyncService.syncFolderPermissions(session, resourceResolver, folderPath);
                        successCount++;
                    } catch (Exception e) {
                        // Continue processing other folders
                        json.put("message",
                                "Error processing folder " + folderPath + ": " + e.getMessage());
                    }
                }
                json.put("message", "Synced permissions for " + folders.length + " folder(s):");
            }
        } catch (Exception e) {
            // Fallback JSON if something goes wrong
            json = new JSONObject();
            try {
                json.put("message", "Error building JSON response: " + e.getMessage());
            } catch (JSONException ignored) {
                // This should never happen, but we guard anyway
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }

    // ---------------------------------------------------------
    // PRIVATE HELPER: Filter out non-private folders
    // ---------------------------------------------------------
    private List<String> filterPrivateFolders(String[] folders, Session session) throws Exception {
        List<String> privateFolders = new ArrayList<>();

        for (String folderPath : folders) {
            String policyPath = folderPath + "/rep:policy";

            if (session.nodeExists(policyPath)) {
                privateFolders.add(folderPath);
            }
        }

        return privateFolders;
    }
}
