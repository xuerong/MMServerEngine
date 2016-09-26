@echo off
cd /d "%~dp0"

set excel_file_name=test.xlsx


..\python\python table.py %excel_file_name%

//pause