# BWHF Agent on MAC OS-X #
![http://lh4.ggpht.com/_jDMClHrENz8/S3zqHPSjpXI/AAAAAAAASqU/aIpg24n-j4w/s800/BWHFonMAC.jpg](http://lh4.ggpht.com/_jDMClHrENz8/S3zqHPSjpXI/AAAAAAAASqU/aIpg24n-j4w/s800/BWHFonMAC.jpg)

This guide was written by teamliquid.net member [Slugbreath](http://www.teamliquid.net/forum/profile.php?user=Slugbreath). I basically copied/quoted his thread with minor modifications. This is the [original source](http://www.teamliquid.net/forum/viewmessage.php?topic_id=112589).

### Step 1 – Checking your version of Java ###
Open up the Terminal.

|The Terminal can be found in the Utilites folder that in turn is located under Applications.|
|:-------------------------------------------------------------------------------------------|

Don’t worry, you won’t have to do a lot of scary stuff in the Terminal and if I was able to figure out what to do, you’ll be able to do this too!

Once in the Terminal, write the following:

`java -version`

Now, you should get a few lines of text back but the only thing we are interested in here is the version of Java that is running.

If your version of Java is 1.6 or higher skip step 2.

### Step 2 – Setting up Java 1.6 ###
If you haven’t got Java version 1.6 or higher, you will have to go and download it from [Apple’s homepage](http://support.apple.com/downloads/Java_for_Mac_OS_X_10_5_Update_1).

Once that is downloaded and installed, go to your Java Preferences that, just like the Terminal, is located under Utilites.

Once you have started the Java Preferences you will be greated by two sets of check-boxes with different versions of Java listed. In the lower of the two, Java programs, tick Java SE 6 and drag it to the top of the list (or untick all of the other options).

Once that it done, go back to the Terminal and once again type:

`java -version`

You should now have the required version of Java installed.

### Step 3 – Download BWHFAgent ###
Download the latest version of BWHFAgent for Mac from [this link](http://code.google.com/p/bwhf/wiki/Downloads?tm=2) and unzip it. While it doesn’t matter where you place the BWHFAgent folder, I would suggest that you place it somewhere that’s easy to access.

### Step 4 – Navigate to the BWHFAgent folder ###
Now go back to the Terminal and navigate to the BWHFAgent folder. If you don't know how to do that, read in the box below.

|We will be using two (or three) commands here. The first is<br><br><code>ls</code><br><br>This command (short for list) lists all of the files in the directory you are browsing with the Terminal.<br><br>The second command we will use is<br><br><code>cd</code><br><br>which is short for change directory. The way it works is by typing<br><br><code>cd foldername</code><br><br>you will navigate to that folder (as long as the folder is located in the folder you are currently in). If the folder name contains a space use put the folder name, with spaces and all, between two “ and you’ll be find.<br><br>To go up a level, just type<br><br><code>cd ..</code><br><br>Use these commands to navigate to the folder where you placed BWHFAgent.<br><br>For example, if you have the BWHFAgent folder in the Starcraft Folder, that in turn is installed under Applications, the following lines will take you there:<br>
<tr><td><code>cd ..</code><br><code>cd ..</code><br><code>cd Applications</code><br><code>cd "Starcraft Folder"</code><br><code>cd BWHFAgent</code></td></tr></tbody></table>

<h3>Step 5 – chmod</h3>
The next step is to type in the following line into Terminal:<br>
<br>
<code>chmod +x BWHFAgent.sh</code>

<h3>Step 6 – Launch the program</h3>
Now that all the set-ups have been done, all that is left to do is to type in<br>
<br>
<code>./BWHFAgent.sh</code>

(Note the .)<br>
<br>
and BWHF should start.<br>
<br>
From now on, whenever you want to launch BWHF just repeat steps four and six.<br>
<br>
<br>
I hope that this guide was helpful. Feel free to PM me or comment if you have any questions.