package hu.belicza.andras.bwhfagent.view;

import hu.belicza.andras.bwhf.control.ReplayScanner;
import hu.belicza.andras.bwhfagent.Consts;
import hu.belicza.andras.bwhfagent.view.charts.EapmUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
		button = new JButton( "Visit Players' Network", IconResourceManager.ICON_WORLD_GO );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.showURLInBrowser( Consts.PLAYERS_NETWORK_PAGE_URL );
			}
		} );
		buttonsPanel.add( button );
		button = new JButton( "Visit Sc2gears home page", IconResourceManager.ICON_WORLD_GO );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				Utils.showURLInBrowser( Consts.SC2GEARS_HOME_PAGE_URL );
			}
		} );
		buttonsPanel.add( button );
		contentBox.add( buttonsPanel );
		
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
		editorPane.addHyperlinkListener( new HyperlinkListener() {
			public void hyperlinkUpdate( final HyperlinkEvent event ) {
				if ( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
					if ( event.getURL() != null )
						Utils.showURLInBrowser( event.getURL().toString() );
			}
		} );
		
		final JScrollPane wrapperScrollPane = new JScrollPane( editorPane );
		contentBox.add( wrapperScrollPane );
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
