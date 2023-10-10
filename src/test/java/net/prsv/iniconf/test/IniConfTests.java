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

    @Test
    void putTest() {
        String testKey = "test_key";
        String illegalTestKey = "test key with spaces";
        String testValue = "test value";
        String testValue2 = "another test value";
        String testSection = "section.subsection.subsubsection";
        IniConf testObject3 = new IniConf();
        assertTrue(testObject3.isEmpty());
        testObject3.put(testKey, testValue);
        assertFalse(testObject3.isEmpty());
        assertTrue(testObject3.isKey(testKey));
        testObject3.put(testSection, testKey, testValue);
        assertTrue(testObject3.isSection(testSection));
        assertTrue(testObject3.isKey(testSection, testKey));
        assertEquals(testValue, testObject3.put(testSection, testKey, testValue2));
        assertEquals(testValue, testObject3.put(testKey, testValue2));
        assertEquals(testValue2, testObject3.get(testKey));
        assertEquals(testValue2, testObject3.get(testSection, testKey));
        assertThrows(IllegalArgumentException.class, () -> testObject3.put(illegalTestKey, testValue));
    }

    @Test
    void addSectionTest() {
        String testSection = "section.subsection.subsubsection";
        String illegalTestSection1 = "section/subsection";
        String illegalTestSection2 = "section subsection";
        String illegalTestSection3 = "section\u0000subsection";
        IniConf testObject4 = new IniConf();
        assertNull(testObject4.addSection(testSection, new IniConf(testObject.toString())));
        assertEquals(testObject, testObject4.addSection(testSection, new IniConf()));
        assertThrows(IllegalArgumentException.class, () -> testObject4.addSection(illegalTestSection1, new IniConf()));
        assertThrows(IllegalArgumentException.class, () -> testObject4.addSection(illegalTestSection2, new IniConf()));
        assertThrows(IllegalArgumentException.class, () -> testObject4.addSection(illegalTestSection3, new IniConf()));
    }

    @Test
    void getSectionTest() {
        IniConf testObject5 = testObject.getSection("section3");
        assertNotNull(testObject5);
        assertFalse(testObject5.isEmpty());
        assertTrue(testObject5.isKey("key6"));
        assertTrue(testObject5.isKey("subsection1", "key"));
        assertTrue(testObject5.isSection("subsection1.subsubsection2"));
        assertEquals("this is value 6", testObject5.get("key6"));
        assertEquals("value9", testObject5.get("subsection1.subsubsection2", "key9"));
        assertEquals("default", testObject5.getOrDefault("subsection1.subsubsection2",
                "key12", "default"));
    }

}
