package hu.belicza.andras.bwhfagent.view.replayfilter;

import java.util.Collection;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Filters replays by game engine.
 * 
 * @author Andras Belicza
 */
public class GameEngineReplayFilter extends PropertySetReplayFilter {
	
	/**
	 * Creates a new GameEngineReplayFilter.
	 * @param validGameEngines valid game engines
	 */
	public GameEngineReplayFilter( final Collection< Byte > validGameEngines ) {
		super( COMPLEXITY_NUMBER_SET, validGameEngines );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return isValueValid( replay.replayHeader.gameEngine );
	}
	
}
