package hu.belicza.andras.bwhf.model;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class modelling an action.
 * 
 * @author Andras Belicza
 */
public class Action {
	
	public static final int ACTION_NAME_INDEX_UNKNOWN      = -1;
	public static final int ACTION_NAME_INDEX_BWCHART_HACK =  0;
	public static final int ACTION_NAME_INDEX_CANCEL_TRAIN =  1;
	public static final int ACTION_NAME_INDEX_0X33         =  2;
	public static final int ACTION_NAME_INDEX_HATCH        =  3;
	public static final int ACTION_NAME_INDEX_TRAIN        =  4;
	public static final int ACTION_NAME_INDEX_HOTKEY       =  5;
	public static final int ACTION_NAME_INDEX_SELECT       =  6;
	public static final int ACTION_NAME_INDEX_MOVE         =  7;
	public static final int ACTION_NAME_INDEX_ATTACK_MOVE  =  8;
	public static final int ACTION_NAME_INDEX_GATHER       =  9;
	public static final int ACTION_NAME_INDEX_BUILD        = 10;
	public static final int ACTION_NAME_INDEX_ALLY         = 11;
	public static final int ACTION_NAME_INDEX_VISION       = 12;
	
	
	/** Possible action names. */
	public static final String[] ACTION_NAMES = {
		"HACK",
		"Cancel Train",
		"!0x33",
		"Hatch",
		"Train",
		"Hotkey",
		"Select",
		"Move",
		"Attack Move",
		"Gather",
		"Build",
		"Ally",
		"Vision"
	};
	
	
	public static final short UNIT_NAME_INDEX_UNKNOWN = (short) -1;
	public static final short UNIT_NAME_INDEX_SCV     = (short) 0x07;
	public static final short UNIT_NAME_INDEX_DRONE   = (short) 0x29;
	public static final short UNIT_NAME_INDEX_PROBE   = (short) 0x40;
	
	/** Unit IDs we're interested in. */
	public static final short[] UNIT_IDS = {
		UNIT_NAME_INDEX_SCV,
		UNIT_NAME_INDEX_DRONE,
		UNIT_NAME_INDEX_PROBE
	};
	
	
	public static final short BUILDING_NAME_INDEX_NON_BUILDING  = (short) -1;
	public static final short BUILDING_NAME_INDEX_COMSAT        = (short) 0x6b;
	public static final short BUILDING_NAME_INDEX_CONTROL_TOWER = (short) 0x73;
	public static final short BUILDING_NAME_INDEX_FIRST_ZERG_BUILDING = (short) 0x83;
	public static final short BUILDING_NAME_INDEX_LAST_ZERG_BUILDING  = (short) 0x95;
	
