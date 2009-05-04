package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.bwhfagent.view.replayfilter.CreatorNameReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.DurationReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.GameEngineReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.GameNameReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.GameTypeReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.MapNameReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.MapSizeReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.PlayerColorReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.PlayerNameReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.PlayerRaceReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.ReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.SaveTimeReplayFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import swingwt.awt.BorderLayout;
import swingwt.awt.Color;
import swingwt.awt.Dimension;
import swingwt.awt.GridBagConstraints;
import swingwt.awt.GridBagLayout;
import swingwt.awt.Insets;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwt.awt.event.MouseAdapter;
import swingwt.awt.event.MouseEvent;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.Box;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JOptionPane;
import swingwtx.swing.JPanel;
import swingwtx.swing.JProgressBar;
import swingwtx.swing.JScrollPane;
import swingwtx.swing.JTable;
import swingwtx.swing.JTextField;
import swingwtx.swing.ListSelectionModel;
import swingwtx.swing.event.ListSelectionEvent;
import swingwtx.swing.event.ListSelectionListener;
import swingwtx.swing.table.DefaultTableModel;

/**
 * Replay search tab.
 * 
 * @author Andras Belicza
 */
public class ReplaySearchTab extends Tab {
	
	/** Simple date format to format and parse replay save time. */
	private static final DateFormat SIMPLE_DATE_FORMAT        = new SimpleDateFormat( "yyyy-MM-dd" );
	/** Result table column names.                               */
	private static final String[]   RESULT_TABLE_COLUMN_NAMES = new String[] { "Engine", "Map", "Duration", "Game type", "Players", "Saved on", "Game name", "Creator", "File" }; 
	/** Index of the file name column in the result table.       */
	private static final int        RESULT_TABLE_FILE_NAME_COLUMN_INDEX = RESULT_TABLE_COLUMN_NAMES.length - 1;
	
	/** Separator to use to separate values in the result list files. */
	private static final char   RESULT_LIST_FILE_VALUE_SEPARATOR_CHAR   = '\t';
	/** RESULT_LIST_FILE_VALUE_SEPARATOR_CHAR as a String.            */
	private static final String RESULT_LIST_FILE_VALUE_SEPARATOR_STRING = Character.toString( RESULT_LIST_FILE_VALUE_SEPARATOR_CHAR );
	
	/** Background color to be used if a filter field contains syntax error. */
	private static final Color      NORMAL_BACKGROUND_COLOR       = Color.WHITE;
	/** Background color to be used if a filter field contains syntax error. */
	private static final Color      SYNTAX_ERROR_BACKGROUND_COLOR = new Color( 255, 130, 130 );
	
	/**
	 * Class to specify a map size.
	 * @author Andras Belicza
	 */
	public static class MapSize {
		/** Standard map lengths (applies both to width and height). */
		private static final short[] STANDARD_MAP_LENGTHS = { 64, 96, 128, 192, 256 };
		
		/** Map size indicating any map size. */
		public static final MapSize MAP_SIZE_ANY = new MapSize( (short) 0, (short) 0 );
		/** Standard map sizes. */
		public static final MapSize[] STANDARD_MAP_SIZES = new MapSize[ 1 + STANDARD_MAP_LENGTHS.length * STANDARD_MAP_LENGTHS.length ];
		static {
			int counter = 0;
			STANDARD_MAP_SIZES[ counter++ ] = MAP_SIZE_ANY;
			for ( final short height : STANDARD_MAP_LENGTHS )
				for ( final short width: STANDARD_MAP_LENGTHS )
					STANDARD_MAP_SIZES[ counter++ ] = new MapSize( height, width );
		}
		
		/** Height of the map. */
		public final short height;
		/** Width of the map.  */
		public final short width;
		/**
		 * Creates a new MapSize.
		 * @param width  width of the map
		 * @param height height of the map
		 */
		private MapSize( final short height, final short width ) {
			this.height = height;
			this.width  = width;
		}
		
		@Override
		public String toString() {
			return this == MAP_SIZE_ANY ? "<any>" : height + " x " + width;
		}
	}
	
	/** Reference to the main frame. */
	private final MainFrame mainFrame = MainFrame.getInstance();
	
