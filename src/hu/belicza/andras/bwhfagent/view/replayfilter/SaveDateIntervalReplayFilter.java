package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Filters replays by replay save date.
 * 
 * @author Andras Belicza
 */
public class SaveDateIntervalReplayFilter extends NumberIntervalReplayFilter {
	
	/**
	 * Creates a new SaveDateIntervalReplayFilter.
	 * @param minSaveDate min valid save date 
	 * @param maxSaveDate max valid save date 
	 */
	public SaveDateIntervalReplayFilter( final Long minSaveDate, final Long maxSaveDate ) {
		super( minSaveDate, maxSaveDate );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return isValueValid( replay.replayHeader.saveTime.getTime() );
	}
	
}
