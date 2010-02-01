package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.bwhfagent.view.charts.EapmUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import swingwt.awt.BorderLayout;
import swingwt.awt.Dimension;
import swingwt.awt.GridLayout;
import swingwt.awt.event.ActionEvent;
import swingwt.awt.event.ActionListener;
import swingwt.awt.event.ComponentAdapter;
import swingwt.awt.event.ComponentEvent;
import swingwtx.swing.JButton;
import swingwtx.swing.JEditorPane;
import swingwtx.swing.JLabel;
import swingwtx.swing.JPanel;
import swingwtx.swing.JScrollPane;
import swingwtx.swing.SwingUtilities;
import swingwtx.swing.SwingWTUtils;

/**
 * About tab.
 * 
 * @author Andras Belicza
 */
public class AboutTab extends Tab {
	
	/** A map containing the possible parameters in the template file and their values to be replaced with. */
	private final Map< String, String > templateParameterValueMap = new HashMap< String, String >();
	
	/** Reference to the content editor pane, so it can be put in focus when the tab is selected. */
	private JEditorPane editorPane;
	
	/**
	 * Creates a new AboutTab.
	 */
	public AboutTab() {
		super( "About", IconResourceManager.ICON_ABOUT );
		
		templateParameterValueMap.put( "%APPLICATION_NAME%"                       , Consts.APPLICATION_NAME + "&#8482;" ); // &#8482; is the code of trade mark ('™')
		templateParameterValueMap.put( "%APPLICATION_AUTHOR%"                     , Consts.APPLICATION_AUTHOR );
		templateParameterValueMap.put( "%AUTHOR_EMAIL%"                           , Consts.AUTHOR_EMAIL );
		templateParameterValueMap.put( "%APPLICATION_VERSION%"                    , MainFrame.getInstance().applicationVersion );
		templateParameterValueMap.put( "%APP_SUBVERSION_SCAN_ENGINE%"             , ReplayScanner.ENGINE_VERSION );
		templateParameterValueMap.put( "%APP_SUBVERSION_PLAYER_MATCHER_ALGORITHM%", PlayerMatcherTab.PLAYER_MATCHER_ALGORITHM_VERSION );
		templateParameterValueMap.put( "%APP_SUBVERSION_EAPM_ALGORITHM%"          , EapmUtil.EAPM_ALGORITHM_VERSION );
		templateParameterValueMap.put( "%HOME_PAGE_URL%"                          , Consts.HOME_PAGE_URL );
		templateParameterValueMap.put( "%BWHF_HACKER_DATA_BASE_SERVER_URL%"       , Consts.BWHF_HACKER_DATA_BASE_SERVER_URL );
		
		buildGUI();
	}
	
