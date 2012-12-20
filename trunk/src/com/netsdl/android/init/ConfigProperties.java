package com.netsdl.android.init;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {
	
	private static Properties props ;
	
	private static final ConfigProperties cp = new ConfigProperties() ;

	public ConfigProperties()
	{
		props = new Properties();  
        InputStream in = ConfigProperties.class.getResourceAsStream("/config.properties");  
        try {  
            props.load(in);  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
	}
	
	public static String getProperties(String properties) {

		return cp.props.getProperty(properties);

	}

}
