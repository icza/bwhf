package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.HackDescription;
import hu.belicza.andras.bwhfagent.Consts;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 * Autoscan tab.
 * 
 * @author Andras Belicza
 */
public class AutoscanTab extends LoggedTab {
	
	/** Log file name for autoscan.                             */
	private static final String LOG_FILE_NAME                         = "autoscan.log";
	/** Time between checking for new replay in ms.             */
	private static final long   TIME_BETWEEN_CHECKS_FOR_NEW_REPLAY_MS = 2000l;
	/** Date format to create timestamps for copied file names. */
	private static final DateFormat DATE_FORMAT                       = new SimpleDateFormat( "yyyy-MM-dd HH-mm-ss" );
	
	/** Checkbox to enable/disable the autoscan.                                */
	private final JCheckBox  autoscanEnabledCheckBox        = new JCheckBox( "Autoscan enabled", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTOSCAN_ENABLED ) ) );
	/** Checkbox to enable/disable autosaving hacker reps.                      */
	private final JCheckBox  saveHackerRepsCheckBox         = new JCheckBox( "Save hacker replays to folder:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SAVE_HACKER_REPS ) ) );
	/** Save hacker replays to this folder.                                     */
	private final JTextField hackerRepsDestinationTextField = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_HACKER_REPS_DESTINATION ), 30 );
	/** Checkbox to enable/disable autosaving all reps.                         */
	private final JCheckBox  saveAllRepsCheckBox            = new JCheckBox( "Save all replays to folder:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_SAVE_ALL_REPS ) ) );
	/** Save hacker replays to this folder.                                     */
	private final JTextField allRepsDestinationTextField    = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_ALL_REPS_DESTINATION ), 30 );
	/** Checkbox to enable/disable playing sound if found hacks.                */
	private final JCheckBox  playSoundCheckBox              = new JCheckBox( "Play wav file if found hacks:", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_PLAY_SOUND ) ) );
	/** Wav file to play when found hacks.                                      */
	private final JTextField foundHacksWavFileTextField     = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_FOUND_HACKS_WAV_FILE ), 30 );
	/** Checkbox to enable/disable bringing main frame to front if found hacks. */
	private final JCheckBox  bringToFrontCheckBox           = new JCheckBox( "Bring agent to front if found hacks", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_BRING_TO_FRONT ) ) );
	
	/**
	 * Creates a new AutoscanTab.
	 */
	public AutoscanTab() {
		super( "Autoscan", LOG_FILE_NAME );
		
		buildGUI();
		startAutoscanner();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	protected void buildGUI() {
		final GridBagLayout      gridBagLayout = new GridBagLayout();
		final GridBagConstraints constraints   = new GridBagConstraints();
		final JPanel             settingsPanel = new JPanel( gridBagLayout );
		
		JButton button;
		
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		JPanel wrapperPanel = Utils.wrapInPanel( autoscanEnabledCheckBox );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		settingsPanel.add( wrapperPanel );
		
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( saveHackerRepsCheckBox, constraints );
		settingsPanel.add( saveHackerRepsCheckBox );
		gridBagLayout.setConstraints( hackerRepsDestinationTextField, constraints );
		settingsPanel.add( hackerRepsDestinationTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = createFileChooserButton( hackerRepsDestinationTextField, JFileChooser.DIRECTORIES_ONLY, null, null );
		gridBagLayout.setConstraints( button, constraints );
		settingsPanel.add( button );
		
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( saveAllRepsCheckBox, constraints );
		settingsPanel.add( saveAllRepsCheckBox );
		gridBagLayout.setConstraints( allRepsDestinationTextField, constraints );
		settingsPanel.add( allRepsDestinationTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		button = createFileChooserButton( allRepsDestinationTextField, JFileChooser.DIRECTORIES_ONLY, null, null );
		gridBagLayout.setConstraints( button, constraints );
		settingsPanel.add( button );
		
		constraints.gridwidth = 1;
		gridBagLayout.setConstraints( playSoundCheckBox, constraints );
		settingsPanel.add( playSoundCheckBox );
		gridBagLayout.setConstraints( foundHacksWavFileTextField, constraints );
		settingsPanel.add( foundHacksWavFileTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		final JPanel panel = new JPanel( new BorderLayout() );
		button = createFileChooserButton( foundHacksWavFileTextField, JFileChooser.FILES_ONLY, new FileFilter() {
			@Override
			public boolean accept( final File file ) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith( ".wav" );
			}
			@Override
			public String getDescription() {
				return "Wave audio files (*.wav)";
			} 
		}, new Runnable() {
			public void run() {
				// If one of our sound file was selected, we replace its path to be relative so it will work if the product is copied to another directory
				final File selectedFile = new File( foundHacksWavFileTextField.getText() );
				if ( selectedFile.getAbsolutePath().equals( new File( Consts.SOUNDS_DIRECTORY_NAME + "/" + selectedFile.getName() ).getAbsolutePath() ) )
					foundHacksWavFileTextField.setText( Consts.SOUNDS_DIRECTORY_NAME + "/" + selectedFile.getName() );
			}
		} );
		panel.add( button, BorderLayout.CENTER );
		final JButton testButton = new JButton( "Play" );
		testButton.setToolTipText( "Play the selected sound file" );
		testButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.playWavFile( foundHacksWavFileTextField.getText() );
			}
		} );
		panel.add( testButton, BorderLayout.EAST );
		gridBagLayout.setConstraints( panel, constraints );
		settingsPanel.add( panel );
		
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( bringToFrontCheckBox, constraints );
		settingsPanel.add( bringToFrontCheckBox );
		
		wrapperPanel = Utils.wrapInPanel( settingsPanel );
		wrapperPanel.setBorder( BorderFactory.createTitledBorder( "Settings" ) );
		contentBox.add( wrapperPanel );
		
		super.buildGUI();
	}
	
	/**
	 * Calls {@link Utils#createFileChooserButton(java.awt.Component, JTextField, int, FileFilter, Runnable)} with the scroll pane of the tab.
	 */
	private JButton createFileChooserButton( final JTextField targetTextField, final int fileSelectionMode, final FileFilter choosableFileFilter, final Runnable taskOnApprove ) {
		return Utils.createFileChooserButton( getScrollPane(), targetTextField, fileSelectionMode, choosableFileFilter, taskOnApprove );
	}
	
	/**
	 * Starts the autoscanner.
	 */
	private void startAutoscanner() {
		new Thread() {
			
			@Override
			public void run() {
				final JTextField starcraftFolderTextField = MainFrame.getInstance().starcraftFolderTextField;
				
				Date autoscanEnabledTime       = null;
				long lastModifiedOfLastChecked = 0l; // Last modified time of the LastReplay.rep that was checked lastly.
				while ( true ) {
					try {
						if ( autoscanEnabledCheckBox.isSelected() ) {
							if ( autoscanEnabledTime == null ) {
								autoscanEnabledTime       = new Date();
								lastModifiedOfLastChecked = 0l;
							}
							
							final File lastReplayFile            = new File( starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME );
							final long newLastReplayLastModified = lastReplayFile.lastModified();
							
							if ( newLastReplayLastModified >= autoscanEnabledTime.getTime() && newLastReplayLastModified != lastModifiedOfLastChecked ) {
								if ( saveAllRepsCheckBox.isSelected() )
									Utils.copyFile( lastReplayFile, new File( allRepsDestinationTextField.getText() ), DATE_FORMAT.format( new Date() ) + " LastReplay.rep" );
								
								logMessage( "LastReplay.rep was modified - proceeding to scan..." );
								lastModifiedOfLastChecked = newLastReplayLastModified;
								
								final List< HackDescription > hackDescriptionList = Utils.scanReplayFile( lastReplayFile );
								if ( hackDescriptionList == null )
									logMessage( "Could not scan LastReplay.rep!" );
								else
									if ( !hackDescriptionList.isEmpty() ) {
										if ( bringToFrontCheckBox.isSelected() )
											MainFrame.getInstance().toFront();
										if ( playSoundCheckBox.isSelected() )
											Utils.playWavFile( foundHacksWavFileTextField.getText() );
										
										logMessage( "Found " + hackDescriptionList.size() + " hack" + (hackDescriptionList.size() == 1 ? "" : "s" ) + " in LastReplay.rep:" );
										for ( final HackDescription hackDescription : hackDescriptionList )
											logMessage( "\t" + hackDescription.description, false );
										
										if ( saveHackerRepsCheckBox.isSelected() )
											Utils.copyFile( lastReplayFile, new File( hackerRepsDestinationTextField.getText() ), DATE_FORMAT.format( new Date() ) + " LastReplay.rep" );
									}
									else
										logMessage( "Found no hacks in LastReplay.rep." );
							}
						}
						else
							autoscanEnabledTime = null;
						
						sleep( TIME_BETWEEN_CHECKS_FOR_NEW_REPLAY_MS );
					} catch ( final InterruptedException ie ) {
					}
				}
			}
			
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_AUTOSCAN_ENABLED       , Boolean.toString( autoscanEnabledCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SAVE_HACKER_REPS       , Boolean.toString( saveHackerRepsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HACKER_REPS_DESTINATION, hackerRepsDestinationTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_SAVE_ALL_REPS          , Boolean.toString( saveAllRepsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_ALL_REPS_DESTINATION   , allRepsDestinationTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_PLAY_SOUND             , Boolean.toString( playSoundCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_FOUND_HACKS_WAV_FILE   , foundHacksWavFileTextField.getText() );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_BRING_TO_FRONT         , Boolean.toString( bringToFrontCheckBox.isSelected() ) );
	}
	
}
