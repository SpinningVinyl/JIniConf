package net.prsv.iniconf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IniConf {
    private final HashMap<String, String> properties;
    private final HashMap<String, IniConf> subsections;

    public IniConf() {
        properties = new HashMap<>();
        subsections = new HashMap<>();
    }

    /**
     * Associates the specified value with the specified key in this dict. If the dict previously contained a mapping
     * for the key, the old value is replaced.
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the value previously associated with the specified key, or {@code null} if there was no such value
     */
    public String put(String key, String value) {
        return properties.put(key, value);
    }

    /**
     * Returns the value associated with the specified key, or {@code null} if no such value exists.
     * @param key the key whose associated value is to be returned
     * @return value associated with the specified key, or {@code null} if no such value exists
     */
    public String get(String key) {
        return properties.get(key);
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

    /**
     * Checks whether the dict contains the specified key.
     * @param key the key to be checked
     * @return {@code true} if the dict contains the specified key, {@code false} otherwise.
     */
    public boolean isKey(String key) {
        return properties.containsKey(key);
    }

    /**
     * Checks whether this dict contains a subsection with the specified name.
     * @param sectionName section name to be checked
     * @return {@code true} if this dict has a subsection with the specified name, {@code false} otherwise.
     */
    public boolean isSubsection(String sectionName) {
        return subsections.containsKey(sectionName);
    }

    /**
     * Returns the subsection associated with the specified subsection name, or {@code null}
     * if no such subsection exists.
     * @param name the name of subsection to be returned
     * @return subsection associated with the specified subsection name, or {@code null}
     *         if no such subsection exists
     */
    public IniConf getSubsection(String name) {
        return subsections.get(name);
    }

    /**
     * Associates the specified subsection with the specified subsection name. If the dict previously had a subsection
     * with the same name, the old subsection is replaced with the new one.
     * @param name the name of subsection to be added
     * @param subsection the subsection to be associated with the specified name
     * @return the subsection previously associated with the specified name, or {@code null} if there was no such
     * subsection.
     */
    public IniConf addSubsection(String name, IniConf subsection) {
        return subsections.put(name, subsection);
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public Map<String, IniConf> getSubsections() {
        return Collections.unmodifiableMap(subsections);
    }

}