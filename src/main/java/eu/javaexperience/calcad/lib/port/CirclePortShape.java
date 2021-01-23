package eu.javaexperience.calcad.lib.port;

import static eu.javaexperience.calcad.lib.Cal.*;

import eu.javaexperience.calcad.lib.surface.SurfaceAlignment;
import eu.mihosoft.jcsg.CSG;

public class CirclePortShape extends AbstractPortShape
{
	public double r;
	
	public CirclePortShape(SurfaceAlignment align, double x, double y, double r)
	{
		super(align, x, y);
		this.r = r;
	}
	
	@Override
	public CSG generate(double thickness)
	{
		double[] off = align.translateFrom(SurfaceAlignment.CENTERED, 2*r, 2*r);
		return cylinder(r, thickness).transformed(move(x+off[0], y+off[1], -thickness/2));
	}

	@Override
	public double[] getShapeSize()
	{
		return new double[]{r*2, r*2};
	}

	@Override
	public CSG generateBase(double thickness)
	{
		return cylinder(r, thickness);
	}
}

