#!/usr/bin/python
#-*- coding: utf-8 -*-

#CSharp所需要的MessageID文件

import os
import sys

proto_file_name = sys.argv[1]
cs_class_name = sys.argv[2]
start_index = int(sys.argv[3])
builder_class_name = sys.argv[4]

proto_file = open(proto_file_name,"r")
lines = proto_file.readlines()

if os.path.exists(cs_class_name):
	os.remove(cs_class_name)

# 初始化CS文件
newCSharplines = []
newCSharpFile = open(cs_class_name,"wb")
newCSharpFile.write("//Auto Generate File, Do NOT Modify!!!!!!!!!!!!!!!\n")
newCSharpFile.write("using System;\n")
newCSharpFile.write("namespace com.protocol\n")
newCSharpFile.write("{\n")
newCSharpFile.write("\tpublic enum %s\n" % (cs_class_name.replace("csharpfile\\","").split('.')[0]))
newCSharpFile.write("\t{\n")


#遍历生成ID
for line in lines:
	#if line.startswith("//"):
		#newCSharpFile.write( "%s" % (line))
	if line.startswith("message"):
		text = line.split(' ')
		if text[1].find("\n") > 0:
			message_name = text[1].split("\n")[0]
		else:
			message_name = text[1]
		if message_name.find("{") > 0:
			message_name = message_name.split("{")[0]

		newCSharpFile.write( "\t\t%s = %s,\n" % (message_name,start_index))
		start_index = start_index + 1
		print message_name

#c sharp文件结束
newCSharpFile.write("\n\t}\n")

newCSharpFile.write("}\n")
newCSharpFile.close()



proto_file.close()