	/** Map of unit IDs and their names. */
	public static final Map< Short, String > UNIT_ID_NAME_MAP = new HashMap< Short, String >();
	static {
		UNIT_ID_NAME_MAP.put( (short) 0x00, "Marine" );
		UNIT_ID_NAME_MAP.put( (short) 0x01, "Ghost" );
		UNIT_ID_NAME_MAP.put( (short) 0x02, "Vulture" );
		UNIT_ID_NAME_MAP.put( (short) 0x03, "Goliath" );
		UNIT_ID_NAME_MAP.put( (short) 0x05, "Siege Tank" );
		UNIT_ID_NAME_MAP.put( (short) 0x07, "SCV" );
		UNIT_ID_NAME_MAP.put( (short) 0x08, "Wraith" );
		UNIT_ID_NAME_MAP.put( (short) 0x09, "Science Vessel" );
		UNIT_ID_NAME_MAP.put( (short) 0x0B, "Dropship" );
		UNIT_ID_NAME_MAP.put( (short) 0x0C, "Battlecruiser" );
		UNIT_ID_NAME_MAP.put( (short) 0x0E, "Nuke" );
		UNIT_ID_NAME_MAP.put( (short) 0x20, "Firebat" );
		UNIT_ID_NAME_MAP.put( (short) 0x22, "Medic" );
		UNIT_ID_NAME_MAP.put( (short) 0x25, "Zergling" );
		UNIT_ID_NAME_MAP.put( (short) 0x26, "Hydralisk" );
		UNIT_ID_NAME_MAP.put( (short) 0x27, "Ultralisk" );
		UNIT_ID_NAME_MAP.put( (short) 0x29, "Drone" );
		UNIT_ID_NAME_MAP.put( (short) 0x2A, "Overlord" );
		UNIT_ID_NAME_MAP.put( (short) 0x2B, "Mutalisk" );
		UNIT_ID_NAME_MAP.put( (short) 0x2C, "Guardian" );
		UNIT_ID_NAME_MAP.put( (short) 0x2D, "Queen" );
		UNIT_ID_NAME_MAP.put( (short) 0x2E, "Defiler" );
		UNIT_ID_NAME_MAP.put( (short) 0x2F, "Scourge" );
		UNIT_ID_NAME_MAP.put( (short) 0x32, "Infested Terran" );
		UNIT_ID_NAME_MAP.put( (short) 0x3A, "Valkyrie" );
		UNIT_ID_NAME_MAP.put( (short) 0x3C, "Corsair" );
		UNIT_ID_NAME_MAP.put( (short) 0x3D, "Dark Templar" );
		UNIT_ID_NAME_MAP.put( (short) 0x3E, "Devourer" );
		UNIT_ID_NAME_MAP.put( (short) 0x40, "Probe" );
		UNIT_ID_NAME_MAP.put( (short) 0x41, "Zealot" );
		UNIT_ID_NAME_MAP.put( (short) 0x42, "Dragoon" );
		UNIT_ID_NAME_MAP.put( (short) 0x43, "High Templar" );
		UNIT_ID_NAME_MAP.put( (short) 0x45, "Shuttle" );
		UNIT_ID_NAME_MAP.put( (short) 0x46, "Scout" );
		UNIT_ID_NAME_MAP.put( (short) 0x47, "Arbiter" );
		UNIT_ID_NAME_MAP.put( (short) 0x48, "Carrier" );
		UNIT_ID_NAME_MAP.put( (short) 0x53, "Reaver" );
		UNIT_ID_NAME_MAP.put( (short) 0x54, "Observer" );
		UNIT_ID_NAME_MAP.put( (short) 0x67, "Lurker" );
		UNIT_ID_NAME_MAP.put( (short) 0x6A, "Command Center" );
		UNIT_ID_NAME_MAP.put( (short) 0x6B, "ComSat" );
		UNIT_ID_NAME_MAP.put( (short) 0x6C, "Nuclear Silo" );
		UNIT_ID_NAME_MAP.put( (short) 0x6D, "Supply Depot" );
		UNIT_ID_NAME_MAP.put( (short) 0x6E, "Refinery" ); //refinery?
		UNIT_ID_NAME_MAP.put( (short) 0x6F, "Barracks" );
		UNIT_ID_NAME_MAP.put( (short) 0x70, "Academy" ); //Academy?
		UNIT_ID_NAME_MAP.put( (short) 0x71, "Factory" );
		UNIT_ID_NAME_MAP.put( (short) 0x72, "Starport" );
		UNIT_ID_NAME_MAP.put( (short) 0x73, "Control Tower" );
		UNIT_ID_NAME_MAP.put( (short) 0x74, "Science Facility" );
		UNIT_ID_NAME_MAP.put( (short) 0x75, "Covert Ops" );
		UNIT_ID_NAME_MAP.put( (short) 0x76, "Physics Lab" );
		UNIT_ID_NAME_MAP.put( (short) 0x78, "Machine Shop" );
		UNIT_ID_NAME_MAP.put( (short) 0x7A, "Engineering Bay" );
		UNIT_ID_NAME_MAP.put( (short) 0x7B, "Armory" );
		UNIT_ID_NAME_MAP.put( (short) 0x7C, "Missile Turret" );
		UNIT_ID_NAME_MAP.put( (short) 0x7D, "Bunker" );
		UNIT_ID_NAME_MAP.put( (short) 0x82, "Infested CC" );
		UNIT_ID_NAME_MAP.put( (short) 0x83, "Hatchery" );
		UNIT_ID_NAME_MAP.put( (short) 0x84, "Lair" );
		UNIT_ID_NAME_MAP.put( (short) 0x85, "Hive" );
		UNIT_ID_NAME_MAP.put( (short) 0x86, "Nydus Canal" );
		UNIT_ID_NAME_MAP.put( (short) 0x87, "Hydralisk Den" );
		UNIT_ID_NAME_MAP.put( (short) 0x88, "Defiler Mound" );
		UNIT_ID_NAME_MAP.put( (short) 0x89, "Greater Spire" );
		UNIT_ID_NAME_MAP.put( (short) 0x8A, "Queens Nest" );
		UNIT_ID_NAME_MAP.put( (short) 0x8B, "Evolution Chamber" );
		UNIT_ID_NAME_MAP.put( (short) 0x8C, "Ultralisk Cavern" );
		UNIT_ID_NAME_MAP.put( (short) 0x8D, "Spire" );
		UNIT_ID_NAME_MAP.put( (short) 0x8E, "Spawning Pool" );
		UNIT_ID_NAME_MAP.put( (short) 0x8F, "Creep Colony" );
		UNIT_ID_NAME_MAP.put( (short) 0x90, "Spore Colony" );
		UNIT_ID_NAME_MAP.put( (short) 0x92, "Sunken Colony" );
		UNIT_ID_NAME_MAP.put( (short) 0x95, "Extractor" );
		UNIT_ID_NAME_MAP.put( (short) 0x9A, "Nexus" );
		UNIT_ID_NAME_MAP.put( (short) 0x9B, "Robotics Facility" );
		UNIT_ID_NAME_MAP.put( (short) 0x9C, "Pylon" );
		UNIT_ID_NAME_MAP.put( (short) 0x9D, "Assimilator" );
		UNIT_ID_NAME_MAP.put( (short) 0x9F, "Observatory" );
		UNIT_ID_NAME_MAP.put( (short) 0xA0, "Gateway" );
		UNIT_ID_NAME_MAP.put( (short) 0xA2, "Photon Cannon" );
		UNIT_ID_NAME_MAP.put( (short) 0xA3, "Citadel of Adun" );
		UNIT_ID_NAME_MAP.put( (short) 0xA4, "Cybernetics Core" );
		UNIT_ID_NAME_MAP.put( (short) 0xA5, "Templar Archives" );
		UNIT_ID_NAME_MAP.put( (short) 0xA6, "Forge" );
		UNIT_ID_NAME_MAP.put( (short) 0xA7, "Stargate" );
		UNIT_ID_NAME_MAP.put( (short) 0xA9, "Fleet Beacon" );
		UNIT_ID_NAME_MAP.put( (short) 0xAA, "Arbiter Tribunal" );
		UNIT_ID_NAME_MAP.put( (short) 0xAB, "Robotics Support Bay" );
		UNIT_ID_NAME_MAP.put( (short) 0xAC, "Shield Battery" );
		UNIT_ID_NAME_MAP.put( (short) 0xC0, "Larva" );
		UNIT_ID_NAME_MAP.put( (short) 0xC1, "Rine/Bat" );
		UNIT_ID_NAME_MAP.put( (short) 0xC2, "Dark Archon" );
		UNIT_ID_NAME_MAP.put( (short) 0xC3, "Archon" );
		UNIT_ID_NAME_MAP.put( (short) 0xC4, "Scarab" );
		UNIT_ID_NAME_MAP.put( (short) 0xC5, "Interceptor" );
		UNIT_ID_NAME_MAP.put( (short) 0xC6, "Interceptor/Scarab" );		
	}
	
