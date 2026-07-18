package net.prsv.iniconf.test;

import net.prsv.iniconf.IniConf;
import net.prsv.iniconf.IniConfReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IniConfReaderTests {

    @TempDir
    Path tempDir;

    @Test
    void readsEmptyFileAsEmptyConfiguration() throws IOException {
        Path emptyFile = Files.createFile(tempDir.resolve("empty.ini"));

        Optional<IniConf> result = IniConfReader.read(emptyFile.toString());

        assertTrue(result.isPresent());
        assertTrue(result.orElseThrow().isEmpty());
    }

    @Test
    void returnsEmptyOptionalForMissingFile() {
        Path missingFile = tempDir.resolve("missing.ini");

        Optional<IniConf> result = IniConfReader.read(missingFile.toString());

        assertTrue(result.isEmpty());
    }

    @Test
    void successfulReadFollowsFailedRead() throws IOException {
        Path missingFile = tempDir.resolve("missing.ini");
        Path validFile = tempDir.resolve("valid.ini");
        Files.writeString(validFile, "key = value\n");

        assertTrue(IniConfReader.read(missingFile.toString()).isEmpty());

        Optional<IniConf> result = IniConfReader.read(validFile.toString());
        assertTrue(result.isPresent());
        assertEquals("value", result.orElseThrow().get("key"));
    }
}
