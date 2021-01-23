package eu.javaexperience.calcad.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

import eu.javaexperience.asserts.AssertArgument;
import eu.javaexperience.calcad.lib.parallel.ParallelTask;
import eu.javaexperience.calcad.lib.preview.CalCadInspector;
import eu.javaexperience.collection.CollectionTools;
import eu.javaexperience.collection.list.NullList;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;
import eu.javaexperience.multithread.TaskExecutorPool;
import eu.javaexperience.reflect.Mirror;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.Cylinder;
import eu.mihosoft.jcsg.Polygon;
import eu.mihosoft.jcsg.STL;
import eu.mihosoft.jcsg.Sphere;
import eu.mihosoft.jcsg.Vertex;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;

public class Cal
{
/* ***************** Logging *************/
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("Cal"));
	
/* ***************** Static default, user overwriteable variables *************/
	public static int FACETS = 32;
	
	public static final CSG CSG_EMPTY = CSG.fromPolygons(new NullList<>());

//Multithreading stuffs
	protected static TaskExecutorPool THREAD_POOL = new TaskExecutorPool()
	{
		public synchronized void execute(Runnable exec)
		{
			int th = this.getThreadsCount();
			super.execute(exec);
			int now = this.getThreadsCount();
			if(th != now)
			{
				LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "Running threads: %s", now);
			}
		};
	};

	protected static int PARALLELISM = Runtime.getRuntime().availableProcessors()-1;
	protected static Semaphore PARALLEL_CALC_SEMAPHORE = new Semaphore(PARALLELISM);
	
	public static void setParallelism(int parallel)
	{
		AssertArgument.assertGreaterOrEqualsThan(parallel, 1, "parallelism");
		
		int diff = PARALLELISM - parallel;
		
		if(diff > 0)
		{
			new Thread()
			{
				public void run()
				{
					try
					{
						PARALLEL_CALC_SEMAPHORE.acquire(diff);
					}
					catch (InterruptedException e)
					{
						Mirror.propagateAnyway(e);
					}
				}
			}.start();
		}
		else
		{
			PARALLEL_CALC_SEMAPHORE.release(-diff);
		}
		PARALLELISM = parallel;
	}
	
	public static int getParallelism()
	{
		return PARALLELISM;
	}
	
//Progress notification suffs
	
	
/* ************************** Mathematical primitives *************************/
	
	/**
	 * Adds the vertex to the specified destination collection if it isn't present yet.
	 * */
	public static boolean addVertexUnique(Collection<Vertex> dst, Vertex v)
	{
		for(Vertex dv:dst)
		{
			if(equals(dv.pos, v.pos))
			{
				return false;
			}
		}
		dst.add(v);
		return true;
	}
	
	public static boolean contains(List<Vertex> vxs, Vertex vx)
	{
		for(Vertex v:vxs)
		{
			if(Cal.equals(vx.pos, v.pos))
			{
				return true;
			}
		}
		
		return false;
	}
	
/* ********************** Object generator functions *************************/

//Cube 
	public static CSG cube(double x, double y, double z)
	{
		return CSG.fromPolygons(new Cube(x, y, z).noCenter().toPolygons());
	}
	
	public static CSG cube(double size)
	{
		return CSG.fromPolygons(new Cube(size).noCenter().toPolygons());
	}
	
// Sphere
	public static CSG sphere(double size, int faces)
	{
		return CSG.fromPolygons(new Sphere(size, faces, faces).toPolygons());
	}
	
	public static CSG sphere(double size)
	{
		return sphere(size, FACETS);
	}
	
// Cylinder
	public static CSG cylinder(double radius, double height, int faces)
	{
		return CSG.fromPolygons(new Cylinder(radius, height, faces).toPolygons());
	}
	
	public static CSG cylinder(double radius, double height)
	{
		return cylinder(radius, height, FACETS);
	}

