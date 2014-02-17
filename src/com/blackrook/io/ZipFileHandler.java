/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.*;

import com.blackrook.commons.Common;
import com.blackrook.commons.hash.CaseInsensitiveHashMap;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.list.List;


/**
 * This is a class that is dedicated to managing a whole bunch of stuff
 * regarding unzipping stuff and managing the unzipped contents. This does
 * A LOT of crap with I/O, so its performance will be bottlenecked by I/O.
 * 
 * The finalize method of this class cleans up all of the files unzipped by this
 * handler class.
 * 
 * @author Matthew Tropiano
 */
public class ZipFileHandler
{
	private static final File WORK_DIR = new File(Common.WORK_DIR);
	
	/** FileFilter that accepts all files. */
	public static final EntryFilter ALL_FILES = new EntryFilter()
	{
		@Override
		public boolean accept(ZipEntry arg0)
		{
			return true;
		}
};
	
	/** A table that matches the source zip file to a reference to an unzipped file. */
	private HashMap<String,File> unzipTable;
	/** The temporary directory to unzip the contents of zip files to. */
	private File tempDir;
	
	/**
	 * Creates a new handler that uses the current working directory as the temporary directory.
	 */
	public ZipFileHandler()
	{
		this(WORK_DIR);
	}
	
	/**
	 * Creates a new handler that uses a defined directory as the temporary directory.
	 * @param temporaryDirectory		the directory to use as the temporary directory.
	 * @throws IllegalArgumentException	if temporaryDirectory is not a directory.
	 */
	public ZipFileHandler(File temporaryDirectory)
	{
		if (temporaryDirectory.exists() && !temporaryDirectory.isDirectory())
			throw new IllegalArgumentException("The temp directory reference cannot be a file.");
		
		tempDir = temporaryDirectory;
		
		if (Common.isWindows())
			unzipTable = new CaseInsensitiveHashMap<File>(10,10);
		else
			unzipTable = new HashMap<String,File>(10,10);
	}
	
	/**
	 * Unzips all files to the temporary directory under a directory for the 
	 * zip file from another zip file and logs it internally in this class 
	 * instance for cleanup later.
	 * @param zf					the source zip file.
	 * @return 						an array of valid references to the files unzipped, in the order that they were unzipped.
	 */
	public File[] unzipAllFiles(ZipFile zf) throws IOException
	{
		return unzipAllFiles(zf,ALL_FILES);
	}
	
	/**
	 * Unzips all files to the temporary directory under a directory for the 
	 * zip file from another zip file and logs it internally in this class 
	 * instance for cleanup later.
	 * @param key					a keyword for tracking files unzipped.
	 * @param zf					the source zip file.
	 * @return 						an array of valid references to the files unzipped, in the order that they were unzipped.
	 */
	public File[] unzipAllFiles(String key, ZipFile zf) throws IOException
	{
		return unzipAllFiles(key,zf,ALL_FILES);
	}
	
	/**
	 * Unzips a bunch of files to the temporary directory under a directory for the 
	 * zip file from another zip file, preserving archive paths, and logs it internally in this class 
	 * instance for cleanup later.
	 * @param zf					the source zip file.
	 * @param filter				the entry filter to use for finding acceptable entries to unzip.
	 * @return 						an array of valid references to the files unzipped, in the order that they were unzipped.
	 */
	public File[] unzipAllFiles(ZipFile zf, EntryFilter filter) throws IOException
	{
		return unzipAllFiles(zf,filter,new File(tempDir.getPath()+File.separatorChar+getZipNameForDirectory(zf)),true);
	}
	
	/**
	 * Unzips a bunch of files to the temporary directory under a directory for the 
	 * zip file from another zip file, preserving archive paths, and logs it internally in this class 
	 * instance for cleanup later.
	 * @param key					a keyword for tracking files unzipped.
	 * @param zf					the source zip file.
	 * @param filter				the entry filter to use for finding acceptable entries to unzip.
	 * @return 						an array of valid references to the files unzipped, in the order that they were unzipped.
	 */
	public File[] unzipAllFiles(String key, ZipFile zf, EntryFilter filter) throws IOException
	{
		return unzipAllFiles(key,zf,filter,new File(tempDir.getPath()+File.separatorChar+getZipNameForDirectory(zf)),true);
	}
	
	/**
	 * Unzips a bunch of files from another zip file and logs it internally 
	 * in this class instance for cleanup later.
	 * @param zf					the source zip file.
	 * @param filter				the entry filter to use for finding acceptable entries to unzip.
	 * @param targetDirectory		the directory to use as the target directory for the file.
	 * @param preservePath			should the directory path in the zip file be preserved on unzip when the file is created?
	 * @return 						an array of valid references to the files unzipped, in the order that they were unzipped.
	 */
	public File[] unzipAllFiles(ZipFile zf, EntryFilter filter, 
			File targetDirectory, boolean preservePath) throws IOException
	{
		return unzipAllFiles(getZipNameForDirectory(zf),zf,filter,targetDirectory,preservePath);
	}
	
