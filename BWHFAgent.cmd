@%~d0
@cd "%~dp0"
java -Djava.library.path=lib/win32 -cp bin;hackerdb/webapp-root/WEB-INF/classes;lib/pcx.jar;lib/swingwt.jar;lib/win32/swt.jar hu/belicza/andras/bwhfagent/BWHFAgent %1
