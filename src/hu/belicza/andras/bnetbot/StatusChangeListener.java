package hu.belicza.andras.bnetbot;

/**
 * Interface for statuc change listeners.
 * 
 * @author Andras Belicza
 */
public interface StatusChangeListener {
	
	/**
	 * Called when the status is changed.
	 * @param status the new status
	 */
	void statusChanged( final Status status );
	
}
