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
     * @return this IniConfDict object
     */
    public IniConfDict put(String key, String value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Returns the value associated with the specified key, or {@code null} if no such value exists.
     * @param key the key whose associated value is to be returned
     * @return value associated with the specified key, or {@code null} if no such value exists
     */
    public String get(String key) {
        return properties.get(key);
    }

    public IniConfDict getSubsection(String name) {
        return subsections.get(name);
    }

    public IniConfDict addSubsection(String name, IniConfDict subsection) {
        subsections.put(name, subsection);
        return this;
    }

}
