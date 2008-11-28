package hu.belicza.andras.bwhf.control;

/**
 * Refers to an error parsing a replay text.
 * 
 * @author Belicza Andras
 */
public class ParseException extends Exception {
	
	/**
	 * Creates a new ParseException.<br>
	 * This constructor should be used if the parser failes to read the source.
	 */
	public ParseException() {
		super( "Error reading the source." );
	}
	
	/**
	 * Creates a new ParseException.<br>
	 * This constructor should be used if the source contains invalid content.
	 * @param line line where the parse failed
	 */
	public ParseException( final int line ) {
		super( "Parse error in line " + line + "." );
	}
	
}
