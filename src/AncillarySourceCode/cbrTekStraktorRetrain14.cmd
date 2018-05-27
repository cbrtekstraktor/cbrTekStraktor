REM cbrTekStraktor V04 retrain all command file
REM

SET PYTHON_HOME=C:\temp\devtools\Python35
PATH=%PYTHON_HOME%;%PYTHON_HOME%\Scripts;%PATH%

SET KDIR=C:\temp\cbrTekStraktor\tutorial\VR
SET KSTART=%KDIR%
SET KPROG=%KDIR%
SET KDATA=%KDIR%\comics

python %KPROG%\retrain14.py --bottleneck_dir=%KSTART%\bottlenecks --how_many_training_steps 500 --model_dir=%KSTART%\inception --output_graph=%KSTART%\comics_graph.pb --output_labels=%KSTART%\comics_labels.txt --image_dir=%KDATA%


pause