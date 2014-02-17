/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.container;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is a class that provides a means for writing a
 * generic container file or stream. 
 * The purpose of this file is to provide a skeletal
 * base for container writers.
 * 
 * @author Matthew Tropiano
 */
public abstract class GenericContainerWriter<H extends GenericContainerHeader, C extends GenericContainerChunk>
{
	/** The reference to the output stream passed to the constructor. */
	protected OutputStream outStream;
	/** The "first write" flag. */
	protected boolean firstWrite;
	/** The container's header object. */
	protected H header;
	
	/** 
	 * Common constructor for all GenericContainerWriters.
	 * <p>The OutputStream passed to this constructor is stored internally, as is
	 * the header object.
	 * @param header the header object. This is written to the output stream on first chunk write.
	 * @param out the output stream to use for writing the container.  
	 */
	protected GenericContainerWriter(H header, OutputStream out) throws IOException
	{
		this.header = header;
		outStream = out;
		firstWrite = false;
	}
	
	/**
	 * Called by writeChunk(C) upon the first chunk write.
	 * The bytes written are taken from header.toByteArray().
	 * @throws IOException if the chunk could not be written.
	 */
	protected void writeHeader() throws IOException
	{
		outStream.write(header.toByteArray());
	}
	
	/**
	 * Writes a chunk to the container.
	 * @param chunk the chunk to write.
	 */
	public void writeChunk(C chunk) throws IOException
	{
		if (!firstWrite)
		{
			writeHeader();
			firstWrite = true;
		}
		
		outStream.write(chunk.toByteArray());
	}
	
	/**
	 * Closes the reader, and the underlying stream that it reads from.
	 * @throws IOException if the stream cannot be closed.
	 */
	public void close() throws IOException
	{
		outStream.close();
	}
	
}
