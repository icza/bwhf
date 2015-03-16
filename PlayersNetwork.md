# Players' Network™ #



Players' Network™ builds a network of Starcraft Broodwar players based on replays.
It has a web interface:

http://bwhf.net/hackerdb/players

The Players' Network automatically recognizes connections between replays where the same name is used for players. With BWHF Agent you can automatically send info about every game you play, and you can send any old replays or entire folder of replays recursively.

**Private or sensitive data is not stored on the server.** For example replays and game chat are not stored, they are not even sent to the server. Only public header information of games are sent.

**Every link in the Players Network can be cached.** For example you can send links of players, games, common game lists to others via email, or you can publish the links in forums. Everyone who opens the link will see the same page.

## Summary of what you can do with Players' Network ##
  * you can list all games
  * you can list all players
  * you can list all AKA groups
  * you can view the details of a game (with detailed info of its players)
  * you can view the details and statistics of a player, also know as the player profile
  * you can list the games of a player (with or without his/her AKAs included)
  * you can list the common games of 2 players (with or without their AKAs included)
  * you can list players who played with a specific player (with or without his/her AKAs included)
  * you can filter the players and AKA groups by player name, and you can filter games by map name

## Game list ##
You can list the games that are added to the Players' Network. You can sort them by version, date, map name, duration etc. by clicking on the columns header. The colored background in the Details column indicates the reported gateway of the game. Sorting by the Details column will sort by gateway.

The map names appear as links. If you click one of the map names, it will filter you the games to that map. If the game list is already filtered to a player's games or to the common games of 2 players, then clicking on the map name will filter to games of the players which was played on that map.

## Game details ##
You can see the details of a game if you click on its number. The game details page gives you other information of the game, and lists the players in it with other details like race, APM, actions and colors.

## Player list ##
You can list the players and filter them by name that are in the Players' Network. You can sort them by name, games count, first/last game and total time in games.

**If you click on a games count, then it will list you the games where the player was in it.**

## Player details ##
If you click on a name of a player anywhere, it will take you to the players profile.
The players profile gives you statistical information about the player such as total games, first/last game date, presence over time, total time in games, average games per day, race distribution, and a link to list the games of the player, and another link to list the players who played with him/her.

If the player has registered akas, they will be listed, and all the mentioned details will be shown when all the akas are included in the calculation.

The games count link shows you the games of the player.

If you click on the "Who played with ..." link, it will take you to the player list page where only players are listed who played with the source player. **Now if you click a games count link on this page, it will show the common games of the 2 players.** If on the profiles page you clicked on the link which includes AKAs, then the player list page will also show links leading to commong games list which also includes games played by any of the 2 players' AKAs.

The **average APM** is a weighted average. Longer games weight more in the average than short ones. For example taking 2 games: 10 min 100 APM and 20 min 200 APM, the weighted average of these 2 games is not 150 but 166.

**Random-o-meter** shows the extent of the player using all 3 races. It displays maximum value if all races are used equally, shows minimum value if only 1 race is used. The exact value between 0 and 100 is calculated with my formula:
```
Randomness = 100 - ( abs(zergGames/gamesCount - 1/3) + abs(terranGames/gamesCount - 1/3) + abs(protossGames/gamesCount - 1/3) ) * 75;
```

**Gateway distribution** shows the distribution of gateways of the games of the player where it was set when the game was added to the Players' Network.

You can see the **Top 15 maps** from games of the player. If you click on the games count here, it will show you the games that the player was in it and were played on that map.

The **Activity chart** shows the player activity, the games/month value over time.

The **APM development chart** shows the development of the average APM of the player over time.

The **Race distribution charts** show the race distribution of the player over time.

## AKA list ##
On this page you can check the registered AKAs. Every player being in the same AKA group denotes the same person behind the accounts of the group.

## Name filters ##
There is a **Filter by name** or a **Filter by map name** option on the pages. These can be used to filter the entities listed on the page to those that contain this text. An entity will be included in the filter process if the text entered here is contained anywhere in it.

For example: filter="ota" includes names like "ota", "otawe", "dakota", "dakota\_fanning".

You can use special characters to filter. The character `_` can match any character. For example: filter="b\_ta" matches the names: "bota", "albbta".

You can use the character % to match any character sequence. For example filter="ako%ing" matches the names: "dakota\_fanning", "ako23inga", "akoing".

If the filter text itself is in quotes, then a full phrase filtering is performed (full match not partial). Special characters can still be used.

**Search tip:**
Some map names contain invisible, so called _control characters_ which results in making their name appear in colors in Starcraft. If you type a name, you will not be able to enter these characters. For example: "neo the lost temple". If you enter that map name filter, you probably won't get any result. That is because there is a control character before and after the "neo" word. You can still list all the "neo the lost temple" maps if you insert the % special character to substitute any characters including the control characters: "neo%the lost temple" (without the quotes).

## Hacker database - Players' Network connection ##
If there are hacker reports with a player name displayed in the Players' Network, an "R" tag will be displayed next to the player name. This tag is a link, you can click on it and the hacker reports with that name will be listed.