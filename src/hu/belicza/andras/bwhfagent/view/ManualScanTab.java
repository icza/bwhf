package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


/**
 * Manual scan tab.
 * 
 * @author Andras Belicza
 */
public class ManualScanTab extends LoggedTab {
	
	/** Log file name for autoscan. */
	private static final String LOG_FILE_NAME = "manual_scan.log";
	
	/** Button to scan the last replay.             */
	final JButton scanLastReplayButton        = new JButton( "Scan 'LastReplay.rep'" );
	/** Butotn to select files and folders to scan. */
	final JButton selectFilesAndFoldersButton = new JButton( "Select files and folders to scan" );
	
	/**
	 * Creates a new AutoscanTab.
	 */
	public ManualScanTab() {
		super( "Manual scan", LOG_FILE_NAME );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	protected void buildGUI() {
		scanLastReplayButton.setMnemonic( 'L' );
		contentBox.add( Utils.wrapInPanel( scanLastReplayButton ) );
		
		selectFilesAndFoldersButton.setMnemonic( 'f' );
		selectFilesAndFoldersButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( new File( Utils.settingsProperties.getProperty( Consts.PROPERTY_STARCRAFT_FOLDER ), Consts.STARCRAFT_REPLAY_FOLDER ) );
				
				fileChooser.addChoosableFileFilter( new FileFilter() {
					@Override
					public boolean accept( final File file ) {
						return file.isDirectory() || file.getName().toLowerCase().endsWith( ".rep" );
					}
					@Override
					public String getDescription() {
						return "Replay files (*.rep)";
					}
				} ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
				fileChooser.setMultiSelectionEnabled( true );
				if ( fileChooser.showOpenDialog( getScrollPane() ) == JFileChooser.APPROVE_OPTION ) {
					fileChooser.getSelectedFiles();
				}
			}
		} );
		contentBox.add( Utils.wrapInPanel( selectFilesAndFoldersButton ) );
		
		super.buildGUI();
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
}
