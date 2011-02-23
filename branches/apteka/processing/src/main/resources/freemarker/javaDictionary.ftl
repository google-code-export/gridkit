package ${package};

import ${attrKeyClass};

public class ${className} {
<#list entries as e>
 	public static final AttrKey<${e.type}> ${e.varName} = new AttrKey<${e.type}>(${e.id}, ${e.name}, ${e.version}, ${e.clazz}, ${e.description});
</#list>
}
