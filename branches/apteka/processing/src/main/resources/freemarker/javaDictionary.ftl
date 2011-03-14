package ${package};

import ${classKeyClass};
import ${ClassKeyFactoryClass};
import ${userAttrKeyClass};

public final class ${className} {
    public static final class Type {
        public static final ClassKey descriptor = ClassKeyFactory.createUserClassKey(${typeDesc.id}, ${typeDesc.version}, ${typeDesc.clazz}.class);
    }

<#list attrDescs as a>
    public static final UserAttrKey<${a.type}> ${a.varName} = new UserAttrKey<${a.type}>(${a.id}, ${a.name}, ${a.version}, ${a.description});
</#list>
}
