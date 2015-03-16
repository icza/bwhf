# EAPM Rules #

The APM/EAPM charts calculates and shows APM and EAPM values based on my algorithm. APM stands for Actions Per Minute. APM measures the amount of commands that would be given by a player in 1 minute. Higher APM means more activity. EAPM stands for Effective APM. EAPM measures the amount of useful/effective commands that would be given by a player in 1 minute.

APM calculation is pretty simple: one just have to divide the number of actions with the number of minutes where the actions spread in time. For example 200 actions in 1 minute results in a 200 APM. EAPM calculation is similar, but it excludes actions that are seemingly useless/needless. The algorithm uses different rules to classify actions _effective_ or _ineffective_. The first 2 minutes of games are always excluded from APM/EAPM calculation. Moreover the durations for APM/EAPM calculations are determined by the last actions of the players. For example if a player leaves after 5 minutes, his/her APM and EAPM values will be calculated with a 3-minute long game.

**Commands that are considered to be _ineffective_:**

  * Unit queue overflow: if more than 5 units/subunits are trained in a short time (~1.05 sec)
  * Too fast cancel (~0.84 sec): if a train, research, upgrade, hatch command gets cancelled right away
  * Too fast repetition of some commands (~0.42 sec): move, attack move, set rally, stop, hold, hotkey assign
  * Too fast switch away from a selected unit or reselecting the same units without giving them any commands (~0.336 sec) (by too fast I mean there is not even time to check the state of the units and optionally react to it accordingly); double tapping a hotkey to center a group of units is NOT considered ineffective
  * Repetition of some commands (without time restriction): hatch, morph, upgrade, research, build, cancel, merge archon, merge dark archon, lift, hotkey assign to same group

**Commands that are _NOT_ considered ineffective:**
  * Double tapping a hotkey to center the group
  * Selecting unit(s) or building to check its state but not giving new order to it.
  * I do not check the target point of move commands: if they are close in time, the first is ineffective (unneccessary); if they are not close in time, they are both effective even if they point to the same location (issuing another move will cause Starcraft to recalculate path and therefore resulting in faster arriving in many cases).