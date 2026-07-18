package net.prsv.iniconf.test;

import net.prsv.iniconf.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

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
        Optional<IniConf> readObject = IniConfReader.read(testFilePath);
        assertTrue(readObject.isPresent());
        testObject = readObject.get();
    }

    @Test
    void constructorWithInputTest() {
        assertFalse(testObject.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "[section.]",
            "[.section]",
            "[section..subsection]",
            "[section name]",
            "[section"
    })
    void constructorRejectsInvalidSectionHeader(String header) {
        String input = "[valid]\nkey = value\n" + header + "\nother = value";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new IniConf(input));

        assertTrue(exception.getMessage().contains("line 3"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "section",
            "section_1.subsection2",
            "level1.level2.level3"
    })
    void sectionPathMethodsAcceptValidPaths(String path) {
        IniConf iniConf = new IniConf();

        assertDoesNotThrow(() -> iniConf.put(path, "key", "value"));
        assertEquals("value", iniConf.get(path, "key"));
        assertEquals("value", iniConf.getOrDefault(path, "key", "default"));
        assertTrue(iniConf.isKey(path, "key"));
        assertTrue(iniConf.isSection(path));
        assertNotNull(iniConf.getSection(path));

        IniConf sectionContainer = new IniConf();
        assertDoesNotThrow(() -> sectionContainer.addSection(path, new IniConf()));
        assertTrue(sectionContainer.isSection(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            ".section",
            "section.",
            "section..subsection",
            "section name",
            "section/subsection"
    })
    void sectionPathMethodsRejectInvalidPaths(String path) {
        IniConf iniConf = new IniConf();

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> iniConf.put(path, "key", "value")),
                () -> assertThrows(IllegalArgumentException.class, () -> iniConf.get(path, "key")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> iniConf.getOrDefault(path, "key", "default")),
                () -> assertThrows(IllegalArgumentException.class, () -> iniConf.isKey(path, "key")),
                () -> assertThrows(IllegalArgumentException.class, () -> iniConf.isSection(path)),
                () -> assertThrows(IllegalArgumentException.class, () -> iniConf.getSection(path)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> iniConf.addSection(path, new IniConf()))
        );
    }

    @Test
    void sectionPathMethodsRejectNullPaths() {
        IniConf iniConf = new IniConf();

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> iniConf.put(null, "key", "value")),
                () -> assertThrows(NullPointerException.class, () -> iniConf.get(null, "key")),
                () -> assertThrows(NullPointerException.class,
                        () -> iniConf.getOrDefault(null, "key", "default")),
                () -> assertThrows(NullPointerException.class, () -> iniConf.isKey(null, "key")),
                () -> assertThrows(NullPointerException.class, () -> iniConf.isSection(null)),
                () -> assertThrows(NullPointerException.class, () -> iniConf.getSection(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> iniConf.addSection(null, new IniConf()))
        );
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
        Optional<IniConf> readObject = IniConfReader.read(testFilePath);
        assertTrue(readObject.isPresent());
        IniConf testObject2 = readObject.get();
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
    void putNormalizesValues() {
        IniConf iniConf = new IniConf();

        assertNull(iniConf.put("key", "  value with internal spaces  "));
        assertEquals("value with internal spaces", iniConf.get("key"));
        assertEquals("value with internal spaces", iniConf.put("key", "\t\u2003replacement\u2003 "));
        assertEquals("replacement", iniConf.get("key"));
        assertNull(iniConf.put("empty", " \t\u2003 "));
        assertEquals("", iniConf.get("empty"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"value\nnext", "value\rnext", "value\u000Bnext", "value\u000Cnext",
            "value\u0085next", "value\u2028next", "value\u2029next", "value\u0000next"})
    void putRejectsInvalidValues(String value) {
        IniConf iniConf = new IniConf();

        assertThrows(IllegalArgumentException.class, () -> iniConf.put("key", value));
        assertFalse(iniConf.isKey("key"));
    }

    @Test
    void putRejectsNullValue() {
        IniConf iniConf = new IniConf();

        assertThrows(NullPointerException.class, () -> iniConf.put("key", null));
        assertFalse(iniConf.isKey("key"));
    }

    @Test
    void parserUsesPutValueNormalizationAndValidation() {
        IniConf iniConf = new IniConf("key =   value with spaces   ");

        assertEquals("value with spaces", iniConf.get("key"));
        assertThrows(IllegalArgumentException.class, () -> new IniConf("key = value\u0000"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"value", "value-123", "it's"})
    void serializerWritesSimpleValuesWithoutQuotes(String value) {
        IniConf iniConf = new IniConf();
        iniConf.put("key", value);

        assertEquals("key = " + value + "\n\n", iniConf.toString());
    }

    @Test
    void serializerQuotesEmptyValues() {
        IniConf iniConf = new IniConf();
        iniConf.put("key", "");

        assertEquals("key = \"\"\n\n", iniConf.toString());
    }

    @Test
    void serializerQuotesValuesContainingInternalWhitespace() {
        IniConf iniConf = new IniConf();
        iniConf.put("key", "value with\tinternal whitespace");

        assertEquals("key = \"value with\tinternal whitespace\"\n\n", iniConf.toString());
    }

    @Test
    void serializerEncodesDoubleQuotesAndBackslashes() {
        IniConf iniConf = new IniConf();
        iniConf.put("key", "C:\\directory\\\"quoted\"");

        assertEquals("key = \"C:\\\\directory\\\\\\\"quoted\\\"\"\n\n", iniConf.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "simple", "users'", "internal spaces", "double \"quotes\"",
            "C:\\directory\\file", "C:\\directory\\\"quoted\" file"})
    void serializedValuesRoundTrip(String value) {
        IniConf original = new IniConf();
        original.put("key", value);

        IniConf parsed = new IniConf(original.toString());

        assertEquals(original, parsed);
    }

    @Test
    void parserDecodesQuotedValues() {
        IniConf iniConf = new IniConf("key = \"C:\\\\directory\\\\\\\"quoted\\\"\"");

        assertEquals("C:\\directory\\\"quoted\"", iniConf.get("key"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "key = \"missing closing quote",
            "key = \"unknown \\q escape\"",
            "key = \"incomplete escape\\",
            "key = \"value\" trailing",
            "key = unquoted\\backslash",
            "key = unquoted\"quote"
    })
    void parserRejectsInvalidEncodedValues(String input) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new IniConf(input));

        assertTrue(exception.getMessage().contains("line 1"));
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
        assertThrows(NullPointerException.class, () -> testObject4.addSection("section", null));
    }

    @Test
    void addSectionRejectsRepeatedSectionInstance() {
        IniConf root = new IniConf();
        IniConf section = new IniConf();
        root.addSection("first", section);

        assertThrows(IllegalArgumentException.class, () -> root.addSection("second", section));
    }

    @Test
    void addSectionRejectsSelfReference() {
        IniConf root = new IniConf();

        assertThrows(IllegalArgumentException.class, () -> root.addSection("self", root));
    }

    @Test
    void addSectionRejectsAncestorReference() {
        IniConf root = new IniConf();
        IniConf child = new IniConf();
        root.addSection("child", child);

        assertThrows(IllegalArgumentException.class, () -> child.addSection("parent", root));
    }

    @Test
    void addSectionAllowsDistinctEqualSection() {
        IniConf root = new IniConf();
        IniConf section = new IniConf();
        assertEquals(root, section);

        assertDoesNotThrow(() -> root.addSection("child", section));
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
