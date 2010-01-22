package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Action;
import hu.belicza.andras.bwhf.model.PlayerActions;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhfagent.Consts;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import swingwt.awt.Dimension;
import swingwt.awt.GridLayout;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwt.awt.event.MouseAdapter;
import swingwt.awt.event.MouseEvent;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;
import swingwtx.swing.JProgressBar;
import swingwtx.swing.JScrollPane;
import swingwtx.swing.JTable;
import swingwtx.swing.JTextField;
import swingwtx.swing.ListSelectionModel;
import swingwtx.swing.table.DefaultTableModel;

/**
 * Player matcher tab.
 * 
 * @author Andras Belicza
 */
public class PlayerMatcherTab extends Tab {
	
	/** Player matcher algorithm version. */
	public static final String PLAYER_MATCHER_ALGORITHM_VERSION = "1.00";
	
	/** Don't compare players with same name checkbox. */
	private final JCheckBox dontCompareSameNamesCheckBox          = new JCheckBox( "Don't compare players with same names", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_DONT_COMPARE_SAME_NAMES ) ) );
	/** Show only matches of players text field.       */
	private final JTextField showOnlyPlayersTextField             = new JTextField( 1 );
	/** Exclude players text field.                    */
	private final JTextField excludePlayersTextField              = new JTextField( 1 );
	/** Authoritativeness threshold combobox.          */
	private final JComboBox  authoritativenessThresholdComboBox;
	/** Matching probability threshold combobox.       */
	private final JComboBox  matchingProbabilityThresholdComboBox = new JComboBox( new Object[] { "100%", "98%", "96%", "94%", "92%", "90%", "80%", "70%", "60%", "50%", "40%", "30%", "20%", "10%", "0%" } );
	/** Max displayable results combobox.              */
	private final JComboBox  maxDisplayableResultsComboBox        = new JComboBox( new Object[] { "100", "1,000", "5,000", "10,000", "20,000", "50,000", "100,000" } );
	
	/** Button to select folders to analyze.         */
	private final JButton selectFoldersButton  = new JButton( "Select folders to analyze recursively...", IconResourceManager.ICON_FOLDER_CHOOSER );
	/** Button to select files to analyze.           */
	private final JButton selectFilesButton    = new JButton( "Select files to analyze...", IconResourceManager.ICON_FILE_CHOOSER );
	/** Button to repeat analysis on the same files. */
	private final JButton repeatAnalysisButton = new JButton( "Repeat analysis", IconResourceManager.ICON_REPEAT );
	/** Button to stop analyzing.                    */
	private final JButton stopAnalyzingButton  = new JButton( "Stop analyzing", IconResourceManager.ICON_STOP );
	
	/** Variable to store stop requests of analyzing. */
	private volatile boolean requestedToStop;
	
	
	/**
	 * The extents of authoritativeness.
	 * @author Andras Belicza
	 */
	private static enum AuthoritativenessExtent {
		USELESS  ( "Useless"   ),
		VERY_POOR( "Very poor" ),
		POOR     ( "Poor"      ),
		AVERAGE  ( "Average"   ),
		GOOD     ( "Good"      ),
		VERY_GOOD( "Very good" ),
		EXCELLENT( "Excellent" );
		
		private final String displayName; 
		
		private AuthoritativenessExtent( final String displayName ) {
			this.displayName = displayName;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
	}
	
	/**
	 * Analysis of a player.
	 * @author Andras Belicza
	 */
	private static class PlayerAnalysis {
		final String  replayPath;
		final long    replayDate;
		final String  playerName;
		final String  loweredPlayerName;
		final byte    race;
		final int     frames;
		final int     realApm;
		final boolean firstTrainThenGather;
		final float   hotkeyRate;
		final float   selectRate;
		final float   shiftSelectRate;
		final float   rallySetRate;
		final float   moveRate;
		final float   attackMoveRate;
		final float[] usedHotkeyRates;
		final float   commandRepeationTendency;
		final float   averageSeletedUnits;
		
		
		public PlayerAnalysis( final String replayPath, final long replayDate, final String playerName, final byte race, final int frames, final int realApm,
				final boolean firstTrainThenGather, final float hotkeyRate, final float selectRate, final float shiftSelectRate, final float rallySetRate, final float moveRate, final float attackMoveRate, final float[] usedHotkeyRates,
				final float commandRepeationTendency, final float averageSelectedUnits ) {
			this.replayPath               = replayPath;
			this.replayDate               = replayDate;
			this.playerName               = playerName;
			loweredPlayerName             = playerName.toLowerCase();
			this.race                     = race;
			this.frames                   = frames;
			this.realApm                  = realApm;
			this.firstTrainThenGather     = firstTrainThenGather;
			this.hotkeyRate               = hotkeyRate;
			this.selectRate               = selectRate;
			this.shiftSelectRate          = shiftSelectRate;
			this.rallySetRate             = rallySetRate;
			this.moveRate                 = moveRate;
			this.attackMoveRate           = attackMoveRate;
			this.usedHotkeyRates          = usedHotkeyRates;
			this.commandRepeationTendency = commandRepeationTendency;
			this.averageSeletedUnits      = averageSelectedUnits;
		}
	}
	
	/**
	 * Comparison result of analysis of 2 players.
	 * @author Andras Belicza
	 */
	private static class Comparison implements Comparable< Comparison > {
		
		final PlayerAnalysis          analysis1;
		final PlayerAnalysis          analysis2;
		final AuthoritativenessExtent authoritativenessExtent;
		final float                   matchingProbability;
		final boolean                 thresholdReached;
		
		public Comparison( final PlayerAnalysis analysis1, final PlayerAnalysis analysis2, final AuthoritativenessExtent authoritativenessExtentThreshold, final float matchingProbabilityThreshold ) {
			this.analysis1 = analysis1;
			this.analysis2 = analysis2;
			authoritativenessExtent = determineAuthoritativenessExtent();
			// If authoritativeness extent is below the threshold, don't bother calculating the matching probability
			if ( authoritativenessExtent.compareTo( authoritativenessExtentThreshold ) >= 0  ) {
				matchingProbability = calculateMatchingProbability();
				thresholdReached = matchingProbability >= matchingProbabilityThreshold;
			}
			else {
				matchingProbability = 0.0f;
				thresholdReached     = false;
			}
		}
		
		@Override
		public String toString() {
			return String.format( "%-9s %3.1f%% - %s, %s  (%s, %s)", authoritativenessExtent.toString(), matchingProbability, analysis1.playerName, analysis2.playerName, analysis1.replayPath, analysis2.replayPath );
		}
		
		/** Number of frames in four minutes. */
		private static final int FOUR_MINUTES_FRAMES = ReplayHeader.convertSecondsToFrames( 4*60 );
		
		/**
		 * Calculates and returns the extent of authoritativeness of this comparison.<br>
		 * This depends on:
		 * <ul>
		 * 		<li>Comparing different races are a huge setback (the matching algorithm operates on action distribution which naturally changes with races).
		 * 		<li>Difference in APM is an obvious negative factor.
		 * 		<li>If games differ in duration, the action distribution might change drastically (gather and micro focused on early, macro later on).
		 * 		<li>Playing style changes over time. Replays from different ages are not very authoritative.
		 * </ul>
		 * @return the extent of authoritativeness of this comparison
		 */
		private AuthoritativenessExtent determineAuthoritativenessExtent() {
			float authroitativenessExtent = 1.0f; // Value 0.0 is the worst, 1.0 is the best
			
			// Race dependent
			if ( analysis1.race != analysis2.race )
				authroitativenessExtent *= 0.5f;
			
			// APM dependent
			if ( analysis1.realApm < 30 || analysis2.realApm < 30 ) // Assumed obsing game
				authroitativenessExtent = 0.0f;
			else
				authroitativenessExtent *= rate( analysis1.realApm, analysis2.realApm );
			
			// Duration dependent
			if ( analysis1.frames < FOUR_MINUTES_FRAMES )
				authroitativenessExtent *= (float) analysis1.frames / FOUR_MINUTES_FRAMES;
			authroitativenessExtent *= rate( analysis1.frames, analysis2.frames );
			
			// Save time dependent
			final int days  = (int) ( Math.abs( analysis1.replayDate - analysis2.replayDate ) / (1000l*60l*60l*24l) );
			authroitativenessExtent *= days < 2000 ? ( 2000.0f - days ) / 2000.0f : 0.0f;
			
			// Range check (might be out of range due to rounding problems or if it remains 1.0)
			final int extentOrdinal = (int) ( authroitativenessExtent * AuthoritativenessExtent.values().length );
			if ( extentOrdinal > AuthoritativenessExtent.values().length - 1 )
				return AuthoritativenessExtent.EXCELLENT;
			if ( extentOrdinal < 0 )
				return AuthoritativenessExtent.USELESS;
			return AuthoritativenessExtent.values()[ extentOrdinal ];
		}
		
		
		/** Weights of the different components of the matching probability. */
		private static final float[] MATCHING_WEIGHTS = new float[] { 0.5f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };
		static {
			// Scale the weights so they add up to 100.0
			float sumWeights = 0.0f;
			for ( final float weight : MATCHING_WEIGHTS )
				sumWeights += weight;
			final float mulFactor = 100.0f / sumWeights;
			for ( int i = MATCHING_WEIGHTS.length - 1; i >= 0; i-- )
				MATCHING_WEIGHTS[ i ] *= mulFactor;
		}
		
		/**
		 * Calculates and returns the matching probability.
		 * @return the matching probability
		 */
		private float calculateMatchingProbability() {
			float matchingProbability = 0.0f;
			int   componentIndex      = 0;
			
			// Startup dependent
			matchingProbability += MATCHING_WEIGHTS[ componentIndex++ ] * ( analysis1.firstTrainThenGather == analysis2.firstTrainThenGather ? 1.0f : 0.0f );
			// Hotkey usage dependent
			matchingProbability += MATCHING_WEIGHTS[ componentIndex++ ] * rate( analysis1.hotkeyRate     , analysis2.hotkeyRate      );
			// Select usage dependent
			matchingProbability += MATCHING_WEIGHTS[ componentIndex++ ] * rate( analysis1.selectRate     , analysis2.selectRate      );
			// Shift-select usage dependent
			matchingProbability += MATCHING_WEIGHTS[ componentIndex++ ] * rate( analysis1.shiftSelectRate, analysis2.shiftSelectRate );
			// Rally set usage dependent
			matchingProbability += MATCHING_WEIGHTS[ componentIndex++ ] * rate( analysis1.rallySetRate   , analysis2.rallySetRate    );
			// Move usage dependent
			matchingProbability += MATCHING_WEIGHTS[ componentIndex++ ] * rate( analysis1.moveRate       , analysis2.moveRate        );
			// Attack move usage dependent
			matchingProbability += MATCHING_WEIGHTS[ componentIndex++ ] * rate( analysis1.attackMoveRate , analysis2.attackMoveRate  );
			// Used hotkeys dependent
			final float weightFor1Hotkey = MATCHING_WEIGHTS[ componentIndex++ ] / analysis1.usedHotkeyRates.length;
			for ( int i = analysis1.usedHotkeyRates.length - 1; i >= 0; i-- )
				matchingProbability += weightFor1Hotkey * rate( analysis1.usedHotkeyRates[ i ], analysis2.usedHotkeyRates[ i ] );
			// Command repeation tendency dependent
			matchingProbability += MATCHING_WEIGHTS[ componentIndex++ ] * rate( analysis1.commandRepeationTendency, analysis2.commandRepeationTendency  );
			// Average selected units dependent
			matchingProbability += MATCHING_WEIGHTS[ componentIndex++ ] * rate( analysis1.averageSeletedUnits, analysis2.averageSeletedUnits );
			
			// Range check (might be out of range due to rounding problems)
			if ( matchingProbability < 0.0f )
				matchingProbability = 0.0f;
			if ( matchingProbability > 100.0f )
				matchingProbability = 100.0f;
			return matchingProbability;
		}
		
		/**
		 * Calculates the rate (quotient) of 2 numbers. The smaller number is divided by the greater one. This will result in a rate not greater than 1.0.<br>
		 * If both numbers are zeros (or negative), the value <code>1.0f</code> is returned.
		 * @param value1 one of the numbers
		 * @param value2 the other number
		 * @return rate (quotient) of 2 numbers not greater than 1.0f
		 */
		private static float rate( final float value1, final float value2 ) {
			if ( value1 <= 0.0f && value2 <= 0.0f )
				return 1.0f;
			return value1 < value2 ? value1 / value2 : value2 / value1;
		}

		/**
		 * Compares this comparison to another one based on the extent of authoritativeness and the matching probability.
		 * @param comparison comparison to compare to
		 * @return >0 if this comparison has higher authoritativeness extent or higher matching probability in case of equal authoritativeness extent; 0 if both equal; and <0 if lower authoritativeness extent or lower matching probability in case of equal authoritativeness
		 */
		public int compareTo( final Comparison comparison ) {
			if ( authoritativenessExtent == comparison.authoritativenessExtent )
				return Float.compare( matchingProbability, comparison.matchingProbability );
			return authoritativenessExtent.compareTo( comparison.authoritativenessExtent );
		}
		
	}
	
	/** Column names of the result table.                          */
	private static final String[]  RESULT_TABLE_COLUMN_NAMES   = new String[]  { "Authoritativeness", "Matching probability", "Player #1", "Player #2", "Replay #1", "Replay #2" };
	/** Tells if the column has to be sorted ascendant by default. */
	private static final boolean[] DEFAULT_SORTING_ASCENDNANTS = new boolean[] { false              , false                 , true       , true       , true       , true        };
	
	/** The progress bar component. */
	private final JProgressBar progressBar = new JProgressBar();
	
	/** Reference to the source files of the last analysis. */
	private File[] lastAnalysisSourceFiles;
	
	/** Table displaying the results.             */
	private final JTable resultTable = new JTable();
	/** List of comparisons of the last analysis. */
	private final ArrayList< Comparison > comparisonList = new ArrayList< Comparison >();
	private int     lastSortingIndex;
	private boolean lastSortingAscendant;
	
	/** Label to display the results count. */
	private final JLabel resultsCountLabel = new JLabel();
	
	/**
	 * Creates a new PlayersNetworkTab.
	 */
	public PlayerMatcherTab() {
		super( "Player matcher", IconResourceManager.ICON_PLAYER_MATCHER );
		
		final List< AuthoritativenessExtent > authoritativenessExtentList = Arrays.asList( AuthoritativenessExtent.values() );
		Collections.reverse( authoritativenessExtentList );
		authoritativenessThresholdComboBox = new JComboBox( authoritativenessExtentList.toArray( new AuthoritativenessExtent[ authoritativenessExtentList.size() ] ) );
		
		authoritativenessThresholdComboBox  .setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTHORITATIVENESS_THRESHOLD ) ) );
		matchingProbabilityThresholdComboBox.setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_MATCHING_PROBABILITY_THRESHOLD ) ) );
		maxDisplayableResultsComboBox       .setSelectedIndex( Integer.parseInt( Utils.settingsProperties.getProperty( Consts.PROPERTY_MAX_DISPLAYABLE_RESULTS ) ) );
		
		buildGUI();
	}
	
	/**
	 * Builds the graphical user interface of the tab.
	 */
	protected void buildGUI() {
		final JButton visitPlayerMatcherHelpPageButton = new JButton( "Visit Player matcher help page", IconResourceManager.ICON_WORLD_GO );
		visitPlayerMatcherHelpPageButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.showURLInBrowser( Consts.PLAYER_MATCHER_HELP_PAGE_URL );
			}
		} );
		contentBox.add( Utils.wrapInPanel( visitPlayerMatcherHelpPageButton ) );
		
		contentBox.add( Utils.wrapInPanel( dontCompareSameNamesCheckBox ) );
		
		final JPanel thresholdPanel = new JPanel( new GridLayout( 5, 2, 7, 0 ) );
		thresholdPanel.setBorder( BorderFactory.createTitledBorder( "Display filters and thresholds:" ) );
		thresholdPanel.add( new JLabel( "Show only matches of players:" ) );
		thresholdPanel.add( showOnlyPlayersTextField );
		thresholdPanel.add( new JLabel( "Exclude matches with players:" ) );
		thresholdPanel.add( excludePlayersTextField );
		thresholdPanel.add( new JLabel( "Extent of authoritativeness:" ) );
		thresholdPanel.add( authoritativenessThresholdComboBox );
		thresholdPanel.add( new JLabel( "Matching probability:" ) );
		thresholdPanel.add( matchingProbabilityThresholdComboBox );
		thresholdPanel.add( new JLabel( "Max displayable results:" ) );
		thresholdPanel.add( maxDisplayableResultsComboBox );
		contentBox.add( Utils.wrapInPanel( thresholdPanel ) );
		
		final ActionListener selectFilesAndFoldersActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( MainFrame.getInstance().generalSettingsTab.getReplayStartFolder() );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( Utils.SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( event.getSource() == selectFoldersButton ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES );
				fileChooser.setMultiSelectionEnabled( true );
				// SwingWT does not support selecting multiple directories yet, getSelectedFiles() returns null so I have to call getSelectedFile() in case of folders.
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					analyzeFilesAndFolders( event.getSource() == selectFoldersButton ? new File[] { fileChooser.getSelectedFile() } : fileChooser.getSelectedFiles() );
			}
		};
		
		final JPanel buttonsPanel = Utils.createWrapperPanel();
		selectFilesButton.setMnemonic( 'f' );
		selectFilesButton.addActionListener( selectFilesAndFoldersActionListener );
		buttonsPanel.add( selectFilesButton );
		selectFoldersButton.setMnemonic( 'd' );
		selectFoldersButton.addActionListener( selectFilesAndFoldersActionListener );
		buttonsPanel.add( selectFoldersButton );
		repeatAnalysisButton.setMnemonic( 'r' );
		repeatAnalysisButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				analyzeFilesAndFolders( lastAnalysisSourceFiles );
			}
		} );
		repeatAnalysisButton.setEnabled( false );
		buttonsPanel.add( repeatAnalysisButton );
		contentBox.add( buttonsPanel );
		
		stopAnalyzingButton.setEnabled( false );
		stopAnalyzingButton.setMnemonic( 't' );
		stopAnalyzingButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				requestedToStop = true; // Access to a volatile variable is automatically synchronized from Java 5.0
				stopAnalyzingButton.setEnabled( false );
			}
		} );
		contentBox.add( Utils.wrapInPanel( stopAnalyzingButton ) );
		
		contentBox.add( Utils.wrapInPanel( new JLabel( "Tip: Double click with LEFT button opens replay #1, double click with RIGHT button opens replay #2." ) ) );
		
		progressBar.setMaximumSize( Utils.getMaxDimension() );
		contentBox.add( progressBar );
		
		final JPanel panel = Utils.createWrapperPanelLeftAligned();
		panel.add( new JLabel( "Comparison result:" ) );
		panel.add( resultsCountLabel );
		contentBox.add( panel );
		
		resultTable.setPreferredSize( new Dimension( 50, 50 ) );
		resultTable.setRowSelectionAllowed( true );
		resultTable.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		resultTable.setColumnSelectionAllowed( true );
		resultTable.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				if ( resultTable.getSelectedRow() >= 0 && event.getClickCount() == 2 ) {
					final String replayPath = event.getButton() == MouseEvent.BUTTON1 ? comparisonList.get( resultTable.getSelectedRow() ).analysis1.replayPath : comparisonList.get( resultTable.getSelectedRow() ).analysis2.replayPath;
					MainFrame.getInstance().selectTab( MainFrame.getInstance().chartsTab );
					MainFrame.getInstance().chartsTab.setReplayFile( new File( replayPath ) );
				}
			}
		} );
		resultTable.getTableHeader().addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				final int     sortingIndex     = resultTable.getTableHeader().columnAtPoint( event.getPoint() );
				final boolean sortingAscendant = sortingIndex == lastSortingIndex ? !lastSortingAscendant : DEFAULT_SORTING_ASCENDNANTS[ sortingIndex ];
				
				lastSortingIndex     = sortingIndex;
				lastSortingAscendant = sortingAscendant;
				
				sortResultTable( -1 );
			}
		} );
		contentBox.add( new JScrollPane( resultTable ) );
	}
	
	/**
	 * Analyzes files and folders to find AKAs..
	 * @param files files and folders to be analyzed
	 */
	protected void analyzeFilesAndFolders( final File[] files ) {
		if ( !selectFilesButton.isEnabled() )
			return;
		
		requestedToStop = false;
		
		selectFoldersButton .setEnabled( false );
		selectFilesButton   .setEnabled( false );
		repeatAnalysisButton.setEnabled( false );
		stopAnalyzingButton .setEnabled( true  );
		
		lastAnalysisSourceFiles = files;
		
		new NormalThread( "Player matcher analyzer" ) {
			/** List of replay files to be analyzed. */
			final List< File > replayFileList = new ArrayList< File >();
			
			@Override
			public void run() {
				try {
					final boolean                 compareSameNames             = !dontCompareSameNamesCheckBox.isSelected();
					final List< String >          showOnlyPlayerList           = splitCommaSeparatedList( showOnlyPlayersTextField.getText() );
					final List< String >          excludePlayerList            = splitCommaSeparatedList( excludePlayersTextField .getText() );
					final float                   matchingProbabilityThreshold = Float.parseFloat( ( (String) matchingProbabilityThresholdComboBox.getSelectedItem() ).substring( 0, ( (String) matchingProbabilityThresholdComboBox.getSelectedItem() ).length() - 1 ) );
					final AuthoritativenessExtent authoritativenessThreshold   = (AuthoritativenessExtent) authoritativenessThresholdComboBox.getSelectedItem();
					
					resultsCountLabel.setText( "Counting..." );
					progressBar.setValue( 0 );
					
					chooseReplayFiles( files );
					progressBar.setMaximum( replayFileList.size() );
					
					if ( requestedToStop )
						return;
					
					resultsCountLabel.setText( "Analyzing " + replayFileList.size() + " replay" + ( replayFileList.size() == 1 ? "" : "s" ) + "..." );
					
					int counter = 0;	
					int skippedRepsCount = 0;
					final List< PlayerAnalysis > playerAnalysisList = new ArrayList< PlayerAnalysis >();
					comparisonList.clear();
					comparisonList.trimToSize();
					comparisonList.ensureCapacity( 10 );
					
					for ( final File replayFile : replayFileList ) {
						if ( requestedToStop )
							return;
						
						final Replay replay = BinRepParser.parseReplay( replayFile, true, false, false, false );
						
						if ( replay == null ) {
							skippedRepsCount++;
						}
						else {
							// Players in one replay must and should not be compared, we add analysis of players of this replay after the replay has been processed.
							final List< PlayerAnalysis > replayPlayerAnalysisList = new ArrayList< PlayerAnalysis >( replay.replayActions.players.length );
							
							for ( final PlayerActions playerActions : replay.replayActions.players ) {
								if ( excludePlayerList != null )
									if ( excludePlayerList.contains( playerActions.playerName.toLowerCase() ) )
										continue;
								
								final ReplayHeader replayHeader = replay.replayHeader;
								
								final int   playerIndex        = replayHeader.getPlayerIndexByName( playerActions.playerName );
								final int   actionsCount       = playerActions.actions.length;
								final float actionsCountFloat  = actionsCount;
								
								final int frames               = playerActions.actions.length > 0 ? playerActions.actions[ actionsCount - 1 ].iteration : 0;
								final int seconds              = ReplayHeader.convertFramesToSeconds( frames );
								boolean firstTrainThenGather   = false;
								int   hotkeysCount             = 0;
								int   selectsCount             = 0;
								int   shiftSelectsCount        = 0;
								int   rallySetsCount           = 0;
								int   movesCount               = 0;
								int   attackMovesCount         = 0;
								int[] usedHotkeysCounts        = new int[ 10 ]; // 0..9
								int   commandRepeationTendency = 0;
								int   averageSelectedUnits     = 0;
								
								if ( playerActions.actions.length >= 3 ) {
									for ( int i = 0; i < 2; i++ )
										if ( playerActions.actions[ i ].actionNameIndex == Action.ACTION_NAME_INDEX_TRAIN || playerActions.actions[ i ].actionNameIndex == Action.ACTION_NAME_INDEX_HATCH ) {
											firstTrainThenGather = true;
											break;
										}
								}
								// Count actions...
								Action lastAction = null;
								for ( final Action action : playerActions.actions ) {
									switch ( action.actionNameIndex ) {
									case Action.ACTION_NAME_INDEX_HOTKEY       : hotkeysCount++     ;
										try {
											final int hotkey = action.parameters.charAt( action.parameters.indexOf( ',' ) + 1 ) - '0';
											usedHotkeysCounts[ hotkey ]++;
										}
										catch ( final Exception e ) {}
										break;
									case Action.ACTION_NAME_INDEX_SELECT       : {
										selectsCount++;
										averageSelectedUnits++; // There is at least 1 parameter, and there is one less commas than parameters
										for ( int i = action.parameters.length() - 1; i >= 0; i-- )
											if ( action.parameters.charAt( i ) == ',' )
												averageSelectedUnits++;
										break;
									}
									case Action.ACTION_NAME_INDEX_SHIFT_SELECT : shiftSelectsCount++; break;
									case Action.ACTION_NAME_INDEX_SET_RALLY    : rallySetsCount++   ; break;
									case Action.ACTION_NAME_INDEX_MOVE         : movesCount++       ; break;
									case Action.ACTION_NAME_INDEX_ATTACK_MOVE  : attackMovesCount++ ; break;
									}
									
									if ( lastAction != null && action.actionNameIndex != Action.ACTION_NAME_INDEX_UNKNOWN && lastAction.actionNameIndex == action.actionNameIndex )
										commandRepeationTendency++;
									lastAction = action;
								}
								
								final float[] usedHotkeyRates = new float[ usedHotkeysCounts.length ];
								for ( int i = usedHotkeyRates.length - 1; i >= 0; i-- )
									usedHotkeyRates[ i ] = usedHotkeysCounts[ i ] / actionsCountFloat;
								
								final PlayerAnalysis playerAnalysis = new PlayerAnalysis( replayFile.getAbsolutePath(), replayHeader.saveTime.getTime(), playerActions.playerName, replayHeader.playerRaces[ playerIndex ],
										frames, frames == 0 ? 0 : seconds == 0 ? 0 : playerActions.actions.length * 60 / seconds,
										firstTrainThenGather, hotkeysCount / actionsCountFloat, selectsCount / actionsCountFloat, shiftSelectsCount / actionsCountFloat, rallySetsCount / actionsCountFloat, movesCount / actionsCountFloat, attackMovesCount / actionsCountFloat, usedHotkeyRates,
										commandRepeationTendency / actionsCountFloat, (float) averageSelectedUnits / selectsCount );
								replayPlayerAnalysisList.add( playerAnalysis );
								
								final boolean playerNameContained = showOnlyPlayerList != null && showOnlyPlayerList.contains( playerAnalysis.loweredPlayerName );
								for ( final PlayerAnalysis playerAnalysis2 : playerAnalysisList ) {
									if ( showOnlyPlayerList != null )
										if ( !playerNameContained && !showOnlyPlayerList.contains( playerAnalysis2.loweredPlayerName ) )
											continue;
									
									if ( compareSameNames || !playerAnalysis2.loweredPlayerName.equals( playerAnalysis.loweredPlayerName ) ) {
										final Comparison comparison = new Comparison( playerAnalysis2, playerAnalysis, authoritativenessThreshold, matchingProbabilityThreshold );
										if ( comparison.thresholdReached )
											comparisonList.add( comparison );
									}
								}
							}
							
							playerAnalysisList.addAll( replayPlayerAnalysisList );
						}
						
						progressBar.setValue( ++counter );
					}
					
					final int maxDisplayableResults = Integer.parseInt( ( (String) maxDisplayableResultsComboBox.getSelectedItem() ).replace( ",", "" ) );
					
					resultsCountLabel.setText( comparisonList.size() + " match" + ( comparisonList.size() == 1 ? "" : "es" ) 
							+ ( comparisonList.size() > maxDisplayableResults ? " limited to " + maxDisplayableResults : "" )
							+ " in " + replayFileList.size() + " replay" + ( replayFileList.size() == 1 ? "" : "s" )
							+ ( skippedRepsCount > 0 ? " (skipped " + skippedRepsCount + ( skippedRepsCount == 1 ? " replay)" : " replays)" ) : "" ) );
					
					lastSortingIndex     = 0;
					lastSortingAscendant = DEFAULT_SORTING_ASCENDNANTS[ lastSortingIndex ];
					sortResultTable( comparisonList.size() > maxDisplayableResults ? maxDisplayableResults : -1 );
				}
				finally {
					if ( requestedToStop )
						resultsCountLabel.setText( "Analyzing was manually aborted!" );
					stopAnalyzingButton .setEnabled( false );
					repeatAnalysisButton.setEnabled( true  );
					selectFilesButton   .setEnabled( true  );
					selectFoldersButton .setEnabled( true  );
				}
			}
			
			private final java.io.FileFilter IO_REPLAY_FILE_FILTER = new java.io.FileFilter() {
				public boolean accept( final File pathname ) {
					return Utils.SWING_REPLAY_FILE_FILTER.accept( pathname );
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
	 * Splits a comma separated value list, and returns a list of their lowercased trimmed values.
	 * @param text text to be split
	 * @return a list of the lowercased trimmed values; or <code>null</code> if the text does not contain values)
	 */
	private static List< String > splitCommaSeparatedList( final String text ) {
		final List< String > valueList = new ArrayList< String >( 2 );
		final StringTokenizer tokenizer = new StringTokenizer( text, "," );
		
		while ( tokenizer.hasMoreTokens() ) {
			final String token = tokenizer.nextToken().trim();
			if ( token.length() > 0 )
				valueList.add( token.toLowerCase() );
		}
		
		return valueList.isEmpty() ? null : valueList;
	}
	
	/**
	 * Sorts the result table based on the <code>lastSortingIndex</code> and <code>lastSortingAscendant</code> properties.
	 * @param maxDisplayableResults max displayable results (worse comparisions will be discarded)
	 */
	private void sortResultTable( final int maxDisplayableResults ) {
		Collections.sort( comparisonList, new Comparator< Comparison >() {
			public int compare( final Comparison comparison1, final Comparison comparison2 ) {
				switch ( lastSortingIndex ) {
				case 0 : return lastSortingAscendant ? comparison1.compareTo( comparison2 ) : -comparison1.compareTo( comparison2 );
				case 1 : {
					int comparisonResult = Float.compare( comparison1.matchingProbability, comparison2.matchingProbability );
					if ( comparisonResult == 0 )
						comparisonResult = comparison2.authoritativenessExtent.ordinal() - comparison1.authoritativenessExtent.ordinal();
					return lastSortingAscendant ? comparisonResult : -comparisonResult;
				}
				case 2 : {
					int comparisonResult = comparison1.analysis1.loweredPlayerName.compareTo( comparison2.analysis1.loweredPlayerName );
					if ( comparisonResult == 0 )
						comparisonResult = -comparison1.compareTo( comparison2 );
					return lastSortingAscendant ? comparisonResult : -comparisonResult;
				}
				case 3 : {
					int comparisonResult = comparison1.analysis2.loweredPlayerName.compareTo( comparison2.analysis2.loweredPlayerName );
					if ( comparisonResult == 0 )
						comparisonResult = -comparison1.compareTo( comparison2 );
					return lastSortingAscendant ? comparisonResult : -comparisonResult;
				}
				case 4 : return lastSortingAscendant ? comparison1.analysis1.replayPath.compareTo( comparison2.analysis1.replayPath ) : -comparison1.analysis1.replayPath.compareTo( comparison2.analysis1.replayPath );
				case 5 : return lastSortingAscendant ? comparison1.analysis2.replayPath.compareTo( comparison2.analysis2.replayPath ) : -comparison1.analysis2.replayPath.compareTo( comparison2.analysis2.replayPath );
				}
				return 0;
			}
		} );
		
		if ( maxDisplayableResults >= 0 ) {
			for ( int i = comparisonList.size() - 1; i >= maxDisplayableResults; i-- )
				comparisonList.remove( i );
			comparisonList.trimToSize();
			System.gc();
		}
		
		refreshResultTable();
	}
	
	/**
	 * Refreshes the result table from the <code>comparisonList</code>.
	 */
	private void refreshResultTable() {
		final Vector< Vector< String > > resultDataVector = new Vector< Vector< String > >( comparisonList.size() );
		for ( final Comparison comparison : comparisonList ) {
			final Vector< String > rowVector = new Vector< String >( 6 );
			rowVector.add( comparison.authoritativenessExtent.toString() );
			rowVector.add( String.format( "%.1f%%", comparison.matchingProbability ) );
			rowVector.add( comparison.analysis1.playerName );
			rowVector.add( comparison.analysis2.playerName );
			rowVector.add( comparison.analysis1.replayPath );
			rowVector.add( comparison.analysis2.replayPath );
			resultDataVector.add( rowVector );
		}
		final Vector< String > columnNameVector = new Vector< String >( 6 );
		Collections.addAll( columnNameVector, RESULT_TABLE_COLUMN_NAMES );
		
		resultTable.setModel( new DefaultTableModel( resultDataVector, columnNameVector ) {
			@Override
			public boolean isCellEditable( final int row, final int column ) {
				return false;
			}
		} );
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_DONT_COMPARE_SAME_NAMES       , Boolean.toString( dontCompareSameNamesCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_AUTHORITATIVENESS_THRESHOLD   , Integer.toString( authoritativenessThresholdComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_MATCHING_PROBABILITY_THRESHOLD, Integer.toString( matchingProbabilityThresholdComboBox.getSelectedIndex() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_MAX_DISPLAYABLE_RESULTS       , Integer.toString( maxDisplayableResultsComboBox.getSelectedIndex() ) );
	}
	
}
