package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhf.model.HackDescription;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;
import swingwtx.swing.filechooser.FileFilter;


/**
 * Manual scan tab.
 * 
 * @author Andras Belicza
 */
public class ManualScanTab extends LoggedTab {
	
	/** Log file name for autoscan.     */
	private static final String LOG_FILE_NAME         = "manual_scan.log";
	/** Replay file extension.          */
	private static final String REPLAY_FILE_EXTENSION = ".rep";
	/** Appendable hacker reps postfix. */
	private static final String HACKER_REPS_FLAG      = "hack";
	
	/** Replay file filter. */
	public static final FileFilter SWING_REPLAY_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( final File file ) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith( REPLAY_FILE_EXTENSION );
		}
		@Override
		public String getDescription() {
			return "Replay files (*.rep)";
		}
	};
	
	/** Button to scan the last replay.                  */
	private final JButton   scanLastReplayButton           = new JButton( "Scan 'LastReplay.rep'" );
	/** Button to select folders to scan.                */
	private final JButton   selectFoldersButton            = new JButton( "Select folders to scan recursively" );
	/** Button to select files to scan.                  */
	private final JButton   selectFilesButton              = new JButton( "Select files to scan" );
	/** Button to stop the current scan.                 */
	private final JButton   stopScanButton                 = new JButton( "Stop current scan" );
	/** Flag hacker reps checkbox.                       */
	private final JCheckBox flagHackerRepsCheckBox         = new JCheckBox( "Flag hacker replays by appending '" + HACKER_REPS_FLAG + "' to the", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_FLAG_HACKER_REPS ) ) );
	/** Position where to flag hacker replays combo box. */
	private final JComboBox flagHackerRepsPositionComboBox = new JComboBox( Consts.FLAG_HACKER_REPS_POSITION_LABELS );
	/** Clean hack flag checkbox.                        */
	private final JCheckBox cleanHackFlagCheckBox          = new JCheckBox( "Clean the '" + HACKER_REPS_FLAG + "' flag from replays where no hackers were found during the scan", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_CLEAN_HACK_FLAG ) ) );
	
	/** Variable to store stop requests of scan.    */
	private volatile boolean requestedToStop;
	
	/**
	 * Creates a new ManualScanTab.
	 */
	public ManualScanTab() {
		super( "Manual scan", LOG_FILE_NAME );
		
		buildGUI();
		
		flagHackerRepsPositionComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_FLAG_HACKER_REPS_POSITION ) ) );
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	protected void buildGUI() {
		scanLastReplayButton.setMnemonic( 'L' );
		scanLastReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				scanFilesAndFolders( new File[] { new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME ) }, true );
			}
		} );
		contentBox.add( Utils.wrapInPanel( scanLastReplayButton ) );
		
		final ActionListener selectFilesAndFoldersActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.STARCRAFT_REPLAY_FOLDER ) );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( event.getSource() == selectFoldersButton ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES );
				fileChooser.setMultiSelectionEnabled( true );
				// SwingWT does not support selecting multiple directories yet, getSelectedFiles() returns null so I have to call getSelectedFile() in case of folders.
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					scanFilesAndFolders( event.getSource() == selectFoldersButton ? new File[] { fileChooser.getSelectedFile() } : fileChooser.getSelectedFiles(), false );
			}
		};
		
		final JPanel selectButtonsPanel = new JPanel();
		selectFoldersButton.setMnemonic( 'd' );
		selectFoldersButton.addActionListener( selectFilesAndFoldersActionListener );
		selectButtonsPanel.add( selectFoldersButton );
		selectFilesButton.setMnemonic( 'f' );
		selectFilesButton.addActionListener( selectFilesAndFoldersActionListener );
		selectButtonsPanel.add( selectFilesButton );
		contentBox.add( selectButtonsPanel );
		
		stopScanButton.setEnabled( false );
		stopScanButton.setMnemonic( 't' );
		stopScanButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				requestedToStop = true; // Access to a volatile variable is automatically synchronized from Java 5.0
				stopScanButton.setEnabled( false );
			}
		} );
		contentBox.add( Utils.wrapInPanel( stopScanButton ) );
		
		final JPanel flagHackerRepsPanel = new JPanel();
		flagHackerRepsPanel.add( flagHackerRepsCheckBox );
		flagHackerRepsPanel.add( flagHackerRepsPositionComboBox );
		flagHackerRepsPanel.add( new JLabel( "of their names" ) );
		contentBox.add( flagHackerRepsPanel );
		
		contentBox.add( Utils.wrapInPanel( cleanHackFlagCheckBox ) );
		
		super.buildGUI();
	}
	
	/**
	 * Helper class to wrap a modifiable integer into an object.
	 * @author Andras Belicza
	 */
	private static class IntWrapper {
		public int value;
	}
	
	/**
	 * Scans the specified files and folders.
	 * @param files        files and folders to be scanned
	 * @param isLastReplay tells whether the lastreplay scan button was activated
	 */
	private void scanFilesAndFolders( final File[] files, final boolean isLastReplay ) {
		requestedToStop = false;
		scanLastReplayButton.setEnabled( false );
		selectFoldersButton .setEnabled( false );
		selectFilesButton   .setEnabled( false );
		stopScanButton      .setEnabled( true  );
		
		new NormalThread() {
			/** List of replay files to be scanned. */
			final List< File > replayFileList = new ArrayList< File >();
			
			@Override
			public void run() {
				try {
					final boolean skipLatterActionsOfHackers = MainFrame.getInstance().generalSettingsTab.skipLatterActionsOfHackersCheckBox.isSelected();
					
					logMessage( "\n", false ); // Prints 2 empty lines
					if ( !isLastReplay )
						logMessage( "Counting replays..." );
					
					chooseReplayFiles( files );
					
					if ( requestedToStop )
						return;
					
					final String scanningMessage = "Scanning " 
						+ ( isLastReplay ? new File( Consts.LAST_REPLAY_FILE_NAME ).getName() : replayFileList.size() + " replay" + ( replayFileList.size() == 1 ? "" : "s" ) );
					logMessage( scanningMessage + "..." );
					
					final long startTimeNanons = System.nanoTime();
					
					int hackerRepsCount  = 0;
					int skippedRepsCount = 0;
					
					final Map< String, IntWrapper > playerHackerRepsCountMap = new HashMap< String, IntWrapper >(); // We count the replays for every hacker in which he was caught
					
					for ( final File replayFile : replayFileList ) {
						if ( requestedToStop )
							return;
						
						List< HackDescription > hackDescriptionList = null; 
						final Replay replay = BinRepParser.parseReplay( replayFile, false );
						if ( replay != null )
							hackDescriptionList = ReplayScanner.scanReplayForHacks( replay.replayActions, skipLatterActionsOfHackers );
						
						if ( hackDescriptionList == null ) {
							skippedRepsCount++;
							logMessage( "Could not scan " + replayFile.getAbsolutePath() + "!" );
						}
						else
							if ( !hackDescriptionList.isEmpty() ) {
								hackerRepsCount++;
								logMessage( "Found " + hackDescriptionList.size() + " hack" + (hackDescriptionList.size() == 1 ? "" : "s" ) + " in " + replayFile.getAbsolutePath() + ":" );
								final Set< String > hackersOfReplaySet = new HashSet< String >();
								for ( final HackDescription hackDescription : hackDescriptionList ) {
									logMessage( "\t" + hackDescription.description, false );
									
									final String lowerCasedHackerName = hackDescription.playerName.toLowerCase();
									if ( !hackersOfReplaySet.contains( lowerCasedHackerName ) ) {
										// Only count a player once per replay in the overall statistics
										hackersOfReplaySet.add( lowerCasedHackerName );
										IntWrapper playerHackerRepsCount = playerHackerRepsCountMap.get( lowerCasedHackerName );
										if ( playerHackerRepsCount == null )
											playerHackerRepsCountMap.put( lowerCasedHackerName, playerHackerRepsCount = new IntWrapper() );
										playerHackerRepsCount.value++;
									}
								}
								if ( flagHackerRepsCheckBox.isSelected() && !isLastReplay ) {
									final String replayName  = replayFile.getName().substring( 0, replayFile.getName().length() - REPLAY_FILE_EXTENSION.length() );
									String flaggedReplayName = null;
									switch ( flagHackerRepsPositionComboBox.getSelectedIndex() ) {
										case Consts.FLAG_HACKER_REPS_POSITION_BEGINNING :
											if ( !replayName.startsWith( HACKER_REPS_FLAG + " - " ) )
												flaggedReplayName = HACKER_REPS_FLAG + " - " + replayName;
											break;
										case Consts.FLAG_HACKER_REPS_POSITION_END :
											if ( !replayName.endsWith( " - " + HACKER_REPS_FLAG ) )
												flaggedReplayName = replayName + " - " + HACKER_REPS_FLAG;
											break;
									}
									if ( flaggedReplayName != null )
										replayFile.renameTo( new File( replayFile.getParent(), flaggedReplayName + REPLAY_FILE_EXTENSION ) );
								}
							}
							else {
								logMessage( "Found no hacks in " + replayFile.getAbsolutePath() + "." );
								
								if ( cleanHackFlagCheckBox.isSelected() && !isLastReplay ) {
									final String replayName  = replayFile.getName().substring( 0, replayFile.getName().length() - REPLAY_FILE_EXTENSION.length() );
									String cleanedReplayName = replayName;
									if ( cleanedReplayName.startsWith( HACKER_REPS_FLAG + " - " ) )
										cleanedReplayName = cleanedReplayName.substring( ( HACKER_REPS_FLAG + " - " ).length() );
									if ( cleanedReplayName.endsWith( " - " + HACKER_REPS_FLAG ) )
										cleanedReplayName = cleanedReplayName.substring( 0, cleanedReplayName.length() - ( " - " + HACKER_REPS_FLAG ).length() + 1 );
									if ( replayName.length() != cleanedReplayName.length() )
										replayFile.renameTo( new File( replayFile.getParent(), cleanedReplayName + REPLAY_FILE_EXTENSION ) );
								}
							}
					}
					
					final long endTimeNanons = System.nanoTime();
					logMessage( scanningMessage + " done in " + Utils.formatNanoTimeAmount( endTimeNanons - startTimeNanons ) );
					logMessage( "\tFound " + hackerRepsCount + " hacker replay" + ( hackerRepsCount == 1 ? "" : "s" ) + ".", false );
					logMessage( "\tSkipped " + skippedRepsCount + " replay" + ( skippedRepsCount == 1 ? "" : "s" ) + ".", false );
					if ( !playerHackerRepsCountMap.isEmpty() ) {
						final StringBuilder hackersBuilder = new StringBuilder( "\tThe following player" + ( playerHackerRepsCountMap.size() == 1 ? " was" : "s were" ) + " found hacking: " );
						boolean firstHacker = true;
						// First we make a TreeMap so we will list hackers sorted by their name
						for ( final Entry< String, IntWrapper > playerHackerRepsCount : new TreeMap< String, IntWrapper >( playerHackerRepsCountMap ).entrySet() ) {
							if ( firstHacker )
								firstHacker = false;
							else
								hackersBuilder.append( ", " );
							hackersBuilder.append( playerHackerRepsCount.getKey() );
							if ( playerHackerRepsCount.getValue().value > 1 )
								hackersBuilder.append( " (x" ).append( playerHackerRepsCount.getValue().value ).append( ')' );
						}
						logMessage( hackersBuilder.toString(), false );
					}
				}
				finally {
					if ( requestedToStop )
						logMessage( "Scan was manually aborted!" );
					stopScanButton      .setEnabled( false );
					selectFilesButton   .setEnabled( true  );
					selectFoldersButton .setEnabled( true  );
					scanLastReplayButton.setEnabled( true  );
				}
			}
			
			private final java.io.FileFilter IO_REPLAY_FILE_FILTER = new java.io.FileFilter() {
				public boolean accept( final File pathname ) {
					return SWING_REPLAY_FILE_FILTER.accept( pathname );
				}
			};
			
			/**
			 * Chooses the replay files in the specified files and folders.
			 * @param files files and folders to be chosen from
			 */
			private void chooseReplayFiles( final File[] files ) {
				if ( files == null )
					return;
				for ( final File file : files ) {
					if ( requestedToStop )
						return;
					if ( file.isDirectory() )
						chooseReplayFiles( file.listFiles( IO_REPLAY_FILE_FILTER ) );
					else
						if ( IO_REPLAY_FILE_FILTER.accept( file ) )
							replayFileList.add( file );
				}
			}
			
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_FLAG_HACKER_REPS         , Boolean.toString( flagHackerRepsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_FLAG_HACKER_REPS_POSITION, Integer.toString( flagHackerRepsPositionComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CLEAN_HACK_FLAG          , Boolean.toString( cleanHackFlagCheckBox.isSelected() ) );
	}
	
}
