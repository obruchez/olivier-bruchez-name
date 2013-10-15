#!/bin/bash

SRC_DIR=../public/src
DST_DIR=src/main/webapp/old_site

declare -a list_types=(books concerts crashes exhibitions movies plays trips)

for list_type in ${list_types[@]}
do
  echo "Generating $list_type HTML/RSS files..."
  saxon -s:$SRC_DIR/$list_type.xml -xsl:$SRC_DIR/xmltorss.xsl > $DST_DIR/$list_type.rss
  saxon -s:$SRC_DIR/$list_type.xml -xsl:$SRC_DIR/xmltohtml.xsl > $DST_DIR/$list_type.html
done

echo "Building the application..."
./sbt clean compile package
