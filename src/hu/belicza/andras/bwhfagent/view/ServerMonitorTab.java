package hu.belicza.andras.bwhfagent.view;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import swingwt.awt.Font;
import swingwt.awt.GridLayout;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.JButton;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;
import swingwtx.swing.JScrollPane;

/**
 * Server monitor tab.
 * 
 * @author Andras Belicza
 */
public class ServerMonitorTab extends Tab {
	
	/** Name of the server list file. */
	private static final String SERVER_LIST_FILE_NAME = "server_list.txt";
	
	/** Name of the different ports. */
	private static final Map< Integer, String > PORT_NAME_MAP = new HashMap< Integer, String >();
	static {
		PORT_NAME_MAP.put( 80  , "Web"        );
		PORT_NAME_MAP.put( 6112, "Battle.net" );
		PORT_NAME_MAP.put( 21  , "Ftp"        );
	}
	
	/** Reference to the servers panel. */
	private final JPanel          serversPanel    = new JPanel();
	/** The list of the check buttons.  */
	private final List< JButton > checkButtonList = new ArrayList< JButton >();
	
	/**
	 * Creates a new ServerMOnitorTab.
	 */
	public ServerMonitorTab() {
		super( "Server monitor", IconResourceManager.ICON_SERVER_MONITOR );
		
		buildGUI();
		
		reloadServerList();
	}
	
