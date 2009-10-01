package hu.belicza.andras.bwhfagent.view.charts;

import hu.belicza.andras.bwhf.model.Action;

/**
 * Implementation of the EAPM calculation.
 * 
 * @author Andras Belicza
 */
public class EapmUtil {
	
	/**
	 * Decides if an action is <i>effective</i> so it can be part of the EAPM calculation.
	 * @param actions     actions of the player
	 * @param actionIndex index of the action to be decided
	 * @param action      shortcut reference to the action to be decided
	 * @return true if the action is considered <i>effective</i>; false otherwise
	 */
	public static boolean isActionEffective( final Action[] actions, final int actionIndex, final Action action ) {
		// Unit queue overflow
		if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_TRAIN || action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD_SUBUNIT )
			if ( countSameActions( actions, actionIndex, action ) >= 6 )
				return false;
		
		final Action prevAction = actions[ actionIndex - 1 ]; // Shortcut to the previous action
		
		// Too fast repetition of commands (regardless of its destination, if destination is different/far, then the first one was useless)
		if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_MOVE || action.actionNameIndex == Action.ACTION_NAME_INDEX_STOP
				|| action.actionNameIndex == Action.ACTION_NAME_INDEX_HOLD
				|| action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.startsWith( "Assign" ) ) {
			if ( actionIndex > 0 && prevAction.actionNameIndex == action.actionNameIndex 
					&& action.iteration - prevAction.iteration <= 20 )
				return false;
		}
		
		// Too fast switch away from or reselectign the same selected unit = no use of selecting it. By too fast I mean it isn't even enough to check the object state
		// TODO exclude double tapping a hotkey
		if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT || action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.startsWith( "Select" ) ) {
			if ( actionIndex > 0 && prevAction.actionNameIndex == action.actionNameIndex 
					&& action.iteration - prevAction.iteration <= 15 )
				return false;
		}
		
		// Fast hotkey selection repeation more than twice
		// If we're here, it cannot be a different hotkey, because it would've been qualified ineffective (previous rule) 
		if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.startsWith( "Select" ) 
				&& prevAction.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && prevAction.parameters.startsWith( "Select" )
				&& actions[ actionIndex - 2 ].actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && actions[ actionIndex - 2 ].parameters.startsWith( "Select" ) )
			return false;
		
		// TODO too fast cancel after train
		
		return true;
	}
	
	/**
	 * Counts how many times an action is repeated on the same object within 1 second.<br>
	 * If the action is repeated more than 6 times, then 6 is returned.
	 * @param actions     actions of the player
	 * @param actionIndex index of the action to be counted
	 * @param action      shortcut reference to the action to be counted
	 * @return the number of repeated actions maximized in 6
	 */
	private static int countSameActions( final Action[] actions, final int actionIndex, final Action action ) {
		final int iterationLimit = action.iteration - 25; // Within about 1 sec
		
		int sameActionsCount = 0;
		for ( int i = actionIndex; i >= 0; i-- ) {
			final Action action2 = actions[ i ];
			if( action2.iteration < iterationLimit )
				break;
			
			if ( action2.actionNameIndex == action.actionNameIndex ) {
				if ( ++sameActionsCount == 6 )
					return sameActionsCount;
			}
			else if ( action2.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT || action2.actionNameIndex == Action.ACTION_NAME_INDEX_SHIFT_SELECT
				   || action2.actionNameIndex == Action.ACTION_NAME_INDEX_SHIFT_DESELECT
				   || ( action2.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action2.parameters.startsWith( "Select" ) ) )
				break;
		}
		
		return sameActionsCount;
	}
	
}
