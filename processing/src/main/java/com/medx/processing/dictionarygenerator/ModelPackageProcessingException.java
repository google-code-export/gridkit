package com.medx.processing.dictionarygenerator;

import javax.lang.model.element.Element;

public class ModelPackageProcessingException extends Exception {
	private static final long serialVersionUID = 7179774215340246431L;
	
	private Element element;
	
    public ModelPackageProcessingException(String message, Throwable cause, Element element) {
        super("[ " + message + " ] : " + createMessage(cause), cause);
        this.element = element;
    }
    
	public Element getElement() {
		return element;
	}
	
	private static String createMessage(Throwable throwable){
		if (throwable.getCause() == null)
			return "[ " + throwable.getMessage() + " ]";
		else
			return "[ " + throwable.getMessage() + " ] : " + createMessage(throwable.getCause());
	}
}
