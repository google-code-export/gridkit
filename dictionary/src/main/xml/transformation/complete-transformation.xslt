<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:af="http://apteka.org/insurance/dictionary/transformation/functions">
	<xsl:import href="functions.xslt"/>
	
	<xsl:param name="sourceFolder" required="yes"/>
	<xsl:param name="dictionaryFolder" required="yes"/>
	<xsl:param name="attributeKeyClass" required="yes"/>
	<xsl:param name="attributeKeyRegistryClass" required="yes"/>
	
	<xsl:output method="text" encoding="UTF-8"/>
	
	<xsl:variable name="dictionaryPackage" select="fn:replace($dictionaryFolder, '/', '.')"/> 

	<xsl:template match="/">
		<xsl:variable name="classes" select="/attributes/attribute[not(af:fullClass(name)=preceding-sibling::attribute/af:fullClass(name))]/af:fullClass(name)"/>
		<xsl:variable name="attributes" select="/attributes/attribute"/>
		
		<xsl:for-each select="$classes">
			<xsl:variable name="currentClass" select="current()"/>
			
			<xsl:variable name="class" select="fn:concat($dictionaryPackage, '.', $currentClass)"/>
			<xsl:variable name="outputFile" select="fn:concat('file:/', fn:replace($sourceFolder, '\\', '/'), '/', fn:replace($class, '\.', '/') , '.java')"></xsl:variable>
		
			<xsl:message terminate="no"><xsl:value-of select="fn:concat('Writing to ', $outputFile)"/></xsl:message>
		
			<xsl:result-document href="{$outputFile}">
				<xsl:value-of select="fn:concat('package ', af:firstName($class), ';&#xA;&#xA;')"/>
				<xsl:value-of select="fn:concat('import ', $attributeKeyClass, ';&#xA;')"/>
				<xsl:value-of select="fn:concat('import ', $attributeKeyRegistryClass, ';&#xA;&#xA;')"/>
				
				<xsl:value-of select="fn:concat('public final class ', af:lastName($class), ' { &#xA;')"/>

				<xsl:variable name="currentAttributes" select="$attributes[af:fullClass(name) = $currentClass]"/>

				<xsl:for-each select="$currentAttributes">
					<xsl:variable name="genericClass" select="fn:concat('AttrKey&lt;' , type, '&gt;')"/>
					<xsl:value-of select="fn:concat('public static final ', $genericClass, ' ', af:lastName(name), ' = new ', $genericClass, '(', @id, ',', type, '.class ,&quot;', description , '&quot;);')"/>
				</xsl:for-each>
				
				<xsl:value-of select="'public static final class Id {'"/>
					<xsl:for-each select="$currentAttributes">
						<xsl:value-of select="fn:concat('public static final int ', af:lastName(name), ' = ', @id, ';')"/>
					</xsl:for-each>
				<xsl:value-of select="'}&#xA;'"/>
				
				<xsl:value-of select="'public static final class Text {'"/>
					<xsl:for-each select="$currentAttributes">
						<xsl:value-of select="fn:concat('public static final String ', af:lastName(name), ' = &quot;', af:lastName(name), '&quot;;')"/>
					</xsl:for-each>
				<xsl:value-of select="'}&#xA;'"/>
				
				<xsl:value-of select="'static {'"/>
					<xsl:for-each select="$currentAttributes">
						<xsl:value-of select="fn:concat('AttrKeyRegistry.getInstance().registerAttrKey(&quot;', af:lastName(name), '&quot;, ', af:lastName(name), ');')"/>
					</xsl:for-each>
				<xsl:value-of select="'}&#xA;'"/>
				
				<xsl:value-of select="'}&#xA;'"/>
			</xsl:result-document>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
