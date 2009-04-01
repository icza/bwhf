package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

import java.util.Collection;

/**
 * Filters replays by game type.
 * 
 * @author Andras Belicza
 */
public class GameTypeReplayFilter extends PropertySetReplayFilter {
	
	/**
	 * Creates a new GameTypeReplayFilter.
	 * @param validGameTypes valid game types 
	 */
	public GameTypeReplayFilter( final Collection< Short > validGameTypes ) {
		super( COMPLEXITY_NUMBER_SET, validGameTypes );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return isValueValid( replay.replayHeader.gameType );
	}
	
}
