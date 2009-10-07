package hu.belicza.andras.bwhf.control;

import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.HackDescription;
import hu.belicza.andras.bwhf.model.PlayerActions;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Replay scanner which analyzes a {@link Replay} in order to find hacks in it.
 * 
 * @author Andras Belicza
 */
public class ReplayScanner {
	
	/** Version of the scan engine. */
	public static final String ENGINE_VERSION = "1.42";
	
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
				if ( playerActions[ actionIndex ].iteration == 10 && playerActions[ actionIndex ].actionNameIndex != Action.ACTION_NAME_INDEX_LEAVE ) {
					hasActionAtIteration10 = true;
					break;
				}
			if ( actionsAtIteration5Count > 10 || !hasActionAtIteration10 )
				hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_AUTOGATHER_AUTOTRAIN, 5 ) );
		}
		
		// Delayed autogather/autotrain hack: select+train/hatch, select+move, select+move, select+move, select+move;
		// Selects with different targets, moves with different targets and all in the same iteration (which is the first action)
		if ( actionsCount >= 10 
				&& playerActions[ 0 ].actionNameIndex == Action.ACTION_NAME_INDEX_SELECT && ( playerActions[ 1 ].actionNameIndex == Action.ACTION_NAME_INDEX_TRAIN || playerActions[ 1 ].actionNameIndex == Action.ACTION_NAME_INDEX_HATCH )
			    && playerActions[ 0 ].iteration == playerActions[ 9 ].iteration ) {
			boolean isHack = true;
			for ( int i = 2; i < 10; i += 2 ) {
				if ( playerActions[ i ].actionNameIndex != Action.ACTION_NAME_INDEX_SELECT || playerActions[ i+1 ].actionNameIndex != Action.ACTION_NAME_INDEX_MOVE
						|| playerActions[ i ].parameters == null || playerActions[ i ].parameters.indexOf( ',' ) > 0 ) {
					isHack = false;
					break;
				}
				if ( i < 8 ) {
					if ( playerActions[ i ].parameters == null || playerActions[ i ].parameters.equals( playerActions[ i+2 ].parameters )
							|| playerActions[ i+1 ].parameters == null || playerActions[ i+1 ].parameters.equals( playerActions[ i+3 ].parameters ) ) {
						isHack = false;
						break;
					}
				}
			}
			if ( isHack )
				hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_AUTOGATHER_AUTOTRAIN, playerActions[ 0 ].iteration ) );
		}
		
		
		int            lastIteration                          = -1;
		int            actionsCountForGeneralMulticommandHack = 0;
		Action         lastAction                             = null;
		boolean        foundTerranComsatCancelHack            = false;
		Action         lastSelectAction                       = null;
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
			
			// Use Cheat drophack
			// Single Player: only melee, ffa and ums is allowed.
			// TODO: This check should relate to all multiplayer modes.
			// For now I only check if the game type is only allowed in multiplayer mode, this might skip some multiplayer game.  
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_USE_CHEAT && ( replayHeader.gameType != ReplayHeader.GAME_TYPE_MELEE && replayHeader.gameType != ReplayHeader.GAME_TYPE_FFA && replayHeader.gameType != ReplayHeader.GAME_TYPE_UMS ) )
				hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_USE_CHEAT_DROPHACK, action.iteration ) );
			
			// Ally-vision drophack
			if ( replayHeader.gameType != ReplayHeader.GAME_TYPE_UMS ) {
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_ALLY )
					if ( !checkAllyParams( action.parameters ) )
						hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_ALLY_VISION_DROPHACK, action.iteration ) );
				if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_VISION )
					if ( !checkVisionParams( action.parameters ) )
						hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_ALLY_VISION_DROPHACK, action.iteration ) );
			}
			
			// Build anywhere hack
			// This can be checked only on standard size maps, because Starcraft only saves standard map sizes.  
			// If map size is not standard, then the saved map size might be smaller than the actual size,
			// and this would result in building outside the map box when in fact it is not.
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD && action.parameters != null && action.parameters.length() > 0 && action.parameterBuildingNameIndex != Action.BUILDING_NAME_INDEX_NON_BUILDING ) {
				final Action.Size buildingSize = Action.BUILDING_ID_SIZE_MAP.get( action.parameterBuildingNameIndex );
				if ( buildingSize != null ) {
					try {
						final int commaIndex = action.parameters.indexOf( ',' );
						final int x = Integer.parseInt( action.parameters.substring( action.parameters.indexOf( '(' ) + 1, commaIndex ) );
						final int y = Integer.parseInt( action.parameters.substring( commaIndex + 1, action.parameters.indexOf( ')', commaIndex ) ) );
						// In the range of x all coordinate is buildable, but in the range of y the bottom line is reserved
						if ( x > replayHeader.mapWidth - buildingSize.width || y > replayHeader.mapHeight - buildingSize.height - 1 )
							hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_BUILD_ANYWHERE, action.iteration ) );
					}
					catch ( final Exception e ) {
					}
				}
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
			
			// Subunit enqueue hack: subsequent of "some" subunit build actions being equally 120 iterations from each other
			if ( action.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD_SUBUNIT ) {
				final int HACK_COUNT_LIMIT = 3;
				final int minTestIteration = action.iteration - ( HACK_COUNT_LIMIT - 1 ) * 120;
				int    patternSubunitBuildsCount = 0; // Subunit builds in the pattern
				int    allSubunitBuildsCount     = 0; // All subunit build commands
				int    testIndex                 = actionIndex;
				int    testIteration             = action.iteration;
				Action testAction                = null;
				do {
					testAction = playerActions[ testIndex ];
					if ( testAction.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD_SUBUNIT )
						allSubunitBuildsCount++;
					if ( testAction.iteration == testIteration ) {
						if ( testAction.actionNameIndex == Action.ACTION_NAME_INDEX_BUILD_SUBUNIT ) {
							patternSubunitBuildsCount++;
							testIteration -= 120;
						}
					}
					else {
						if ( testAction.iteration < testIteration ) // There is at least 1 missing build subunit action from the hack pattern...
							break;
					}
				} while ( testIndex-- > 0 && testAction.iteration >= minTestIteration );
				if ( patternSubunitBuildsCount >= HACK_COUNT_LIMIT && allSubunitBuildsCount == patternSubunitBuildsCount ) // To filter out human subunit build spam (this filters out some real hack cases too)
					hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_SUBUNIT_ENQUEUE, action.iteration ) );
			}
			
			// Multicommand hack: giving "several" actions in the same iteration
			// If actions being next to each other are the same actions (regardless to its parameters),
			// it can be due to lag and/or "action spam". Don't count and report those.
			// This "same" action checking is not completely correct, since we don't distinguish between a lot of actions,
			// but this is accurate enough since we parse and use the most common actions for hack detection
			if ( lastIteration == action.iteration && lastAction.actionNameIndex != action.actionNameIndex && action.actionNameIndex != Action.ACTION_NAME_INDEX_HOTKEY && action.actionNameIndex != Action.ACTION_NAME_INDEX_ALLY && action.actionNameIndex != Action.ACTION_NAME_INDEX_VISION )
				actionsCountForGeneralMulticommandHack++;
			else {
				if ( actionsCountForGeneralMulticommandHack > 20 )
					hackDescriptionList.add( new HackDescription( player.playerName, HackDescription.HACK_TYPE_MULTICOMMAND, lastAction.iteration ) );
				lastIteration = action.iteration;
				actionsCountForGeneralMulticommandHack = 0;
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
	 * @return true if parameters are correct; false otherwise
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
	 * @return true if parameters are correct; false otherwise
	 */
	private static boolean checkVisionParams( final String parameters ) {
		if ( parameters.charAt( 3 ) != '0' || parameters.charAt( 4 ) != '0' )
			return false;
		
		if ( parameters.charAt( 0 ) == '0' && parameters.charAt( 1 ) == '0' )
			return false;  // A player must always give vision to himself unless it's an UMS (which might allow "lights off")
		
		return true;
	}
	
}
