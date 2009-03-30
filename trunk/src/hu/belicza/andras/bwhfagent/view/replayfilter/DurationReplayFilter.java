package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Filters replays by duration.
 * 
 * @author Andras Belicza
 */
public class DurationReplayFilter extends NumberIntervalReplayFilter {
	
	/**
	 * Creates a new DurationReplayFilter.
	 * @param minDuration min valid duration
	 * @param maxDuration max valid duration
	 */
	public DurationReplayFilter( final Integer minDuration, final Integer maxDuration ) {
		super( minDuration, maxDuration );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return isValueValid( replay.replayHeader.getDurationSeconds() );
	}
	
}
