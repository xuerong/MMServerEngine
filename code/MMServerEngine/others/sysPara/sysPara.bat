@echo off
cd /d "%~dp0"


..\python\python sysPara.py
copy .\com\sys\SysPara.java ..\..\src\main\java\com\sys\SysPara.java

//pause