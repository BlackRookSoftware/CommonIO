/*******************************************************************************
 * Copyright (c) 2009-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.files.wav;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import com.blackrook.commons.math.RMath;
import com.blackrook.io.SuperReader;
import com.blackrook.io.files.SoundFileInfo;
import com.blackrook.io.files.SoundFileInfo.SampleEndian;
import com.blackrook.io.files.SoundFileInfo.SampleType;

/**
 * A WAV File reading class.
 * After file read, the file pointer should be at the beginning of the DATA chunk.
 * @author Matthew Tropiano
 * @since 2.4.0
 */
public class WAVFile extends RandomAccessFile implements Closeable
{
	/** For readPCM(). */
	private static final long[][][] BOUND_LOOKUP =
	{
		// 1
		{
			// unsigned
			{0, 255},
			// signed
			{-128, 127},
		},
		// 2
		{
			// unsigned
			{0, 65536},
			// signed
			{-32768, 32767},
		}
	};
	
	/** For readPCM(). */
	private static final long[] SIGNED_SUBTRAHEND =
	{
		256,
		65536,
	};
	
	/** Sound info. */
	private SoundFileInfo soundInfo;
	/** Data length. */
	private long dataLength;
	/** Data offset. */
	private long dataOffset;
	
	/**
	 * Opens a WadFile from a file specified by "path."
	 * @param path	the path to the File;
	 * @throws IOException if the file can't be read, or is not a WAV File
	 * @throws FileNotFoundException if the file can't be found.
	 * @throws SecurityException if you don't have permission to access the file.
	 * @throws NullPointerException if "path" is null.
	 */
	public WAVFile(String path) throws IOException
	{
		this(new File(path));
	}

	/**
	 * Opens a WadFile from a file.
	 * @param f	the file.
	 * @throws IOException if the file can't be read, or is not a WAV File.
	 * @throws FileNotFoundException if the file can't be found.
	 * @throws SecurityException if you don't have permission to access the file.
	 * @throws NullPointerException if "f" is null.
	 */
	public WAVFile(File f) throws IOException
	{
		super(f, "r");
		seek(0);

		byte[] buffer = new byte[4];
		String chunkName = null;
		
		read(buffer);
		chunkName = new String(buffer, "ASCII");
		// check for RIFF/RIFX
		SampleEndian endian = null;
		if (chunkName.equals("RIFF"))
			endian = SampleEndian.LITTLE_ENDIAN;
		else if (chunkName.equals("RIFF"))
			endian = SampleEndian.BIG_ENDIAN;
		else	
			throw new IOException("Not a WAV file.");
		
		// length of RIFF data.
		read(buffer);

		//long riffLen = 0;
		//riffLen |= SuperReader.bytesToInt(buffer,SuperReader.LITTLE_ENDIAN);

		read(buffer);
		chunkName = new String(buffer, "ASCII");
		// check for WAVE
		if (!chunkName.equals("WAVE"))
			throw new IOException("Not a WAV file.");

		read(buffer);
		chunkName = new String(buffer, "ASCII");
		// check for format chunk.
		if (!chunkName.equals("fmt "))
			throw new IOException("Not a WAV file.");

		// read format chunk length.
		read(buffer);
		int len = SuperReader.bytesToInt(buffer,SuperReader.LITTLE_ENDIAN);

		byte[] formatChunkData = new byte[len];
		read(formatChunkData);
		
		soundInfo = readFormat(new ByteArrayInputStream(formatChunkData));
		if (soundInfo == null)
			throw new IOException("Unsupported data type in WAV.");

		soundInfo.setEndianMode(endian);
		soundInfo.setSampleType(soundInfo.getBitsPerSample() == 16 ? 
				SampleType.INTEGER_SIGNED : SampleType.INTEGER_UNSIGNED);
		
		// skip chunks until we find "data"
		read(buffer);
		chunkName = new String(buffer, "ASCII");
		while (!chunkName.equals("data"))
		{
			long size = 0;
			size |= read();
			size |= read() << 8;
			size |= read() << 16;
			size |= read() << 24;
			seek(getFilePointer() + size);
			
			if (getFilePointer() >= length())
				throw new IOException("WAV file with no data chunk!");
			
			read(buffer);
			chunkName = new String(buffer, "ASCII");
		}
		
		dataLength = 0;
		dataLength |= read();
		dataLength |= read() << 8;
		dataLength |= read() << 16;
		dataLength |= read() << 24;
		
		dataOffset = getFilePointer();
	}
	
