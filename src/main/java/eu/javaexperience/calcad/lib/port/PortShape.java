package eu.javaexperience.calcad.lib.port;

import eu.mihosoft.jcsg.CSG;

public interface PortShape
{
	public CSG generate(double thickness);
}
