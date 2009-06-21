@echo off

%~d0
cd "%~dp0"

SET WAR_DIR="T:\hackerdb2"

IF NOT EXIST %WAR_DIR% md %WAR_DIR%

IF EXIST %WAR_DIR% goto copyfiles
echo Cannot create directory: %WAR_DIR%!
goto quit

:copyfiles
copy webapp-root\*.* %WAR_DIR%
xcopy /Q webapp-root\WEB-INF\*.* %WAR_DIR%\WEB-INF\
xcopy /Q webapp-root\WEB-INF\lib\*.* %WAR_DIR%\WEB-INF\lib\
xcopy /Q webapp-root\WEB-INF\classes\hu\belicza\andras\hackerdb\*.* %WAR_DIR%\WEB-INF\classes\hu\belicza\andras\hackerdb\
xcopy /Q ..\bin\hu\belicza\andras\bwhf\model\ReplayHeader.class %WAR_DIR%\WEB-INF\classes\hu\belicza\andras\bwhf\model\


:quit
pause
