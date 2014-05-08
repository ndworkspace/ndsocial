package cn.nd.social.hotspot;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 
 * 
 * @author Administrator
 * 
 */
public class RDataOutputStream implements DataOutput {

	private DataOutputStream dos;

	public RDataOutputStream(DataOutputStream dos) {
		this.dos = dos;
	}

	@Override
	public void write(byte[] b) throws IOException {
		dos.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		dos.write(b, off, len);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		dos.writeBoolean(v);

	}

	@Override
	public void writeByte(int v) throws IOException {
		dos.writeByte(v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		dos.writeChar((char) v);

	}

	@Override
	public void writeInt(int v) throws IOException {
		dos.writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		dos.writeLong(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		dos.writeShort((short) v);
	}

	public void flush() throws IOException {
		dos.flush();
	}

	public void close() throws IOException {
		dos.close();
	}

	// ----------------------------------------------------------
	@Override
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeBytes(String s) throws IOException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void writeChars(String s) throws IOException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void writeDouble(double v) throws IOException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void writeFloat(float v) throws IOException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void writeUTF(String s) throws IOException {
		throw new UnsupportedOperationException();
	}
}
