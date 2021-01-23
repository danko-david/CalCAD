package eu.javaexperience.calcad.lib.port;

import eu.javaexperience.calcad.lib.surface.SurfaceAlignment;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.RoundedCube;

public class RoundedRectanglePortShape extends AbstractPortShape
{
	public double w;
	public double h;
	
	public double r;
	
	public RoundedRectanglePortShape(SurfaceAlignment align, double x, double y, double w, double h, double r)
	{
		super(align, x, y);
		this.w = w;
		this.h = h;
		this.r = r;
	}

	@Override
	public double[] getShapeSize()
	{
		return new double[]{w, h};
	}

	@Override
	public CSG generateBase(double thickness)
	{
		return CSG.fromPolygons(new RoundedCube(w, h, thickness).cornerRadius(r).toPolygons());
	}
}
