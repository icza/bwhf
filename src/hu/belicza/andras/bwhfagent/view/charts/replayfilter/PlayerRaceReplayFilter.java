package hu.belicza.andras.bwhfagent.view.charts.replayfilter;

import java.util.Collection;

import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;

/**
 * Filters replays by player races.
 * 
 * @author Andras Belicza
 */
public class PlayerRaceReplayFilter extends NumberPropertySetReplayFilter {
	
	/**
	 * Creates a new PlayerRaceReplayFilter.
	 * @param validPlayerRaces valid player races 
	 */
	public PlayerRaceReplayFilter( final Collection< Byte > validPlayerRaces ) {
		super( validPlayerRaces );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		final ReplayHeader replayHeader = replay.replayHeader;
		for ( int i = replayHeader.playerRaces.length - 1; i >= 0; i-- )
			if ( replayHeader.playerNames[ i ] != null && isValueValid( replayHeader.playerRaces[ i ] ) )
				return true;
		
		return false;
	}
	
}
