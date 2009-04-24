package hu.belicza.andras.keygen;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Authorization key generator utility for BWHF Agent.
 * 
 * @author Belicza Andras
 */
public class KeyGen extends JFrame {
	
	/** Length of the generated passwords.   */
	private static final int PASSWORD_LENGTH = 20;
	/** Characters to be used for passwords. */
	private static final char[] PASSWORD_CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	
	/**
	 * Entry point of the program.
	 * @param arguments used to take parameters from the running environment - not used here
	 */
	public static void main( final String[] arguments ) {
		new KeyGen();
	}
	
	/**
	 * Creates a new KeyGen.
	 */
	public KeyGen() {
		super( "Key Generator for BWHFAgent (c) András Belicza" );
		setDefaultCloseOperation( EXIT_ON_CLOSE );
		buildGUI();
		
		pack();
		
		setLocation( 200, 200 );
		final Rectangle bounds = getBounds();
		bounds.width += 50; // To get rid of horizontal scrollbars of the text areas
		setBounds( bounds );
		
		setVisible( true );
	}
	
	/**
	 * Builds the graphical user interface of the program.
	 */
	private void buildGUI() {
		final Box contentBox = Box.createVerticalBox();
		
		final JPanel controlPanel = new JPanel();
		final JButton generateButton = new JButton( "Generate" );
		controlPanel.add( generateButton );
		generateButton.setMnemonic( generateButton.getText().charAt( 0 ) );
		contentBox.add( controlPanel );
		
		final JPanel formPanel = new JPanel( new GridLayout( 4, 2 ) );
		
		formPanel.add( new JLabel( "Number of keys:", JLabel.RIGHT ) );
		final JSpinner keysCountSpinner = new JSpinner( new SpinnerNumberModel( 1, 1, 1000, 1 ) );
		formPanel.add( keysCountSpinner );
		
		formPanel.add( new JLabel( "Name of the person:", JLabel.RIGHT ) );
		final JTextField nameTextField = new JTextField( 20 );
		formPanel.add( nameTextField );
		
		formPanel.add( new JLabel( "E-mail of the person:", JLabel.RIGHT ) );
		final JTextField emailTextField = new JTextField();
		formPanel.add( emailTextField );
		
		formPanel.add( new JLabel( "Comment to the person:", JLabel.RIGHT ) );
		final JTextField commentTextField = new JTextField();
		formPanel.add( commentTextField );
		
		final JPanel wrapperPanel = new JPanel();
		wrapperPanel.add( formPanel );
		contentBox.add( wrapperPanel );
		
		final JTextArea sqlTextArea = new JTextArea( 8, 60 );
		JScrollPane scrollPane = new JScrollPane( sqlTextArea );
		scrollPane.setBorder( BorderFactory.createTitledBorder( "SQL:" ) );
		contentBox.add( scrollPane );
		
		final JTextArea emailMessageTextArea = new JTextArea( 18, 60 );
		scrollPane = new JScrollPane( emailMessageTextArea );
		scrollPane.setBorder( BorderFactory.createTitledBorder( "Email:" ) );
		contentBox.add( scrollPane );
		
		getContentPane().add( contentBox, BorderLayout.CENTER );
		
		generateButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final Random          random      = new Random( System.nanoTime() );
				final int             keysCount   = (Integer) keysCountSpinner.getValue();
				final StringBuilder[] keyBuilders = new StringBuilder[ keysCount ];
				
				for ( int i = 0; i < keysCount; i++ ) {
					final StringBuilder keyBuilder = keyBuilders[ i ] = new StringBuilder();
					for ( int j = 0; j < PASSWORD_LENGTH; j++ )
						keyBuilder.append( PASSWORD_CHARSET[ random.nextInt( PASSWORD_CHARSET.length ) ] );
				}
				
				final StringBuilder sqlBuilder = new StringBuilder( "INSERT INTO person (name,email,comment) VALUES(" );
				
				if ( nameTextField.getText().length() > 0 )
					sqlBuilder.append( '\'' ).append( nameTextField.getText() ).append( "'," );
				else
					sqlBuilder.append( "null," );
				
				if ( emailTextField.getText().length() > 0 )
					sqlBuilder.append( '\'' ).append( emailTextField.getText() ).append( "'," );
				else
					sqlBuilder.append( "null," );
				if ( commentTextField.getText().length() > 0 )
					sqlBuilder.append( '\'' ).append( commentTextField.getText() ).append( '\'' );
				else
					sqlBuilder.append( "null" );
				sqlBuilder.append( ");\n" );
				
				for ( int i = 0; i < keysCount; i++ ) {
					sqlBuilder.append( "INSERT INTO key (value,person) VALUES ('" );
					sqlBuilder.append( keyBuilders[ i ] );
					sqlBuilder.append( "',(SELECT MAX(id) FROM person));\n" );
				}
				
				sqlTextArea.setText( sqlBuilder.toString() );
				
				final StringBuilder emailMessageBuilder = new StringBuilder();
				if ( emailTextField.getText().length() > 0 )
					emailMessageBuilder.append( emailTextField.getText() );
				emailMessageBuilder.append( "\nBWHF authorization key\n\n\n" );
				
				emailMessageBuilder.append( "Hi" );
				if ( nameTextField.getText().length() > 0 )
					emailMessageBuilder.append( ' ' ).append( nameTextField.getText() );
				emailMessageBuilder.append( "!\n\n" );
				
				emailMessageBuilder.append( keysCount == 1 ? "This is your BWHF authorization key:" : "Here are your BWHF authorization keys:" );
				
				for ( final StringBuilder keyBuilder : keyBuilders )
					emailMessageBuilder.append( '\n' ).append( keyBuilder );
				
				emailMessageBuilder.append( "\n\n" );
				
				if ( keysCount == 1 ) {
					emailMessageBuilder.append( "It's your own, don't give it to anyone." );
				}
				else {
					emailMessageBuilder.append( "Plz make sure that one key is only used by one person." );
				}
				emailMessageBuilder.append( "\n\nCheers,\n    Dakota_Fanning" );
				
				emailMessageTextArea.setText( emailMessageBuilder.toString() );
			}
		} );
	}
	
}
