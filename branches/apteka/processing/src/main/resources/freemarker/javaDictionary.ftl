package ${package};

import ${typeKeyClass};
import ${attrKeyClass};

public final class ${className} {
    public static final class Type {
        public static final TypeKey<${typeDesc.clazz}> descriptor = new TypeKey<${typeDesc.clazz}>(${typeDesc.id}, ${typeDesc.version}, ${typeDesc.clazz}.class);
    }

<#list attrDescs as a>
    public static final AttrKey<${a.type}> ${a.varName} = new AttrKey<${a.type}>(${a.id}, ${a.name}, ${a.version}, ${a.clazz}, ${a.description});
</#list>
}
