package hu.belicza.andras.bwhfagent.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * Autoscan tab.
 * 
 * @author Andras Belicza
 */
public class AutoscanTab extends LoggedTab {
	
	/** Checkbox to enable/disable the autoscan.                 */
	private final JCheckBox  enabledCheckBox                = new JCheckBox( "Autoscan enabled", true );
	/** Starcraft directory.                                     */
	private final JTextField starcraftFolderTextField       = new JTextField( "C:/Program Files/Starcraft", 30 );
	/** Checkbox to enable/disable autosaving hacker reps.       */
	private final JCheckBox  saveHackerRepsCheckBox         = new JCheckBox( "Save hacker replays to folder:", true );
	/** Save hacker replays to this folder.                      */
	private final JTextField hackerRepsDestinationTextField = new JTextField( "C:/Program Files/Starcraft/maps/replays/hacker", 30 );
	/** Checkbox to enable/disable autosaving all reps.          */
	private final JCheckBox  saveAllRepsCheckBox            = new JCheckBox( "Save all replays to folder:", true );
	/** Save hacker replays to this folder.                      */
	private final JTextField allRepsDestinationTextField    = new JTextField( "C:/Program Files/Starcraft/maps/replays/allreps", 30 );
	/** Spinner to set the time interval between checks.         */
	private final JSpinner   checkIntervalSpinner           = new JSpinner( new SpinnerNumberModel( 3, 1, 30, 1 ) );
	/** Checkbox to enable/disable playing sound if found hacks. */
	private final JCheckBox  playSoundCheckBox              = new JCheckBox( "Play wav file if found hacks:", true );
	/** Wav file to play when found hacks.                       */
	private final JTextField foundHacksWavFileTextField     = new JTextField( "foundHacks.wav", 15 );
	
	/**
	 * Creates a new AutoscanTab.
	 */
	public AutoscanTab() {
		super( "Autoscan" );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	protected void buildGUI() {
		final GridBagLayout      gridBagLayout = new GridBagLayout();
		final GridBagConstraints constraints   = new GridBagConstraints();
		final JPanel             settingsPanel = new JPanel( gridBagLayout );
		
		JLabel  label;
		JButton button;
		
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		JPanel wrapperPanel = Utils.wrapInPanel( enabledCheckBox );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		settingsPanel.add( wrapperPanel );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Starcraft directory:" );
		gridBagLayout.setConstraints( label, constraints );
		settingsPanel.add( label );
		gridBagLayout.setConstraints( starcraftFolderTextField, constraints );
		settingsPanel.add( starcraftFolderTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = createFileChooserButton( starcraftFolderTextField, JFileChooser.DIRECTORIES_ONLY );
		gridBagLayout.setConstraints( button, constraints );
		settingsPanel.add( button );
		
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( saveHackerRepsCheckBox, constraints );
		settingsPanel.add( saveHackerRepsCheckBox );
		gridBagLayout.setConstraints( hackerRepsDestinationTextField, constraints );
		settingsPanel.add( hackerRepsDestinationTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = createFileChooserButton( hackerRepsDestinationTextField, JFileChooser.DIRECTORIES_ONLY );
		gridBagLayout.setConstraints( button, constraints );
		settingsPanel.add( button );
		
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( saveAllRepsCheckBox, constraints );
		settingsPanel.add( saveAllRepsCheckBox );
		gridBagLayout.setConstraints( allRepsDestinationTextField, constraints );
		settingsPanel.add( allRepsDestinationTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = createFileChooserButton( allRepsDestinationTextField, JFileChooser.DIRECTORIES_ONLY );
		gridBagLayout.setConstraints( button, constraints );
		settingsPanel.add( button );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Time between checks for new replay:" );
		gridBagLayout.setConstraints( label, constraints );
		settingsPanel.add( label );
		gridBagLayout.setConstraints( checkIntervalSpinner, constraints );
		settingsPanel.add( checkIntervalSpinner );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		label = new JLabel( " seconds" );
		gridBagLayout.setConstraints( label, constraints );
		settingsPanel.add( label );
		
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( playSoundCheckBox, constraints );
		settingsPanel.add( playSoundCheckBox );
		gridBagLayout.setConstraints( foundHacksWavFileTextField, constraints );
		settingsPanel.add( foundHacksWavFileTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = createFileChooserButton( foundHacksWavFileTextField, JFileChooser.FILES_ONLY );
		gridBagLayout.setConstraints( button, constraints );
		settingsPanel.add( button );
		
		wrapperPanel = Utils.wrapInPanel( settingsPanel );
		wrapperPanel.setBorder( BorderFactory.createTitledBorder( "Settings" ) );
		contentBox.add( wrapperPanel );
		
		super.buildGUI();
	}
	
	/**
	 * Creates and returns a button with a registered action listener which opens a file chooser
	 * with the specified file seletcion mode, and on approved returned option stores the selected file
	 * into the target text field. 
	 * @param targetTextField   text field to be updated if file/folder is selected
	 * @param fileSelectionMode the type of files to be displayed
	 * 							<ul>
	 * 								<li>JFileChooser.FILES_ONLY
	 * 								<li>JFileChooser.DIRECTORIES_ONLY
	 * 								<li>JFileChooser.FILES_AND_DIRECTORIES
	 * 							</ul>
	 * @return a button handling the file chooser
	 */
	private JButton createFileChooserButton( final JTextField targetTextField, final int fileSelectionMode ) {
		final JButton chooseButton = new JButton( "Choose..." );
		
		chooseButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode( fileSelectionMode );
				if ( fileChooser.showOpenDialog( getScrollPane() ) == JFileChooser.APPROVE_OPTION )
					targetTextField.setText( fileChooser.getSelectedFile().getAbsolutePath() );
			}
		} );
		
		return chooseButton;
	}
	
}
