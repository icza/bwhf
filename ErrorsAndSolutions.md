# BWHF Agent errors and solutions #



## Can't start BWHF Agent? ##
First check if you have Java installed in your system. BWHF Agent requires the 32-bit version of Java 6.0 or higher. Type in a command window to check what version and if you have Java installed:
```
java -version
```
You can download and install Java here: http://www.java.com/download

You can start the program with any of the following programs/scripts:
  * `BWHFAgent.exe` (preferred)
  * `BWHFAgent.cmd`
  * `BWHFAgent-console.cmd` (opens a console window for detailed error messages; good for debugging)
Or on linux and OS-X platforms:
  * `BWHFAgent.sh` (you might need to set executable privilige first)

## I have Java installed, still can't start ##
BWHF Agent requires the 32 bit version of java. If you installed the 64 version, please install the 32 bit version too, and if neccessary, edit the starter scripts to use the 32 bit version's java.exe and javaw.exe.

Another cause can be that even if you might have java installed in your system, the java.exe and the javaw.exe commands are not available because their folder is not in the execution path.

You can fix this in 2 ways:
  * You can add the java installation folder to your `PATH` environment. In fact you have to add the bin subfolder of it where actually the java exe files are located. For example this folder can be: `"C:\Program Files\Java\jre6\bin"` Google it how to set/change the `PATH` environment variable.
  * You can edit the `BWHFAgent.cmd` and `BWHFAgent-console.cmd` scripts, and add the full path of java.exe where it is installed. Don't forget to put it in quotes if the directory names contain spaces. For example: `"C:\Program Files\Java\jre6\bin\java.exe" -Djava.library.path=...`

Tip: you can check the value of the `PATH` environment variable by typing `PATH` in a command window.

## Autoscan, PCX autoconversion or Player checker doesn't work? ##
You have to set Starcraft directory to let BWHF Agent know where to look for LastReplay.rep and screenshot files.

## Can't report hackers? ##
You have to select your gateway corresponding to where you play in order to report hackers. Moreover you need a valid autorization key. You can read about these keys here: AuthorizationKeys

## I think BWHF Agent is misbehaving, but I don't see any errors ##
Try to start the program with the `BWHFAgent-console.cmd` script on windows platform. That way you get a console where you might see some errors and details.

In a case when error or exception occurs, you are welcome to report it by creating an issue here in the [Issues](http://code.google.com/p/bwhf/issues/list) tab.

## People from the hacker list joined my game, but BWHF Agent didn't alert me ##
Check if you have the player checker enabled on the Player checker tab.
To issue a player check, you have to press the _'Print Screen'_ (or _'PrtScn'_) key on your keyboard. Player check only works in the game lobby. BWHF Agent reads the player names from the screenshot saved by Starcraft, so be sure you move your mouse out of the way (so it won't overlay player names).
BWHF Agent only notifies you of players reported with the same gateway as you set in your autoscan tab. Check if you have set the gateway and if you set the right one.

_If you use Advloader too, it disables making screenshot in the game lobby. Try it without Advloader. I don't know why it disables lobby screenshot._

If you still don't hear anything, check if your sound is not muted in BWHF Agent on the general settings tab.

If the hacker was just recently reported, your local cache of the hacker list might not yet contain it. You can initiate a manual update of your local cache of the hacker list at any time by pressing the 'Update now' button on the Player checker tab.

## I have MAC OS-X and I can't start BWHF Agent ##
I will give some hints here about how to start BWHF Agent on MAC OS-X. There is also a detailed, step-by-step instruction page, be sure to check it out: [BWHFonMAC](BWHFonMAC.md)

First thing you should know is that you need Java 6.0 or newer to run BWHF Agent. On some old MAC only Java 5.0 is available. Check out this link which helps you to install Java 6.0 on old MAC computers:

http://support.apple.com/downloads/Java_for_Mac_OS_X_10_5_Update_1

You can verify the verssion of your java by typing the following command on a console/terminal:

`java -version`

Once you confirmed you have Java 6.0, you can start the Agent by executing the `BWHFAgent.sh` starter script. You might need to give executable permission on it. You can do that if you open a terminal/console, navigate to the BWHF Agent's folder, and enter the following command:

`chmod +x BWHFAgent.sh`

If you someone can't execute the script (for example it always opens in an editor or something), then open a console/terminal, navigate to the BWHF Agent's folder, and enter the command that is inside the `BWHFAgent.sh` starter script (open it in an editor, copy the command from it and paste it to the terminal). It should start now.

If you have multiple Java installed (like 5.0 and 6.0), you might need to alter the command, and add the path to your Java 6.0 version.

**MAC OS-X test case**

Let's say you extracted BWHF Agent to the folder: `/home/joe/BWHFAgent`. This is how you can start it:
Open a terminal/console, and type the following commands:
```
cd /home/joe/BWHFAgent
chmod +x BWHFAgent.sh
./BWHFAgent.sh
```
(The `chmod` command is required only once, at the first start.)

If that fails, try this one:
```
cd /home/joe/BWHFAgent
java -Xmx320m -cp lib/BWHFAgent.jar:lib/pcx.jar hu/belicza/andras/bwhfagent/BWHFAgent
```