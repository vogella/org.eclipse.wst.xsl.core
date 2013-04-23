/******************************************************************************
 * Copyright (c) 2008 Lars Vogel 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/eplv10.html
 *
 * Contributors:
 * Lars Vogel - lars.vogel@vogella.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xsl.core.internal.ant;

import java.io.File;
import java.util.Collection;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.wst.xsl.core.internal.xinclude.XIncluder;

public class XIncludeTask extends Task {
	private String inFile;
	private String outFile;
	private boolean ignoreDotFiles = false;

	public void setIn(String inFile) {
		log("Setting the output File to: " + inFile, Project.MSG_VERBOSE);
		this.inFile = inFile;
	}

	public void setOut(String outFile) {
		log("Setting the output File to: " + outFile, Project.MSG_VERBOSE);
		this.outFile = outFile;
	}

	public void setIgnoreDotFiles(boolean ignore) {
		log("Dot files will be ignored: " + ignore, Project.MSG_VERBOSE);
		this.ignoreDotFiles = ignore;
	}

	@Override
	public void execute() {
		validate();
		log("Executing the XIncludeTask", Project.MSG_VERBOSE);
		File file = new File(inFile);
		if (!file.exists()) {
			throw new BuildException("Specified Inputfile does not exists "
					+ inFile);
		}
		// We will check if the file exists, if not we will try to create the
		// output file and or the output directory
		File fileout = new File(outFile);
		File dir = new File(fileout.getParent());
		if (!dir.exists()) {
			log("Creating the output directory " + dir.getAbsolutePath());

			Boolean success = (new File(dir.getAbsolutePath())).mkdirs();
			if (!success) {
				throw new BuildException("Could not create outputfile"
						+ outFile);
			}
		}
		// check if the included files are modified after the last run
		// Assumption that all the included files are part of the input file
		// directory
		if (checkmodified(inFile, outFile)) {
			log("Changes detected. Creating a new output file for " + inFile,
					Project.MSG_INFO);
			XIncluder la = new XIncluder();
			try {
				la.extractXMLFile(inFile, outFile);
			} catch (Exception e) {
				throw new BuildException("Problems with accessing the files."
						+ e.getMessage());
			}
		}
	}

	private boolean checkmodified(String inFile, String outFile) {
		File in = new File(inFile);
		File out = new File(outFile);
		File dir = new File(in.getParent());

		Collection<File> allFiles = listFiles(dir, true);
		for (File f : allFiles) {
			if (checkFileIgnored(in)) {
				if (f.lastModified() > out.lastModified()) {
					log("Modified file: " + f.getAbsoluteFile().toString());
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkFileIgnored(File f) {
		if (ignoreDotFiles == false) {
			return true;
		}
		if (!f.getName().toString().startsWith(".")) {
			return false;
		}
		return true;
	}

	public Collection<File> listFiles(File directory, boolean recurse) {
		// List of files / directories
		Vector<File> files = new Vector<File>();

		// Get files / directories in the directory
		File[] entries = directory.listFiles();

		// Go over entries
		for (File entry : entries) {

			files.add(entry);

			// If the file is a directory and the recurse flag
			// is set, recurse into the directory
			if (recurse && entry.isDirectory()) {
				files.addAll(listFiles(entry, recurse));
			}
		}

		// Return collection of files
		return files;
	}

	private void validate() {
		if (inFile == null) {
			throw new BuildException("Please specify inputfile ");
		}
		if (outFile == null) {
			throw new BuildException("Please specify outputfile ");
		}
	}
}
