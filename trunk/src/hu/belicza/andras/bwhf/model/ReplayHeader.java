package hu.belicza.andras.bwhf.model;

/**
 * Class modelling the header of a replay.
 * 
 * @author Andras Belicza
 */
public class ReplayHeader {
	
	// Size of the header section
	public static final int HEADER_SIZE = 0x279;
	
	// Constants for the header field values
	
	public static final byte GAME_ENGINE_STARCRAFT = (byte) 0x00;
	public static final byte GAME_ENGINE_BROODWAR  = (byte) 0x01;
	
	// Header fields
	
	public byte   gameEngine;
	public int    gameFrames;
	public byte[] padding0    = new byte[ 3  ];  // Always { 0x00, 0x00, 0x48 }
	public int    saveTime;
	public byte[] padding1    = new byte[ 12 ];  // Always { 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x00, 0x00, 0x00, 0x00 }
	public byte[] gameName    = new byte[ 28 ];  // Padding 0x00 as extra characters
	public short  mapWidth;
	public short  mapHeight;
	public byte[] padding2    = new byte[ 16 ];  // Unknown
	public byte[] creatorName = new byte[ 24 ];  // Padding 0x00 as extra characters
	public byte   padding3;                      // Unknown, map type?
	public byte[] mapName     = new byte[ 26 ];  // Padding 0x00 as extra characters
	public byte[] padding4    = new byte[ 38 ];  // Unknown
	public byte[] playerRecords = new byte[ 432 ]; // 12 player records, 12*36 bytes
	public byte[] playerColors  = new byte[ 8 * 4 ]; // Player spot color (ABGR)
	public byte[] playerSpotIndices = new byte[ 8 ]; 
	
}
