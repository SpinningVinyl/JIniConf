package net.prsv.iniconf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniConf {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("^.*[\\t\\f ].*$");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^[;#].*$");
    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\s*\\[([\\w.]+)]\\s*$");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("^\\s*(\\w*)\\s*=\\s*['\"]?(.*?)['\"]?\\s*$");

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
     */
    public IniConf(String input) {
        this();
        parse(input);
    }

    /**
     * Associates the specified value with the specified key in this IniConf. If the IniConf previously contained a mapping
     * for the key, the old value is replaced.
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the value previously associated with the specified key, or {@code null} if there was no such value
     */
    public String put(String key, String value) {
        return properties.put(key, value);
    }

    /**
     * Associates the specified value with the specified key in the specified subsection of this IniConf.
     * If the specified subsection of this IniConf previously contained a mapping for the key, the old value is replaced.
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the value previously associated with the specified key in the specified subsection of this IniConf,
     * or {@code null} if there was no such value
     */
    public String put(String subsection, String key, String value) {
        IniConf currentDict = this;
        String[] sectionPath = subsection.split("\\.");
        for (String section : sectionPath) {
            if (currentDict.getChild(section) == null) {
                currentDict.addSubsection(section, new IniConf());
            }
            currentDict = currentDict.getChild(section);
        }
        return currentDict.put(key, value);
    }

    /**
     * Returns the value associated with the specified key, or {@code null} if no such value exists.
     * @param key the key whose associated value is to be returned
     * @return value associated with the specified key, or {@code null} if no such value exists
     */
    public String get(String key) {
        return properties.get(key);
    }

    public String get(String subsection, String key) {
        IniConf currentDict = this;
        String[] sectionPath = subsection.split("\\.");
        for (String section : sectionPath) {
            if (currentDict.getChild(section) == null) {
                return null;
            }
            currentDict = currentDict.getChild(section);
        }
        return currentDict.get(key);
    }

    /**
     * Returns the value associated with the specified key, or {@code defaultValue} if this map contains no mapping
     * for the key.
     * @param key the key whose associated values is to be returned
     * @param defaultValue the default value to be returned if there is no value associated with the specified key
     * @return the value to which the specified key is mapped, or {@code defaultValue} if this map contains no mapping for the key
     */
    public String getOrDefault(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    public String getOrDefault(String subsection, String key, String defaultValue) {
        IniConf currentDict = this;
        String[] sectionPath = subsection.split("\\.");
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
        return properties.containsKey(key);
    }
    /**
     * Checks whether the specified subsection contains the specified key.
     * @param key the key to be checked in the specified subsection
     * @param subsection the subsection to be checked for the specified key
     * @return {@code true} if the specified subsection contains the specified key, {@code false} otherwise.
     */
    public boolean isKey(String subsection, String key) {
        IniConf currentDict = this;
        String[] sectionPath = subsection.split("\\.");
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
     */
    public boolean isSubsection(String sectionName) {
        IniConf currentDict = this;
        String[] sectionPath = sectionName.split("\\.");
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
     */
    public IniConf getSubsection(String name) {
        IniConf currentDict = this;
        String[] sectionPath = name.split("\\.");
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


    /**
     * Associates the specified subsection with the specified subsection name. If the IniConf previously had a subsection
     * with the same name, the old subsection is replaced with the new one.
     * @param name the name of subsection to be added
     * @param subsection the subsection to be associated with the specified name
     * @return the subsection previously associated with the specified name, or {@code null} if there was no such
     * subsection.
     */
    public IniConf addSubsection(String name, IniConf subsection) {
        return subsections.put(name, subsection);
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
    public Map<String, IniConf> getSubsections() {
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

    private String flatten(IniConf dict, String currentDictName) {
        Map<String, String> properties = dict.getProperties();
        Map<String, IniConf> sections = dict.getSubsections();
        StringBuilder sb = new StringBuilder();
        if (currentDictName != null) {
            sb.append('[').append(currentDictName).append(']').append("\n");
        }
        if (!properties.isEmpty()) {
            for (String key : properties.keySet()) {
                String value = properties.get(key);
                Matcher whitespaceMatcher = WHITESPACE_PATTERN.matcher(value);
                if (whitespaceMatcher.find()) {
                    value = "\"" + value + "\"";
                }
                sb.append(key).append(" = ").append(value).append("\n");
            }
            sb.append("\n");
        }
        for (String dictName : sections.keySet()) {
            sb.append(flatten(sections.get(dictName),
                    currentDictName == null ? dictName : currentDictName + '.' + dictName));
        }
        return sb.toString();
    }

    private void parse(String input) {
        String[] lines = input.split("\\R");
        IniConf currentDict = this;
        String currentSection;

        for (String line: lines) {
            Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
            Matcher sectionMatcher = SECTION_PATTERN.matcher(line);
            Matcher propertyMatcher = PROPERTY_PATTERN.matcher(line);
            if (commentMatcher.find()) {
                continue; // comment -- skip the line
            }
            if (sectionMatcher.find()) {
                currentDict = this;
                currentSection = sectionMatcher.group(1).toLowerCase();
                // create new subsections if they don't already exist
                String[] sectionPath = currentSection.split("\\.");
                for (String section : sectionPath) {
                    if (currentDict.getChild(section) == null) {
                        currentDict.addSubsection(section, new IniConf());
                    }
                    currentDict = currentDict.getChild(section);
                }
            }
            if (propertyMatcher.find()) {
                // add new property to the current IniConf object
                currentDict.put(propertyMatcher.group(1).toLowerCase(), propertyMatcher.group(2));
            }
        }
    }

}
