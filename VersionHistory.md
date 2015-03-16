# BWHF Agent version history #



<a href='Hidden comment: ===Changes and new features in the upcoming release===
'></a>


---

### 3.30 2010-03-12 ###
  * Added detection of **auto subunit re-queue hack**.
  * Target points of recall and launch nuke actions were not marked in the map view chart. Now they are.
  * Corrected/completed unit enumeration.
  * Fixed a bug in build anywhere hack detection.


---

### 3.20 2010-02-01 ###
  * A new **MAP VIEW** chart type. This chart shows a zoomable map image (approximation), and if you select an action in the action list, it will show buildings that were built (were issued to build) up to that time. You can choose to show the **images of the buildings** or just filled rectangles. The Agent does not uses Starcraft or its files (like the MPQs) to render the map view (so you can view them without Starcraft).<br>If you click on a build command, it will <b>show with a rectange where it is targeted</b>. If you click on an action which has a target point (such as <i>move</i>, <i>attack move</i>, <i>set rally</i>, <i>casting spells</i>, <i>comsat scan</i> etc.), it will <b>show the point where it is targeted</b> in the map with a big red "X". If chart is zoomed and the target point is not visible, it will be scrolled to view.<br><i>As a general rule</i>, only buildings that are listed in the action list will be shown in the map. So if you uncheck players or you filter the actions, it will hide those buildings. For example if you filter the actions with <i>"build gateway"</i>, only the gateway build commands will be listed, and therefore only the gateways will be drawn on the map. This can be used to quickly learn/find out where others build certain buildings (for example where to build cannons, turrets etc.).<br>Since not all build commands get executed, and as the game goes on some buildings might get destroyed and new ones can be built on their place or some terran buildings can be lifted, there is an option to <b>hide overlapped buildings</b>. This option is turned on by default.<br>
