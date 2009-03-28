package hu.belicza.andras.bwhfagent.view.charts.replayfilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a filter which filters replays by one number property.
 * 
 * @author Andras Belicza
 */
public abstract class NumberPropertySetReplayFilter extends ReplayFilter {
	
	/** The valid property byte values. */
	protected final Set< Number > validValueSet;
	
	/**
	 * Creates a new BytePropertySetReplayFilter.
	 * @param validValues the valid property byte values
	 */
	public NumberPropertySetReplayFilter( final Collection< ? extends Number > validValueCollection ) {
		super( COMPLEXITY_NUMBER_SET );
		validValueSet = new HashSet< Number >( validValueCollection );
	}
	
	/**
	 * Tells if the specified value is valid.
	 * @param value value to be tested
	 * @return true if the specified value is valid; false otherwise
	 */
	public boolean isValueValid( final Number value ) {
		return validValueSet.contains( value );
	}
	
}
