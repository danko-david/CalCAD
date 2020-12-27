package eu.javaexperience.calcad.lib;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import eu.javaexperience.calcad.lib.triangulate.ReusablePolygonTriangulator;
import eu.javaexperience.io.BufferedOutput;
import eu.javaexperience.text.StringTools;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Polygon;
import eu.mihosoft.vvecmath.Vector3d;

public class ModelObjectTools
{
	/* *
	 * The existance of this two Field it the mark of the over-S.O.L.I.D.-ified
	 * code. The plane class and it's normal not accessible outside from,
	 * so i have to use nasty reflect code to acquire the normal of the polygon.
	 * 
	 * protected static Field F_PLANE = Mirror.getClassFieldOrNull(Polygon.class, "plane");
	 * protected static Field F_NORMAL = Mirror.getClassFieldOrNull("eu.mihosoft.vvecmath.Plane", "normal");
	 * 
	 * I discovered that every Vertex contains the normal of the polygon.
	 * So i commented this out. That was close enough to a bad judgement.
	 * Anyway i keep this comment here.
	 * */
	
	protected static void writeFloat(OutputStream dos, float value) throws IOException
	{
		int v = Float.floatToRawIntBits(value);
		dos.write(v & 0xFF);
		dos.write((v >> 8) & 0xFF);
		dos.write((v >> 16) & 0xFF);
		dos.write((v >> 24) & 0xFF);
	}
	
	protected static void writeVector(OutputStream dos, Vector3d vec) throws IOException
	{
		writeFloat(dos, (float) vec.getX());
		writeFloat(dos, (float) vec.getY());
		writeFloat(dos, (float) vec.getZ());
	}
	
	public static class Triangle
	{
		public Vector3d normal;
		public Vector3d x;
		public Vector3d y;
		public Vector3d z;
		
		public Triangle(Vector3d normal, Vector3d x, Vector3d y, Vector3d z)
		{
			this.normal = normal;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void write(OutputStream dos) throws IOException
		{
			writeVector(dos, normal);
			writeVector(dos, x);
			writeVector(dos, y);
			writeVector(dos, z);
			dos.write(0);
			dos.write(0);
		}
	}

	public static List<Triangle> toTriangles(CSG object)
	{
		List<Polygon> polys = object.getPolygons();
		List<Triangle> tris = new ArrayList<>();
		
		for(Polygon poly:polys)
		{
			addPoly(poly, tris);
		}
		
		return tris;
	}
	
	public static void addPoly(Polygon poly, List<Triangle> tris)
	{
		for(int i=0;i<poly.vertices.size()-2;++i)
		{
			tris.add(new Triangle
			(
				poly.vertices.get(0).normal,
				poly.vertices.get(0).pos,
				poly.vertices.get(i+1).pos,
				poly.vertices.get(i+2).pos
			));
		}
	}


	/**
	 * Writes the binary 3d model into an OutputStream.
	 * 
	 * File format: https://en.wikipedia.org/wiki/STL_(file_format)
	 * 
	 * */
	public static void writeStlData(OutputStream out, CSG object) throws IOException
	{
		BufferedOutput os = new BufferedOutput(out);
		
		//write header
		{
			String header = "CalCAD exported 3d Object";
			header = header+StringTools.repeatChar(' ', 80-header.length());
			os.write(header.getBytes());
		}
		
		{
			List<Triangle> tris = ReusablePolygonTriangulator.toTriangles(object);
			
			//write size
			{
				int v = tris.size();
				os.write(v & 0xFF);
				os.write((v >> 8) & 0xFF);
				os.write((v >> 16) & 0xFF);
				os.write((v >> 24) & 0xFF);
			}
			
			//write triangles
			for(Triangle t:tris)
			{
				t.write(os);
			}
		}
		
		os.flush();
	}
}
