package hu.belicza.andras.bnetbot;

/**
 * Class representing a binary packet sent to and received from a Battle.net server.
 *  
 * @author Andras Belicza
 */
public class BnetPacket {
	
	/** Packet identifier.         */
	public final byte   id;
	
	/** Data buffer of the packet. */
	public final byte[] data;
	
	/**
	 * Creates a new BnetPacket.
	 * @param data data buffer of the packet.
	 */
	public BnetPacket( final byte id, final byte[] data ) {
		this.id   = id;
		this.data = data;
	}
	
}
