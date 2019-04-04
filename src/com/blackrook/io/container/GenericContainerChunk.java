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
 * Chunk interface for the generic container reader/writer.
 * @author Matthew Tropiano
 */
public interface GenericContainerChunk
{
	/** 
	 * Converts the entirety of this chunk to a byte string, chunk
	 * headers, payload, and all.
	 * @return a byte array representation of the entrie chunk.
	 */
	public abstract byte[] toByteArray();

	/** Returns this chunk's data payload as an array of bytes. */
	public byte[] getPayload();
	
	/** Returns this chunk's data payload as an input stream. */
	public InputStream getStream() throws IOException;
	
	
}
