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

@SupportedAnnotationTypes("com.medx.framework.annotation.ModelPackage")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DictionaryGenerationProcessor extends AbstractProcessor {	
	@Override
	public boolean process(Set<? extends TypeElement> elements, RoundEnvironment environment) {
		for (TypeElement modelPackage : elements) {
			for (Element packet : environment.getElementsAnnotatedWith(modelPackage)) {
				ModelPackageProcessor modelPackageProcessor = new ModelPackageProcessor((PackageElement)packet, environment);
				
				try {
					modelPackageProcessor.process();
				} catch (ModelPackageProcessingException e) {
					processingEnv.getMessager().printMessage(Kind.WARNING, e.getMessage(), e.getElement());
				}
			}
		}
		
		return true;
	}
}
