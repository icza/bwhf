package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;

import swingwt.awt.BorderLayout;
import swingwt.awt.GridBagConstraints;
import swingwt.awt.GridBagLayout;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;
import swingwtx.swing.JTextField;
import swingwtx.swing.filechooser.FileFilter;

/**
 * Player checker tab.<br>
 * This tab provides the functionality to check players in the game lobby if they have been reported before.
 * 
 * @author Andras Belicza
 */
public class PlayerCheckerTab extends LoggedTab {
	
	/** Checkbox to enable/disable the autoscan.                  */
	private final JCheckBox  playerCheckerEnabledCheckBox       = new JCheckBox( "Enable checking players in the game lobby when pressing the 'Print Screen' key", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_PLAYER_CHECKER_ENABLED ) ) );
	/** Combo box to set the hacker list update interval.         */
	private final JComboBox  hackerListUpdateIntervalComboBox   = new JComboBox( new Object[] { 1, 2, 6, 12, 24, 48 } );
	/** Label to display the last update time of the hacker list. */
	private final JLabel     hackerListLastUpdatedLabel         = new JLabel();
	/** Button to update hacker list now.                         */
	private final JButton    updateNowButton                    = new JButton( "Update now" );
	/** Checkbox to enable/disable the autoscan.                  */
	private final JCheckBox  deleteGameLobbyScreenshotsCheckBox = new JCheckBox( "Delete game lobby screenshots after checking players", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS ) ) );
	/** Checkbox to include extra player list.                    */
	private final JCheckBox  includeExtraPlayerListCheckBox     = new JCheckBox( "Include this extra player list:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS ) ) );
	/** File of hte extra player list.                            */
	private final JTextField extraPlayerListFileTextField       = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_EXTRA_PLAYER_LIST_FILE ) );
	
	/**
	 * Creates a new PlayerCheckerTab.
	 */
	public PlayerCheckerTab() {
		super( "Player checker", "player_checker.log" );
		
		buildGUI();
		
		hackerListUpdateIntervalComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_HACKER_LIST_UPDATE_INTERVAL ) ) );
	}
	
	@Override
	protected void buildGUI() {
		final GridBagLayout      gridBagLayout = new GridBagLayout();
		final GridBagConstraints constraints   = new GridBagConstraints();
		final JPanel             settingsPanel = new JPanel( gridBagLayout );
		settingsPanel.setBorder( BorderFactory.createTitledBorder( "Settings:" ) );
		
		JPanel  wrapperPanel;
		JLabel  label;
		JButton button;
		
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		wrapperPanel = Utils.wrapInPanel( playerCheckerEnabledCheckBox );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		settingsPanel.add( wrapperPanel );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Automatically update hacker list in every " );
		gridBagLayout.setConstraints( label, constraints );
		settingsPanel.add( label );
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( hackerListUpdateIntervalComboBox, constraints );
		settingsPanel.add( hackerListUpdateIntervalComboBox );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		label = new JLabel( " hours." );
		gridBagLayout.setConstraints( label, constraints );
		settingsPanel.add( label );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Hacker list last updated at:" );
		gridBagLayout.setConstraints( label, constraints );
		settingsPanel.add( label );
		hackerListLastUpdatedLabel.setText( "2009-04-14 08:12:34" );
		gridBagLayout.setConstraints( hackerListLastUpdatedLabel, constraints );
		settingsPanel.add( hackerListLastUpdatedLabel );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		// In SwingWT Button text cannot be changed if a mnemonic has been assigned
		//updateNowButton.setMnemonic( ( updateNowButton.getText().charAt( 0 ) ) );
		gridBagLayout.setConstraints( updateNowButton, constraints );
		settingsPanel.add( updateNowButton );
		
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( includeExtraPlayerListCheckBox, constraints );
		settingsPanel.add( includeExtraPlayerListCheckBox );
		gridBagLayout.setConstraints( extraPlayerListFileTextField, constraints );
		settingsPanel.add( extraPlayerListFileTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		wrapperPanel = new JPanel( new BorderLayout() );
		button = Utils.createFileChooserButton( getContent(), extraPlayerListFileTextField, JFileChooser.FILES_ONLY, new FileFilter() {
			@Override
			public boolean accept( final File file ) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith( ".txt" );
			}
			@Override
			public String getDescription() {
				return "Text files (*.txt)";
			}
		}, new String[][] { new String[] { "*.txt", "*.*" }, new String[] { "Text files (*.txt)", "All files (*.*)" } }, new Runnable() {
			public void run() {
				final File selectedFile = new File( extraPlayerListFileTextField.getText() );
				if ( selectedFile.getAbsolutePath().equals( new File( Consts.SOUNDS_DIRECTORY_NAME + "/" + selectedFile.getName() ).getAbsolutePath() ) )
					extraPlayerListFileTextField.setText( Consts.SOUNDS_DIRECTORY_NAME + "/" + selectedFile.getName() );
			}
		} );
		wrapperPanel.add( button, BorderLayout.WEST );
		button = new JButton( "Reload" );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
			}
		} );
		wrapperPanel.add( button, BorderLayout.CENTER );
		button = new JButton( "Edit" );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.editFile( extraPlayerListFileTextField.getText() );
			}
		} );
		wrapperPanel.add( button, BorderLayout.EAST );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		settingsPanel.add( wrapperPanel );
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( deleteGameLobbyScreenshotsCheckBox, constraints );
		settingsPanel.add( deleteGameLobbyScreenshotsCheckBox );
		
		contentBox.add( Utils.wrapInPanel( settingsPanel ) );
		
		super.buildGUI();
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_PLAYER_CHECKER_ENABLED       , Boolean.toString( playerCheckerEnabledCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HACKER_LIST_UPDATE_INTERVAL  , Integer.toString( hackerListUpdateIntervalComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_INCLUDE_EXTRA_PLAYER_LIST    , Boolean.toString( includeExtraPlayerListCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_EXTRA_PLAYER_LIST_FILE       , extraPlayerListFileTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_DELETE_GAME_LOBBY_SCREENSHOTS, Boolean.toString( deleteGameLobbyScreenshotsCheckBox.isSelected() ) );
	}
	
}
