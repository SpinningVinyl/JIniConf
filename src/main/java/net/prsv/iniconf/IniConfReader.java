package net.prsv.iniconf;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

public class IniConfReader {

    // do not instantiate
    private IniConfReader() {}

    /**
     * Reads the specified file and tries to parse it as an INI file. Returns an {@code Optional<IniConf>} or {@code Optional.empty()}
     * if an {@link IOException} occurs while reading the file.
     * @param filename name of the input file
     * @return the resulting {@code Optional<IniConf>} or {@code Optional.empty()}
     */
    public static Optional<IniConf> read(String filename) {
        File inputFile = new File(filename);
        String chunk = "";
        try (Scanner s = new Scanner(inputFile)) {
            s.useDelimiter("\\Z");
            if (s.hasNext()) {
                chunk = s.next(); // read the whole file at once
            }
            if (s.ioException() != null) throw s.ioException();
        } catch (IOException e) {
            return Optional.empty();
        }
        chunk = chunk.replace('\u0000', '\ufffd');
        return Optional.of(new IniConf(chunk));
    }

}
