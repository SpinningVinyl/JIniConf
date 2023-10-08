package net.prsv.iniconf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class IniConfWriter {

    public static boolean write(String filename, IniConf dict) {
        String output = flatten(dict, null);
        try {
            FileWriter fw = new FileWriter(filename);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(output);
            bw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static String flatten(IniConf dict, String currentDictName) {
        Map<String, String> properties = dict.getProperties();
        Map<String, IniConf> sections = dict.getSubsections();
        StringBuilder sb = new StringBuilder();
        if (currentDictName != null) {
            sb.append('[').append(currentDictName).append(']').append("\n");
        }
        if (!properties.isEmpty()) {
            for (String key : properties.keySet()) {
                sb.append(key).append(" = ").append(properties.get(key)).append("\n");
            }
            sb.append("\n");
        }
        for (String dictName : sections.keySet()) {
            sb.append(flatten(sections.get(dictName),
                    currentDictName == null ? dictName : currentDictName + '.' + dictName));
        }
        return sb.toString();
    }

}
