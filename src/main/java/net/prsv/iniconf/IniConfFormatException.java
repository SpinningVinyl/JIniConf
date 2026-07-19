package net.prsv.iniconf;

import java.io.Serial;

/**
 * Thrown when INI input contains malformed content.
 */
public final class IniConfFormatException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 22936806L;

    /** One-based line number containing the malformed content. */
    private final int lineNumber;

    /**
     * Constructs an exception describing malformed INI content at the specified line.
     * @param lineNumber one-based line number containing the malformed content
     * @param detail description of the format error
     */
    public IniConfFormatException(int lineNumber, String detail) {
        super("Invalid INI format at line " + lineNumber + ": " + detail);
        if (lineNumber < 1) {
            throw new IllegalArgumentException("lineNumber must be positive");
        }
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the one-based line number containing the malformed content.
     * @return the line number containing the malformed content
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
