package eu.javaexperience.calcad.lib.port;

import static eu.javaexperience.calcad.lib.Cal.*;

import eu.javaexperience.calcad.lib.surface.SurfaceAlignment;
import eu.mihosoft.jcsg.CSG;

public abstract class AbstractPortShape implements PortShape
{
	public double x;
	public double y;
	
	public SurfaceAlignment align;
	
	public AbstractPortShape(SurfaceAlignment align, double x, double y)
	{
		this.x = x;
		this.y = y;
		this.align = align;
	}
	
	public abstract double[] getShapeSize();
	public abstract CSG generateBase(double thickness);
	
	@Override
	public CSG generate(double thickness)
	{
		double[] size = getShapeSize();
		double[] off = align.translateFrom(SurfaceAlignment.CENTERED, size[0], size[1]);
		return generateBase(thickness).transformed(move(x+off[0], y+off[1], 0));
	}
}