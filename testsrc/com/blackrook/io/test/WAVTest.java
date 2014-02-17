/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.io.test;

import com.blackrook.commons.Common;
import com.blackrook.io.files.wav.WAVFile;

public class WAVTest
{
	public static void main(String[] args) throws Exception
	{
		WAVFile wav = new WAVFile("d:/Windows Crap/Sound/Windows XP Startup with Tyreal.wav");
		double[] d = new double[512000];
		double[] d2 = new double[512000];
		int b = wav.readSamples(d, d2);
		for (int i = 0; i < b; i++)
			System.out.println((d[i] * 0.5) + (d2[i] * 0.5));
		Common.close(wav);
	}
}
