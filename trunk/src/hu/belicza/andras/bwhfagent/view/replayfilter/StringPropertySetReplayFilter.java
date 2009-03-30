package hu.belicza.andras.bwhfagent.view.replayfilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Represents a filter which filters replays by a string property having multiple valid values.
 * 
 * @author Andras Belicza
 */
public abstract class StringPropertySetReplayFilter extends PropertySetReplayFilter {
	
	/** Tells if exact match is required or substring match is allowed. */
	protected final boolean exactMatch;
	
	/**
	 * Creates a new StringPropertySetReplayFilter.
	 * @param validValuesString comma separated string of valid values
	 */
	public StringPropertySetReplayFilter( final String validValuesString, final boolean exactMatch ) {
		super( exactMatch ? COMPLEXITY_STRING_SET_EXACT_MATCH : COMPLEXITY_STRING_SET_SUBSTRING, parseValues( validValuesString ) );
		this.exactMatch = exactMatch;
	}
	
	/**
	 * Parses values in a string separated by commas.
	 * @param validValuesString comma separated string of valid values
	 * @return a collection of the parsed valid values
	 */
	private static Collection< String > parseValues( final String validValuesString ) {
		final StringTokenizer validValuesTokenizer = new StringTokenizer( validValuesString, "," );
		final List< String >  validValueList       = new ArrayList< String >( validValuesTokenizer.countTokens() );
		
		while ( validValuesTokenizer.hasMoreTokens() )
			validValueList.add( validValuesTokenizer.nextToken() );
		
		return validValueList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean isValueValid( final Object value ) {
		if ( exactMatch )
			return super.isValueValid( value );
		else {
			final String stringValue = (String) value;
			for ( final String stringValidValue : (Set< String >) validValueSet )
				if ( stringValue.contains( stringValidValue ) )
					return true;
			return false;
		}
	}
	
}
