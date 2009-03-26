package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.model.ReplayHeader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import swingwt.awt.BorderLayout;
import swingwt.awt.GridBagConstraints;
import swingwt.awt.GridBagLayout;
import swingwt.awt.Insets;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.Box;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JList;
import swingwtx.swing.JPanel;
import swingwtx.swing.JProgressBar;
import swingwtx.swing.JScrollPane;
import swingwtx.swing.JTextField;

/**
 * Replay search tab.
 * 
 * @author Andras Belicza
 */
public class ReplaySearchTab extends Tab {
	
	/** Button to select folders to search.  */
	private final JButton selectFoldersButton        = new JButton( "Select folders to search recursively" );
	/** Button to select files to search.    */
	private final JButton selectFilesButton          = new JButton( "Select files to search" );
	/** Button to stop the current search. */
	private final JButton stopSearchButton           = new JButton( "Stop current search" );
	/** Button to repeat search on the same files. */
	private final JButton repeatSearch               = new JButton( "Repeat search" );
	/** Button to run search on the same files. */
	private final JButton searchPreviousResultButton = new JButton( "Search previous result (narrow search)" );
	
	/** The progress bar component. */
	private final JProgressBar progressBar = new JProgressBar();
	
	/** List displaying the results. */
	private final JList resultList = new JList( new Object[] { "one", "two", "three", "four", "five", "six", "seven", "eight"} );
	
	/** Variable to store stop requests of search. */
	private volatile boolean requestedToStop;
	
	/** Game engine filter checkboxes.          */
	private final JCheckBox[] gameEngineCheckBoxes      = new JCheckBox[ ReplayHeader.GAME_ENGINE_NAMES.length ];
	/** Game name filter text field.            */
	private final JTextField  gameNameTextField         = new JTextField();
	/** Game name filter is regexp checkbox.    */
	private final JCheckBox   gameNameRegexpCheckBox    = new JCheckBox( "Regexp" );
	/** Creator name filter text field.         */
	private final JTextField  creatorNameTextField      = new JTextField();
	/** Creator name filter is regexp checkbox. */
	private final JCheckBox   creatorNameRegexpCheckBox = new JCheckBox( "Regexp" );
	/** Map name filter text field.            */
	private final JTextField  mapNameTextField          = new JTextField();
	/** Map name filter is regexp checkbox.    */
	private final JCheckBox   mapNameRegexpCheckBox     = new JCheckBox( "Regexp" );
	/** Player name filter text field.          */
	private final JTextField  playerNameTextField       = new JTextField();
	/** Player name filter is regexp checkbox.  */
	private final JCheckBox   playerNameRegexpCheckBox  = new JCheckBox( "Regexp" );
	/** Race filter checkboxes.                 */
	private final JCheckBox[] raceCheckBoxes            = new JCheckBox[ ReplayHeader.RACE_NAMES.length ];
	/** In-game player color filter checkboxes. */
	private final JCheckBox[] inGameColorCheckBoxes     = new JCheckBox[ Math.min( 16, ReplayHeader.IN_GAME_COLOR_NAMES.length ) ];
	/** Min duration filter text field.         */
	private final JTextField  durationMinTextField      = new JTextField();
	/** Max duration filter text field.         */
	private final JTextField  durationMaxTextField      = new JTextField();
	/** Earliest save time filter text field.   */
	private final JTextField  saveTimeEarliestTextField = new JTextField();
	/** Latest save time filter text field.     */
	private final JTextField  saveTimeLatestTextField   = new JTextField();
	
	/**
	 * Creates a new ReplaySearchTab.
	 */
	public ReplaySearchTab() {
		super( "Replay search" );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the panel.
	 */
	private void buildGUI() {
		final GridBagLayout      gridBagLayout      = new GridBagLayout();
		final GridBagConstraints constraints        = new GridBagConstraints();
		final JPanel             headerFiltersPanel = new JPanel( gridBagLayout );
		headerFiltersPanel.setBorder( BorderFactory.createTitledBorder( "Replay header fields:" ) );
		
		JLabel label;
		JPanel wrapperPanel;
		
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
		gridBagLayout.setConstraints( gameNameRegexpCheckBox, constraints );
		headerFiltersPanel.add( gameNameRegexpCheckBox );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Creator name:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( creatorNameTextField, constraints );
		headerFiltersPanel.add( creatorNameTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( creatorNameRegexpCheckBox, constraints );
		headerFiltersPanel.add( creatorNameRegexpCheckBox );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Map name:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( mapNameTextField, constraints );
		headerFiltersPanel.add( mapNameTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( mapNameRegexpCheckBox, constraints );
		headerFiltersPanel.add( mapNameRegexpCheckBox );
		
		constraints.gridwidth = 1;
		label = new JLabel( "Player name:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( playerNameTextField, constraints );
		headerFiltersPanel.add( playerNameTextField );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( playerNameRegexpCheckBox, constraints );
		headerFiltersPanel.add( playerNameRegexpCheckBox );
		
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
		label = new JLabel( "Save time earliest:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		gridBagLayout.setConstraints( saveTimeEarliestTextField, constraints );
		headerFiltersPanel.add( saveTimeEarliestTextField );
		label = new JLabel( "Save time latest:" );
		gridBagLayout.setConstraints( label, constraints );
		headerFiltersPanel.add( label );
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagLayout.setConstraints( saveTimeLatestTextField, constraints );
		headerFiltersPanel.add( saveTimeLatestTextField );
		
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
		selectButtonsPanel.add( repeatSearch );
		searchPreviousResultButton.setEnabled( false );
		searchPreviousResultButton.setMnemonic( 'p' );
		selectButtonsPanel.add( searchPreviousResultButton );
		contentBox.add( selectButtonsPanel );
		
		progressBar.setMaximumSize( Utils.getMaxDimension() );
		contentBox.add( progressBar );
		
		contentBox.add( new JLabel( "Replays matching the filters:" ) );
		final JPanel resultsPanel = new JPanel( new BorderLayout() );
		resultsPanel.add( new JScrollPane( resultList ), BorderLayout.CENTER );
		final JPanel resultActionsBox = Box.createVerticalBox();
		resultActionsBox.add( new JButton( "Do this" ) );
		resultActionsBox.add( new JButton( "Do that" ) );
		resultActionsBox.add( new JButton( "Do something" ) );
		resultsPanel.add( Utils.wrapInPanel( resultActionsBox ), BorderLayout.EAST );
		contentBox.add( resultsPanel );
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
		
		new NormalThread() {
			/** List of replay files to be scanned. */
			final List< File > replayFileList = new ArrayList< File >();
			
			@Override
			public void run() {
				try {
					progressBar.setValue( 0 );
					
					chooseReplayFiles( files );
					progressBar.setMaximum( replayFileList.size() );
					
					if ( requestedToStop )
						return;
					
				}
				finally {
					stopSearchButton   .setEnabled( false );
					selectFilesButton  .setEnabled( true  );
					selectFoldersButton.setEnabled( true  );
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
	
	@Override
	public void assignUsedProperties() {
	}
	
}
