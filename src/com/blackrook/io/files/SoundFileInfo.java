/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.files;

/**
 * A class that holds rudimentary info about a sound file.
 * As this is for information purposes, calling the setter methods will not
 * actually change the characteristics of the underlying file.
 * @author Matthew Tropiano
 * @since 2.4.0
 */
public class SoundFileInfo
{
	/**
	 * Sample Type.
	 */
	public static enum SampleType
	{
		/** Uses signed integers. */
		UNKNOWN,
		/** Uses signed integers. */
		INTEGER_SIGNED,
		/** Uses unsigned integers. */
		INTEGER_UNSIGNED,
		/** Uses floating-point values. */
		FLOATING_POINT;
	}

	/**
	 * Endian mode of integer samples.
	 */
	public static enum SampleEndian
	{
		/** N/A */
		NOT_APPLICABLE,
		/** Lower byte first. */
		LITTLE_ENDIAN,
		/** Higher byte first. */
		BIG_ENDIAN;
	}
	
	/** Number of channels. */
	private int channels;
	/** Sample encoding type. */
	private SampleType sampleType;
	/** Sample endian mode. */
	private SampleEndian endianMode;

	/** Sample rate. */
	private int sampleRate;
	/** Bytes per sample. */
	private int bytesPerSample;
	/** Bits per sample. */
	private int bitsPerSample;

	/** Bytes per second. */
	private int bytesPerSecond;
	/** Bits per second. */
	private int bitsPerSecond;

	/**
	 * Creates an empty
	 */
	public SoundFileInfo()
	{
		setChannels(0);
		setSampleType(SampleType.UNKNOWN);
		setEndianMode(SampleEndian.NOT_APPLICABLE);
		setSampleRate(0);
		setBytesPerSample(0);
		setBitsPerSample(0);
		setBytesPerSecond(0);
		setBitsPerSecond(0);
	}
	
	/**
	 * Returns the amount of sound channels that this de-muxes to. 
	 */
	public int getChannels()
	{
		return channels;
	}
	
	/**
	 * Sets the amount of sound channels that this de-muxes to. 
	 */
	public void setChannels(int channels)
	{
		this.channels = channels;
	}
	
	/**
	 * Returns the sample encoding type. 
	 */
	public SampleType getSampleType()
	{
		return sampleType;
	}
	
	/**
	 * Sets the sample encoding type. 
	 */
	public void setSampleType(SampleType sampleType)
	{
		this.sampleType = sampleType;
	}
	
	/**
	 * Returns the sample endian mode (if integer - may return NOT_APPLICABLE otherwise). 
	 */
	public SampleEndian getEndianMode()
	{
		return endianMode;
	}
	
	/**
	 * Sets the sample endian mode (if integer - may be NOT_APPLICABLE otherwise). 
	 */
	public void setEndianMode(SampleEndian endianMode)
	{
		this.endianMode = endianMode;
	}
	
	/**
	 * Returns the sample rate in samples per second. 
	 */
	public int getSampleRate()
	{
		return sampleRate;
	}
	
	/**
	 * Sets the sample rate in samples per second. 
	 */
	public void setSampleRate(int sampleRate)
	{
		this.sampleRate = sampleRate;
	}
	
	/**
	 * Returns the number of bytes per sample. 
	 */
	public int getBytesPerSample()
	{
		return bytesPerSample;
	}
	
	/**
	 * Sets the number of bytes per sample. 
	 */
	public void setBytesPerSample(int bytesPerSample)
	{
		this.bytesPerSample = bytesPerSample;
	}
	
	/**
	 * Returns the number of bits per sample. 
	 */
	public int getBitsPerSample()
	{
		return bitsPerSample;
	}
	
	/**
	 * Sets the number of bits per sample. 
	 */
	public void setBitsPerSample(int bitsPerSample)
	{
		this.bitsPerSample = bitsPerSample;
	}
	
	/**
	 * Returns the number of bytes per second (byterate). 
	 */
	public int getBytesPerSecond()
	{
		return bytesPerSecond;
	}
	
	/**
	 * Sets the number of bytes per second (byterate). 
	 */
	public void setBytesPerSecond(int bytesPerSecond)
	{
		this.bytesPerSecond = bytesPerSecond;
	}
	
	/**
	 * Returns the number of bits per second (bitrate). 
	 */
	public int getBitsPerSecond()
	{
		return bitsPerSecond;
	}
	
	/**
	 * Sets the number of bits per second (bitrate). 
	 */
	public void setBitsPerSecond(int bitsPerSecond)
	{
		this.bitsPerSecond = bitsPerSecond;
	}
	

}
