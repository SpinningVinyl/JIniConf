package net.prsv.iniconf;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class IniConfReader {

    private static boolean error = false;

    // do not instantiate
    private IniConfReader() {}

    /**
     * Reads the specified file and tries to parse it as an INI file. Returns an {@link IniConf} object or {@code null}
     * if an {@link IOException} occurs while reading the file.
     * @param filename name of the input file
     * @return the resulting {@link IniConf} object
     */
    public static IniConf read(String filename) {
        File inputFile = new File(filename);
        String chunk = "";
        try (Scanner s = new Scanner(inputFile)) {
            chunk = s.useDelimiter("\\Z").next(); // read the whole file at once
        } catch (IOException e) {
            e.printStackTrace();
            error = true;
        }
        if (error) {
            return null;
        }
        chunk = chunk.replace('\u0000', '\ufffd');
        return new IniConf(chunk);
    }

}
