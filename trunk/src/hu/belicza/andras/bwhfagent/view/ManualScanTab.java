package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhf.model.HackDescription;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
public class ManualScanTab extends ProgressLoggedTab {
	
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
	private final JButton   scanLastReplayButton            = new JButton( "Scan 'LastReplay.rep'" );
	/** Button to select folders to scan.                */
	private final JButton   selectFoldersButton             = new JButton( "Select folders to scan recursively" );
	/** Button to select files to scan.                  */
	private final JButton   selectFilesButton               = new JButton( "Select files to scan" );
	/** Button to stop the current scan.                 */
	private final JButton   stopScanButton                  = new JButton( "Stop current scan" );
	/** Flag hacker reps checkbox.                       */
	private final JCheckBox flagHackerRepsCheckBox          = new JCheckBox( "Flag hacker replays by appending '" + HACKER_REPS_FLAG + "' to the", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_FLAG_HACKER_REPS ) ) );
	/** Position where to flag hacker replays combo box. */
	private final JComboBox flagHackerRepsPositionComboBox  = new JComboBox( Consts.FLAG_HACKER_REPS_POSITION_LABELS );
	/** Clean hack flag checkbox.                        */
	private final JCheckBox cleanHackFlagCheckBox           = new JCheckBox( "Clean the '" + HACKER_REPS_FLAG + "' flag from replays where no hackers were found during the scan", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_CLEAN_HACK_FLAG ) ) );
	/** Create HTML report summary checkbox.             */
	private final JCheckBox createHtmlSummaryReportCheckBox = new JCheckBox( "Create and open a detailed HTML summary report at the end of scan", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_CREATE_HTML_SUMMARY_REPORT ) ) );
	
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
		
		contentBox.add( Utils.wrapInPanel( createHtmlSummaryReportCheckBox ) );
		
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
					progressBar.setValue( 0 );
					
					final boolean skipLatterActionsOfHackers = MainFrame.getInstance().generalSettingsTab.skipLatterActionsOfHackersCheckBox.isSelected();
					
					logMessage( "\n", false ); // Prints 2 empty lines
					if ( !isLastReplay )
						logMessage( "Counting replays..." );
					
					chooseReplayFiles( files );
					progressBar.setMaximum( replayFileList.size() );
					
					if ( requestedToStop )
						return;
					
					final String scanningMessage = "Scanning " 
						+ ( isLastReplay ? new File( Consts.LAST_REPLAY_FILE_NAME ).getName() : replayFileList.size() + " replay" + ( replayFileList.size() == 1 ? "" : "s" ) );
					logMessage( scanningMessage + "..." );
					
					final long startNanoTime = System.nanoTime();
					final Date startTime     = new Date();
					
					int hackerRepsCount  = 0;
					int skippedRepsCount = 0;
					
					// We count the replays for every hacker in which he was caught
					final Map< String, IntWrapper > playerHackerRepsCountMap = new HashMap< String, IntWrapper >();
					
					// For the HTML summary report
					final Map< String, Map< String, Set< Integer > > > playerNameHackerRepMapMap =
						createHtmlSummaryReportCheckBox.isSelected() && !isLastReplay ? new HashMap< String, Map< String, Set< Integer > > >() : null;
					
					int counter = 0;	
					for ( final File replayFile : replayFileList ) {
						if ( requestedToStop )
							return;
						
						final String replayFileAbsolutePath = replayFile.getAbsolutePath();
						
						List< HackDescription > hackDescriptionList = null; 
						final Replay replay = BinRepParser.parseReplay( replayFile, false );
						if ( replay != null )
							hackDescriptionList = ReplayScanner.scanReplayForHacks( replay.replayActions, skipLatterActionsOfHackers );
						
						if ( hackDescriptionList == null ) {
							skippedRepsCount++;
							logMessage( "Could not scan " + replayFileAbsolutePath + "!" );
						}
						else
							if ( !hackDescriptionList.isEmpty() ) {
								hackerRepsCount++;
								logMessage( "Found " + hackDescriptionList.size() + " hack" + (hackDescriptionList.size() == 1 ? "" : "s" ) + " in " + replayFileAbsolutePath + ":" );
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
									
									// Build data for the HTML summary report:
									if ( playerNameHackerRepMapMap != null ) {
										Map< String, Set< Integer > > replayNameUsedHackTypeSet = playerNameHackerRepMapMap.get( lowerCasedHackerName );
										if ( replayNameUsedHackTypeSet == null ) {
											replayNameUsedHackTypeSet = new HashMap< String, Set< Integer > >( 1 );
											playerNameHackerRepMapMap.put( lowerCasedHackerName, replayNameUsedHackTypeSet );
										}
										Set< Integer > usedHackTypeSet = replayNameUsedHackTypeSet.get( replayFileAbsolutePath );
										if ( usedHackTypeSet == null ) {
											usedHackTypeSet = new HashSet< Integer >( 1 );
											replayNameUsedHackTypeSet.put( replayFileAbsolutePath, usedHackTypeSet );
										}
										usedHackTypeSet.add( hackDescription.hackType );
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
								logMessage( "Found no hacks in " + replayFileAbsolutePath + "." );
								
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
						
						progressBar.setValue( ++counter );
					}
					
					final long endNanoTime = System.nanoTime();
					final Date endTime     = new Date();
					
					final String[] infoMessages = new String[ 3 ];
					logMessage( infoMessages[ 0 ] = ( scanningMessage + " done in " + Utils.formatNanoTimeAmount( endNanoTime - startNanoTime ) ) );
					logMessage( infoMessages[ 1 ] = ( "\tFound " + hackerRepsCount + " hacker replay" + ( hackerRepsCount == 1 ? "" : "s" ) + "." ), false );
					logMessage( infoMessages[ 2 ] = ( "\tSkipped " + skippedRepsCount + " replay" + ( skippedRepsCount == 1 ? "" : "s" ) + "." ), false );
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
					
					if ( playerNameHackerRepMapMap != null )
						saveAndOpenHtmlSummaryReport( playerNameHackerRepMapMap, startTime, endTime, infoMessages );
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
	
	/**
	 * Saves and opens the detailed HTML summary report of the scan.
	 * @param playerNameHackerRepMapMap map containing the replays of players where they were found hacking, and the types of hacks they used
	 * @param scanStartTime start time of the scan
	 * @param scanEndTime   end time of the scan
	 * @param infoMessages  contains information messages to be included in the report
	 */
	private void saveAndOpenHtmlSummaryReport( final Map< String, Map< String, Set< Integer > > > playerNameHackerRepMapMap, final Date scanStartTime, final Date scanEndTime, final String[] infoMessages ) {
		final File htmlReportDirectory = new File( Consts.HTML_REPORT_DIRECTORY_NAME );
		final File htmlReportFile      = new File( htmlReportDirectory, "Manual scan report " + Utils.DATE_FORMAT.format( new Date() ) + ".html" );
		
		PrintWriter output = null;
		try {
			if ( !htmlReportDirectory.exists() )
				htmlReportDirectory.mkdir();
			
			output = new PrintWriter( new OutputStreamWriter( new FileOutputStream( htmlReportFile ), "UTF-8" ) );
			
			output.println( "<html>" );
			output.println( "<head>" );
			output.println( "<title>BWHF Agent Manual Scan Summary Report</title>" );
			output.println( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />" );
			output.println( "<style>" );
			output.println( ".row00 {background:#9999ff;}" );
			output.println( ".row01 {background:#9999cc;}" );
			output.println( ".row10 {background:#ff9999;}" );
			output.println( ".row11 {background:#cc9999;}" );
			output.println( "</style>" );
			output.println( "</head>" );
			
			output.println( "<body><center>" );
			output.println( "<h2>BWHF Agent Manual Scan Summary Report</h2>" );
			output.println( "The manual scan took place between <b>" + DATE_FORMAT.format( scanStartTime ) + "</b> and <b>" + DATE_FORMAT.format( scanEndTime ) + "</b>" );
			output.println( "<br><br>" );
			for ( final String infoMessage : infoMessages ) {
				output.println( infoMessage );
				output.println( "<br>" );
			}
			output.println( "<br>" );
			
			output.println( "<b>The following players were found hacking:</b><br>" );
			output.println( "(You can use your browser's search function to find a specific name.)" );
			output.println( "<table border=1>" );
			output.println( "<tr><th>&nbsp;#&nbsp;<th>Hacker<th>Replays count<th>Replay<th>Hacks used" );
			int counter = 0;
			for ( final Map.Entry< String, Map< String, Set< Integer > > > playerNameHackerRepMapMapEntry :
						new TreeMap< String, Map< String, Set< Integer > > >( playerNameHackerRepMapMap ).entrySet() ) {
				counter++;
				
				final String playerName   = playerNameHackerRepMapMapEntry.getKey();
				final int    replayCount  = playerNameHackerRepMapMapEntry.getValue().size();
				
				output.println( "<tr class='row" + (counter & 0x01) + "0'><td align=right rowspan=" + replayCount + ">" + counter + "<td rowspan=" + replayCount + ">" + playerName + "<td align=center rowspan=" + replayCount + ">" + replayCount );
				
				int counter2 = 0;
				for ( final  Map.Entry< String, Set< Integer > > replayNameUsedHackTypeSetEntry : playerNameHackerRepMapMapEntry.getValue().entrySet() ) {
					if ( counter2++ > 0 )
						output.print( ( "<tr class='row" + (counter & 0x01) ) + (counter2 + 1 & 0x1) + "'>" );
					output.print( "<td>" + replayNameUsedHackTypeSetEntry.getKey() + "<td>" );
					final Iterator< Integer > usedHackTypeIterator = replayNameUsedHackTypeSetEntry.getValue().iterator();
					while ( usedHackTypeIterator.hasNext() ) {
						output.print( HackDescription.HACK_TYPE_NAMES[ usedHackTypeIterator.next() ] + "hack" );
						if ( usedHackTypeIterator.hasNext() )
							output.print( ", " );
						else
							output.println();
					}
				}
				
			}
			output.println( "</table>" );
			
			output.println( "</center></body>" );
			output.println( "</html>" );
			
			output.flush();
			output.close();
			
			logMessage( "A detailed HTML summary report has been saved to file '" + htmlReportFile.getAbsolutePath() + "'." );
			
			Utils.showURLInBrowser( htmlReportFile.toURI().toURL().toString() );
		}
		catch ( final Exception e ) {
			logMessage( "Failed to save HTML summary report to file '" + htmlReportFile.getAbsolutePath() + "'!" );
		}
		finally {
			if ( output != null ) {
				output.flush();
				output.close();
			}
		}
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_FLAG_HACKER_REPS          , Boolean.toString( flagHackerRepsCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_FLAG_HACKER_REPS_POSITION , Integer.toString( flagHackerRepsPositionComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CLEAN_HACK_FLAG           , Boolean.toString( cleanHackFlagCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_CREATE_HTML_SUMMARY_REPORT, Boolean.toString( createHtmlSummaryReportCheckBox.isSelected() ) );
	}
	
}