/* ********************** Complex object creation functions *******************/
	
	//TODO text
	
	
	public static Polygon polygonZ(double... xy)
	{
		ArrayList<Vertex> vs = new ArrayList<>();
		for(int i=0;i<xy.length;i+=2)
		{
			vs.add(new Vertex(Vector3d.xyz(xy[i], xy[i+1], 0), Vector3d.Z_ONE));
		}
		
		return new Polygon(vs);
	}
	
/* ***************************** Transformations ******************************/

// Move
	public static Transform move(double x, double y, double z)
	{
		return new Transform().translate(x, y, z);
	}
	
//Rotate
	public static Transform rotate(double degX, double degY, double degZ)
	{
		return new Transform().rot(degX, degY, degZ);
	}
	
//Scale
	public static Transform scale(double scale)
	{
		return new Transform().scale(scale);
	}
	
	public static Transform scale(double sX, double sY, double sZ)
	{
		return new Transform().scale(sX, sY, sZ);
	}

//Combine
	public static Transform combine(Transform... transforms)
	{
		Transform t = new Transform();
		for(Transform tr:transforms)
		{
			if(null != tr)
			{
				t.apply(tr);
			}
		}
		return t;
	}
	
	public static CSG transform(CSG csg, Transform... transforms)
	{
		return csg.transformed(combine(transforms));
	}
	
	/*
	Matrices are hidden in Transform class. 
	S_O_LID sucks.  
	public static Transform invert(Transform t)
	{

	}*/
	
/* ******************************** Operations ********************************/
//helper functions
	protected static CSG tryGetFirstOrMakeEmpty(CSG[] arr)
	{
		if(null == arr || 0 == arr.length || null == arr[0])
		{
			return CSG.fromPolygons(NullList.instance);
		}
		return arr[0];
	}

	protected static CSG tryGetFirstOrMakeEmpty(List<CSG> arr)
	{
		if(null == arr || 0 == arr.size() || null == arr.get(0))
		{
			return CSG.fromPolygons(NullList.instance);
		}
		return arr.get(0);
	}

	protected static <T> List<T>[] twoHalf(List<T> list)
	{
		List[] ret = new List[]
		{
			new ArrayList<>(),
			new ArrayList<>()
		};
		
		for(int i=0;i<list.size()/2;++i)
		{
			ret[0].add(list.get(i));
		}
		
		for(int i=list.size()/2;i<list.size();++i)
		{
			ret[1].add(list.get(i));
		}
		
		return ret;
	}
	
	protected static List<CSG> from(List<CSG> source, int from)
	{
		List<CSG> ret = new ArrayList<>();
		for(int i=from;i<source.size();++i)
		{
			ret.add(source.get(i));
		}
		
		return ret;
	}
	
	protected static ParallelTask postCalculateUnion(List<CSG> obj)
	{
		ParallelTask ret = new ParallelTask(obj)
		{
			@Override
			protected CSG doCsgOperation(List<CSG> inputs)
			{
				return runUnionParallel(obj);
			}
		};
		THREAD_POOL.execute(ret);
		return ret;
	}
	
	protected static CSG runUnionParallel(List<CSG> objs)
	{
		switch(objs.size())
		{
		case 0:	return CSG.fromPolygons(NullList.instance);
		case 1: return objs.get(0);
		case 2:
		{
			try
			{
				PARALLEL_CALC_SEMAPHORE.acquire();
				return objs.get(0).union(objs.get(1));
			}
			catch(Exception e)
			{
				Mirror.propagateAnyway(e);
			}
			finally
			{
				PARALLEL_CALC_SEMAPHORE.release();
			}
			return null;
		}
		default: break;
		}
		
		List<CSG>[] hs = twoHalf(objs);
		ParallelTask a = postCalculateUnion(hs[0]);
		ParallelTask b = postCalculateUnion(hs[1]);
		
		return runUnionParallel(CollectionTools.inlineArrayList(a.getDoneResult(), b.getDoneResult()));
	}

