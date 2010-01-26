package hu.belicza.andras.bwhf.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class modeling some data about the map.
 * 
 * @author Belicza Andras
 */
public class MapData {
	
	/** Defines the tile set of the map. */
	public short   tileSet = -1;
	
	/** Map tile data: width x height elements. */
	public short[] tiles;
	
	/** Mineral positions on the map. */
	public List< short[] > mineralFieldList = new ArrayList< short[] >();
	
	/** Vespene geyser positions on the map. */
	public List< short[] > geyserList       = new ArrayList< short[] >();
	
}
