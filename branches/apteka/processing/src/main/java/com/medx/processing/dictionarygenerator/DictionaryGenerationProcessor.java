package com.medx.processing.dictionarygenerator;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.medx.processing.util.MessageUtil;

@SupportedAnnotationTypes("com.medx.framework.annotation.ModelPackage")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DictionaryGenerationProcessor extends AbstractProcessor {	
	@Override
	public boolean process(Set<? extends TypeElement> elements, RoundEnvironment roundEnv) {
		for (TypeElement modelPackage : elements) {
			for (Element element : roundEnv.getElementsAnnotatedWith(modelPackage)) {
				PackageElement packet = (PackageElement) element;
				
				ModelPackageProcessor modelPackageProcessor = new ModelPackageProcessor(packet, roundEnv, processingEnv);
				
				try {
					modelPackageProcessor.process();
				} catch (ModelPackageProcessingException e) {
					if (e.isLog())
						processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage(), e.getElement());
				} catch (Throwable t) {
					processingEnv.getMessager().printMessage(Kind.ERROR, MessageUtil.createMessage("Unexpected Throwable during annotation processing", t), packet);
				}
			}
		}
		
		return true;
	}
}
