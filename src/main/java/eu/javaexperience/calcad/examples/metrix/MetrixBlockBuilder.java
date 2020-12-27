package eu.javaexperience.calcad.examples.metrix;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.vvecmath.Transform;

import static eu.javaexperience.calcad.lib.Cal.*;

import java.util.ArrayList;
import java.util.List;

public class MetrixBlockBuilder
{
	public int block_x = 1;
	public int block_y = 1;
	public int block_z = 1;
	public double drill_dia = 4.1;
	public boolean drill_x = true;
	public boolean drill_y = true;
	public boolean drill_z = true;
	public boolean relieve = true;
	public double relieve_dia = 2;
	
	protected CSG drill_hole(double h)
	{
		return cylinder(drill_dia/2, h);
	}
	
	protected CSG relieve_hole(double h)
	{
		return cylinder(relieve_dia/2, h);
	}
	
	protected void relieve_holes
	(
		List<CSG> subs,
		Transform rotate,
		double x0,
		double y0,
		double z0,
		double h, 
		boolean fl_x,
		boolean fl_y
	)
	{
		double relieve_offset = 2.5 + relieve_dia/4;
		
		//near side holes
		subs.add(relieve_hole(h).transformed(combine(rotate, move(x0+relieve_offset, y0+relieve_offset, z0))));
		subs.add(relieve_hole(h).transformed(combine(rotate, move(x0+relieve_offset, y0-relieve_offset, z0))));
		
		subs.add(relieve_hole(h).transformed(combine(rotate, move(x0-relieve_offset, y0+relieve_offset, z0))));
		subs.add(relieve_hole(h).transformed(combine(rotate, move(x0-relieve_offset, y0-relieve_offset, z0))));
		
		if(fl_x)
		{
			subs.add(relieve_hole(h).transformed(combine(rotate, move(x0+5, y0, z0))));
		}
		
		if(fl_y)
		{
			subs.add(relieve_hole(h).transformed(combine(rotate, move(x0, y0+5, z0))));
		}
	}
	
	protected static boolean first_last(int i, int max)
	{
		return 0 < i && i < max;
	}
	
	protected void machining
	(
		List<CSG> subs,
		Transform rotate,
		double x0,
		double y0,
		double z0,
		double drill,
		boolean fl_x,
		boolean fl_y
	)
	{
		subs.add(drill_hole(drill).transformed(combine(rotate, move(x0, y0, z0))));
		if(relieve)
		{
			relieve_holes(subs, rotate, x0, y0, z0, drill, fl_x, fl_y);
		}
	}
	
	public CSG generateBlock()
	{
		boolean debug_drills = false;
		
		CSG base = cube(block_x*10, block_y*10, block_z*10);
		
		if(drill_x)
		{
			List<CSG> subs = new ArrayList<>();
			Transform rotx = rotate(-90, -90, 0);
			for(int y = 1;y<= block_y;++y)
			{
				for(int z = 1;z<= block_z;++z)
				{
					machining
					(
						subs,
						rotx,
						(double) -5+y*10,
						(double) -5+z*10,
						(double) -0.5,
						1+block_x*10,
						first_last(y, block_y),
						first_last(z, block_z)
					);
				}
			}
			
			base = debug_drills?
					union(base, union(subs))
				:
					base.difference(union(subs));
		}
		
		if(drill_y)
		{
			List<CSG> subs = new ArrayList<>();
			Transform roty = rotate(-90, 0, 0);
			for(int x=1;x <= block_x;++x)
			{
				for(int z=1;z <= block_z;++z)
				{
					machining
					(
						subs,
						roty,
						-5+x*10,
						-5+z*10,
						-0.5-block_y*10,
						1+block_y*10,
						first_last(x, block_x),
						first_last(z, block_z)
					);
				}
			}
			base = debug_drills?
					union(base, union(subs))
				:
					base.difference(union(subs));

		}
		
		if(drill_z)
		{
			List<CSG> subs = new ArrayList<>();
			Transform rotz = rotate(0, 0, 0);
			for(int x=1;x <= block_x;++x)
			{
				for(int y=1;y <= block_y;++y)
				{
					machining
					(
						subs,
						rotz,
						-5+x*10,
						-5+y*10,
						-0.5, 1+block_z*10,
						first_last(x, block_x),
						first_last(y, block_y)
					);
				}
			}
			base = debug_drills?
					union(base, union(subs))
				:
					base.difference(union(subs));
		}
		
		return base;
	}
	
}
