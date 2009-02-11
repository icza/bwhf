package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwtx.swing.JButton;
import swingwtx.swing.JCheckBox;
import swingwtx.swing.JComboBox;
import swingwtx.swing.JFileChooser;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;
import swingwtx.swing.JTextField;
import swingwtx.swing.filechooser.FileFilter;

/**
 * PCX converter tab.
 * 
 * @author Andras Belicza
 */
public class PcxConverterTab extends LoggedTab {
	
	/** Log file name for PCX converter.               */
	private static final String LOG_FILE_NAME                            = "pcx_converter.log";
	/** PCX file extension.                            */
	private static final String PCX_FILE_EXTENSION                       = ".pcx";
	/** Time between checking for new PCX files in ms. */
	private static final long   TIME_BETWEEN_CHECKS_FOR_NEW_PCX_FILES_MS = 3000l;
	
	/** PCX file filter. */
	private static final FileFilter SWING_PCX_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( final File file ) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith( PCX_FILE_EXTENSION );
		}
		@Override
		public String getDescription() {
			return "PCX image files (*.pcx)";
		}
	};
	
	/** Checkbox to enable/disable the autoscan.       */
	private final JCheckBox autoConvertEnabledCheckBox = new JCheckBox( "Automatically convert new PCX images detected in Starcraft directory", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTO_CONVERT_PCX_ENABLED ) ) );
	/** Combo box to display supported output formats. */
	private final JComboBox outputFormatComboBox       = new JComboBox( unifyStrings( ImageIO.getWriterFormatNames() ) );
	/** Button to select files to scan.                  */
	private final JButton   selectFilesButton          = new JButton( "Select PCX files to convert" );
	
	/**
	 * Creates a new PcxConverterTab.
	 */
	public PcxConverterTab() {
		super( "PCX converter", LOG_FILE_NAME );
		
		outputFormatComboBox.setSelectedItem( Utils.settingsProperties.getProperty( Consts.PROPERTY_PCX_OUTPUT_FORMAT ) );
		buildGUI();
		
		startAutoConverter();
	}
	
	/**
	 * Unifies strings with no case sensitivity.
	 * @param strings strings to be unified
	 * @return an array of unified strings
	 */
	private static String[] unifyStrings( final String[] strings ) {
		final Set< String > unifiedStringSet = new HashSet< String >();
		
		for ( final String string : strings ) {
			final String uppercasedString = string.toUpperCase();
			if ( !uppercasedString.equals( "WBMP" ) ) // WBMP conversion doesn't really work...
				unifiedStringSet.add( uppercasedString.equals( "JPEG" ) ? "JPG" : uppercasedString );
		}
		
		return unifiedStringSet.toArray( new String[ unifiedStringSet.size() ] );
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	protected void buildGUI() {
		JPanel panel;
		
		contentBox.add( Utils.wrapInPanel( autoConvertEnabledCheckBox ) );
		
		panel = new JPanel();
		panel.add( new JLabel( "Output image format:" ) );
		panel.add( outputFormatComboBox );
		contentBox.add( panel );
		
		selectFilesButton.setMnemonic( 'f' );
		selectFilesButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( new File( MainFrame.getInstance().starcraftFolderTextField.getText() ) );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.pcx", "*.*" }, new String[] { "PCX image files (*.pcx)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( SWING_PCX_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
				fileChooser.setMultiSelectionEnabled( true );
				
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					convertPcxFiles( fileChooser.getSelectedFiles(), false, true );
			}
		} );
		
		contentBox.add( Utils.wrapInPanel( selectFilesButton ) );
		
		super.buildGUI();
	}
	
	/**
	 * Converts pcx files to the selected output format.
	 * @param pcxFiles pcx files to be converted
	 * @param deleteOnSuccess tells if successfully converted files have to be deleted
	 * @param printNewLinesAtStart tells if we have to print new lines at start
	 */
	private void convertPcxFiles( final File[] pcxFiles, final boolean deleteOnSuccess, final boolean printNewLinesAtStart ) {
		selectFilesButton.setEnabled( false );
		new NormalThread() {
			@Override
			public void run() {
				try {
					if ( printNewLinesAtStart )
						logMessage( "\n", false ); // Prints 2 empty lines
					final String convertingMessage = "Converting " + pcxFiles.length + " pcx file" + ( pcxFiles.length == 1 ? "" : "s" );
					logMessage( convertingMessage + "..." );
					
					final long startTimeNanons = System.nanoTime();
					
					final String formatName = (String) outputFormatComboBox.getSelectedItem();
					final String extension = "." + formatName.toLowerCase();
					for ( final File pcxFile : pcxFiles )
						if ( pcxFile.isFile() ) {
							final String absolutePcxPath = pcxFile.getAbsolutePath();
							try {
								final int extensionIndex = absolutePcxPath.lastIndexOf( '.' );
								final BufferedImage image = ImageIO.read( pcxFile );
								if ( image == null )
									throw new Exception( "Failed parsing PCX file: " + absolutePcxPath );
								ImageIO.write( image, formatName, new File( ( extensionIndex < 0 ? absolutePcxPath : absolutePcxPath.substring( 0, extensionIndex ) ) + extension ) );
								logMessage( "'" + absolutePcxPath + "' converted successfully." );
								if ( deleteOnSuccess )
									pcxFile.delete();
							} catch ( final Exception e ) {
								logMessage( "Failed to convert '" + absolutePcxPath + "'!");
							}
						}
					
					final long endTimeNanons = System.nanoTime();
					logMessage( convertingMessage + " done in " + Utils.formatNanoTimeAmount( endTimeNanons - startTimeNanons ) );
				}
				finally {
					selectFilesButton.setEnabled( true );
				}
			}
		}.start();
	}
	
	/**
	 * Starts the auto converter.
	 */
	private void startAutoConverter() {
		new NormalThread() {
			
			@Override
			public void run() {
				final JTextField starcraftFolderTextField = MainFrame.getInstance().starcraftFolderTextField;
				
				final Date[] autoscanEnabledTimeHolder = new Date[ 1 ];
				while ( true ) {
					try {
						if ( autoConvertEnabledCheckBox.isSelected() ) {
							if ( autoscanEnabledTimeHolder[ 0 ] == null ) {
								autoscanEnabledTimeHolder[ 0 ] = new Date();
							}
							
							final File[] pcxFiles = new File( starcraftFolderTextField.getText() ).listFiles( new java.io.FileFilter() {
								public boolean accept( final File pathname ) {
									return pathname.lastModified() > autoscanEnabledTimeHolder[ 0 ].getTime() && pathname.getName().toLowerCase().endsWith( PCX_FILE_EXTENSION );
								}
							} );
							if ( pcxFiles != null && pcxFiles.length > 0 ) {
								logMessage( "\n", false ); // Prints 2 empty lines
								logMessage( "New PCX file" + ( pcxFiles.length == 1 ? "" : "s" ) + " detected - proceeding to convert..." );
								convertPcxFiles( pcxFiles, true, false );
							}
						}
						else
							autoscanEnabledTimeHolder[ 0 ] = null;
						
						sleep( TIME_BETWEEN_CHECKS_FOR_NEW_PCX_FILES_MS );
					}
					catch ( final InterruptedException ie ) {
					}
				}
			}
			
		}.start();
	}
	
	@Override
	public void assignUsedProperties() {
		Utils.settingsProperties.setProperty( Consts.PROPERTY_AUTO_CONVERT_PCX_ENABLED, Boolean.toString( autoConvertEnabledCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_PCX_OUTPUT_FORMAT       , (String) outputFormatComboBox.getSelectedItem() );
	}
	
}
