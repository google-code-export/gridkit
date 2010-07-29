package serializers;

import java.io.IOException;
import java.util.List;

import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

import data.media.Image;
import data.media.Media;
import data.media.MediaContent;
import data.media.Image.Size;
import data.media.Media.Player;

public class CoherencePofSerializer
{
	public static void register(TestGroups groups)
	{
		XmlElement config = XmlHelper.loadFileOrResource("coherence-pof-config.xml", "POF config");
		XmlElement list = config.getElement("user-type-list");
		
		XmlElement _image = list.addElement("user-type");
		_image.addElement("type-id").setInt(1002);
		_image.addElement("class-name").setString(Image.class.getName());
		_image.addElement("serializer").addElement("class-name").setString(ImageCodec.class.getName());
		
		XmlElement _mediaContent = list.addElement("user-type");
		_mediaContent.addElement("type-id").setInt(1003);
		_mediaContent.addElement("class-name").setString(MediaContent.class.getName());
		_mediaContent.addElement("serializer").addElement("class-name").setString(MediaContentCodec.class.getName());
		
		XmlElement _media = list.addElement("user-type");
		_media.addElement("type-id").setInt(1004);
		_media.addElement("class-name").setString(Media.class.getName());
		_media.addElement("serializer").addElement("class-name").setString(MediaCodec.class.getName());
		
		ConfigurablePofContext manualSerializer = new ConfigurablePofContext(config);
		groups.media.<MediaContent>add(JavaBuiltIn.MediaTransformer, new Codec("pof-cust", manualSerializer));
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
	
	@SuppressWarnings("unchecked")
	private static Enum readEnum(Enum[] values, PofReader in, int prop) throws IOException {
		int n = in.readInt(prop);
		return n < 0 ? null : values[n];
	}
	
	public static class MediaCodec implements PofSerializer {

		@SuppressWarnings("unchecked")
		@Override
		public Object deserialize(PofReader in) throws IOException {
			int n = 0;
			Media m = new Media();			
			
			m.uri = in.readString(n++);
			m.title = in.readString(n++);
			m.width = in.readInt(n++);
			m.height = in.readInt(n++);
			m.format = in.readString(n++);
			m.duration = in.readLong(n++);
			m.size = in.readLong(n++);
			m.bitrate = in.readInt(n++);
			m.hasBitrate = in.readBoolean(n++);
			m.persons = (List<String>) in.readObject(n++);
			m.player = (Player) readEnum(Player.values(), in, n++);
			m.copyright = in.readString(n++);  
			
			in.readRemainder();
			
			return m;
		}

		@Override
		public void serialize(PofWriter out, Object o) throws IOException {
			Media m = (Media) o;
			int n = 0;
			out.writeString(n++, m.uri);
			out.writeString(n++, m.title);
			out.writeInt(n++, m.width);
			out.writeInt(n++, m.height);
			out.writeString(n++, m.format);
			out.writeLong(n++, m.duration);
			out.writeLong(n++, m.size);
			out.writeInt(n++, m.bitrate);
			out.writeBoolean(n++, m.hasBitrate);
			out.writeObject(n++, m.persons);
			out.writeInt(n++, m.player == null ? -1 : m.player.ordinal());
			out.writeString(n++, m.copyright);  
			out.writeRemainder(null);
		}
	}
	
	public static class ImageCodec implements PofSerializer {

		@Override
		public Object deserialize(PofReader in) throws IOException {
			Image i = new Image();
			int n = 0;
			
			i.uri = in.readString(n++);
			i.title = in.readString(n++);
			i.width = in.readInt(n++);
			i.height = in.readInt(n++);
			i.size = (Size) readEnum(Size.values(), in, n++);
			in.readRemainder();
			
			return i;
		}

		@Override
		public void serialize(PofWriter out, Object o) throws IOException {
			Image i = (Image) o;
			int n = 0;
			
			out.writeString(n++, i.uri);
			out.writeString(n++, i.title);
			out.writeInt(n++, i.width);
			out.writeInt(n++, i.height);
			out.writeInt(n++, i.size == null ? -1 : i.size.ordinal());
			out.writeRemainder(null);
		}
	}
	
	public static class MediaContentCodec implements PofSerializer {
		
		@SuppressWarnings("unchecked")
		@Override
		public Object deserialize(PofReader in) throws IOException {
			MediaContent mc = new MediaContent();
			int n = 0;
			mc.media = (Media) in.readObject(n++);
			mc.images = (List)in.readObject(n++);
			in.readRemainder();
			return mc;
		}
		
		@Override
		public void serialize(PofWriter out, Object o) throws IOException {
			MediaContent mc = (MediaContent) o;
			int n = 0;
			out.writeObject(n++, mc.media);
			out.writeObject(n++, mc.images);
			out.writeRemainder(null);
		}
	}

}
