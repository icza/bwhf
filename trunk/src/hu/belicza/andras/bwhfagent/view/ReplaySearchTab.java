package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhf.model.ReplayHeader;
import hu.belicza.andras.bwhfagent.view.replayfilter.CreatorNameReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.DurationReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.GameEngineReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.GameNameReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.MapNameReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.MapSizeReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.PlayerColorReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.PlayerNameReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.PlayerRaceReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.ReplayFilter;
import hu.belicza.andras.bwhfagent.view.replayfilter.SaveTimeReplayFilter;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import swingwt.awt.BorderLayout;
import swingwt.awt.Color;
import swingwt.awt.GridBagConstraints;
import swingwt.awt.GridBagLayout;
import swingwt.awt.Insets;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.Box;
import swingwtx.swing.DefaultListModel;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JList;
import swingwtx.swing.JOptionPane;
import swingwtx.swing.JPanel;
import swingwtx.swing.JProgressBar;
import swingwtx.swing.JScrollPane;
import swingwtx.swing.JTextField;
import swingwtx.swing.ListSelectionModel;
import swingwtx.swing.event.ListSelectionEvent;
import swingwtx.swing.event.ListSelectionListener;

/**
 * Replay search tab.
 * 
 * @author Andras Belicza
 */
public class ReplaySearchTab extends Tab {
	
	/**
	 * Class to specify a map size.
	 * @author Andras Belicza
	 */
	public static class MapSize {
		/** Standard map lengths (applies both to widht and height). */
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
	
	/** Button to select folders to search.  */
	private final JButton selectFoldersButton        = new JButton( "Select folders to search recursively" );
	/** Button to select files to search.    */
	private final JButton selectFilesButton          = new JButton( "Select files to search" );
	/** Button to stop the current search. */
	private final JButton stopSearchButton           = new JButton( "Stop current search" );
	/** Button to repeat search on the same files. */
	private final JButton repeatSearch               = new JButton( "Repeat search" );
	/** Button to run search on the same files. */
	private final JButton searchPreviousResultButton = new JButton( "Search in previous result (narrows previous result)" );
	
	/** The progress bar component. */
	private final JProgressBar progressBar = new JProgressBar();
	
	/** List displaying the results. */
	private final JList resultList = new JList();
	
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
	
