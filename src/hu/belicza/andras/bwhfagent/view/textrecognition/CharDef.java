package hu.belicza.andras.bwhfagent.view.textrecognition;

/**
 * Definition of a character used in the game lobby in Starcraft.<br>
 * This font builds up the characters using 3 tones of a color. I will only model the strongest color to define
 * the outline of the characters.<br>
 * 9x9 is the biggest dimension of a char, and there can be 1 more row and column for character shadow
 * (color of weakest tone). We can enclose any character in a 10x10 square.<br>
 * The height of the characters is always <code>10</code>.
 * The <code>width</code> of the characters defines the smallest area where the character has pixels.<br>
 * <br>
 * One thing that should be known: the characters 'I' and 'l' looks exactly the same, there is no way to distinguish between them.
 * The <code>CHAR_DEFS</code> array only contains and parses these 2 letters as 'l'.
 * 
 * @author Andras Belicza
 */
public class CharDef {
	
	/** Height of the characters. */
	private static final int HEIGHT = 10;
	
	/** Width of the character in pixels.              */
	public final int  width;
	/** Height of the character in pixels.             */
	public final int  height;
	/** The associated character with this definition. */
	public final char associatedChar;
	
	/** The outline (strongest colors) of the image of the character.<br>
	 * Each element of the array describes 1 line in the characters' picture:
	 * First element is the first line, 2nd element is the second line etc.<br>
	 * An element stores the pixels in bits: first (least significant bit) is the state of the 1st pixel in the line,
	 * 2nd bit is the 2nd pixel in the line etc. Value of bit 0 means no pixel, 1 means there is an outline pixel in the position.<br>
	 * The array has a number of <code>height</code> elements which is always <code>10</code>, and elements has a number of <code>width</code> valuable bits.
	 */
	public final int[] outline;
	
	/**
	 * Creates a new CharDef.
	 * @param width          width of the character in pixels
	 * @param associatedChar the associated character with this definition
	 * @param outline        the outline of the image of the character
	 */
	public CharDef( final int width, final char associatedChar, final int[] outline ) {
		this.width          = width;
		this.height         = HEIGHT;
		this.associatedChar = associatedChar;
		this.outline        = outline;
	}
	
	/** The available and distinguishable characters in the game lobby font (does not contain 'I' because it looks the same as 'l').*/
	public static final CharDef[] CHAR_DEFS = {
		new CharDef(  6, 'a', new int[] { 0x00, 0x00, 0x0e, 0x10, 0x1e, 0x11, 0x1e, 0x00, 0x00, 0x00 } ),
		new CharDef(  6, 'b', new int[] { 0x01, 0x01, 0x0f, 0x11, 0x11, 0x11, 0x0f, 0x00, 0x00, 0x00 } ),
		new CharDef(  5, 'c', new int[] { 0x00, 0x00, 0x06, 0x09, 0x01, 0x09, 0x06, 0x00, 0x00, 0x00 } ),
		
		new CharDef(  0, ' ', new int[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 } ),
	};
	
}
