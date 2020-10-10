package com.palgeymaim.client.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {
	
	private static Properties props;
	
	public static Properties getProperties() throws FileNotFoundException, IOException {
		
		if(props == null) {
			 props = new Properties();
			
			try(FileInputStream fis = new FileInputStream("config.properties")){
				props.load(fis);
				return props;
			}
		} 
		
		return props;
		
	}

}
