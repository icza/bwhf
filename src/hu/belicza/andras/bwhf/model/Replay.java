package hu.belicza.andras.bwhf.model;

/**
 * Class modeling a replay.
 * 
 * @author Belicza Andras
 */
public class Replay {
	
	public final ReplayHeader  replayHeader;
	public final ReplayActions replayActions;
	
	public Replay( final ReplayHeader replayHeader, final ReplayActions replayActions ) {
		this.replayHeader  = replayHeader;
		this.replayActions = replayActions;
	}
	
}
