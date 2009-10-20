package hu.belicza.andras.bwhf.model;

/**
 * Class modeling a replay.
 * 
 * @author Belicza Andras
 */
public class Replay {
	
	/** Header of the replay.        */
	public final ReplayHeader  replayHeader;
	/** Actions of the replay.       */
	public final ReplayActions replayActions;
	/** Formatted text of game chat. */
	public final String        gameChat;
	
	/**
	 * Creates a new Replay.
	 * @param replayHeader  header of the replay
	 * @param replayActions actions of the replay
	 * @param gameChat      formatted text of game chat
	 */
	public Replay( final ReplayHeader replayHeader, final ReplayActions replayActions, final String gameChat ) {
		this.replayHeader  = replayHeader;
		this.replayActions = replayActions;
		this.gameChat      = gameChat;
	}
	
}
