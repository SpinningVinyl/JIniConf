package net.prsv.iniconf.test;

import net.prsv.iniconf.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class IniConfTests {

    private final URL testFile = IniConfTests.class.getResource("sample.ini");
    private final String testFilePath;

    {
        assert testFile != null;
        testFilePath = Path.of(testFile.toURI()).toString();
    }

    private IniConf testObject;

    public IniConfTests() throws URISyntaxException {
    }

    @BeforeEach
    void setup() {
        testObject = IniConfReader.read(testFilePath);
        assert(testObject != null);
    }

    @Test
    void constructorWithInputTest() {
        assertFalse(testObject.isEmpty());
    }

    @Test
    void isKeyTest() {
        assertTrue(testObject.isKey("ns_key1"));
        assertTrue(testObject.isKey("ns_key2"));
        assertTrue(testObject.isKey("section3", "key5"));
        assertTrue(testObject.isKey("section3.subsection1.subsubsection2", "key9"));
        assertFalse(testObject.isKey("key5"));
        assertFalse(testObject.isKey("subsection1", "key3"));
    }

    @Test
    void isSectionTest() {
        assertTrue(testObject.isSection("section1"));
        assertTrue(testObject.isSection("section2"));
        assertTrue(testObject.isSection("section3"));
        assertTrue(testObject.isSection("section3.subsection1"));
        assertTrue(testObject.isSection("section3.subsection2"));
        assertTrue(testObject.isSection("section3.subsection1.subsubsection1"));
        assertTrue(testObject.isSection("section3.subsection1.subsubsection2"));
        assertTrue(testObject.isSection("section3.subsection2.subsubsection1"));
        assertFalse(testObject.isSection("section4"));
        assertFalse(testObject.isSection("section1.subsection1"));
        assertFalse(testObject.isSection("section3.subsection1.subsubsection5"));
    }

    @Test
    void getTest() {
        assertEquals("ns_value2", testObject.get("ns_key2"));
        assertEquals("value5", testObject.get("section3", "key5"));
        assertEquals("value", testObject.get("section3.subsection1", "key"));
        assertEquals("value", testObject.get("section3.subsection2.subsubsection1", "key"));
        assertEquals("this is value 6", testObject.get("section3", "key6"));
    }

    @Test
    void getOrDefaultTest() {
        assertEquals("ns_value2", testObject.getOrDefault("ns_key2", "default"));
        assertEquals("default", testObject.getOrDefault("ns_key3", "default"));
        assertEquals("value9", testObject.getOrDefault("section3.subsection1.subsubsection2",
                "key9", "default"));
        assertEquals("default", testObject.getOrDefault("section3.subsection1.subsubsection2",
                "key10", "default"));
    }

    @Test
    void equalsAndHashCodeTest() {
        assertEquals(testObject, testObject);
        IniConf testObject2 = IniConfReader.read(testFilePath);
        assert(testObject2 != null);
        assertEquals(testObject, testObject2);
        assertEquals(testObject.hashCode(), testObject2.hashCode());
        testObject2.put("section2.subsection1", "key11", "value11");
        assertNotEquals(testObject, testObject2);
        assertNotEquals(testObject.hashCode(), testObject2.hashCode());
    }


}
