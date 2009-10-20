package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Filters replays by creator name.
 * 
 * @author Andras Belicza
 */
public class CreatorNameReplayFilter extends StringPropertyReplayFilter {
	
	/**
	 * Creates a new CreatorNameReplayFilter.
	 * @param validCreatorName string defining the valid game names
	 * @param exactMatch       tells if exact match is required or substring match is allowed
	 * @param regexp           tells if <code>validCreatorName</code> is a regexp or a comma separated list string
	 */
	public CreatorNameReplayFilter( final String validCreatorName, final boolean exactMatch, final boolean regexp ) {
		super( validCreatorName, exactMatch, regexp );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return isValueValid( replay.replayHeader.creatorName );
	}
	
}
