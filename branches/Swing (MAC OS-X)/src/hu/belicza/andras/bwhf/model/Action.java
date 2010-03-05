package hu.belicza.andras.bwhf.model;

import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Class modeling an action.
 * 
 * @author Andras Belicza
 */
public class Action implements Comparable< Action > {
	
	/**
	 * Class to describe the size of a building.
	 * @author Andras Belicza
	 */
	public static class Size {
		/** Width of the building.  */
		public final int width;
		/** Height of the building. */
		public final int height;
		/**
		 * Creates a new Size.<br>
		 * Private because no need to create different size than the supplied constant ones.
		 * @param width  width of the building
		 * @param height height of the building
		 */
		private Size( final int width, final int height ) {
			this.width  = width;
			this.height = height;
		}
		/** Constant for size of 4x3. */
		public static final Size SIZE4X3 = new Size( 4, 3 );
		/** Constant for size of 4x2. */
		public static final Size SIZE4X2 = new Size( 4, 2 );
		/** Constant for size of 3x2. */
		public static final Size SIZE3X2 = new Size( 3, 2 );
		/** Constant for size of 2x2. */
		public static final Size SIZE2X2 = new Size( 2, 2 );
	}
	
	// These are my own constants reserved for special commands
	public static final byte ACTION_NAME_INDEX_UNKNOWN           = (byte) 0xff;
	public static final byte ACTION_NAME_INDEX_BWCHART_HACK      = (byte) 0xfe;
	public static final byte ACTION_NAME_INDEX_ATTACK_MOVE       = (byte) 0xfd;
	public static final byte ACTION_NAME_INDEX_GATHER            = (byte) 0xfc;
	public static final byte ACTION_NAME_INDEX_SET_RALLY         = (byte) 0xfb;
	
	public static final byte ACTION_NAME_INDEX_CANCEL            = (byte) 0x18;
	public static final byte ACTION_NAME_INDEX_CANCEL_TRAIN      = (byte) 0x20;
	public static final byte ACTION_NAME_INDEX_0X33              = (byte) 0x33;
	public static final byte ACTION_NAME_INDEX_HATCH             = (byte) 0x23;
	public static final byte ACTION_NAME_INDEX_CANCEL_HATCH      = (byte) 0x19;
	public static final byte ACTION_NAME_INDEX_TRAIN             = (byte) 0x1f;
	public static final byte ACTION_NAME_INDEX_HOTKEY            = (byte) 0x13;
	public static final byte ACTION_NAME_INDEX_USE_CHEAT         = (byte) 0x12;
	public static final byte ACTION_NAME_INDEX_SELECT            = (byte) 0x09;
	public static final byte ACTION_NAME_INDEX_SHIFT_SELECT      = (byte) 0x0a;
	public static final byte ACTION_NAME_INDEX_SHIFT_DESELECT    = (byte) 0x0b;
	public static final byte ACTION_NAME_INDEX_MOVE              = (byte) 0x14;
	public static final byte ACTION_NAME_INDEX_BUILD             = (byte) 0x0c;
	public static final byte ACTION_NAME_INDEX_ALLY              = (byte) 0x0e;
	public static final byte ACTION_NAME_INDEX_VISION            = (byte) 0x0d;
	public static final byte ACTION_NAME_INDEX_UNLOAD_ALL        = (byte) 0x28;
	public static final byte ACTION_NAME_INDEX_UNLOAD            = (byte) 0x29;
	public static final byte ACTION_NAME_INDEX_MORPH             = (byte) 0x35;
	public static final byte ACTION_NAME_INDEX_LEAVE             = (byte) 0x57;
	public static final byte ACTION_NAME_INDEX_BUILD_SUBUNIT     = (byte) 0x27;
	public static final byte ACTION_NAME_INDEX_STOP              = (byte) 0x1a;
	public static final byte ACTION_NAME_INDEX_HOLD              = (byte) 0x2b;
	public static final byte ACTION_NAME_INDEX_RESEARCH          = (byte) 0x30;
	public static final byte ACTION_NAME_INDEX_CANCEL_RESEARCH   = (byte) 0x31;
	public static final byte ACTION_NAME_INDEX_UPGRADE           = (byte) 0x32;
	public static final byte ACTION_NAME_INDEX_CANCEL_UPGRADE    = (byte) 0x33;
	public static final byte ACTION_NAME_INDEX_MERGE_ARCHON      = (byte) 0x2a;
	public static final byte ACTION_NAME_INDEX_MERGE_DARK_ARCHON = (byte) 0x5a;
	public static final byte ACTION_NAME_INDEX_LIFT              = (byte) 0x2f;
	
	public static final byte SUBACTION_NAME_INDEX_UNKNOWN     = (byte) 0xff;
	public static final byte SUBACTION_NAME_INDEX_UNLOAD      = (byte) 0x70;
	public static final byte SUBACTION_NAME_INDEX_LAUNCH_NUKE = (byte) 0x7e;
	public static final byte SUBACTION_NAME_INDEX_RECALL      = (byte) 0x8f;
	
	/** Set of actions that have an exact point target. */
	public static final Set< Byte > ACTION_NAME_INDICES_WITH_POINT_TARGET_SET = new HashSet< Byte >(
			Arrays.asList( ACTION_NAME_INDEX_ATTACK_MOVE, ACTION_NAME_INDEX_GATHER, ACTION_NAME_INDEX_SET_RALLY, ACTION_NAME_INDEX_MOVE,
					SUBACTION_NAME_INDEX_LAUNCH_NUKE, SUBACTION_NAME_INDEX_RECALL, (byte) 0x15 /* All subactions. */ )
	);
	
	/** Action IDs we're interested in when parsing exported text by BWChart.<br>
	 *  Parsing from exported BWChart text is no longer a goal. This might be inaccurate. */
	public static final byte[] ACTION_IDS = {
		ACTION_NAME_INDEX_BWCHART_HACK,
		ACTION_NAME_INDEX_CANCEL_TRAIN,
		ACTION_NAME_INDEX_0X33,
		ACTION_NAME_INDEX_HATCH,
		ACTION_NAME_INDEX_TRAIN,
		ACTION_NAME_INDEX_HOTKEY,
		ACTION_NAME_INDEX_USE_CHEAT, // BWChart uses different name for this, this won't be parsed from BWChart
		ACTION_NAME_INDEX_SELECT,
		ACTION_NAME_INDEX_MOVE,
		ACTION_NAME_INDEX_ATTACK_MOVE,
		ACTION_NAME_INDEX_GATHER,
		ACTION_NAME_INDEX_BUILD,
		ACTION_NAME_INDEX_ALLY,
		ACTION_NAME_INDEX_VISION
	};
	
