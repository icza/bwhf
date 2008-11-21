package hu.belicza.andras.bwhfagent.view;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Utilitiy methods related to view and GUI.
 * 
 * @author Belicza Andras
 */
public class Utils {
	
	/**
	 * Wraps a component into a {@link JPanel} with a {@link FlowLayout}.
	 * @param component component to be wrapped
	 * @return the panel wrapping the component
	 */
	public static JPanel wrapInPanel( final JComponent component ) {
		final JPanel panel = new JPanel();
		panel.add( component );
		return panel;
	}
	
}
