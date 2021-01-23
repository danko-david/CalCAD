package eu.javaexperience.calcad.lib.surface;

public enum SurfaceAlignment
{
	CENTERED(0.5, 0.5),
	
	LEFT_MIDDLE(0, 0.5),
	RIGHT_MIDDLE(1, 0.5),
	
	TOP_MIDDLE(0.5, 0),
	BOTTOM_MIDDLE(0.5, 1),
	
	LEFT_TOP(0, 0),
	LEFT_BOTTOM(0, 1),
	
	RIGHT_TOP(1, 0),
	RIGHT_BOTTOM(1, 1),
	
	;
	
	public final double sx;
	public final double sy;
	
	private SurfaceAlignment(double sx, double sy)
	{
		this.sx = sx;
		this.sy = sy;
	}
	
	public static double[] translate(SurfaceAlignment input, SurfaceAlignment output, double x, double y)
	{
		return new double[]
		{
			x * (output.sx - input.sx),
			y * (output.sy - input.sy)
		};
	}
	
	public double[] translateFrom(SurfaceAlignment input, double x, double y)
	{
		return translate(input, this, x, y);
	}
}