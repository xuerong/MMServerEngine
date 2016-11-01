@echo off
cd /d "%~dp0"

set proto_file_name=LivePB.proto
::set opcode_class_name=LiveOpcode.java
set start_index=11001

set opcode_class_name=%proto_file_name:PB.proto=Opcode.java%

set java_class_name=%proto_file_name:proto=java%

protoc.exe --java_out=./ .\protos\%proto_file_name%
copy .\com\protocol\%java_class_name% ..\..\src\main\java\com\protocol\%java_class_name%

..\python\python idcreator.py .\protos\%proto_file_name% %opcode_class_name% %start_index%
copy .\com\protocol\%opcode_class_name% ..\..\src\main\java\com\protocol\%opcode_class_name%

::pause