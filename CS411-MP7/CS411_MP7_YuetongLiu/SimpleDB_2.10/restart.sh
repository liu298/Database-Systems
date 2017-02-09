
# !/bin/bash
# clean and compile
rm simpledb/parse/*.class
rm simpledb/planner/*.class
rm simpledb/query/*.class
rm ~/studentdb/*
javac simpledb/*/*.java simpledb/*/*.java

# start simpledb server
java simpledb.server.Startup studentdb
jar cf simpledb.jar simpledb/*/*.class simpledb/*/*/*.class
cp simpledb.jar studentClient/simpledb/