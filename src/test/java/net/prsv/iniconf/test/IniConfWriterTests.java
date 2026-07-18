package net.prsv.iniconf.test;

import net.prsv.iniconf.IniConf;
import net.prsv.iniconf.IniConfReader;
import net.prsv.iniconf.IniConfWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IniConfWriterTests {

    @TempDir
    Path tempDir;

    @Test
    void writesUtf8EncodedConfiguration() throws IOException {
        IniConf iniConf = new IniConf();
        iniConf.put("key", "Zażółć gęślą jaźń — 日本語 😀");
        Path outputFile = tempDir.resolve("utf8.ini");

        assertTrue(IniConfWriter.write(outputFile.toString(), iniConf));
        assertArrayEquals(iniConf.toString().getBytes(StandardCharsets.UTF_8), Files.readAllBytes(outputFile));
        assertEquals(iniConf, IniConfReader.read(outputFile.toString()).orElseThrow());
    }
}