	/**
	 * Unzips a bunch of files from another zip file and logs it internally 
	 * in this class instance for cleanup later.
	 * @param key					a keyword for tracking files unzipped.
	 * @param zf					the source zip file.
	 * @param filter				the entry filter to use for finding acceptable entries to unzip.
	 * @param targetDirectory		the directory to use as the target directory for the file.
	 * @param preservePath			should the directory path in the zip file be preserved on unzip when the file is created?
	 * @return 						an array of valid references to the files unzipped, in the order that they were unzipped.
	 */
	public File[] unzipAllFiles(String key, ZipFile zf, EntryFilter filter, 
			File targetDirectory, boolean preservePath) throws IOException
	{
		List<File> list = new List<File>(zf.size());
		Enumeration<? extends ZipEntry> entries = zf.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry ze = entries.nextElement();
			if (filter.accept(ze))
				list.add(unzip(key,zf,ze,targetDirectory,preservePath));
		}
		
		File[] out = new File[list.size()];
		list.toArray(out);
		return out;
	}
	
	/**
	 * Unzips a file to the temporary directory from another zip file and logs it internally 
	 * in this class instance for cleanup later, preserving archive paths.
	 * @param key					a keyword for tracking files unzipped.
	 * @param zf					the source zip file.
	 * @param ze					the entry in the zip file to unzip.
	 * @return 						null if the entry is not a valid entry in the file, 
	 * 								or a valid reference to the file unzipped.
	 * @throws IOException			if something happens during the unzipping that shouldn't happen.
	 * @throws SecurityException	if the target cannot be made by the current user, or some other security violation occurs.
	 */
	public File unzip(String key, ZipFile zf, ZipEntry ze) throws IOException
	{
		return unzip(key,zf,ze,new File(tempDir.getPath()+File.separatorChar+getZipNameForDirectory(zf)));
	}

	/**
	 * Unzips a file to the temporary directory from another zip file and logs it internally 
	 * in this class instance for cleanup later, preserving archive paths.
	 * @param zf					the source zip file.
	 * @param ze					the entry in the zip file to unzip.
	 * @return 						null if the entry is not a valid entry in the file, 
	 * 								or a valid reference to the file unzipped.
	 * @throws IOException			if something happens during the unzipping that shouldn't happen.
	 * @throws SecurityException	if the target cannot be made by the current user, or some other security violation occurs.
	 */
	public File unzip(ZipFile zf, ZipEntry ze) throws IOException
	{
		return unzip(zf,ze,new File(tempDir.getPath()+File.separatorChar+getZipNameForDirectory(zf)));
	}

	/**
	 * Unzips a file from another zip file and logs it internally 
	 * in this class instance for cleanup later, preserving archive paths.
	 * @param key					a keyword for tracking files unzipped.
	 * @param zf					the source zip file.
	 * @param ze					the entry in the zip file to unzip.
	 * @param targetDirectory		the directory to use as the target directory for the file.
	 * @return 						null if the entry is not a valid entry in the file, 
	 * 								or a valid reference to the file unzipped.
	 * @throws IOException			if something happens during the unzipping that shouldn't happen.
	 * @throws SecurityException	if the target cannot be made by the current user, or some other security violation occurs.
	 */
	public File unzip(String key, ZipFile zf, ZipEntry ze, File targetDirectory) throws IOException
	{
		return unzip(key,zf,ze,targetDirectory,true);
	}

	/**
	 * Unzips a file from another zip file and logs it internally 
	 * in this class instance for cleanup later, preserving archive paths.
	 * @param zf					the source zip file.
	 * @param ze					the entry in the zip file to unzip.
	 * @param targetDirectory		the directory to use as the target directory for the file.
	 * @return 						null if the entry is not a valid entry in the file, 
	 * 								or a valid reference to the file unzipped.
	 * @throws IOException			if something happens during the unzipping that shouldn't happen.
	 * @throws SecurityException	if the target cannot be made by the current user, or some other security violation occurs.
	 */
	public File unzip(ZipFile zf, ZipEntry ze, File targetDirectory) throws IOException
	{
		return unzip(zf,ze,targetDirectory,true);
	}

	/**
	 * Unzips a file from another zip file and logs it internally 
	 * in this class instance for cleanup later.
	 * @param zf					the source zip file.
	 * @param ze					the entry in the zip file to unzip.
	 * @param targetDirectory		the directory to use as the target directory for the file.
	 * @param preservePath			should the directory path in the zip file be preserved on unzip when the file is created?
	 * @return 						null if the entry is not a valid entry in the file, 
	 * 								or a valid reference to the file unzipped.
	 * @throws IOException			if something happens during the unzipping that shouldn't happen.
	 * @throws SecurityException	if the target cannot be made by the current user, or some other security violation occurs.
	 */
	public File unzip(ZipFile zf, ZipEntry ze, 
			File targetDirectory, boolean preservePath) throws IOException
	{
		return unzip(getZipNameForDirectory(zf),zf,ze,targetDirectory,preservePath);
	}
	
	/**
	 * Unzips a file from another zip file and logs it internally 
	 * in this class instance for cleanup later.
	 * @param key					a keyword for tracking files unzipped.
	 * @param zf					the source zip file.
	 * @param ze					the entry in the zip file to unzip.
	 * @param targetDirectory		the directory to use as the target directory for the file.
	 * @param preservePath			should the directory path in the zip file be preserved on unzip when the file is created?
	 * @return 						null if the entry is not a valid entry in the file, 
	 * 								or a valid reference to the file unzipped.
	 * @throws IOException			if something happens during the unzipping that shouldn't happen.
	 * @throws SecurityException	if the target cannot be made by the current user, or some other security violation occurs.
	 */
	public File unzip(String key, ZipFile zf, ZipEntry ze, 
			File targetDirectory, boolean preservePath) throws IOException
	{
		if (targetDirectory.exists() && !targetDirectory.isDirectory())
			throw new IllegalArgumentException("The target directory reference is not a directory: "+targetDirectory.getPath());
		
		String targetPath = ze.getName();
		String targetName = (targetPath.lastIndexOf("/") > -1) ? 
				targetPath.substring(targetPath.lastIndexOf("/")+1,targetPath.length()) : 
				targetPath;
		
		File outFile = null;
		if (preservePath)
			outFile = new File(targetDirectory.getPath()+File.separatorChar+targetPath);
		else
			outFile = new File(targetDirectory.getPath()+File.separatorChar+targetName);
		
		if (!Common.createPathForFile(outFile))
			return null;
		
		InputStream inStream = zf.getInputStream(ze);
		OutputStream outStream = new FileOutputStream(outFile);

		Common.relay(inStream, outStream);
		
		inStream.close();
		outStream.close();
		
		unzipTable.put(key, outFile);
		
		return outFile;
	}
	
	/**
	 * Returns true if all files tied to this key have been deleted.
	 */
	public boolean cleanUpAllUsing(String key)
	{
		if (!unzipTable.containsKey(key))
			return false;
		
		File f = null;
		while ((f = unzipTable.removeUsingKey(key)) != null)
		{
			f.delete();
			File parent = f.getParentFile();
			if (parent != null && parent.list() != null && parent.list().length == 0)
				parent.delete();
		}
		
		return true;
	}
	
	/**
	 * Returns true if all files have been deleted after being unzipped from a zip file.
	 */
	public boolean cleanUpAllFrom(ZipFile zf)
	{
		return cleanUpAllUsing(getZipNameForDirectory(zf));
	}
	
	/**
	 * Returns true if all files tracked by this handler have been deleted.
	 */
	public boolean cleanUpAll()
	{
		Iterator<File> it = unzipTable.valueIterator();
		while (it.hasNext())
		{
			File f = it.next();
			f.delete();
			File parent = f.getParentFile();
			if (parent != null && parent.list().length == 0)
				parent.delete();
		}
		
		unzipTable.clear();
		return true;
	}
	
	/**
	 * Returns the reference to the default temporary directory.
	 */
	public final File getTemporaryDirectory()
	{
		return tempDir;
	}
	
	private String getZipPath(ZipFile zf)
	{
		return zf.getName();
	}
	
	private String getZipNameForDirectory(ZipFile zf)
	{
		String targetPath = getZipPath(zf);
		int sindex = targetPath.lastIndexOf(File.separator); 
		if (sindex > -1)
			return targetPath.substring(targetPath.lastIndexOf(File.separator)+1,targetPath.length()).replace('.', '_');
		else	
			return targetPath.replace('.', '_');
	}
	
	@Override
	public void finalize() throws Throwable
	{
		cleanUpAll();
		super.finalize();
	}
	
	/**
	 * Filter class for bulk unzip.
	 * @author Matthew Tropiano
	 */
	public abstract static class EntryFilter
	{
		protected EntryFilter()	{}
		
		/**
		 * Returns true if this entry is accepted by the filter.
		 */
		public abstract boolean accept(ZipEntry ze);
		
	}

}
