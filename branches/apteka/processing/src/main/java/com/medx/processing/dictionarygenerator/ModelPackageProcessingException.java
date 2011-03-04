package com.medx.processing.dictionarygenerator;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;

import com.medx.processing.util.MessageUtil;

public class ModelPackageProcessingException extends Exception {
	private static final long serialVersionUID = 7179774215340246431L;
	
	private final boolean log;
	private final Element element;
	
	public ModelPackageProcessingException(Element element, boolean log) {
		this.log = log;
		this.element = element;
	}
	
	public ModelPackageProcessingException(String message, PackageElement element, boolean log) {
        super(message);
        
        this.log = log;
        this.element = element;
	}
	
    public ModelPackageProcessingException(String message, Throwable cause, Element element, boolean log) {
        super(MessageUtil.createMessage(message, cause), cause);
        
        this.log = log;
        this.element = element;
    }

	public Element getElement() {
		return element;
	}

	public boolean isLog() {
		return log;
	}
}
