package eu.javaexperience.calcad.lib;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.document.DocumentTools;
import eu.javaexperience.functional.BoolFunctions;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.semantic.references.MayNull;
import eu.mihosoft.jcsg.Polygon;
import eu.mihosoft.jcsg.Vertex;
import eu.mihosoft.vvecmath.Vector3d;

public class CalSvg
{
	protected static Map<String, String> PATH_SVG_OBJECTS = new SmallMap<>();
	static
	{
		PATH_SVG_OBJECTS.put("polygon", "points");
		//PATH_SVG_OBJECTS.put("path", "d");
	}
	
	public static Document readXml(String file)
	{
		try(FileInputStream fis = new FileInputStream(file))
		{
			return DocumentTools.parseDocument(fis);
		}
		catch(Exception e)
		{
			Mirror.propagateAnyway(e);
			return null;
		}
	}
	
	public static Polygon importSvgPolygon(String file, @MayNull String id)
	{
		GetBy1<Boolean, Node> sel = null;
		if(null != id)
		{
			sel = DocumentTools.selectNodesByAttributeValue("id", id);
		}
		return importSvgWithProbes(file, sel, PATH_SVG_OBJECTS);
	}
	
	public static Polygon importSvgWithProbes(String file, GetBy1<Boolean, Node> sel, Map<String, String> node_attrs)
	{
		Document doc = readXml(file);
		
		for(Entry<String, String> kv:node_attrs.entrySet())
		{
			
			GetBy1<Boolean, Node> s = DocumentTools.selectNodesByTagName(kv.getKey());
			if(null != sel)
			{
				s = BoolFunctions.and(s, sel);
			}
			
			Node el = DocumentTools.findFirst(doc, s);
			if(null != el)
			{
				Node ps = DocumentTools.getAttr(el, kv.getValue());
				return parsePolygon(ps.getNodeValue());
			}
		}
		
		throw new RuntimeException("Polygon not found");
	}
	
	public static Polygon importSvgPath(String file, String id)
	{
		Document doc = readXml(file);
		
		GetBy1<Boolean, Node> s = DocumentTools.selectNodesByTagName("path");
		
		GetBy1<Boolean, Node> sel = null;
		if(null != id)
		{
			sel = BoolFunctions.and(sel, DocumentTools.selectNodesByAttributeValue("id", id));
		}
		
		Node el = DocumentTools.findFirst(doc, s);
		if(null != el)
		{
			Node ps = DocumentTools.getAttr(el, "d");
			
			String[] els = ps.getNodeValue().split("\\s+");
			
			double x = 0;
			double y = 0;
			
			List<Vertex> vs = new ArrayList<>();
			for(int i=0;i<els.length;++i)
			{
				boolean hv = false;
				switch(els[i])
				{
				//we ignore Bézier curves, just using the end point
				//go to the 3rd point
				case "c":
				case "C":
					++i;
					
				//go to the 2nd point
				case "s":
				case "S":
					++i;
				
					//go to the next point
				case "m":
				case "M":
					
					//intentional pass trought
				case "l":
				case "L":
					
					++i;
					
				default:
					String[] cc = els[i].split(",");
					x += Double.parseDouble(cc[0]);
					y += Double.parseDouble(cc[1]);
					vs.add(new Vertex(Vector3d.xyz(x, y, 0), Vector3d.Z_ONE));
					
					break;
				
				case "h":
				case "H":
					hv = true;
				case "v":
				case "V":
					++i;
					double d = Double.parseDouble(els[i]);
					if(hv)
					{
						x += d;
					}
					else
					{
						y += d;
					}
					
					vs.add(new Vertex(Vector3d.xyz(x, y, 0), Vector3d.Z_ONE));
					break;
					
				case "z":
				case "Z":
					//we close that path anyway
					++i;
					break;
				}
			}
			
			return new Polygon(vs);
		}
		
		throw new RuntimeException("Polygon not found");
	}
	
	public static Polygon parsePolygon(String poly)
	{
		List<Vertex> vs = new ArrayList<>();
		poly = poly.trim();
		for(String p:poly.split("\\s+"))
		{
			String[] cc = p.split(",");
			if(2 == cc.length)
			{
				vs.add(new Vertex(Vector3d.xyz(Double.parseDouble(cc[0]), Double.parseDouble(cc[1]), 0), Vector3d.Z_ONE));
			}
		}
		
		return new Polygon(vs);
	}
}
