package NDCSdk;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class ByteConv {
	
	public static short getShort(byte[] Data, Offset offset)
	{
		byte[] bValue = {Data[offset.value], Data[offset.value+1]};
		offset.value += 2;
		return ByteBuffer.wrap(bValue).order(ByteOrder.BIG_ENDIAN).getShort();
	}
	
	public static int getInt(byte[] Data, Offset offset)
	{
		byte[] bValue = {Data[offset.value], Data[offset.value+1],
				Data[offset.value+2], Data[offset.value+3]};
		offset.value += 4;
		return ByteBuffer.wrap(bValue).order(ByteOrder.BIG_ENDIAN).getInt();
	}
	
	public static long getLong(byte[] Data, Offset offset)
	{
		byte[] bValue = {Data[offset.value], Data[offset.value+1],Data[offset.value+2], Data[offset.value+3],
							Data[offset.value+4], Data[offset.value+5],Data[offset.value+6], Data[offset.value+7]};
		
		offset.value += 8;
		return ByteBuffer.wrap(bValue).order(ByteOrder.BIG_ENDIAN).getLong();
	}
	
	public static String getString(byte[] Data, Offset offset, int nLen)
	{
		byte[] bValue = new byte[nLen];
		System.arraycopy(Data, offset.value, bValue, 0,  nLen);
		
		offset.value += nLen;
		return new String(bValue);
	}
	
	public static byte[] getByte(byte[] Data, Offset offset, int nLen)
	{
		byte[] bValue = new byte[nLen+1];
		System.arraycopy(Data, offset.value, bValue, 0,  nLen);		
		offset.value += nLen;
		return bValue;
	}
	
	
	public static int getOffset(Offset oset)
	{
		return oset.value;
	}
	
	public static class Offset
	{
		public Offset()
		{
			value = 0;
		}
		int value;
	}
	
	public static int ReadInt(byte[] Data, Offset offset)
	{
		byte[] bValue = {Data[offset.value], Data[offset.value+1],
				Data[offset.value+2], Data[offset.value+3]};
		
		offset.value += 4;
		return ByteBuffer.wrap(bValue).order(ByteOrder.BIG_ENDIAN).getInt();
	}
	
	public static short ReadShort(byte[] Data, Offset offset)
	{
		byte[] bValue = {Data[offset.value], Data[offset.value+1]};
		offset.value += 2;
		return ByteBuffer.wrap(bValue).order(ByteOrder.BIG_ENDIAN).getShort();
	}
	
	public static byte[] ReadByte(byte[] Data, Offset offset, int nLen)
	{
		byte[] bValue = new byte[nLen+1];
		
		try
		{
		System.arraycopy(Data, offset.value, bValue, 0,  nLen);	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		offset.value += nLen;
		return bValue;
	}

}
