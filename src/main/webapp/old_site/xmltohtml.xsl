<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="utf-8" indent="yes"/>

	<xsl:variable name="color1">d2d2d2</xsl:variable>
	<xsl:variable name="color2">f0f0f0</xsl:variable>

	<xsl:template match="books">
		<html>
			<head>
				<title><xsl:value-of select="@title"/></title>
				<link rel="alternate" type="application/rss+xml" title="{@title} Feed" href="{@feed}"/>
			</head>
			<body>
    			<table border="0">
    				<tr bgcolor="#{$color1}"><th>Date</th><th>Author</th><th>Title</th><th>Subtitle</th><th>Year</th><th>Rating (0-5)</th></tr>
    				<xsl:for-each select="book">
    					<xsl:call-template name="book"/>
    				</xsl:for-each>
    			</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="book">
		<xsl:variable name="color">
			<xsl:choose>
				<xsl:when test="position() mod 2 = 0"><xsl:value-of select="$color1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$color2"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<tr bgcolor="#{$color}">
			<a name="{1+last()-position()}">
				<td valign="top"><xsl:value-of select="substring(child::date[1], 1, 4)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 6, 2)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 9, 2)"/></td>
				<td valign="top"><xsl:value-of select="child::author[1]"/></td>
				<td valign="top"><xsl:value-of select="child::title[1]"/></td>
				<td valign="top"><xsl:value-of select="child::subtitle[1]"/></td>
				<td valign="top"><xsl:value-of select="child::publishingyear[1]"/></td>
				<td valign="top">
				    <xsl:if test="string-length(child::rating[1])>0">
				        <xsl:value-of select="child::rating[1]-1"/>
				    </xsl:if>
				</td>
			</a>
		</tr>
	</xsl:template>

	<xsl:template match="concerts">
		<html>
			<head>
				<title><xsl:value-of select="@title"/></title>
				<link rel="alternate" type="application/rss+xml" title="{@title} Feed" href="{@feed}"/>
			</head>
			<body>
    			<table border="0">
    				<tr bgcolor="#{$color1}"><th>Date</th><th>Musicians</th><th>Location</th><th>Event</th><th>Rating (0-5)</th><th>Comments</th></tr>
    				<xsl:for-each select="concert">
    					<xsl:call-template name="concert"/>
    				</xsl:for-each>
    			</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="concert">
		<xsl:variable name="color">
			<xsl:choose>
				<xsl:when test="position() mod 2 = 0"><xsl:value-of select="$color1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$color2"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<tr bgcolor="#{$color}">
			<a name="{1+last()-position()}">
				<td valign="top"><xsl:value-of select="substring(child::date[1], 1, 4)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 6, 2)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 9, 2)"/></td>
				<td valign="top">
					<xsl:if test="string-length(child::group[1])>0">
				        <b><xsl:value-of select="child::group[1]"/></b>
				        <xsl:if test="count(child::musician)>0">: </xsl:if>
				    </xsl:if>
					<xsl:for-each select="child::musician">
						<xsl:choose>
							<xsl:when test="(@leader='true' or last()=1) and string-length(parent::*/child::group[1])=0"><b><xsl:value-of select="."/></b></xsl:when>
							<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="position()=last()-1">
								<xsl:choose>
									<xsl:when test="last()=2">
										<xsl:text> and </xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>, and </xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:when test="position()!=last()">
								<xsl:text>, </xsl:text>
							</xsl:when>
						</xsl:choose>
					</xsl:for-each>
				</td>
				<td valign="top"><xsl:value-of select="child::location[1]"/></td>
				<td valign="top"><xsl:value-of select="child::event[1]"/></td>
				<td valign="top">
				    <xsl:if test="string-length(child::rating[1])>0">
				        <xsl:value-of select="child::rating[1]-1"/>
				    </xsl:if>
				</td>
				<td valign="top"><xsl:value-of select="child::comments[1]"/></td>
			</a>
		</tr>
	</xsl:template>

	<xsl:template match="crashes">
		<html>
			<head>
				<title><xsl:value-of select="@title"/></title>
				<link rel="alternate" type="application/rss+xml" title="{@title} Feed" href="{@feed}"/>
			</head>
			<body>
    			<table border="0">
    				<tr bgcolor="#{$color1}"><th>Date</th><th>Manufacturer</th><th>Model</th><th>Comments</th></tr>
    				<xsl:for-each select="crash">
    					<xsl:call-template name="crash"/>
    				</xsl:for-each>
    			</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="crash">
		<xsl:variable name="color">
			<xsl:choose>
				<xsl:when test="position() mod 2 = 0"><xsl:value-of select="$color1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$color2"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<tr bgcolor="#{$color}">
			<a name="{1+last()-position()}">
				<td valign="top"><xsl:value-of select="substring(child::date[1], 1, 4)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 6, 2)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 9, 2)"/></td>
				<td valign="top"><xsl:value-of select="child::manufacturer[1]"/></td>
				<td valign="top"><xsl:value-of select="child::model[1]"/></td>
				<td valign="top"><xsl:value-of select="child::comments[1]"/></td>
			</a>
		</tr>
	</xsl:template>
	
	<xsl:template match="exhibitions">
		<html>
			<head>
				<title><xsl:value-of select="@title"/></title>
				<link rel="alternate" type="application/rss+xml" title="{@title} Feed" href="{@feed}"/>
			</head>
			<body>
    			<table border="0">
    				<tr bgcolor="#{$color1}"><th>Date</th><th>Name</th><th>Museum</th><th>Rating (0-5)</th></tr>
    				<xsl:for-each select="exhibition">
    					<xsl:call-template name="exhibition"/>
    				</xsl:for-each>
    			</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="exhibition">
		<xsl:variable name="color">
			<xsl:choose>
				<xsl:when test="position() mod 2 = 0"><xsl:value-of select="$color1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$color2"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<tr bgcolor="#{$color}">
			<a name="{1+last()-position()}">
				<td valign="top"><xsl:value-of select="substring(child::date[1], 1, 4)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 6, 2)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 9, 2)"/></td>
				<td valign="top"><xsl:value-of select="child::name[1]"/></td>
				<td valign="top"><xsl:value-of select="child::museum[1]"/></td>
				<td valign="top">
				    <xsl:if test="string-length(child::rating[1])>0">
				        <xsl:value-of select="child::rating[1]-1"/>
				    </xsl:if>
				</td>
			</a>
		</tr>
	</xsl:template>

	<xsl:template match="movies">
		<html>
			<head>
				<title><xsl:value-of select="@title"/></title>
				<link rel="alternate" type="application/rss+xml" title="{@title} Feed" href="{@feed}"/>
			</head>
			<body>
    			<table border="0">
    				<tr bgcolor="#{$color1}"><th>Date</th><th>Theater</th><th>Director</th><th width="25%">Title</th><th>Alternative Title(s)</th><th>Version</th><th>Rating (0-5)</th><th width="25%">Comments</th></tr>
    				<xsl:for-each select="movie">
    					<xsl:call-template name="movie"/>
    				</xsl:for-each>
    			</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="movie">
		<xsl:variable name="color">
			<xsl:choose>
				<xsl:when test="position() mod 2 = 0"><xsl:value-of select="$color1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$color2"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<tr bgcolor="#{$color}">
			<a name="{1+last()-position()}">
				<td valign="top"><xsl:value-of select="substring(child::date[1], 1, 4)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 6, 2)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 9, 2)"/></td>
				<td valign="top"><xsl:value-of select="child::theater[1]"/></td>
				<td valign="top"><xsl:value-of select="child::director[1]"/></td>
				<td valign="top">
				    <xsl:choose>
				        <xsl:when test="count(child::title[string-length(@language)=0])>0">
				            <xsl:choose>
				                <xsl:when test="count(child::uri[1])>0">
				                    <a href="{child::uri[1]}"><xsl:value-of select="child::title[string-length(@language)=0]"/></a>
				                </xsl:when>
				                <xsl:otherwise>
				                    <xsl:value-of select="child::title[string-length(@language)=0]"/>
				                </xsl:otherwise>
				            </xsl:choose>
				        </xsl:when>
				    </xsl:choose>
				</td>
				<td valign="top">
				    <xsl:for-each select="child::title[string-length(@language)>0]">
				        <xsl:choose>
				            <xsl:when test="position()!=1"><xsl:text>, </xsl:text></xsl:when>
				        </xsl:choose>
				        <xsl:value-of select="."/>
				        <xsl:choose>
				            <xsl:when test="@language='fr'"><xsl:text> (French)</xsl:text></xsl:when>
				            <xsl:when test="@language='en'"><xsl:text> (English)</xsl:text></xsl:when>
                            <xsl:when test="@language='jp'"><xsl:text> (Japanese)</xsl:text></xsl:when>
				            <xsl:otherwise><xsl:text> (</xsl:text><xsl:value-of select="@language"/><xsl:text>)</xsl:text></xsl:otherwise>
				        </xsl:choose>
				    </xsl:for-each>
				</td>
				<td valign="top">
				    <xsl:choose>
				        <xsl:when test="string-length(child::version[1])=0">Original</xsl:when>
				        <xsl:when test="child::version[1]='fr'">French</xsl:when>
				        <xsl:when test="child::version[1]='de'">German</xsl:when>
				        <xsl:otherwise><xsl:value-of select="child::version[1]"/></xsl:otherwise>
				    </xsl:choose>
				</td>
				<td valign="top">
				    <xsl:if test="string-length(child::rating[1])>0">
				        <xsl:value-of select="child::rating[1]-1"/>
				    </xsl:if>
				</td>
				<td valign="top"><xsl:value-of select="child::comments[1]"/></td>
			</a>
		</tr>
	</xsl:template>

	<xsl:template match="plays">
		<html>
			<head>
				<title><xsl:value-of select="@title"/></title>
				<link rel="alternate" type="application/rss+xml" title="{@title} Feed" href="{@feed}"/>
			</head>
			<body>
    			<table border="0">
    				<tr bgcolor="#{$color1}"><th>Date</th><th>Location</th><th>Name</th><th>Author</th><th>Director</th><th>Adaptation</th><th>Translation</th><th>Actors</th><th>Rating (0-5)</th><th>Comments</th></tr>
    				<xsl:for-each select="play">
    					<xsl:call-template name="play"/>
    				</xsl:for-each>
    			</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="play">
		<xsl:variable name="color">
			<xsl:choose>
				<xsl:when test="position() mod 2 = 0"><xsl:value-of select="$color1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$color2"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<tr bgcolor="#{$color}">
			<a name="{1+last()-position()}">
				<td valign="top"><xsl:value-of select="substring(child::date[1], 1, 4)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 6, 2)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::date[1], 9, 2)"/></td>
				<td valign="top"><xsl:value-of select="child::location[1]"/></td>
				<td valign="top"><xsl:value-of select="child::name[1]"/></td>
				<td valign="top"><xsl:value-of select="child::author[1]"/></td>
				<td valign="top"><xsl:value-of select="child::director[1]"/></td>
				<td valign="top"><xsl:value-of select="child::adaptation[1]"/></td>
				<td valign="top"><xsl:value-of select="child::translation[1]"/></td>
				<td valign="top">
					<xsl:for-each select="child::actor">
						<xsl:value-of select="."/>
						<xsl:choose>
							<xsl:when test="position()=last()-1">
								<xsl:choose>
									<xsl:when test="last()=2">
										<xsl:text> and </xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>, and </xsl:text>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:when test="position()!=last()">
								<xsl:text>, </xsl:text>
							</xsl:when>
						</xsl:choose>
					</xsl:for-each>
				</td>
				<td valign="top">
				    <xsl:if test="string-length(child::rating[1])>0">
				        <xsl:value-of select="child::rating[1]-1"/>
				    </xsl:if>
				</td>
				<td valign="top"><xsl:value-of select="child::comments[1]"/></td>
			</a>
		</tr>
	</xsl:template>

	<xsl:template match="trips">
		<html>
			<head>
				<title><xsl:value-of select="@title"/></title>
				<link rel="alternate" type="application/rss+xml" title="{@title} Feed" href="{@feed}"/>
			</head>
			<body>
    			<table border="0">
    				<tr bgcolor="#{$color1}"><th>From</th><th>To</th><th>Place</th><th>Pictures</th></tr>
    				<xsl:for-each select="trip">
    					<xsl:call-template name="trip"/>
    				</xsl:for-each>
    			</table>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="trip">
		<xsl:variable name="color">
			<xsl:choose>
				<xsl:when test="position() mod 2 = 0"><xsl:value-of select="$color1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$color2"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<tr bgcolor="#{$color}">
			<a name="{1+last()-position()}">
				<td valign="top"><xsl:value-of select="substring(child::from[1], 1, 4)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::from[1], 6, 2)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::from[1], 9, 2)"/></td>
				<td valign="top"><xsl:value-of select="substring(child::to[1], 1, 4)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::to[1], 6, 2)"/><xsl:text>/</xsl:text><xsl:value-of select="substring(child::to[1], 9, 2)"/></td>
				<td valign="top"><xsl:value-of select="child::place[1]"/></td>
				<td valign="top">
				    <xsl:for-each select="child::pictures">
    					<xsl:choose>
							<xsl:when test="string-length(.)>0">
								<a href="{@url}"><xsl:value-of select="."/></a><br/>
							</xsl:when>
							<xsl:otherwise>
								<a href="{@url}"><xsl:value-of select="parent::*/child::place[1]"/></a><br/>
							</xsl:otherwise>
						</xsl:choose>
    				</xsl:for-each>
				</td>
			</a>
		</tr>
	</xsl:template>
</xsl:stylesheet>
