package hu.belicza.andras.bwhfagent.view.charts;

import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.Action.Size;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;

/**
 * Manages the images required to render the map.
 * 
 * @author Andras Belicza
 */
public class MapImagesManager {
	
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
	
	/** Width of the tiles image.  */
	public static final int TILE_IMAGE_WIDTH  = 32;
	/** Height of the tiles image. */
	public static final int TILE_IMAGE_HEIGHT = 32;
	
	/** Number of tile sets.               */
	private static final int TILE_SETS_COUNT = 8;
	/** Max number of tiles in a tile set. */
	private static final int MAX_TILES_COUNT = 16;
	
	/** Width of the buildings image.  */
	public static final int BUILDING_IMAGE_WIDTH  = 36;
	/** Height of the buildings image. */
	public static final int BUILDING_IMAGE_HEIGHT = 34;
	
	/** Positions of the images of the different buildings in the big pictures. */
	private static final List< ? >[] BUILDING_POSITION_LIST = new List[] {
		// Zerg
		Arrays.asList( (short) 0x82, (short) 0x83, (short) 0x85, (short) 0x84, (short) 0x86, (short) 0x87, (short) 0x88, (short) 0x89, (short) 0x8A, (short) 0x8B, (short) 0x8C, (short) 0x8D, (short) 0x8E, (short) 0x8F, (short) 0x90, (short) 0x92, (short) 0x95 ),
		// Terran
		Arrays.asList( (short) 0x6A, (short) 0x6D, (short) 0x6E, (short) 0x6F, (short) 0x70, (short) 0x71, (short) 0x72, (short) 0x74, (short) 0x7A, (short) 0x7B, (short) 0x7C, (short) 0x7D, (short) 0x6B, (short) 0x6C, (short) 0x73, (short) 0x75, (short) 0x76, (short) 0x78 ),
		// Protoss
		Arrays.asList( (short) 0x9A, (short) 0x9B, (short) 0x9C, (short) 0x9D, (short) 0x9F, (short) 0xA0, (short) 0xA2, (short) 0xA3, (short) 0xA4, (short) 0xA5, (short) 0xA6, (short) 0xA7, (short) 0xA9, (short) 0xAA, (short) 0xAB, (short) 0xAC )
	};
	
	/** All images of the map tiles. Loaded only once.         */
	private static Image mapTilesImage;
	/** The scale factor of the cached scaled tile images.     */
	private static int   tileScaleFactor = -1;
	
	/** All images of the buildings. Loaded only once.         */
	private static Image buildingsImage;
	/** The scale factor of the cached scaled building images. */
	private static int   buildingScaleFactor = -1;
	
	/** Scaled tile images of the different tiles. Cache of the result of scaling.     */
	private static BufferedImage[][] scaledTileImages     = new BufferedImage[ TILE_SETS_COUNT ][];
	
	/** Scaled building images of the different races. Cache of the result of scaling. */
	private static BufferedImage[][] scaledBuildingImages = new BufferedImage[ BUILDING_POSITION_LIST.length ][];
	
	/**
	 * Returns the scaled versions of the tile images of the specified tile set.
	 * @param tileSet tile set whose tile images to be returned
	 * @param zoom    zoom value (if zoom = tile size => scale factor=1.0)
	 * @return the scaled versions of the tile images of the specified tile set
	 */
	public static BufferedImage[] getTileSetScaledImages( final int tileSet, final int zoom ) {
		if ( tileScaleFactor != zoom ) {
			// We only scale if no scaled image yet or zoom changed
			if ( mapTilesImage == null )
				mapTilesImage = new ImageIcon( MapImagesManager.class.getResource( "map_tiles.png" ) ).getImage(); // We only load this once on demand.
			
			// Zoom has changed, discard the cached scaled tile images
			for ( int i = 0; i < scaledTileImages.length; i ++ ) {
				if ( scaledTileImages[ i ] != null ) {
					for ( final BufferedImage bi : scaledTileImages[ i ] )
						bi.flush();
					scaledTileImages[ i ] = null;
				}
			}
			
			tileScaleFactor = zoom;
		}
		
		if ( scaledTileImages[ tileSet ] == null ) {
			// We only scale the required tile set images 
			scaledTileImages[ tileSet ] = new BufferedImage[ MAX_TILES_COUNT ];
			for ( int i = 0; i < scaledTileImages[ tileSet ].length; i++ ) {
				scaledTileImages[ tileSet ][ i ] = new BufferedImage( zoom, zoom, BufferedImage.TYPE_INT_RGB );
				scaledTileImages[ tileSet ][ i ].getGraphics().drawImage( mapTilesImage, 0, 0, zoom, zoom,
						i * TILE_IMAGE_WIDTH, tileSet * TILE_IMAGE_HEIGHT, i * TILE_IMAGE_WIDTH + TILE_IMAGE_WIDTH, tileSet * TILE_IMAGE_HEIGHT + TILE_IMAGE_HEIGHT, null );
			}
		}
		
		return scaledTileImages[ tileSet ];
	}
	
	/**
	 * Returns the scaled version of the specified building.
	 * @param race       race of the building
	 * @param buildingId id of the building
	 * @param zoom       zoom value (if zoom = building size => scale factor=1.0)
	 * @return the scaled versions of the specified building
	 */
	public static BufferedImage getBuildingScaledImage( final int race, final short buildingId, final int zoom ) {
		if ( race < 0 || race > 2 )
			return null;
		final int buildingIndex = BUILDING_POSITION_LIST[ race ].indexOf( buildingId );
		if ( buildingIndex < 0 )
			return null;
		
		if ( buildingScaleFactor != zoom ) {
			// We only scale if no scaled image yet or zoom changed
			if ( buildingsImage == null )
				buildingsImage = new ImageIcon( MapImagesManager.class.getResource( "buildings.png" ) ).getImage(); // We only load this once on demand.
			
			// Zoom has changed, discard the cached scaled building images
			for ( int i = 0; i < scaledBuildingImages.length; i ++ ) {
				if ( scaledBuildingImages[ i ] != null ) {
					for ( final BufferedImage bi : scaledBuildingImages[ i ] )
						bi.flush();
					scaledBuildingImages[ i ] = null;
				}
			}
			
			buildingScaleFactor = zoom;
		}
		
		if ( scaledBuildingImages[ race ] == null ) {
			// We only scale the required race building images 
			scaledBuildingImages[ race ] = new BufferedImage[ BUILDING_POSITION_LIST[ race ].size() ];
			for ( int i = 0; i < scaledBuildingImages[ race ].length; i++ ) {
				final Size size = Action.BUILDING_ID_SIZE_MAP.get( BUILDING_POSITION_LIST[ race ].get( i ) );
				scaledBuildingImages[ race ][ i ] = new BufferedImage( size.width * zoom-1, size.height * zoom-1, BufferedImage.TYPE_INT_RGB );
				scaledBuildingImages[ race ][ i ].getGraphics().drawImage( buildingsImage, 0, 0, size.width * zoom-1, size.height * zoom-1,
						race * BUILDING_IMAGE_WIDTH+2, i * BUILDING_IMAGE_HEIGHT+1, race * BUILDING_IMAGE_WIDTH + BUILDING_IMAGE_WIDTH-4, i * BUILDING_IMAGE_HEIGHT + BUILDING_IMAGE_HEIGHT-2, null );
			}
		}
		
		return scaledBuildingImages[ race ][ buildingIndex ];
	}
	
}
