package com.cat.csc.core.filters;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "CATCSC Asset Filename Validation Filter Configuration",
        description = "Controls filename validation for uploads to AEM DAM"
)
public @interface AssetFilenameValidationConfig {

    @AttributeDefinition(
            name = "Enabled",
            description = "Enable or disable filename validation"
    )
    boolean enabled() default true;

    @AttributeDefinition(
            name = "Target Folder Pattern",
            description = "Regex for paths to validate (default: /content/dam)"
    )
    String targetFolderPattern() default "^/content/dam(/.*)?$";

    @AttributeDefinition(
            name = "Invalid Character Regex",
            description = "Characters not allowed in filenames"
    )
    String invalidCharacterRegex() default "[\\\\/:*?\"<>|{}\\[\\];#%^&+=@!`~]";

    @AttributeDefinition(
            name = "Validate POST Requests",
            description = "Check POST uploads"
    )
    boolean validatePost() default true;

    @AttributeDefinition(
            name = "Validate PUT Requests",
            description = "Check PUT uploads"
    )
    boolean validatePut() default true;
}
