package eu.javaexperience.calcad.lib;

import java.util.ArrayList;

import java.util.List;

import eu.javaexperience.collection.list.NullList;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Polygon;
import eu.mihosoft.jcsg.Vertex;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;

public class Extruder
{
	protected List<Polygon> segments = new ArrayList<>();
	
	protected Polygon current;
	
	public Extruder(Polygon p)
	{
		this.current = p.flip();
		segments.add(current);
	}
	
	protected static Polygon createValidPolygon(int minPoints, Vertex... vx)
	{
		return new Polygon(vx);
		/*List<Vertex> vxs = new ArrayList<>();
		
		for(Vertex v:vx)
		{
			Cal.addVertexUnique(vxs, v);
		}
		
		if(vxs.size() >= minPoints)
		{
			return new Polygon(vxs);
		}
		
		return null;*/
	}
	
	protected static boolean addValidPolygon(List<Polygon> segments, int minPoints, Vertex... vxs)
	{
		Polygon ps = createValidPolygon(minPoints, vxs);
		if(null != ps)
		{
			segments.add(ps);
		}
		
		return null != ps;
	}
	
	public void step(Transform transform)
	{
		//Notice: normal vector doesn't count anywhere...
		//walk direction that really matters.
		Polygon next = current.clone().transform(transform);
		
		List<Vertex> cv = current.vertices;
		List<Vertex> nv = next.vertices;
		
		for(int i=0;i < cv.size()-1;i++)
		{
			addValidPolygon
			(
				segments,
				4,
				new Vertex(nv.get(i).pos, Vector3d.Z_ONE),
				new Vertex(nv.get(i+1).pos, Vector3d.Z_ONE),
				new Vertex(cv.get(i+1).pos, Vector3d.Z_ONE),
				new Vertex(cv.get(i).pos, Vector3d.Z_ONE)
			);
		}
		
		addValidPolygon
		(
			segments,
			4,
			new Vertex(nv.get(cv.size()-1).pos, Vector3d.Z_ONE),
			new Vertex(nv.get(0).pos, Vector3d.Z_ONE),
			new Vertex(cv.get(0).pos, Vector3d.Z_ONE),
			new Vertex(cv.get(cv.size()-1).pos, Vector3d.Z_ONE)
		);
		
		current = next;
	}
	
	public CSG end()
	{
		if(segments.isEmpty())
		{
			return CSG.fromPolygons(NullList.instance);
		}
		
		//flip and merge
		segments.add(current.flip());
		return CSG.fromPolygons(segments);
	}

	public static Extruder toExtrudeDirection(Polygon shape, Vector3d dir)
	{
		if(shape.vertices.get(0).normal.dot(dir) < 0 )
		{
			return new Extruder(shape.flip());
		}
		return new Extruder(shape);
	}
	
}