	/** Reference to the source files of the last search.                */
	private       File[]         lastSearchSourceFiles;
	/** Reference to the result file file of the last search.            */
	private final List< File >   lastSearchResultFileList   = new ArrayList< File >();
	/** Reference to the result file descriptin list of the last search. */
	private final List< String > lastSearchResultStringList = new ArrayList< String >();
	/** Number of replay files in the last search.                       */
	private       int            lastSearchReplayFilesCount;
	
	
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
				gameNameExactMatchCheckBox.setSelected( false );
				gameNameRegexpCheckBox.setSelected( false );
				gameNameRegexpCheckBox.doClick();
				creatorNameTextField.setText( "" );
				creatorNameExactMatchCheckBox.setSelected( false );
				creatorNameRegexpCheckBox.setSelected( false );
				creatorNameRegexpCheckBox.doClick();
				mapNameTextField.setText( "" );
				mapNameExactMatchCheckBox.setSelected( false );
				mapNameRegexpCheckBox.setSelected( false );
				mapNameRegexpCheckBox.doClick();
				playerNameTextField.setText( "" );
				playerNameExactMatchCheckBox.setSelected( false );
				playerNameRegexpCheckBox.setSelected( false );
				playerNameRegexpCheckBox.doClick();
				for ( final JCheckBox checkBox : raceCheckBoxes )
					checkBox.setSelected( false );
				for ( final JCheckBox checkBox : inGameColorCheckBoxes )
					checkBox.setSelected( false );
				durationMinTextField.setText( "" );
				durationMaxTextField.setText( "" );
				saveDateEarliestTextField.setText( "" );
				saveDateLatestTextField.setText( "" );
				versionMinComboBox.setSelectedIndex( 0 );
				versionMaxComboBox.setSelectedIndex( 0 );
				mapSizeMinComboBox.setSelectedIndex( 0 );
				mapSizeMaxComboBox.setSelectedIndex( 0 );
			}
		} );
		filterFieldsButtonsPanel.add( resetFilterFieldsButton );
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
		final GridBagLayout      gridBagLayout2 = new GridBagLayout();
		final GridBagConstraints constraints2   = new GridBagConstraints();
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
		
		contentBox.add( Utils.wrapInPanel( headerFiltersPanel ) );
		
		JPanel selectButtonsPanel = Utils.createWrapperPanel();
		final ActionListener selectFilesAndFoldersActionListener = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( MainFrame.getInstance().generalSettingsTab.getReplayStartFolder() );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( ManualScanTab.SWING_REPLAY_FILE_FILTER ); 
				
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
				requestedToStop = true; // Access to a volatile variable is automatically synchronized from Java 5.0
				stopSearchButton.setEnabled( false );
			}
		} );
		selectButtonsPanel.add( stopSearchButton );
		contentBox.add( selectButtonsPanel );
		
		selectButtonsPanel = Utils.createWrapperPanel();
		repeatSearch.setEnabled( false );
		repeatSearch.setMnemonic( 'r' );
		repeatSearch.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent evnet ) {
				searchFilesAndFolders( lastSearchSourceFiles );
			}
		} );
		selectButtonsPanel.add( repeatSearch );
		searchPreviousResultButton.setEnabled( false );
		searchPreviousResultButton.setMnemonic( 'p' );
		searchPreviousResultButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent evnet ) {
				searchFilesAndFolders( lastSearchResultFileList.toArray( new File[ lastSearchResultFileList.size() ] ) );
			}
		} );
		selectButtonsPanel.add( searchPreviousResultButton );
		contentBox.add( selectButtonsPanel );
		
		progressBar.setMaximumSize( Utils.getMaxDimension() );
		contentBox.add( progressBar );
		
		final JPanel panel = Utils.createWrapperPanelLeftAligned();
		panel.add( new JLabel( "Replays matching the filters:" ) );
		panel.add( resultsCountLabel );
		contentBox.add( panel );
		
		final JPanel resultsPanel = new JPanel( new BorderLayout() );
		showOnChartsButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				mainFrame.chartsTab.setReplayFile( lastSearchResultFileList.get( resultList.getSelectedIndex() ) );
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
				mainFrame.gameChatTab.showGameChatFromReplay( lastSearchResultFileList.get( resultList.getSelectedIndex() ) );
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
				
				if ( event.getSource() != deleteReplaysButton ) {
					final JFileChooser fileChooser = new JFileChooser( MainFrame.getInstance().generalSettingsTab.getReplayStartFolder() );
					fileChooser.setTitle( ( event.getSource() == copyReplaysButton ? "Copy " : "Move ") + selectedFiles.length + " replay" + ( selectedFiles.length == 1 ? "" : "s" ) + " to" );
					fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
					if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION ) {
						final File destinationFolder = fileChooser.getSelectedFile();
						for ( final File selectedFile : selectedFiles )
							Utils.copyFile( selectedFile, destinationFolder, selectedFile.getName() );
						
						if ( event.getSource() == moveReplaysButton )
							for ( int index : resultList.getSelectedIndices() )
								lastSearchResultFileList.set( index, new File( destinationFolder, lastSearchResultFileList.get( index ).getName() ) );
					}
					else
						return;
				}
				
				if ( event.getSource() == deleteReplaysButton )
					if ( JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog( getContent(), "Are you sure that you want to delete " + selectedFiles.length + " replay" + ( selectedFiles.length == 1 ? "?" : "s?" ), "Warning!", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION ) )
						return;
				
				boolean success = true;
				if ( event.getSource() != copyReplaysButton ) {
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
		resultList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		resultList.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				final int selectedCount = resultList.getSelectedIndices().length;
				showOnChartsButton   .setEnabled( selectedCount == 1 );
				scanForHacksButton   .setEnabled( selectedCount >  0 );
				displayGameChatButton.setEnabled( selectedCount == 1 );
				extractGameChatButton.setEnabled( selectedCount >  0 );
				removeFromListButton .setEnabled( selectedCount >  0 );
				copyReplaysButton    .setEnabled( selectedCount >  0 );
				moveReplaysButton    .setEnabled( selectedCount >  0 );
				deleteReplaysButton  .setEnabled( selectedCount >  0 );
			}
		} );
		resultsPanel.add( new JScrollPane( resultList ), BorderLayout.CENTER );
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
	 * Returns the selected result files.
	 * @return the selected result files
	 */
	private File[] getSelectedResultFiles() {
		final int[]  selectedIndices = resultList.getSelectedIndices();
		final File[] selectedFiles   = new File[ selectedIndices.length ];
		
		for ( int i = 0; i < selectedIndices.length; i++ )
			selectedFiles[ i ] = lastSearchResultFileList.get( selectedIndices[ i ] );
		
		return selectedFiles;
	}
	
	/**
	 * Removes selected lines from result list.
	 */
	private void removeSelectedFromResultList() {
		final int[] selectedIndices = resultList.getSelectedIndices();
		final DefaultListModel resultListModel = new DefaultListModel();
		for ( int i = selectedIndices.length - 1; i >= 0; i-- ) { // Downward is a must, indices change when an element is removed!
			lastSearchResultFileList.remove( selectedIndices[ i ] );
			lastSearchResultStringList.remove( selectedIndices[ i ] );
		}
		for ( final String replayString : lastSearchResultStringList )
			resultListModel.addElement( replayString );
		resultList.setModel( resultListModel );
		updatedResultsCountLabel();
	}
	
	/**
	 * Updates the results count label.
	 */
	private void updatedResultsCountLabel() {
		resultsCountLabel.setText( resultList.getItemCount() + " out of " + lastSearchReplayFilesCount );
	}
	
	/**
	 * Searches the specified files and folders.
	 * @param files        files and folders to be searched
	 */
	private void searchFilesAndFolders( final File[] files ) {
		requestedToStop = false;
		selectFoldersButton.setEnabled( false );
		selectFilesButton  .setEnabled( false );
		stopSearchButton   .setEnabled( true  );
		
		disableResultHandlerButtons();
		
		lastSearchSourceFiles = files;
		lastSearchResultFileList.clear();
		lastSearchResultStringList.clear();
		
		final ReplayFilter[] replayFilters = getReplayFilters();
		
		new NormalThread() {
			/** List of replay files to be scanned. */
			final List< File > replayFileList = new ArrayList< File >();
			
			@Override
			public void run() {
				final DefaultListModel resultListModel = new DefaultListModel();
				try {
					( (DefaultListModel) resultList.getModel() ).clear();
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
								final String replayString = replayFile.getAbsolutePath();
								lastSearchResultStringList.add( replayString );
								resultListModel.addElement( replayString );
							}
						}
						
						progressBar.setValue( ++counter );
					}
					
				}
				finally {
					resultList.setModel( resultListModel );
					updatedResultsCountLabel();
					stopSearchButton          .setEnabled( false );
					selectFilesButton         .setEnabled( true  );
					selectFoldersButton       .setEnabled( true  );
					repeatSearch              .setEnabled( true  );
					searchPreviousResultButton.setEnabled( true  );
				}
			}
			
			private final java.io.FileFilter IO_REPLAY_FILE_FILTER = new java.io.FileFilter() {
				public boolean accept( final File pathname ) {
					return ManualScanTab.SWING_REPLAY_FILE_FILTER.accept( pathname );
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
		if ( stringValue.length() > 0 )
			replayFilterList.add( new GameNameReplayFilter( stringValue, gameNameExactMatchCheckBox.isSelected(), gameNameRegexpCheckBox.isSelected() ) );
		
		stringValue = creatorNameTextField.getText();
		if ( stringValue.length() > 0 )
			replayFilterList.add( new CreatorNameReplayFilter( stringValue, creatorNameExactMatchCheckBox.isSelected(), creatorNameRegexpCheckBox.isSelected() ) );
		
		stringValue = mapNameTextField.getText();
		if ( stringValue.length() > 0 )
			replayFilterList.add( new MapNameReplayFilter( stringValue, mapNameExactMatchCheckBox.isSelected(), mapNameRegexpCheckBox.isSelected() ) );
		
		stringValue = playerNameTextField.getText();
		if ( stringValue.length() > 0 )
			replayFilterList.add( new PlayerNameReplayFilter( stringValue, playerNameExactMatchCheckBox.isSelected(), playerNameRegexpCheckBox.isSelected() ) );
		
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
		if ( !selectedByteValueList.isEmpty() )
			replayFilterList.add( new PlayerColorReplayFilter( selectedIntegerValueList ) );
		
		Integer minDuration = null;
		Integer maxDuration = null;
		try {
			if ( durationMinTextField.getText().length() > 0 )
				minDuration = Integer.parseInt( durationMinTextField.getText() );
		}
		catch ( final Exception e ) {
			durationMinTextField.setText( "" );
		}
		try {
			if ( durationMaxTextField.getText().length() > 0 )
				maxDuration = Integer.parseInt( durationMaxTextField.getText() );
		}
		catch ( final Exception e ) {
			durationMaxTextField.setText( "" );
		}
		if ( minDuration != null || maxDuration != null )
			replayFilterList.add( new DurationReplayFilter( minDuration, maxDuration ) );
		
		final DateFormat SDF = new SimpleDateFormat( "yyyy-MM-dd" );
		Long minSaveDate = null;
		Long maxSaveDate = null;
		try {
			if ( saveDateEarliestTextField.getText().length() > 0 )
				minSaveDate = SDF.parse( saveDateEarliestTextField.getText() ).getTime();
		}
		catch ( final Exception e ) {
			saveDateEarliestTextField.setText( "" );
		}
		try {
			SDF.parse( saveDateLatestTextField.getText() );
		}
		catch ( final Exception e ) {
			saveDateLatestTextField.setText( "" );
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
		
		final ReplayFilter[] replayFilters = replayFilterList.toArray( new ReplayFilter[ replayFilterList.size() ] );
		Arrays.sort( replayFilters );
		
		return replayFilters;
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
}
