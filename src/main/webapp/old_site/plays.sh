#!/bin/bash
saxon -s:plays.xml -xsl:xmltorss.xsl > plays.rss
saxon -s:plays.xml -xsl:xmltohtml.xsl > plays.html
