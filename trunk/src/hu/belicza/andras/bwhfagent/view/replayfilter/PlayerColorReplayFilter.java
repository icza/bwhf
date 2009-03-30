package hu.belicza.andras.bwhfagent.view.replayfilter;

import java.util.Collection;

import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;

/**
 * Filters replays by player colors.
 * 
 * @author Andras Belicza
 */
public class PlayerColorReplayFilter extends NumberPropertySetReplayFilter {
	
	/**
	 * Creates a new PlayerColorReplayFilter.
	 * @param validPlayerColors valid player colors
	 */
	public PlayerColorReplayFilter( final Collection< Integer > validPlayerColors ) {
		super( validPlayerColors );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		final ReplayHeader replayHeader = replay.replayHeader;
		for ( int i = replayHeader.playerRaces.length - 1; i >= 0; i-- )
			if ( replayHeader.playerNames[ i ] != null && isValueValid( replayHeader.playerColors[ i ] ) )
				return true;
		
		return false;
	}
	
}