//union
	public static CSG union(CSG... objects)
	{
		return union(CollectionTools.inlineArrayList(objects));
	}
	
	public static CSG union(List<CSG> objects)
	{
		long t0 = System.currentTimeMillis();
		CSG ret = runUnionParallel(objects);
		LoggingTools.tryLogFormat(LOG, LogLevel.MEASURE, "union of objects %s took %s ms", objects, System.currentTimeMillis()-t0);
		return ret;
	}

//difference
	public static CSG difference(CSG... objects)
	{
		CSG ret = tryGetFirstOrMakeEmpty(objects);
		ret = ret.difference(union(Arrays.copyOfRange(objects, 1, objects.length)));
		return ret;
	}
	
	public static CSG difference(List<CSG> objects)
	{
		CSG ret = tryGetFirstOrMakeEmpty(objects);
		ret = ret.difference(from(objects, 1));
		return ret;
	}

//intersection
	public static CSG intersection(CSG... objects)
	{
		CSG ret = tryGetFirstOrMakeEmpty(objects);
		/* i think the internal implementation of intersects is wrong,
		 * because in takes the union of the arguments.
		 * This way i doesn't make the intersection of all supplied objects
		 * But the intersection of the first and the union of all remaining
		 * object.
		 */
		
		ret = ret.intersect(Arrays.copyOfRange(objects, 1, objects.length));
		return ret;
	}
	
	public static CSG intersection(List<CSG> objects)
	{
		CSG ret = tryGetFirstOrMakeEmpty(objects);
		ret = ret.intersect(from(objects, 1));
		return ret;
	}

//hull
	public static CSG hull(CSG... objects)
	{
		CSG ret = tryGetFirstOrMakeEmpty(objects);
		ret = ret.hull(Arrays.copyOfRange(objects, 1, objects.length));
		return ret;
	}
	
	public static CSG hull(List<CSG> objects)
	{
		CSG ret = tryGetFirstOrMakeEmpty(objects);
		ret = ret.hull(from(objects, 1));
		return ret;
	}
	
/* ****************************** Measurements ********************************/
	
	public static AxisBoundingBox getBoundBox(CSG obj)
	{
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double minZ = Double.MAX_VALUE;
		
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		double maxZ = -Double.MAX_VALUE;
		
		for(Polygon p:obj.getPolygons())
		{
			for(Vertex v:p.vertices)
			{
				if(minX > v.pos.x())
				{
					minX = v.pos.x();
				}
				
				if(minY > v.pos.y())
				{
					minY = v.pos.y();
				}
				
				if(minZ > v.pos.z())
				{
					minZ = v.pos.z();
				}
				
				if(maxX < v.pos.x())
				{
					maxX = v.pos.x();
				}
				
				if(maxY < v.pos.y())
				{
					maxY = v.pos.y();
				}
				
				if(maxZ < v.pos.z())
				{
					maxZ = v.pos.z();
				}
			}
		}
		
		return new AxisBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
/* *************************** Utility functions ******************************/
/*	public static CSG optimize(CSG object)
	{
		//TODO merge polygons
		return object;
	}
*/
	
/* **************************** equals functions ******************************/
	
	public static boolean equals(Vector3d a, Vector3d b)
	{
		return a.x() == b.x() && a.y() == b.y() && a.z() == b.z();
	}
	
/* **************************** Import functions ******************************/
	
	
/* ****************************** View functions ******************************/
	public static void showInNewInspectionWindow(CSG obj)
	{
		CalCadInspector.openNewWindow("CalCAD - Object inspector", 1024, 768, obj);
	}
	
/* *********************** Object save and load methods ***********************/
	
	public static CSG loadStl(String file)
	{
		try
		{
			return STL.file(new File(file).toPath());
		}
		catch (IOException e)
		{
			Mirror.propagateAnyway(e);
			return null;
		}
	}
	
	public static void writeStl(CSG obj, String file)
	{
		try(FileOutputStream fos = new FileOutputStream(file))
		{
			ModelObjectTools.writeStlData(fos, obj);
			fos.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Mirror.propagateAnyway(e);
		}
	}
}
