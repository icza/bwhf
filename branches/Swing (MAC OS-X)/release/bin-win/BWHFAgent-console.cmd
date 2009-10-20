@echo off
%~d0
cd "%~dp0"
java -Xmx320m -cp lib/BWHFAgent.jar;lib/pcx.jar -splash:starting_bwhf.png hu/belicza/andras/bwhfagent/BWHFAgent "%~f1"
