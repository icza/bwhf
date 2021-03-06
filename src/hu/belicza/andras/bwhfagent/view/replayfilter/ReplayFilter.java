package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Represents a comparable replay filter. Order is determined by the filter's complexity.
 * 
 * @author Andras Belicza
 */
public abstract class ReplayFilter implements Comparable< ReplayFilter > {
	
	protected static final Integer COMPLEXITY_NUMBER_INTERVAL        =  10;
	protected static final Integer COMPLEXITY_NUMBER_PAIR_INTERVAL   =  20;
	protected static final Integer COMPLEXITY_NUMBER_SET             =  30;
	protected static final Integer COMPLEXITY_STRING_SET_EXACT_MATCH =  60;
	protected static final Integer COMPLEXITY_STRING_SET_SUBSTRING   =  70;
	protected static final Integer COMPLEXITY_REGEXP                 = 100;
	
	/** Complexity of the filter. */
	private final Integer complexity;
	
	/**
	 * Creates a new ReplayFilter.
	 * @param complexity complexity of the filter.
	 */
	public ReplayFilter( final Integer complexity ) {
		this.complexity = complexity;
	}
	
	/**
	 * Tests if this filter includes the specified replay.
	 * @param replay replay to be tested
	 * @return true if this filter includes the specified replay; false otherwise
	 */
	public abstract boolean isReplayIncluded( final Replay replay );
	
	/**
	 * Compares this replay filter to another one based on their complexity.
	 * @param replayFilter replay filter to be compared to
	 * @return
	 */
	public int compareTo( final ReplayFilter replayFilter ) {
		return complexity.compareTo( replayFilter.complexity );
	}
	
}
