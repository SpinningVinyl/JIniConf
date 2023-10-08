package net.prsv.iniconf;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniConfReader {

    private static final Pattern COMMENT_PATTERN = Pattern.compile("^[;#].*$");
    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\s*\\[([\\w.]+)]\\s*$");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("^\\s*(\\w*)\\s*=\\s*['\"]?(.*?)['\"]?\\s*$");

    private static boolean error = false;

    // do not instantiate
    private IniConfReader() {}

    /**
     * Reads the specified file and tries to parse it as an INI file. Returns an {@link IniConfDict} object or {@code null}
     * if an {@link IOException} occurs while reading the file.
     * @param filename name of the input file
     * @return the resulting {@link IniConfDict} object
     */
    public static IniConfDict read(String filename) {
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
        return parse(chunk);
    }

    /**
     * Parses the specified string and returns the resulting {@link IniConfDict} object.
     * @param input {@link String} to be parsed
     * @return the resulting {@link IniConfDict} object
     */
    public static IniConfDict parse(String input) {

        String[] lines = input.split("\\R");
        IniConfDict result = new IniConfDict();
        IniConfDict currentDict = result;
        String currentSection;

        for (String line: lines) {
            Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
            Matcher sectionMatcher = SECTION_PATTERN.matcher(line);
            Matcher propertyMatcher = PROPERTY_PATTERN.matcher(line);
            if (commentMatcher.find()) {
                continue; // comment -- skip the line
            }
            if (sectionMatcher.find()) {
                currentDict = result;
                currentSection = sectionMatcher.group(1).toLowerCase();
                String[] sectionPath = currentSection.split("\\.");
                for (String section : sectionPath) {
                    if (currentDict.getSubsection(section) == null) {
                        currentDict.addSubsection(section, new IniConfDict());
                    }
                    currentDict = currentDict.getSubsection(section);
                }
            }
            if (propertyMatcher.find()) {
                currentDict.put(propertyMatcher.group(1).toLowerCase(), propertyMatcher.group(2));
            }
        }
        return result;
    }

}
