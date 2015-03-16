# Main features of BWHF Agent #



### Autoscan ###
It can autosave your replays. It can separate hacker replays to a different folder (duplicates them). It autoscans your replays after games, and if hackers are found in them, they are read out loudly, for example: _"hacker blue protoss"_. If you have a valid BWHF authorization key, it can autoreport hackers to the central hacker database.

More about the authorization keys: [AuthorizationKeys](AuthorizationKeys.md)

More about the central hacker database: [OnlineHackerDatabase](OnlineHackerDatabase.md)

### Manual scan ###
You can scan your old replays or entire folder of replays recursively. If hacker replays are found, they can be flagged by appending 'hack' to the beginning or to the end of their names. A detailed HTML summary report can be generated at the end of scan.

### Player checker ###
Players in the game lobby can be checked whether they have been previously reported to the hacker database. If hackers found in the game lobby, it will say out loudly: _"hacker at slot x"_. The player check can be issued by pressing the _Print Screen_ key on your keyboard (works only in the game lobby). You can define your own custom list of players to be checked.

### Charts ###
Displays information about a replay and draws various charts of replays like APM (with or without EAPM), Hotkeys, Build orders, Strategy, Overall APM (with or without Overall EAPM), Action Sequences and Map view. Lists the actions of players, you can search, filter and export the actions.

Right click on the player names gives you direct access to player profile pages of many Starcraft sites.

Check out the screenshots for example charts: [Screenshots](Screenshots.md)

You can read more about APM/EAPM here: EapmRules

### Replay search ###
You can search replays by **any** of the replay header fields. In case of text fields you can even define _regular expressions_. The result list of the search can be saved and loaded. Comments can be appended to replays (saved with the list). You can perform various operations on the replays such as extract game chat, show on charts, scan for hacks, move-delete-copy-rename them, group rename them etc.

More about replay search: [ReplaySearchInBWHFAgent](ReplaySearchInBWHFAgent.md)

### Game chat ###
You can display or extract game chat and replay header informations from replays. Displaying will show them on your screen, extracting will save them to text files.

### PCX converter ###
It can automatically convert your Starcraft PCX screenshots to known formats such as JPG, PNG, GIF, BMP. If autoconvert is enabled, you will find the converted screenshot images in your Starcraft folder as if Starcraft would've saved them in that format in the first place. You can manually convert any PCX files.

There is an option to resize the converted images. The original Starcraft screenshots have a dimension of 640x480. You can specify a new width in the range of 4..1920. The height will be automatically determined to keep the aspect ratio of the original image.

### Players' Network ###
You can enable/disable to auto-add last replays to the Players' Network. You can also add any of your saved replays to the Players' Network.

More about the Players' Network: [PlayersNetwork](PlayersNetwork.md)

### Player matcher ###
You can select replays to analyze them in order to find the same players with different names. Or you can check if the same name in different replays is the same person. Player matcher compares the playing styles of the players.

More about the player matcher: [PlayerMatcherInBWHFAgent](PlayerMatcherInBWHFAgent.md)

### Server monitor ###
On the Server monitor tab you can check the online status of the battle.net servers and Starcraft websites. You can edit the server list to add any of your websites (or any other servers) or to remove sites you don't care about. Basically you can add any server that listens on a TCP port (like ftp, online video and audio streams etc.). If a server is offline, you can start monitoring it. When the server goes online again, you will hear an alert sound. You can set the re-check time interval of the monitored servers. You can open the listed web sites with a click.

### Miscellaneous ###
  * Can be minimized to system tray. Can be started minimized to tray.
  * You can set to auto-load a replay list on startup.
  * A replay can be passed as argument which will be opened on Charts tab.
  * Logs everything to text files.