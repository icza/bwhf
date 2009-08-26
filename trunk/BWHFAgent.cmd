@%~d0
@cd "%~dp0"
java -Xmx320m -Djava.library.path=lib/win32 -cp bin;hackerdb/webapp-root/WEB-INF/classes;lib/pcx.jar;lib/swingwt.jar;lib/win32/swt.jar -splash:starting_bwhf.png hu/belicza/andras/bwhfagent/BWHFAgent "%~f1"
