#!/bin/bash
saxon -s:concerts.xml -xsl:xmltorss.xsl > concerts.rss
saxon -s:concerts.xml -xsl:xmltohtml.xsl > concerts.html
