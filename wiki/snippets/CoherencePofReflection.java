package serializers;

import org.gridkit.coherence.utils.pof.ReflectionPofSerializer;

import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

import data.media.Image;
import data.media.Media;
import data.media.MediaContent;

public class CoherencePofReflection
{
	public static void register(TestGroups groups)
	{
		XmlElement config = XmlHelper.loadFileOrResource("coherence-pof-config.xml", "POF config");
		XmlElement list = config.getElement("user-type-list");
		
		XmlElement _image = list.addElement("user-type");
		_image.addElement("type-id").setInt(1002);
		_image.addElement("class-name").setString(Image.class.getName());
		_image.addElement("serializer").addElement("class-name").setString(ReflectionPofSerializer.class.getName());

		XmlElement _mediaContent = list.addElement("user-type");
		_mediaContent.addElement("type-id").setInt(1003);
		_mediaContent.addElement("class-name").setString(MediaContent.class.getName());
		_mediaContent.addElement("serializer").addElement("class-name").setString(ReflectionPofSerializer.class.getName());

		XmlElement _media = list.addElement("user-type");
		_media.addElement("type-id").setInt(1004);
		_media.addElement("class-name").setString(Media.class.getName());
		_media.addElement("serializer").addElement("class-name").setString(ReflectionPofSerializer.class.getName());
		
		ConfigurablePofContext reflectionSerializer = new ConfigurablePofContext(config);
		groups.media.<MediaContent>add(JavaBuiltIn.MediaTransformer, new Codec("pof-refl", reflectionSerializer));
	}

	public static class Codec extends Serializer<MediaContent>
	{
		private String name;
		private ConfigurablePofContext serializer;
		
		public Codec(String name, ConfigurablePofContext serializer) {
			this.name = name;
			this.serializer = serializer;
		}

		public String getName() { 
			return name; 
		}		
		
		@Override
		public MediaContent deserialize(byte[] array) throws Exception {			
			try {
				return (MediaContent) ExternalizableHelper.fromBinary(new Binary(array), serializer);
			}
			catch(RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
		}

		@Override
		public byte[] serialize(MediaContent content) throws Exception {
			try {
				return ExternalizableHelper.toByteArray(content, serializer);
			}
			catch(RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
}
