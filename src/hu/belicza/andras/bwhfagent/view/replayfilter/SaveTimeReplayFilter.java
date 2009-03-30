package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Filters replays by save time.
 * 
 * @author Andras Belicza
 */
public class SaveTimeReplayFilter extends NumberIntervalReplayFilter {
	
	/**
	 * Creates a new SaveTimeReplayFilter.
	 * @param minSaveTime min valid save time
	 * @param maxSaveTime max valid save time
	 */
	public SaveTimeReplayFilter( final Long minSaveTime, final Long maxSaveTime ) {
		super( minSaveTime, maxSaveTime );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return isValueValid( replay.replayHeader.saveTime.getTime() );
	}
	
}
