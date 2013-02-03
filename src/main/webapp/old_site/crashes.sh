#!/bin/bash
saxon -s:crashes.xml -xsl:xmltorss.xsl > crashes.rss
saxon -s:crashes.xml -xsl:xmltohtml.xsl > crashes.html
