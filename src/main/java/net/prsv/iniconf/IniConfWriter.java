package net.prsv.iniconf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Writes INI configurations to UTF-8 encoded files.
 */
public final class IniConfWriter {

    // do not instantiate
    private IniConfWriter() {}

    /**
     * Writes the specified configuration to a UTF-8 encoded file.
     * @param filename name of the output file
     * @param dict configuration to be written
     * @return {@code true} if the configuration was written successfully, or {@code false} if an I/O error occurred
     * @throws NullPointerException if {@code filename} or {@code dict} is {@code null}
     */
    public static boolean write(String filename, IniConf dict) {
        Objects.requireNonNull(dict, "dict must not be null");
        try {
            Files.writeString(Path.of(filename), dict.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
