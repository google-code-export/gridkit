package org.gridkit.coherence.offheap.storage.memlog;

/**
 * A simple variation of CRC
 * @author aragozin
 */
class BinHash {

	static public int[] Table = new int[256];
	
	static
	{
		for (int i = 0; i < 256; i++)
		{
			int r = i;
			for (int j = 0; j < 8; j++)
				if ((r & 1) != 0)
					r = (r >>> 1) ^ 0xEDB88320;
				else
					r >>>= 1;
			Table[i] = r;
		}
	}

	
	public static int hash(ByteChunk bytes)	{
		int hash = -1;
		for (int i = 0; i < bytes.lenght(); i++) {
			hash = Table[(hash ^ bytes.at(i)) & 0xFF] ^ (hash >>> 8);
		}
		return hash;
	}
}
