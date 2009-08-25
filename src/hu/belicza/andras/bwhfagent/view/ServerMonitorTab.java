package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
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
import swingwtx.swing.JComboBox;
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
		PORT_NAME_MAP.put( 9367, "BNLS"       );
		PORT_NAME_MAP.put( 443 , "Secure Web" );
	}
	
	/** Reference to the servers panel.                                 */
	private final JPanel          serversPanel                   = new JPanel();
	/** The list of the check buttons.                                  */
	private final List< JButton > checkButtonList                = new ArrayList< JButton >();
	/** Combo box to set the monitor re-check time interval in seconds. */
	private final JComboBox       monitorRecheckIntervalComboBox = new JComboBox( new Object[] { 5, 10, 15, 30, 60, 120 } );
	/** Check button list of the monitored servers.                     */
	private final List< JButton > monitoredServerButtonList      = new ArrayList< JButton >();
	
	/** Reference to the monitor thread. */
	private volatile NormalThread monitorThread;
	
	/**
	 * Creates a new ServerMonitorTab.
	 */
	public ServerMonitorTab() {
		super( "Server monitor", IconResourceManager.ICON_SERVER_MONITOR );
		
		buildGUI();
		
		monitorRecheckIntervalComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_MONITOR_RECHECK_INTERVAL ) ) );
		
		reloadServerList();
	}
	
	/**
	 * Builds the graphical user interface of the tab.
	 */
	protected void buildGUI() {
		final JPanel settingsPanel = Utils.createWrapperPanel();
		settingsPanel.add( new JLabel( "Re-check time interval for monitored servers:" ) );
		settingsPanel.add( monitorRecheckIntervalComboBox );
		settingsPanel.add( new JLabel( "seconds." ) );
		contentBox.add( settingsPanel );
		
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
		stopMonitoring();
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
					
					final JButton checkButton   = new JButton( "Check", IconResourceManager.ICON_SERVER_CONNECT );
					final JLabel  statusLabel   = new JLabel( "", JLabel.CENTER );
					final JButton monitorButton = new JButton( "Monitor", IconResourceManager.ICON_MONITOR_SERVER );
					
					checkButton.addActionListener( new ActionListener() {
						public void actionPerformed( final ActionEvent event ) {
							checkServer( (String) server[ 0 ], (Integer) server[ 1 ], checkButton, monitorButton, statusLabel );
						}
					} );
					serversPanel.add( checkButton );
					checkButtonList.add( checkButton );
					
					serversPanel.add( statusLabel );
					
					monitorButton.addActionListener( new ActionListener() {
						public void actionPerformed( final ActionEvent event ) {
							if ( monitorButton.getText().equals( "Monitor" ) ) {
								disableMonitorButton( monitorButton );
								synchronized ( monitoredServerButtonList ) {
									monitoredServerButtonList.add( checkButton );
								}
								
								if ( monitorThread == null )
									startMonitoring();
							}
							else {
								synchronized ( monitoredServerButtonList ) {
									monitoredServerButtonList.remove( checkButton );
								}
								enableMonitorButton( monitorButton );
								// The monitor thread stops itself if there is nothing to be monitored.
							}
						}
					} );
					serversPanel.add( monitorButton );
					
					if ( port == 80 || port == 443 ) {
						final JButton visitButton = new JButton( "Visit", IconResourceManager.ICON_WORLD_GO );
						visitButton.addActionListener( new ActionListener() {
							public void actionPerformed( final ActionEvent event ) {
								Utils.showURLInBrowser( ( port == 80 ? "http://" : port == 443 ? "https://" : "" ) + (String) server[ 0 ] );
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
	 * Disables a monitor button.
	 * @param monitorButton monitor button to be disabled
	 */
	private void disableMonitorButton( final JButton monitorButton ) {
		monitorButton.setText( "Stop" );
		monitorButton.setIcon( IconResourceManager.ICON_STOP_MONITOR );
	}
	
	/**
	 * Enables a monitor button.
	 * @param monitorButton monitor button to be disabled
	 */
	private void enableMonitorButton( final JButton monitorButton ) {
		// This method is called from a synchronized block, no need (and must not use) another synchronized block! 
		monitoredServerButtonList.remove( monitorButton );
		monitorButton.setText( "Monitor" );
		monitorButton.setIcon( IconResourceManager.ICON_MONITOR_SERVER );
	}
	
	/**
	 * Checks a server in a new Thread.
	 * 
	 * @param serverUrl     url of the server
	 * @param port          port of the server
	 * @param checkButton   button that is associated with this check
	 * @param monitorButton monitor button that is associated with this server
	 * @param statusLabel   status label to be updated
	 */
	private void checkServer( final String serverUrl, final int port, final JButton checkButton, final JButton monitorButton, final JLabel statusLabel ) {
		if ( !checkButton.isEnabled() )
			return;
		
		checkButton.setEnabled( false );
		statusLabel.setIcon( IconResourceManager.ICON_WAITING );
		new NormalThread( "Server checker" ) {
			@Override
			public void run() {
				try {
					final Socket socket = new Socket( serverUrl, port );
					socket.getInputStream();
					socket.close();
					statusLabel.setIcon( IconResourceManager.ICON_TICK );
					synchronized ( monitoredServerButtonList ) {
						if ( monitoredServerButtonList.contains( checkButton ) ) {
							monitoredServerButtonList.remove( checkButton );
							enableMonitorButton( monitorButton );
							// TODO: play sound: "server is back online"
						}
					}
				}
				catch ( final Exception e ) {
					statusLabel.setIcon( IconResourceManager.ICON_DELETE_REPLAY );
				}
				finally {
					checkButton.setEnabled( true );
				}
			}
		}.start();
	}
	
	/**
	 * Starts the monitoring thread.
	 */
	private void startMonitoring() {
		if ( monitorThread != null )
			return;
		
		monitorThread = new NormalThread( "Server monitor" ) {
			@Override
			public void run() {
				try {
					long lastCheckTime = new Date().getTime();
					while ( true ) {
						sleep( 300l );
						
						synchronized ( monitoredServerButtonList ) {
							// We have to check this first because if reload was pressed, it will wait for this thread to die,
							// and it is a SwingWT thread meaning we can't query the selected item of monitorRecheckIntervalComboBox
							// (cause the SwingWT thread is blocked by us).
							if ( monitoredServerButtonList.isEmpty() )
								return;
						}
						
						final long recheckIntervalMs = (Integer) monitorRecheckIntervalComboBox.getSelectedItem() * 1000l;
						if ( lastCheckTime + recheckIntervalMs < new Date().getTime() ) {
							lastCheckTime = new Date().getTime();
							synchronized ( monitoredServerButtonList ) {
								for ( final JButton checkButton : monitoredServerButtonList )
									checkButton.doClick(); // The action listener might access the monitoredServerButtonList lock, but doClick() spawns a new thread, so there will be no dead-locking.
							}
						}
					}
				}
				catch ( InterruptedException ie ) {
				}
				finally {
					monitorThread = null;
				}
			}
		};
		
		monitorThread.start();
	}
	
	/**
	 * Stops the monitoring thread.
	 */
	private void stopMonitoring() {
		if ( monitorThread == null )
			return;
		
		synchronized ( monitoredServerButtonList ) {
			monitoredServerButtonList.clear();
		}
		
		try {
			monitorThread.join();
		}
		catch ( final InterruptedException ie ) {}
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_MONITOR_RECHECK_INTERVAL, Integer.toString( monitorRecheckIntervalComboBox.getSelectedIndex() ) );
	}
	
}
