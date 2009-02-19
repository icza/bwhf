package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.BinRepParser;
import hu.belicza.andras.bwhf.model.Replay;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.bwhfagent.view.charts.ChartsComponent;

import java.io.File;

import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;

/**
 * Charts tab.
 * 
 * @author Andras Belicza
 */
public class ChartsTab extends Tab {
	
	/** Button to display game chat from the last replay.  */
	private final JButton   openLastReplayButton = new JButton( "Open 'LastReplay.rep'" );
	/** Button to select files to extract game chat.       */
	private final JButton   selectFileButton     = new JButton( "Select file to open" );
	/** Label to display the loaded replay.                */
	private final JLabel    loadedReplayLabel    = new JLabel();
	
	/** The component visualising the charts. */
	private final ChartsComponent chartsComponent = new ChartsComponent();
	
	public ChartsTab() {
		super( "Charts" );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		final JPanel panel = new JPanel();
		openLastReplayButton.setMnemonic( 'l' );
		openLastReplayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				setReplayFile( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.LAST_REPLAY_FILE_NAME ) );
			}
		} );
		panel.add( openLastReplayButton );
		selectFileButton.setMnemonic( 'f' );
		selectFileButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( new File( MainFrame.getInstance().starcraftFolderTextField.getText(), Consts.STARCRAFT_REPLAY_FOLDER ) );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.rep", "*.*" }, new String[] { "Replay Files (*.rep)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( ManualScanTab.SWING_REPLAY_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
				
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					setReplayFile( fileChooser.getSelectedFile() );
			}
		} );
		panel.add( selectFileButton );
		contentBox.add( panel );
		
		contentBox.add( loadedReplayLabel );
		
		contentBox.add( chartsComponent );
	}
	
	private void setReplayFile( final File file ) {
		final Replay replay = BinRepParser.parseReplay( file, false );
		
		if ( replay == null )
			loadedReplayLabel.setText( "Failed to load " + file.getAbsolutePath() + "!" );
		else
			loadedReplayLabel.setText( "Loaded replay: " + file.getAbsolutePath() );
		
		chartsComponent.setReplay( replay );
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
}