	/**
	 * Builds the GUI of the tab.
	 */
	private void buildGUI() {
		final JPanel buttonsPanel = Utils.createWrapperPanel();
		// We need to issue a validate() if the state of a maximized window changes (SwingWT bug). 
		buttonsPanel.addComponentListener( new ComponentAdapter() {
			@Override
			public void componentResized( final ComponentEvent ce ) {
				SwingUtilities.invokeLater( new Runnable() {
					public void run() {
						// This is needed, else the scroll pane does not get resized (SwingWT bug also).
						editorPane.setHeight( 10 );
						editorPane.getParent().validate();
						buttonsPanel.getParent().validate();
					}
				} );
			}
		} );
		buttonsPanel.setMaximumSize( Utils.getMaxDimension() );
		JButton button = new JButton( "Visit home page", IconResourceManager.ICON_WORLD_GO );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.showURLInBrowser( Consts.HOME_PAGE_URL );
			}
		} );
		buttonsPanel.add( button );
		button = new JButton( "View online hacker database", IconResourceManager.ICON_WORLD_GO );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.showURLInBrowser( Consts.BWHF_HACKER_DATA_BASE_SERVER_URL );
			}
		} );
		buttonsPanel.add( button );
		buttonsPanel.add( button );
		button = new JButton( "Visit Players' Network", IconResourceManager.ICON_WORLD_GO );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.showURLInBrowser( Consts.PLAYERS_NETWORK_PAGE_URL );
			}
		} );
		buttonsPanel.add( button );
		contentBox.add( buttonsPanel );
		
		if ( SwingWTUtils.isWindows() ) {
			final StringBuilder aboutHtmlBuilder = new StringBuilder();
			BufferedReader input = null;
			try {
				input = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( Consts.ABOUT_TEMLATE_RESOURCE_NAME ) ) );
				while ( input.ready() )
					aboutHtmlBuilder.append( input.readLine() );
			} catch ( final IOException ie ) {
			}
			finally {
				if ( input != null )
					try { input.close(); } catch ( final IOException ie ) {}
			}
			
			String aboutHtml = aboutHtmlBuilder.toString();
			for ( final Map.Entry< String, String > entry : templateParameterValueMap.entrySet() )
				aboutHtml = aboutHtml.replace( entry.getKey(), entry.getValue() );
			
			editorPane = new JEditorPane( "text/html", aboutHtml );
			editorPane.setEditable( false );
			/*editorPane.addHyperlinkListener( new HyperlinkListener() {
				public void hyperlinkUpdate( final HyperlinkEvent event ) {
					if ( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
						if ( event.getURL() != null )
							Utils.showURLInBrowser( event.getURL().toString() );
				}
			} );*/
			
			// Why do we need this?
			// Not setting preferred size of the editorPane results in really wide window size after pack,
			// because the about html contains long lines which are not broken by default.
			// Setting preferred size of the editorPane results in no scrollbars around it, so its
			// full content wouldn't be visible.
			// Solution: the component added to the content box should be sized and scrolled.
			final JScrollPane wrapperScrollPane = new JScrollPane( editorPane );
			wrapperScrollPane.setPreferredSize( new Dimension( 200, 200 ) );
			contentBox.add( wrapperScrollPane );
		}
		else {
			final JPanel aboutPanel = new JPanel( new GridLayout( 8, 2, 0, 12 ) );
			aboutPanel.add( new JLabel( "Application name:" ) );
			aboutPanel.add( new JLabel( Consts.APPLICATION_NAME + "\u2122" ) );  // \u2122 is the unicode of trade mark ('™')
			aboutPanel.add( new JLabel( "Version " ) );
			aboutPanel.add( new JLabel( MainFrame.getInstance().applicationVersion ) );
			aboutPanel.add( new JLabel( "Author:" ) );
			aboutPanel.add( new JLabel( Consts.APPLICATION_AUTHOR ) );
			aboutPanel.add( new JLabel( "Author e-mail:" ) );
			aboutPanel.add( new JLabel( Consts.AUTHOR_EMAIL ) );
			aboutPanel.add( new JLabel( "Battle.net account:" ) );
			aboutPanel.add( new JLabel( "Dakota_Fanning@USEast" ) );
			aboutPanel.add( new JLabel( "Home page:" ) );
			aboutPanel.add( new JLabel( Consts.HOME_PAGE_URL ) );
			aboutPanel.add( new JLabel( "Online hacker database:" ) );
			aboutPanel.add( new JLabel( Consts.BWHF_HACKER_DATA_BASE_SERVER_URL ) );
			aboutPanel.add( new JLabel( "Player's Network:" ) );
			aboutPanel.add( new JLabel( Consts.PLAYERS_NETWORK_PAGE_URL ) );
			
			contentBox.add( Utils.wrapInPanel( aboutPanel ) );
			
			contentBox.add( new JPanel( new BorderLayout() ) );
		}
	}
	
	@Override
	public void assignUsedProperties() {
	}
	
	@Override
	public void onSelected() {
		if ( editorPane != null )
			editorPane.requestFocusInWindow();
	}
	
}
