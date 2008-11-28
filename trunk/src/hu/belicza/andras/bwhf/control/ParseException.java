package hu.belicza.andras.bwhf.control;

/**
 * Refers to an error parsing a replay text.
 * 
 * @author Belicza Andras
 */
public class ParseException extends Exception {
	
	/** Line where the parse failed. */
	private final Integer line;
	
	/**
	 * Creates a new ParseException.<br>
	 * This constructor should be used if the parser failes to read the source.
	 * @param line line where the parse failed
	 */
	public ParseException() {
		line = null;
	}
	
	/**
	 * Creates a new ParseException.<br>
	 * This constructor should be used if the source contains invalid content.
	 * @param line line where the parse failed
	 */
	public ParseException( final int line ) {
		this.line = line;
	}
	
	@Override
	public String getMessage() {
		return line == null ? "Error reading the source." : "Parse error in line " + line + ".";
	}
	
}
