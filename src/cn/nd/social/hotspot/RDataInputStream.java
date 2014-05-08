package cn.nd.social.hotspot;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * 
 * 
 * @author Administrator
 * 
 */
public class RDataInputStream implements DataInput {

	public DataInputStream dis;

	public RDataInputStream(DataInputStream dis) {
		this.dis = dis;
	}

	@Override
	public char readChar() throws IOException {
		return dis.readChar();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return dis.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return dis.readByte();
	}

	@Override
	public int readInt() throws IOException {
		return dis.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return dis.readLong();
	}

	@Override
	public short readShort() throws IOException {
		return dis.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return (short) dis.readUnsignedShort();
	}

	public final int read(byte[] b, int off, int len) throws IOException {
		return dis.read(b, off, len);
	}

	public int available() throws IOException {
		return dis.available();
	}

	public final int read(byte[] b) throws IOException {
		return dis.read(b);
	}

	public void close() throws IOException {
		dis.close();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return dis.readUnsignedByte();
	}

	// ----------------------------------------------------------
	@Override
	public double readDouble() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public float readFloat() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int skipBytes(int n) throws IOException {
		throw new UnsupportedOperationException();
	}
}
