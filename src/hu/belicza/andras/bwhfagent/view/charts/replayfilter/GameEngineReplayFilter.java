package hu.belicza.andras.bwhfagent.view.charts.replayfilter;

import java.util.Collection;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Filters replays by game engine.
 * 
 * @author Andras Belicza
 */
public class GameEngineReplayFilter extends NumberPropertySetReplayFilter {
	
	/**
	 * Creates a new GameEngineReplayFilter.
	 * @param validGameEngines valid game engines
	 */
	public GameEngineReplayFilter( final Collection< Byte > validGameEngines ) {
		super( validGameEngines );
	}
	
	@Override
	public boolean isReplayIncluded( final Replay replay ) {
		return isValueValid( replay.replayHeader.gameEngine );
	}
	
}
