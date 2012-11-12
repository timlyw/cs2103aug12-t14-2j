//@author A0088669A

package mhs.src.common;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * USed for test File Read/Write
 * 
 * @author Shekhar
 * 
 */
public class FileHandler {

	private final String FILE;

	/**
	 * Creates and initilates a new file
	 * 
	 * @param fileName
	 */
	public FileHandler(String fileName) {
		File file = new File(fileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
		}
		FILE = fileName;
	}

	/**
	 * Appends given string to file
	 * 
	 * @param writeString
	 */
	public void writeToFile(String writeString) {
		BufferedWriter output;
		try {
			output = openFileAppend(FILE);
			output.write(writeString);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Clears the file
	 */
	public void clearFile() {
		PrintWriter writer;
		try {
			writer = new PrintWriter(FILE);
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Opens file in append mode
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private static BufferedWriter openFileAppend(String fileName)
			throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
		return out;
	}

}
