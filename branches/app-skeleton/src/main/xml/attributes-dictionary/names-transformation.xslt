<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                extension-element-prefixes="fn"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:output method="text" encoding="UTF-8"/>
	
	<xsl:param name="package"/>

	<xsl:template match="/">
		<xsl:text>package </xsl:text>
		<xsl:value-of select="fn:replace($package, '/', '.')"/>
		<xsl:text>;</xsl:text>
		
		<xsl:text>public class Names {</xsl:text>
		
		<xsl:for-each select="attributes/attribute">
			<xsl:text>public final String </xsl:text><xsl:value-of select="name"/><xsl:text>="";</xsl:text>
		</xsl:for-each>
		
		<xsl:text>}</xsl:text>
	</xsl:template>
</xsl:stylesheet>
