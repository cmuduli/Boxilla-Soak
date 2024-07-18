@echo off


call popd
call cd C:\Test_Workstation\SeleniumAutomation
IF EXIST "test-output\Screenshots" rmdir /s /q "test-output\Screenshots"

java -cp C:\Test_Workstation\SeleniumAutomation\lib\*;C:\Test_Workstation\SeleniumAutomation\bin -Dbrowser=%browser% -Dcount=%count% -Drelease1=%release1% -Drelease2=%release2% -Demerald=%emerald% -Demeraldse=%emeraldse% org.testng.TestNG soaktestFull.xml
REM call ant
REM call ant GenerateSeleniumReport
REM IF EXIST C:\Test_Workstation\SeleniumAutomation\Screenshots xcopy "C:\Test_Workstation\SeleniumAutomation\Screenshots" "C:\Test_Workstation\SeleniumAutomation\Screenshots\" /E