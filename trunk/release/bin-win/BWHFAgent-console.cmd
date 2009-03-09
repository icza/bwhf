@echo off
%~d0
cd "%~dp0"
java -Djava.library.path=lib/win32/ -cp lib/BWHFAgent.jar;lib/pcx.jar;lib/swingwt.jar;lib/win32/swt.jar hu/belicza/andras/bwhfagent/BWHFAgent "%~f1"
