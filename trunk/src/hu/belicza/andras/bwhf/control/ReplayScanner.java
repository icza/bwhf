package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.HackDescription;
import hu.belicza.andras.bwhf.model.PlayerActions;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayActions;
import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Replay scanner which analyzes a {@link ReplayActions} in order to find hacks in it.
 * 
 * @author Andras Belicza
 */
public class ReplayScanner {
	
	/**
	 * Scans the replay actions for hacks.
	 * 
	 * @param replay                     replay to be scanned
	 * @param skipLatterActionsOfHackers tells whether we have to proceed to the next player if one is found hacking
	 * @return a list of {@link HackDescription}s describing the hacks found in the rep
	 */
	public static List< HackDescription > scanReplayForHacks( final Replay replay, final boolean skipLatterActionsOfHackers ) {
		final List< HackDescription > hackDescriptionList = new ArrayList< HackDescription >();
		
		for ( final PlayerActions playerActions : replay.replayActions.players )
			scanPlayerForHacks( replay.replayHeader, playerActions, hackDescriptionList, skipLatterActionsOfHackers );
		
		return hackDescriptionList;
	}
	
	/**
	 * Searches known hack patterns in the actions of a player.
	 * 
	 * @param replayHeader        header of the replay being scanned
	 * @param player              player to be scanned
	 * @param hackDescriptionList reference to a hack description list where to put new hack descriptions
	 * @param skipLatterActionsOfHackers tells whether we have to proceed to the next player if one is found hacking
	 */
	private static void scanPlayerForHacks( final ReplayHeader replayHeader, final PlayerActions player, final List< HackDescription > hackDescriptionList, final boolean skipLatterActionsOfHackers ) {
		final Action[] playerActions = player.actions;
		final int      actionsCount  = playerActions.length;
		
		final int initialHackDescriptionListSize = hackDescriptionList.size();
		
		// Autogather/autotrain hack: having more than 1 action at iteration 5
		int actionsAtIteration5Count = 0;
		for ( int actionIndex = 0; actionIndex < actionsCount && actionIndex < 25; actionIndex++ )
			if ( playerActions[ actionIndex ].iteration == 5 ) {
				if ( playerActions[ actionIndex ].actionNameIndex != Action.ACTION_NAME_INDEX_UNKNOWN )
					actionsAtIteration5Count++;
			}
			else
				break;
		if ( actionsAtIteration5Count > 1 ) {
			// If the player has actions at iteration 10, it is more likely because he's using the latchanger program.
			// In that case we don't report autogather/autotrain.
			// If the player had more than 10 actions at iteration 5, we take that as proof of hack nonetheless.
			boolean hasActionAtIteration10 = false;
			for ( int actionIndex = 0; actionIndex < actionsCount && playerActions[ actionIndex ].iteration <= 10; actionIndex++ )
				if ( playerActions[ actionIndex ].iteration == 10 ) {
					hasActionAtIteration10 = true;
					break;
				}
			if ( actionsAtIteration5Count > 10 || !hasActionAtIteration10 )
				hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_AUTOGATHER_AUTOTRAIN, 5 ) );
		}
		
		
		int            lastIteration                        = -1;
		int            nonHotkeyActionsCountInSameIteration = 0;
		Action         lastAction                           = null;
		boolean        foundTerranComsatCancelHack          = false;
		Action         lastSelectAction                     = null;
		// We store the last select action which identifies units which were assigned/added to the hotkeys.
		// So if in the future we see a "hotkey select,xx" action, it will basically mean the action: lastSelectActionSetAsHotkeys[ xx ]
		// This is only an approximate solution! Exceptions:
		//    -if "hotkey add,xx" was used, this case it's only a subset of the real selection
		//    -if unit(s)/building included in the selection was/were taken out => we get a superior/greater set
		final Action[] lastSelectActionSetAsHotkeys         = new Action[ 11 ]; // We might have 11 hotkeys (0..10)
		
		// Actions ahead local variables for multicommand unit control detection
		Action actionAhead1, actionAhead2, actionAhead3;
		
		for ( int actionIndex = 0; actionIndex < actionsCount; actionIndex++ ) {
			final Action action = playerActions[ actionIndex ];
			
			// Ally-vision drophack
			if ( replayHeader.gameType != ReplayHeader.GAME_TYPE_UMS ) {
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_ALLY )
					if ( !checkAllyParams( action.parameters ) )
						hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_ALLY_VISION_DROPHACK, action.iteration ) );
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_VISION )
					if ( !checkVisionParams( action.parameters ) )
						hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_ALLY_VISION_DROPHACK, action.iteration ) );
			}
			
			// Building selection hack: selecting more than one non-zerg building object with one select command
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT || action.actionNameIndex == Action.ACTION_NAME_INDEX_BWCHART_HACK )
				if ( action.parameterBuildingNameIndex != Action.BUILDING_NAME_INDEX_NON_BUILDING )
					if ( action.parameterBuildingNameIndex < Action.BUILDING_NAME_INDEX_FIRST_ZERG_BUILDING || action.parameterBuildingNameIndex > Action.BUILDING_NAME_INDEX_LAST_ZERG_BUILDING ) // Not a zerg building selected multiple times (that can be done wihtout hack by selecting drones about to morph)
						if ( action.parameters.length() != Action.UNIT_ID_NAME_MAP.get( (byte) action.parameterBuildingNameIndex ).length() )
							if ( action.parameters.startsWith( Action.UNIT_ID_NAME_MAP.get( (byte) action.parameterBuildingNameIndex ) + "(x" ) )
								hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_BUILDING_SELECTION, action.iteration ) );
			
			// Old zerg and protoss moneyhacks
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_0X33 )
				if ( lastSelectAction.parameterUnitNameIndex == Action.UNIT_NAME_INDEX_PROBE )
					hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_PROTOSS_MONEYHACK, action.iteration ) );
				else
					if ( lastSelectAction.parameterUnitNameIndex == Action.UNIT_NAME_INDEX_DRONE )
						hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_ZERG_MONEYHACK, action.iteration ) );
					else
						if ( lastSelectAction.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_NON_BUILDING ) // giving !0x33 means cancel upgrade; sometimes it's just a number (not recognized by BWChart), if not a building => moneyhack
							hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_MONEYHACK, action.iteration ) );
			
			// Old terran moneyhack (comsat cancel)
			if ( !foundTerranComsatCancelHack )
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_CANCEL_TRAIN && action.parameters.equals( "00 00" ) )
					if ( lastAction != null && lastAction.actionNameIndex == Action.ACTION_NAME_INDEX_TRAIN
							&& ( lastAction.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_COMSAT || lastAction.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_CONTROL_TOWER ) ) {
						foundTerranComsatCancelHack = true;
						hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_TERRAN_MONEYHACK, action.iteration ) );
					}
			
			// Multicommand unit control hack and multicommand rally set hack
			if ( actionIndex + 3 < actionsCount ) {
				actionAhead1 = playerActions[ actionIndex + 1 ];
				actionAhead2 = playerActions[ actionIndex + 2 ];
				actionAhead3 = playerActions[ actionIndex + 3 ];
				if ( action.parameters != null && actionAhead1.parameters != null && actionAhead2.parameters != null && actionAhead2.parameters != null )
					if ( action.iteration == actionAhead1.iteration && action.iteration == actionAhead2.iteration && action.iteration == actionAhead3.iteration
					  && action.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT && actionAhead2.actionNameIndex == Action.ACTION_NAME_INDEX_SELECT
					  && ( actionAhead1.actionNameIndex == Action.ACTION_NAME_INDEX_MOVE || actionAhead1.actionNameIndex == Action.ACTION_NAME_INDEX_ATTACK_MOVE )
					  && actionAhead1.actionNameIndex == actionAhead3.actionNameIndex
					  && actionAhead1.parameters.equals( actionAhead3.parameters ) ) {
						if ( action.parameterBuildingNameIndex == Action.BUILDING_NAME_INDEX_NON_BUILDING )
							hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_MULTICOMMAND_UNIT_CONTROL, action.iteration ) );
						else
							hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_MULTICOMMAND_RALLY_SET, action.iteration ) );
					}
			}
			
			// Old protoss moneyhack
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BWCHART_HACK && action.parameters.startsWith( "00 15" ) )
				hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_PROTOSS_MONEYHACK, action.iteration ) );
			
			// Zerg moneyhack with cancelling eggs (from Starcraft version 1.15.1)
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_CANCEL_TRAIN && ( action.parameters.equals( "FE 00" ) || action.parameters.equals( "00 00" ) ) )
				if ( lastAction != null && lastAction.actionNameIndex == Action.ACTION_NAME_INDEX_HATCH )
					hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_ZERG_MONEYHACK, action.iteration ) );
			
			// Multicommand hack: giving "several" actions in the same iteration
			// If actions being next to each other are the same actions (regardless to its parameters),
			// it can be due to lag and/or "action spam". Don't count and report those
			// This "same" action checking is not completely correct, since we don't distinguish between a lot of actions,
			// but this is accurate enough since we parse and use the most common actions for hack detection
			if ( lastIteration == action.iteration && lastAction.actionNameIndex != action.actionNameIndex && action.actionNameIndex != Action.ACTION_NAME_INDEX_HOTKEY )
				nonHotkeyActionsCountInSameIteration++;
			else {
				if ( nonHotkeyActionsCountInSameIteration > 18 )
					hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_MULTICOMMAND, lastAction.iteration ) );
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
				catch ( final ArrayIndexOutOfBoundsException aioobe ) {
					// TODO: investigate, resulting in here can be proof of hack?
					// Real life example:  "2530	TwilightNinja69	Hotkey	Select,15		"
				}
			}
			
			lastAction = action;
			
			if ( skipLatterActionsOfHackers )
				if ( initialHackDescriptionListSize != hackDescriptionList.size() )
					break;
		}
	}
	
	/**
	 * Checks the parameters of an ally command.
	 * @param parameters parameters of an ally command
	 * @return true if paramameters are correct; false otherwise
	 */
	private static boolean checkAllyParams( final String parameters ) {
		if ( !parameters.endsWith( "0 00" ) )
			return false;
		if ( parameters.charAt( 6 ) != '8' && parameters.charAt( 6 ) != '4' )
			return false;
		
		if ( parameters.startsWith( "00 00" ) )
			return false; // Has to ally at least to himself
		
		/*final StringBuilder bitsBuilder = new StringBuilder();
		for ( final int paramBytePos : new int[] { 0, 1, 3, 4 } ) { // We skip the space
			final String byteBinary = Integer.toBinaryString( Integer.parseInt( parameters.substring( paramBytePos, paramBytePos+1 ), 16 ) );
			bitsBuilder.append( "00000000".substring( byteBinary.length(), 8 ) );
			bitsBuilder.append( byteBinary );
		}
		
		final boolean avOn = parameters.charAt( 6 ) == '4';
		
		boolean foundAlliedPlayer = false;
		for ( int bitPos = bitsBuilder.length() - ( avOn ? 2 : 1 ); bitPos >= 0; bitPos -= 2 )
			if ( bitsBuilder.charAt( bitPos ) == '1' ) {
				foundAlliedPlayer = true;
				break;
			}
		if ( !foundAlliedPlayer )
			return false;
		
		for ( int bitPos = bitsBuilder.length() - ( avOn ? 1 : 2 ); bitPos >= 0; bitPos -= 2 )
			if ( bitsBuilder.charAt( bitPos ) != '0' )
				return false;*/
		
		return true;
	}
	
	/**
	 * Checks the parameters of a vision command.
	 * @param parameters parameters of a vision command
	 * @return true if paramameters are correct; false otherwise
	 */
	private static boolean checkVisionParams( final String parameters ) {
		if ( parameters.charAt( 3 ) != '0' || parameters.charAt( 4 ) != '0' )
			return false;
		
		if ( parameters.charAt( 0 ) == '0' && parameters.charAt( 1 ) == '0' )
			return false;  // A player must always give vision to himself unless it's an UMS (which might allow "lights off")
		
		return true;
	}
	
}
