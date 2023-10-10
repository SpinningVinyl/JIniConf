package net.prsv.iniconf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class IniConfWriter {

    public static boolean write(String filename, IniConf dict) {
        assert(dict != null);
        try {
            FileWriter fw = new FileWriter(filename);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(dict.toString());
            bw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
