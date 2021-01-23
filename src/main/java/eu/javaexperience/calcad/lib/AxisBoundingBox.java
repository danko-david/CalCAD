package eu.javaexperience.calcad.lib;

public class AxisBoundingBox
{
	public final double minX;
	public final double minY;
	public final double minZ;
	
	public final double maxX;
	public final double maxY;
	public final double maxZ;
	
	public AxisBoundingBox
	(
		double minX,
		double minY,
		double minZ,
		double maxX,
		double maxY,
		double maxZ
	)
	{
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	public double getSizeX()
	{
		return maxX - minX;
	}
	
	public double getSizeY()
	{
		return maxY - minY;
	}
	
	public double getSizeZ()
	{
		return maxZ - minZ;
	}
}
