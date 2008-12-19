package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.io.IOException;

import swingwt.awt.BorderLayout;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
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
	
	/** Starcraft directory. */
	public final JTextField starcraftFolderTextField = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_STARCRAFT_FOLDER ) );
	
	/** Reference to the general settings tab. */
	public final GeneralSettingsTab generalSettingsTab;
	
	/** Tabs in the main frame. */
	private final Tab[] tabs;
	
	/**
	 * Creates a new MainFrame.
	 */
	public MainFrame( final String applicationVersion ) {
		if ( MainFrame.mainFrame != null )
			throw new RuntimeException( "Only one main frame is allowed per Java Virtual Machine!" );
		
		MainFrame.mainFrame = this;
		
		this.applicationVersion = applicationVersion;
		
		setTitle( Consts.APPLICATION_NAME );
		setIconImage( new ImageIcon( getClass().getResource( ICON_IMAGE_RESOURCE_NAME ) ).getImage() );
		
		generalSettingsTab = new GeneralSettingsTab();
		tabs = new Tab[] { new AutoscanTab(), new ManualScanTab(), generalSettingsTab, new AboutTab() };
		buildGUI();
		
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
		
		setBounds( 100, 100, 730, 600 );
		setVisible( true );
	}
	
	/**
	 * Builds the graphical user interface.
	 */
	private void buildGUI() {
		final JPanel northPanel = new JPanel( new BorderLayout() );
		final JButton startScButton = new JButton( "Start/Switch to Starcraft" );
		startScButton.setMnemonic( startScButton.getText().charAt( 0 ) );
		startScButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					Runtime.getRuntime().exec( new File( starcraftFolderTextField.getText(), Consts.STARCRAFT_EXECUTABLE_FILE_NAME ).getCanonicalPath() );
				} catch ( final IOException ie ) {
					JOptionPane.showMessageDialog( MainFrame.this, "Cannot start " + Consts.STARCRAFT_EXECUTABLE_FILE_NAME + "!\nIs Starcraft directory properly set?", "Error", JOptionPane.ERROR_MESSAGE );
				}
			}
		} );
		northPanel.add( Utils.wrapInPanel( startScButton ), BorderLayout.NORTH );
		northPanel.add( new JLabel( "Starcraft directory:" ), BorderLayout.WEST );
		northPanel.add( starcraftFolderTextField, BorderLayout.CENTER );
		northPanel.add( Utils.createFileChooserButton( this, starcraftFolderTextField, JFileChooser.DIRECTORIES_ONLY, null, null ), BorderLayout.EAST );
		getContentPane().add( Utils.wrapInPanel( northPanel ), BorderLayout.NORTH );
		
		final JTabbedPane tabbedPane = new JTabbedPane();
		
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
	 * Returns the starcraft folder text field.
	 * @return the starcraft folder text field
	 */
	public JTextField getStarcraftFolderTextField() {
		return starcraftFolderTextField;
	}
	
}
