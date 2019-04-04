/*******************************************************************************
 * Copyright (c) 2009-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.files;

import java.io.BufferedReader;

/**
 * A text file reader that automatically skips blank lines and lines that begin
 * with a specified prefix.
 * @author Matthew Tropiano
 */
public class ConfigReader extends AbstractTextFileReader
{
	/** The prefix that designates a comment. */
	private String commentPrefix;
	
	/**
	 * Creates a new ConfigReader that encapsulates a BufferedReader.
	 * @param br the reader to read from.
	 */
	public ConfigReader(BufferedReader br, String commentPrefix)
	{
		super(br);
		this.commentPrefix = commentPrefix;
	}

	@Override
	protected boolean skipLine(String line, String trimmedLine, int number, int index)
	{
		return trimmedLine.length() == 0 || trimmedLine.startsWith(commentPrefix);
	}

}
