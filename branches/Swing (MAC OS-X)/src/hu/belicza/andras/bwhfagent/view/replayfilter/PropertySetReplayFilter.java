package hu.belicza.andras.bwhfagent.view.replayfilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a filter which filters replays by a property having multiple valid values.
 * 
 * @author Andras Belicza
 */
public abstract class PropertySetReplayFilter extends ReplayFilter {
	
	/** The valid property byte values. */
	protected final Set< ? extends Object > validValueSet;
	
	/**
	 * Creates a new BytePropertySetReplayFilter.
	 * @param validValues the valid property byte values
	 */
	public PropertySetReplayFilter( final int complexity, final Collection< ? extends Object > validValueCollection ) {
		super( complexity );
		validValueSet = new HashSet< Object >( validValueCollection );
	}
	
	/**
	 * Tells if the specified value is valid.
	 * @param value value to be tested
	 * @return true if the specified value is valid; false otherwise
	 */
	public boolean isValueValid( final Object value ) {
		return validValueSet.contains( value );
	}
	
}
