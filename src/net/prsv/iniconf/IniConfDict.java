package net.prsv.iniconf;

import java.util.HashMap;

public class IniConfDict {
    private final HashMap<String, String> properties;
    private final HashMap<String, IniConfDict> subsections;

    public IniConfDict() {
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
     * Returns the subsection associated with the specified subsection name, or {@code null}
     * if no such subsection exists.
     * @param name the name of subsection to be returned
     * @return subsection associated with the specified subsection name, or {@code null}
     *         if no such subsection exists
     */
    public IniConfDict getSubsection(String name) {
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
    public IniConfDict addSubsection(String name, IniConfDict subsection) {
        return subsections.put(name, subsection);
    }

    protected HashMap<String, String> getProperties() {
        return properties;
    }

    protected HashMap<String, IniConfDict> getSubsections() {
        return subsections;
    }

}
