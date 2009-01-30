@echo off

%~d0
cd "%~dp0"

SET RELEASE_DIR="T:\BWHFAgent"

IF NOT EXIST %RELEASE_DIR% md %RELEASE_DIR%

IF EXIST %RELEASE_DIR% goto copyfiles
echo Cannot create directory: %RELEASE_DIR%!
pause
goto quit

:copyfiles
xcopy /Q BWHFAgent.cmd %RELEASE_DIR%
xcopy /Q BWHFAgent-console.cmd %RELEASE_DIR%
xcopy /E /Q ..\bin\hu %RELEASE_DIR%\BWHFAgent.jar\hu\
xcopy /Q ..\hackerdb\webapp-root\WEB-INF\classes\hu\belicza\andras\hackerdb\ServerApiConsts.class %RELEASE_DIR%\BWHFAgent.jar\hu\belicza\andras\hackerdb\
xcopy /Q ..\sounds\*.* %RELEASE_DIR%\sounds\
xcopy /Q ..\lib\*.* %RELEASE_DIR%\lib\
xcopy /Q ..\lib\win32\*.* %RELEASE_DIR%\lib\win32\
xcopy /Q META-INF\MANIFEST.MF %RELEASE_DIR%\BWHFAgent.jar\META-INF\

:quit
