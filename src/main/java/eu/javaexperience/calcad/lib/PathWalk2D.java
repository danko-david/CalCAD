package eu.javaexperience.calcad.lib;

import java.util.Arrays;

public class PathWalk2D
{
	protected double[] points = new double[16];
	protected int ep = 0;
	
	public PathWalk2D step(double x, double y)
	{
		if(points.length <= ep+2)
		{
			points = Arrays.copyOf(points, points.length*2);
		}
		
		points[ep++] = x;
		points[ep++] = y;
		return this;
	}
	
	public double[] end()
	{
		return Arrays.copyOf(points, ep);
	}
	
	public Extruder toExtruder()
	{
		return new Extruder(Cal.polygonZ(end()));
	}
	
	public Extruder toFlippedExtruder()
	{
		return new Extruder(Cal.polygonZ(end()));
	}
}