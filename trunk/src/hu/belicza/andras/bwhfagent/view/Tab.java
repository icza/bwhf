package hu.belicza.andras.bwhfagent.view;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Defines a tab to be added to the main frame.<br>
 * It's a simple {@link JPanel} with a {@link BorderLayout} layout manager.
 * 
 * @author Belicza Andras
 */
public class Tab extends JPanel {
	
	/** The title of the tab. */
	private String title;
	
	/**
	 * Creates a new Tab.
	 * @param title title of the tab
	 */
	public Tab( final String title ) {
		super( new BorderLayout() );
		this.title = title;
	}
	
	/**
	 * Returns the title of the tab.
	 * @return the title of the tab
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the content component of the tab.<br>
	 * First wraps it into a {@link JScrollPane} and adds it to the center of the panel.
	 * @param component content component to be set
	 */
	protected void setContent( final JComponent component ) {
		add( new JScrollPane( component ), BorderLayout.CENTER );
	}
	
}
