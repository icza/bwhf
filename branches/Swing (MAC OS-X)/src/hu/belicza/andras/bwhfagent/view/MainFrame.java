package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.hackerdb.ServerApiConsts;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import swingwt.awt.BorderLayout;
import swingwt.awt.CardLayout;
import swingwt.awt.Color;
import swingwt.awt.Component;
import swingwt.awt.Cursor;
import swingwt.awt.Dimension;
import swingwt.awt.Toolkit;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwt.awt.event.MouseAdapter;
import swingwt.awt.event.MouseEvent;
import swingwt.awt.event.MouseListener;
import swingwt.awt.event.WindowAdapter;
import swingwt.awt.event.WindowEvent;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.Box;
import swingwtx.swing.JButton;
import swingwtx.swing.JFrame;
import swingwtx.swing.JLabel;
import swingwtx.swing.JOptionPane;
import swingwtx.swing.JPanel;
import swingwtx.swing.SwingConstants;
import swingwtx.swing.event.ChangeEvent;
import swingwtx.swing.event.ChangeListener;

/**
 * The main frame of BWHFAgent.
 * 
 * @author Andras Belicza
 */
public class MainFrame extends JFrame {
	
	/** Stores the reference of the main frame. */
	private static MainFrame mainFrame;
	
	/**
	 * Returns the reference of the main frame.
	 * @return the reference of the main frame
	 */
	public static MainFrame getInstance() {
		return mainFrame;
	}
	
	/** Current version of the application. */
	public final String applicationVersion;
	
	/** Button to start to Starcraft.                          */
	protected final JButton    startScButton              = new JButton( "Start StarCraft", IconResourceManager.ICON_WRAITH );
	/** Button to minimize to tray.                            */
	protected final JButton    minimizeToTrayButton       = new JButton( "Minimize to tray", IconResourceManager.ICON_MINIMIZE );
	/** Label to display if starcraft folder is set correctly. */
	protected final JLabel     starcraftFolderStatusLabel = new JLabel();
	
	/** Card layout used for showing the contents of the tabs. */
	private final CardLayout cardLayout       = new CardLayout();
	/** Panel to hold and display the contents of the tabs.    */
	private final JPanel     tabsContentPanel = new JPanel( cardLayout );
	/** Reference to the visible tab.                          */
	private Tab              visibleTab;
	/** Navigation box.                                        */
	private final Box        navigationBox    = Box.createVerticalBox();
	
	/** Reference to the autoscan tab.         */
	protected final AutoscanTab        autoscanTab;
	/** Reference to the manual scan tab.      */
	protected final ManualScanTab      manualScanTab;
	/** Reference to the manual scan tab.      */
	protected final PlayerCheckerTab   playerCheckerTab;
	/** Reference to the charts tab.           */
	protected final ChartsTab          chartsTab;
	/** Reference to the charts tab.           */
	protected final ReplaySearchTab    replaySearchTab;
	/** Reference to the game chat tab.        */
	protected final GameChatTab        gameChatTab;
	/** Reference to the players' network tab. */
	protected final PlayersNetworkTab  playersNetworkTab;
	/** Reference to the player matcher tab.   */
	protected final PlayerMatcherTab   playerMatcherTab;
	/** Reference to the general settings tab. */
	protected final GeneralSettingsTab generalSettingsTab;
	
	/** Tabs in the main frame. */
	private final Tab[] tabs;
	
	/** Reference to the tray icon of BWHF Agent. */
	private TrayIcon trayIcon;
	/** Reference to the restore main window menu item in the popup menu of the tray icon. */
	private MenuItem restoreMainWindowMenuItem;
	/** Reference to the hide main window menu item in the popup menu of the tray icon.    */
	private MenuItem hideMainWindowMenuItem;
	/** Reference to the disable tray icon menu item in the popup menu of the tray icon.   */
	private MenuItem disableTrayIconMenuItem;
	/** Tells if the tray icon has been successfully added to the system tray.             */
	private boolean  trayIconInstalled;
	
	/** To know when we first make window visible. */
	private boolean windowHasBeenShown;
	
