package eu.javaexperience.calcad.examples.metrix;

import static eu.javaexperience.calcad.lib.Cal.*;

import eu.javaexperience.calcad.lib.Cal;
import eu.javaexperience.calcad.lib.preview.HcrPrototyper;
import eu.javaexperience.interfaces.simple.SimpleGet;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.mihosoft.jcsg.CSG;

public class CalCadMetrixExample
{
	public static void main(String[] args) throws Throwable
	{
		JavaExperienceLoggingFacility.addStdOut();
		
		SimpleGet<CSG> gen = ()->
		{
			Cal.FACETS = 16;
			MetrixBlockBuilder b = new MetrixBlockBuilder();
			b.block_x = 5;
			b.block_y = 1;
			b.block_z = 1;
			
			return b.generateBlock();
		};
		
		HcrPrototyper proto = new HcrPrototyper(gen);
		proto.showWindow();
		proto.getInspector().setExitOnClose();
	}
}