	/**
	 * Builds the graphical user interface of the tab.
	 */
	protected void buildGUI() {
		final JPanel buttonsPanel = Utils.createWrapperPanel();
		final JButton checkAllServersButton = new JButton( "Check all servers", IconResourceManager.ICON_SERVER_CONNECT );
		checkAllServersButton.setMnemonic( checkAllServersButton.getText().charAt( 0 ) );
		checkAllServersButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				synchronized ( checkButtonList ) {
					for ( final JButton checkButton : checkButtonList )
						checkButton.doClick();
				}
			}
		} );
		buttonsPanel.add( checkAllServersButton );
		final JButton reloadServerListButton = new JButton( "Reload server list", IconResourceManager.ICON_ARROW_REFRESH );
		reloadServerListButton.setMnemonic( reloadServerListButton.getText().charAt( 0 ) );
		reloadServerListButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				reloadServerList();
			}
		} );
		buttonsPanel.add( reloadServerListButton );
		final JButton editServerListButton = new JButton( "Edit server list", IconResourceManager.ICON_EDIT );
		editServerListButton.setMnemonic( editServerListButton.getText().charAt( 0 ) );
		editServerListButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.editFile( SERVER_LIST_FILE_NAME );
			}
		} );
		buttonsPanel.add( editServerListButton );
		contentBox.add( buttonsPanel );
		
		final JScrollPane scrollPane = new JScrollPane( serversPanel );
		scrollPane.setBorder( BorderFactory.createTitledBorder( "Server list:" ) );
		contentBox.add( scrollPane );
	}
	
	/**
	 * Reloads the server list from the file.
	 */
	private void reloadServerList() {
		synchronized ( checkButtonList ) {
			try {
				final BufferedReader input = new BufferedReader( new FileReader( SERVER_LIST_FILE_NAME ) );
				
				checkButtonList.clear();
				for ( int i = serversPanel.getComponentCount() - 1; i >= 0; i-- )
					serversPanel.remove( i );
				
				final List< Object[] > serverList = new ArrayList< Object[] >();
				while ( input.ready() ) {
					final String line = input.readLine().trim();
					if ( line.length() == 0 || line.charAt( 0 ) == '#' )
						continue;
					
					final StringTokenizer lineTokenizer = new StringTokenizer( line );
					final String          server        = lineTokenizer.nextToken();
					
					String serverUrl = server;
					int    port      = 80;
					
					final int colonIndex = server.lastIndexOf( ':' );
					if ( colonIndex >= 0 )
						try {
							port      = Integer.parseInt( server.substring( colonIndex + 1 ) );
							serverUrl = server.substring( 0, colonIndex );
						}
						catch ( final Exception e ) {}
					
					serverList.add( new Object[] { serverUrl, port, lineTokenizer.nextToken( "" ) } );
				}
				
				// Build the server list panel
				serversPanel.setLayout( new GridLayout( serverList.size() + 1, 6, 10, 1 ) );
				serversPanel.add( createHeaderLabel( "Server:"  ) );
				serversPanel.add( createHeaderLabel( "Type:"    ) );
				serversPanel.add( createHeaderLabel( "Check:"   ) );
				serversPanel.add( createHeaderLabel( "Status:"  ) );
				serversPanel.add( createHeaderLabel( "Monitor:" ) );
				serversPanel.add( createHeaderLabel( "Visit:"   ) );
				
				for ( final Object[] server : serverList ) {
					serversPanel.add( new JLabel( (String) server[ 2 ], JLabel.CENTER ) );
					
					final int port = (Integer) server[ 1 ];
					final String portName = PORT_NAME_MAP.get( port );
					
					serversPanel.add( new JLabel( portName == null ? "Port " + port : portName, JLabel.CENTER ) );
					
					final JButton checkButton = new JButton( "Check", IconResourceManager.ICON_SERVER_CONNECT );
					final JLabel  statusLabel = new JLabel( "", JLabel.CENTER );
					checkButton.addActionListener( new ActionListener() {
						public void actionPerformed( final ActionEvent event ) {
							checkServer( (String) server[ 0 ], (Integer) server[ 1 ], checkButton, statusLabel );
						}
					} );
					serversPanel.add( checkButton );
					checkButtonList.add( checkButton );
					
					serversPanel.add( statusLabel );
					
					final JButton monitorButton = new JButton( "Monitor", IconResourceManager.ICON_MONITOR_SERVER );
					monitorButton.addActionListener( new ActionListener() {
						public void actionPerformed( final ActionEvent event ) {
							monitorButton.setText( "Stop" );
							monitorButton.setIcon( IconResourceManager.ICON_STOP_MONITOR );
						}
					} );
					serversPanel.add( monitorButton );
					
					if ( port == 80 ) {
						final JButton visitButton = new JButton( "Visit", IconResourceManager.ICON_WORLD_GO );
						visitButton.addActionListener( new ActionListener() {
							public void actionPerformed( final ActionEvent event ) {
								Utils.showURLInBrowser( "http://" + (String) server[ 0 ] );
							}
						} );
						serversPanel.add( visitButton );
					}
					else
						serversPanel.add( new JLabel() );
				}
				
				input.close();
			}
			catch ( final FileNotFoundException fnfe ) {
				fnfe.printStackTrace();
			}
			catch ( final IOException ie ) {
				ie.printStackTrace();
			}
			serversPanel.getParent().validate();
		}
	}
	
	/**
	 * Creates a header label used in the server list table.
	 * @param text text of the header label
	 * @return a header label used in the server list table
	 */
	private static JLabel createHeaderLabel( final String text ) {
		final JLabel headerLabel = new JLabel( text, JLabel.CENTER );
		headerLabel.setFont( new Font( "Default", Font.BOLD, 9 ) );
		return headerLabel;
	}
	
	/**
	 * Checks a server in a new Thread.
	 */
	private void checkServer( final String serverUrl, final int port, final JButton sourceButton, final JLabel statusLabel ) {
		sourceButton.setEnabled( false );
		statusLabel.setText( "Checking..." );
		new NormalThread() {
			@Override
			public void run() {
				try {
					final Socket socket = new Socket( serverUrl, port );
					socket.getInputStream();
					socket.close();
					statusLabel.setIcon( IconResourceManager.ICON_ACCEPT );
				}
				catch ( final Exception e ) {
					statusLabel.setIcon( IconResourceManager.ICON_DELETE_REPLAY );
				}
				finally {
					sourceButton.setEnabled( true );
				}
			}
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
}
