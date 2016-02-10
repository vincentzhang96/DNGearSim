@ECHO OFF

CD winrun4j
COPY WinRun4J64.exe DNGearSim.exe
RCEDIT64.exe /N DNGearSim.exe DNGearSim.ini
RCEDIT64.exe /S DNGearSim.exe splash.bmp
RCEDIT64.exe /I DNGearSim.exe icon.ico
COPY "DNGearSim.exe" "../../runtime/DNGearSim.exe"

CD ../..
CD Bootstrap/target
COPY "bootstrap.jar" "../../runtime/bootstrap.jar"

PAUSE