package com.palgeymaim.client.utils;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class ScreenDimensionsUtil {
	
	private static GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	
	public static double getWidthFactor() {
		int screenWidth = gd.getDisplayMode().getWidth();
		return screenWidth/1280.0;  
	}
	
	public static double getHeightFactor() {
		int screenHeight = gd.getDisplayMode().getHeight();
	    return screenHeight/800.0; 
	}
	
	public static double getHeight() {
		return gd.getDisplayMode().getHeight();
	}
	
	public static double getWidth() {
		return gd.getDisplayMode().getWidth();
	}

}
