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
		if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_TRAIN || action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD_SUBUNIT ) {
			if ( countSameActions( actions, actionIndex, action ) >= 6 )
				return false;
		}
		
		final Action prevAction = actionIndex > 0 ? actions[ actionIndex - 1 ] : null; // Shortcut to the previous action
		
		// Too fast cancel
		if ( actionIndex > 0 && action.iteration - prevAction.iteration <= 20 ) {
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_TRAIN && prevAction.actionNameIndex == Action.ACTION_NAME_INDEX_CANCEL_TRAIN )
				return false;
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_RESEARCH && prevAction.actionNameIndex == Action.ACTION_NAME_INDEX_CANCEL_RESEARCH )
				return false;
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_UPGRADE && prevAction.actionNameIndex == Action.ACTION_NAME_INDEX_CANCEL_UPGRADE )
				return false;
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_HATCH && prevAction.actionNameIndex == Action.ACTION_NAME_INDEX_CANCEL_HATCH )
				return false;
		}
		
		// Too fast repetition of commands in a short time (regardless of its destination, if destination is different/far, then the first one was useless)
		if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_MOVE || action.actionNameIndex == Action.ACTION_NAME_INDEX_STOP
				|| action.actionNameIndex == Action.ACTION_NAME_INDEX_HOLD || action.actionNameIndex == Action.ACTION_NAME_INDEX_ATTACK_MOVE
				|| action.actionNameIndex == Action.ACTION_NAME_INDEX_SET_RALLY 
				|| action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.charAt( 0 ) == 'A' ) {
			if ( actionIndex > 0 && prevAction.actionNameIndex == action.actionNameIndex 
					&& action.iteration - prevAction.iteration <= 10 )
				return false;
		}
		
		// Too fast switch away from or reselecting the same selected unit = no use of selecting it. By too fast I mean it isn't even enough to check the object state
		if ( actionIndex > 0 && ( action.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT || action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.charAt( 0 ) == 'S' ) ) {
			if ( prevAction.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT || prevAction.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.charAt( 0 ) == 'S'
					&& action.iteration - prevAction.iteration <= 8 )
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && prevAction.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.equals( prevAction.parameters ) ) {
					// Exclude double tapping a hotkey, it's only ineffective if it was pressed more than 3 times
					if ( actionIndex > 1 ) {
						final Action prevPrevAction = actions[ actionIndex - 2 ]; // Shortcut to the previous action before the previous
						if ( prevAction.iteration - prevPrevAction.iteration <= 8 && prevPrevAction.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY
								&& action.parameters.equals( prevPrevAction.parameters ) )
							return false;
					}
				}
				else
					return false;
		}
		
		// Repetition of commands without time restriction
		if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_HATCH || action.actionNameIndex == Action.ACTION_NAME_INDEX_MORPH 
				|| action.actionNameIndex == Action.ACTION_NAME_INDEX_UPGRADE || action.actionNameIndex == Action.ACTION_NAME_INDEX_RESEARCH
				|| action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD || action.actionNameIndex == Action.ACTION_NAME_INDEX_CANCEL
				|| action.actionNameIndex == Action.ACTION_NAME_INDEX_MERGE_ARCHON || action.actionNameIndex == Action.ACTION_NAME_INDEX_MERGE_DARK_ARCHON
				|| action.actionNameIndex == Action.ACTION_NAME_INDEX_LIFT )
			if ( actionIndex > 0 && prevAction.actionNameIndex == action.actionNameIndex )
				return false;
		
		// Repetition of the same hotkey assign
		if ( actionIndex > 0 && action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.charAt( 0 ) == 'A'
				&& prevAction.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action.parameters.equals( prevAction.parameters ) )
			return false;
		
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
				   || ( action2.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY && action2.parameters.charAt( 0 ) == 'S' ) )
				break;
		}
		
		return sameActionsCount;
	}
	
}
