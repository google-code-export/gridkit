package com.medx.processing.dictionarymerger;

public class DictionaryMerger {
	/*private static final Logger log = LoggerFactory.getLogger(DictionaryMerger.class);
	
	public static void main(String[] args) throws SAXException, ValidityException, ParsingException, IOException {
		File destinationFile = new File(args[0]);
		Document destinationDictionary = loadOrCreateDictionary(destinationFile);
		
		String sources[] = Arrays.copyOfRange(args, 1, args.length);
		
		for (String source : sources)
			mergeDictionary(loadDictionary(new File(source)), destinationDictionary);
		
		storeDictionary(destinationDictionary, destinationFile);
	}
	
	public static void mergeDictionary(Document source, Document destination) {
		int defaultVersion = getDictionaryVersion(source);
		
		Nodes nodes = source.query("/attributes/attribute[@id and name]");
		
		for (int i = 0; i < nodes.size(); ++i) {
			Element element = (Element) nodes.get(i);
			
			String id = element.getAttributeValue("id");
			String name = element.getChildElements("name").get(0).getValue();
			
			if (destination.query(format("/attributes/attribute[id = '%s' or name = '%s']", id, name)).size() > 0)
				log.warn(format("Attribute with id = '%s' or name = '%s' already presented in ", id, name));
			else {
				if (element.getAttribute("version") == null)
					element.addAttribute(new Attribute("version", String.valueOf(defaultVersion)));
				
				destination.getRootElement().appendChild(element.copy());
			}
		}
	}
	*/
}
