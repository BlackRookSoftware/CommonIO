/*******************************************************************************
 * Copyright (c) 2009-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.container;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.blackrook.io.SuperReader;
import com.blackrook.commons.linkedlist.Queue;

/**
 * An Ogg stream reader.
 * @author Matthew Tropiano
 */
public class OggContainerReader extends SuperReader
{
	/***/
	protected static final byte[] OggS = new byte[]{0x4f,0x67,0x67,0x53};
	
	public static final int END_OF_PACKET = -1;

	/**
	 * Creates a new Ogg container reader from a file.
	 */
	public OggContainerReader(File f) throws IOException
	{
		this(new FileInputStream(f));
	}
	
	/**
	 * Creates a new Ogg container reader using an input stream.
	 */
	public OggContainerReader(InputStream i) throws IOException
	{
		super(i, SuperReader.LITTLE_ENDIAN);
	}

	/**
	 * Returns another OggPage for reading.
	 */
	public OggPage nextChunk() throws IOException
	{
		return readNextPage(getInputStream());
	}

	/**
	 * Seeks to the next the next Ogg page.
	 * @throws IOException
	 */
	protected OggPage readNextPage(InputStream in) throws IOException
	{
		if (!seekToPattern(OggS))
			return null;
		return new OggPage(new SuperReader(in,SuperReader.LITTLE_ENDIAN));
	}
	

	/**
	 * Chunk data encapsulation for Ogg streams.
	 */
	public static class OggPage
	{
		/** Version number. */
		private byte version;
		/** Header type. */
		private byte header;
		/** Granule position. */
		private long granule;
		/** Serial number. */
		private int serialNumber;
		/** Sequence number. */
		private int sequenceNumber;
		/** Checksum. */
		private int CRC;
		/** Page segments. */
		private short segments;

		/** The current reader/input stream. */
		ByteArrayInputStream currentPacketInputStream;
		SuperReader packetReader;
		int currentPacketBits;
		
		/** Chunk payload, separated into packets. */
		private Queue<byte[]> payload;

		/**
		 * Makes a new Ogg page.
		 */
		protected OggPage(SuperReader sr) throws IOException
		{
			payload = new Queue<byte[]>();
			version = sr.readByte();
			header = sr.readByte();
			granule = sr.readLong();
			serialNumber = sr.readInt();
			sequenceNumber = sr.readInt();
			CRC = sr.readInt();
			segments = (short)(sr.readByte() & 0x00ff);
			
			byte[] b = new byte[segments];
			short[] lacingValues = new short[segments];
			
			if (sr.readBytes(b) != b.length)
				throw new IOException("Incomplete or bad Ogg page data.");
			
			for (int i = 0; i < b.length; i++)
				lacingValues[i] = (short)(b[i] & 0x00ff);

			readPayload(sr, lacingValues);
			nextPacket();
		}
		
		private void readPayload(SuperReader sr, short[] lacingValues) throws IOException
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[255];
			for (int i = 0; i < lacingValues.length; i++)
			{
				int buf = sr.readBytes(b, lacingValues[i]);
				if (buf < 0)
					break;
				bos.write(b, 0, buf);
				
				if (buf < 255)
				{
					addPacket(bos.toByteArray());
					bos.reset();
				}
			}
			if (bos.size() > 0)
				addPacket(bos.toByteArray());
		}

		/**
		 * Reads a bit.
		 */
		public boolean readBit() throws IOException
		{
			return readBits(1) != 0;
		}
		
		/**
		 * Reads a bunch of bits and returns them in an int.
		 */
		public int readBits(int n) throws IOException
		{
			if (currentPacketBits == 0)
				return END_OF_PACKET;
			else if (currentPacketBits < n)
				n = currentPacketBits;
			int out = packetReader.readIntBits(n);
			currentPacketBits -= n;
			return out;
		}
		
		/**
		 * Reads a byte from the current packet.
		 */
		public byte readByte() throws IOException
		{
			return (byte)readBits(8);
		}
		
		/**
		 * Reads a UTF-8 string.
		 * @param bytes	amount of bytes to read.
		 */
		public String readString(int bytes) throws IOException
		{
			byte[] b = new byte[bytes];
			for (int i = 0; i < b.length; i++)
				b[i] = readByte();
			return new String(b,"UTF-8");
		}
		
		/**
		 * Reads an ASCII string, prefixed with a 32-bit length.
		 */
		public String readStringVector() throws IOException
		{
			return packetReader.readASCIIString(readBits(32));
		}
		
		/**
		 * Reads a bunch of bytes and checks to see if a set bytes match completely
		 * with the input byte string. It reads up to the length of b before it starts the check.
		 * @param b	the input byte string.
		 * @return true if the bytes read equal the the same bytes in the input array.
		 */
		public boolean readFor(byte[] b) throws IOException
		{
			byte[] read = new byte[b.length];

			for (int i = 0; i < b.length; i++)
				read[i] = readByte();
			
			for (int i = 0; i < b.length; i++)
				if (read[i] != b[i])
					return false;
			
			return true;
		}
		
		/**
		 * Prepares the next packet.
		 */
		public void nextPacket()
		{
			byte[] b = getPacket();
			if (b != null)
			{
				currentPacketInputStream = new ByteArrayInputStream(b);
				packetReader = new SuperReader(currentPacketInputStream,SuperReader.LITTLE_ENDIAN);
				currentPacketBits = b.length*8;
			}
			else
			{
				currentPacketInputStream = null;
				packetReader = null;
				currentPacketBits = 0;
			}
		}
		
		/**
		 * Does this page have any bits left to read in the current packet?
		 */
		public boolean hasBitsLeft()
		{
			return currentPacketBits > 0;
		}
		
		public int getVersion()				{return version;}
		public long getGranule()			{return granule;}
		public int getSerialNumber()		{return serialNumber;}
		public int getSequenceNumber()		{return sequenceNumber;}
		public int getCRC()					{return CRC;}
		public int getNumSegments()			{return segments;}

		public boolean isContinuingPacket()	{return (header & 0x01) != 0;}
		public boolean isFirstPage()		{return (header & 0x02) != 0;}
		public boolean isLastPage()			{return (header & 0x04) != 0;}
		
		/**
		 * Adds a new packet.
		 * @param bytes	the byte packet to add.
		 */
		protected void addPacket(byte[] bytes)
		{
			payload.enqueue(bytes);
		}
		
		/**
		 * Removes a packet from this chunk to read as a byte array.
		 */
		protected byte[] getPacket()
		{
			return payload.dequeue();
		}

		/**
		 * Does this page have any packets left to read?
		 */
		protected boolean hasPacketsLeft()
		{
			return !payload.isEmpty();
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("OggS ");
			sb.append(String.format("v%d, h%d, Granule %016x, Serial %08x, Seq %d, CRC %08x",
					version, header, granule, serialNumber, sequenceNumber, CRC
					));
			return sb.toString();
		}
		
	}
	
}
