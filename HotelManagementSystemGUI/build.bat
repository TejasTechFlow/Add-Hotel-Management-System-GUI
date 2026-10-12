@echo off
REM ─────────────────────────────────────────────────────────────
REM  build.bat  —  Compile and run Hotel Management System GUI
REM  Usage:
REM    build.bat          (compile + run)
REM    build.bat compile  (compile only)
REM    build.bat run      (run only)
REM ─────────────────────────────────────────────────────────────

SET MAIN_CLASS=com.hotel.HotelManagementApp
SET OUT_DIR=out
SET LIB_DIR=lib

FOR %%F IN (%LIB_DIR%\mysql-connector-j-*.jar) DO SET JDBC_JAR=%%F

IF "%JDBC_JAR%"=="" (
    echo ERROR: No mysql-connector-j-*.jar found in lib\
    echo Download from https://dev.mysql.com/downloads/connector/j/
    EXIT /B 1
)

IF "%1"=="run" GOTO RUN

:COMPILE
echo -- Compiling -------------------------------------------
IF NOT EXIST %OUT_DIR% MKDIR %OUT_DIR%
javac -cp "%JDBC_JAR%" -d %OUT_DIR% -encoding UTF-8 ^
  src\com\hotel\HotelManagementApp.java ^
  src\com\hotel\model\*.java ^
  src\com\hotel\dao\*.java ^
  src\com\hotel\service\*.java ^
  src\com\hotel\util\*.java ^
  src\com\hotel\exception\*.java ^
  src\com\hotel\ui\components\*.java ^
  src\com\hotel\ui\dialogs\*.java ^
  src\com\hotel\ui\panels\*.java ^
  src\com\hotel\ui\LoginFrame.java ^
  src\com\hotel\ui\MainFrame.java

IF %ERRORLEVEL% NEQ 0 (
    echo Compilation failed.
    EXIT /B 1
)
echo Compilation successful.
IF "%1"=="compile" GOTO END

:RUN
echo -- Launching GUI ----------------------------------------
java -cp "%OUT_DIR%;%JDBC_JAR%" %MAIN_CLASS%

:END
