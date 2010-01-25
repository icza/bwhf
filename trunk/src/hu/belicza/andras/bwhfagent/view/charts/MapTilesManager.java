package hu.belicza.andras.bwhfagent.view.charts;

import swingwt.awt.Image;
import swingwt.awt.image.BufferedImage;
import swingwtx.swing.ImageIcon;

/**
 * Manages the map tile images.
 * 
 * @author Andras Belicza
 */
public class MapTilesManager {
	
	/** Badlands tile set. */
	public static final int TILE_SET_BADLANDS       = 0;
	/** Space platform tile set.  */
	public static final int TILE_SET_SPACE_PLATFORM = 1;
	/** Installation tile set.    */
	public static final int TILE_SET_INSTALLATION   = 2;
	/** Ashworld tile set.        */
	public static final int TILE_SET_ASHWORLD       = 3;
	/** Jungle world tile set.    */
	public static final int TILE_SET_JUNGLE_WORLD   = 4;
	/** Desert world tile set.    */
	public static final int TILE_SET_DESERT_WORLD   = 5;
	/** Arctic world tile set.   */
	public static final int TILE_SET_ARCTIC_WORLD   = 6;
	/** Twilight world tile set. */
	public static final int TILE_SET_TWILIGHT_WORLD = 7;
	
	/** All images of the map tiles. Loaded only once.                  */
	private static Image mapTilesImage;
	/** Scaled Images of the map tiles. Cache of the result of scaling. */
	private static Image scaledMapTilesImage;
	/** The scale factor of the cached scaled tile images.              */
	private static int   scaleFactor = -1;
	
	/** Width of the tiles image.  */
	private static final int TILE_IMAGE_WIDTH  = 32;
	/** Height of the tiles image. */
	private static final int TILE_IMAGE_HEIGHT = 32;
	
	/** Number of tile sets.               */
	private static final int TILE_SETS_COUNT = 8;
	/** Max number of tiles in a tile set. */
	private static final int MAX_TILES_COUNT = 16;
	
	/** Scaled tile images of the different tiles. Cache of the result of scaling. */
	private static BufferedImage[][] scaledTileImages = new BufferedImage[ TILE_SETS_COUNT ][];
	
	/**
	 * Returns the scaled versions of the tile images of the specified tile set.
	 * @param tileSet tile set whose tile images to be returned
	 * @param zoom    zoom value (if zoom = tile size => scale factor=1.0)
	 * @return the scaled versions of the tile images of the specified tile set
	 */
	public static BufferedImage[] getTileSetScaledImages( final int tileSet, final int zoom ) {
		if ( scaleFactor != zoom ) {
			// We only scale if no scaled image yet or zoom changed
			if ( mapTilesImage == null )
				mapTilesImage = new ImageIcon( MapTilesManager.class.getResource( "map_tiles.png" ) ).getImage(); // We only load this once on demand.
			
			scaledMapTilesImage = mapTilesImage.getScaledInstance( mapTilesImage.getWidth() * zoom / TILE_IMAGE_WIDTH, mapTilesImage.getHeight() * zoom / TILE_IMAGE_HEIGHT, Image.SCALE_AREA_AVERAGING );
			
			scaledMapTilesImage = new BufferedImage( mapTilesImage.getWidth() * zoom / TILE_IMAGE_WIDTH, mapTilesImage.getHeight() * zoom / TILE_IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB );
			scaledMapTilesImage.getGraphics().drawImage( mapTilesImage, 0, 0, scaledMapTilesImage.getWidth(), scaledMapTilesImage.getHeight(), null );
			
			// Zoom has changed, discard the cached scaled tile images
			for ( int i = 0; i < scaledTileImages.length; i ++ )
				scaledTileImages[ i ] = null;
			
			scaleFactor = zoom;
		}
		
		if ( scaledTileImages[ tileSet ] == null ) {
			// We only scale the required tile set images 
			scaledTileImages[ tileSet ] = new BufferedImage[ MAX_TILES_COUNT ];
			for ( int i = 0; i < scaledTileImages[ tileSet ].length; i++ ) {
				scaledTileImages[ tileSet ][ i ] = new BufferedImage( zoom, zoom, BufferedImage.TYPE_INT_ARGB );
				scaledTileImages[ tileSet ][ i ].getGraphics().drawImage( scaledMapTilesImage, 0, 0, zoom, zoom,
						i * zoom, tileSet * zoom, i * zoom + zoom, tileSet * zoom + zoom, null );
			}
		}
		
		return scaledTileImages[ tileSet ];
	}
	
}
