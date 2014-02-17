/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.blackrook.io.SuperReader;

public class PNGContainerReader extends SuperReader
{
	/** PNG Header. */
	private static final byte[] PNG_HEADER = {
		(byte)0x089, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
	};
	
	/**
	 * Creates a new PNG container reader from a file.
	 */
	public PNGContainerReader(File f) throws IOException
	{
		this(new FileInputStream(f));
	}
	
	/**
	 * Creates a new PNG container reader using an input stream.
	 */
	public PNGContainerReader(InputStream i) throws IOException
	{
		super(i, SuperReader.BIG_ENDIAN);
		checkHeader();
	}
	
	/** Checks the PNG header. Throws an Exception if bad. */
	protected void checkHeader() throws IOException
	{
		if (!Arrays.equals(PNG_HEADER, readBytes(8)))
			throw new IOException("Not a PNG file. Header may be corrupt.");
	}

	/**
	 * Reads the next chunk in this container stream.
	 */
	public Chunk nextChunk() throws IOException
	{
		Chunk chunk = null;
		try {chunk = new Chunk(this);	} catch (EOSException e) {}
		return chunk;
	}

	/**
	 * PNG Chunk data.
	 */
	public static class Chunk
	{
		/** Chunk name. */
		private String name;
		/** CRC number. */
		private int crcNumber;
		/** Data. */
		private byte[] data;
		
		Chunk(SuperReader sr) throws IOException
		{
			int len = sr.readInt();
			name = sr.readASCIIString(4).trim();
			data = sr.readBytes(len);
			crcNumber = sr.readInt();
		}

		/**
		 * Gets this chunk's identifier.
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Gets this chunk's CRC value.
		 */
		public int getCRCNumber()
		{
			return crcNumber;
		}

		/**
		 * Gets the data in this chunk.
		 */
		public byte[] getData()
		{
			return data;
		}
		
		@Override
		public String toString()
		{
			return name + " Length: " + data.length + " CRC: " + String.format("%08x", crcNumber);
		}
		
		/**
		 * Is this chunk not a part of the required image chunks?
		 */
		public boolean isAncillary()
		{
			return Character.isLowerCase(name.charAt(0));
		}
		
		/**
		 * Is this chunk part of a non-public specification?
		 */
		public boolean isPrivate()
		{
			return Character.isLowerCase(name.charAt(1));
		}
		
		/**
		 * Does this chunk have the reserved bit set?
		 */
		public boolean isReserved()
		{
			return Character.isLowerCase(name.charAt(2));
		}

		/**
		 * Is this chunk safe to blindly copy, requiring no
		 * other chunks and contains no image-centric data?
		 */
		public boolean isSafeToCopy()
		{
			return Character.isLowerCase(name.charAt(3));
		}
	}
	
}
