package net.prsv.iniconf;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A mutable representation of an INI configuration containing properties and nested sections.
 * All textual content is represented as Unicode Java strings; {@link IniConfReader} and {@link IniConfWriter} use
 * UTF-8 when converting between those strings and files.
 * Property keys and section names are normalized to lowercase using {@link Locale#ROOT} before they are stored, and
 * query arguments are normalized in the same way.
 */
public final class IniConf {

    private static final String SECTION_PATH_REGEX = "\\w+(?:\\.\\w+)*";
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^[;#].*$");
    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\s*\\[(" + SECTION_PATH_REGEX + ")]\\s*$");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*=\\s*(.*?)\\s*$");
    private static final Pattern KEY_PATTERN = Pattern.compile("^\\w+$");
    private static final Pattern SECTION_NAME_PATTERN = Pattern.compile("^" + SECTION_PATH_REGEX + "$");
    private static final Pattern LINE_TERMINATOR_PATTERN = Pattern.compile("\\R");

    private final HashMap<String, String> properties;
    private final HashMap<String, IniConf> subsections;

    /**
     * Constructs an empty IniConf object.
     */
    public IniConf() {
        properties = new HashMap<>();
        subsections = new HashMap<>();
    }

    /**
     * Parses the input string and creates a new IniConf object.
     * @param input the {@link String} to be parsed
     * @throws NullPointerException if {@code input} is {@code null}
     * @throws IllegalArgumentException if the input contains an invalid section header or property value
     */
    public IniConf(String input) {
        this();
        parse(input);
    }

    /**
     * Checks whether this IniConf object is empty.
     * @return {@code true} if this IniConf contains no properties and no sections
     */
    public boolean isEmpty() {
        return properties.isEmpty() && subsections.isEmpty();
    }

    /**
     * Associates the specified value with the specified key in this IniConf. If the IniConf previously contained a mapping
     * for the key, the old value is replaced.
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key; leading and trailing whitespace is removed before
     *              the value is stored
     * @return the value previously associated with the specified key, or {@code null} if there was no such value
     * @throws NullPointerException if {@code key} or {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code key} is invalid, or if {@code value} contains a line terminator or
     *                                  the NUL character
     */
    public String put(String key, String value) {
        validateAgainstPattern(KEY_PATTERN, key);
        return properties.put(normalizeIdentifier(key), normalizeValue(value));
    }

    /**
     * Associates the specified value with the specified key in the specified subsection of this IniConf.
     * If the specified subsection of this IniConf previously contained a mapping for the key, the old value is replaced.
     * Creating any missing subsections and inserting the key-value pair are treated as two separate operations.
     * Consequently, if insertion fails because the key or value is invalid, subsections created while resolving the
     * specified path remain in this IniConf.
     * @param subsection path of the subsection in which the value is to be associated
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key; leading and trailing whitespace is removed before
     *              the value is stored
     * @return the value previously associated with the specified key in the specified subsection of this IniConf,
     * or {@code null} if there was no such value
     * @throws NullPointerException if {@code subsection}, {@code key}, or {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code subsection} or {@code key} is invalid, or if {@code value} contains
     *                                  a line terminator or the NUL character
     */
    public String put(String subsection, String key, String value) {
        validateAgainstPattern(SECTION_NAME_PATTERN, subsection);
        IniConf currentDict = this;
        String[] sectionPath = normalizeIdentifier(subsection).split("\\.");
        for (String section : sectionPath) {
            if (currentDict.getChild(section) == null) {
                currentDict.addChild(section, new IniConf());
            }
            currentDict = currentDict.getChild(section);
        }
        return currentDict.put(key, value);
    }

    private static void validateAgainstPattern(Pattern pattern, String str) {
        Matcher matcher = pattern.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("String '%s' does not match the provided pattern: '%s'", str, pattern.toString()));
        }
    }

    private static String normalizeValue(String value) {
        Objects.requireNonNull(value, "value must not be null");
        if (LINE_TERMINATOR_PATTERN.matcher(value).find()) {
            throw new IllegalArgumentException("value must not contain line terminators");
        }
        if (value.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("value must not contain the NUL character");
        }
        return value.strip();
    }

    private static String normalizeIdentifier(String identifier) {
        return identifier.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the value associated with the specified key, or {@code null} if no such value exists.
     * @param key the key whose associated value is to be returned
     * @return value associated with the specified key, or {@code null} if no such value exists
     */
    public String get(String key) {
        return properties.get(normalizeIdentifier(key));
    }

    /**
     * Returns the value associated with the specified key in the specified subsection, or {@code null} if no such
     * value exists.
     * @param subsection path of the subsection containing the key
     * @param key the key whose associated value is to be returned
     * @return the value associated with the specified key, or {@code null} if no such value exists
     * @throws NullPointerException if {@code subsection} is {@code null}
     * @throws IllegalArgumentException if {@code subsection} is invalid
     */
    public String get(String subsection, String key) {
        validateAgainstPattern(SECTION_NAME_PATTERN, subsection);
        IniConf currentDict = this;
        String[] sectionPath = normalizeIdentifier(subsection).split("\\.");
        for (String section : sectionPath) {
            if (currentDict.getChild(section) == null) {
                return null;
            }
            currentDict = currentDict.getChild(section);
        }
        return currentDict.get(key);
    }

    /**
     * Returns the value associated with the specified key, or {@code defaultValue} if this IniConf object contains no mapping
     * for the key.
     * @param key the key whose associated values is to be returned
     * @param defaultValue the default value to be returned if there is no value associated with the specified key
     * @return the value to which the specified key is mapped, or {@code defaultValue} if this IniConf object contains no mapping for the key
     */
    public String getOrDefault(String key, String defaultValue) {
        return properties.getOrDefault(normalizeIdentifier(key), defaultValue);
    }

    /**
     * Returns the value associated with the specified key in the specified section, or {@code defaultValue} if
     * there is no such value.
     * @param subsection path of the subsection containing the key
     * @param key the key whose associated values is to be returned
     * @param defaultValue the default value to be returned if there is no value associated with the specified key
     * @return the value to which the specified key in the specified section is mapped, or
     * {@code defaultValue} if no such value exists
     * @throws NullPointerException if {@code subsection} is {@code null}
     * @throws IllegalArgumentException if {@code subsection} is invalid
     */
    public String getOrDefault(String subsection, String key, String defaultValue) {
        validateAgainstPattern(SECTION_NAME_PATTERN, subsection);
        IniConf currentDict = this;
        String[] sectionPath = normalizeIdentifier(subsection).split("\\.");
        for (String section : sectionPath) {
            if (currentDict.getChild(section) == null) {
                return defaultValue;
            }
            currentDict = currentDict.getChild(section);
        }
        return currentDict.getOrDefault(key, defaultValue);
    }

    /**
     * Checks whether the IniConf contains the specified key.
     * @param key the key to be checked
     * @return {@code true} if the IniConf contains the specified key, {@code false} otherwise.
     */
    public boolean isKey(String key) {
        return properties.containsKey(normalizeIdentifier(key));
    }
    /**
     * Checks whether the specified subsection contains the specified key.
     * @param key the key to be checked in the specified subsection
     * @param subsection the subsection to be checked for the specified key
     * @return {@code true} if the specified subsection contains the specified key, {@code false} otherwise.
     * @throws NullPointerException if {@code subsection} is {@code null}
     * @throws IllegalArgumentException if {@code subsection} is invalid
     */
    public boolean isKey(String subsection, String key) {
        validateAgainstPattern(SECTION_NAME_PATTERN, subsection);
        IniConf currentDict = this;
        String[] sectionPath = normalizeIdentifier(subsection).split("\\.");
        for (String section : sectionPath) {
            if (currentDict.getChild(section) == null) {
                return false;
            }
            currentDict = currentDict.getChild(section);
        }
        return currentDict.isKey(key);
    }

    /**
     * Checks whether this IniConf contains a subsection with the specified name.
     * @param sectionName section name to be checked
     * @return {@code true} if this IniConf has a subsection with the specified name, {@code false} otherwise.
     * @throws NullPointerException if {@code sectionName} is {@code null}
     * @throws IllegalArgumentException if {@code sectionName} is invalid
     */
    public boolean isSection(String sectionName) {
        validateAgainstPattern(SECTION_NAME_PATTERN, sectionName);
        IniConf currentDict = this;
        String[] sectionPath = normalizeIdentifier(sectionName).split("\\.");
        for (String section : sectionPath) {
            if (currentDict.getChild(section) == null) {
                return false;
            }
            currentDict = currentDict.getChild(section);
        }
        return true;
    }

    /**
     * Returns the subsection associated with the specified subsection name, or {@code null}
     * if no such subsection exists.
     * @param name the name of subsection to be returned
     * @return subsection associated with the specified subsection name, or {@code null}
     * if no such subsection exists
     * @throws NullPointerException if {@code name} is {@code null}
     * @throws IllegalArgumentException if {@code name} is invalid
     */
    public IniConf getSection(String name) {
        validateAgainstPattern(SECTION_NAME_PATTERN, name);
        IniConf currentDict = this;
        String[] sectionPath = normalizeIdentifier(name).split("\\.");
        for (String section : sectionPath) {
            if (currentDict.getChild(section) == null) {
                return null;
            }
            currentDict = currentDict.getChild(section);
        }
        return currentDict;
    }

    private IniConf getChild(String name) {
        return subsections.get(name);
    }

    private IniConf addChild(String name, IniConf section) {
        return subsections.put(normalizeIdentifier(name), section);
    }

    /**
     * Associates the specified subsection with the specified subsection name. If the IniConf previously had a subsection
     * with the same name, the old subsection is replaced with the new one.
     * @param name the name of subsection to be added
     * @param section the subsection to be associated with the specified name
     * @return the subsection previously associated with the specified name, or {@code null} if there was no such
     * subsection.
     * @throws NullPointerException if {@code name} or {@code section} is {@code null}
     * @throws IllegalArgumentException if {@code name} is invalid, or if adding {@code section} would reuse an
     * existing section or create an irregular section graph
     */
    public IniConf addSection(String name, IniConf section) {
        Objects.requireNonNull(section, "section must not be null");
        validateAgainstPattern(SECTION_NAME_PATTERN, name);
        if (section == this) {
            throw new IllegalArgumentException("addSection(): a section cannot contain itself.");
        }
        ensureDisjointRegularGraphs(section);
        IniConf currentDict = this;
        String[] sectionPath = normalizeIdentifier(name).split("\\.");
        for (int i = 0; i < sectionPath.length - 1; i++) {
            if (currentDict.getChild(sectionPath[i]) == null) {
                currentDict.addChild(sectionPath[i], new IniConf());
            }
            currentDict = currentDict.getChild(sectionPath[i]);
        }
        return currentDict.addChild(sectionPath[sectionPath.length - 1], section);
    }

    private void ensureDisjointRegularGraphs(IniConf section) {
        Set<IniConf> currentGraph = collateRegularGraph(this);
        Set<IniConf> sectionGraph = collateRegularGraph(section);
        for (IniConf candidate : sectionGraph) {
            if (currentGraph.contains(candidate)) {
                throw new IllegalArgumentException("addSection(): section is already part of this section graph.");
            }
        }
    }

    private static Set<IniConf> collateRegularGraph(IniConf root) {
        Set<IniConf> sections = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<IniConf> pending = new ArrayDeque<>();
        pending.push(root);
        while (!pending.isEmpty()) {
            IniConf current = pending.pop();
            if (!sections.add(current)) {
                throw new IllegalArgumentException("addSection(): section graph contains a repeated section.");
            }
            for (IniConf child : current.subsections.values()) {
                if (child == null) {
                    throw new IllegalArgumentException("addSection(): section graph contains a null section.");
                }
                pending.push(child);
            }
        }
        return sections;
    }

    /**
     * Returns an unmodifiable {@link Map} view of all properties in this IniConf object.
     * @return an unmodifiable Map view of all properties in this IniConf object
     */
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Returns an unmodifiable {@link Map} view of all subsections in this IniConf object.
     * @return an unmodifiable Map view of all subsections in this IniConf object
     */
    public Map<String, IniConf> getSections() {
        return Collections.unmodifiableMap(subsections);
    }

    /**
     * Returns a string representation of this IniConf object.
     * @return a string representation of this IniConf object
     */
    @Override
    public String toString() {
        return flatten(this, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        IniConf that = (IniConf) o;
        if (that.properties.size() != this.properties.size()) {
            return false;
        } else {
            for (String key : this.properties.keySet()) {
                if (!this.properties.get(key).equals(that.properties.get(key))) {
                    return false;
                }
            }
        }
        if (this.subsections.size() != that.subsections.size()) {
            return false;
        } else {
            for (String section : this.subsections.keySet()) {
                if (!this.subsections.get(section).equals(that.subsections.get(section))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = 23 * properties.hashCode();

        if (!subsections.isEmpty()) {
            for (String section : subsections.keySet()) {
                result += subsections.get(section).hashCode();
            }
        }
        return result;
    }

    private String flatten(IniConf dict, String currentDictName) {
        Map<String, String> properties = dict.getProperties();
        Map<String, IniConf> sections = dict.getSections();
        StringBuilder sb = new StringBuilder();
        if (currentDictName != null) {
            sb.append('[').append(currentDictName).append(']').append("\n");
        }
        if (!properties.isEmpty()) {
            for (String key : properties.keySet()) {
                sb.append(key).append(" = ").append(serializeValue(properties.get(key))).append("\n");
            }
            sb.append("\n");
        }
        for (String dictName : sections.keySet()) {
            sb.append(flatten(sections.get(dictName),
                    currentDictName == null ? dictName : currentDictName + '.' + dictName));
        }
        return sb.toString();
    }

    private static String serializeValue(String value) {
        boolean requiresQuotes = value.isEmpty()
                || value.indexOf('"') >= 0
                || value.indexOf('\\') >= 0
                || value.codePoints().anyMatch(Character::isWhitespace);
        if (!requiresQuotes) {
            return value;
        }

        String encodedValue = value.replace("\\", "\\\\").replace("\"", "\\\"");
        return '"' + encodedValue + '"';
    }

    private static String deserializeValue(String value, int lineNumber) {
        if (!value.startsWith("\"")) {
            if (value.indexOf('"') >= 0 || value.indexOf('\\') >= 0) {
                throw invalidPropertyValue(lineNumber, "quotes and backslashes must be encoded in a quoted value");
            }
            return value;
        }

        StringBuilder decodedValue = new StringBuilder();
        for (int index = 1; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '"') {
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
        throw invalidPropertyValue(lineNumber, "missing closing quote");
    }

    private static IllegalArgumentException invalidPropertyValue(int lineNumber, String reason) {
        return new IllegalArgumentException("Invalid property value at line " + lineNumber + ": " + reason);
    }

    private void parse(String input) {
        String[] lines = input.split("\\R");
        IniConf currentDict = this;
        String currentSection;

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
            Matcher sectionMatcher = SECTION_PATTERN.matcher(line);
            Matcher propertyMatcher = PROPERTY_PATTERN.matcher(line);
            if (commentMatcher.find()) {
                continue; // comment -- skip the line
            }
            if (sectionMatcher.find()) {
                currentDict = this;
                currentSection = normalizeIdentifier(sectionMatcher.group(1));
                // create new subsections if they don't already exist
                String[] sectionPath = currentSection.split("\\.");
                for (String section : sectionPath) {
                    if (currentDict.getChild(section) == null) {
                        currentDict.addChild(section, new IniConf());
                    }
                    currentDict = currentDict.getChild(section);
                }
            } else if (line.stripLeading().startsWith("[")) {
                throw new IllegalArgumentException(
                        "Invalid section header at line " + (lineIndex + 1) + ": " + line);
            }
            if (propertyMatcher.find()) {
                // add new property to the current IniConf object
                currentDict.put(propertyMatcher.group(1),
                        deserializeValue(propertyMatcher.group(2), lineIndex + 1));
            }
        }
    }

}