	/** Map of action IDs and their names. */
	public static final Map< Byte, String > ACTION_ID_NAME_MAP = new HashMap< Byte, String >();
	static {
		ACTION_ID_NAME_MAP.put( (byte) 0x09, "Select" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0a, "Shift Select" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0b, "Shift Deselect" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0c, "Build" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0d, "Vision" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0e, "Ally" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0f, "Change Game Speed" );
		ACTION_ID_NAME_MAP.put( (byte) 0x12, "Use Cheat" );
		ACTION_ID_NAME_MAP.put( (byte) 0x13, "Hotkey" );
		ACTION_ID_NAME_MAP.put( (byte) 0x14, "Move" );
		ACTION_ID_NAME_MAP.put( (byte) 0x18, "Cancel" );
		ACTION_ID_NAME_MAP.put( (byte) 0x19, "Cancel Hatch" );
		ACTION_ID_NAME_MAP.put( (byte) 0x1a, "Stop" );
		ACTION_ID_NAME_MAP.put( (byte) 0x1e, "Return Chargo" );
		ACTION_ID_NAME_MAP.put( (byte) 0x1f, "Train" );
		ACTION_ID_NAME_MAP.put( (byte) 0x20, "Cancel Train" );
		ACTION_ID_NAME_MAP.put( (byte) 0x21, "Cloack" );
		ACTION_ID_NAME_MAP.put( (byte) 0x22, "Decloack" );
		ACTION_ID_NAME_MAP.put( (byte) 0x23, "Hatch" );
		ACTION_ID_NAME_MAP.put( (byte) 0x25, "Unsiege" );
		ACTION_ID_NAME_MAP.put( (byte) 0x26, "Siege" );
		ACTION_ID_NAME_MAP.put( (byte) 0x27, "Build Interceptor/Scarab" );
		ACTION_ID_NAME_MAP.put( (byte) 0x28, "Unload All" );
		ACTION_ID_NAME_MAP.put( (byte) 0x29, "Unload" );
		ACTION_ID_NAME_MAP.put( (byte) 0x2a, "Merge Archon" );
		ACTION_ID_NAME_MAP.put( (byte) 0x2b, "Hold Position" );
		ACTION_ID_NAME_MAP.put( (byte) 0x2c, "Burrow" );
		ACTION_ID_NAME_MAP.put( (byte) 0x2d, "Unborrow" );
		ACTION_ID_NAME_MAP.put( (byte) 0x2e, "Cancel Nuke" );
		ACTION_ID_NAME_MAP.put( (byte) 0x2f, "Lift" );
		ACTION_ID_NAME_MAP.put( (byte) 0x30, "Research" );
		ACTION_ID_NAME_MAP.put( (byte) 0x31, "Cancel Research" );
		ACTION_ID_NAME_MAP.put( (byte) 0x32, "Upgrade" );
		ACTION_ID_NAME_MAP.put( (byte) 0x33, "!0x33" );
		ACTION_ID_NAME_MAP.put( (byte) 0x35, "Morph" );
		ACTION_ID_NAME_MAP.put( (byte) 0x36, "Stim" );
		ACTION_ID_NAME_MAP.put( (byte) 0x57, "Leave Game" );
		ACTION_ID_NAME_MAP.put( (byte) 0x58, "Minimap Ping" );
		ACTION_ID_NAME_MAP.put( (byte) 0x5a, "Merge Dark Archon" );
		ACTION_ID_NAME_MAP.put( (byte) 0x5c, "Game Chat" );
		
		ACTION_ID_NAME_MAP.put( ACTION_NAME_INDEX_ATTACK_MOVE, "Attack Move" );
		ACTION_ID_NAME_MAP.put( ACTION_NAME_INDEX_GATHER, "Gather" );
	}
	
	/** Subactions of action 0x15 */
	public static final Map< Byte, String > SUBACTION_ID_NAME_MAP = new HashMap< Byte, String >();
	static {
		SUBACTION_ID_NAME_MAP.put( (byte) 0x00, "Move" ); // Move with right click
		SUBACTION_ID_NAME_MAP.put( (byte) 0x06, "Move" ); // Move by click move icon
		SUBACTION_ID_NAME_MAP.put( (byte) 0x08, "Attack" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x09, "Gather" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x0e, "Attack Move" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x13, "Failed Casting" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x1b, "Infest CC" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x22, "Repair" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x27, "Clear Rally" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x28, "Set Rally" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x4f, "Gather" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x50, "Gather" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x70, "Unload" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x71, "Yamato" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x73, "Lockdown" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x77, "Dark Swarm" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x78, "Parasite" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x79, "Spawn Broodling" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x7a, "EMP" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x7e, "Launch Nuke" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x84, "Lay Mine" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x8b, "Comsat Scan" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x8d, "Defense Matrix" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x8e, "Psionic Storm" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x8f, "Recall" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x90, "Plague" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x91, "Consume" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x92, "Ensnare" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x93, "Statis" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x94, "Hallucination" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0x98, "Patrol" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0xb1, "Heal" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0xb4, "Restore" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0xb5, "Distruption Web" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0xb6, "Mind Control" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0xb8, "Feedback" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0xb9, "Optic Flare" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0xba, "Maelstorm" );
		SUBACTION_ID_NAME_MAP.put( (byte) 0xc0, "Irradiate" );
	}
	
	/** Researches (parameters of action 0x30). */
	public static final Map< Byte, String > RESEARCH_ID_NAME_MAP = new HashMap< Byte, String >();
	static {
		RESEARCH_ID_NAME_MAP.put( (byte) 0x00, "Stim Pack" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x01, "Lockdown" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x02, "EMP Shockwave" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x03, "Spider Mines" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x05, "Siege Tank" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x07, "Irradiate" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x08, "Yamato Gun" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x09, "Cloacking Field (Wraith)" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x0a, "Personal Cloacking (Ghost)" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x0b, "Burrow" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x0d, "Spawn Broodling" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x0f, "Plague" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x10, "Consume" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x11, "Ensnare" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x13, "Psionic Storm" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x14, "Hallucination" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x15, "Recall" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x16, "Statis Field" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x18, "Restoration" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x19, "Distruption Web" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x1b, "Mind control" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x1e, "Optical Flare" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x1f, "Maelstorm" );
		RESEARCH_ID_NAME_MAP.put( (byte) 0x20, "Lurker Aspect" );
	}
	
