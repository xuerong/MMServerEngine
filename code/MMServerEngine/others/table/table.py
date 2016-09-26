#!/usr/bin/python
#-*- coding: utf-8 -*-

import xlrd
import os
import sys
import shutil

proto_file_name = sys.argv[1]
data = xlrd.open_workbook(proto_file_name)

def createJavaFile(name,content):
    java_class_path = "com/table/"+name+".java"
    #删除旧文件
    if os.path.exists(java_class_path):
    	os.remove(java_class_path)
    newJavaFile = open(java_class_path,"wb")
    newJavaFile.write(content)
    newJavaFile.close()
    shutil.copy(java_class_path,  "../../src/main/java/"+java_class_path)


typeStrs = {"int":"int","long":"long","String":"String","string":"String","float":"float","double":"double"}

tables = data.sheets()
for table in tables:
    nrows = table.nrows
    if nrows <2:
        continue
    sb = "package com.table;\n//Auto Generate File, Do NOT Modify!!!!!!!!!!!!!!!\npublic class "+table.name+"{\n"
    getSet = ""
    error = 0
    names = table.row_values(0)
    types = table.row_values(1)
    i = 0
    for record in names:
        name = str(record)
        typeStr = str(types[i])
        if not typeStrs.has_key(typeStr):
            print table.name +" is not table"
            error = 1
            break;
        type = typeStrs.get(typeStr)

        sb = sb +"\tprivate "+type+" "+name+";\n"
        getSet = getSet+"\tpublic "+type+" get"+name.capitalize()+"(){return "+name+";}\n"
        getSet = getSet+"\tpublic void set"+name.capitalize()+"("+type+" "+name+"){this."+name+"="+name+";}\n"
        i=i+1
    if error == 1:
        continue;
    sb=sb+"\n"+getSet+"}"
    createJavaFile(table.name,sb)




