package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Filters replays by map name.
 * 
 * @author Andras Belicza
 */
public class MapNameReplayFilter extends StringPropertyReplayFilter {
	
	/**
	 * Creates a new MapNameReplayFilter.
	 * @param validMapName string defining the valid game names
	 * @param exactMatch   tells if exact match is required or substring match is allowed
	 * @param regexp       tells if <code>validMapName</code> is a regexp or a comma separated list string
	 */
	public MapNameReplayFilter( final String validMapName, final boolean exactMatch, final boolean regexp ) {
		super( validMapName, exactMatch, regexp );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return isValueValid( replay.replayHeader.mapName );
	}
	
}
