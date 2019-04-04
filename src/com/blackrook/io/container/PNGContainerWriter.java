/*******************************************************************************
 * Copyright (c) 2009-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.container;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.blackrook.io.SuperReader;
import com.blackrook.io.SuperWriter;
import com.blackrook.commons.math.CRC32;

public class PNGContainerWriter extends SuperWriter
{
	/** PNG Header. */
	private static final byte[] PNG_HEADER = {
		(byte)0x089, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
	};
	
	/** PNG CRC32 generator. */
	private static final CRC32 PNG_CRC = new CRC32(CRC32.POLYNOMIAL_IEEE);
	
	/** Did we write the header, yet? */
	private boolean wroteHeader;
	
	/**
	 * Creates a new PNG container reader from a file.
	 */
	public PNGContainerWriter(File f) throws IOException
	{
		this(new FileOutputStream(f));
	}
	
	/**
	 * Creates a new PNG container reader using an input stream.
	 */
	public PNGContainerWriter(OutputStream out) throws IOException
	{
		super(out, SuperReader.BIG_ENDIAN);
	}
	
	/** Starts the PNG header. Called if not called yet. */
	protected void startHeader() throws IOException
	{
		writeBytes(PNG_HEADER);
	}

	/**
	 * Writes the next chunk in this container stream.
	 * @param name	the name of the chunk. Must be length 4 (excluding whitespace), 
	 * 				and follow the guidelines for naming necessary/private/etc. chunks.
	 * @param data	the data to write.
	 * @throws IOException	if the write could not occur.
	 */
	public void writeChunk(String name, byte[] data) throws IOException
	{
		if (name.trim().length() != 4)
			throw new IllegalArgumentException("Name must be 4 alphabetical characters long.");
		
		if (!wroteHeader)
		{
			startHeader();
			wroteHeader = true;
		}
		
		writeInt(data.length);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SuperWriter sw = new SuperWriter(out, SuperWriter.BIG_ENDIAN);
		sw.writeASCIIString(name);
		sw.writeBytes(data);
		byte[] bytes = out.toByteArray();
		writeBytes(bytes);
		writeInt(PNG_CRC.createCRC32(bytes));
	}

}
