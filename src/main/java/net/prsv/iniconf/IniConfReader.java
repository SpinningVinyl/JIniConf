package net.prsv.iniconf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads UTF-8 encoded INI configuration files.
 */
public final class IniConfReader {

    private static final char BYTE_ORDER_MARK = '\ufeff';

    // do not instantiate
    private IniConfReader() {}

    /**
     * Reads the specified UTF-8 encoded file and tries to parse it as an INI file. Returns an
     * {@code Optional<IniConf>} or {@code Optional.empty()} if an {@link IOException} occurs while reading the file.
     * @param filename name of the UTF-8 encoded input file
     * @return the resulting {@code Optional<IniConf>} or {@code Optional.empty()}
     * @throws NullPointerException if {@code filename} is {@code null}
     * @throws IllegalArgumentException if the file contains invalid INI content
     */
    public static Optional<IniConf> read(String filename) {
        try {
            String input = Files.readString(Path.of(filename), StandardCharsets.UTF_8);
            if (!input.isEmpty() && input.charAt(0) == BYTE_ORDER_MARK) {
                input = input.substring(1);
            }
            return Optional.of(new IniConf(input));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

}