	public static final String HOTKEY_ACTION_PARAM_NAME_SELECT = "Select";
	public static final String HOTKEY_ACTION_PARAM_NAME_ADD    = "Add";
	public static final String HOTKEY_ACTION_PARAM_NAME_ASSIGN = "Assign";
	
	/** Iteration when this action was given. */
	public final int     iteration;
	/** Name of the action.                   */
	public String        name;
	/** Parameter string of the action.       */
	public final String  parameters;
	/** Unit ids string of the action.        */
	public final String  unitIds;
	
	/** Constant for identifying the action name.       */
	public final int     actionNameIndex;
	/** Constant for identifying the action's unit.     */
	public final short   parameterUnitNameIndex;
	/** Constant for identifying the action's building. */
	public final short   parameterBuildingNameIndex;
	
	
	/**
	 * Creates a new Action.
	 * @param iteration  iteration of the action
	 * @param name       name of the atcion
	 * @param parameters parameter string of the atcion
	 * @param unitIds    unit ids string of the action
	 */
	public Action( final int iteration, final String name, final String parameters, final String unitIds ) {
		this.iteration  = iteration;
		this.name       = name;
		this.parameters = parameters;
		this.unitIds    = unitIds;
		
		int actionNameIndex_ = ACTION_NAME_INDEX_UNKNOWN;
		for ( int i = ACTION_NAMES.length - 1; i >= 0; i-- )
			if ( ACTION_NAMES[ i ].equals( name ) ) {
				actionNameIndex_ = i;
				break;
			}
		actionNameIndex = actionNameIndex_;
		
		short parameterBuildingNameIndex_ = BUILDING_NAME_INDEX_NON_BUILDING;
		if ( actionNameIndex == ACTION_NAME_INDEX_SELECT || actionNameIndex == ACTION_NAME_INDEX_BWCHART_HACK || actionNameIndex == ACTION_NAME_INDEX_TRAIN )
			for ( final Entry< Short, String > entry : UNIT_ID_NAME_MAP.entrySet() )
				if ( parameters.startsWith( entry.getValue() ) ) {
					parameterBuildingNameIndex_ = entry.getKey();
					break;
				}
		parameterBuildingNameIndex = parameterBuildingNameIndex_;
		
		short parameterUnitNameIndex_ = UNIT_NAME_INDEX_UNKNOWN;
		if ( actionNameIndex == ACTION_NAME_INDEX_SELECT && parameterBuildingNameIndex == BUILDING_NAME_INDEX_NON_BUILDING )
			for ( final short unitId : UNIT_IDS )
				if ( parameters.equals( UNIT_ID_NAME_MAP.get( (byte) unitId ) ) ) {
					parameterUnitNameIndex_ = unitId;
					break;
				}
		parameterUnitNameIndex = parameterUnitNameIndex_;
	}
	
