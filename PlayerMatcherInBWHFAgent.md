# Player Matcher in BWHF Agent #
Player matcher allows you to select any replays, and it will analyze all of them and compare all players across the selected replays individually. BWHF Agent will compare players by their **playing styles**. After the analysis it displays the results in a table. The table contains the **extent of authoritativeness** of the comparisions, the **matching probability** of the 2 players and the names of the players and the replay files.

You can choose whether to compare players with same name too. The comparisions can go really high if you select many replays, so there are options to set the display thresholds of the authoritativeness and the matching probability. You can limit the number of results too.

You can filter the matches by the player names. There are 2 filters provided for this: _"Show only matches of players"_ and  _"Exclude matches with players"_. Both is a comma separated list of player names. Only those matches will be displayed where the match **does** contain a name from the first filter and **does not** contain a name from the second filter. These 2 filters can be used together or independently.


If you double click on a match with the left button, replay #1 will be opened on the charts tab. If you double click on a match with the right button, replay #2 will be opened on the charts tab. Clicking on the column headers will sort the table. Clicking again will reverse the sort order.

**BWHF Agent pairs all players from every replays with all other players from all other replays! The matching pairs grow exponentially with the number of replays. Always keep a reasonable display threshold which you can lower anytime if the result is insufficient!**

For example without display threshold: if you select 10 replays with 6 players in each, that's 60 players, each of them would be compared with 54 other players which gives us 1620 rows! With reasonable thresholds you can narrow this down to 10 which you can evaluate in seconds.

Another example: If you analyze 1000 replays and let's say all is 4v4 and all different players or you're comparing same names too, that's 8x1000=8000 players. Every player will be compared to all other 7992 players (excluded the 8 being in the same game). That is almost 32 MILLION comparisions (31,968,000 exactly)!!
(In case of 2v2 games this is 8 million!)

If you analyze that amount of replays in one shot, setting the probability to 80% and authoritativeness to good or very good is a must minimum (or you can just limit the number of results). If you're interested in some particular names only, you can filter the replays in the replay search tab with a few clicks, then select everything=>Check player matches.

## Extent of authoritativeness ##
The extent of authoritativeness tells you how much you can rely on the result of the analysis. For example comparing 2 players from 2 replays where the first player was zerg in a 2-minute long game from 5 years ago and the second player was protoss in a 30-minute long game played just today, that comparision result is good for absolutely nothing regardless what the result is.

The following properties are taken into account when determining the extent of authoritativeness:
  * **Races:** Comparing different races are a huge setback (the matching algorithm operates on action distribution which naturally changes with races).
  * **APM:** Difference in APM is an obvious negative factor.
  * **Duration:** If games differ in duration, the action distribution might change drastically (gather and micro focused on early, macro later on).
  * **Replay save time:** Playing style changes over time. Replays from different ages are not very authoritative.


## Matching probability ##
The algorithm which calculates the matching probability is mainly based on the action distribution and the used hotkeys. It calculates a number between 0 and 100. 100 means the 2 players have exactly the same playing style, 0 means they don't look similar at all.

The algorithm examines the following properties:
  * Players' start (either train first then gather, or gather first then train)
  * Hotkey actions (assing, select, add) usage
  * Select action usage
  * Shift-select action usage
  * Rally set action usage
  * Move action usage
  * Attack move action usage
  * The usage of the different hotkeys (0..9)
  * Command repetion tendency (how much the player tends to repeat the last action regardless to its parameters)
  * Average selected units