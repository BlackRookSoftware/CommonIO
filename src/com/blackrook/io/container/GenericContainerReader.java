/*******************************************************************************
 * Copyright (c) 2009-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.container;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is a class that provides a means for reading a
 * generic container file or stream and provides an opportunity
 * for chunk CRC. The purpose of this file is to provide a skeletal
 * base for container readers.
 * 
 * @author Matthew Tropiano
 */
public abstract class GenericContainerReader<H extends GenericContainerHeader, C extends GenericContainerChunk>
{
	/** The container header encapsulation object. */
	protected H header;
	/** The reference to the input stream passed to the constructor. */
	protected InputStream inStream;
	
	/** 
	 * Common constructor for all GenericContainerReaders.
	 * This constructor calls <code>readHeader(in)</code> and assigns
	 * its return object to the internal header field (obtainable through
	 * <code>getHeader()</code>).
	 * <p>The InputStream passed to this constructor is stored internally. 
	 */
	protected GenericContainerReader(InputStream in) throws IOException
	{
		inStream = in;
		header = readHeader(in);
	}
	
	/**
	 * Called by the constructor <code>GenericContainerReader(InputStream in)</code> in
	 * order to gather necessary header info for the container.
	 * @param in the input stream to read from.
	 * @return a header object containing all of the necessary header information.
	 * @throws IOException if something unexpected was read or the header is improperly stored.
	 */
	protected abstract H readHeader(InputStream in) throws IOException;
	
	/**
	 * Returns this container's header.
	 */
	public H getHeader()
	{
		return header;
	}
	
	/**
	 * Gets the next chunk in the container.
	 * Should return null if there are no chunks left.
	 * @return an encapsulation of the chunk data, 
	 * or null if the end of the stream has been reached.
	 */
	public abstract C getNextChunk() throws IOException;
	
	/**
	 * Closes the reader, and the underlying stream that it reads from.
	 * @throws IOException if the stream cannot be closed.
	 */
	public void close() throws IOException
	{
		inStream.close();
	}
	
}
