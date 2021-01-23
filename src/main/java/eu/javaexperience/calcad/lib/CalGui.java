package eu.javaexperience.calcad.lib;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import eu.javaexperience.reflect.Mirror;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.STL;

public class CalGui
{
	public static File promptFile(String title, boolean save_open, String typeName, String extension)
	{
		return promptFile(title, save_open, null == typeName || null == extension?null:new FileNameExtensionFilter(typeName, extension));
	}
	
	public static File promptFile(String title, boolean save_open, FileNameExtensionFilter filter)
	{
		JFrame parentFrame = new JFrame();
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		
		if(null != filter)
		{
			chooser.setFileFilter(filter);
		}
		
		int userSelection = save_open?
			chooser.showSaveDialog(parentFrame)
		:
			chooser.showOpenDialog(parentFrame);
		
		if (userSelection == JFileChooser.APPROVE_OPTION)
		{
			return chooser.getSelectedFile();
		}
		
		return null;
	}
	
	public static CSG promptOpenStlFile()
	{
		try
		{
			File f = promptFile("Opening STL file", true, new FileNameExtensionFilter("STL file","stl"));
			if(null != f)
			{
				return STL.file(f.toPath());
			}
			return null;
		}
		catch(Exception e)
		{
			Mirror.propagateAnyway(e);
			return null;
		}
	}
	
	public static boolean promptSaveStlFile(CSG obj)
	{
		File f = promptFile("Specify STL file to save", true, new FileNameExtensionFilter("STL file","stl"));
		if(null != f)
		{
			Cal.writeStl(obj, f.toString());
			return true;
		}
		return false;
	}
}
