package net.prsv.iniconf;

import java.util.regex.Pattern;

/**
 * Regular-expression patterns used by {@link IniConf}.
 */
final class IniConfPatterns {

    private static final String SECTION_PATH_REGEX = "\\w+(?:\\.\\w+)*";

    static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*[;#].*$");
    static final Pattern SECTION_PATTERN = Pattern.compile("^\\s*\\[(" + SECTION_PATH_REGEX + ")]\\s*$");
    static final Pattern PROPERTY_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.*?)\\s*$");
    static final Pattern KEY_PATTERN = Pattern.compile("^\\w+$");
    static final Pattern SECTION_NAME_PATTERN = Pattern.compile("^" + SECTION_PATH_REGEX + "$");
    static final Pattern SECTION_PATH_SEPARATOR_PATTERN = Pattern.compile("\\.");
    static final Pattern LINE_TERMINATOR_PATTERN = Pattern.compile("\\R");

    private IniConfPatterns() {
    }
}
