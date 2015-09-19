package eu.wordnice.db.serialize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import eu.wordnice.streams.Input;
import eu.wordnice.streams.Output;
import eu.wordnice.streams.InputAdv;
import eu.wordnice.streams.OutputAdv;

public class SerializeUtils {
	
	public static void write(DataWriter dw, File file) throws SerializeException, IOException {
		if(file.exists() == false) {
			file.createNewFile();
		}
		Output os = OutputAdv.forFile(file);
		dw.write(os);
		os.close();
	}
	
	public static void read(DataReader dr, File file) throws SerializeException, FileNotFoundException, IOException {
		Input is = InputAdv.forFile(file);
		dr.read(is);
		is.close();
	}
	
}
