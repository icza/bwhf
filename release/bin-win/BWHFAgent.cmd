@echo off
%~d0
cd "%~dp0"
start javaw -Xmx320m -cp lib/BWHFAgent.jar;lib/pcx.jar -splash:starting_bwhf.png hu/belicza/andras/bwhfagent/BWHFAgent "%~f1"
