package org.gridkit.lab.orchestration.script;

import java.io.Serializable;

import org.gridkit.util.concurrent.Box;

public class Creation implements ScriptAction, Serializable {
    private static final long serialVersionUID = 1208879727905581237L;
    
    private ScriptBean bean;
    
    @Override
    public void execute(Box<Void> box) {
        bean.create(box);
    }

    public ScriptBean getBean() {
        return bean;
    }

    public void setBean(ScriptBean bean) {
        this.bean = bean;
    }
}
