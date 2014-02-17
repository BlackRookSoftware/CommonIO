/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.files;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

/**
 * Abstract reader for text files that has a logic step for eliminating insignificant lines.
 * @author Matthew Tropiano
 */
public abstract class AbstractTextFileReader implements Closeable
{
	/** Encapsulated reader. */
	private BufferedReader reader;
	
	/** Current line number (1 ... n). */
	private int lineNumber;
	/** Current line index (0 ... n). */
	private int lineIndex;
	/** Current significant line (1 ... n, not skipped). */
	private int significantLineNumber;

	/**
	 * Creates a new aAbstractTextFileReader wrapped around a {@link BufferedReader}.
	 * @param br the {@link BufferedReader} to wrap.
	 */
	protected AbstractTextFileReader(BufferedReader br)
	{
		this.reader = br;
		this.lineNumber = 0;
		this.lineIndex = -1;
		this.significantLineNumber = 0;
	}
	
	/**
	 * Returns the current line number read (from 1 ... n).
	 */
	public int getLineNumber()
	{
		return lineNumber;
	}
	
	/**
	 * Returns the current line index read (from 0 ... n), one less than {@link #getLineNumber()}.
	 */
	public int getLineIndex()
	{
		return lineIndex;
	}

	/**
	 * Returns the current <i>significant</> line number.
	 * Counts unskipped lines.
	 */
	public int getSignificantLineNumber()
	{
		return significantLineNumber;
	}
	
	/**
	 * Reads and returns the next significant line of text.
	 * According to {@link BufferedReader}, a line is considered to be 
	 * terminated by any one of a line feed ('\n'), a carriage return ('\r'), 
	 * or a carriage return followed immediately by a linefeed.
	 * Calls {@link #skipLine(String, String, int, int)} to figure out what is significant or not.
	 * @return the next significant line 
	 * @throws IOException if an error occurs during read.
	 * @see #skipLine(String, String, int, int)
	 */
	public String readLine() throws IOException
	{
		String line = null;
		boolean good = false;
		while (!good && (line = reader.readLine()) != null)
		{
			lineNumber++;
			lineIndex++;
			
			String trimmedLine = line;

			// do we need to trim? trimming can be expensive!
			if (line.length() > 0 && (Character.isWhitespace(line.charAt(0)) || Character.isWhitespace(line.charAt(line.length()-1))))
				trimmedLine = line.trim();
			
			if (skipLine(line, trimmedLine, lineNumber, lineIndex))
				continue;
			else
			{
				significantLineNumber++;
				good = true;
			}
			
		}
		return line;
	}

	/**
	 * Called by {@link #readLine()}, this decides whether to skip a line or not.
	 * @param line the line read.
	 * @param trimmedLine the same line, but with whitespace trimmed from it.
	 * @param number the line number.
	 * @param index the line index.
	 * @return true if this line can be skipped, false if not.
	 */
	protected abstract boolean skipLine(String line, String trimmedLine, int number, int index);
	
	/**
	 * Closes the encapsulated reader.
	 * @throws IOException if an error happened during the close.
	 */
	@Override
	public void close() throws IOException
	{
		reader.close();
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
	
}
