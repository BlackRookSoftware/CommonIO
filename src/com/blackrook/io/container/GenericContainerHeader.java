/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.container;

/**
 * Header encapsulation interface for the generic container reader/writer.
 * @author Matthew Tropiano
 */
public interface GenericContainerHeader
{
	/** 
	 * Converts this header to a byte string.
	 * @return a byte array representation of this header object.
	 */
	public abstract byte[] toByteArray();
	
}
