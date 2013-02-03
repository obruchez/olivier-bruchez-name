#!/bin/bash
saxon -s:movies.xml -xsl:xmltorss.xsl > movies.rss
saxon -s:movies.xml -xsl:xmltohtml.xsl > movies.html
