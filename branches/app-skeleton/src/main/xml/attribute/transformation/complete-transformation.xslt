<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:attr="http://griddynamics.com/attr-functions">
	<xsl:param name="sourceFolder" required="yes"/>
	<xsl:param name="dictionaryFolder" required="yes"/>
	
	<xsl:variable name="dictionaryPackage" select="fn:replace($dictionaryFolder, '/', '.')"/> 
	
	<xsl:param name="defaultClass" select="'Default'" required="no"/>
	
	<xsl:output method="text" encoding="UTF-8"/>

	<xsl:function name="attr:package">
		<xsl:param name="name"/>
		<xsl:variable name="nameSeq" select="fn:tokenize($name, '\.')"/>
		<xsl:value-of select="fn:string-join(fn:subsequence($nameSeq, 1, fn:count($nameSeq) - 2), '.')"/>
	</xsl:function>
	
	<xsl:function name="attr:class">
		<xsl:param name="name"/>
		<xsl:variable name="nameSeq" select="fn:tokenize($name, '\.')"/>
		<xsl:choose>
			<xsl:when test="fn:count($nameSeq) > 1">
				<xsl:value-of select="fn:subsequence($nameSeq, fn:count($nameSeq) - 1, 1)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$defaultClass"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	<xsl:function name="attr:fullClass">
		<xsl:param name="name"/>
		<xsl:variable name="nameSeq" select="fn:tokenize($name, '\.')"/>
		<xsl:value-of select="fn:concat(attr:package($name), '.', attr:class($name))"/>
	</xsl:function>

	<xsl:function name="attr:firstName">
		<xsl:param name="name"/>
		<xsl:variable name="nameSeq" select="fn:tokenize($name, '\.')"/>
		<xsl:value-of select="fn:string-join(fn:subsequence($nameSeq, 1, fn:count($nameSeq) - 1), '.')"/>
	</xsl:function>
	
	<xsl:function name="attr:lastName">
		<xsl:param name="name"/>
		<xsl:variable name="nameSeq" select="fn:tokenize($name, '\.')"/>
		<xsl:choose>
			<xsl:when test="fn:count($nameSeq) > 0">
				<xsl:value-of select="fn:subsequence($nameSeq, fn:count($nameSeq))"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message terminate="yes">Argument of 'attr:lastName' is empty</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

	<xsl:template match="/">
		<xsl:variable name="classes" select="/attributes/attribute[not(attr:fullClass(name)=preceding-sibling::attribute/attr:fullClass(name))]/attr:fullClass(name)"/>
		<xsl:variable name="attributes" select="/attributes/attribute"/>
		
		<xsl:for-each select="$classes">
			<xsl:variable name="currentClass" select="current()"/>
			
			<xsl:variable name="class" select="fn:concat($dictionaryPackage, '.' ,current())"/>
			<xsl:variable name="outputFile" select="fn:concat('file:/', fn:replace($sourceFolder, '\\', '/'), '/', fn:replace($class, '\.', '/') , '.java')"></xsl:variable>
		
			<xsl:message terminate="no"><xsl:value-of select="fn:concat('Writing to ', $outputFile)"/></xsl:message>
		
			<xsl:result-document href="{$outputFile}">
				<xsl:value-of select="fn:concat('package ', attr:firstName($class), ';&#xA;&#xA;')"/>
				<xsl:value-of select="fn:concat('import ', attr:firstName($dictionaryPackage), '.attribute.AttributeKey;&#xA;&#xA;')"/>
				<xsl:value-of select="fn:concat('public class ', attr:lastName($class), ' { &#xA;')"/>

				<xsl:for-each select="$attributes[attr:fullClass(name) = $currentClass]">
					<xsl:variable name="genericClass" select="fn:concat('AttributeKey&lt;' , type, '&gt;')"/>
					<xsl:value-of select="fn:concat('public static final ', $genericClass, ' ', attr:lastName(name), ' = new ', $genericClass, '((short)', @id, ',', type, '.class ,&quot;', description , '&quot;);')"/>
				</xsl:for-each>
				
				<xsl:value-of select="'}&#xA;'"/>
			</xsl:result-document>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
