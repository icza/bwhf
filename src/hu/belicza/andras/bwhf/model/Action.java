package hu.belicza.andras.bwhf.model;

/**
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
	
	
	private static final String[] ACTION_NAMES = {
		"HACK",
		"Cancel Train",
		"!0x33",
		"Hatch",
		"Train",
		"Hotkey",
		"Select"
	};
	
	
	public static final int UNIT_NAME_INDEX_UNKNOWN = -1;
	public static final int UNIT_NAME_INDEX_PROBE   =  0;
	public static final int UNIT_NAME_INDEX_DRONE   =  1;
	
	public static final String[] UNIT_NAMES = {
		"Probe",
		"Drone"
	};
	
	
	public static final int BUILDING_NAME_INDEX_NON_BUILDING  = -1;
	public static final int BUILDING_NAME_INDEX_COMSAT        =  1;
	public static final int BUILDING_NAME_INDEX_CONTROL_TOWER =  9;
	public static final int BUILDING_NAME_INDEX_FIRST_ZERG_BUILDING = 18;
	public static final int BUILDING_NAME_INDEX_LAST_ZERG_BUILDING  = 33;
	
	public static final String[] BUILDING_NAMES = {
		"Command Center",
		"ComSat",
		"Nuclear Silo",
		"Supply Depot",
		"Refinery",
		"Barracks",
		"Academy",
		"Factory",
		"Starport",
		"Control Tower",
		"Science Facility",
		"Covert Ops",
		"Physics Lab",
		"Machine Shop",
		"Engineering Bay",
		"Armory",
		"Missile Turret",
		"Bunker",
		
		"Hatchery",
		"Lair",
		"Hive",
		"Nydus Canal",
		"Hydralisk Den",
		"Defiler Mound",
		"Greater Spire",
		"Queens Nest",
		"Evolution Chamber",
		"Ultralisk Cavern",
		"Spire",
		"Spawning Pool",
		"Creep Colony",
		"Spore Colony",
		"Sunken Colony",
		"Extractor",
		
		"Nexus",
		"Robotics Facility",
		"Pylon",
		"Assimilator",
		"Observatory",
		"Gateway",
		"Photon Cannon",
		"Citadel of Adun",
		"Cybernetics Core",
		"Templar Archives",
		"Forge",
		"Stargate",
		"Fleet Beacon",
		"Arbiter Tribunal",
		"Robotics Support Bay",
		"Shield Battery"
	};
	
	public static final String HOTKEY_ACTION_PARAM_NAME_SELECT = "Select";
	public static final String HOTKEY_ACTION_PARAM_NAME_ADD    = "Add";
	public static final String HOTKEY_ACTION_PARAM_NAME_ASSIGN = "Assign";
	
	public final int     iteration;
	public final String  name;
	public final String  parameters;
	public final String  unitIds;
	
	public final int     actionNameIndex;
	public final int     parameterUnitNameIndex;
	public final int     parameterBuildingNameIndex;
	
	
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
		
		int parameterBuildingNameIndex_ = BUILDING_NAME_INDEX_NON_BUILDING;
		if ( actionNameIndex == ACTION_NAME_INDEX_SELECT || actionNameIndex == ACTION_NAME_INDEX_BWCHART_HACK || actionNameIndex == ACTION_NAME_INDEX_TRAIN )
			for ( int i = BUILDING_NAMES.length - 1; i >= 0; i-- )
				if ( parameters.startsWith( BUILDING_NAMES[ i ] ) ) {
					parameterBuildingNameIndex_ = i;
					break;
				}
		parameterBuildingNameIndex = parameterBuildingNameIndex_;
		
		int parameterUnitNameIndex_ = UNIT_NAME_INDEX_UNKNOWN;
		if ( actionNameIndex == ACTION_NAME_INDEX_SELECT && parameterBuildingNameIndex == BUILDING_NAME_INDEX_NON_BUILDING )
			for ( int i = UNIT_NAMES.length - 1; i >= 0; i-- )
				if ( parameters.equals( UNIT_NAMES[ i ] ) ) {
					parameterUnitNameIndex_ = i;
					break;
				}
		parameterUnitNameIndex = parameterUnitNameIndex_;
	}
	
}
