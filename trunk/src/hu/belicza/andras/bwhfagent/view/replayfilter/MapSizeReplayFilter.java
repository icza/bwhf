package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhfagent.view.ReplaySearchTab.MapSize;

/**
 * Filters replays by map size.
 * 
 * @author Andras Belicza
 */
public class MapSizeReplayFilter extends ReplayFilter {
	
	/** The number interval filter for the width of the map.  */
	private final NumberIntervalReplayFilter mapWidthNumberIntervalReplayFilter;
	/** The number interval filter for the height of the map. */
	private final NumberIntervalReplayFilter mapHeightNumberIntervalReplayFilter;
	
	/**
	 * Creates a new MapSizeReplayFilter.
	 * @param minMapSize min valid map size
	 * @param maxMapSize max valid map size
	 */
	public MapSizeReplayFilter( final MapSize minMapSize, final MapSize maxMapSize ) {
		super( COMPLEXITY_NUMBER_PAIR_INTERVAL );
		
		mapWidthNumberIntervalReplayFilter = new NumberIntervalReplayFilter( minMapSize == null ? null : minMapSize.width, maxMapSize == null ? null : maxMapSize.width ) {
			@Override
			public boolean isReplayIncluded( final Replay replay ) {
				throw new RuntimeException( "This method should not be used!" );
			}
		};
		mapHeightNumberIntervalReplayFilter = new NumberIntervalReplayFilter( minMapSize == null ? null : minMapSize.height, maxMapSize == null ? null : maxMapSize.height ) {
			@Override
			public boolean isReplayIncluded( final Replay replay ) {
				throw new RuntimeException( "This method should not be used!" );
			}
		};
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return mapWidthNumberIntervalReplayFilter .isValueValid( replay.replayHeader.mapWidth  )
			&& mapHeightNumberIntervalReplayFilter.isValueValid( replay.replayHeader.mapHeight );
	}
	
}
