package net.prsv.iniconf;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniConfReader {

    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\s*\\[(.*)\\]\\s$");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^;.*$");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("^\\s*(\\w*)\\s*=\\s*['\"]?(.*?)['\"]?\\s*$");

    private static boolean error = false;

    // do not instantiate
    private IniConfReader() {}

    public static IniConfDict read(String filename) {
        File inputFile = new File(filename);
        String chunk = "";
        try (Scanner s = new Scanner(inputFile)) {
            chunk = s.useDelimiter("\\Z").next();
        } catch (IOException e) {
            error = true;
        }
        if (error) {
            return null;
        }
        return parse(chunk);
    }

    private static IniConfDict parse(String chunk) {

        String[] lines = chunk.split("\\R");
        IniConfDict result = new IniConfDict();
        for (String line: lines) {
            Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
            Matcher propertyMatcher = PROPERTY_PATTERN.matcher(line);
            Matcher sectionMatcher = SECTION_PATTERN.matcher(line);
        }
        return null;
    }

}
