package eu.javaexperience.calcad.lib.parallel;

import java.util.List;
import java.util.concurrent.Semaphore;

import eu.javaexperience.reflect.Mirror;
import eu.mihosoft.jcsg.CSG;

public abstract class ParallelTask implements CsgTask, Runnable
{
	public Semaphore done = new Semaphore(0);
	public List<CSG> inputs;
	public CSG result;
	
	public ParallelTask(List<CSG> inputs)
	{
		this.inputs = inputs;
	}
	
	@Override
	public void execute()
	{
		result = doCsgOperation(inputs);
		done.release(Integer.MAX_VALUE);
	}
	
	@Override
	public void run()
	{
		execute();
	}
	
	protected abstract CSG doCsgOperation(List<CSG> inputs);
	
	public CSG getDoneResult()
	{
		try
		{
			done.acquire();
		}
		catch (InterruptedException e)
		{
			Mirror.propagateAnyway(e);
		}
		return result;
	}
}
