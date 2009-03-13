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
public class Action implements Comparable< Action > {
	
	public static final byte ACTION_NAME_INDEX_UNKNOWN      = (byte) 0xff;
	public static final byte ACTION_NAME_INDEX_BWCHART_HACK = (byte) 0xfe;
	public static final byte ACTION_NAME_INDEX_ATTACK_MOVE  = (byte) 0xfd;
	public static final byte ACTION_NAME_INDEX_GATHER       = (byte) 0xfc;
	
	public static final byte ACTION_NAME_INDEX_CANCEL_TRAIN = (byte) 0x20;
	public static final byte ACTION_NAME_INDEX_0X33         = (byte) 0x33;
	public static final byte ACTION_NAME_INDEX_HATCH        = (byte) 0x23;
	public static final byte ACTION_NAME_INDEX_TRAIN        = (byte) 0x1f;
	public static final byte ACTION_NAME_INDEX_HOTKEY       = (byte) 0x13;
	public static final byte ACTION_NAME_INDEX_SELECT       = (byte) 0x09;
	public static final byte ACTION_NAME_INDEX_MOVE         = (byte) 0x14;
	public static final byte ACTION_NAME_INDEX_BUILD        = (byte) 0x0c;
	public static final byte ACTION_NAME_INDEX_ALLY         = (byte) 0x0e;
	public static final byte ACTION_NAME_INDEX_VISION       = (byte) 0x0d;
	
	/** Action IDs we're interested in when parsing exported text by BWChart. */
	public static final byte[] ACTION_IDS = {
		ACTION_NAME_INDEX_BWCHART_HACK,
		ACTION_NAME_INDEX_CANCEL_TRAIN,
		ACTION_NAME_INDEX_0X33,
		ACTION_NAME_INDEX_HATCH,
		ACTION_NAME_INDEX_TRAIN,
		ACTION_NAME_INDEX_HOTKEY,
		ACTION_NAME_INDEX_SELECT,
		ACTION_NAME_INDEX_MOVE,
		ACTION_NAME_INDEX_ATTACK_MOVE,
		ACTION_NAME_INDEX_GATHER,
		ACTION_NAME_INDEX_BUILD,
		ACTION_NAME_INDEX_ALLY,
		ACTION_NAME_INDEX_VISION
	};
	
	/** Map of unit IDs and their names. */
	public static final Map< Byte, String > ACTION_ID_NAME_MAP = new HashMap< Byte, String >();
	static {
		ACTION_ID_NAME_MAP.put( (byte) 0x09, "Select" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0a, "Shift Select" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0b, "Shift Deselect" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0c, "Build" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0d, "Vision" );
		ACTION_ID_NAME_MAP.put( (byte) 0x0e, "Ally" );
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
	
	
	public static final byte SUBACTION_NAME_INDEX_UNKNOWN = (byte) 0xff;
	
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
	
	public static final short UNIT_NAME_INDEX_UNKNOWN = (short) -1;
	public static final short UNIT_NAME_INDEX_SCV     = (short) 0x07;
	public static final short UNIT_NAME_INDEX_DRONE   = (short) 0x29;
	public static final short UNIT_NAME_INDEX_PROBE   = (short) 0x40;
	
	/** Unit IDs we're interested in when parsing exported text by BWChart. */
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
	 * @param name       name of the atcion
	 * @param parameters parameter string of the atcion
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
	 * Creates a new Action with pre-identified indices.
	 * 
	 * @param iteration                  iteration of the action
	 * @param parameters                 parameter string of the atcion
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
	 * @param parameters                 parameter string of the atcion
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
		return toString( null );
	}
	
	public String toString( final String playerName ) {
		String actionName = null;
		
		if ( subactionNameIndex != SUBACTION_NAME_INDEX_UNKNOWN )
			actionName = SUBACTION_ID_NAME_MAP.get( subactionNameIndex );
		if ( actionName == null && actionNameIndex != ACTION_NAME_INDEX_UNKNOWN ) {
			actionName = ACTION_ID_NAME_MAP.get( actionNameIndex );
			if ( actionName == null )
				actionName = "0x" + Integer.toHexString( actionNameIndex & 0xff );
		}
		if ( actionName == null )
			actionName = "<not parsed>";
		
		if ( playerName == null )
			return new Formatter().format( "%6d %-13s %s", iteration, actionName, parameters ).toString();
		else
			return new Formatter().format( "%6d %-25s %-15s %s", iteration, playerName, actionName, parameters ).toString();
	}
	
	public int compareTo( final Action anotherAction ) {
		return iteration < anotherAction.iteration ? -1 : ( iteration > anotherAction.iteration ? 1 : 0 );
	}
	
}