	/** Tells if we're in Wonderland (newew version is available). */
	private boolean inWonderland = false;
	
	/** The state of the navigation bar: true = collapsed, false = expanded. */
	private boolean navigationBarCollapsed;
	
	/**
	 * Creates a new MainFrame.
	 * @param applicationVersion the application version string
	 * @param arguments          arguments taken from the running environment
	 */
	public MainFrame( final String applicationVersion, final String[] arguments ) {
		if ( MainFrame.mainFrame != null )
			throw new RuntimeException( "Only one main frame is allowed per Java Virtual Machine!" );
		
		MainFrame.mainFrame = this;
		
		this.applicationVersion = applicationVersion;
		
		setTitle( Consts.APPLICATION_NAME );
		setIconImage( ( inWonderland ? IconResourceManager.ICON_BLUE_PILL : IconResourceManager.ICON_RED_PILL ).getImage() );
		
		generalSettingsTab = new GeneralSettingsTab(); // This has to be created fist, autoscan tab uses this.
		autoscanTab        = new AutoscanTab();
		manualScanTab      = new ManualScanTab();
		playerCheckerTab   = new PlayerCheckerTab();
		chartsTab          = new ChartsTab();
		replaySearchTab    = new ReplaySearchTab();
		gameChatTab        = new GameChatTab();
		playersNetworkTab  = new PlayersNetworkTab();
		playerMatcherTab   = new PlayerMatcherTab();
		tabs = new Tab[] { autoscanTab, manualScanTab, playerCheckerTab, chartsTab, replaySearchTab, gameChatTab, new PcxConverterTab(), playersNetworkTab, playerMatcherTab, new ServerMonitorTab(), generalSettingsTab, new AboutTab() };
		
		buildGUI();
		
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( final WindowEvent event ) {
				closeAgent();
			}
			@Override
			public void windowIconified( final WindowEvent event ) {
				if ( trayIconInstalled && generalSettingsTab.alwaysMinimizeToTrayCheckBox.isSelected() )
					setVisible( false );
			}
		} );
		
		if ( generalSettingsTab.enableSystemTrayIconCheckBox.isSelected() )
			installSystemTrayIcon();
		
		File argumentFile = null;
		if ( arguments.length > 0 )
			argumentFile = new File( arguments[ 0 ] );
		
		if ( argumentFile != null && argumentFile.isFile() ) {
			setVisible( true );
			selectTab( chartsTab );
			chartsTab.setReplayFile( argumentFile );
		}
		else {
			// We have to call setVisible() even if we start minimized to tray, because menu items have to be initialized. 
			setVisible( !trayIconInstalled || !generalSettingsTab.startAgentMinimizedToTrayCheckBox.isSelected() );
			selectTab( autoscanTab );
		}
		
		if ( !new File( Consts.SETTINGS_PROPERTIES_FILE ).exists() ) {
			// First run of BWHF Agent
			JOptionPane.showMessageDialog( null, "Welcome!\nThis is the first run of BWHF Agent.\n\nPlease set the Starcraft directory in the General settings tab.\nDon't forget to re-enter your Key if you have one.\n\nOptionally you can copy the 'settings.properties' file from your old BWHF Agent's folder to keep the old settings.\n\nThank you for choosing BWHF Agent.", "Welcome", JOptionPane.INFORMATION_MESSAGE );
		}
	}
	
	/**
	 * Builds the graphical user interface.
	 */
	private void buildGUI() {
		final JPanel northBox = new JPanel( new BorderLayout() );
		JPanel panel = new JPanel( new BorderLayout() );
		panel.add( new JLabel( "Ver.: " + applicationVersion.substring( 0, applicationVersion.indexOf( ' ' ) ) + " © " + Consts.APPLICATION_AUTHOR ), BorderLayout.WEST );
		final JPanel startStarcraftPanel = Utils.createWrapperPanel();
		startScButton.setMnemonic( startScButton.getText().charAt( 0 ) );
		startScButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				startStarcraft();
			}
		} );
		startStarcraftPanel.add( startScButton );
		startStarcraftPanel.add( starcraftFolderStatusLabel );
		panel.add( startStarcraftPanel, BorderLayout.CENTER );
		minimizeToTrayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				setVisible( false );
			}
		} );
		panel.add( Utils.wrapInPanel( minimizeToTrayButton ), BorderLayout.EAST );
		northBox.add( panel, BorderLayout.CENTER );
		
		getContentPane().add( northBox, BorderLayout.NORTH );
		
		navigationBox.setBorder( BorderFactory.createTitledBorder( "Navigation:" ) );
		final JPanel[] tabLinePanels = new JPanel[ tabs.length ];
		final JLabel[] iconLabels    = new JLabel[ tabs.length ];
		final JLabel[] titleLabels   = new JLabel[ tabs.length ];
		for ( int tabIndex = 0; tabIndex < tabs.length; tabIndex++ ) {
			final Tab tab = tabs[ tabIndex ];
			tabsContentPanel.add( tab.getTitle(), tab.getContent() );
			final JPanel        tabLinePanel  = tabLinePanels[ tabIndex ] = Utils.createWrapperPanelLeftAligned();
			final JLabel        iconLabel     = iconLabels   [ tabIndex ] =  new JLabel( tab.getIcon(), SwingConstants.LEFT );
			final JLabel        titleLabel    = titleLabels  [ tabIndex ] = tab.getTitleLabel();
			final MouseListener mouseListener = new MouseAdapter() {
				@Override
				public void mousePressed( final MouseEvent event ) {
					selectTab( tab );
				}
			};
			iconLabel   .addMouseListener( mouseListener );
			titleLabel  .addMouseListener( mouseListener );
			tabLinePanel.addMouseListener( mouseListener );
			tabLinePanel.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
			tabLinePanel.add( iconLabel  );
			tabLinePanel.add( titleLabel );
			navigationBox.add( tabLinePanel );
		}
		
		navigationBox.add( new JPanel( new BorderLayout( 0, 0 ) ) ); // To consume the available space
		
		// Collapse expand label
		final JPanel        tabLinePanel  = Utils.createWrapperPanelLeftAligned();
		final JLabel        iconLabel     = new JLabel( IconResourceManager.ICON_ARROW_IN, SwingConstants.LEFT );
		final JLabel        titleLabel    = new JLabel( "Collapse" );
		tabLinePanel.setToolTipText( "Collapse the navigation bar" );
		iconLabel   .setToolTipText( "Collapse the navigation bar" );
		titleLabel  .setToolTipText( "Collapse the navigation bar" );
		final MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mousePressed( final MouseEvent event ) {
				final Dimension titleLabelDimension = navigationBarCollapsed ? null : new Dimension( 0, 0 );
				
				for ( int tabIndex = 0; tabIndex < tabs.length; tabIndex++ ) {
					titleLabels  [ tabIndex ].setPreferredSize( titleLabelDimension );
					final String toolTipText = navigationBarCollapsed ? null : titleLabels[ tabIndex ].getText();
					tabLinePanels[ tabIndex ].setToolTipText( toolTipText );
					iconLabels   [ tabIndex ].setToolTipText( toolTipText );
					titleLabels  [ tabIndex ].setToolTipText( toolTipText );
				}
				
				titleLabel.setPreferredSize( titleLabelDimension );
				iconLabel.setIcon( navigationBarCollapsed ? IconResourceManager.ICON_ARROW_IN : IconResourceManager.ICON_ARROW_OUT );
				final String toolTipText = navigationBarCollapsed ? "Collapse the navigation bar" : "Expand the navigation bar";
				tabLinePanel.setToolTipText( toolTipText );
				iconLabel   .setToolTipText( toolTipText );
				titleLabel  .setToolTipText( toolTipText );
				
				navigationBarCollapsed = !navigationBarCollapsed;
				navigationBox.getParent().validate();
			}
		};
		iconLabel   .addMouseListener( mouseListener );
		titleLabel  .addMouseListener( mouseListener );
		tabLinePanel.addMouseListener( mouseListener );
		tabLinePanel.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
		tabLinePanel.add( iconLabel  );
		tabLinePanel.add( titleLabel );
		navigationBox.add( tabLinePanel );
		
		getContentPane().add( navigationBox, BorderLayout.WEST );
		navigationBarCollapsed = false;
		if ( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_NAVIGATION_BAR_COLLAPSED ) ) )
			mouseListener.mousePressed( null );
		getContentPane().add( tabsContentPanel, BorderLayout.CENTER );
		
		
		if ( SystemTray.isSupported() ) {
			trayIcon = new TrayIcon( new javax.swing.ImageIcon( getClass().getResource( inWonderland ? IconResourceManager.ICON_RESOURCE_BLUE_PILL : IconResourceManager.ICON_RESOURCE_RED_PILL ) ).getImage() );
			trayIcon.setImageAutoSize( true );
			
			trayIcon.setToolTip( "BWHF Agent is running." );
			trayIcon.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed( java.awt.event.ActionEvent event ) {
					setVisible( true );
				}
			} );
			
			final PopupMenu popupMenu = new PopupMenu();
			final MenuItem startStarcraftMenuItem = new MenuItem( "Start Starcraft" );
			startStarcraftMenuItem.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed( final java.awt.event.ActionEvent event ) {
					startStarcraft();
				}
			} );
			popupMenu.add( startStarcraftMenuItem );
			final PopupMenu gatewayChangerPopupMenu = new PopupMenu( "Change gateway" );
			final Runnable setGatewayMenuItemStatesTask = new Runnable() {
				public void run() {
					final String selectedGateway = (String) autoscanTab.gatewayComboBox.getSelectedItem();
					for ( int i = gatewayChangerPopupMenu.getItemCount() - 1; i >= 0; i-- ) {
						final CheckboxMenuItem gatewayMenuItem = (CheckboxMenuItem) gatewayChangerPopupMenu.getItem( i );
						gatewayMenuItem.setState( gatewayMenuItem.getLabel().equals( selectedGateway ) );
					}
				}
			};
			for ( final String gateway : ServerApiConsts.GATEWAYS ) {
				final CheckboxMenuItem gatewayMenuItem = new CheckboxMenuItem( gateway );
				gatewayMenuItem.addItemListener( new ItemListener() {
					public void itemStateChanged( final ItemEvent event ) {
						autoscanTab.gatewayComboBox.setSelectedItem( gateway );
						setGatewayMenuItemStatesTask.run();
					}
				} );
				gatewayChangerPopupMenu.add( gatewayMenuItem );
			}
			setGatewayMenuItemStatesTask.run(); // To select the initial gateway
			autoscanTab.gatewayComboBox.addChangeListener( new ChangeListener() {
				public void stateChanged( final ChangeEvent event ) {
					setGatewayMenuItemStatesTask.run();
				}
			} );
			popupMenu.add( gatewayChangerPopupMenu );
			final PopupMenu lastReplayPopupMenu = new PopupMenu( "LastReplay" );
			final MenuItem showOnChartsMenuItem = new MenuItem( "Show on charts" );
			showOnChartsMenuItem.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed( final java.awt.event.ActionEvent event ) {
					MainFrame.this.setVisible( true );
					selectTab( chartsTab );
					chartsTab.setReplayFile( new File( generalSettingsTab.starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME ) );
				}
			} );
			lastReplayPopupMenu.add( showOnChartsMenuItem );
			final MenuItem displayGameChatMenuItem = new MenuItem( "Display game chat" );
			displayGameChatMenuItem.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed( final java.awt.event.ActionEvent event ) {
					MainFrame.this.setVisible( true );
					selectTab( gameChatTab );
					if ( gameChatTab.displayFromLastReplayButton.isEnabled() )
						gameChatTab.displayFromLastReplayButton.doClick();
				}
			} );
			lastReplayPopupMenu.add( displayGameChatMenuItem );
			final MenuItem scanMenuItem = new MenuItem( "Scan for hacks" );
			scanMenuItem.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed( final java.awt.event.ActionEvent event ) {
					MainFrame.this.setVisible( true );
					selectTab( manualScanTab );
					if ( manualScanTab.scanLastReplayButton.isEnabled() )
						manualScanTab.scanLastReplayButton.doClick();
				}
			} );
			lastReplayPopupMenu.add( scanMenuItem );
			popupMenu.add( lastReplayPopupMenu );
			popupMenu.addSeparator();
			restoreMainWindowMenuItem = new MenuItem( "Restore main window" );
			restoreMainWindowMenuItem.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed( final java.awt.event.ActionEvent event ) {
					MainFrame.this.setVisible( true );
				}
			} );
			popupMenu.add( restoreMainWindowMenuItem );
			hideMainWindowMenuItem = new MenuItem( "Hide main window" );
			hideMainWindowMenuItem.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed( final java.awt.event.ActionEvent event ) {
					MainFrame.this.setVisible( false );
				}
			} );
			//hideMainWindowMenuItem.setEnabled( false );
			popupMenu.add( hideMainWindowMenuItem );
			popupMenu.addSeparator();
			disableTrayIconMenuItem = new MenuItem( "Disable tray icon" );
			disableTrayIconMenuItem.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed( final java.awt.event.ActionEvent event ) {
					generalSettingsTab.enableSystemTrayIconCheckBox.setSelected( false );
					generalSettingsTab.enableSystemTrayIconCheckBox.doClick(); // This does not change the state of the checkbox
				}
			} );
			popupMenu.add( disableTrayIconMenuItem );
			final MenuItem closeMenuItem = new MenuItem( "Close BWHF Agent" );
			closeMenuItem.addActionListener( new java.awt.event.ActionListener() {
				public void actionPerformed( final java.awt.event.ActionEvent event ) {
					closeAgent();
				}
			} );
			popupMenu.add( closeMenuItem );
			
			trayIcon.setPopupMenu( popupMenu );
		}
	}
	
	@Override
	public void setVisible( final boolean visible ) {
		if ( restoreMainWindowMenuItem != null )
			restoreMainWindowMenuItem.setEnabled( !visible );
		if ( hideMainWindowMenuItem != null )
			hideMainWindowMenuItem.setEnabled( visible );
		if ( disableTrayIconMenuItem != null )
			disableTrayIconMenuItem.setEnabled( visible );
		
		boolean windowMaximized = false;
		if ( visible && !windowHasBeenShown ) {
			// First time
			final StringTokenizer boundsTokenizer = new StringTokenizer( generalSettingsTab.saveWindowPositionCheckBox.isSelected() ? Utils.settingsProperties.getProperty( Consts.PROPERTY_WINDOW_POSITION ) : Consts.DEFAULT_SETTINGS_PROPERTIES.getProperty( Consts.PROPERTY_WINDOW_POSITION ), "," );
			windowMaximized = boundsTokenizer.countTokens() != 4;
			if ( !windowMaximized ) {
				final int x      = Integer.parseInt( boundsTokenizer.nextToken() );
				final int y      = Integer.parseInt( boundsTokenizer.nextToken() );
				final int width  = Integer.parseInt( boundsTokenizer.nextToken() );
				final int height = Integer.parseInt( boundsTokenizer.nextToken() );
				setBounds( x, y, width, height );
			}
		}
		
		super.setVisible( visible );
		
		if ( visible ) {
			if ( getExtendedState() == ICONIFIED )
				setExtendedState( NORMAL ); // This restores state maximized if window was maximized
			
			if ( !windowHasBeenShown ) {
				if ( windowMaximized )
					setExtendedState( MAXIMIZED_BOTH );
				
				for ( final Tab tab : tabs )
					tab.initializationEnded();
				windowHasBeenShown = true;
			}
			
			toFront();
		}
	}
	
	/**
	 * Saves the properties and closes the agent.
	 */
	private void closeAgent() {
		// First our properties
		Utils.settingsProperties.setProperty( Consts.PROPERTY_NAVIGATION_BAR_COLLAPSED, Boolean.toString( navigationBarCollapsed ) );
		// And now the properties of the tabs
		for ( final Tab tab : tabs )
			tab.assignUsedProperties();
		
		Utils.saveSettingsProperties();
		System.exit( 0 );
	}
	
	/**
	 * Starts Starcraft.
	 */
	private void startStarcraft() {
		try {
			Runtime.getRuntime().exec( new File( generalSettingsTab.starcraftFolderTextField.getText(), Consts.STARCRAFT_EXECUTABLE_FILE_NAME ).getCanonicalPath(), null, new File( generalSettingsTab.starcraftFolderTextField.getText() ) );
		} catch ( final IOException ie ) {
			final JFrame errorFrame = new JFrame( "BWHF Agent error" );
			errorFrame.setIconImage( ( inWonderland ? IconResourceManager.ICON_BLUE_PILL : IconResourceManager.ICON_RED_PILL ).getImage() );
			errorFrame.getContentPane().add( Utils.wrapInPanel( new JLabel( "Could not start " + Consts.STARCRAFT_EXECUTABLE_FILE_NAME + "!" ) ), BorderLayout.NORTH );
			errorFrame.getContentPane().add( Utils.wrapInPanel( new JLabel( "Is Starcraft directory properly set?" ) ), BorderLayout.CENTER );
			final JButton closeButton = new JButton( "Close" );
			closeButton.setMnemonic( closeButton.getText().charAt( 0 ) );
			closeButton.addActionListener( new ActionListener() {
				public void actionPerformed( final ActionEvent event ) {
					errorFrame.dispose();
				}
			} );
			errorFrame.add( Utils.wrapInPanel( closeButton ), BorderLayout.SOUTH );
			errorFrame.setResizable( false );
			errorFrame.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
			errorFrame.pack();
			
			final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			errorFrame.setLocation( ( screenSize.width - 220 ) / 2, ( screenSize.height - 120 ) / 2 );
			
			errorFrame.setVisible( true );
			closeButton.requestFocusInWindow();
		}
	}
	
	/**
	 * Selects the specified tab.
	 * @param tab tab to be selected
	 */
	public void selectTab( final Tab tab ) {
		if ( visibleTab != null ) {
			visibleTab.getTitleLabel().setForeground( Color.BLACK );
			final Color defaultBackgroundColor = getContentPane().getBackground();
			( (JPanel) visibleTab.getTitleLabel().getParent() ).setBackground( defaultBackgroundColor );
			for ( final Component component : ( (JPanel) visibleTab.getTitleLabel().getParent() ).getComponents() )
				component.setBackground( defaultBackgroundColor );
		}
		
		visibleTab = tab;
		
		tab.getTitleLabel().setForeground( Color.RED );
		( (JPanel) visibleTab.getTitleLabel().getParent() ).setBackground( Color.YELLOW );
		for ( final Component component : ( (JPanel) visibleTab.getTitleLabel().getParent() ).getComponents() )
			component.setBackground( Color.YELLOW );
		
		cardLayout.show( tabsContentPanel, tab.getTitle() );
	}
	
	/**
	 * Installs a system tray icon for BWHF Agent.
	 */
	public void installSystemTrayIcon() {
		if ( trayIcon != null ) {
			final SystemTray systemTray = SystemTray.getSystemTray();
			if ( systemTray.getTrayIcons().length == 0 )
				try {
					systemTray.add( trayIcon );
					trayIconInstalled = true;
				} catch ( final AWTException ae ) {
				}
		}
	}
	
	/**
	 * Removes the installed system tray icon of BWHF Agent.
	 */
	public void removeSystemTrayIcon() {
		if ( trayIcon != null ) {
			SystemTray.getSystemTray().remove( trayIcon );
			trayIconInstalled = false;
		}
	}
	
	/**
	 * "Stays in Wonderland."<br>
	 * Changes the window icon and the tray icon to the blue pill.
	 */
	public void stayInWonderland() {
		inWonderland = true;
		setIconImage( IconResourceManager.ICON_BLUE_PILL.getImage() );
		if ( trayIcon != null )
			trayIcon.setImage( new javax.swing.ImageIcon( getClass().getResource( IconResourceManager.ICON_RESOURCE_BLUE_PILL ) ).getImage() );
	}
	
}
