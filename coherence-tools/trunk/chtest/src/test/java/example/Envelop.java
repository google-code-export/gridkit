package example;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.util.Binary;

public class Envelop {

	public static final int TIMESTAMP_POF 	=   1;
	public static final int DELETED_POF   =   2;
	public static final int PAYLOAD_POF 	=  20;
	
	protected long timestamp;
	protected boolean deleted;
    protected Object payload;
    protected Binary binaryPayload;
    transient boolean serverMode;

    /** TO BE USED WITH SERIALIZER */
    protected Envelop(long timestamp, boolean deleted, Object payload, Binary binaryPayload, boolean serverMode) {
		this.timestamp = timestamp;
		this.deleted = deleted;
		this.payload = payload;
		this.binaryPayload = binaryPayload;
		this.serverMode = serverMode;
	}

    /** Constructor used on client side */
	public Envelop(Object payload, long timestamp, boolean deleted) {
        this.payload = payload;
        this.timestamp = timestamp;
        this.deleted = deleted;
        this.serverMode = false;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public Binary getBinaryPayload() {
    	return binaryPayload;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public static class ServerSerializer implements PofSerializer {

		@Override
		public Object deserialize(PofReader in) throws IOException {
			long timestamp = in.readLong(TIMESTAMP_POF);
			boolean deleted = in.readBoolean(DELETED_POF);
			Binary data = in.readRemainder();			
			Envelop dv = new Envelop(timestamp, deleted, null, data, true);
			return dv;
		}

		@Override
		public void serialize(PofWriter out, Object o) throws IOException {			
			Envelop dv = (Envelop) o;
			if (!dv.serverMode) {
				throw new IllegalArgumentException("Object is in client mode, but server serializer is used. Something wrong with POF config!");
			}
			out.writeLong(TIMESTAMP_POF, dv.getTimestamp());
			out.writeBoolean(DELETED_POF, dv.isDeleted());
			out.writeRemainder(dv.getBinaryPayload());
		}
    }
    
    public static class ClientSerializer implements PofSerializer {

		@Override
		public Object deserialize(PofReader in) throws IOException {
			long timestamp = in.readLong(TIMESTAMP_POF);
			boolean deleted = in.readBoolean(DELETED_POF);
			Object payload = in.readObject(PAYLOAD_POF);
			Binary data = in.readRemainder();			
			Envelop dv = new Envelop(timestamp, deleted, payload, data, false);
			return dv;
		}

		@Override
		public void serialize(PofWriter out, Object o) throws IOException {
			Envelop dv = (Envelop) o;
			if (dv.serverMode) {
				throw new IllegalArgumentException("Envelop is in server mode, but client serializer is used. Something wrong with POF config!");
			}
			out.writeLong(TIMESTAMP_POF, dv.getTimestamp());
			out.writeBoolean(DELETED_POF, dv.isDeleted());
			out.writeObject(PAYLOAD_POF, dv.getPayload());
			out.writeRemainder(dv.getBinaryPayload());
		}    	
    }
}