	/**
	 * Reads the format chunk.
	 */
	private SoundFileInfo readFormat(InputStream in) throws IOException
	{
		SoundFileInfo info = new SoundFileInfo();
		SuperReader sr = new SuperReader(in, SuperReader.LITTLE_ENDIAN);
		
		// must be 1 for PCM.
		if (sr.readUnsignedShort() != 1)
			return null;
		
		info.setChannels(sr.readUnsignedShort());
		info.setSampleRate(sr.readInt());
		
		info.setBytesPerSecond(sr.readInt());
		info.setBitsPerSecond(info.getBytesPerSecond() * 8);
		
		info.setBytesPerSample(sr.readUnsignedShort() / info.getChannels());
		info.setBitsPerSample(sr.readUnsignedShort());
				
		return info;
	}
	
	/**
	 * Returns the sound info object for this WAV file.
	 */
	public SoundFileInfo getSoundInfo()
	{
		return soundInfo;
	}
	
	/**
	 * Gets the length of the DATA chunk in this WAV file (WAV data in bytes).
	 */
	public long getDataLength()
	{
		return dataLength;
	}
	
	/**
	 * Gets the exact position where the data in the data chunk starts.
	 */
	public long getDataOffset()
	{
		return dataOffset;
	}
	
	/**
	 * Convenience method for:</br>
	 * <code>seek(getDataOffset())</code>
	 * @throws IOException if an I/O error occurs because of the seek.
	 */
	public void seekToData() throws IOException
	{
		seek(getDataOffset());
	}
	
	/**
	 * Reads a bunch of samples from the WAV file and outputs them into <code>sampleOut</code>,
	 * in channel order. This method expects the length of the first dimension of <code>sampleOut</code>
	 * to be equal to the number of channels. This also advances the file pointer.
	 * @param sampleOut the arrays to put the samples into.
	 * @return the amount of samples read. if 0, the end of the data chunk was reached.
	 * @throws IOException if the pointer is not within the data chunk of the WAV or the data cannot be read.
	 * @throws IllegalArgumentException if sampleOut.length != <code>getSoundInfo().getChannels()</code>,
	 * 		or the arrays in sampleOut are not the same length.
	 */
	public int readSamples(double[] ... sampleOut) throws IOException
	{
		long ptrend = dataOffset + dataLength;
		long ptr = getFilePointer();
		if (ptr < dataOffset || ptr > ptrend)
			throw new IOException("File pointer not within DATA chunk.");
		if (sampleOut.length != soundInfo.getChannels())
			throw new IllegalArgumentException("Output dimensions do not equal channels.");

		int dl = 0;
		for (double[] d : sampleOut)
		{
			if (dl == 0)
				dl = d.length;
			else if (dl != d.length)
				throw new IllegalArgumentException("Output arrays do not have equal dimensions.");
		}

		if (ptr == ptrend)
			return 0;
		
		boolean signed = soundInfo.getSampleType() == SampleType.INTEGER_SIGNED;
		boolean le = soundInfo.getEndianMode() == SampleEndian.LITTLE_ENDIAN;
		int bytes = soundInfo.getBytesPerSample();
		int channels = soundInfo.getChannels();
		
		int out = 0;
		while (out < dl && ptr < ptrend)
		{
			for (int c = 0; c < channels; c++)
				sampleOut[c][out] = readPCM(bytes, signed, le);
			ptr += channels * bytes;
			out++;
		}
		
		return out;
	}
	
	/**
	 * Reads a chunk of PCM data.
	 * @param bytes the number of bytes to read.
	 * @param signed if the number read is signed.
	 * @param littleEndian if true, read bytes as little-endian, else big-endian.
	 * @return the read sample as a signed value from -1 to 1.
	 */
	private double readPCM(int bytes, boolean signed, boolean littleEndian) throws IOException
	{
		long lo = BOUND_LOOKUP[bytes - 1][signed ? 1 : 0][0];
		long hi = BOUND_LOOKUP[bytes - 1][signed ? 1 : 0][1];
		long readLong = 0L;

		for (int i = 0; i < bytes; i++)
		{
			if (littleEndian)
				readLong |= read() << (i * 8);
			else
			{
				readLong |= read();
				if (i < bytes - 1)
					readLong <<= 8;
			}
		}
		
		if (signed && readLong >= SIGNED_SUBTRAHEND[bytes - 1] / 2)
			readLong -= SIGNED_SUBTRAHEND[bytes - 1];
		
		return RMath.getInterpolationFactor(readLong, lo, hi) * 2.0 - 1.0;
	}

}
