package hu.belicza.andras.bwhf.model;

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
		"Attack Move"
	};
	
	
	public static final int UNIT_NAME_INDEX_UNKNOWN = -1;
	public static final int UNIT_NAME_INDEX_PROBE   =  0;
	public static final int UNIT_NAME_INDEX_DRONE   =  1;
	
	/** Unit names we're interested in. */
	public static final String[] UNIT_NAMES = {
		"Probe",
		"Drone"
	};
	
	
	public static final int BUILDING_NAME_INDEX_NON_BUILDING  = -1;
	public static final int BUILDING_NAME_INDEX_COMSAT        =  1;
	public static final int BUILDING_NAME_INDEX_CONTROL_TOWER =  9;
	public static final int BUILDING_NAME_INDEX_FIRST_ZERG_BUILDING = 18;
	public static final int BUILDING_NAME_INDEX_LAST_ZERG_BUILDING  = 33;
	
	/** Possible building names. */
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
	
	/** Iteration when this action was given. */
	public final int     iteration;
	/** Name of the action.                   */
	public final String  name;
	/** Parameter string of the action.       */
	public final String  parameters;
	/** Unit ids string of the action.        */
	public final String  unitIds;
	
	/** Constant for identifying the action name.       */
	public final int     actionNameIndex;
	/** Constant for identifying the action's unit.     */
	public final int     parameterUnitNameIndex;
	/** Constant for identifying the action's building. */
	public final int     parameterBuildingNameIndex;
	
	
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
	
	/**
	 * Creates a new Action with pre-identified indices.
	 * 
	 * @param iteration                  iteration of the action
	 * @param parameters                 parameter string of the atcion
	 * @param actionNameIndex            index determining the action name
	 * @param parameterUnitNameIndex     index determining the unit name
	 * @param parameterBuildingNameIndex index determining the building name
	 */
	public Action( final int iteration, final String parameters, final int actionNameIndex, final int parameterUnitNameIndex, final int parameterBuildingNameIndex ) { 
		this.iteration  = iteration;
		this.name       = null;
		this.parameters = parameters;
		this.unitIds    = null;
		
		this.actionNameIndex            = actionNameIndex;
		this.parameterUnitNameIndex     = parameterUnitNameIndex;
		this.parameterBuildingNameIndex = parameterBuildingNameIndex;
	}
}
