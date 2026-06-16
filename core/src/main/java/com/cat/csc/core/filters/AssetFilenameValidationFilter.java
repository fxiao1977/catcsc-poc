package com.cat.csc.core.filters;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.engine.EngineConstants;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.util.regex.Pattern;

@Component(
        service = Filter.class,
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = {
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_REQUEST
        }
)
@Designate(ocd = AssetFilenameValidationConfig.class)
public class AssetFilenameValidationFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AssetFilenameValidationFilter.class);

    private boolean enabled;
    private Pattern targetFolderPattern;
    private Pattern invalidCharPattern;
    private boolean validatePost;
    private boolean validatePut;

    @Activate
    @Modified
    protected void activate(AssetFilenameValidationConfig config) {
        this.enabled = config.enabled();
        this.targetFolderPattern = Pattern.compile(config.targetFolderPattern());
        this.invalidCharPattern = Pattern.compile(config.invalidCharacterRegex());
        this.validatePost = config.validatePost();
        this.validatePut = config.validatePut();

        log.info("AssetFilenameValidationFilter activated. Enabled={}, TargetPattern={}, InvalidRegex={}, POST={}, PUT={}",
                enabled, config.targetFolderPattern(), config.invalidCharacterRegex(),
                validatePost, validatePut);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Not used in OSGi DS, but required by javax.servlet.Filter
    }

    @Override
    public void destroy() {
        // Not used in OSGi DS
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;

        String method = slingRequest.getMethod();
        String path = slingRequest.getRequestPathInfo().getResourcePath();
        String userId = slingRequest.getUserPrincipal() != null
                ? slingRequest.getUserPrincipal().getName()
                : "unknown";

        boolean isPost = "POST".equalsIgnoreCase(method);
        boolean isPut = "PUT".equalsIgnoreCase(method);

        log.debug("Filename validation triggered. Method={}, Path={}, User={}", method, path, userId);

        if ((isPost && !validatePost) || (isPut && !validatePut)) {
            chain.doFilter(request, response);
            return;
        }

        if (!targetFolderPattern.matcher(path).matches()) {
            chain.doFilter(request, response);
            return;
        }

        String fileName = extractFileName(slingRequest);

        if (fileName == null) {
            chain.doFilter(request, response);
            return;
        }

        if (invalidCharPattern.matcher(fileName).find()) {
            log.warn("Rejected upload. Invalid filename='{}', User='{}', Path='{}', Method='{}'",
                    fileName, userId, path, method);

            slingResponse.setStatus(400);
            slingResponse.getWriter().write(
                    "Invalid filename: '" + fileName +
                            "'. The name contains characters not allowed by AEM."
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private String extractFileName(SlingHttpServletRequest request) {

        if (request.getRequestParameter("file") != null) {
            return request.getRequestParameter("file").getFileName();
        }

        if (request.getRequestParameter(":name") != null) {
            return request.getRequestParameter(":name").getString();
        }

        if (request.getRequestParameter("fileName") != null) {
            return request.getRequestParameter("fileName").getString();
        }

        Resource resource = request.getResource();
        if (resource != null) {
            return resource.getName();
        }

        return null;
    }
}
