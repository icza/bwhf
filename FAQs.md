# Frequently Asked Questions #



If you have other common questions, post them here and I might include them.

### Q: Does BWHF Agent detect only autogather/autotrain? ###
A: Hell no! By default, the Agent stops scanning further actions of a player if a hack has been identified. And since autogather/autotrain is the first hack it looks for, that is the most common hack that it will alert for. If you want to know the full hack list that were detected, you can disable this _fast scan_ by unchecking the "During a replay scan if a player is found hacking..." option on the general settings tab.

### Q: Could it be that someone is alerted for hacking because of lag? ###
A: No. Lag is known and is built into the scan engine.

### Q: Does BWHF Agent detect maphacks? ###
A: Some but not all by far.

### Q: Is BWHF Agent allowed on iCCup? ###
A: Yes. Since it does not interact with Starcraft at all (it does not read or write Starcraft's memory), it can be used anywhere with or without any other launchers.

### Q: Is latency changer (not Chaos LAN latency) considered a hack? ###
A: Not by the Agent.

### Q: I run the Agent and it detected a hacker but I can't find him in the database. Why? ###
A: There can be several reasons:
  * You don't have a valid authorization key to report hackers.
  * You have not enabled reporting hackers.
  * Manual scan does not report. Only autoscan.
  * The server might have been down when you detected the hacker. You can check this in your "autoscan.log" file.
  * The hacker you reported might have been removed (due to spoof or key revocation or being an invalid report).
  * The player you reported might be guarded (white-listed).

### Q: Why doesn't work the player checker for me in the game lobby? ###
A: The 3 main important things to keep in mind about player checker:
  1. you have to press the Print Screen key
  1. it will only alert for hackers reported with the same gateway you have set on your autoscan tab
  1. as far as I know Advloader disables making screenshot in the lobby, so it probably won't work with Advloader on. For more information check out the last section on this page: ErrorsAndSolutions

### Q: A hacker got recently reported, he is on the hacker list, but player checker did not alert him in the lobby. Why? ###
A: If the player checker works for you, then these can be the reasons:
  * If he got reported recently, your local cache of the hacker list might not yet contain his name. It is updated automatically, or you can issue an update at any time by pressing the "Update now" button on the Player checker tab.
  * If he was reported not so recently, you might had some trouble reacing the BWHF hacker database when your Agent tried to update your local cache (either you had a connection blackout or the server was down). Try to update it manually with the "Update now" button on the Player checker tab.
  * The player might be guarded (white-listed).

### Q: I used the player checker feature and it said: "possible hacker at slot x". What does this mean? ###
A: In the game lobby Starcraft uses a font in which the capital i and the small L looks the same, there is no way to differentiate between them just by looking at them. In cases when a name detected in the lobby which consists some capital i's or small L's and at least one of the variations is on the hacker list, it will say _"possible hacker at slot x"_.

### Q: Is there a way to view hackers that I reported? ###
A: Yes. On the hacker database page there is a filter: "reported with key". Enter your key there, and reports will be filtered to your key only.

### Q: My name is on the hacker list but I don't hack. What can I do? ###
A: If you never hack but you're on the hacker list, contact me and try to convience me. My gmail user name is **iczaaa**. Drop me an email.

### Q: Does BWHF Agent detect spoofs? ###
A: No. BWHF Agent operates on replays, so there is no way to detect spoofs based on replays.

### Q: What does the "R" tag mean next to some player names in the Players' Network? ###
A: It means there are hacker reports with that name. The "R" tag is a link, you can click on it and it will list the hacker reports with that name.

### Q: I applied for a key days ago but I haven't received any response. ###
A: I reply to everyone who applies for a key. Check your spam folder, maybe the response was put there. Or it could that you gave a wrong email address and I was not able to contact you.

### Q: I'm using a laptop and I can't use the Print screen key. Is there a workaround? ###
A: Yes. You can remap the Print Screen key to another one that is comfortable for you on your computer. You have to use an external program for this, for example KeyTweak or SharpKeys.