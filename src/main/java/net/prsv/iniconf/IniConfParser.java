package net.prsv.iniconf;

import java.util.regex.Matcher;

/**
 * Parses INI-formatted strings into {@link IniConf} instances.
 */
final class IniConfParser {

    private IniConfParser() {
    }

    static void parseInto(String input, IniConf destination) {
        String[] lines = IniConfPatterns.LINE_TERMINATOR_PATTERN.split(input);
        IniConf currentSection = destination;

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            int lineNumber = lineIndex + 1;
            Matcher commentMatcher = IniConfPatterns.COMMENT_PATTERN.matcher(line);
            Matcher sectionMatcher = IniConfPatterns.SECTION_PATTERN.matcher(line);
            Matcher propertyMatcher = IniConfPatterns.PROPERTY_PATTERN.matcher(line);
            if (line.isBlank() || commentMatcher.matches()) {
                continue;
            }
            if (sectionMatcher.matches()) {
                currentSection = destination.getOrCreateSection(sectionMatcher.group(1));
                continue;
            }
            if (line.stripLeading().startsWith("[")) {
                throw new IniConfFormatException(lineNumber, "Invalid section header: " + line);
            }
            if (propertyMatcher.matches()) {
                String value = deserializeValue(propertyMatcher.group(2), lineNumber);
                putProperty(currentSection, propertyMatcher.group(1), value, lineNumber);
                continue;
            }
            throw new IniConfFormatException(lineNumber, "Malformed input: " + line);
        }
    }

    private static void putProperty(IniConf section, String key, String value, int lineNumber) {
        try {
            section.put(key, value);
        } catch (IllegalArgumentException exception) {
            throw invalidPropertyValue(lineNumber, exception.getMessage());
        }
    }

    private static String deserializeValue(String value, int lineNumber) {
        boolean quoted = value.startsWith("\"");
        StringBuilder decodedValue = new StringBuilder();
        for (int index = quoted ? 1 : 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '"') {
                if (!quoted) {
                    throw invalidPropertyValue(lineNumber, "quotation marks must be encoded");
                }
                if (index != value.length() - 1) {
                    throw invalidPropertyValue(lineNumber, "unexpected characters after closing quote");
                }
                return decodedValue.toString();
            }
            if (current == '\\') {
                if (++index == value.length()) {
                    throw invalidPropertyValue(lineNumber, "incomplete escape sequence");
                }
                char escaped = value.charAt(index);
                if (escaped != '"' && escaped != '\\') {
                    throw invalidPropertyValue(lineNumber, "unknown escape sequence: \\" + escaped);
                }
                decodedValue.append(escaped);
            } else {
                decodedValue.append(current);
            }
        }
        if (quoted) {
            throw invalidPropertyValue(lineNumber, "missing closing quote");
        }
        return decodedValue.toString();
    }

    private static IniConfFormatException invalidPropertyValue(int lineNumber, String reason) {
        return new IniConfFormatException(lineNumber, "Invalid property value: " + reason);
    }
}
