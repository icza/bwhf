package hu.belicza.andras.bwhfagent.view.replayfilter;

import hu.belicza.andras.bwhf.model.Replay;

/**
 * Represents a filter which filters replays by a string property which may be either 
 * a regexp or multiple valid values.
 * 
 * @author Andras Belicza
 */
public abstract class StringPropertyReplayFilter extends ReplayFilter {
	
	/** The regexp filter in case valid strings are defined with regexp.                              */
	protected final RegexpStringReplayFilter      regexpStringReplayFilter;
	/** The string property set filter in case valid strings are defined with a comma separated list. */
	protected final StringPropertySetReplayFilter stringPropertySetReplayFilter;
	
	/**
	 * Creates a new StringPropertyReplayFilter.
	 * @param validValues string defining the valid values
	 * @param exactMatch  tells if exact match is required or substring match is allowed
	 * @param regexp      tells if <code>validValues</code> is a regexp or a comma separated list string
	 */
	public StringPropertyReplayFilter( final String validValues, final boolean exactMatch, final boolean regexp ) {
		super( regexp ? COMPLEXITY_REGEXP : ( exactMatch ? COMPLEXITY_STRING_SET_EXACT_MATCH : COMPLEXITY_STRING_SET_SUBSTRING ) );
		
		if ( regexp ) {
			regexpStringReplayFilter = new RegexpStringReplayFilter( validValues, exactMatch ) {
				@Override
				public boolean isReplayIncluded( final Replay replay ) {
					throw new RuntimeException( "This method should not be used!" );
				}
			};
			stringPropertySetReplayFilter = null;
		}
		else {
			stringPropertySetReplayFilter = new StringPropertySetReplayFilter( validValues, exactMatch ) {
				@Override
				public boolean isReplayIncluded( final Replay replay ) {
					throw new RuntimeException( "This method should not be used!" );
				}
			};
			regexpStringReplayFilter = null;
		}
	}
	
	/**
	 * Tells if the specified value is valid.
	 * @param value value to be tested
	 * @return true if the specified value is valid; false otherwise
	 */
	public boolean isValueValid( final String value ) {
		if ( regexpStringReplayFilter != null )
			return regexpStringReplayFilter.isValueValid( value );
		else
			return stringPropertySetReplayFilter.isValueValid( value );
	}
	
}
