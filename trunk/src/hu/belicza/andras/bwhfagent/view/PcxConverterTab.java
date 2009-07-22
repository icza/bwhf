package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhfagent.Consts;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwt.awt.event.KeyEvent;
import swingwt.awt.event.KeyListener;
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
public class PcxConverterTab extends ProgressLoggedTab {
	
	/** Log file name for PCX converter.               */
	private static final String LOG_FILE_NAME                            = "pcx_converter.log";
	/** PCX file extension.                            */
	private static final String PCX_FILE_EXTENSION                       = ".pcx";
	/** Time between checking for new PCX files in ms. */
	private static final long   TIME_BETWEEN_CHECKS_FOR_NEW_PCX_FILES_MS = 1000l;
	/** Min width of the resized images.               */
	private static final int    MIN_RESIZED_WIDTH                        = 4;
	/** Max width of the resized images.               */
	private static final int    MAX_RESIZED_WIDTH                        = 1920;
	
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
	
	/** Checkbox to enable/disable the autoscan.           */
	private final JCheckBox  autoConvertEnabledCheckBox    = new JCheckBox( "Automatically convert new PCX images detected in Starcraft directory", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_AUTO_CONVERT_PCX_ENABLED ) ) );
	/** Combo box to display supported output formats.     */
	private final JComboBox  outputFormatComboBox          = new JComboBox( unifyStrings( ImageIO.getWriterFormatNames() ) );
	/** Checkbox to enable/disable the autoscan.           */
	private final JCheckBox  resizeConvertedImagesCheckBox = new JCheckBox( "Resize converted images,", Boolean.parseBoolean( Utils.settingsProperties.getProperty( Consts.PROPERTY_RESIZE_CONVERTED_IMAGES ) ) );
	/** Checkbox to enable/disable the autoscan.           */
	private final JTextField resizedImageWidthTextField    = new JTextField( Utils.settingsProperties.getProperty( Consts.PROPERTY_RESIZED_IMAGE_WIDTH ), 1 );
	/** Label to display the height of the resized images. */
	private final JLabel     resizedImageHeightLabel       = new JLabel();
	/** Button to select files to scan.                      */
	private final JButton    selectFilesButton             = new JButton( "Select PCX files to convert...", IconResourceManager.ICON_FILE_CHOOSER );
	
	/**
	 * Creates a new PcxConverterTab.
	 */
	public PcxConverterTab() {
		super( "PCX converter", IconResourceManager.ICON_PCX_CONVERTER, LOG_FILE_NAME );
		
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
	
	@Override
	protected void buildGUI() {
		JPanel panel;
		
		contentBox.add( Utils.wrapInPanel( autoConvertEnabledCheckBox ) );
		
		panel = Utils.createWrapperPanel();
		panel.add( new JLabel( "Output image format:" ) );
		panel.add( outputFormatComboBox );
		contentBox.add( panel );
		
		panel = Utils.createWrapperPanel();
		resizeConvertedImagesCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				resizedImageWidthTextField.setEnabled( resizeConvertedImagesCheckBox.isSelected() );
			}
		} );
		resizeConvertedImagesCheckBox.doClick();
		panel.add( resizeConvertedImagesCheckBox );
		panel.add( new JLabel( "resized image width (" + MIN_RESIZED_WIDTH + ".." + MAX_RESIZED_WIDTH + "):" ) );
		// This is a workaround becase SwingWT does not implement DocumentListener correctly :S
		resizedImageWidthTextField.addKeyListener( new KeyListener() {
			public void keyPressed( final KeyEvent event ) {
				syncResizedImageHeightLabel();
			}
			public void keyReleased( final KeyEvent event ) {
				syncResizedImageHeightLabel();
			}
			public void keyTyped( final KeyEvent event ) {
				syncResizedImageHeightLabel();
			}
		} );
		panel.add( resizedImageWidthTextField );
		panel.add( new JLabel( " pixels, height: " ) );
		panel.add( resizedImageHeightLabel );
		panel.add( new JLabel( " pixels." ) );
		syncResizedImageHeightLabel();
		contentBox.add( panel );
		
		selectFilesButton.setMnemonic( 'f' );
		selectFilesButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final JFileChooser fileChooser = new JFileChooser( new File( MainFrame.getInstance().generalSettingsTab.starcraftFolderTextField.getText() ) );
				
				// This is for SwingWT:
				fileChooser.setExtensionFilters( new String[] { "*.pcx", "*.*" }, new String[] { "PCX image files (*.pcx)", "All files (*.*)" } );
				// This is for Swing:
				fileChooser.addChoosableFileFilter( SWING_PCX_FILE_FILTER ); 
				
				fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
				fileChooser.setMultiSelectionEnabled( true );
				
				if ( fileChooser.showOpenDialog( getContent() ) == JFileChooser.APPROVE_OPTION )
					convertPcxFiles( fileChooser.getSelectedFiles(), false, true );
			}
		} );
		
		contentBox.add( Utils.wrapInPanel( selectFilesButton ) );
		
		super.buildGUI();
	}
	
	/**
	 * Checks if the supplied width is valid, and synchronizes the text of resized image height label to the supplied width.
	 */
	private void syncResizedImageHeightLabel() {
		final Integer width = getSuppliedWidth();
		if ( width == null )
			resizedImageHeightLabel.setText( "N/A" );
		else
			resizedImageHeightLabel.setText( Integer.toString( width * 3 / 4 ) ); // Starcraft 640x480 resolution is a 4:3 ratio
	}
	
	/**
	 * Returns the supplied width for resized images. 
	 * @return the supplied width for resized images; or <code>null</code> if the supplied width is invalid
	 */
	private Integer getSuppliedWidth() {
		try {
			final int width = Integer.parseInt( resizedImageWidthTextField.getText() );
			if ( width < MIN_RESIZED_WIDTH || width > MAX_RESIZED_WIDTH )
				return null;
			
			return width;
		}
		catch ( final NumberFormatException nfe ) {
			return null;
		}
	}
	
	/** Lock to synchronize auto and manual convert. */
	private final Object lock = new Object();
	
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
					synchronized (lock) {
						progressBar.setValue( 0 );
						progressBar.setMaximum( pcxFiles.length );
						if ( printNewLinesAtStart )
							logMessage( "\n", false ); // Prints 2 empty lines
						final String convertingMessage = "Converting " + pcxFiles.length + " pcx file" + ( pcxFiles.length == 1 ? "" : "s" );
						logMessage( convertingMessage + "..." );
						
						final long startTimeNanons = System.nanoTime();
						
						final String formatName = (String) outputFormatComboBox.getSelectedItem();
						final String extension = "." + formatName.toLowerCase();
						int counter = 0;
						for ( final File pcxFile : pcxFiles ) {
							if ( pcxFile.isFile() ) {
								final String absolutePcxPath = pcxFile.getAbsolutePath();
								try {
									final int extensionIndex = absolutePcxPath.lastIndexOf( '.' );
									BufferedImage image = ImageIO.read( pcxFile );
									if ( image == null )
										throw new Exception( "Failed parsing PCX file: " + absolutePcxPath );
									final boolean resize = resizeConvertedImagesCheckBox.isSelected() && getSuppliedWidth() != null;
									if ( resize ) {
										final int width  = getSuppliedWidth();
										final int height = width * 3 / 4;
										final BufferedImage resizedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
										final Graphics2D graphics2D = resizedImage.createGraphics();
										graphics2D.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
										graphics2D.drawImage( image, 0, 0, width, height, null );
										graphics2D.dispose();
										image.flush();
										image = resizedImage;
									}
									ImageIO.write( image, formatName, new File( ( extensionIndex < 0 ? absolutePcxPath : absolutePcxPath.substring( 0, extensionIndex ) ) + extension ) );
									image.flush();
									logMessage( "'" + absolutePcxPath + "' converted " + ( resize ? "and resized " : "" ) + "successfully." );
									if ( deleteOnSuccess )
										pcxFile.delete();
								} catch ( final Exception e ) {
									logMessage( "Failed to convert '" + absolutePcxPath + "'!");
								}
							}
							progressBar.setValue( ++counter );
						}
						
						final long endTimeNanons = System.nanoTime();
						logMessage( convertingMessage + " done in " + Utils.formatNanoTimeAmount( endTimeNanons - startTimeNanons ) );
					}
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
				final JTextField       starcraftFolderTextField = MainFrame.getInstance().generalSettingsTab.starcraftFolderTextField;
				final PlayerCheckerTab playerCheckerTab         = MainFrame.getInstance().playerCheckerTab;
				
				final Date[] lastCheckTimeHolder = new Date[ 1 ]; // Have to use a holder so the file filter local class can access it
				while ( true ) {
					try {
						synchronized (lock) {
							if ( autoConvertEnabledCheckBox.isSelected() || playerCheckerTab.playerCheckerEnabledCheckBox.isSelected() ) {
								if ( lastCheckTimeHolder[ 0 ] == null )
									lastCheckTimeHolder[ 0 ] = new Date();
								
								File[] pcxFiles = new File( starcraftFolderTextField.getText() ).listFiles( new java.io.FileFilter() {
									public boolean accept( final File pathname ) {
										return pathname.lastModified() > lastCheckTimeHolder[ 0 ].getTime() && pathname.getName().toLowerCase().endsWith( PCX_FILE_EXTENSION );
									}
								} );
								// We store it here, because what comes next might take some time (while new screenshot might be saved).
								lastCheckTimeHolder[ 0 ] = new Date();
								
								if ( pcxFiles != null && pcxFiles.length > 0 ) {
									if ( playerCheckerTab.playerCheckerEnabledCheckBox.isSelected() )
										pcxFiles = playerCheckerTab.checkPlayers( pcxFiles );
									
									if ( autoConvertEnabledCheckBox.isSelected() && pcxFiles.length > 0 ) {
										logMessage( "\n", false ); // Prints 2 empty lines
										logMessage( "New PCX file" + ( pcxFiles.length == 1 ? "" : "s" ) + " detected - proceeding to convert..." );
										convertPcxFiles( pcxFiles, true, false );
									}
								}
							}
							else
								lastCheckTimeHolder[ 0 ] = null;
						}
						
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
		Utils.settingsProperties.setProperty( Consts.PROPERTY_RESIZE_CONVERTED_IMAGES , Boolean.toString( resizeConvertedImagesCheckBox.isSelected() ) );
		Utils.settingsProperties.setProperty( Consts.PROPERTY_RESIZED_IMAGE_WIDTH     , resizedImageWidthTextField.getText() );
	}
	
}
