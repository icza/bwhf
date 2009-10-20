package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Filters replays by game name.
 * 
 * @author Andras Belicza
 */
public class GameNameReplayFilter extends StringPropertyReplayFilter {
	
	/**
	 * Creates a new GameNameReplayFilter.
	 * @param validGameName string defining the valid game names
	 * @param exactMatch    tells if exact match is required or substring match is allowed
	 * @param regexp        tells if <code>validGameName</code> is a regexp or a comma separated list string
	 */
	public GameNameReplayFilter( final String validGameName, final boolean exactMatch, final boolean regexp ) {
		super( validGameName, exactMatch, regexp );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return isValueValid( replay.replayHeader.gameName );
	}
	
}
