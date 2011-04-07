package sample;

public class LargeDomainObjAttrib extends DomainObjAttrib {

    byte[] blob;
    
    public LargeDomainObjAttrib() {
        super();
    }

    public LargeDomainObjAttrib(String str, byte[] blob) {
        super(str);
        this.blob = blob;
    }
}