	/** Upgrades (parameters of action 0x32). */
	public static final Map< Byte, String > UPGRADE_ID_NAME_MAP = new HashMap< Byte, String >();
	static {
		UPGRADE_ID_NAME_MAP.put( (byte) 0x00, "Terran Infantry Armor" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x01, "Terran Vehicle Plating" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x02, "Terran Ship Plating" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x03, "Zerg Carapace" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x04, "Zerg Flyer Carapace" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x05, "Protoss Ground Armor" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x06, "Protoss Air Armor" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x07, "Terran Infantry Weapons" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x08, "Terran Vehicle Weapons" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x09, "Terran Ship Weapons" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x0A, "Zerg Melee Attacks" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x0B, "Zerg Missile Attacks" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x0C, "Zerg Flyer Attacks" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x0D, "Protoss Ground Weapons" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x0E, "Protoss Air Weapons" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x0F, "Protoss Plasma Shields" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x10, "U-238 Shells (Marine Range)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x11, "Ion Thrusters (Vulture Speed)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x13, "Titan Reactor (Science Vessel Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x14, "Ocular Implants (Ghost Sight)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x15, "Moebius Reactor (Ghost Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x16, "Apollo Reactor (Wraith Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x17, "Colossus Reactor (Battle Cruiser Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x18, "Ventral Sacs (Overlord Transport)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x19, "Antennae (Overlord Sight)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x1A, "Pneumatized Carapace (Overlord Speed)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x1B, "Metabolic Boost (Zergling Speed)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x1C, "Adrenal Glands (Zergling Attack)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x1D, "Muscular Augments (Hydralisk Speed)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x1E, "Grooved Spines (Hydralisk Range)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x1F, "Gamete Meiosis (Queen Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x20, "Defiler Energy" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x21, "Singularity Charge (Dragoon Range)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x22, "Leg Enhancement (Zealot Speed)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x23, "Scarab Damage" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x24, "Reaver Capacity" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x25, "Gravitic Drive (Shuttle Speed)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x26, "Sensor Array (Observer Sight)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x27, "Gravitic Booster (Observer Speed)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x28, "Khaydarin Amulet (Templar Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x29, "Apial Sensors (Scout Sight)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x2A, "Gravitic Thrusters (Scout Speed)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x2B, "Carrier Capacity" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x2C, "Khaydarin Core (Arbiter Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x2F, "Argus Jewel (Corsair Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x31, "Argus Talisman (Dark Archon Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x33, "Caduceus Reactor (Medic Energy)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x34, "Chitinous Plating (Ultralisk Armor)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x35, "Anabolic Synthesis (Ultralisk Speed)" );
		UPGRADE_ID_NAME_MAP.put( (byte) 0x36, "Charon Boosters (Goliath Range)" );
	}
	
	public static final short UNIT_NAME_INDEX_UNKNOWN   = (short) -1;
	public static final short UNIT_NAME_INDEX_SCV       = (short) 0x07;
	public static final short UNIT_NAME_INDEX_DRONE     = (short) 0x29;
	public static final short UNIT_NAME_INDEX_PROBE     = (short) 0x40;
	public static final short UNIT_NAME_MINERAL_FIELD_1 = (short) 0xB0;
	public static final short UNIT_NAME_MINERAL_FIELD_2 = (short) 0xB1;
	public static final short UNIT_NAME_MINERAL_FIELD_3 = (short) 0xB2;
	public static final short UNIT_NAME_VESPENE_GEYSER  = (short) 0xBC;
	public static final short UNIT_NAME_START_LOCATION  = (short) 0xD6;
	
	/** Unit IDs we're interested in when parsing exported text by BWChart. */
	public static final short[] UNIT_IDS = {
		UNIT_NAME_INDEX_SCV,
		UNIT_NAME_INDEX_DRONE,
		UNIT_NAME_INDEX_PROBE
	};
	
	
	public static final short BUILDING_NAME_INDEX_NON_BUILDING        = (short) -1;
	public static final short BUILDING_NAME_INDEX_COMSAT              = (short) 0x6b;
	public static final short BUILDING_NAME_INDEX_CONTROL_TOWER       = (short) 0x73;
	public static final short BUILDING_NAME_INDEX_NEXUS               = (short) 0x9A;
	public static final short BUILDING_NAME_INDEX_COMMAND_CENTER      = (short) 0x6A;
	public static final short BUILDING_NAME_INDEX_REFINERY            = (short) 0x6E;
	public static final short BUILDING_NAME_INDEX_HATCHERY            = (short) 0x83;
	public static final short BUILDING_NAME_INDEX_BUNKER              = (short) 0x7D;
	public static final short BUILDING_NAME_INDEX_PHOTON_CANNON       = (short) 0xA2;
	public static final short BUILDING_NAME_INDEX_SUNKEN_COLONY       = (short) 0x92;
	public static final short BUILDING_NAME_INDEX_EXTRACTOR           = (short) 0x95;
	public static final short BUILDING_NAME_INDEX_ASSIMILATOR         = (short) 0x9D;
	public static final short BUILDING_NAME_INDEX_NYDUS_CANAL         = (short) 0x86;
	public static final short BUILDING_NAME_INDEX_FIRST_ZERG_BUILDING = (short) 0x83;
	public static final short BUILDING_NAME_INDEX_LAST_ZERG_BUILDING  = (short) 0x95;
	
	/** Map of unit IDs and their names. */
	public static final Map< Short, String > UNIT_ID_NAME_MAP = new HashMap< Short, String >();
	static {
		UNIT_ID_NAME_MAP.put( (short) 0x00, "Marine" );
		UNIT_ID_NAME_MAP.put( (short) 0x01, "Ghost" );
		UNIT_ID_NAME_MAP.put( (short) 0x02, "Vulture" );
		UNIT_ID_NAME_MAP.put( (short) 0x03, "Goliath" );
		UNIT_ID_NAME_MAP.put( (short) 0x04, "Goliath Turret" );
		UNIT_ID_NAME_MAP.put( (short) 0x05, "Siege Tank (Tank Mode)" );
		UNIT_ID_NAME_MAP.put( (short) 0x06, "Siege Tank Turret (Tank Mode)" );
		UNIT_ID_NAME_MAP.put( (short) 0x07, "SCV" );
		UNIT_ID_NAME_MAP.put( (short) 0x08, "Wraith" );
		UNIT_ID_NAME_MAP.put( (short) 0x09, "Science Vessel" );
		UNIT_ID_NAME_MAP.put( (short) 0x0A, "Gui Motang (Firebat)" );
		UNIT_ID_NAME_MAP.put( (short) 0x0B, "Dropship" );
		UNIT_ID_NAME_MAP.put( (short) 0x0C, "Battlecruiser" );
		UNIT_ID_NAME_MAP.put( (short) 0x0D, "Spider Mine" );
		UNIT_ID_NAME_MAP.put( (short) 0x0E, "Nuclear Missile" );
		UNIT_ID_NAME_MAP.put( (short) 0x0F, "Terran Civilian" );
		UNIT_ID_NAME_MAP.put( (short) 0x10, "Sarah Kerrigan (Ghost)" );
		UNIT_ID_NAME_MAP.put( (short) 0x11, "Alan Schezar (Goliath)" );
		UNIT_ID_NAME_MAP.put( (short) 0x12, "Alan Schezar Turret" );
		UNIT_ID_NAME_MAP.put( (short) 0x13, "Jim Raynor (Vulture)" );
		UNIT_ID_NAME_MAP.put( (short) 0x14, "Jim Raynor (Marine)" );
		UNIT_ID_NAME_MAP.put( (short) 0x15, "Tom Kazansky (Wraith)" );
		UNIT_ID_NAME_MAP.put( (short) 0x16, "Magellan (Science Vessel)" );
		UNIT_ID_NAME_MAP.put( (short) 0x17, "Edmund Duke (Tank Mode)" );
		UNIT_ID_NAME_MAP.put( (short) 0x18, "Edmund Duke Turret (Tank Mode)" );
		UNIT_ID_NAME_MAP.put( (short) 0x19, "Edmund Duke (Siege Mode)" );
		UNIT_ID_NAME_MAP.put( (short) 0x1A, "Edmund Duke Turret (Siege Mode)" );
		UNIT_ID_NAME_MAP.put( (short) 0x1B, "Arcturus Mengsk (Battlecruiser)" );
		UNIT_ID_NAME_MAP.put( (short) 0x1C, "Hyperion (Battlecruiser)" );
		UNIT_ID_NAME_MAP.put( (short) 0x1D, "Norad II (Battlecruiser)" );
		UNIT_ID_NAME_MAP.put( (short) 0x1E, "Terran Siege Tank (Siege Mode)" );
		UNIT_ID_NAME_MAP.put( (short) 0x1F, "Siege Tank Turret (Siege Mode)" );
		UNIT_ID_NAME_MAP.put( (short) 0x20, "Firebat" );
		UNIT_ID_NAME_MAP.put( (short) 0x21, "Scanner Sweep" );
		UNIT_ID_NAME_MAP.put( (short) 0x22, "Medic" );
		UNIT_ID_NAME_MAP.put( (short) 0x23, "Larva" );
		UNIT_ID_NAME_MAP.put( (short) 0x24, "Egg" );
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
		UNIT_ID_NAME_MAP.put( (short) 0x30, "Torrasque (Ultralisk)" );
		UNIT_ID_NAME_MAP.put( (short) 0x31, "Matriarch (Queen)" );
		UNIT_ID_NAME_MAP.put( (short) 0x32, "Infested Terran" );
		UNIT_ID_NAME_MAP.put( (short) 0x33, "Infested Kerrigan (Infested Terran)" );
		UNIT_ID_NAME_MAP.put( (short) 0x34, "Unclean One (Defiler)" );
		UNIT_ID_NAME_MAP.put( (short) 0x35, "Hunter Killer (Hydralisk)" );
		UNIT_ID_NAME_MAP.put( (short) 0x36, "Devouring One (Zergling)" );
		UNIT_ID_NAME_MAP.put( (short) 0x37, "Kukulza (Mutalisk)" );
		UNIT_ID_NAME_MAP.put( (short) 0x38, "Kukulza (Guardian)" );
		UNIT_ID_NAME_MAP.put( (short) 0x39, "Yggdrasill (Overlord)" );
		UNIT_ID_NAME_MAP.put( (short) 0x3A, "Valkyrie" );
		UNIT_ID_NAME_MAP.put( (short) 0x3B, "Mutalisk Cocoon" );
		UNIT_ID_NAME_MAP.put( (short) 0x3C, "Corsair" );
		UNIT_ID_NAME_MAP.put( (short) 0x3D, "Dark Templar" );
		UNIT_ID_NAME_MAP.put( (short) 0x3E, "Devourer" );
		UNIT_ID_NAME_MAP.put( (short) 0x3F, "Dark Archon" );
		UNIT_ID_NAME_MAP.put( (short) 0x40, "Probe" );
		UNIT_ID_NAME_MAP.put( (short) 0x41, "Zealot" );
		UNIT_ID_NAME_MAP.put( (short) 0x42, "Dragoon" );
		UNIT_ID_NAME_MAP.put( (short) 0x43, "High Templar" );
		UNIT_ID_NAME_MAP.put( (short) 0x44, "Archon" );
		UNIT_ID_NAME_MAP.put( (short) 0x45, "Shuttle" );
		UNIT_ID_NAME_MAP.put( (short) 0x46, "Scout" );
		UNIT_ID_NAME_MAP.put( (short) 0x47, "Arbiter" );
		UNIT_ID_NAME_MAP.put( (short) 0x48, "Carrier" );
		UNIT_ID_NAME_MAP.put( (short) 0x49, "Interceptor" );
		UNIT_ID_NAME_MAP.put( (short) 0x4A, "Protoss Dark Templar (Hero)" );
		UNIT_ID_NAME_MAP.put( (short) 0x4B, "Zeratul (Dark Templar)" );
		UNIT_ID_NAME_MAP.put( (short) 0x4C, "Tassadar/Zeratul (Archon)" );
		UNIT_ID_NAME_MAP.put( (short) 0x4D, "Fenix (Zealot)" );
		UNIT_ID_NAME_MAP.put( (short) 0x4E, "Fenix (Dragoon)" );
		UNIT_ID_NAME_MAP.put( (short) 0x4F, "Tassadar (Templar)" );
		UNIT_ID_NAME_MAP.put( (short) 0x50, "Mojo (Scout)" );
		UNIT_ID_NAME_MAP.put( (short) 0x51, "Warbringer (Reaver)" );
		UNIT_ID_NAME_MAP.put( (short) 0x52, "Gantrithor (Carrier)" );
		UNIT_ID_NAME_MAP.put( (short) 0x53, "Reaver" );
		UNIT_ID_NAME_MAP.put( (short) 0x54, "Observer" );
		UNIT_ID_NAME_MAP.put( (short) 0x55, "Scarab" );
		UNIT_ID_NAME_MAP.put( (short) 0x56, "Danimoth (Arbiter)" );
		UNIT_ID_NAME_MAP.put( (short) 0x57, "Aldaris (Templar)" );
		UNIT_ID_NAME_MAP.put( (short) 0x58, "Artanis (Scout)" );
		UNIT_ID_NAME_MAP.put( (short) 0x59, "Rhynadon (Badlands Critter)" );
		UNIT_ID_NAME_MAP.put( (short) 0x5A, "Bengalaas (Jungle Critter)" );
		UNIT_ID_NAME_MAP.put( (short) 0x5B, "Cargo Ship (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0x5C, "Mercenary Gunship (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0x5D, "Scantid (Desert Critter)" );
		UNIT_ID_NAME_MAP.put( (short) 0x5E, "Kakaru (Twilight Critter)" );
		UNIT_ID_NAME_MAP.put( (short) 0x5F, "Ragnasaur (Ashworld Critter)" );
		UNIT_ID_NAME_MAP.put( (short) 0x60, "Ursadon (Ice World Critter)" );
		UNIT_ID_NAME_MAP.put( (short) 0x61, "Lurker Egg" );
		UNIT_ID_NAME_MAP.put( (short) 0x62, "Raszagal (Corsair)" );
		UNIT_ID_NAME_MAP.put( (short) 0x63, "Samir Duran (Ghost)" );
		UNIT_ID_NAME_MAP.put( (short) 0x64, "Alexei Stukov (Ghost)" );
		UNIT_ID_NAME_MAP.put( (short) 0x65, "Map Revealer" );
		UNIT_ID_NAME_MAP.put( (short) 0x66, "Gerard DuGalle (BattleCruiser)" );
		UNIT_ID_NAME_MAP.put( (short) 0x67, "Lurker" );
		UNIT_ID_NAME_MAP.put( (short) 0x68, "Infested Duran (Infested Terran)" );
		UNIT_ID_NAME_MAP.put( (short) 0x69, "Disruption Web" );
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
		UNIT_ID_NAME_MAP.put( (short) 0x79, "Repair Bay (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0x7A, "Engineering Bay" );
		UNIT_ID_NAME_MAP.put( (short) 0x7B, "Armory" );
		UNIT_ID_NAME_MAP.put( (short) 0x7C, "Missile Turret" );
		UNIT_ID_NAME_MAP.put( (short) 0x7D, "Bunker" );
		UNIT_ID_NAME_MAP.put( (short) 0x7E, "Norad II (Crashed)" );
		UNIT_ID_NAME_MAP.put( (short) 0x7F, "Ion Cannon" );
		UNIT_ID_NAME_MAP.put( (short) 0x80, "Uraj Crystal" );
		UNIT_ID_NAME_MAP.put( (short) 0x81, "Khalis Crystal" );
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
		UNIT_ID_NAME_MAP.put( (short) 0x91, "Unused Zerg Building1" );
		UNIT_ID_NAME_MAP.put( (short) 0x92, "Sunken Colony" );
		UNIT_ID_NAME_MAP.put( (short) 0x93, "Zerg Overmind (With Shell)" );
		UNIT_ID_NAME_MAP.put( (short) 0x94, "Overmind" );
		UNIT_ID_NAME_MAP.put( (short) 0x95, "Extractor" );
		UNIT_ID_NAME_MAP.put( (short) 0x96, "Mature Chrysalis" );
		UNIT_ID_NAME_MAP.put( (short) 0x97, "Cerebrate" );
		UNIT_ID_NAME_MAP.put( (short) 0x98, "Cerebrate Daggoth" );
		UNIT_ID_NAME_MAP.put( (short) 0x99, "Unused Zerg Building2" );
		UNIT_ID_NAME_MAP.put( (short) 0x9A, "Nexus" );
		UNIT_ID_NAME_MAP.put( (short) 0x9B, "Robotics Facility" );
		UNIT_ID_NAME_MAP.put( (short) 0x9C, "Pylon" );
		UNIT_ID_NAME_MAP.put( (short) 0x9D, "Assimilator" );
		UNIT_ID_NAME_MAP.put( (short) 0x9E, "Unused Protoss Building1" );
		UNIT_ID_NAME_MAP.put( (short) 0x9F, "Observatory" );
		UNIT_ID_NAME_MAP.put( (short) 0xA0, "Gateway" );
		UNIT_ID_NAME_MAP.put( (short) 0xA1, "Unused Protoss Building2" );
		UNIT_ID_NAME_MAP.put( (short) 0xA2, "Photon Cannon" );
		UNIT_ID_NAME_MAP.put( (short) 0xA3, "Citadel of Adun" );
		UNIT_ID_NAME_MAP.put( (short) 0xA4, "Cybernetics Core" );
		UNIT_ID_NAME_MAP.put( (short) 0xA5, "Templar Archives" );
		UNIT_ID_NAME_MAP.put( (short) 0xA6, "Forge" );
		UNIT_ID_NAME_MAP.put( (short) 0xA7, "Stargate" );
		UNIT_ID_NAME_MAP.put( (short) 0xA8, "Stasis Cell/Prison" );
		UNIT_ID_NAME_MAP.put( (short) 0xA9, "Fleet Beacon" );
		UNIT_ID_NAME_MAP.put( (short) 0xAA, "Arbiter Tribunal" );
		UNIT_ID_NAME_MAP.put( (short) 0xAB, "Robotics Support Bay" );
		UNIT_ID_NAME_MAP.put( (short) 0xAC, "Shield Battery" );
		UNIT_ID_NAME_MAP.put( (short) 0xAD, "Khaydarin Crystal Formation" );
		UNIT_ID_NAME_MAP.put( (short) 0xAE, "Protoss Temple" );
		UNIT_ID_NAME_MAP.put( (short) 0xAF, "Xel'Naga Temple" );
		UNIT_ID_NAME_MAP.put( (short) 0xB0, "Mineral Field (Type 1)" );
		UNIT_ID_NAME_MAP.put( (short) 0xB1, "Mineral Field (Type 2)" );
		UNIT_ID_NAME_MAP.put( (short) 0xB2, "Mineral Field (Type 3)" );
		UNIT_ID_NAME_MAP.put( (short) 0xB3, "Cave (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xB4, "Cave-in (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xB5, "Cantina (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xB6, "Mining Platform (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xB7, "Independent Command Center (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xB8, "Independent Starport (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xB9, "Independent Jump Gate (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xBA, "Ruins (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xBB, "Khaydarin Crystal Formation (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xBC, "Vespene Geyser" );
		UNIT_ID_NAME_MAP.put( (short) 0xBD, "Warp Gate" );
		UNIT_ID_NAME_MAP.put( (short) 0xBE, "Psi Disrupter" );
		UNIT_ID_NAME_MAP.put( (short) 0xBF, "Zerg Marker" );
		UNIT_ID_NAME_MAP.put( (short) 0xC0, "Terran Marker" );
		UNIT_ID_NAME_MAP.put( (short) 0xC1, "Protoss Marker" );
		UNIT_ID_NAME_MAP.put( (short) 0xC2, "Zerg Beacon" );
		UNIT_ID_NAME_MAP.put( (short) 0xC3, "Terran Beacon" );
		UNIT_ID_NAME_MAP.put( (short) 0xC4, "Protoss Beacon" );
		UNIT_ID_NAME_MAP.put( (short) 0xC5, "Zerg Flag Beacon" );
		UNIT_ID_NAME_MAP.put( (short) 0xC6, "Terran Flag Beacon" );
		UNIT_ID_NAME_MAP.put( (short) 0xC7, "Protoss Flag Beacon" );
		UNIT_ID_NAME_MAP.put( (short) 0xC8, "Power Generator" );
		UNIT_ID_NAME_MAP.put( (short) 0xC9, "Overmind Cocoon" );
		UNIT_ID_NAME_MAP.put( (short) 0xCA, "Dark Swarm" );
		UNIT_ID_NAME_MAP.put( (short) 0xCB, "Floor Missile Trap" );
		UNIT_ID_NAME_MAP.put( (short) 0xCC, "Floor Hatch (Unused)" );
		UNIT_ID_NAME_MAP.put( (short) 0xCD, "Left Upper Level Door" );
		UNIT_ID_NAME_MAP.put( (short) 0xCE, "Right Upper Level Door" );
		UNIT_ID_NAME_MAP.put( (short) 0xCF, "Left Pit Door" );
		UNIT_ID_NAME_MAP.put( (short) 0xD0, "Right Pit Door" );
		UNIT_ID_NAME_MAP.put( (short) 0xD1, "Floor Gun Trap" );
		UNIT_ID_NAME_MAP.put( (short) 0xD2, "Left Wall Missile Trap" );
		UNIT_ID_NAME_MAP.put( (short) 0xD3, "Left Wall Flame Trap" );
		UNIT_ID_NAME_MAP.put( (short) 0xD4, "Right Wall Missile Trap" );
		UNIT_ID_NAME_MAP.put( (short) 0xD5, "Right Wall Flame Trap" );
		UNIT_ID_NAME_MAP.put( (short) 0xD6, "Start Location" );
		UNIT_ID_NAME_MAP.put( (short) 0xD7, "Flag" );
		UNIT_ID_NAME_MAP.put( (short) 0xD8, "Young Chrysalis" );
		UNIT_ID_NAME_MAP.put( (short) 0xD9, "Psi Emitter" );
		UNIT_ID_NAME_MAP.put( (short) 0xDA, "Data Disc" );
		UNIT_ID_NAME_MAP.put( (short) 0xDB, "Khaydarin Crystal" );
		UNIT_ID_NAME_MAP.put( (short) 0xDC, "Mineral Cluster Type 1" );
		UNIT_ID_NAME_MAP.put( (short) 0xDD, "Mineral Cluster Type 2" );
		UNIT_ID_NAME_MAP.put( (short) 0xDE, "Protoss Vespene Gas Orb Type 1" );
		UNIT_ID_NAME_MAP.put( (short) 0xDF, "Protoss Vespene Gas Orb Type 2" );
		UNIT_ID_NAME_MAP.put( (short) 0xE0, "Zerg Vespene Gas Sac Type 1" );
		UNIT_ID_NAME_MAP.put( (short) 0xE1, "Zerg Vespene Gas Sac Type 2" );
		UNIT_ID_NAME_MAP.put( (short) 0xE2, "Terran Vespene Gas Tank Type 1" );
		UNIT_ID_NAME_MAP.put( (short) 0xE3, "Terran Vespene Gas Tank Type 2" );
	}
	
	/** Map of building IDs and their sizes in matrices. */
	public static final Map< Short, Size > BUILDING_ID_SIZE_MAP = new HashMap< Short, Size >();
	static {
		BUILDING_ID_SIZE_MAP.put( (short) 0x6A, Size.SIZE4X3 ); // Command Center
		BUILDING_ID_SIZE_MAP.put( (short) 0x6B, Size.SIZE2X2 ); // ComSat
		BUILDING_ID_SIZE_MAP.put( (short) 0x6C, Size.SIZE2X2 ); // Nuclear Silo
		BUILDING_ID_SIZE_MAP.put( (short) 0x6D, Size.SIZE3X2 ); // Supply Depot
		BUILDING_ID_SIZE_MAP.put( (short) 0x6E, Size.SIZE4X2 ); // Refinery
		BUILDING_ID_SIZE_MAP.put( (short) 0x6F, Size.SIZE4X3 ); // Barracks
		BUILDING_ID_SIZE_MAP.put( (short) 0x70, Size.SIZE3X2 ); // Academy
		BUILDING_ID_SIZE_MAP.put( (short) 0x71, Size.SIZE4X3 ); // Factory
		BUILDING_ID_SIZE_MAP.put( (short) 0x72, Size.SIZE4X3 ); // Starport
		BUILDING_ID_SIZE_MAP.put( (short) 0x73, Size.SIZE2X2 ); // Control Tower
		BUILDING_ID_SIZE_MAP.put( (short) 0x74, Size.SIZE4X3 ); // Science Facility
		BUILDING_ID_SIZE_MAP.put( (short) 0x75, Size.SIZE2X2 ); // Covert Ops
		BUILDING_ID_SIZE_MAP.put( (short) 0x76, Size.SIZE2X2 ); // Physics Lab
		BUILDING_ID_SIZE_MAP.put( (short) 0x78, Size.SIZE2X2 ); // Machine Shop
		BUILDING_ID_SIZE_MAP.put( (short) 0x7A, Size.SIZE4X3 ); // Engineering Bay
		BUILDING_ID_SIZE_MAP.put( (short) 0x7B, Size.SIZE3X2 ); // Armory
		BUILDING_ID_SIZE_MAP.put( (short) 0x7C, Size.SIZE2X2 ); // Missile Turret
		BUILDING_ID_SIZE_MAP.put( (short) 0x7D, Size.SIZE3X2 ); // Bunker
		BUILDING_ID_SIZE_MAP.put( (short) 0x82, Size.SIZE4X3 ); // Infested CC
		BUILDING_ID_SIZE_MAP.put( (short) 0x83, Size.SIZE4X3 ); // Hatchery
		BUILDING_ID_SIZE_MAP.put( (short) 0x84, Size.SIZE4X3 ); // Lair
		BUILDING_ID_SIZE_MAP.put( (short) 0x85, Size.SIZE4X3 ); // Hive
		BUILDING_ID_SIZE_MAP.put( (short) 0x86, Size.SIZE2X2 ); // Nydus Canal
		BUILDING_ID_SIZE_MAP.put( (short) 0x87, Size.SIZE3X2 ); // Hydralisk Den
		BUILDING_ID_SIZE_MAP.put( (short) 0x88, Size.SIZE4X2 ); // Defiler Mound
		BUILDING_ID_SIZE_MAP.put( (short) 0x89, Size.SIZE2X2 ); // Greater Spire
		BUILDING_ID_SIZE_MAP.put( (short) 0x8A, Size.SIZE3X2 ); // Queens Nest
		BUILDING_ID_SIZE_MAP.put( (short) 0x8B, Size.SIZE3X2 ); // Evolution Chamber
		BUILDING_ID_SIZE_MAP.put( (short) 0x8C, Size.SIZE3X2 ); // Ultralisk Cavern
		BUILDING_ID_SIZE_MAP.put( (short) 0x8D, Size.SIZE2X2 ); // Spire
		BUILDING_ID_SIZE_MAP.put( (short) 0x8E, Size.SIZE3X2 ); // Spawning Pool
		BUILDING_ID_SIZE_MAP.put( (short) 0x8F, Size.SIZE2X2 ); // Creep Colony
		BUILDING_ID_SIZE_MAP.put( (short) 0x90, Size.SIZE2X2 ); // Spore Colony
		BUILDING_ID_SIZE_MAP.put( (short) 0x92, Size.SIZE2X2 ); // Sunken Colony
		BUILDING_ID_SIZE_MAP.put( (short) 0x95, Size.SIZE4X2 ); // Extractor
		BUILDING_ID_SIZE_MAP.put( (short) 0x9A, Size.SIZE4X3 ); // Nexus
		BUILDING_ID_SIZE_MAP.put( (short) 0x9B, Size.SIZE3X2 ); // Robotics Facility
		BUILDING_ID_SIZE_MAP.put( (short) 0x9C, Size.SIZE2X2 ); // Pylon
		BUILDING_ID_SIZE_MAP.put( (short) 0x9D, Size.SIZE4X2 ); // Assimilator
		BUILDING_ID_SIZE_MAP.put( (short) 0x9F, Size.SIZE3X2 ); // Observatory
		BUILDING_ID_SIZE_MAP.put( (short) 0xA0, Size.SIZE4X3 ); // Gateway
		BUILDING_ID_SIZE_MAP.put( (short) 0xA2, Size.SIZE2X2 ); // Photon Cannon
		BUILDING_ID_SIZE_MAP.put( (short) 0xA3, Size.SIZE3X2 ); // Citadel of Adun
		BUILDING_ID_SIZE_MAP.put( (short) 0xA4, Size.SIZE3X2 ); // Cybernetics Core
		BUILDING_ID_SIZE_MAP.put( (short) 0xA5, Size.SIZE3X2 ); // Templar Archives
		BUILDING_ID_SIZE_MAP.put( (short) 0xA6, Size.SIZE3X2 ); // Forge
		BUILDING_ID_SIZE_MAP.put( (short) 0xA7, Size.SIZE4X3 ); // Stargate
		BUILDING_ID_SIZE_MAP.put( (short) 0xA9, Size.SIZE3X2 ); // Fleet Beacon
		BUILDING_ID_SIZE_MAP.put( (short) 0xAA, Size.SIZE3X2 ); // Arbiter Tribunal
		BUILDING_ID_SIZE_MAP.put( (short) 0xAB, Size.SIZE3X2 ); // Robotics Support Bay
		BUILDING_ID_SIZE_MAP.put( (short) 0xAC, Size.SIZE3X2 ); // Shield Battery
	}
	
	/** Map of building IDs and their sizes in matrices. */
	public static final Map< Byte, String > GAME_SPEED_MAP = new HashMap< Byte, String >();
	static {
		GAME_SPEED_MAP.put( (byte) 0x00, "Slowest" );
		GAME_SPEED_MAP.put( (byte) 0x01, "Slower" );
		GAME_SPEED_MAP.put( (byte) 0x02, "Slow" );
		GAME_SPEED_MAP.put( (byte) 0x03, "Normal" );
		GAME_SPEED_MAP.put( (byte) 0x04, "Fast" );
		GAME_SPEED_MAP.put( (byte) 0x05, "Faster" );
		GAME_SPEED_MAP.put( (byte) 0x06, "Fastest" );
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
	public final byte    actionNameIndex;
	/** Constant for identifying the subaction name.    */
	public final byte    subactionNameIndex;
	/** Constant for identifying the action's unit.     */
	public final short   parameterUnitNameIndex;
	/** Constant for identifying the action's building. */
	public final short   parameterBuildingNameIndex;
	
	
	/**
	 * Creates a new Action.
	 * @param iteration  iteration of the action
	 * @param name       name of the action
	 * @param parameters parameter string of the action
	 * @param unitIds    unit ids string of the action
	 */
	public Action( final int iteration, final String name, final String parameters, final String unitIds ) {
		this.iteration  = iteration;
		this.name       = name;
		this.parameters = parameters;
		this.unitIds    = unitIds;
		
		byte actionNameIndex_ = ACTION_NAME_INDEX_UNKNOWN;
		for ( final byte actionId : ACTION_IDS )
			if ( name.equals( ACTION_ID_NAME_MAP.get( actionId ) ) ) {
				actionNameIndex_ = actionId;
				break;
			}
		actionNameIndex = actionNameIndex_;
		// Warning! This is not parsed since I don't initialize from BWChart anymore!
		subactionNameIndex = SUBACTION_NAME_INDEX_UNKNOWN;
		
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
	 * Creates a new Action with pre-identified indices.<br>
	 * Subaction, unit and building name indices are unknown.
	 * 
	 * @param iteration                  iteration of the action
	 * @param parameters                 parameter string of the action
	 * @param actionNameIndex            index determining the action name
	 */
	public Action( final int iteration, final String parameters, final byte actionNameIndex ) {
		this( iteration, parameters, actionNameIndex, SUBACTION_NAME_INDEX_UNKNOWN, UNIT_NAME_INDEX_UNKNOWN, BUILDING_NAME_INDEX_NON_BUILDING );
	}
	
	/**
	 * Creates a new Action with pre-identified indices.
	 * 
	 * @param iteration                  iteration of the action
	 * @param parameters                 parameter string of the action
	 * @param actionNameIndex            index determining the action name
	 * @param parameterUnitNameIndex     index determining the unit name
	 * @param parameterBuildingNameIndex index determining the building name
	 */
	public Action( final int iteration, final String parameters, final byte actionNameIndex, final short parameterUnitNameIndex, final short parameterBuildingNameIndex ) {
		this( iteration, parameters, actionNameIndex, SUBACTION_NAME_INDEX_UNKNOWN, parameterUnitNameIndex, parameterBuildingNameIndex );
	}
	
	/**
	 * Creates a new Action with pre-identified indices.
	 * 
	 * @param iteration                  iteration of the action
	 * @param parameters                 parameter string of the action
	 * @param actionNameIndex            index determining the action name
	 * @param subactionNameIndex         index determining the subaction name
	 * @param parameterUnitNameIndex     index determining the unit name
	 * @param parameterBuildingNameIndex index determining the building name
	 */
	public Action( final int iteration, final String parameters, final byte actionNameIndex, final byte subactionNameIndex, final short parameterUnitNameIndex, final short parameterBuildingNameIndex ) { 
		this.iteration  = iteration;
		this.name       = null;
		this.parameters = parameters;
		this.unitIds    = null;
		
		this.actionNameIndex            = actionNameIndex;
		this.subactionNameIndex         = subactionNameIndex;
		this.parameterUnitNameIndex     = parameterUnitNameIndex;
		this.parameterBuildingNameIndex = parameterBuildingNameIndex;
	}
	
	@Override
	public String toString() {
		return toString( null, false );
	}
	
	
	/** Attribute to cache the value returned by the <code>toString()</code> method. */
	private String toStringValue;
	/** Attribute to cache the value returned by the <code>toString()</code> method with seconds. */
	private String toStringValueSeconds;
	
	/**
	 * Returns the string representation of this action owned by the specified player.
	 * @param playerName    name of player owning this action
	 * @param timeInSeconds tells if time has to be printed as seconds instead of iteration
	 * @return the string representation of this action owned by the specified player
	 */
	public String toString( final String playerName, final boolean timeInSeconds ) {
		if ( timeInSeconds && toStringValueSeconds == null || !timeInSeconds && toStringValue == null ) {
			String actionName = null;
			
			if ( subactionNameIndex != SUBACTION_NAME_INDEX_UNKNOWN )
				actionName = SUBACTION_ID_NAME_MAP.get( subactionNameIndex );
			if ( actionName == null && actionNameIndex != ACTION_NAME_INDEX_UNKNOWN ) {
				actionName = ACTION_ID_NAME_MAP.get( actionNameIndex );
				if ( actionName == null )
					actionName = "0x" + Integer.toHexString( actionNameIndex & 0xff );
			}
			if ( actionName == null ) {
				actionName = "<not parsed>";
			}
			
			final StringBuilder actionStringBuilder = new StringBuilder();
			if ( timeInSeconds )
				ReplayHeader.formatFrames( iteration, actionStringBuilder, true );
			
			final Formatter actionStringFormatter = new Formatter( actionStringBuilder );
			if ( playerName == null ) {
				if ( timeInSeconds )
					actionStringFormatter.format( " %-13s %s", actionName, parameters );
				else
					actionStringFormatter.format( "%6d %-13s %s", iteration, actionName, parameters );
			}
			else {
				if ( timeInSeconds )
					actionStringFormatter.format( " %-25s %-17s %s", playerName, actionName, parameters );
				else
					actionStringFormatter.format( "%6d %-25s %-17s %s", iteration, playerName, actionName, parameters );
			}
			
			if ( timeInSeconds )
				toStringValueSeconds = actionStringBuilder.toString();
			else
				toStringValue = actionStringBuilder.toString();
		}
		
		return timeInSeconds ? toStringValueSeconds : toStringValue;
	}
	
	/**
	 * Compares this action to another based on their iteration.
	 * @param anotherAction another action to compare to
	 * @return 1 if this action has greater iteration than the other; 0 if both have the same iteration; -1 if this action has smaller iteration than the other
	 */
	public int compareTo( final Action anotherAction ) {
		return iteration - anotherAction.iteration;
	}
	
}
