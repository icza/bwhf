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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import swingwt.awt.BorderLayout;
import swingwt.awt.Color;
import swingwt.awt.Dimension;
import swingwt.awt.Font;
import swingwt.awt.GridBagConstraints;
import swingwt.awt.GridBagLayout;
import swingwt.awt.GridLayout;
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
import swingwtx.swing.JDialog;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JMenuItem;
import swingwtx.swing.JOptionPane;
import swingwtx.swing.JPanel;
import swingwtx.swing.JPopupMenu;
import swingwtx.swing.JProgressBar;
import swingwtx.swing.JScrollPane;
import swingwtx.swing.JTable;
import swingwtx.swing.JTextArea;
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
	
	/** Button to hide/show filters.               */
	private final JButton hideFiltersButton          = new JButton( "Hide filters", IconResourceManager.ICON_ARROW_IN );
	/** Reference to the header filters panel.     */
	private JPanel headerFiltersPanel; 
	
	/** Checkbox to add new search results to previous one (do not clear). */
	private final JCheckBox appendResultsToTableCheckBox = new JCheckBox( "Append results to table (will not clear table)", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_APPEND_RESULTS_TO_TABLE ) ) );
	
	/** Button to select folders to search.        */
	private final JButton selectFoldersButton        = new JButton( "Select folders to search recursively...", IconResourceManager.ICON_FOLDER_CHOOSER );
	/** Button to select files to search.          */
	private final JButton selectFilesButton          = new JButton( "Select files to search...", IconResourceManager.ICON_FILE_CHOOSER );
	/** Button to stop the current search.         */
	private final JButton stopSearchButton           = new JButton( "Stop current search", IconResourceManager.ICON_STOP );
	/** Button to repeat search on the same files. */
	private final JButton repeatSearchButton         = new JButton( "Repeat search", IconResourceManager.ICON_REPEAT );
	/** Button to run search on the same files.    */
	private final JButton searchPreviousResultButton = new JButton( "Search in previous results (narrows previous results)", IconResourceManager.ICON_TABLE_GO );
	
	/** Button to save result list.                */
	private final JButton saveResultListButton       = new JButton( "Save result list...", IconResourceManager.ICON_TABLE_SAVE );
	/** Button to save result list.                */
	private final JButton loadResultListButton       = new JButton( "Load result list...", IconResourceManager.ICON_TABLE_LOAD );
	
	/** The progress bar component. */
	private final JProgressBar progressBar = new JProgressBar();
	
	/** Table displaying the results. */
	private final JTable resultTable = new JTable();
	
	/** Label to display the results count. */
	private final JLabel resultsCountLabel = new JLabel();
	
	/** The replay operations popup menu.                          */
	private JPopupMenu replayOperationsPopupMenu;
	/** Menu item to show selected replay on charts.               */
	private final JMenuItem showOnChartsMenuItem       = new JMenuItem( "Show on charts", IconResourceManager.ICON_CHARTS );
	/** Menu item to scan selected replays for hacks.              */
	private final JMenuItem scanForHacksMenuItem       = new JMenuItem( "Scan for hacks", IconResourceManager.ICON_MANUAL_SCAN );
	/** Menu item to display game chat from selected replay.       */
	private final JMenuItem displayGameChatMenuItem    = new JMenuItem( "Display game chat", IconResourceManager.ICON_GAME_CHAT );
	/** Menu item to extract game chat from selected replays.      */
	private final JMenuItem extractGameChatMenuItem    = new JMenuItem( "Extract game chat", IconResourceManager.ICON_GAME_CHATS );
	/** Menu item to remove selected replays from the result list. */
	private final JMenuItem removeFromListMenuItem     = new JMenuItem( "Remove from list", IconResourceManager.ICON_REMOVE_FROM_LIST );
	/** Menu item to copy selected replay files.                   */
	private final JMenuItem copyReplaysMenuItem        = new JMenuItem( "Copy replays...", IconResourceManager.ICON_COPY_REPLAY );
	/** Menu item to move selected replay files.                   */
	private final JMenuItem moveReplaysMenuItem        = new JMenuItem( "Move replays..." );
	/** Menu item to delete selected replay files.                 */
	private final JMenuItem deleteReplaysMenuItem      = new JMenuItem( "Delete replays...", IconResourceManager.ICON_DELETE_REPLAY );
	/** Menu item to rename replay.                                */
	private final JMenuItem renameReplayMenuItem       = new JMenuItem( "Rename replay...", IconResourceManager.ICON_EDIT );
	/** Menu item to rename replay.                                */
	private final JMenuItem groupRenameReplaysMenuItem = new JMenuItem( "Group rename replays...", IconResourceManager.ICON_GROUP_RENAME_REPLAY );
	/** Menu item to open replay's folder in explorer.             */
	private final JMenuItem openInExplorerMenuItem     = new JMenuItem( "Open replay's folder in explorer", IconResourceManager.ICON_FOLDER );
	
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
	
	// SwingWT does not implement column rearrange and sorting, so here it comes:
	/** Tells the model indices of the columns. */
	private final int[] columnModelIndices    = new int[ RESULT_TABLE_COLUMN_NAMES.length ];
	private int         lastSortingModelIndex = -1;
	private boolean     lastSortingAscendant;
	
	/**
	 * Ascendant comparator for replay data.
	 * @author Andras Belicza
	 */
	private static class AscendantReplayDataComparator implements Comparator< String[] > {
		private final int sortingModelIndex;
		public AscendantReplayDataComparator( final int sortingModelIndex ) {
			this.sortingModelIndex = sortingModelIndex;
		}
		public int compare( final String[] replayData1, final String[] replayData2 ) {
			if ( replayData1[ sortingModelIndex ] == null )
				return 1;
			if ( replayData2[ sortingModelIndex ] == null )
				return -1;
			return replayData1[ sortingModelIndex ].compareTo( replayData2[ sortingModelIndex ] );
		}
	};
	
	/**
	 * Descendant comparator for replay data.
	 * @author Andras Belicza
	 */
	private static class DescendantReplayDataComparator implements Comparator< String[] > {
		private final int sortingModelIndex;
		public DescendantReplayDataComparator( final int sortingModelIndex ) {
			this.sortingModelIndex = sortingModelIndex;
		}
		public int compare( final String[] replayData1, final String[] replayData2 ) {
			if ( replayData1[ sortingModelIndex ] == null )
				return -1;
			if ( replayData2[ sortingModelIndex ] == null )
				return 1;
			return -replayData1[ sortingModelIndex ].compareTo( replayData2[ sortingModelIndex ] );
		}
	};
	
	/**
	 * Creates a new ReplaySearchTab.
	 */
	public ReplaySearchTab() {
		super( "Replay search", IconResourceManager.ICON_REPLAY_SEARCH );
		
		versionMinComboBox.addItem( "<any>" );
		versionMaxComboBox.addItem( "<any>" );
		for ( int i = ReplayHeader.VERSION_NAMES.length - 1; i >= 0; i-- ) {
			versionMinComboBox.addItem( ReplayHeader.VERSION_NAMES[ i ] );
			versionMaxComboBox.addItem( ReplayHeader.VERSION_NAMES[ i ] );
		}
		
		final StringTokenizer columnModelIndicesTokenizer = new StringTokenizer( Utils.settingsProperties.getProperty( Consts.PROPERTY_REPLAY_COLUMN_MODEL_INDICES ), "," );
		try {
			for ( int i = 0; i < columnModelIndices.length; i++ )
				columnModelIndices[ i ] = Integer.parseInt( columnModelIndicesTokenizer.nextToken() );
		}
		catch ( final Exception e ) {
			for ( int i = 0; i < columnModelIndices.length; i++ )
				columnModelIndices[ i ] = i;
		}
		
		buildGUI();
		
		appendResultsToTableCheckBox.doClick(); // To enable/disable dependant buttons (will not change anything, but logically needed; might change in the future).
		
		if ( Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_HIDE_SEARCH_FILTERS ) ) )
			hideFiltersButton.doClick();
		
		// Replay list to be loaded on startup will be loaded in the initializationEnded() method,
		// because it loads a lot faster if window is visible!
	}
	
	/**
	 * Builds the GUI of the panel.
	 */
	private void buildGUI() {
		final JPanel filterFieldsButtonsPanel = Utils.createWrapperPanel();
		final JButton resetFilterFieldsButton = new JButton( "Reset fields", IconResourceManager.ICON_UNDO );
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
		filterFieldsButtonsPanel.add( hideFiltersButton );
		final JButton visitSearchHelpPageButton = new JButton( "Visit search help page", IconResourceManager.ICON_WORLD_GO );
		visitSearchHelpPageButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.showURLInBrowser( Consts.SEARCH_HELP_PAGE_URL );
			}
		} );
		filterFieldsButtonsPanel.add( visitSearchHelpPageButton );
		contentBox.add( filterFieldsButtonsPanel );
		
		final GridBagLayout      gridBagLayout = new GridBagLayout();
		final GridBagConstraints constraints   = new GridBagConstraints();
		headerFiltersPanel = new JPanel( gridBagLayout );
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
				hideFiltersButton.setIcon( headerFiltersPanel.isVisible() ? IconResourceManager.ICON_ARROW_IN : IconResourceManager.ICON_ARROW_OUT );
				contentBox.validate();
			}
		} );
		
		final JPanel allButtonsWrapperPanel = Utils.createWrapperPanel();
		wrapperBox = Box.createVerticalBox();
		appendResultsToTableCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( !stopSearchButton.isEnabled() && !lastSearchResultFileList.isEmpty() )
					searchPreviousResultButton.setEnabled( !appendResultsToTableCheckBox.isSelected() );
			}
		} );
		wrapperBox.add( appendResultsToTableCheckBox );
		final JButton columnSetupButton = new JButton( "Column setup...", IconResourceManager.ICON_COLUMNS );
		columnSetupButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				showColumnSetupDialog();
			}
		} );
		wrapperBox.add( Utils.wrapInPanel( columnSetupButton ) );
		allButtonsWrapperPanel.add( wrapperBox );
		
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
				loadResultList( null );
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
		
		showOnChartsMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				mainFrame.chartsTab.setReplayFile( lastSearchResultFileList.get( resultTable.getSelectedRow() ), resultTable.getSelectedRow() );
				mainFrame.selectTab( mainFrame.chartsTab );
			}
		} );
		scanForHacksMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				mainFrame.selectTab( mainFrame.manualScanTab );
				mainFrame.manualScanTab.scanFilesAndFolders( getSelectedResultFiles(), false );
			}
		} );
		displayGameChatMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				mainFrame.selectTab( mainFrame.gameChatTab );
				mainFrame.gameChatTab.showGameChatFromReplay( lastSearchResultFileList.get( resultTable.getSelectedRow() ) );
			}
		} );
		extractGameChatMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				mainFrame.selectTab( mainFrame.gameChatTab );
				mainFrame.gameChatTab.extractGameChatFromFiles( getSelectedResultFiles() );
			}
		} );
		removeFromListMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				removeSelectedFromResultList();
			}
		} );
		final ActionListener copyMoveDeleteReplaysActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final File[] selectedFiles = getSelectedResultFiles();
				
				boolean success = true;
				if ( event.getSource() != deleteReplaysMenuItem ) {
					final JFileChooser fileChooser = new JFileChooser( mainFrame.generalSettingsTab.getReplayStartFolder() );
					fileChooser.setTitle( ( event.getSource() == copyReplaysMenuItem ? "Copy " : "Move ") + selectedFiles.length + " replay" + ( selectedFiles.length == 1 ? "" : "s" ) + " to" );
					fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
					if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION ) {
						final File destinationFolder = fileChooser.getSelectedFile();
						for ( final File selectedFile : selectedFiles )
							success &= Utils.copyFile( selectedFile, destinationFolder, selectedFile.getName() );
						
						if ( event.getSource() == moveReplaysMenuItem && success )
							for ( int index : resultTable.getSelectedRows() )
								lastSearchResultFileList.set( index, new File( destinationFolder, lastSearchResultFileList.get( index ).getName() ) );
					}
					else
						return;
				}
				
				if ( event.getSource() == deleteReplaysMenuItem )
					if ( JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog( getContent(), "Are you sure that you want to delete " + selectedFiles.length + " replay" + ( selectedFiles.length == 1 ? "?" : "s?" ), "Warning!", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION ) )
						return;
				
				if ( success && event.getSource() != copyReplaysMenuItem ) { // If copying failed, we don't delete
					for ( final File selectedFile : selectedFiles )
						success &= selectedFile.delete();
				}
				if ( event.getSource() == deleteReplaysMenuItem && success )
					removeSelectedFromResultList();
				JOptionPane.showMessageDialog( getContent(), selectedFiles.length + " replay" + ( selectedFiles.length == 1 ? " " : "s " ) + ( event.getSource() == copyReplaysMenuItem ? "copied" : ( event.getSource() == moveReplaysMenuItem ? "moved" : "deleted" ) ) + ( success ? " successfully." : " with some errors." ), "Done", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE );
			}
		};
		copyReplaysMenuItem  .addActionListener( copyMoveDeleteReplaysActionListener );
		moveReplaysMenuItem  .addActionListener( copyMoveDeleteReplaysActionListener );
		deleteReplaysMenuItem.addActionListener( copyMoveDeleteReplaysActionListener );
		renameReplayMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final File selectedFile = lastSearchResultFileList.get( resultTable.getSelectedRow() );
				final Object newName = JOptionPane.showInputDialog( getContent(), "Enter the new name of the replay file:", "Renaming replay...", JOptionPane.QUESTION_MESSAGE, null, null, selectedFile.getName() );
				if ( newName != null ) {
					final String newNameString = (String) newName;
					if ( selectedFile.getName().equals( newName ) )
						JOptionPane.showMessageDialog( getContent(), "You provided the same name!", "Error", JOptionPane.ERROR_MESSAGE );
					else if ( newNameString.indexOf( '/' ) >= 0 || newNameString.indexOf( '\\' ) >= 0 || newNameString.indexOf( '\n' ) >= 0 )
						JOptionPane.showMessageDialog( getContent(), "The provided name contains invalid characters!", "Error", JOptionPane.ERROR_MESSAGE );
					else {
						final File newFile = new File( selectedFile.getParent(), newNameString );
						if ( newFile.exists() ) 
							JOptionPane.showMessageDialog( getContent(), "The provided name already exists!", "Error", JOptionPane.ERROR_MESSAGE );
						else {
							if ( !selectedFile.renameTo( newFile ) )
								JOptionPane.showMessageDialog( getContent(), "Failed to rename file!", "Error", JOptionPane.ERROR_MESSAGE );
							else {
								lastSearchResultFileList.set( resultTable.getSelectedRow(), newFile );
								lastSearchResultRowsData.get( resultTable.getSelectedRow() )[ RESULT_TABLE_FILE_NAME_COLUMN_INDEX ] = newFile.getAbsolutePath();
								refreshResultTable();
							}
						}
					}
				}
			}
		} );
		groupRenameReplaysMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				showGroupRenameDialog();
			}
		} );
		openInExplorerMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					Runtime.getRuntime().exec( new String[] { "explorer", lastSearchResultFileList.get( resultTable.getSelectedRow() ).getParent() } );
				} catch ( final Exception e ) {
				}
			}
		} );
		disableReplayOperationMenuItems();
		resultTable.setPreferredSize( new Dimension( 50, 50 ) );
		resultTable.setRowSelectionAllowed( true );
		resultTable.getSelectionModel().setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		resultTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				final int selectedCount = resultTable.getSelectedRowCount();
				showOnChartsMenuItem      .setEnabled( selectedCount >  0 );
				scanForHacksMenuItem      .setEnabled( selectedCount >  0 );
				displayGameChatMenuItem   .setEnabled( selectedCount == 1 );
				extractGameChatMenuItem   .setEnabled( selectedCount >  0 );
				removeFromListMenuItem    .setEnabled( selectedCount >  0 );
				copyReplaysMenuItem       .setEnabled( selectedCount >  0 );
				moveReplaysMenuItem       .setEnabled( selectedCount >  0 );
				deleteReplaysMenuItem     .setEnabled( selectedCount >  0 );
				renameReplayMenuItem      .setEnabled( selectedCount == 1 );
				groupRenameReplaysMenuItem.setEnabled( selectedCount >  0 );
				openInExplorerMenuItem    .setEnabled( selectedCount == 1 );
			}
		} );
		rebuildReplayOperationsPopupMenu();
		resultTable.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				if ( event.getButton() == MouseEvent.BUTTON3 )
					replayOperationsPopupMenu.show( resultTable, event.getX(), event.getY() );
				if ( event.getButton() == MouseEvent.BUTTON1 )
					if ( resultTable.getSelectedRow() >= 0 && event.getClickCount() == 2 )
						showOnChartsMenuItem.doClick();
			}
		} );
		resultTable.getTableHeader().addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked( final MouseEvent event ) {
				final int     sortingModelIndex = columnModelIndices[ resultTable.getTableHeader().columnAtPoint( event.getPoint() ) ];
				final boolean sortingAscendant  = sortingModelIndex == lastSortingModelIndex ? !lastSortingAscendant : true;
				
				Collections.sort( lastSearchResultRowsData, sortingAscendant ? new AscendantReplayDataComparator( sortingModelIndex ) : new DescendantReplayDataComparator( sortingModelIndex ) );
				for ( int i = lastSearchResultRowsData.size() - 1; i >= 0; i-- )
					lastSearchResultFileList.set( i, new File( lastSearchResultRowsData.get( i )[ RESULT_TABLE_FILE_NAME_COLUMN_INDEX ] ) );
				
				lastSortingModelIndex = sortingModelIndex;
				lastSortingAscendant  = sortingAscendant;
				
				refreshResultTable();
			}
		} );
		contentBox.add( new JScrollPane( resultTable ) );
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
	 * Rebuilds the replay operations popup menu.
	 */
	private void rebuildReplayOperationsPopupMenu() {
		replayOperationsPopupMenu = new JPopupMenu();
		replayOperationsPopupMenu.add( showOnChartsMenuItem       );
		replayOperationsPopupMenu.add( scanForHacksMenuItem       );
		replayOperationsPopupMenu.add( displayGameChatMenuItem    );
		replayOperationsPopupMenu.add( extractGameChatMenuItem    );
		replayOperationsPopupMenu.add( removeFromListMenuItem     );
		replayOperationsPopupMenu.addSeparator();
		replayOperationsPopupMenu.add( copyReplaysMenuItem        );
		replayOperationsPopupMenu.add( moveReplaysMenuItem        );
		replayOperationsPopupMenu.add( deleteReplaysMenuItem      );
		replayOperationsPopupMenu.addSeparator();
		replayOperationsPopupMenu.add( renameReplayMenuItem       );
		replayOperationsPopupMenu.add( groupRenameReplaysMenuItem );
		replayOperationsPopupMenu.addSeparator();
		replayOperationsPopupMenu.add( openInExplorerMenuItem     );
	}
	
	/**
	 * Disables the replay operation menu items.
	 */
	private void disableReplayOperationMenuItems() {
		showOnChartsMenuItem      .setEnabled( false );
		scanForHacksMenuItem      .setEnabled( false );
		displayGameChatMenuItem   .setEnabled( false );
		extractGameChatMenuItem   .setEnabled( false );
		removeFromListMenuItem    .setEnabled( false );
		copyReplaysMenuItem       .setEnabled( false );
		moveReplaysMenuItem       .setEnabled( false );
		deleteReplaysMenuItem     .setEnabled( false );
		renameReplayMenuItem      .setEnabled( false );
		groupRenameReplaysMenuItem.setEnabled( false );
		openInExplorerMenuItem    .setEnabled( false );
	}
	
	/**
	 * Shows the column setup dialog.
	 */
	private void showColumnSetupDialog() {
		final JDialog dialog = new JDialog( mainFrame, "Column setup" );
		dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		final int[] columnModelIndicesClone = columnModelIndices.clone();
		
		final JPanel columnsGrid = new JPanel( new GridLayout( columnModelIndices.length, 3 ) );
		buildColumnsGrid( columnsGrid, columnModelIndicesClone );
		dialog.add( columnsGrid, BorderLayout.CENTER );
		
		final JPanel buttonsPanel = Utils.createWrapperPanel();
		final JButton applyButton = new JButton( "Apply" );
		applyButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				for ( int i = columnModelIndices.length - 1; i >= 0; i-- )
					columnModelIndices[ i ]= columnModelIndicesClone[ i ];
				dialog.dispose();
				if ( !lastSearchResultFileList.isEmpty() )
					refreshResultTable();
			}
		} );
		applyButton.setMnemonic( applyButton.getText().charAt( 0 ) );
		buttonsPanel.add( applyButton );
		final JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				dialog.dispose();
			}
		} );
		cancelButton.setMnemonic( cancelButton.getText().charAt( 0 ) );
		buttonsPanel.add( cancelButton );
		dialog.add( buttonsPanel, BorderLayout.SOUTH );
		
		dialog.pack();
		dialog.setLocation( mainFrame.getLocationOnScreen().x + mainFrame.getWidth() / 2 - 150, mainFrame.getLocationOnScreen().y + mainFrame.getHeight() / 2 - 200 );
		dialog.setVisible( true );
	}
	
	/**
	 * Builds the columns grid with buttons to rearrange columns.
	 * @param columnsGrid             panel with <code>GridLayout</code> to build columns grid on
	 * @param columnModelIndicesClone column model indices to use
	 */
	private void buildColumnsGrid( final JPanel columnsGrid, final int[] columnModelIndicesClone ) {
		for ( int i = columnsGrid.getComponentCount() - 1; i >= 0; i-- )
			columnsGrid.remove( i );
		for ( int i = 0; i < columnModelIndicesClone.length; i++ ) {
			final int i_ = i;
			final JLabel columnNameLabel = new JLabel( RESULT_TABLE_COLUMN_NAMES[ columnModelIndicesClone[ i ] ] );
			columnNameLabel.setFont( new Font( "Default", Font.BOLD, 9 ) );
			columnsGrid.add( columnNameLabel );
			if ( i > 0 ) {
				final JButton moveUpButton = new JButton( IconResourceManager.ICON_ARROW_UP );
				moveUpButton.addActionListener( new ActionListener() {
					public void actionPerformed( final ActionEvent event ) {
						final int tempIndex = columnModelIndicesClone[ i_ ];
						columnModelIndicesClone[ i_ ] = columnModelIndicesClone[  i_ - 1 ];
						columnModelIndicesClone[ i_ - 1 ] = tempIndex;
						buildColumnsGrid( columnsGrid, columnModelIndicesClone );
					}
				} );
				columnsGrid.add( moveUpButton );
			}
			else
				columnsGrid.add( new JLabel() );
			if ( i < columnModelIndicesClone.length - 1 ) {
				final JButton moveDownButton = new JButton( IconResourceManager.ICON_ARROW_DOWN );
				moveDownButton.addActionListener( new ActionListener() {
					public void actionPerformed( final ActionEvent event ) {
						final int tempIndex = columnModelIndicesClone[ i_ ];
						columnModelIndicesClone[ i_ ] = columnModelIndicesClone[  i_ + 1 ];
						columnModelIndicesClone[ i_ + 1 ] = tempIndex;
						buildColumnsGrid( columnsGrid, columnModelIndicesClone );
					}
				} );
				columnsGrid.add( moveDownButton );
			}
			else
				columnsGrid.add( new JLabel() );
		}
	}
	
	/**
	 * Shows the group rename dialog.
	 */
	private void showGroupRenameDialog() {
		final int[]  selectedIndices = resultTable.getSelectedRows();
		
		final JDialog dialog = new JDialog( mainFrame, "Group renaming replays..." );
		dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		final Box box = Box.createVerticalBox();
		box.add( new JLabel( "Enter the template for renaming replays. You can insert any text and you can use the following symbols:\n\t/n the original name\n\t/e extension ('rep')\n\t/c counter which starts from 1 and will be incremented by 1 on each use" ) );
		final JTextField templateTextField = new JTextField( "NEW /n /c./e" );
		box.add( templateTextField );
		final JPanel buttonsPanel = Utils.createWrapperPanel();
		final JButton previewButton = new JButton( "Preview" );
		final JButton renameButton = new JButton( "Rename replays" );
		final JTextArea previewTextArea = new JTextArea( 5, 10 );
		final ActionListener renameActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final String template = templateTextField.getText();
				if ( template.length() == 0 ) {
					JOptionPane.showMessageDialog( dialog, "No template has been specified!", "Error", JOptionPane.ERROR_MESSAGE );
					return;
				}
				
				if ( template.indexOf( '\\' ) >= 0 ) {
					JOptionPane.showMessageDialog( dialog, "Template contains invalid characters!", "Error", JOptionPane.ERROR_MESSAGE );
					return;
				}
				
				final boolean previewOnly = event.getSource() == previewButton;
				if ( previewOnly )
					previewTextArea.setText( "" );
				
				final char[] templateCharArray = template.toCharArray();
				int counter      = 1;
				int errorCounter = 0;
				for ( final int selectedIndex : selectedIndices ) {
					final File selectedFile = lastSearchResultFileList.get( selectedIndex );
					final int lastPointIndex = selectedFile.getName().lastIndexOf( '.' );
					final String originalName = lastPointIndex >= 0 ? selectedFile.getName().substring( 0, lastPointIndex ) : selectedFile.getName();
					final String originalExt  = lastPointIndex >= 0 ? selectedFile.getName().substring( lastPointIndex + 1 ) : "";
					final StringBuilder newNameBuilder = new StringBuilder();
					boolean symbol = false;
					for ( final char templateChar : templateCharArray ) {
						if ( templateChar == '/' )
							symbol = true;
						else {
							if ( symbol ) {
								switch ( templateChar ) {
									case 'n': newNameBuilder.append( originalName ); break;
									case 'e': newNameBuilder.append( originalExt  ); break;
									case 'c': newNameBuilder.append( counter++    ); break;
									default : 
										JOptionPane.showMessageDialog( dialog, "Invalid symbol character: /" + templateChar, "Error", JOptionPane.ERROR_MESSAGE );
										return;
								}
								symbol = false;
							}
							else
								newNameBuilder.append( templateChar );
						}
					}
					if ( symbol ) {
						JOptionPane.showMessageDialog( dialog, "Missing symbol character at the end!", "Error", JOptionPane.ERROR_MESSAGE );
						return;
					}
					
					if ( previewOnly ) {
						previewTextArea.append( newNameBuilder.toString() );
						previewTextArea.append( "\n" );
					}
					else {
						final File newFile = new File( selectedFile.getParent(), newNameBuilder.toString() );
						if ( newFile.exists() ) 
							errorCounter++;
						else {
							if ( !selectedFile.renameTo( newFile ) )
								errorCounter++;
							else {
								lastSearchResultFileList.set( selectedIndex, newFile );
								lastSearchResultRowsData.get( selectedIndex )[ RESULT_TABLE_FILE_NAME_COLUMN_INDEX ] = newFile.getAbsolutePath();
							}
						}
					}
				}
				
				if ( !previewOnly ) {
					if ( errorCounter > 0 ) {
						if ( errorCounter < selectedIndices.length ) {
							JOptionPane.showMessageDialog( getContent(), "Rename completed with some errors (" + errorCounter + " out of " + selectedIndices.length + ")!", "Warning", JOptionPane.WARNING_MESSAGE );
							refreshResultTable();
						}
						else
							JOptionPane.showMessageDialog( getContent(), "Failed to rename replays!", "Error", JOptionPane.ERROR_MESSAGE );
					}
					else {
						JOptionPane.showMessageDialog( getContent(), "Successfully renamed " + selectedIndices.length + " replay" + ( selectedIndices.length == 1 ? "." : "s." ), "Success", JOptionPane.INFORMATION_MESSAGE );
						refreshResultTable();
					}
					dialog.dispose();
				}
			}
		};
		previewButton.addActionListener( renameActionListener );
		buttonsPanel.add( previewButton );
		renameButton.addActionListener( renameActionListener );
		buttonsPanel.add( renameButton );
		final JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				dialog.dispose();
			}
		} );
		buttonsPanel.add( cancelButton );
		box.add( buttonsPanel );
		box.add( new JLabel( "Preview of the new names:" ) );
		previewTextArea.setEditable( false );
		box.add( new JScrollPane( previewTextArea ) );
		dialog.add( box, BorderLayout.CENTER );
		
		previewButton.doClick();
		dialog.setSize( 700, 300 );
		dialog.setLocation( mainFrame.getLocationOnScreen().x + mainFrame.getWidth() / 2 - 350, mainFrame.getLocationOnScreen().y + mainFrame.getHeight() / 2 - 150 );
		dialog.setVisible( true );
	}
	
	/**
	 * Refreshes the result table from the <code>lastSearchResultRowsData</code> data list.
	 */
	private void refreshResultTable() {
		final Vector< Vector< String > > resultDataVector = new Vector< Vector< String > >( lastSearchResultRowsData.size() );
		for ( final String[] rowData : lastSearchResultRowsData ) {
			final Vector< String > rowVector = new Vector< String >( RESULT_TABLE_COLUMN_NAMES.length );
			for ( final int columnModelIndex : columnModelIndices )
				rowVector.add( rowData[ columnModelIndex ] );
			resultDataVector.add( rowVector );
		}
		final Vector< String > columnNameVector = new Vector< String >( RESULT_TABLE_COLUMN_NAMES.length );
		for ( final int columnModelIndex : columnModelIndices )
			columnNameVector.add( RESULT_TABLE_COLUMN_NAMES[ columnModelIndex ] );
		
		resultTable.setModel( new DefaultTableModel( resultDataVector, columnNameVector ) {
			@Override
			public boolean isCellEditable( final int row, final int column ) {
				return false;
			}
		} );
		
		// When model is replaced, the context menu is disposed (bug in SwingWT?). We have to rebuild it.
		rebuildReplayOperationsPopupMenu();
		
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
	 * If there are more than 1 selected replays, then the selection is the source, otherwise the whole result list.
	 * If the index refers to the last, the first is returned.<br>
	 * The new index is set in the indexWrapper.
	 * @param indexWrapper wrapper of the index to calculate next replay from; can be <code>null</code> in which case the first is returned
	 * @return the next file or the source list
	 */
	public File getNextReplayFile( final Integer[] indexWrapper ) {
		final Integer index = indexWrapper[ 0 ];
		
		final int[] selectedIndices = resultTable.getSelectedRows();
		if ( selectedIndices.length > 1 ) {
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
	 * If there are more than 1 selected replays, then the selection is the source, otherwise the whole result list.
	 * If the index refers to the first, the last is returned.
	 * The new index is set in the indexWrapper.
	 * @param indexWrapper wrapper of the index to calculate next replay from; can be <code>null</code> in which case the last is returned
	 * @return the previous file or the source list
	 */
	public File getPreviousReplayFile( final Integer[] indexWrapper ) {
		final Integer index = indexWrapper[ 0 ];
		
		final int[] selectedIndices = resultTable.getSelectedRows();
		if ( selectedIndices.length > 1 ) {
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
		
		disableReplayOperationMenuItems();
		
		lastSearchSourceFiles = files;
		if ( !appendResultsToTableCheckBox.isSelected() ) {
			lastSearchResultFileList.clear();
			lastSearchResultRowsData.clear();
			lastSortingModelIndex = -1;
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
									replayHeader.mapName, replayHeader.getDurationString( true ), ReplayHeader.GAME_TYPE_NAMES[ replayHeader.gameType ], replayHeader.getPlayerNamesString(), SIMPLE_DATE_FORMAT.format( replayHeader.saveTime ),
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
	 * Loads a result list from a file.<br>
	 * If the name of the list file is provided, error messages will not pop up.
	 * @param listFileName name of the list file to load; can be <code>null</code> and then a file chooser dialog will be shown 
	 */
	private void loadResultList( final String listFileName ) {
		File listFile = null;
		if ( listFileName == null ) {
			final JFileChooser fileChooser = new JFileChooser( mainFrame.generalSettingsTab.defaultReplayListsFolderTextField.getText() );
			fileChooser.setTitle( "Load result list..." );
			
			fileChooser.addChoosableFileFilter( Utils.SWING_TEXT_FILE_FILTER ); 
			fileChooser.setExtensionFilters( new String[] { "*.txt", "*.*" }, new String[] { "Text files (*.txt)", "All files (*.*)" } );
			
			fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
			if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
				listFile = fileChooser.getSelectedFile();
		}
		else
			listFile = new File( listFileName );
		
		if ( listFile != null ) {
			BufferedReader input = null;
			try {
				input = new BufferedReader( new FileReader( listFile ) );
				
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
				lastSortingModelIndex = -1;
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
				
				// We enable searching in previous results and saving result list.
				searchPreviousResultButton.setEnabled( true  );
				saveResultListButton      .setEnabled( true );
				
				updatedResultsCountLabel();
				refreshResultTable();
			}
			catch ( final Exception e ) {
				if ( listFileName == null )
					JOptionPane.showMessageDialog( getContent(), "Could not load result list!", "Error!", JOptionPane.ERROR_MESSAGE );
			}
			finally {
				if ( input != null )
					try { input.close(); } catch ( final IOException ie ) { ie.printStackTrace(); }
			}
		}
	}
	
	@Override
	public void initializationEnded() {
		super.initializationEnded();
		if ( mainFrame.generalSettingsTab.replayListToLoadOnStartupTextField.getText().length() > 0 )
			loadResultList( mainFrame.generalSettingsTab.replayListToLoadOnStartupTextField.getText() );
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_HIDE_SEARCH_FILTERS, Boolean.toString( !headerFiltersPanel.isVisible() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_APPEND_RESULTS_TO_TABLE, Boolean.toString( appendResultsToTableCheckBox.isSelected() ) );
		final StringBuilder columnModelIndicesBuilder = new StringBuilder();
		for ( final int columnModelIndex : columnModelIndices ) {
			if ( columnModelIndicesBuilder.length() > 0 )
				columnModelIndicesBuilder.append( ',' );
			columnModelIndicesBuilder.append( columnModelIndex );
		}
		Utils.settingsProperties.setProperty( Consts.PROPERTY_REPLAY_COLUMN_MODEL_INDICES, columnModelIndicesBuilder.toString() );
	}
	
}
