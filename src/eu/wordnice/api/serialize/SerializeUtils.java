package eu.wordnice.api.serialize;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import eu.wordnice.api.IStream;
import eu.wordnice.api.OStream;

public class SerializeUtils {
	
	public static void write(DataWriter dw, File file) throws SerializeException, IOException {
		if(file.exists() == false) {
			file.createNewFile();
		}
		OStream os = new OStream(new BufferedOutputStream(new FileOutputStream(file)));
		dw.write(os);
		os.close();
	}
	
	public static void read(DataReader dr, File file) throws SerializeException, FileNotFoundException, IOException {
		IStream is = new IStream(new BufferedInputStream(new FileInputStream(file)));
		dr.read(is);
		is.close();
	}
	
}
