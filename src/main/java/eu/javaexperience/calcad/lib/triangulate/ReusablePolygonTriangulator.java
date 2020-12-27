package eu.javaexperience.calcad.lib.triangulate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.javaexperience.calcad.lib.ModelObjectTools.Triangle;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Polygon;
import eu.mihosoft.jcsg.Vertex;
import eu.mihosoft.vvecmath.ModifiableVector3d;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;

public class ReusablePolygonTriangulator
{
	protected List<Polygon> dst = new ArrayList<>();
	
	protected List<Integer> triangles = new ArrayList<>();
	protected float[] d2points = new float[16];
	protected int d2ep = 0;
	
	public static double angleX(Vector3d v)
	{
		return (180/Math.PI)*Math.acos(v.y()/v.magnitude());
	}
	
	public static double angleY(Vector3d v)
	{
		return (180/Math.PI)*Math.acos(v.x()/v.magnitude());
	}
	
	public List<Polygon> triangulate(Vector3d normal, Polygon poly)
	{
		dst.clear();
		if(poly.vertices.size() < 4)
		{
			dst.add(poly);
			return dst;
		}
		
		d2ep = 0;
		triangles.clear();
		
		/*
		 * The idea is to rotate plane to X 90 an Y 90 angle, so
		 * Z coordinates become identical, therefore i can be dropped
		 * and polygon triangulation can be done like do on a 2D shape
		 */
		
		//triangulate
		Transform rot = new Transform().rot(normal, Vector3d.Z_ONE);
		
		for(Vertex v:poly.vertices)
		{
			ModifiableVector3d pos = v.pos.asModifiable();
			pos = rot.transform(pos);
			
			if(d2points.length +2 < d2ep)
			{
				d2points = Arrays.copyOf(d2points, d2points.length*2);
			}
			
			d2points[d2ep++] = (float) pos.x();
			d2points[d2ep++] = (float) pos.y();
		}
		
		EarCut.earcut(triangles, d2points, d2ep, null, 2);
		
		for(int i=0;i<triangles.size();i+=3)
		{
			dst.add(new Polygon
			(
				poly.vertices.get(triangles.get(i)),
				poly.vertices.get(triangles.get(i+1)),
				poly.vertices.get(triangles.get(i+2))
			));
		}
		
		return dst;
	}

	public void reuse()
	{
	}
	
	public static Polygon createPolygon
	(
		double normalX, double normalY, double normalZ, 
		double... coords3d
	)
	{
		Vector3d normal = Vector3d.xyz(normalX, normalY, normalZ);
		
		ArrayList<Vertex> vs = new ArrayList<>();
		
		for(int i=0;i<coords3d.length;i+=3)
		{
			vs.add(new Vertex(Vector3d.xyz(coords3d[i], coords3d[i+1], coords3d[i+2]), normal));
		}
		return new Polygon(vs);
	}
	
	public static void addPoly(Polygon poly, List<Triangle> tris, ReusablePolygonTriangulator tri)
	{
		Vector3d normal = poly.vertices.get(0).normal;
		
		tri.reuse();
		
		for(Polygon t:tri.triangulate(normal, poly))
		{
			if(3 != t.vertices.size())
			{
				System.out.println("invalid");
			}
			
			tris.add
			(
				new Triangle
				(
					normal,
					t.vertices.get(0).pos,
					t.vertices.get(1).pos,
					t.vertices.get(2).pos
				)
			);
		}
	}

	public static List<Triangle> toTriangles(CSG object)
	{
		List<Polygon> polys = object.getPolygons();
		List<Triangle> tris = new ArrayList<>();
		
		ReusablePolygonTriangulator tri = new ReusablePolygonTriangulator();
		for(Polygon poly:polys)
		{
			addPoly(poly, tris, tri);
		}
		
		return tris;
	}
}
