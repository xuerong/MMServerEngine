@echo off
cd /d "%~dp0"

set proto_file_name=LivePB.proto
set start_index=11001

set dir=csharpfile\

set opcode_class_name=csharpfile\%proto_file_name:PB.proto=Opcode.cs%
set csharp_class_name=csharpfile\%proto_file_name:proto=cs%
set builder_class_name=csharpfile\%proto_file_name:.proto=Builder.cs%
::set builder_class_name=AA.cs
del %csharp_class_name%

::CodeEngine.exe -i:..\protos\%proto_file_name% -o:%csharp_class_name% -c:csharp
.\csharp-tools\CodeGenerator.exe ..\protos\%proto_file_name% --output=%csharp_class_name%

python idcreator.py ..\protos\%proto_file_name% %opcode_class_name% %start_index% %builder_class_name%


::pause