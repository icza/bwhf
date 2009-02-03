package hu.belicza.andras.bwhf.model;

/**
 * Class modeling a replay.
 * 
 * @author Belicza Andras
 */
public class Replay {
	
	public final ReplayHeader  replayHeader;
	public final ReplayActions replayActions;
	public final String        gameChat;
	
	public Replay( final ReplayHeader replayHeader, final ReplayActions replayActions, final String gameChat ) {
		this.replayHeader  = replayHeader;
		this.replayActions = replayActions;
		this.gameChat      = gameChat;
	}
	
}