<ul><li>If charts are zoomed, now you can simply <b>drag them with your mouse</b> to scroll them (scroll bars are still available). This is useful especially if you view large zoomed maps.<br>
</li><li>If you move your mouse over the charts, a live <b>tooltip text</b> will show you the exact <b>frame and time</b> of the point under the mouse cursor. In case of the map view chart the tooltip text will be the exact <b>map pixel and tile position</b> of the point under the mouse (the <i>tile position</i> is the location where buildings are/can be placed).<br>
</li><li>Now charts are finally drawn <b>double buffered</b>, which means no more flickering!<br>
</li><li>When charts are scrolled, vertical axis labels remain visible (they are drawn to fixed positions, they don't scroll with the charts).<br>
</li><li>Added a new setting on the Player checker tab: <b>Ignore the following names</b>. Names enumerated here will be completely ignored (no hacker check, no record check). By default this contains 3 trivial names: <i>"Open, Closed, Computer"</i>. But the thing is in non-English Starcraft these words are different. So non-English Starcraft users can easily adjust these to their words. For example in Spanish change this to: <i>"Abierta, Cerrado, Ordenador"</i>.<br>
</li><li>Filter label has green background to draw attention if action filter is activated.<br>
</li><li>Added new tips.<br>
</li><li>Other minor improvements and bugfixes.</li></ul>

<hr />
<h3>3.00 2010-01-14</h3>
<ul><li>Added a new chart type: <b>Action Sequences</b>. This chart visualizes how fast the players execute different action sequences. It is measured in pairs/sec (action pairs in one second). <i>Action sequences</i> are subsequent select+command pairs without being interrupted by other commands or by a specific time break, and contains at least 2 select+command pairs. For example: <i>select+move+select+move+...</i>, another: <i>hotkey select+set rally+hotkey select+set rally+...</i>.<br>These sequences are presented as bars, where the width of a bar is the duration of the sequence, the height of a bar is the average execution rate of the sequence in pairs/sec. The max allowed frame delay in sequences is a parameter and can be changed. Non-hotkey sequences are drawn with brighter color, and they can be hidden. Like any other chart, it can be zoomed, or multiple players can be shown in one chart.<br>
</li><li>Added a <b>BWHF player record checker</b> to the Player checker tab. When this feature is enabled, pressing Print Screen in the game lobby will also check the BWHF records of the players (not just whether they are hackers). You can set different alert levels: <i>New record</i> (0..9 games), <i>Small record</i> (10..99) <i>Medium record</i> (100..499 games), <i>Big record</i> (500..1999 games) and <i>Huge record</i> (2000 or more games).<br>If you set the alert level for Medium for example, for a player in the lobby who is not in the BWHF Players' Network, you will hear <i>"new at x"</i>, for a player who has 250 games you will hear: <i>"medium at x"</i> (you won't be alerted for players with more than 500 games). If you set the alert level to the max (<i>Huge record</i>), it will announce the record of all players in the lobby. It's like an automated /stats command for all players in the lobby which operates on the BWHF database.<br>
</li><li>Added check and sound alert for <b>missing gateway</b>. If you press the Print Screen in the game lobby but no gateway is set, you will hear: <i>"gateway not set"</i> instead of <i>"clean"</i>.<br>
</li><li>Autoscan now says <i>"clean"</i> if last replay contained no hacks.<br>
</li><li>Changed the 2nd colors of players on the EAPM and Action Sequences charts.<br>
</li><li>Added new recognized characters in the lobby: ` (this is not allowed anymore on battle.net, but there are still old names with this in it), ~ (this was recognized as a quotation mark), {, }, =, §<br>
</li><li>Fixed a bug on the Charts tab (when all actions in a replay happened at the first iteration and there was hack in the replay).<br>
</li><li>Added new tips.</li></ul>

<hr />
<h3>2.90 2009-11-30</h3>
<ul><li><b>A new popup menu when right clicking on the player names on the Charts tab.</b> In this menu you can open the direct profile page of the player on several web sites: BWHF Player profile, the iCCup Player profile, StarCraft Dream Player profile, Vile Gaming Player profile, Team Liquid Player profile, GosuGamers Player profile and the BWHF hacker reports page in case of a hacker.<br>
</li><li><b>A new setting on the charts tab: Zoom.</b> With this you can zoom any chart type on the time axis. Green background draws your attention if zoom is activated.<br>
</li><li>If you open a replay on the charts tab, <b>the names of reported hackers and custom listed players will be emphasized by different backgrounds</b>. The names of players who have been reported as hackers will have red background (regardless if the player hacked in the replay), and those who are on your custom list will have yellow background. Tooltip text explains the coloring.<br>
</li><li>Random tips at the bottom of the window. Clicking on the tip will show another random tip.<br>
</li><li>Action list is resynced to the marker if its content changes (this applies to changing the action filter too).<br>
</li><li><b>Added detection for auto unit re-queue hack.</b>
</li><li><b>Added detection for a new variant of the delayed autogather/autotrain hack.</b>
</li><li>Fixed a bug which might give false hack alerts in case of corrupted replays.<br>
</li><li>Some other minor bugfix.</li></ul>

<hr />
<h3>2.80 2009-10-21</h3>
<ul><li><b>Now available for MAC OS-X too!</b>
</li><li>Added 2 new buttons to the charts tab: <b>"Prev autorep"</b> and <b>"Next autorep"</b>. With these buttons you can easily navigate between the autosaved replays. The autoreplay folder is taken from the autoscan tab. These 2 buttons will jump from replay to replay based on the <b>last modification dates</b> of the files. So even if you rename the replays, it will still iterate over them in the right cronological order.<br>
</li><li>More matching probability thresholds on the player matcher tab.<br>
</li><li>Displaying game chat did not display the correct name of the replay file (always displayed the name of last replay).<br>
</li><li>Fixed a bug wich caused some layout problems when the state of the maximized window changed.<br>
</li><li>Other minor fixes and improvements.</li></ul>

<hr />
<h3>2.72 2009-10-07</h3>
<ul><li>A small bugfix in the delayed autogather/autotrain detection.</li></ul>

<hr />
<h3>2.71 2009-10-07</h3>
<ul><li>Added tooltip texts to the elements of the collapsed navigation bar.<br>
</li><li>Replaced the sound which is played when a monitored server comes back online. The new sound says: "Server is back online."<br>
</li><li>A small bugfix in the EAPM algorithm.</li></ul>

<hr />
<h3>2.70 2009-10-06</h3>
<ul><li><b>Added EAPM support: there are options on both the APM and Overall Apm charts to display EAPM and Overall EAPM.</b> The calculated EAPM values might differ somewhat from the values calculated by bwRepInfo because I used my own algorithm. The rules I used can be read here: EapmRules<br>
</li><li>APM/EAPM calculation: the first 2 minutes are ommitted, and the times of the players' last actions are used for calculation (if a player leaves after 5 minutes, the player's APM is calculated with a 3-minute long game).<br>
</li><li>Added a <b>Collapse</b> icon to the navigation bar. If you click on it, the navigation bar will collapse and show only the icons, leaving more space for charts, tables etc. If you click on it again, it will restore the navigation labels too.<br>
</li><li><b>Added detection of the delayed autogather/autotrain hack.</b>
</li><li>Added game info text to the Charts tab.<br>
</li><li>Added help text about opening replays on the Player matcher tab.<br>
</li><li>Added sub-version numbers on the About tab.</li></ul>

<hr />
<h3>2.61 2009-08-26</h3>
<ul><li>Map size was taken from the map data section of the replays which contains 0 width and height in some cases. In these cases the data recorded in the replay header is used. This fixes false build anywhere hack alerts.</li></ul>

<hr />
<h3>2.60 2009-08-26</h3>
<ul><li>Changed the horizontal tab bar to a vertical navigation panel.<br>
</li><li>A new <b>Server monitor</b> tab. On this tab you can check the online status of the battle.net servers and Starcraft websites. You can edit the list to add any of your websites (or any other servers) or to remove sites you don't care about. Basically you can add any server that listens on a TCP port (like ftp, online video and audio streams etc.). If a server is offline, you can start <b>monitoring</b> it. When the server goes online again, you will hear an alert sound. You can set the re-check time interval of the monitored servers. You can open the listed web sites with a click.<br>
</li><li>New filters on the Player matcher tab: <i>"Show only matches of players"</i> and <i>"Exclude matches with players"</i>. You can enter a comma separated list in each. The first one will filter out matches that <b>do not</b> contain any of the players entered here. The second filter will filter out matches that <b>do</b> contain any of the players entered here. These 2 filters can be used together or independently. The excluder filter is the stronger one.<br>
</li><li>New setting on the Player matcher tab: <i>"Max displayable results"</i>. If the provided thresholds allow more results, you don't have to worry about the Agent hanging or working too long. The highest matches with the best authoritativeness will be kept and the worse results will be thrown away.<br>
</li><li>Added a startup splash image.<br>
</li><li>Added hand cursor to the chart canvas.<br>
</li><li>Fixed a bug which returned a false map size if it was a non-standard size (now it's read from the map data section, the replay header contains wrong value).</li></ul>

<hr />
<h3>2.50 2009-08-04</h3>
<ul><li>A new <b>Player matcher</b> tab. On this tab you can select replays or folder of replays, and BWHF Agent will analyze the replays, compare all players across all selected replays, and display the matching probability for the players. Replays and their players are compared individually, statistics of a specific player across multiple replays is not processed.<br>
</li><li>New setting on the PCX converter tab: <b>Resize converted images</b>. With this you can change the size of the converted images. This can be useful if you want to post the screenshots on forums where the allowed images are limited (either in dimension or in size).<br>
</li><li>New operation in the context menu of replay search tab: "Check player matches".<br>
</li><li><b>Detection of subunit (scarab/interceptor) enqueue hack.</b>
</li><li>Bugfix in replay search (did not handle latest save date).<br>
</li><li>Optimizations and other minor bugfixes.</li></ul>

<hr />
<h3>2.42 2009-06-29</h3>
<ul><li>Replay autosave function (and therefore autoscan and report) was not working properly in version 2.40 and 2.41. This is fixed now.</li></ul>

<hr />
<h3>2.41 2009-06-29</h3>
<ul><li>Autosending info about last replay did not include gateway information. It is added now.</li></ul>

<hr />
<h3>2.40 2009-06-29</h3>
<ul><li>A new <b>Players' Network</b> tab where you can add any old replays (or folder of replays recursively) to the Players' Network. You can enable/disable autosending info about 'LastReplay.rep'. Sending twice exactly the same replay will only appear in the database once.<br>
</li><li>New operation in the replay search tab's context menu: "Send to Players' Network".<br>
</li><li>New setting on the autoscan tab: 'Use short names for autosaved replays'.<br>
</li><li>Fixed a bug which prevented game chat extraction from games containing computer players.<br>
</li><li>Fixed some rendering bugs wich occured on Windows Vista.<br>
</li><li>Some other minor fixes.</li></ul>

<hr />
<h3>2.30 2009-05-12</h3>
<ul><li><b>Added new icons</b> to tabs and buttons (user interface is much more friendly this way).<br>
</li><li>New <b>Previous replay</b> and <b>Next replay</b> buttons on the charts tab to navigate easily between the replays of the replay search tab. If there are more than 1 replays selected, the buttons will iterate through the selected replays. If there are no selected replays, these buttons will iterate through all replays of the result table.<br>
</li><li>New setting on the player checker tab: <b>Say "clean" if no hackers found</b>. This way you can be sure a player check was performed but no hackers found in the game lobby.<br>
</li><li>A new <b>"Column setup..."</b> button on the replay search tab to customize replay columns order.<br>
</li><li>Clicking on the column headers in the replay search tab will <b>sort the table</b>. Clicking again on the same column will reverse the order.<br>
</li><li>New setting on the charts tab: <b>Display actions in seconds</b>.<br>
</li><li>Removed the replay handler buttons from the search tab, and added a context menu (right click popup menu) to the result table. You can find here all operations that were available through the buttons. This gives more space to the result table.<br>
</li><li>New setting on the general settings tab: <b>Replay list to load on startup</b>. Note that if you select a big list with thousands of replays, it might significantly slow down the startup of BWHF Agent.<br>
</li><li>Hide search filters state is now saved and remembered on next startup.<br>
</li><li>New replay operation: <b>Edit comment</b>. Comments can be added to replays in the result list. The comments are saved in the replay list files.<br>
</li><li>New replay operation: <b>Rename replay</b>.<br>
</li><li>New replay operation: <b>Group rename replays</b>. Group rename operates on a template. This template can contain any text and special symbols. Special symbols will be substituted with proper text when renaming specific replays. Valid symbols are: <b>/n</b> - the name of the original replay; <b>/e</b> - the extension of the original replay (usually 'rep'); <b>/c</b> - a counter which starts from 1 and will be incremented by 1 at each use. With this you can easily rename many replays. Let's say you have 20 tournament replays. You can select those replays and click on group rename. Give a template of <i>"Tourney reps /c./e"</i>. This will rename all those reps to <i>"Tourney reps 1.rep"</i>, <i>"Tourney reps 2.rep"</i> etc. You can preview the new names before proceeding to rename.<br>
</li><li>New replay operation: <b>Open replay's folder in explorer</b>.<br>
</li><li>Modified the <b>build anywhere</b> hack detection to recognize more cases.<br>
</li><li>Some minor bugfixes.</li></ul>

Note that if you make changes on the result list such as edit/add a comment, sort the table or rename replays, you have to save the list to keep the changes.<br>
<br>
<hr />
<h3>2.21 2009-04-24</h3>
<ul><li>Modified the Player checker to properly recognize game lobby screenshots (apparently there are minor variations of the picture of game lobby on different computers).<br>
</li><li>New setting on the Player checker tab: "Echo recognized player names in the log below".<br>
</li><li>Fine-tuned the general multicommand hack detection (fixes a false alert).</li></ul>

<hr />
<h3>2.20 2009-04-21</h3>
<ul><li>A new <b>Player checker</b> tab where you can enable <b>checking players in the game lobby BEFORE game</b>. If players in the game lobby has already been reported as hackers, you will get an alert sound indicating the slots of the hackers you're in the same game with. For example: <i>"hacker at slot 3"</i>. This check is implemented in a way that BWHF Agent <b>still remains legit</b>, BWHF Agent obtains the player names <b>WITHOUT</b> reading Starcraft's memory (uses image processing and text recognition)! A local cache of the hacker list is kept in the Agent's folder which is automatically updated in an interval of your choice or at will at anytime. The player check will conform to the gateway set in you autoscan tab and to the reported gateways of the hackers.<br>
</li><li>On the Player checker tab you can choose <b>your own extra list of players</b> who you want to check whether they are in the same game as you. You can use this list to enumerate players who you think hack or you don't want to play with or you just want to remember them but they are not int the BWHF hacker database. The format of the custom player list file is very simple: every line contains a player, the line starts with a number identifying the gateway and after that the player name separated with a comma. Valid gateways are: 0-USEast, 1-USWest, 2-Europe, 3-Asia, 4-iCCup, 5-Other<br>
</li><li><b>Autoscan now reads out loud the hackers' colors and races</b>, so you will know who hacked in your last game without switching (alt+tab) to BWHF Agent. For example: <i>"hacker blue protoss"</i>.<br>
</li><li>Added 2 new buttons to the Replay search tab: <b>Save result list</b> and <b>Load result list</b>. With these buttons you can create your own quickly accessible replay lists like tournament replays, replays of a specific player, a list with all your replays etc. After loading a list, you can filter it, export replays, open on charts etc. The saved replay list is a single tab separated list, <b>you can open it with Excel</b> for example to do whatever you want with it.<br>
</li><li>Added a new 'Append results to table' setting on the replay search tab. If this is checked, the search result will simple be added to the table, the previous result will not be cleared. If search fields are not changed, all selected replays will simple be added to the previous results.<br>
</li><li>New setting on the general settings tab to set the default replay lists folder.<br>
</li><li>New setting on the general settings tab to select a program to view/edit files.<br>
</li><li>Added a new 'View entire log' button to all tabs which have a log text area. This button opens the proper log file in the editor set in the general settings tab.<br>
</li><li>If updates are available, the window icon and the tray icon is changed to a blue pill image.<br>
</li><li>Some other minor bug fixes.</li></ul>

<hr />
<h3>2.00 2009-04-01</h3>
<ul><li>New <b>System Tray</b> icon for BWHF Agent. You can control BWHF Agent through the system tray: start Starcraft, change gateway, operate on LastReplay.rep (like show on charts, display game chat, scan for hacks), hide/restore the main window, disable the tray icon, close the agent.<br>
</li><li>A new <b>Replay search</b> tab. You can search replays by any of the header fields (including game engine, game name, creator name, map name, player name, player race, <i>player color</i>, duration, save date, <i>version</i>, map size, <i>game type</i>). Text fields can be filtered as substrings or as exact match. You may enter comma separated lists, or you can even use <i>regular expressions</i> to filter. Search result can be searched again (narrowing the results), can be scanned, shown on charts, copied, moved, deleted, or game chat can be extracted from them.<br>
</li><li>A new <b>Strategy</b> chart type. This chart is similar to the build orders chart. For now it displays the following strategy actions: <i>Expand</i> (when Nexus, CC or Hatchery is built), <i>Defense</i> (when Bunker, Cannon or Sunken is built), <i>Drop</i> (when unload commands are given), <i>Recall</i>, <i>Nuke</i> (when Nuke is launched) and <i>Nydus</i> (when Nydus is built).<br>
</li><li>A new <b>Overall APM</b> chart type. This chart displays the overall APM from the beginning of the game at every time. You can see how different game phases (like micro in the early, macro in the late) changes players' APM in overall. Ascendent chart means the player becomes more active later when more units and macro come into play, descendent chart means the bigger part of the players' APM come from early game (probably spamming when there are less things to do). Even though these things can be read from the APM chart, this is a new way to look at it. Since overall APM chart is quite flat compared to the momentary APM chart, this gives a much cleaner look if all players are displayed on the same chart.<br>
</li><li>Settings on the general settings tab to enable/disable the system tray, to minimize window always to system tray and to start the agent minimized to tray. This can be useful if you want to autostart the agent when you log in to windows (linking BWHF Agent into your Startup menu for example). That way you can be sure the agent will be running and will scan your replays even if you start Starcraft with another launcher. But starting Starcraft will be available with just 2 clicks from your tray icon.<br>
</li><li>Added a button to minimize to tray below the window state changer buttons.<br>
</li><li>Added a button on the game chat tab to display game chat from any replay file (not just  'LastReplay.rep'). Extracting game chat from replays to text files is still available.<br>
</li><li>Setting to enable/disable saving and remembering window position.<br>
</li><li>Added a new icon to the window and to the starter <code>BWHFAgent.exe</code> file.<br>
</li><li>Regrouped user interface components.<br>
</li><li>Extracted game chat now contains version information.<br>
</li><li><b>Detection of use cheat drophack.</b>
</li><li><b>Detection of some build anywhere hack.</b></li></ul>

<hr />
<h3>1.51 2009-03-18</h3>
<ul><li>Bugfix: added UMS game type check for invalid ally and vision commands (invalid ally-vision commands are possible in UMS-type games).</li></ul>

<hr />
<h3>1.50 2009-03-17</h3>
<ul><li>Players' action list is now displayed in the charts tab. Action list or any part of it <b>can be exported</b> simply by selecting and coping it to the clipboard.<br>
</li><li>Added a <b>Jump to iteration</b> option to jump to any iteration or one being <i>close</i> to it.<br>
</li><li>Added a universal <b>Search text</b> option to the action list. The search text can be multiple words or a fraction of a word and can aim anything that is visible in the action list: player name, action name, unit name, upgrade etc.<br>
</li><li>Added a <b>Filter actions</b> option to filter the displayed actions. This option filters out the actions that do not contain the entered filter text. This can be used for example to filter down to any select actions (like Select, Shift Select, Shift Deselect, Hotkey Select etc.) or to find any actions that relates to Zealot (Train Zealot, Upgrade Zealot speed). The filter text might contain several words which will be in logical AND connection by default. Writing out AND is not needed but it's not a syntax error. The filter terms: "<code>train zealot</code>" and "<code>train and zealot</code>" are equal. However you can use logical OR connection too by explicitly writing <code>or</code> between words. These 2 logical operators (AND and OR) can be combined in any way. For example the result of setting the filter text: "<code>hatch or train zealot or dragoon</code>" will display only those actions that contain the single word <code>hatch</code> <i>or</i> contain both words <code>train</code> <i>and</i> <code>zealot</code> <i>or</i> contains the single word <code>dragoon</code>. Note that the logical AND has a higher precedence than OR.<br>
</li><li><b>Replaced the starter <code>BWHFAgent.exe</code> file</b> because the old one was flagged as a trojan by some minor antivirus programs.<br>
</li><li>If a replay file is passed to any of the starter scripts or exe, it will be opened in the charts tab by default. If you associate replay files with one of BWHF Agent's script files, the replay will be opened in the charts tab if you double click on the replay (this only works with the script files).<br>
</li><li>Detection of invalid ally and vision commands which can be used to drop players.<br>
</li><li>Components are now centered properly when window is enlarged.</li></ul>

<hr />
<h3>1.40 2009-03-04</h3>
<ul><li>A new <b>Charts</b> tab. Setting for visalizing all players on separate or on one chart with any chart type (gives the possibility to compare the speed of players on the Build order chart for example). Setting to draw charts using players' in-game colors to easily associate. Setting for auto-disabling players with less than 30 APM.<br>
</li><li><b>APM</b> chart to visualize players' APMs customizable by detail level in pixels. Detected hack actions are marked in the charts.<br>
</li><li><b>Hotkeys</b> chart to show players' hotkey usage. Setting for showing/hiding select hotkeys.<br>
</li><li><b>Build order</b> chart to visualize buildings and units built/trained by the players. Setting to show/hide units in the charts. Setting for showing/hiding worker units (such as SCV, probe, drone). Note that only the build/train/hatch actions are visualized which are not neccessarily the same as the buildings/units that really get built/trained. Setting for changing the build order display levels.<br>
</li><li><b>Now available for Linux too!</b>
</li><li>New setting on the general settings tab: "Start folder when selecting replay files". This folder will be the start location when you want to select replay files on any tab.<br>
</li><li>Extracting game chat now contains more info about players: their in-game colors and the number of their actions (along with their races and APMs).<br>
</li><li>Updated the scan engine to detect a new variation of autogather/multicommand hack.<br>
</li><li>Fixed a bug which caused the agent's window to "blink" or resized when some button was pressed ("Check key" example).</li></ul>

<hr />
<h3>1.30 2009-02-18</h3>
<ul><li>New setting to create and open a <b>detailed HTML summary report</b> at the end of manual scan. These HTML reports are also saved in the <code>HTML reports</code> folder so you can view them later or share them on the internet.<br>
</li><li>Added a button to the manual scan tab to open previous HTML reports.<br>
</li><li>New setting to clean the 'hack' flag from replays where no hackers were found during the scan on the manual scan tab.<br>
</li><li>Added a <b>progress bar</b> to the manual scan, PCX converter and Game chat tabs.<br>
</li><li>Game chat extraction now contains more info about players (race and APM).<br>
</li><li>Fixed a bug which occasionally caused <code>LastReplay.rep</code> to be saved and scanned (and reported hackers if found) twice when changed.<br>
</li><li>Players using latency changer program might occasionally got reported using autogather/autotrain hack. This is no longer the case.<br>
</li><li>Logged text areas have more descriptive titles.<br>
</li><li>Renamed <code>settings.properites</code> to <code>settings.properties</code>.</li></ul>

<hr />
<h3>1.20 2009-02-03</h3>
<ul><li>A new <b>PCX converter</b> tab which can convert your Starcraft PCX screenshot files to JPG, GIF, PNG or BMP image files. The tab has an auto converter feature which automatically converts your Starcraft screenshots so you don't have to do it manually. You will find the JPG (or PNG or GIF or BMP) screenshots in your Starcraft directory right away as if Starcraft would save the screenshots in JPG format (or in the format of your choice) in the first place.<br>
</li><li>A new <b>Game chat</b> tab which can extract replay header information and game chat from replays and save them to text files.<br>
</li><li>It is displayed if Starcraft folder is set correctly, and the 'Start/Switch to Starcraft' button is enabled based on that.<br>
</li><li>Moved <code>BWHFAgent.jar</code> to the <code>lib</code> folder to avoid launching misunderstanding and added a <code>BWHFAgent.exe</code> starter program.<br>
</li><li>Fixed an error which caused scanning a replay with no actions to fail.<br>
</li><li>Fine-tuned the general multicommand hack detection to fix 2 false alerts.<br>
</li><li>Updated the binary replay parser to recognize and parse game chat commands properly (which was introduced in Starcraft version 1.16).</li></ul>

<hr />
<h3>1.10 2009-01-12</h3>
<ul><li>Added option to define the position (beginning or end) where to flag hacker replays on the Manual scan tab.<br>
</li><li>Added volume control setting for alert sounds.<br>
</li><li>Handling a kind of replay corruption (fixes a false alert).<br>
</li><li>Hack report at the end of manual scan is built with no case sensitivity in hacker's name, and the list is sorted by name.</li></ul>

<hr />
<h3>1.00 2008-12-30</h3>
<ul><li>Fixed a bug which caused the scan process to hang when tried to scan a replay with actions starting at iteration 0.<br>
</li><li>Fixed a false multicommand hack alert.<br>
</li><li>Changed the generated names of the autosaved replays (the previous one was too long and Starcraft couldn't play it without renaming).</li></ul>

<hr />
<h3>0.99 2008-12-28</h3>
The first public release.