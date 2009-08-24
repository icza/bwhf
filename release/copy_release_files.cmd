@echo off

%~d0
cd "%~dp0"

SET RELEASE_DIR="T:\BWHFAgent-release"

IF NOT EXIST %RELEASE_DIR% md %RELEASE_DIR%

IF EXIST %RELEASE_DIR% goto copyfiles
echo Cannot create directory: %RELEASE_DIR%!
goto quit

:copyfiles
echo Copying BWHFAgent.jar files...
xcopy /E /Q ..\bin\hu %RELEASE_DIR%\BWHFAgent.jar\hu\
xcopy /Q ..\hackerdb\webapp-root\WEB-INF\classes\hu\belicza\andras\hackerdb\ServerApiConsts.class %RELEASE_DIR%\BWHFAgent.jar\hu\belicza\andras\hackerdb\
xcopy /Q resources\META-INF\MANIFEST.MF %RELEASE_DIR%\BWHFAgent.jar\META-INF\
echo .
echo Copying Windows files...
xcopy /Q bin-win\*.* %RELEASE_DIR%\win\BWHFAgent\
xcopy /Q ..\sounds\*.* %RELEASE_DIR%\win\BWHFAgent\sounds\
xcopy /Q ..\lib\*.jar %RELEASE_DIR%\win\BWHFAgent\lib\
xcopy /Q ..\lib\win32\*.* %RELEASE_DIR%\win\BWHFAgent\lib\win32\
xcopy /Q ..\starting_bwhf.png %RELEASE_DIR%\win\BWHFAgent\
echo .
echo Copying Linux files...
xcopy /Q bin-linux\*.* %RELEASE_DIR%\linux\BWHFAgent\
xcopy /Q ..\sounds\*.* %RELEASE_DIR%\linux\BWHFAgent\sounds\
xcopy /Q ..\lib\*.jar %RELEASE_DIR%\linux\BWHFAgent\lib\
xcopy /Q ..\lib\linux_gtk2\*.* %RELEASE_DIR%\linux\BWHFAgent\lib\linux_gtk2\
xcopy /Q ..\starting_bwhf.png %RELEASE_DIR%\linux\BWHFAgent\
echo .
echo Copying MAC files...
xcopy /Q bin-mac\*.* %RELEASE_DIR%\mac\BWHFAgent\
xcopy /Q ..\sounds\*.* %RELEASE_DIR%\mac\BWHFAgent\sounds\
xcopy /Q ..\lib\*.jar %RELEASE_DIR%\mac\BWHFAgent\lib\
xcopy /Q ..\lib\macosx_carbon\*.* %RELEASE_DIR%\mac\BWHFAgent\lib\macosx_carbon\
xcopy /Q ..\starting_bwhf.png %RELEASE_DIR%\mac\BWHFAgent\

:quit
pause
