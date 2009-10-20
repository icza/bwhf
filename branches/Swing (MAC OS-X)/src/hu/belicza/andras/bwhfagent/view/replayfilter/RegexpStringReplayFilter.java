package hu.belicza.andras.bwhfagent.view.replayfilter;

import java.util.regex.Pattern;

/**
 * Represents a filter which filters replays by one a string property defined by a regexp.
 * 
 * @author Andras Belicza
 */
public abstract class RegexpStringReplayFilter extends ReplayFilter {
	
	/** Pattern compiled from the regexp.                               */
	protected final Pattern pattern;
	/** Tells if exact match is required or substring match is allowed. */
	protected final boolean exactMatch;
	
	/**
	 * Creates a new RegexpStringReplayFilter.
	 * @param regexp     regexp defining valid values
	 * @param exactMatch tells if exact match is required or substring match is allowed
	 */
	public RegexpStringReplayFilter( final String regexp, final boolean exactMatch ) {
		super( COMPLEXITY_REGEXP );
		pattern         = Pattern.compile( regexp );
		this.exactMatch = exactMatch;
	}
	
	/**
	 * Tells if the specified value is valid.
	 * @param value value to be tested
	 * @return true if the specified value is valid; false otherwise
	 */
	public boolean isValueValid( final String value ) {
		return exactMatch ? pattern.matcher( value.toLowerCase() ).matches() : pattern.matcher( value.toLowerCase() ).find();
	}
	
}
