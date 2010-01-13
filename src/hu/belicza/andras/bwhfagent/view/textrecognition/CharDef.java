package hu.belicza.andras.bwhfagent.view.textrecognition;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

/**
 * Definition of a character used in the game lobby in Starcraft.<br>
 * This font builds up the characters using 4 tones of a color.<br>
 * The maximum height of the characters is <code>11</code>.
 * The <code>width</code> of the characters defines the smallest area where the character has pixels.<br>
 * If there is a space between 2 characters, there are 6 empty lines.<br>
 * <br>
 * One thing that should be known: the characters 'I' and 'l' looks exactly the same, there is no way to distinguish between them.
 * The <code>CHAR_DEFS</code> array contains and parses these 2 letters as 'l'.
 * 
 * @author Andras Belicza
 */
public class CharDef {
	
	/** Height of the characters. */
	public static final int HEIGHT = 11;
	
	/** Name of the image file containing the character definitions. */
	private static final String CHAR_DEFS_FILE_NAME = "chardefs.png";
	
	/** HSB values of the colors of the characters' image.<br>
	 * First element indicates no pixel. */
	public static final float[][] CHAR_IMAGE_HSBS = { null, Color.RGBtoHSB( 120, 216, 80, null ), Color.RGBtoHSB( 104, 179, 64, null ), Color.RGBtoHSB( 75, 135, 52, null ), Color.RGBtoHSB( 45, 85, 29, null ), Color.RGBtoHSB( 29, 38, 59, null ) };
	
	/** Width of the space character. */
	public static final int SPACE_CHAR_WIDTH = 5;
	
	/** Width of the character in pixels.              */
	public final int  width;
	/** Height of the character in pixels.             */
	public final int  height;
	/** The associated character with this definition. */
	public final char associatedChar;
	
	/** The pixels of the image of the character.<br>
	 * The value <code>(bye) 0x00</code> means no pixel.
	 */
	public final byte[][] imageData;
	
	/**
	 * Creates a new CharDef.
	 * @param width          width of the character in pixels
	 * @param associatedChar the associated character with this definition
	 * @param imageData      the pixels of the image of the character
	 */
	public CharDef( final int width, final char associatedChar, final byte[][] imageData ) {
		this.width          = width;
		this.height         = HEIGHT;
		this.associatedChar = associatedChar;
		this.imageData      = imageData;
	}
	
	/** The available and distinguishable characters in the game lobby font (does not contain 'I' because it looks the same as 'l').*/
	public static final CharDef[]                 CHAR_DEFS;
	/** The same width chars mapped to their width. */
	public static final Map< Integer, CharDef[] > CHAR_WIDTH_CHAR_DEFS_MAP = new HashMap< Integer, CharDef[] >();
	/** Maximum char width.                         */
	public static final int                       MAX_CHAR_WIDTH;
	static {
		final List< Character > charsInCharDefsPic = new ArrayList< Character >( 100 );
		for ( char ch = 'a'; ch <= 'z'; ch++ )
			charsInCharDefsPic.add( ch );
		for ( char ch = 'A'; ch <= 'Z'; ch++ )
			charsInCharDefsPic.add( ch );
		for ( char ch = '0'; ch <= '9'; ch++ )
			charsInCharDefsPic.add( ch );
		for ( final char ch : new char[] { '.', ',', ';', '_', '(', ')', '[', ']', '-', '~', '!', '@', '#', '$', '%', '\'', '`', '{', '}', '=', '§' } )
			charsInCharDefsPic.add( ch );
		
		CHAR_DEFS = new CharDef[ charsInCharDefsPic.size() ];
		final Map< Integer, List< CharDef > > charWidthCharDefListMap = new HashMap< Integer, List< CharDef > >();
		int maxCharWidth = 0;
		try {
			final BufferedImage charDefsImage = ImageIO.read( CharDef.class.getResource( CHAR_DEFS_FILE_NAME ) );
			
			int xPos = 0;
			for ( int i = 0; i < charsInCharDefsPic.size(); i++ ) {
				// First let's find out the width of the character by scanning for the empty line after that.
				int width = 0;
				for ( int x = xPos + 2; ; x++ ) // Chars are at least 2 length, we skip scanning the first 2 columns
					if ( isCharColumnEmpty( x, 0, charDefsImage ) ) {
						width = x - xPos;
						break;
					}
				
				final Character ch = charsInCharDefsPic.get( i );
				if ( ch.charValue() != 'I' ) {
					final byte[][] imageData = new byte[ HEIGHT ][ width ];
					for ( int x = xPos + width - 1; x >= xPos; x-- )
						for ( int y = HEIGHT - 1; y >= 0; y-- )
							for ( byte colorIndex = (byte) ( CHAR_IMAGE_HSBS.length - 1 ); colorIndex > (byte) 0; colorIndex-- )
								if ( doPixelsMatch( charDefsImage.getRGB( x, y ), CHAR_IMAGE_HSBS[ colorIndex ] ) ) {
									imageData[ y ][ x - xPos ] = colorIndex;
									break;
								}
					
					final CharDef charDef = new CharDef( width, ch, imageData );
					
					CHAR_DEFS[ i ] = charDef;
					List< CharDef > charDefList = charWidthCharDefListMap.get( width );
					if ( charDefList == null )
						charWidthCharDefListMap.put( width, charDefList = new ArrayList< CharDef >() );
					charDefList.add( charDef );
					if ( width > maxCharWidth )
						maxCharWidth = width;
				}
				
				xPos += width + 1;
			}
		} catch ( final IOException ie) {
			ie.printStackTrace();
		}
		
		for ( final Entry< Integer, List< CharDef > > charWidthCharDefListEntry : charWidthCharDefListMap.entrySet() ) {
			final List< CharDef > charDefList = charWidthCharDefListEntry.getValue();
			CHAR_WIDTH_CHAR_DEFS_MAP.put( charWidthCharDefListEntry.getKey(), charDefList.toArray( new CharDef[ charDefList.size() ] ) );
		}
		MAX_CHAR_WIDTH = maxCharWidth;
	}
	
	/**
	 * Check if there is an empty column at the specified position. Empty columns indicate end of the picture of a character.
	 * 
	 * @param x     x coordinate of the position
	 * @param y     y coordinate of the position
	 * @param image image in which to check
	 * @return true if there is an empty column at the specified position; false otherwise
	 */
	public static boolean isCharColumnEmpty( final int x, int y, final BufferedImage image ) {
		final int y2 = y + HEIGHT;
		
		for ( ; y < y2; y++ ) {
			int picRgb = image.getRGB( x, y );
			for ( int colorIndex = CHAR_IMAGE_HSBS.length - 1; colorIndex > 0; colorIndex-- )
				if ( doPixelsMatch( picRgb, CHAR_IMAGE_HSBS[ colorIndex ] ) )
					return false;
		}
		
		return true;
	}
	
	/**
	 * Tests if a pixel given with its rgb values match a hsb specified color disregarding their brightness.
	 * @param rgb rgb value of the pixel to be tested
	 * @param hsb hsb values of the second pixel
	 * @return true if the 2 pixels match disregarding their brightness
	 */
	public static boolean doPixelsMatch( int rgb, float[] hsb ) {
		final float[] hsb2 = Color.RGBtoHSB( ( rgb >> 16 ) & 0xff, ( rgb >> 8 ) & 0xff, rgb & 0xff, null );
		
		final float diff = hsb[ 0 ] - hsb2[ 0 ];
		
		return diff > -0.1f && diff < 0.1f;
	}
	
}
