package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.io.IOException;

import swingwt.awt.BorderLayout;
import swingwt.awt.Color;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwt.awt.event.KeyEvent;
import swingwt.awt.event.KeyListener;
import swingwt.awt.event.WindowAdapter;
import swingwt.awt.event.WindowEvent;
import swingwtx.swing.ImageIcon;
import swingwtx.swing.JButton;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JFrame;
import swingwtx.swing.JLabel;
import swingwtx.swing.JOptionPane;
import swingwtx.swing.JPanel;
import swingwtx.swing.JTabbedPane;
import swingwtx.swing.JTextField;
import swingwtx.swing.event.ChangeEvent;
import swingwtx.swing.event.ChangeListener;

/**
 * The main frame of BWHFAgent.
 * 
 * @author Andras Belicza
 */
public class MainFrame extends JFrame {
	
	/** Name of the image resource file to be used as icon image. */
	private static final String ICON_IMAGE_RESOURCE_NAME = "redpill.gif";
	
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
	
	/** Button to start/switch to Starcraft.                   */
	private final JButton    startScButton              = new JButton( "Start/Switch to Starcraft" );
	/** Starcraft directory.                                   */
	public final  JTextField starcraftFolderTextField   = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_STARCRAFT_FOLDER ) );
	/** Label to display if starcraft folder is set correctly. */
	private final JLabel     starcraftFolderStatusLabel = new JLabel();
	
	/** The tabbed pane holding the tabs. */
	private final JTabbedPane tabbedPane = new JTabbedPane();
	
	/** Reference to the charts tab.           */
	public final ChartsTab          chartsTab;
	/** Reference to the general settings tab. */
	public final GeneralSettingsTab generalSettingsTab;
	
	/** Tabs in the main frame. */
	private final Tab[] tabs;
	
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
		setIconImage( new ImageIcon( getClass().getResource( ICON_IMAGE_RESOURCE_NAME ) ).getImage() );
		
		chartsTab          = new ChartsTab();
		generalSettingsTab = new GeneralSettingsTab();
		tabs = new Tab[] { new AutoscanTab(), new ManualScanTab(), chartsTab, new GameChatTab(), new PcxConverterTab(), generalSettingsTab, new AboutTab() };
		setBounds( 50, 20, 950, 700 );
		buildGUI();
		checkStarcraftFolder();
		
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( final WindowEvent we ) {
				Utils.settingsProperties.setProperty( Consts.PROPERTY_STARCRAFT_FOLDER, starcraftFolderTextField.getText() );
				for ( final Tab tab : tabs )
					tab.assignUsedProperties();
				
				Utils.saveSettingsProperties();
				System.exit( 0 );
			}
		} );
		
		setBounds( 50, 20, 950, 700 );
		setVisible( true );
		
		for ( final Tab tab : tabs )
			tab.initializationEnded();
		
		if ( arguments.length > 0 ) {
			final File argumentFile = new File( arguments[ 0 ] );
			if ( argumentFile.isFile() ) {
				chartsTab.setReplayFile( new File( arguments[ 0 ] ) );
				selectTab( chartsTab );
			}
		}
	}
	
	/**
	 * Builds the graphical user interface.
	 */
	private void buildGUI() {
		final JPanel northPanel = new JPanel( new BorderLayout() );
		final JPanel panel = Utils.createWrapperPanel();
		startScButton.setMnemonic( startScButton.getText().charAt( 0 ) );
		startScButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					Runtime.getRuntime().exec( new File( starcraftFolderTextField.getText(), Consts.STARCRAFT_EXECUTABLE_FILE_NAME ).getCanonicalPath(), null, new File( starcraftFolderTextField.getText() ) );
				} catch ( final IOException ie ) {
					JOptionPane.showMessageDialog( MainFrame.this, "Cannot start " + Consts.STARCRAFT_EXECUTABLE_FILE_NAME + "!\nIs Starcraft directory properly set?", "Error", JOptionPane.ERROR_MESSAGE );
				}
			}
		} );
		panel.add( startScButton );
		panel.add( starcraftFolderStatusLabel );
		northPanel.add( panel, BorderLayout.NORTH );
		northPanel.add( new JLabel( "Starcraft directory:" ), BorderLayout.WEST );
		// This is a workaround becase SwingWT does not implement DocumentListener correctly :S
		starcraftFolderTextField.addKeyListener( new KeyListener() {
			public void keyPressed( final KeyEvent event ) {
				checkStarcraftFolder();
			}
			public void keyReleased( final KeyEvent event ) {
				checkStarcraftFolder();
			}
			public void keyTyped( final KeyEvent event ) {
				checkStarcraftFolder();
			}
		} );
		northPanel.add( starcraftFolderTextField, BorderLayout.CENTER );
		northPanel.add( Utils.createFileChooserButton( this, starcraftFolderTextField, JFileChooser.DIRECTORIES_ONLY, null, null, new Runnable() {
			public void run() {
				checkStarcraftFolder();
			}
		} ), BorderLayout.EAST );
		getContentPane().add( Utils.wrapInPanel( northPanel ), BorderLayout.NORTH );
		
		for ( int tabIndex = 0; tabIndex < tabs.length; tabIndex++ ) {
			final Tab tab = tabs[ tabIndex ];
			final char mnemonicChar = Integer.toString( tabIndex + 1 ).charAt( 0 );
			tabbedPane.add( mnemonicChar + " " + tab.getTitle(), tab.getContent() );
			// SwingWT doesn't support mnemonics for tabs
			//tabbedPane.setMnemonicAt( tabIndex, mnemonicChar );
		}
		
		tabbedPane.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				tabs[ tabbedPane.getSelectedIndex() ].onSelected();
			}
		} );
		
		getContentPane().add( tabbedPane, BorderLayout.CENTER );
		
		tabbedPane.setSelectedIndex( 0 );
	}
	
	/**
	 * Selects the specified tab.
	 * @param tab tab to be selected
	 */
	public void selectTab( final Tab tab ) {
		tabbedPane.setSelectedIndex( tabbedPane.indexOfComponent( tab.getContent() ) );
	}
	
	/**
	 * Checks if starcraft folder is set correctly.
	 */
	private void checkStarcraftFolder() {
		if ( new File( starcraftFolderTextField.getText(), Consts.STARCRAFT_EXECUTABLE_FILE_NAME ).exists() ) {
			startScButton.setEnabled( true );
			starcraftFolderStatusLabel.setText( "Starcraft directory is set correctly." );
			starcraftFolderStatusLabel.setForeground( Color.GREEN.darker() );
		}
		else {
			startScButton.setEnabled( false );
			starcraftFolderStatusLabel.setText( "Cannot find 'StarCraft.exe'!" );
			starcraftFolderStatusLabel.setForeground( Color.RED );
		}
	}
}
