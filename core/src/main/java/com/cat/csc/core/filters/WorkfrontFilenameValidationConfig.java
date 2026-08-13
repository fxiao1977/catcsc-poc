package com.cat.csc.core.filters;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
        name = "CSC Workfront Filename Validation Filter Configuration",
        description = "Controls filename validation for uploads to AEM DAM"
)
public @interface WorkfrontFilenameValidationConfig {

    @AttributeDefinition(
            name = "Enabled",
            description = "Enable or disable workfront filename validation"
    )
    boolean enabled() default true;

    @AttributeDefinition(
            name = "Invalid Character Regex",
            description = "Characters not allowed in filenames"
    )
    String[] invalidCharacters() default {
            "#",
            "%",
            "&",
            "*",
            "?",
            ":",
            ";",
            "/",
            "\\",
            "|",
            "<",
            ">",
            "\"",
            "'",
            "@",
            "!",
            "$",
            "^",
            "(",
            ")",
            "[",
            "]",
            "{",
            "}",
            "="
    };

    @AttributeDefinition(
            name = "Error Message",
            description = "Error message returned to Workfront"
    )
    String errorMessage() default
            "File name contains invalid special characters.";
}
