package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.Player;
import hu.belicza.andras.bwhf.model.Replay;

import java.util.ArrayList;
import java.util.List;

/**
 * Replay scanner which analyzes a {@link Replay} in order to find hacks in it.
 * 
 * @author Andras Belicza
 */
public class ReplayScanner {
	
	/**
	 * Scans the replay for hacks.
	 * 
	 * @param replay replay to be scanned
	 * @param skipLatterActionsOfHackers tells whether we have to proceed to the next player if one is found hacking
	 * @return a list of string messages describing the hacks found in the rep;
	 */
	public static List< String > scanReplayForHacks( final Replay replay, final boolean skipLatterActionsOfHackers ) {
		final List< String > hackDescriptionList = new ArrayList< String >();
		
		for ( int playerIndex = 0; playerIndex < replay.players.length; playerIndex++ )
			scanPlayerForHacks( replay.players[ playerIndex ], hackDescriptionList, skipLatterActionsOfHackers );
		
		return hackDescriptionList;
	}
	
	/**
	 * Searches known hack patterns in the actions of a player.
	 * 
	 * @param player player to be scanned
	 * @param hackDescriptionList reference to a hack description list where to put new hack descriptions
	 * @param skipLatterActionsOfHackers tells whether we have to proceed to the next player if one is found hacking
	 */
	private static void scanPlayerForHacks( final Player player, final List< String > hackDescriptionList, final boolean skipLatterActionsOfHackers ) {
		final Action[] playerActions = player.actions;
		final int actionsCount = playerActions.length;
		
		final int initialHackDescriptionListSize = hackDescriptionList.size();
		
		// Autogather/autotrain hack: having more than 1 action at iteration 5
		int actionsAtIteration5Count = 0;
		for ( int actionIndex = 0; actionIndex < actionsCount && actionIndex < 5; actionIndex++ )
			if ( playerActions[ actionIndex ].iteration == 5 )
				actionsAtIteration5Count++;
			else
				break;
		if ( actionsAtIteration5Count > 1 )
			hackDescriptionList.add( player.name + " used autogather/autotrain hack at 5" );
		
		
		int            lastIteration                        = 0;
		int            nonHotkeyActionsCountInSameIteration = 0;
		Action         lastAction                           = null;
		boolean        foundTerranComsatCancelHack          = false;
		Action         lastSelectAction                     = null;
		// We store the last select action which identifies units which were assigned/added to the hotkeys.
		// So if in the future we see a "hotkey select,xx" action, it will basically mean the action: lastSelectActionSetAsHotkeys[ xx ]
		// This is only an approximate solution! Exceptions:
		//    -if "hotkey add,xx" was used, this case it's only a subset of the real selection
		//    -if unit(s)/building included in the selection was/were taken out => we get a superior/greater set
		final Action[] lastSelectActionSetAsHotkeys         = new Action[ 11 ]; // We might have 11 hotkeys (0..11)
		
		for ( final Action action : playerActions ) {
			
			// Building selection hack: selecting more than one non-zerg building object with one select command
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT || action.actionNameIndex == Action.ACTION_NAME_INDEX_BWCHART_HACK )
				if ( action.parameterBuildingNameIndex != Action.BUILDING_NAME_INDEX_NON_BUILDING )
					if ( action.parameterBuildingNameIndex < Action.BUILDING_NAME_INDEX_FIRST_ZERG_BUILDING || action.parameterBuildingNameIndex > Action.BUILDING_NAME_INDEX_LAST_ZERG_BUILDING ) // Not a zerg building selected multiple times (that can be done wihtout hack by selecting drones about to morph)
						if ( action.parameters.length() != Action.BUILDING_NAMES[ action.parameterBuildingNameIndex ].length() )
							if ( action.parameters.startsWith( Action.BUILDING_NAMES[ action.parameterBuildingNameIndex ] + "(x" ) )
								hackDescriptionList.add( player.name + " used building selection hack at " + action.iteration );
			
			// Old zerg and protoss moneyhacks
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_0X33 )
				if ( lastSelectAction.parameterUnitNameIndex == Action.UNIT_NAME_INDEX_PROBE )
					hackDescriptionList.add( player.name + " used protoss moneyhack at " + action.iteration );
				else
					if ( lastSelectAction.parameterUnitNameIndex == Action.UNIT_NAME_INDEX_DRONE )
						hackDescriptionList.add( player.name + " used zerg moneyhack at " + action.iteration );
					else
						if ( lastSelectAction.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_NON_BUILDING ) // giving !0x33 means cancel upgrade; sometimes it's just a number (not recognized by BWChart), if not a building => moneyhack
							hackDescriptionList.add( player.name + " used moneyhack at " + action.iteration );
			
			// Old terran moneyhack (comsat cancel)
			if ( !foundTerranComsatCancelHack )
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_CANCEL_TRAIN && action.parameters.equals( "00 00" ) )
					if ( lastAction != null && lastAction.actionNameIndex == Action.ACTION_NAME_INDEX_TRAIN
							&& ( lastAction.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_COMSAT || lastAction.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_CONTROL_TOWER ) ) {
						foundTerranComsatCancelHack = true;
						hackDescriptionList.add( player.name + " used terran moneyhakc at " + action.iteration + " (this hack is reported only once)" );
					}
			
			// Old protoss moneyhack
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BWCHART_HACK && action.parameters.startsWith( "00 15" ) )
				hackDescriptionList.add( player.name + " used protoss moneyhack at " + action.iteration );
			
			// Zerg moneyhack with cancelling eggs (from Starcraft version 1.15.1)
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_CANCEL_TRAIN && ( action.parameters.equals( "FE 00" ) || action.parameters.equals( "00 00" ) ) )
				if ( lastAction != null && lastAction.actionNameIndex == Action.ACTION_NAME_INDEX_HATCH )
					hackDescriptionList.add( player.name + " used zerg moneyhack at " + action.iteration );
			
			// Multicommand hack: giving "several" actions in the same iteration
			if ( lastIteration == action.iteration ) {
				if ( action.actionNameIndex != Action.ACTION_NAME_INDEX_HOTKEY )
					nonHotkeyActionsCountInSameIteration++;
			}
			else {
				if ( nonHotkeyActionsCountInSameIteration > 14 )
					hackDescriptionList.add( player.name + " used multicommand hack at " + lastAction.iteration );
				lastIteration = action.iteration;
				nonHotkeyActionsCountInSameIteration = 0;
			}
			
			
			// Proceeding to the next action
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT )
				lastSelectAction = action;
			
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_HOTKEY ) {
				try {
					final int hotkey = Integer.parseInt( action.parameters.substring( action.parameters.indexOf( ',' ) + 1 ) );
					if ( action.parameters.startsWith( Action.HOTKEY_ACTION_PARAM_NAME_ASSIGN ) || action.parameters.startsWith( Action.HOTKEY_ACTION_PARAM_NAME_ADD ) )
						lastSelectActionSetAsHotkeys[ hotkey ] = lastSelectAction;
					else
						if ( action.parameters.startsWith( Action.HOTKEY_ACTION_PARAM_NAME_SELECT ) )
							lastSelectAction = lastSelectActionSetAsHotkeys[ hotkey ];
				}
				catch ( ArrayIndexOutOfBoundsException aioobe ) {
					// TODO: consider, resulting in here can be proof of hack?
					// Real life example:  "2530	TwilightNinja69	Hotkey	Select,15		"
				}
			}
			
			lastAction = action;
			
			if ( skipLatterActionsOfHackers )
				if ( initialHackDescriptionListSize != hackDescriptionList.size() )
					break;
		}
	}
	
}
