package de.andrano.bootdream;

import java.io.File;
import java.util.zip.ZipFile;

public class Dream {
	
	public File file;
	public String name;
	public String path;
	public Boolean intern;
	
	private String description = "desc.txt";
	
	public Dream(String name) {
		this.file = null;
		this.name = name;
		this.path = name;
		this.intern = true;
	}
	
	public Dream(File file) {
		this.file = file;
		this.name = file.getName();
		this.path = file.getAbsolutePath();
		this.intern = false;
	}
	
	public boolean isDream() {
		if (intern) {
			return true;
		}
		try {
			ZipFile zip = new ZipFile(file);
			if (!zip.getEntry(description).equals(null)) {
				zip.close();
				return true;
			}
			zip.close();
		} catch (Exception e) {}
		return false;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
}
