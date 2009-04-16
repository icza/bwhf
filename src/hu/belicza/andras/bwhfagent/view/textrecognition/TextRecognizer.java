package hu.belicza.andras.bwhfagent.view.textrecognition;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Text recognizer class.
 * 
 * @author Andras Belicza
 */
public class TextRecognizer {
	
	/** Width of the valid screenshot.  */
	private static final int VALID_SCREENSHOT_WIDTH  = 640;
	/** Height of the valid screenshot. */
	private static final int VALID_SCREENSHOT_HEIGHT = 480;
	
	/** RGB value of the color of inactive player frames in the game lobby. */
	private static final int PLAYER_FRAME_RGB = new Color( 149, 0, 0 ).getRGB(); // Active frame has a color of new Color( 253, 0, 0 ) 
	
	/** y coordinate of the first player slot frame. */
	private static final int FIRST_PLAYER_SLOT_FRAME_Y = 35;
	/** x coordinate of the player slot frames.      */
	private static final int PLAYER_SLOT_FRAME_X       = 52;
	/** Height of the player slots.                  */
	private static final int PLAYER_SLOT_HEIGHT        = 16;
	/** Gap between player slots.                    */
	private static final int GAP_BETWEEN_PLAYER_SLOTS  = 2;
	
	/**
	 * Tries to read player names from a game lobby screenshot.
	 * @param image screenshot image of the game lobby
	 * @return <code>null</code> if the image is not a game lobby screenshot, else an array of player names read from the image
	 */
	public static String[] readPlayerNamesFromGameLobbyImage( final BufferedImage image ) {
		if ( !isGameLobbyScreenshot( image ) )
			return null;
		
		final List< String > playerNames = new ArrayList< String >();
		for ( int slot = 0; slot < 12; slot++ )
			if ( isPlayerSlotPlayer( slot, image ) ) {
				String playerName = readString( PLAYER_SLOT_FRAME_X + 4, FIRST_PLAYER_SLOT_FRAME_Y + ( PLAYER_SLOT_HEIGHT + GAP_BETWEEN_PLAYER_SLOTS ) * slot + 2, image );
				if ( playerName != null ) {
					playerName = playerName.toLowerCase();
					if ( !playerName.equals( "open" ) && !playerName.equals( "closed" ) && !playerName.equals( "computer" ) )
						playerNames.add( playerName );
				}
			}
		
		return playerNames.toArray( new String[ playerNames.size() ] );
	}
	
	/**
	 * Checks if an image is a game lobby screenshot
	 * @param image image to be checked
	 * @return true if the image is a game lobby screenshot; false otherwise
	 */
	private static boolean isGameLobbyScreenshot( final BufferedImage image ) {
		if ( image.getWidth() != VALID_SCREENSHOT_WIDTH || image.getHeight() != VALID_SCREENSHOT_HEIGHT )
			return false;
		
		// We check if the first game slot frame is present
		for ( int x = PLAYER_SLOT_FRAME_X + 2; x < PLAYER_SLOT_FRAME_X + 2 + 15; x++ )
			if ( image.getRGB( x, FIRST_PLAYER_SLOT_FRAME_Y ) != PLAYER_FRAME_RGB )
				return false;
		
		return true;
	}
	
	/**
	 * Checks if a player slot is used for player
	 * @param slot slot to be checked
	 * @param image screenshot image of the game lobby
	 * @return true if the specified slot is for player; false otherwise
	 */
	private static boolean isPlayerSlotPlayer( final int slot, final BufferedImage image ) {
		// Player slots are more idented than non-player slots.
		final int y1 = FIRST_PLAYER_SLOT_FRAME_Y + ( PLAYER_SLOT_HEIGHT + GAP_BETWEEN_PLAYER_SLOTS ) * slot + 2;
		
		for ( int y = y1 + PLAYER_SLOT_HEIGHT - 5; y >= y1; y-- )
			if ( image.getRGB( PLAYER_SLOT_FRAME_X, y ) != PLAYER_FRAME_RGB )
				return false;
		
		return true;
	}
	
	/**
	 * Tries to read a string from an image starting at the specified position.
	 * @param x     x coordinate of the start position
	 * @param y     y coordinate of the start position
	 * @param image image to be read from
	 * @return <code>null</code> if string could not be parsed (due to unrecognizable chars); else the recognized string
	 */
	private static String readString( int x, final int y, final BufferedImage image ) {
		final StringBuilder recognizedStringBuilder = new StringBuilder();
		final int[] rgbBuffer = new int[ CharDef.HEIGHT * CharDef.MAX_CHAR_WIDTH ];
		
		while ( true ) {
			// First determine the widht of the next char
			int width = 0;
			for ( int xPos = x; x < VALID_SCREENSHOT_WIDTH; xPos++ )
				if ( CharDef.isCharColumnEmpty( xPos, y, image ) ) {
					width = xPos - x;
					break;
				}
			
			if ( width == 0 ) // We do not handle spaces yet.
				break;
			
			image.getRGB( x, y, width, CharDef.HEIGHT, rgbBuffer, 0, CharDef.MAX_CHAR_WIDTH );
			
			boolean recognizedChar = false;
			for ( final CharDef charDef : CharDef.CHAR_WIDTH_CHAR_DEFS_MAP.get( width ) ) {
				
				// Check if charDef matches the picture at the given location
				boolean charDefMatches = true;
				charCheck:
				for ( int charY = 0; charY < CharDef.HEIGHT; charY++ ) {
					final byte[] imageDataRow = charDef.imageData[ charY ];
					int rgbBufferIndex = charY * CharDef.MAX_CHAR_WIDTH + width - 1;
					for ( int charX = width - 1; charX >= 0; charX--, rgbBufferIndex-- ) {
						final byte colorIndex = imageDataRow[ charX ];
						final int  pixelRgb   = rgbBuffer[ rgbBufferIndex ]; 
						
						if ( colorIndex == (byte) 0x00 ) // If no pixel in charDef, no valid char color may occur
							for ( int colorIndex2 = CharDef.CHAR_IMAGE_RGBS.length - 1; colorIndex2 > 1; colorIndex2-- ) {
								if ( pixelRgb == CharDef.CHAR_IMAGE_RGBS[ colorIndex2 ] ) {
									charDefMatches = false;
									break charCheck;
								}
							}
						else                             // Pixel in charDef must match the picture
							if ( pixelRgb != CharDef.CHAR_IMAGE_RGBS[ colorIndex ] ) {
								charDefMatches = false;
								break charCheck;
							}
					}
				}
				
				if ( charDefMatches ) {
					recognizedChar = true;
					recognizedStringBuilder.append( charDef.associatedChar );
					break;
				}
			}
			
			if ( !recognizedChar )
				return null;
			
			x += width + 1;
		}
		
		return recognizedStringBuilder.toString();
	}
	
}
