package hu.belicza.andras.bnetbot.impl;

import hu.belicza.andras.bnetbot.Status;
import hu.belicza.andras.bnetbot.StatusChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of a status manager which handles change events when the status is changed.
 * 
 * @author Andras Belicza
 */
public class StatusManager {
	
	/** The managed status. */
	private Status status;
	
	/** List of the registered status change listeners. */
	private List< StatusChangeListener > statusChangeListenerList = new ArrayList< StatusChangeListener >();
	
	/**
	 * Registers a status change listener.
	 * @param statusChangeListener status change listener to be registered
	 */
	public void registerStatusChangeListener( final StatusChangeListener statusChangeListener ) {
		statusChangeListenerList.add( statusChangeListener );
	}
	
	/**
	 * Removes a status change listener.
	 * @param statusChangeListener status change listener to be removed
	 */
	public void removeStatusChangeListener( final StatusChangeListener statusChangeListener ) {
		statusChangeListenerList.remove( statusChangeListener );
	}
	
	/**
	 * Fires a status change event.
	 * @param newStatus the new status 
	 */
	public void fireStatusChangeEvent( final Status newStatus ) {
		for ( final StatusChangeListener statusChangeListener : statusChangeListenerList )
			statusChangeListener.statusChanged( newStatus );
	}
	
	/**
	 * Returns the current status.
	 * @return the current status
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Sets the status and fires a status change event.
	 * @param status status to be set
	 */
	public void setStatus( final Status status ) {
		final boolean statusChanges = this.status != status;
		
		this.status = status;
		
		if ( statusChanges )
			fireStatusChangeEvent( status );
	}
	
}
