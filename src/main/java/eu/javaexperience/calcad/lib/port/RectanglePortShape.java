package eu.javaexperience.calcad.lib.port;

import eu.javaexperience.calcad.lib.surface.SurfaceAlignment;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import static eu.javaexperience.calcad.lib.Cal.*;

public class RectanglePortShape extends AbstractPortShape
{
	public double w;
	public double h;
	
	public RectanglePortShape(SurfaceAlignment align, double x, double y, double w, double h)
	{
		super(align, x, y);
		this.w = w;
		this.h = h;
	}

	@Override
	public double[] getShapeSize()
	{
		return new double[]{w, h};
	}

	@Override
	public CSG generateBase(double thickness)
	{
		return new Cube(w, h, thickness).toCSG().transformed(move(0, 0, 0));
	}
}