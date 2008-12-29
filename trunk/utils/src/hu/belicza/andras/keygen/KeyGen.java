package hu.belicza.andras.keygen;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

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
		setVisible( true );
	}
	
	/**
	 * Builds the graphical user interface of the program.
	 */
	private void buildGUI() {
		final JPanel controlPanel = new JPanel();
		final JButton generateButton = new JButton( "Generate" );
		controlPanel.add( generateButton );
		generateButton.setMnemonic( generateButton.getText().charAt( 0 ) );
		getContentPane().add( controlPanel, BorderLayout.NORTH );
		
		
		final JPanel formPanel = new JPanel( new GridLayout( 5, 2 ) );
		
		formPanel.add( new JLabel( "Number of keys:" ) );
		final JSpinner keysCountSpinner = new JSpinner( new SpinnerNumberModel( 1, 1, 1000, 1 ) );
		formPanel.add( keysCountSpinner );
		
		formPanel.add( new JLabel( "Id of person:" ) );
		final JSpinner personIdSpinner = new JSpinner( new SpinnerNumberModel( 1, 0, 1000000000, 1 ) );
		formPanel.add( personIdSpinner );
		
		formPanel.add( new JLabel( "Name of the person:" ) );
		final JTextField nameTextField = new JTextField();
		formPanel.add( nameTextField );
		
		formPanel.add( new JLabel( "E-mail of the person:" ) );
		final JTextField emailTextField = new JTextField();
		formPanel.add( emailTextField );
		
		formPanel.add( new JLabel( "Comment to the person:" ) );
		final JTextField commentTextField = new JTextField();
		formPanel.add( commentTextField );
		
		getContentPane().add( formPanel, BorderLayout.CENTER );
		
		final JTextArea resultTextArea = new JTextArea( 10, 40 );
		getContentPane().add( new JScrollPane( resultTextArea ), BorderLayout.SOUTH );
		
		generateButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int personId = (Integer) personIdSpinner.getValue();
				final StringBuilder sqlBuilder = new StringBuilder();
				sqlBuilder.append( "INSERT INTO person (id,name,email,comment) VALUES(" )
					.append( personId ).append( ',' );
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
				
				final Random random = new Random( System.nanoTime() );
				final int keysCount = (Integer) keysCountSpinner.getValue();
				for ( int i = 0; i < keysCount; i++ ) {
					sqlBuilder.append( "INSERT INTO key (value,person) VALUES ('" );
					for ( int j = 0; j < PASSWORD_LENGTH; j++ )
						sqlBuilder.append( PASSWORD_CHARSET[ random.nextInt( PASSWORD_CHARSET.length ) ] );
					sqlBuilder.append( "'," ).append( personId ).append( ");\n" );
				}
				
				resultTextArea.setText( sqlBuilder.toString() );
			}
		} );
	}
	
}
