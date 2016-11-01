@echo off
cd /d "%~dp0"
:: excel文件
set excel_file_name=test.xlsx


..\python\python table.py .\tables\%excel_file_name%

//pause