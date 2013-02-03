#!/bin/bash
saxon -s:books.xml -xsl:xmltorss.xsl > books.rss
saxon -s:books.xml -xsl:xmltohtml.xsl > books.html
