/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;

import com.blackrook.commons.Common;
import com.blackrook.commons.hash.CaseInsensitiveHashMap;
import com.blackrook.commons.hash.HashMap;

/**
 * A class used for logging messages to log files.
 * This is a useful class for programs that have 
 * a use for multi-file or any other kind of logging.
 * <p>
 * Please note that the logs are written in the system's preferred encoding.
 * @author Matthew Tropiano
 * @deprecated Seriously, this is terrible. Use com.blackrook.commons.logging.
 */
public final class Logger
{
	/** The file table. */
	private static HashMap<String,PrintWriter> fileTable;
	
	private Logger() {}
	
	static
	{
		if (Common.isWindows())
			fileTable = new CaseInsensitiveHashMap<PrintWriter>(); 
		else
			fileTable = new HashMap<String,PrintWriter>();
	}
	
	/**
	 * Sends a message to a log file.
	 * Auto-flushes on print.
	 * @param filename	the name of the log file.
	 * @param message	the message object to send (converted to String).
	 * @return	true if written, false otherwise.
	 */
	public static final boolean log(String filename, Object message)
	{
		return log(filename, false, message);
	}
	
	/**
	 * Sends a message to a log file.
	 * Auto-flushes on print.
	 * @param filename	the name of the log file.
	 * @param addDate	if true, adds the date and time to the beginning of the line.
	 * @param message	the message object to send (converted to String).
	 * @return	true if written, false otherwise.
	 */
	public static final boolean log(String filename, boolean addDate, Object message)
	{
		PrintWriter pw = getWriter(filename);
		if (pw != null)
		{
			pw.println((addDate ? (new Date()+": ") : "")+message.toString());
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Sends a message to a log file.
	 * Appends a newline at the end automatically.
	 * Auto-flushes on print.
	 * @param filename	the name of the log file.
	 * @param message	the (formatted) message to send.
	 * @param args		the list of arguments (see PrintStream.printf()).
	 * @return	true if written, false otherwise.
	 */
	public static final boolean log(String filename, String message, Object... args)
	{
		return log(filename, false, message, args);
	}
		
	/**
	 * Sends a message to a log file.
	 * Appends a newline at the end automatically.
	 * Auto-flushes on print.
	 * @param filename	the name of the log file.
	 * @param addDate	if true, adds the date and time to the beginning of the line.
	 * @param message	the (formatted) message to send.
	 * @param args		the list of arguments (see PrintStream.printf()).
	 * @return	true if written, false otherwise.
	 */
	public static final boolean log(String filename, boolean addDate, String message, Object... args)
	{
		PrintWriter pw = getWriter(filename);
		if (pw != null)
		{
			pw.println((addDate ? (new Date()+": ") : "")+String.format(message, args));
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Returns the entire contents of a log file.
	 * @param filename the filename path of which to read the contents.
	 * @return the log's contents as a contiguous String or null if the file
	 * cannot be opened or read or any reason.
	 */
	public static final String getContents(String filename)
	{
		InputStream in = null;
		String out = null;
		try {
			in = new FileInputStream(new File(filename));
			if (in != null)
			{
				out = Common.getTextualContents(in);
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return out;
	}
	
	/**
	 * Gets a PrintWriter for a particular file.
	 * If one doesn't exist, one is created.
	 * If a PrintWriter can't be made, this returns null.
	 */
	private static PrintWriter getWriter(String filePath)
	{
		File f = new File(filePath);
		PrintWriter out = null;
		try 
		{
			if (fileTable.containsKey(filePath))
				return fileTable.get(filePath);
			else
			{
				if (Common.createPathForFile(f))
					out = new PrintWriter(new FileOutputStream(f,true),true);
				else
					System.err.println("Could not open a PrintWriter for a log file.");
			}
		} catch (IOException e) {
			System.err.println("An I/O Exception has occurred opening a PrintWriter for a log file.");
			e.printStackTrace(System.err);
		}
		
		return out;
	}

}
