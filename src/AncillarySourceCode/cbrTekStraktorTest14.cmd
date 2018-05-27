REM cbrTekStraktor V04 test command file
REM

SET PYTHON_HOME=C:\temp\devtools\Python35
PATH=%PYTHON_HOME%;%PYTHON_HOME%\Scripts;%PATH%

SET KDIR=C:\temp\cbrTekStraktor\tutorial\VR
SET KPROG=%KDIR%


python %KPROG%\test14.py %KDIR%\test.jpg

pause