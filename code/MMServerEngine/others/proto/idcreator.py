#!/usr/bin/python
#-*- coding: utf-8 -*-

import os
import sys

proto_file_name = sys.argv[1]
java_class_name = sys.argv[2]
start_index = int(sys.argv[3])

java_class_path = "com/protocol/"+java_class_name

proto_file = open(proto_file_name,"r")
lines = proto_file.readlines()

#删除旧文件
if os.path.exists(java_class_path):
	os.remove(java_class_path)
	
#初始化JAVA文件
newJavaFile = open(java_class_path,"wb") 
newJavaFile.write("package com.protocol;\n")
newJavaFile.write("//Auto Generate File, Do NOT Modify!!!!!!!!!!!!!!!\n")
newJavaFile.write("public class %s {\n" % (java_class_name.split('.')[0]))

#遍历生成ID
for line in lines:
	if line.startswith("message"):
		text = line.split(' ')
		if text[1].find("\n") > 0:
			message_name = text[1].split("\n")[0]
		else:
			message_name = text[1]
		if message_name.find("{") > 0:
			message_name = message_name.split("{")[0]
			
		newJavaFile.write( "\tpublic static final int %s = %s;\n" % (message_name,start_index))
		start_index = start_index + 1
		print message_name	
		
#java文件结束
newJavaFile.write("\n}\n")
newJavaFile.close()
#
proto_file.close()