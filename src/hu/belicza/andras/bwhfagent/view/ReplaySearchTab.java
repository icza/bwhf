package hu.belicza.andras.bwhfagent.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import swingwt.awt.BorderLayout;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.BorderFactory;
import swingwtx.swing.Box;
import swingwtx.swing.JButton;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JList;
import swingwtx.swing.JPanel;
import swingwtx.swing.JProgressBar;
import swingwtx.swing.JScrollPane;

/**
 * Replay search tab.
 * 
 * @author Andras Belicza
 */
public class ReplaySearchTab extends Tab {
	
	/** Button to select folders to scan.  */
	private final JButton selectFoldersButton = new JButton( "Select folders to search recursively" );
	/** Button to select files to scan.    */
	private final JButton selectFilesButton   = new JButton( "Select files to search" );
	/** Button to stop the current search. */
	private final JButton stopSearchButton    = new JButton( "Stop current search" );
	
	/** The progress bar component. */
	private final JProgressBar progressBar = new JProgressBar();
	
	/** List displaying the results. */
	private final JList resultList = new JList( new Object[] { "one", "two", "three", "four", "five", "six", "seven", "eight"} );
	
	/** Variable to store stop requests of search. */
	private volatile boolean requestedToStop;
	
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
		final JPanel headerFiltersPanel = new JPanel();
		headerFiltersPanel.setBorder( BorderFactory.createTitledBorder( "Replay header fields" ) );
		headerFiltersPanel.add( new JLabel( "Filter fields come here..." ) );
		contentBox.add( Utils.wrapInPanel( headerFiltersPanel ) );
		
		final JPanel selectButtonsPanel = Utils.createWrapperPanel();
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