	/**
	 * Creates a new Action with pre-identified indices.
	 * 
	 * @param iteration                  iteration of the action
	 * @param parameters                 parameter string of the atcion
	 * @param actionNameIndex            index determining the action name
	 * @param parameterUnitNameIndex     index determining the unit name
	 * @param parameterBuildingNameIndex index determining the building name
	 */
	public Action( final int iteration, final String parameters, final int actionNameIndex, final short parameterUnitNameIndex, final short parameterBuildingNameIndex ) { 
		this.iteration  = iteration;
		this.name       = null;
		this.parameters = parameters;
		this.unitIds    = null;
		
		this.actionNameIndex            = actionNameIndex;
		this.parameterUnitNameIndex     = parameterUnitNameIndex;
		this.parameterBuildingNameIndex = parameterBuildingNameIndex;
	}
	
	@Override
	public String toString() {
		return iteration + "    " + ( name == null && actionNameIndex == ACTION_NAME_INDEX_UNKNOWN ? "<not parsed>" : ACTION_NAMES[ actionNameIndex ] ) + "    " + parameters;
	}
	
	public String toString( final String playerName ) {
		return new Formatter().format( "%5d %-25s %-13s %s", iteration, playerName, name == null && actionNameIndex == ACTION_NAME_INDEX_UNKNOWN ? "<not parsed>" : ACTION_NAMES[ actionNameIndex ], parameters ).toString();
	}
	
}
