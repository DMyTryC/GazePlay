@echo on


java -Xms256m -Xmx1g -jar ..\lib\gazeplay-tobii-setup-${project.version}.jar


REM prints a nice "Press any key to continue . . . " message
pause
