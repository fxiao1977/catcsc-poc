package com.cat.csc.core.filters;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.engine.EngineConstants;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.google.gson.JsonObject;

@Component(
        service = Filter.class,
        immediate = true,
        property = {
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_REQUEST,
                "sling.filter.pattern=^/bin/workfront-tools/uploadInit$",
                "sling.filter.methods=POST",
                "service.ranking:Integer=10000"
        }
)
@Designate(ocd = WorkfrontFilenameValidationConfig.class)
public class WorkfrontFilenameValidationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkfrontFilenameValidationFilter.class);
    private volatile WorkfrontFilenameValidationConfig config;

    @Activate
    @Modified
    protected void activate(WorkfrontFilenameValidationConfig config) {
        LOGGER.info("Workfront Filename Validation Filter activated");
        this.config = config;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Workfront Filename Validation Filter init");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        if (!config.enabled()) {
            chain.doFilter(request, response);
            return;
        }

        SlingHttpServletRequest slingRequest =
                (SlingHttpServletRequest) request;

        SlingHttpServletResponse slingResponse =
                (SlingHttpServletResponse) response;

        String fileName = slingRequest.getParameter("filename");

        // No filename - let the existing connector handle it
        if (fileName == null || fileName.trim().isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String invalidCharacter = findInvalidCharacter(fileName);

        if (invalidCharacter != null) {

            slingResponse.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            slingResponse.setContentType("application/json");
            slingResponse.setCharacterEncoding("UTF-8");

            String message = config.errorMessage()+" Invalid character: '"+ escapeJson(invalidCharacter)+"'";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "error");
            jsonObject.addProperty("error", message);
            String json = jsonObject.toString();

            slingResponse.getWriter().write(json);

            return;
        }

        // Filename is valid - continue to Hoodoo connector
        chain.doFilter(request, response);
    }

    private String findInvalidCharacter(String fileName) {

        Set<String> invalidCharacters =
                new HashSet<>(
                        Arrays.asList(config.invalidCharacters())
                );

        for (int i = 0; i < fileName.length(); i++) {

            String character =
                    String.valueOf(fileName.charAt(i));

            if (invalidCharacters.contains(character)) {
                return character;
            }
        }

        return null;
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    @Override
    public void destroy() {
    }

}