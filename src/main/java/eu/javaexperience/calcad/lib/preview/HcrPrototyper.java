package eu.javaexperience.calcad.lib.preview;

import javax.swing.JButton;

import eu.javaexperience.calcad.lib.CalGui;
import eu.javaexperience.interfaces.simple.SimpleGet;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;
import eu.mihosoft.jcsg.CSG;

/**
 * Hot Code Replace Prototyper.
 * 	Eclipse in debugging mode with java hotspot has the ability to replace
 * 	bytecode in a running java instance.
 * 
 * 	Using this feature we can modify and replace bytecode on the fly and by
 * 	hitting the regenerate button we can see what changed by the code.
 * */
public class HcrPrototyper
{
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("HcrPrototyper"));
	protected CalCadInspector inspector = new CalCadInspector();
	
	protected SimpleGet<CSG> generator;
	
	protected JButton btnRefresh = new JButton("Refresh");
	protected JButton btnSave = new JButton("Save STL");
	
	public HcrPrototyper(SimpleGet<CSG> generator)
	{
		this.generator = generator;
		inspector.frame.setTitle("CalCAD - HCR Prototyper inspector");
		
		inspector.getBottomPanel().add(btnRefresh);
		inspector.getBottomPanel().add(btnSave);
		
		btnRefresh.addActionListener(e->this.regenerateAndInspect());
		btnSave.addActionListener(e->this.promptSaveCurrentModel());
		
		CalCadInspector.addViewHotkeys(inspector);
	}
	
	protected void promptSaveCurrentModel()
	{
		CalGui.promptSaveStlFile(inspector.getShownObject());
	}

	public void regenerateAndInspect()
	{
		long t0 = System.currentTimeMillis();
		LoggingTools.tryLogFormat(LOG, LogLevel.MEASURE, "Calling regenerateAndInspect");
		inspector.show(generator.get());
		LoggingTools.tryLogFormat(LOG, LogLevel.MEASURE, "Calling regenerateAndInspect took: %s ms", System.currentTimeMillis()-t0);
	}
	
	public void showWindow()
	{
		inspector.setInspectorVisible(true);
		btnRefresh.doClick();
		inspector.viewIsometric();
	}

	public CalCadInspector getInspector()
	{
		return inspector;
	}
}
