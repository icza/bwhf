package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Filters replays by player name.
 * 
 * @author Andras Belicza
 */
public class PlayerNameReplayFilter extends StringPropertyReplayFilter {
	
	/**
	 * Creates a new PlayerNameReplayFilter.
	 * @param validPlayerName string defining the valid game names
	 * @param exactMatch    tells if exact match is required or substring match is allowed
	 * @param regexp        tells if <code>validPlayerName</code> is a regexp or a comma separated list string
	 */
	public PlayerNameReplayFilter( final String validPlayerName, final boolean exactMatch, final boolean regexp ) {
		super( validPlayerName, exactMatch, regexp );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		for ( final String playerName : replay.replayHeader.playerNames )
			if ( playerName != null && isValueValid( playerName ) )
				return true;
		
		return false;
	}
	
}
