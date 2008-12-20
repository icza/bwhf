package hu.belicza.andras.bwhf.model;

/**
 * Class modelling a replay.
 * For java bitwise operations, see See also: http://www.cs.utsa.edu/~wagner/laws/Abytes.html.
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