	/** Checkbox to add new search results to previous one (do not clear). */
	private final JCheckBox appendResultsToTableCheckBox = new JCheckBox( "Append results to table (will not clear table)", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_APPEND_RESULTS_TO_TABLE ) ) );
	
	/** Button to select folders to search.  */
	private final JButton selectFoldersButton        = new JButton( "Select folders to search recursively..." );
	/** Button to select files to search.    */
	private final JButton selectFilesButton          = new JButton( "Select files to search..." );
	/** Button to stop the current search. */
	private final JButton stopSearchButton           = new JButton( "Stop current search" );
	/** Button to repeat search on the same files. */
	private final JButton repeatSearchButton         = new JButton( "Repeat search" );
	/** Button to run search on the same files. */
	private final JButton searchPreviousResultButton = new JButton( "Search in previous results (narrows previous results)" );
	
	/** Button to save result list.  */
	private final JButton saveResultListButton       = new JButton( "Save result list..." );
	/** Button to save result list.  */
	private final JButton loadResultListButton       = new JButton( "Load result list..." );
	
	/** The progress bar component. */
	private final JProgressBar progressBar = new JProgressBar();
	
	/** Table displaying the results. */
	private final JTable resultTable = new JTable();
	
	/** Label to display the results count. */
	private final JLabel resultsCountLabel = new JLabel();
	
	/** Button to show selected replay on charts.               */
	private final JButton showOnChartsButton    = new JButton( "Show on charts"    );
	/** Button to scan selected replays for hacks.              */
	private final JButton scanForHacksButton    = new JButton( "Scan for hacks"    );
	/** Button to display game chat from selected replay.       */
	private final JButton displayGameChatButton = new JButton( "Display game chat" );
	/** Button to extract game chat from selected replays.      */
	private final JButton extractGameChatButton = new JButton( "Extract game chat" );
	/** Button to remove selected replays from the result list. */
	private final JButton removeFromListButton  = new JButton( "Remove from list"  );
	/** Button to copy selected replay files.                   */
	private final JButton copyReplaysButton     = new JButton( "Copy replays..."   );
	/** Button to move selected replay files.                   */
	private final JButton moveReplaysButton     = new JButton( "Move replays..."   );
	/** Button to delete selected replay files.                 */
	private final JButton deleteReplaysButton   = new JButton( "Delete replays..." );
	
	/** Reference to the source files of the last search.            */
	private       File[]           lastSearchSourceFiles;
	/** Reference to the result file file of the last search.        */
	private final List< File >     lastSearchResultFileList = new ArrayList< File >();
	/** Reference to the result description list of the last search. */
	private final List< String[] > lastSearchResultRowsData = new ArrayList< String[] >();
	/** Number of replay files in the last search.                   */
	private       int              lastSearchReplayFilesCount;
	
	
	/** Variable to store stop requests of search. */
	private volatile boolean requestedToStop;
	
	/** Game engine filter checkboxes.                    */
	private final JCheckBox[] gameEngineCheckBoxes          = new JCheckBox[ ReplayHeader.GAME_ENGINE_NAMES.length ];
	/** Game name filter text field.                      */
	private final JTextField  gameNameTextField             = new JTextField();
	/** Game name filter is regexp checkbox.              */
	private final JCheckBox   gameNameRegexpCheckBox        = new JCheckBox( "Regexp" );
	/** Game name filter must be exact match checkbox.    */
	private final JCheckBox   gameNameExactMatchCheckBox    = new JCheckBox( "Exact match" );
	/** Creator name filter text field.                   */
	private final JTextField  creatorNameTextField          = new JTextField();
	/** Creator name filter is regexp checkbox.           */
	private final JCheckBox   creatorNameRegexpCheckBox     = new JCheckBox( "Regexp" );
	/** Creator name filter must be exact match checkbox. */
	private final JCheckBox   creatorNameExactMatchCheckBox = new JCheckBox( "Exact match" );
	/** Map name filter text field.                       */
	private final JTextField  mapNameTextField              = new JTextField();
	/** Map name filter is regexp checkbox.               */
	private final JCheckBox   mapNameRegexpCheckBox         = new JCheckBox( "Regexp" );
	/** Map name filter must be exact match checkbox.     */
	private final JCheckBox   mapNameExactMatchCheckBox     = new JCheckBox( "Exact match" );
	/** Player name filter text field.                    */
	private final JTextField  playerNameTextField           = new JTextField();
	/** Player name filter is regexp checkbox.            */
	private final JCheckBox   playerNameRegexpCheckBox      = new JCheckBox( "Regexp" );
	/** Player name filter must be exact match checkbox.  */
	private final JCheckBox   playerNameExactMatchCheckBox  = new JCheckBox( "Exact match" );
	/** Race filter checkboxes.                           */
	private final JCheckBox[] raceCheckBoxes                = new JCheckBox[ ReplayHeader.RACE_NAMES.length ];
	/** In-game player color filter checkboxes.           */
	private final JCheckBox[] inGameColorCheckBoxes         = new JCheckBox[ Math.min( 16, ReplayHeader.IN_GAME_COLOR_NAMES.length ) ];
	/** Min duration filter text field.                   */
	private final JTextField  durationMinTextField          = new JTextField();
	/** Max duration filter text field.                   */
	private final JTextField  durationMaxTextField          = new JTextField();
	/** Earliest save time filter text field.             */
	private final JTextField  saveDateEarliestTextField     = new JTextField();
	/** Latest save time filter text field.               */
	private final JTextField  saveDateLatestTextField       = new JTextField();
	/** Min version combo box.                            */
	private final JComboBox   versionMinComboBox            = new JComboBox();
	/** Max version combo box.                            */
	private final JComboBox   versionMaxComboBox            = new JComboBox();
	/** Min map size combo box.                           */
	private final JComboBox   mapSizeMinComboBox            = new JComboBox( MapSize.STANDARD_MAP_SIZES );
	/** Max map size combo box.                           */
	private final JComboBox   mapSizeMaxComboBox            = new JComboBox( MapSize.STANDARD_MAP_SIZES );
	/** Game type filter checkboxes.                      */
	private final JCheckBox[] gameTypeCheckBoxes            = new JCheckBox[ ReplayHeader.GAME_TYPE_NAMES.length ];
	
	// TODO: missing fields: game type
	
	/**
	 * Creates a new ReplaySearchTab.
	 */
	public ReplaySearchTab() {
		super( "Replay search" );
		
		versionMinComboBox.addItem( "<any>" );
		versionMaxComboBox.addItem( "<any>" );
		for ( int i = ReplayHeader.VERSION_NAMES.length - 1; i >= 0; i-- ) {
			versionMinComboBox.addItem( ReplayHeader.VERSION_NAMES[ i ] );
			versionMaxComboBox.addItem( ReplayHeader.VERSION_NAMES[ i ] );
		}
		
		buildGUI();
		
		appendResultsToTableCheckBox.doClick(); // To enable/disable dependent buttons (will not change anything, but logically needed; might change in the future).
	}
	
	/**
	 * Builds the GUI of the panel.
	 */
	private void buildGUI() {
		final JPanel filterFieldsButtonsPanel = Utils.createWrapperPanel();
		final JButton resetFilterFieldsButton = new JButton( "Reset fields" );
		resetFilterFieldsButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				for ( final JCheckBox checkBox : gameEngineCheckBoxes )
					checkBox.setSelected( false );
				gameNameTextField.setText( "" );
				gameNameTextField.setBackground( NORMAL_BACKGROUND_COLOR );
				gameNameExactMatchCheckBox.setSelected( false );
				gameNameRegexpCheckBox.setSelected( false );
				gameNameRegexpCheckBox.doClick();
				creatorNameTextField.setText( "" );
				creatorNameTextField.setBackground( NORMAL_BACKGROUND_COLOR );
				creatorNameExactMatchCheckBox.setSelected( false );
				creatorNameRegexpCheckBox.setSelected( false );
				creatorNameRegexpCheckBox.doClick();
				mapNameTextField.setText( "" );
				mapNameTextField.setBackground( NORMAL_BACKGROUND_COLOR );
				mapNameExactMatchCheckBox.setSelected( false );
				mapNameRegexpCheckBox.setSelected( false );
				mapNameRegexpCheckBox.doClick();
				playerNameTextField.setText( "" );
				playerNameTextField.setBackground( NORMAL_BACKGROUND_COLOR );
				playerNameExactMatchCheckBox.setSelected( false );
				playerNameRegexpCheckBox.setSelected( false );
				playerNameRegexpCheckBox.doClick();
				for ( final JCheckBox checkBox : raceCheckBoxes )
					checkBox.setSelected( false );
				for ( final JCheckBox checkBox : inGameColorCheckBoxes )
					checkBox.setSelected( false );
				durationMinTextField.setText( "" );
				durationMinTextField.setBackground( NORMAL_BACKGROUND_COLOR );
				durationMaxTextField.setText( "" );
				durationMaxTextField.setBackground( NORMAL_BACKGROUND_COLOR );
				saveDateEarliestTextField.setText( "" );
				saveDateEarliestTextField.setBackground( NORMAL_BACKGROUND_COLOR );
				saveDateLatestTextField.setText( "" );
				saveDateLatestTextField.setBackground( NORMAL_BACKGROUND_COLOR );
				versionMinComboBox.setSelectedIndex( 0 );
				versionMaxComboBox.setSelectedIndex( 0 );
				mapSizeMinComboBox.setSelectedIndex( 0 );
				mapSizeMaxComboBox.setSelectedIndex( 0 );
				for ( final JCheckBox checkBox : gameTypeCheckBoxes )
					if ( checkBox != null )
						checkBox.setSelected( false );
			}
		} );
		filterFieldsButtonsPanel.add( resetFilterFieldsButton );
		final JButton hideFiltersButton = new JButton( "Hide filters" );
		filterFieldsButtonsPanel.add( hideFiltersButton );
		final JButton visitSearchHelpPageButton = new JButton( "Visit search help page" );
		visitSearchHelpPageButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.showURLInBrowser( Consts.SEARCH_HELP_PAGE_URL );
			}
		} );
		filterFieldsButtonsPanel.add( visitSearchHelpPageButton );
		contentBox.add( filterFieldsButtonsPanel );
		
		final GridBagLayout      gridBagLayout      = new GridBagLayout();
		final GridBagConstraints constraints        = new GridBagConstraints();
		final JPanel             headerFiltersPanel = new JPanel( gridBagLayout );
		headerFiltersPanel.setBorder( BorderFactory.createTitledBorder( "Replay header fields:" ) );
		
		JLabel label;
		JPanel wrapperPanel;
		Box    wrapperBox; 
		
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets( 1, 1, 0, 0 );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Game engines:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		wrapperPanel = Utils.createWrapperPanelLeftAligned();
		for ( int i = 0; i < gameEngineCheckBoxes.length; i++ )
			wrapperPanel.add( gameEngineCheckBoxes[ i ] = new JCheckBox( ReplayHeader.GAME_ENGINE_NAMES[ i ] ) );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		headerFiltersPanel.add( wrapperPanel );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Game name:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( gameNameTextField, constraints );
		headerFiltersPanel.add( gameNameTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		wrapperBox = Box.createHorizontalBox();
		wrapperBox.add( gameNameExactMatchCheckBox );
		wrapperBox.add( gameNameRegexpCheckBox );
		wrapperBox.add( createListAllowedLabelBox( gameNameRegexpCheckBox ) );
		wrapperBox.add( new JPanel( new BorderLayout() ) ); // Consume remaining space
		gridBagLayout.setConstraints( wrapperBox, constraints );
		headerFiltersPanel.add( wrapperBox );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Creator name:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( creatorNameTextField, constraints );
		headerFiltersPanel.add( creatorNameTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		wrapperBox = Box.createHorizontalBox();
		wrapperBox.add( creatorNameExactMatchCheckBox );
		wrapperBox.add( creatorNameRegexpCheckBox );
		wrapperBox.add( createListAllowedLabelBox( creatorNameRegexpCheckBox ) );
		wrapperBox.add( new JPanel( new BorderLayout() ) ); // Consume remaining space
		gridBagLayout.setConstraints( wrapperBox, constraints );
		headerFiltersPanel.add( wrapperBox );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Map name:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( mapNameTextField, constraints );
		headerFiltersPanel.add( mapNameTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		wrapperBox = Box.createHorizontalBox();
		wrapperBox.add( mapNameExactMatchCheckBox );
		wrapperBox.add( mapNameRegexpCheckBox );
		wrapperBox.add( createListAllowedLabelBox( mapNameRegexpCheckBox ) );
		wrapperBox.add( new JPanel( new BorderLayout() ) ); // Consume remaining space
		gridBagLayout.setConstraints( wrapperBox, constraints );
		headerFiltersPanel.add( wrapperBox );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Player name:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( playerNameTextField, constraints );
		headerFiltersPanel.add( playerNameTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		wrapperBox = Box.createHorizontalBox();
		wrapperBox.add( playerNameExactMatchCheckBox );
		wrapperBox.add( playerNameRegexpCheckBox );
		wrapperBox.add( createListAllowedLabelBox( playerNameRegexpCheckBox ) );
		wrapperBox.add( new JPanel( new BorderLayout() ) ); // Consume remaining space
		gridBagLayout.setConstraints( wrapperBox, constraints );
		headerFiltersPanel.add( wrapperBox );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Player race:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		wrapperPanel = Utils.createWrapperPanelLeftAligned();
		for ( int i = 0; i < raceCheckBoxes.length; i++ )
			wrapperPanel.add( raceCheckBoxes[ i ] = new JCheckBox( ReplayHeader.RACE_NAMES[ i ] ) );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		headerFiltersPanel.add( wrapperPanel );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Player color:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		GridBagLayout      gridBagLayout2 = new GridBagLayout();
		GridBagConstraints constraints2   = new GridBagConstraints();
		wrapperPanel = new JPanel( gridBagLayout2 );
		constraints2.fill = GridBagConstraints.BOTH;
		constraints2.gridwidth = 1;
		for ( int i = 0; i < inGameColorCheckBoxes.length; i++ ) {
			constraints2.gridwidth = i == ( inGameColorCheckBoxes.length - 1 ) / 2 || i == inGameColorCheckBoxes.length - 1 ? GridBagConstraints.REMAINDER : 1;
			inGameColorCheckBoxes[ i ] = new JCheckBox( ReplayHeader.IN_GAME_COLOR_NAMES[ i ] );
			gridBagLayout2.setConstraints( inGameColorCheckBoxes[ i ], constraints2 );
			wrapperPanel.add( inGameColorCheckBoxes[ i ] );
		}
		wrapperPanel = Utils.wrapInPanelLeftAligned( wrapperPanel );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		headerFiltersPanel.add( wrapperPanel );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Duration min (sec):" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( durationMinTextField, constraints );
		headerFiltersPanel.add( durationMinTextField );
		label = new JLabel( "Duration max (sec):" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( durationMaxTextField, constraints );
		headerFiltersPanel.add( durationMaxTextField );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Save date earliest:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		saveDateEarliestTextField.setToolTipText( "Format: 2009-03-28" );
		gridBagLayout.setConstraints( saveDateEarliestTextField, constraints );
		headerFiltersPanel.add( saveDateEarliestTextField );
		label = new JLabel( "Save date latest:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		saveDateLatestTextField.setToolTipText( "Format: 2009-03-28" );
		gridBagLayout.setConstraints( saveDateLatestTextField, constraints );
		headerFiltersPanel.add( saveDateLatestTextField );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Version min:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( versionMinComboBox, constraints );
		headerFiltersPanel.add( versionMinComboBox );
		label = new JLabel( "Version max:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( versionMaxComboBox, constraints );
		headerFiltersPanel.add( versionMaxComboBox );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Map size min:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( mapSizeMinComboBox, constraints );
		headerFiltersPanel.add( mapSizeMinComboBox );
		label = new JLabel( "Map size max:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( mapSizeMaxComboBox, constraints );
		headerFiltersPanel.add( mapSizeMaxComboBox );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Game type:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout2 = new GridBagLayout();
		constraints2   = new GridBagConstraints();
		wrapperPanel = new JPanel( gridBagLayout2 );
		constraints2.fill = GridBagConstraints.BOTH;
		constraints2.gridwidth = 1;
		for ( int i = 0; i < gameTypeCheckBoxes.length; i++ ) {
			if ( ReplayHeader.GAME_TYPE_NAMES[ i ] == null )
				continue;
			constraints2.gridwidth = i == ( gameTypeCheckBoxes.length - 1 ) / 2 || i == gameTypeCheckBoxes.length - 1 ? GridBagConstraints.REMAINDER : 1;
			gameTypeCheckBoxes[ i ] = new JCheckBox( ReplayHeader.GAME_TYPE_NAMES[ i ] );
			gridBagLayout2.setConstraints( gameTypeCheckBoxes[ i ], constraints2 );
			wrapperPanel.add( gameTypeCheckBoxes[ i ] );
		}
		wrapperPanel = Utils.wrapInPanelLeftAligned( wrapperPanel );
		gridBagLayout.setConstraints( wrapperPanel, constraints );
		headerFiltersPanel.add( wrapperPanel );
		
		contentBox.add( Utils.wrapInPanel( headerFiltersPanel ) );
		
		hideFiltersButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				headerFiltersPanel.setVisible( !headerFiltersPanel.isVisible() );
				hideFiltersButton.setText( headerFiltersPanel.isVisible() ? "Hide filters" : "Show filters" );
				contentBox.validate();
			}
		} );
		
		final JPanel allButtonsWrapperPanel = Utils.createWrapperPanel();
		appendResultsToTableCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( !stopSearchButton.isEnabled() && !lastSearchResultFileList.isEmpty() )
					searchPreviousResultButton.setEnabled( !appendResultsToTableCheckBox.isSelected() );
			}
		} );
		allButtonsWrapperPanel.add( Utils.wrapInPanel( appendResultsToTableCheckBox ) );
		
		final Box searchStartButtonsBox = Box.createVerticalBox();
		JPanel selectButtonsPanel = Utils.createWrapperPanel();
		final ActionListener selectFilesAndFoldersActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( mainFrame.generalSettingsTab.getReplayStartFolder() );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( Utils.SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( event.getSource() == selectFoldersButton ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES );
				fileChooser.setMultiSelectionEnabled( true );
				// SwingWT does not support selecting multiple directories yet, getSelectedFiles() returns null so I have to call getSelectedFile() in case of folders.
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					searchFilesAndFolders( event.getSource() == selectFoldersButton ? new File[] { fileChooser.getSelectedFile() } : fileChooser.getSelectedFiles() );
			}
		};
		selectFilesButton.setMnemonic( 'f' );
		selectFilesButton.addActionListener( selectFilesAndFoldersActionListener );
		selectButtonsPanel.add( selectFilesButton );
		selectFoldersButton.setMnemonic( 'd' );
		selectFoldersButton.addActionListener( selectFilesAndFoldersActionListener );
		selectButtonsPanel.add( selectFoldersButton );
		stopSearchButton.setEnabled( false );
		stopSearchButton.setMnemonic( 't' );
		stopSearchButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				requestedToStop = true;
				stopSearchButton.setEnabled( false );
			}
		} );
		selectButtonsPanel.add( stopSearchButton );
		searchStartButtonsBox.add( selectButtonsPanel );
		
		selectButtonsPanel = Utils.createWrapperPanel();
		repeatSearchButton.setEnabled( false );
		repeatSearchButton.setMnemonic( 'r' );
		repeatSearchButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent evnet ) {
				searchFilesAndFolders( lastSearchSourceFiles );
			}
		} );
		selectButtonsPanel.add( repeatSearchButton );
		searchPreviousResultButton.setEnabled( false );
		searchPreviousResultButton.setMnemonic( 'p' );
		searchPreviousResultButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent evnet ) {
				searchFilesAndFolders( lastSearchResultFileList.toArray( new File[ lastSearchResultFileList.size() ] ) );
			}
		} );
		selectButtonsPanel.add( searchPreviousResultButton );
		searchStartButtonsBox.add( selectButtonsPanel );
		allButtonsWrapperPanel.add( Utils.wrapInPanel( searchStartButtonsBox ) );
		
		final Box resultListHandlerBox = Box.createVerticalBox();
		saveResultListButton.setEnabled( false );
		saveResultListButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				saveResultList();
			} 
		} );
		resultListHandlerBox.add( saveResultListButton );
		loadResultListButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				loadResultList();
			} 
		} );
		resultListHandlerBox.add( loadResultListButton );
		allButtonsWrapperPanel.add( Utils.wrapInPanel( resultListHandlerBox ) );
		
		contentBox.add( allButtonsWrapperPanel );
		
		progressBar.setMaximumSize( Utils.getMaxDimension() );
		contentBox.add( progressBar );
		
		final JPanel panel = Utils.createWrapperPanelLeftAligned();
		panel.add( new JLabel( "Replays matching the filters:" ) );
		panel.add( resultsCountLabel );
		contentBox.add( panel );
		
		final JPanel resultsPanel = new JPanel( new BorderLayout() );
		showOnChartsButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				mainFrame.chartsTab.setReplayFile( lastSearchResultFileList.get( resultTable.getSelectedRow() ) );
				mainFrame.selectTab( mainFrame.chartsTab );
			}
		} );
		scanForHacksButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				mainFrame.selectTab( mainFrame.manualScanTab );
				mainFrame.manualScanTab.scanFilesAndFolders( getSelectedResultFiles(), false );
			}
		} );
		displayGameChatButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				mainFrame.selectTab( mainFrame.gameChatTab );
				mainFrame.gameChatTab.showGameChatFromReplay( lastSearchResultFileList.get( resultTable.getSelectedRow() ) );
			}
		} );
		extractGameChatButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				mainFrame.selectTab( mainFrame.gameChatTab );
				mainFrame.gameChatTab.extractGameChatFromFiles( getSelectedResultFiles() );
			}
		} );
		removeFromListButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				removeSelectedFromResultList();
			}
		} );
		final ActionListener copyMoveDeleteReplaysActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final File[] selectedFiles = getSelectedResultFiles();
				
				boolean success = true;
				if ( event.getSource() != deleteReplaysButton ) {
					final JFileChooser fileChooser = new JFileChooser( mainFrame.generalSettingsTab.getReplayStartFolder() );
					fileChooser.setTitle( ( event.getSource() == copyReplaysButton ? "Copy " : "Move ") + selectedFiles.length + " replay" + ( selectedFiles.length == 1 ? "" : "s" ) + " to" );
					fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
					if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION ) {
						final File destinationFolder = fileChooser.getSelectedFile();
						for ( final File selectedFile : selectedFiles )
							success &= Utils.copyFile( selectedFile, destinationFolder, selectedFile.getName() );
						
						if ( event.getSource() == moveReplaysButton && success )
							for ( int index : resultTable.getSelectedRows() )
								lastSearchResultFileList.set( index, new File( destinationFolder, lastSearchResultFileList.get( index ).getName() ) );
					}
					else
						return;
				}
				
				if ( event.getSource() == deleteReplaysButton )
					if ( JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog( getContent(), "Are you sure that you want to delete " + selectedFiles.length + " replay" + ( selectedFiles.length == 1 ? "?" : "s?" ), "Warning!", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION ) )
						return;
				
				if ( success && event.getSource() != copyReplaysButton ) { // If copying failed, we don't delete
					for ( final File selectedFile : selectedFiles )
						success &= selectedFile.delete();
				}
				if ( event.getSource() == deleteReplaysButton && success )
					removeSelectedFromResultList();
				JOptionPane.showMessageDialog( getContent(), selectedFiles.length + " replay" + ( selectedFiles.length == 1 ? " " : "s " ) + ( event.getSource() == copyReplaysButton ? "copied" : ( event.getSource() == moveReplaysButton ? "moved" : "deleted" ) ) + ( success ? " successfully." : " with some errors." ), "Done", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE );
			}
		};
		copyReplaysButton  .addActionListener( copyMoveDeleteReplaysActionListener );
		moveReplaysButton  .addActionListener( copyMoveDeleteReplaysActionListener );
		deleteReplaysButton.addActionListener( copyMoveDeleteReplaysActionListener );
		disableResultHandlerButtons();
		resultTable.setPreferredSize( new Dimension( 50, 50 ) );
		resultTable.setRowSelectionAllowed( true );
		resultTable.getSelectionModel().setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		resultTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				final int selectedCount = resultTable.getSelectedRowCount();
				showOnChartsButton   .setEnabled( selectedCount >  0 );
				scanForHacksButton   .setEnabled( selectedCount >  0 );
				displayGameChatButton.setEnabled( selectedCount == 1 );
				extractGameChatButton.setEnabled( selectedCount >  0 );
				removeFromListButton .setEnabled( selectedCount >  0 );
				copyReplaysButton    .setEnabled( selectedCount >  0 );
				moveReplaysButton    .setEnabled( selectedCount >  0 );
				deleteReplaysButton  .setEnabled( selectedCount >  0 );
			}
		} );
		resultTable.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				if ( event.getClickCount() == 2 )
					showOnChartsButton.doClick();
			}
		} );
		resultsPanel.add( new JScrollPane( resultTable ), BorderLayout.CENTER );
		final JPanel resultActionsBox = Box.createVerticalBox();
		resultActionsBox.add( showOnChartsButton    );
		resultActionsBox.add( scanForHacksButton    );
		resultActionsBox.add( displayGameChatButton );
		resultActionsBox.add( extractGameChatButton );
		resultActionsBox.add( removeFromListButton  );
		resultActionsBox.add( copyReplaysButton     );
		resultActionsBox.add( moveReplaysButton     );
		resultActionsBox.add( deleteReplaysButton   );
		resultsPanel.add( Utils.wrapInPanel( resultActionsBox ), BorderLayout.EAST );
		contentBox.add( resultsPanel );
	}
	
	/**
	 * Creates a box with a label saying that list is allowed.
	 * @return the box containing the label
	 */
	private Box createListAllowedLabelBox( final JCheckBox disablerCheckBox ) {
		final String defaultText =  "You may enter a comma separated list";
		
		final Box vbox = Box.createVerticalBox();
		vbox.add( Box.createVerticalGlue() );
		final JLabel label = new JLabel( defaultText );
		label.setForeground( Color.GREEN.darker() );
		vbox.add( label );
		
		disablerCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				label.setText( disablerCheckBox.isSelected() ? "" : defaultText );
			}
		} );
		
		return vbox;
	}
	
	/**
	 * Disables the result handler buttons.
	 */
	private void disableResultHandlerButtons() {
		showOnChartsButton   .setEnabled( false );
		scanForHacksButton   .setEnabled( false );
		displayGameChatButton.setEnabled( false );
		extractGameChatButton.setEnabled( false );
		removeFromListButton .setEnabled( false );
		copyReplaysButton    .setEnabled( false );
		moveReplaysButton    .setEnabled( false );
		deleteReplaysButton  .setEnabled( false );
	}
	
	/**
	 * Refreshes the result table from the <code>lastSearchResultRowsData</code> data list.
	 */
	private void refreshResultTable() {
		resultTable.setModel( new DefaultTableModel( lastSearchResultRowsData.toArray( new String[ lastSearchResultRowsData.size() ][] ), RESULT_TABLE_COLUMN_NAMES ) {
			@Override
			public boolean isCellEditable( final int row, final int column ) {
				return false;
			}
		} );
		mainFrame.chartsTab.onReplayResultListChange( !lastSearchResultRowsData.isEmpty() );
	}
	
	/**
	 * Returns the selected result files.
	 * @return the selected result files
	 */
	private File[] getSelectedResultFiles() {
		final int[]  selectedIndices = resultTable.getSelectedRows();
		final File[] selectedFiles   = new File[ selectedIndices.length ];
		
		for ( int i = 0; i < selectedIndices.length; i++ )
			selectedFiles[ i ] = lastSearchResultFileList.get( selectedIndices[ i ] );
		
		return selectedFiles;
	}
	
	/**
	 * Removes selected lines from result list.
	 */
	private void removeSelectedFromResultList() {
		final int[] selectedIndices = resultTable.getSelectedRows();
		for ( int i = selectedIndices.length - 1; i >= 0; i-- ) { // Downward is a must, indices change when an element is removed!
			lastSearchResultFileList.remove( selectedIndices[ i ] );
			lastSearchResultRowsData.remove( selectedIndices[ i ] );
		}
		refreshResultTable();
		updatedResultsCountLabel();
	}
	
	/**
	 * Updates the results count label.
	 */
	private void updatedResultsCountLabel() {
		resultsCountLabel.setText( lastSearchResultFileList.size() + " out of " + lastSearchReplayFilesCount );
	}
	
	/**
	 * Returns the next replay file based on a current index.<br>
	 * If there are selected replays, then the selection is the source, otherwise the whole result list.
	 * If the index refers to the last, the first is returned.<br>
	 * The new index is set in the indexWrapper.
	 * @param indexWrapper wrapper of the index to calculate next replay from; can be <code>null</code> in which case the first is returned
	 * @return the next file or the source list
	 */
	public File getNextReplayFile( final Integer[] indexWrapper ) {
		final Integer index = indexWrapper[ 0 ];
		
		final int[] selectedIndices = resultTable.getSelectedRows();
		if ( selectedIndices.length > 0 ) {
			if ( index == null )
				return lastSearchResultFileList.get( indexWrapper[ 0 ] = selectedIndices[ 0 ] );
			
			for ( final int selectedIndex : selectedIndices )
				if ( selectedIndex > index )
					return lastSearchResultFileList.get( indexWrapper[ 0 ] = selectedIndex );
			
			return lastSearchResultFileList.get( indexWrapper[ 0 ] = selectedIndices[ 0 ] );
		}
		else {
			if ( index == null )
				return lastSearchResultFileList.get( indexWrapper[ 0 ] = 0 );
			
			return lastSearchResultFileList.get( indexWrapper[ 0 ] = index < lastSearchResultFileList.size() - 1 ? index + 1 : 0 );
		}
	}
	
	/**
	 * Returns the previous replay file based on a current index.<br>
	 * If there are selected replays, then the selection is the source, otherwise the whole result list.
	 * If the index refers to the first, the last is returned.
	 * The new index is set in the indexWrapper.
	 * @param indexWrapper wrapper of the index to calculate next replay from; can be <code>null</code> in which case the last is returned
	 * @return the previous file or the source list
	 */
	public File getPreviousReplayFile( final Integer[] indexWrapper ) {
		final Integer index = indexWrapper[ 0 ];
		
		final int[] selectedIndices = resultTable.getSelectedRows();
		if ( selectedIndices.length > 0 ) {
			if ( index == null )
				return lastSearchResultFileList.get( indexWrapper[ 0 ] = selectedIndices[ selectedIndices.length - 1 ] );
			
			for ( int i = selectedIndices.length - 1; i >= 0; i-- )
				if ( selectedIndices[ i ] < index )
					return lastSearchResultFileList.get( indexWrapper[ 0 ] = selectedIndices[ i ] );
			
			return lastSearchResultFileList.get( indexWrapper[ 0 ] = selectedIndices[ selectedIndices.length - 1 ] );
		}
		else {
			if ( index == null )
				return lastSearchResultFileList.get( indexWrapper[ 0 ] = lastSearchResultFileList.size() - 1 );
			
			return lastSearchResultFileList.get( indexWrapper[ 0 ] = index > 0 ? index - 1 : lastSearchResultFileList.size() - 1 );
		}
	}
	
	/**
	 * Searches the specified files and folders.
	 * @param files files and folders to be searched
	 */
	private void searchFilesAndFolders( final File[] files ) {
		requestedToStop = false;
		selectFoldersButton         .setEnabled( false );
		selectFilesButton           .setEnabled( false );
		repeatSearchButton          .setEnabled( false );
		searchPreviousResultButton  .setEnabled( false );
		stopSearchButton            .setEnabled( true  );
		saveResultListButton        .setEnabled( false );
		loadResultListButton        .setEnabled( false );
		appendResultsToTableCheckBox.setEnabled( false );
		
		disableResultHandlerButtons();
		
		lastSearchSourceFiles = files;
		if ( !appendResultsToTableCheckBox.isSelected() ) {
			lastSearchResultFileList.clear();
			lastSearchResultRowsData.clear();
		}
		
		final ReplayFilter[] replayFilters = getReplayFilters();
		
		new NormalThread() {
			/** List of replay files to be scanned. */
			final List< File > replayFileList = new ArrayList< File >();
			
			@Override
			public void run() {
				try {
					resultsCountLabel.setText( "Counting..." );
					
					progressBar.setValue( 0 );
					
					chooseReplayFiles( files );
					lastSearchReplayFilesCount = replayFileList.size();
					progressBar.setMaximum( lastSearchReplayFilesCount );
					resultsCountLabel.setText( "Searching " + lastSearchReplayFilesCount + " replay file" + ( lastSearchReplayFilesCount == 1 ? "" : "s" ) + "..." );
					
					int counter = 0;	
					for ( final File replayFile : replayFileList ) {
						if ( requestedToStop )
							return;
						
						final Replay replay = BinRepParser.parseReplay( replayFile, false, false );
						if ( replay != null ) {
							boolean replayIncluded = true;
							for ( final ReplayFilter replayFilter : replayFilters )
								if ( !replayFilter.isReplayIncluded( replay ) ) {
									replayIncluded = false;
									break;
								}
							
							if ( replayIncluded ) {
								lastSearchResultFileList.add( replayFile );
								final ReplayHeader replayHeader = replay.replayHeader;
								lastSearchResultRowsData.add( new String[] {
									ReplayHeader.GAME_ENGINE_SHORT_NAMES[ replayHeader.gameEngine ] + " " + replayHeader.guessVersionFromDate(),
									replayHeader.mapName, replayHeader.getDurationString(), ReplayHeader.GAME_TYPE_NAMES[ replayHeader.gameType ], replayHeader.getPlayerNamesString(), SIMPLE_DATE_FORMAT.format( replayHeader.saveTime ),
									replayHeader.gameName, replayHeader.creatorName, replayFile.getAbsolutePath().toString()
								} );
							}
						}
						
						progressBar.setValue( ++counter );
					}
					
				}
				finally {
					updatedResultsCountLabel();
					refreshResultTable();
					saveResultListButton        .setEnabled( true );
					appendResultsToTableCheckBox.setEnabled( true );
					loadResultListButton        .setEnabled( true  );
					saveResultListButton        .setEnabled( true  );
					stopSearchButton            .setEnabled( false );
					selectFilesButton           .setEnabled( true  );
					selectFoldersButton         .setEnabled( true  );
					repeatSearchButton          .setEnabled( true  );
					searchPreviousResultButton  .setEnabled( !appendResultsToTableCheckBox.isSelected() );
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
	 * Construct and returns an array of {@link ReplayFilter} from the specified filter fields
	 * from the UI components.<br>
	 * The returned array is sorted by replay filter complexity.
	 * @return an array of {@link ReplayFilter} from the specified filter fields
	 */
	private ReplayFilter[] getReplayFilters() {
		final List< ReplayFilter > replayFilterList = new ArrayList< ReplayFilter >();
		
		final List< Byte > selectedByteValueList = new ArrayList< Byte >();
		for ( int i = gameEngineCheckBoxes.length - 1; i >= 0; i-- )
			if ( gameEngineCheckBoxes[ i ].isSelected() )
				selectedByteValueList.add( (byte) i );
		if ( !selectedByteValueList.isEmpty() )
			replayFilterList.add( new GameEngineReplayFilter( selectedByteValueList ) );
		
		String stringValue = gameNameTextField.getText();
		gameNameTextField.setBackground( NORMAL_BACKGROUND_COLOR );
		if ( stringValue.length() > 0 )
			try {
				replayFilterList.add( new GameNameReplayFilter( stringValue, gameNameExactMatchCheckBox.isSelected(), gameNameRegexpCheckBox.isSelected() ) );
			}
			catch ( final Exception e ) {
				gameNameTextField.setBackground( SYNTAX_ERROR_BACKGROUND_COLOR );
			}
		
		stringValue = creatorNameTextField.getText();
		creatorNameTextField.setBackground( NORMAL_BACKGROUND_COLOR );
		if ( stringValue.length() > 0 )
			try {
				replayFilterList.add( new CreatorNameReplayFilter( stringValue, creatorNameExactMatchCheckBox.isSelected(), creatorNameRegexpCheckBox.isSelected() ) );
			}
			catch ( final Exception e ) {
				creatorNameTextField.setBackground( SYNTAX_ERROR_BACKGROUND_COLOR );
			}
		
		stringValue = mapNameTextField.getText();
		mapNameTextField.setBackground( NORMAL_BACKGROUND_COLOR );
		if ( stringValue.length() > 0 )
			try {
				replayFilterList.add( new MapNameReplayFilter( stringValue, mapNameExactMatchCheckBox.isSelected(), mapNameRegexpCheckBox.isSelected() ) );
			}
			catch ( final Exception e ) {
				mapNameTextField.setBackground( SYNTAX_ERROR_BACKGROUND_COLOR );
			}
		
		stringValue = playerNameTextField.getText();
		playerNameTextField.setBackground( NORMAL_BACKGROUND_COLOR );
		if ( stringValue.length() > 0 )
			try {
				replayFilterList.add( new PlayerNameReplayFilter( stringValue, playerNameExactMatchCheckBox.isSelected(), playerNameRegexpCheckBox.isSelected() ) );
			}
			catch ( final Exception e ) {
				playerNameTextField.setBackground( SYNTAX_ERROR_BACKGROUND_COLOR );
			}
		
		selectedByteValueList.clear();
		for ( int i = raceCheckBoxes.length - 1; i >= 0; i-- )
			if ( raceCheckBoxes[ i ].isSelected() )
				selectedByteValueList.add( (byte) i );
		if ( !selectedByteValueList.isEmpty() )
			replayFilterList.add( new PlayerRaceReplayFilter( selectedByteValueList ) );
		
		final List< Integer > selectedIntegerValueList = new ArrayList< Integer >();
		for ( int i = inGameColorCheckBoxes.length - 1; i >= 0; i-- )
			if ( inGameColorCheckBoxes[ i ].isSelected() )
				selectedIntegerValueList.add( i );
		if ( !selectedIntegerValueList.isEmpty() )
			replayFilterList.add( new PlayerColorReplayFilter( selectedIntegerValueList ) );
		
		Integer minDuration = null;
		Integer maxDuration = null;
		durationMinTextField.setBackground( NORMAL_BACKGROUND_COLOR );
		durationMaxTextField.setBackground( NORMAL_BACKGROUND_COLOR );
		try {
			if ( durationMinTextField.getText().length() > 0 )
				minDuration = Integer.parseInt( durationMinTextField.getText() );
		}
		catch ( final Exception e ) {
			durationMinTextField.setBackground( SYNTAX_ERROR_BACKGROUND_COLOR );
		}
		try {
			if ( durationMaxTextField.getText().length() > 0 )
				maxDuration = Integer.parseInt( durationMaxTextField.getText() );
		}
		catch ( final Exception e ) {
			durationMaxTextField.setBackground( SYNTAX_ERROR_BACKGROUND_COLOR );
		}
		if ( minDuration != null || maxDuration != null )
			replayFilterList.add( new DurationReplayFilter( minDuration, maxDuration ) );
		
		Long minSaveDate = null;
		Long maxSaveDate = null;
		saveDateEarliestTextField.setBackground( NORMAL_BACKGROUND_COLOR );
		saveDateLatestTextField.setBackground( NORMAL_BACKGROUND_COLOR );
		try {
			if ( saveDateEarliestTextField.getText().length() > 0 )
				minSaveDate = SIMPLE_DATE_FORMAT.parse( saveDateEarliestTextField.getText() ).getTime();
		}
		catch ( final Exception e ) {
			saveDateEarliestTextField.setBackground( SYNTAX_ERROR_BACKGROUND_COLOR );
		}
		try {
			if ( saveDateLatestTextField.getText().length() > 0 )
				SIMPLE_DATE_FORMAT.parse( saveDateLatestTextField.getText() );
		}
		catch ( final Exception e ) {
			saveDateLatestTextField.setBackground( SYNTAX_ERROR_BACKGROUND_COLOR );
		}
		if ( minSaveDate != null || maxSaveDate != null )
			replayFilterList.add( new SaveTimeReplayFilter( minSaveDate, maxSaveDate ) );
		
		minSaveDate = versionMinComboBox.getSelectedIndex() > 0 ? ReplayHeader.VERSION_RELEASE_DATES[ ReplayHeader.VERSION_NAMES.length - versionMinComboBox.getSelectedIndex() ] : null;
		maxSaveDate = versionMaxComboBox.getSelectedIndex() > 0 ? versionMaxComboBox.getSelectedIndex() == 1 ? Long.MAX_VALUE : ReplayHeader.VERSION_RELEASE_DATES[ 1 + ReplayHeader.VERSION_NAMES.length - versionMaxComboBox.getSelectedIndex() ] : null;
		if ( minSaveDate != null || maxSaveDate != null )
			replayFilterList.add( new SaveTimeReplayFilter( minSaveDate, maxSaveDate ) );
		
		MapSize minMapSize = (MapSize) mapSizeMinComboBox.getSelectedItem();
		MapSize maxMapSize = (MapSize) mapSizeMaxComboBox.getSelectedItem();
		if ( minMapSize == MapSize.MAP_SIZE_ANY )
			minMapSize = null;
		if ( maxMapSize == MapSize.MAP_SIZE_ANY )
			maxMapSize = null;
		if ( minMapSize != null || maxMapSize != null )
			replayFilterList.add( new MapSizeReplayFilter( minMapSize, maxMapSize ) );
		
		final List< Short > selectedShortValueList = new ArrayList< Short >();
		for ( int i = gameTypeCheckBoxes.length - 1; i >= 0; i-- )
			if ( gameTypeCheckBoxes[ i ] != null && gameTypeCheckBoxes[ i ].isSelected() )
				selectedShortValueList.add( (short) i );
		if ( !selectedShortValueList.isEmpty() )
			replayFilterList.add( new GameTypeReplayFilter( selectedShortValueList ) );
		
		final ReplayFilter[] replayFilters = replayFilterList.toArray( new ReplayFilter[ replayFilterList.size() ] );
		Arrays.sort( replayFilters );
		
		return replayFilters;
	}
	
	/**
	 * Saves the result list.<br>
	 * First prompts the file to save to.
	 */
	private void saveResultList() {
		final JFileChooser fileChooser = new JFileChooser( mainFrame.generalSettingsTab.defaultReplayListsFolderTextField.getText() );
		fileChooser.setTitle( "Save result list to..." );
		
		fileChooser.addChoosableFileFilter( Utils.SWING_TEXT_FILE_FILTER ); 
		fileChooser.setExtensionFilters( new String[] { "*.txt", "*.*" }, new String[] { "Text files (*.txt)", "All files (*.*)" } );
		
		fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
		if ( fileChooser.showSaveDialog( getContent() ) == JFileChooser.APPROVE_OPTION ) {
			PrintWriter output = null;
			try {
				output = new PrintWriter( fileChooser.getSelectedFile() );
				
				for ( final String resultTableColumnName : RESULT_TABLE_COLUMN_NAMES ) {
					 // Column name cannot be empty string!
					output.print( resultTableColumnName.replace( RESULT_LIST_FILE_VALUE_SEPARATOR_CHAR, ' ' ) );
					output.print( RESULT_LIST_FILE_VALUE_SEPARATOR_CHAR );
				}
				output.println();
				
				for ( final String[] searchResultRowData : lastSearchResultRowsData ) {
					for ( final String searchResultCell : searchResultRowData ) {
						if ( searchResultCell != null )
							output.print( searchResultCell.replace( RESULT_LIST_FILE_VALUE_SEPARATOR_CHAR, ' ' ) );
						output.print( RESULT_LIST_FILE_VALUE_SEPARATOR_CHAR );
					}
					output.println();
				}
				
				output.flush();
				JOptionPane.showMessageDialog( getContent(), "Result list saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE );
			}
			catch ( final Exception e ) {
				JOptionPane.showMessageDialog( getContent(), "Could not save result list!", "Error!", JOptionPane.ERROR_MESSAGE );
			}
			finally {
				if ( output != null )
					output.close();
			}
		}
	}
	
	/**
	 * Loads a result list from a file.
	 */
	private void loadResultList() {
		final JFileChooser fileChooser = new JFileChooser( mainFrame.generalSettingsTab.defaultReplayListsFolderTextField.getText() );
		fileChooser.setTitle( "Load result list..." );
		
		fileChooser.addChoosableFileFilter( Utils.SWING_TEXT_FILE_FILTER ); 
		fileChooser.setExtensionFilters( new String[] { "*.txt", "*.*" }, new String[] { "Text files (*.txt)", "All files (*.*)" } );
		
		fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
		if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION ) {
			BufferedReader input = null;
			try {
				input = new BufferedReader( new FileReader( fileChooser.getSelectedFile() ) );
				
				String line = input.readLine();
				if ( line == null )
					throw new Exception();
				
				StringTokenizer lineTokenizer = new StringTokenizer( line, RESULT_LIST_FILE_VALUE_SEPARATOR_STRING ); // Column name cannot be empty string!
				final int[] columnIndices = new int[ lineTokenizer.countTokens() ];
				int fileColumnIndexForFileName = -1;
				
				int fileColumnIndex = 0;
				while ( lineTokenizer.hasMoreTokens() ) {
					final String columnName = lineTokenizer.nextToken();
					columnIndices[ fileColumnIndex ] = -1; // We assume we don't have a column for the stored value as long as we don't find one
					
					for ( int i = RESULT_TABLE_COLUMN_NAMES.length - 1; i >= 0; i-- )
						if ( columnName.equals( RESULT_TABLE_COLUMN_NAMES[ i ] ) ) {
							columnIndices[ fileColumnIndex ] = i;
							if ( i == RESULT_TABLE_FILE_NAME_COLUMN_INDEX )
								fileColumnIndexForFileName = i;
							break;
						}
					
					fileColumnIndex++;
				}
				
				lastSearchResultFileList.clear();
				lastSearchResultRowsData.clear();
				while ( ( line = input.readLine() ) != null ) {
					lineTokenizer = new StringTokenizer( line, RESULT_LIST_FILE_VALUE_SEPARATOR_STRING, true );
					
					final String[] searchResultRowData = new String[ RESULT_TABLE_COLUMN_NAMES.length ];
					fileColumnIndex = 0;
					
					while ( lineTokenizer.hasMoreTokens() ) {
						final String token = lineTokenizer.nextToken();
						
						if ( !token.equals( RESULT_LIST_FILE_VALUE_SEPARATOR_STRING ) ) {
							searchResultRowData[ columnIndices[ fileColumnIndex ] ] = token;
							if ( fileColumnIndex == fileColumnIndexForFileName ) {
								// We found file name, we can add it to the results table now.
								lastSearchResultFileList.add( new File( token ) );
								lastSearchResultRowsData.add( searchResultRowData );
							}
						}
						else
							fileColumnIndex++;
					}
				}
				
				// We enable searching in previous results now.
				searchPreviousResultButton.setEnabled( true  );
				
				updatedResultsCountLabel();
				refreshResultTable();
			}
			catch ( final Exception e ) {
				JOptionPane.showMessageDialog( getContent(), "Could not load result list!", "Error!", JOptionPane.ERROR_MESSAGE );
			}
			finally {
				if ( input != null )
					try { input.close(); } catch ( final IOException ie ) { ie.printStackTrace(); }
			}
		}
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_APPEND_RESULTS_TO_TABLE, Boolean.toString( appendResultsToTableCheckBox.isSelected() ) );
	}
	
}
