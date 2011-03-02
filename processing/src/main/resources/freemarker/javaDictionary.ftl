package ${package};

import ${attrKeyClass};

public class ${className} {
<#list attrDescs as a>
 	public static final AttrKey<${a.type}> ${a.varName} = new AttrKey<${a.type}>(${a.id}, ${a.name}, ${a.version}, ${a.clazz}, ${a.description});
</#list>
}
