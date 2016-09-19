#!/bin/sh
cd `pwd`;
echo "start proto...";
protoc --java_out=./ *.proto;
echo "end proto...";
read -n1 -p "Press any key to continue...";
