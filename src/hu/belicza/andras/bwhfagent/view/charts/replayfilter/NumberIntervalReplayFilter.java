package hu.belicza.andras.bwhfagent.view.charts.replayfilter;

/**
 * Represents a filter which filters replays by one number property.
 * 
 * @author Andras Belicza
 */
public abstract class NumberIntervalReplayFilter extends ReplayFilter {
	
	/** Min valid value. */
	protected final Number minValidValue;
	/** Max valid value. */
	protected final Number maxValidValue;
	
	/**
	 * Creates a new NumberIntervalReplayFilter.
	 * @param minValue min valid value
	 * @param maxValue max valid value
	 */
	public NumberIntervalReplayFilter( final Number minValidValue, final Number maxValidValue ) {
		super( COMPLEXITY_NUMBER_INTERVAL );
		this.minValidValue = minValidValue;
		this.maxValidValue = maxValidValue;
	}
	
	/**
	 * Tells if the specified value is valid.
	 * @param value value to be tested
	 * @return true if the specified value is valid; false otherwise
	 */
	public boolean isValueValid( final Number value ) {
		if ( value instanceof Long ) {
			if ( minValidValue != null && (Long) value < (Long) minValidValue )
				return false;
			if ( maxValidValue != null && (Long) value > (Long) maxValidValue )
				return false;
		}
		else if ( value instanceof Short ) {
			if ( minValidValue != null && (Short) value < (Short) minValidValue )
				return false;
			if ( maxValidValue != null && (Short) value > (Short) maxValidValue )
				return false;
		}
		else
			throw new RuntimeException( "Class " + value.getClass() + " is not yet implemented for NumberIntervalReplayFilter!" );
		
		return true;
	}
	
}
