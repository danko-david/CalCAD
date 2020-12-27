package eu.javaexperience.calcad.lib.preview;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.swing.JPanel;

import eu.javaexperience.calcad.lib.Cal;
import eu.javaexperience.reflect.Mirror;
import eu.mihosoft.jcsg.Bounds;
import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.MeshContainer;
import eu.mihosoft.vvecmath.Vector3d;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;

public class CalCadInspector
{
	protected JFrame frame = new JFrame();
	
	protected JPanel topPanel = new JPanel();
	protected JPanel bottomPanel = new JPanel();

	protected JPanel leftPanel = new JPanel();
	protected JPanel rightPanel = new JPanel();
	
	protected JFXPanel fxPanel = new JFXPanel();
	protected CalCadInspectionNavigator nav;
	
	protected PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
	
	protected ParallelCamera parallelCamera = new ParallelCamera();
	
	
	public CalCadInspector()
	{
		createLayout();
		
		/*
		 * maybe add some suff:
		 * 	- controller: fixed views: isometric, XYZ plane
		 * 	- export to file toolbar
		 */
		
		viewGroup.setAutoSizeChildren(true);
		
		scene.setCamera(perspectiveCamera);
		scene.setFill(Color.LIGHTGRAY);
		
		nav = addNavigationBehavior(fxPanel, viewGroup);
		
		Platform.runLater(()->fxPanel.setScene(scene));
	}
	
	protected void createLayout()
	{
		frame.setLayout(new BorderLayout(1, 1));
		topPanel.setLayout(new FlowLayout());
		bottomPanel.setLayout(new FlowLayout());
		leftPanel.setLayout(new FlowLayout());
		rightPanel.setLayout(new FlowLayout());
		
		frame.add(topPanel, BorderLayout.PAGE_START);
		frame.add(fxPanel, BorderLayout.CENTER);
		frame.add(bottomPanel, BorderLayout.PAGE_END);
		
		frame.add(leftPanel, BorderLayout.LINE_START);
		frame.add(rightPanel, BorderLayout.LINE_END);
		
		frame.setSize(1024, 768);
	}
	
	public void setInspectorVisible(boolean visible)
	{
		frame.setVisible(visible);
	}
	
	protected Group viewGroup = new Group();
	protected Scene scene =  new Scene(viewGroup, 1024, 768, true);
	
	protected CSG csg;
	
	public JFrame getWindow()
	{
		return frame;
	}
	
	public void show(CSG csg)
	{
		show(csg, false);
	}
	
	public void show(CSG csg, boolean waitShow)
	{
		Semaphore s = waitShow?new Semaphore(0):null;
		Platform.runLater(()->
		{
			doShow(csg);
			if(null != s)
			{
				s.release(Integer.MAX_VALUE);
			}
		});
		
		if(null != s)
		{
			try
			{
				s.acquire();
			}
			catch (InterruptedException e)
			{
				Mirror.propagateAnyway(e);
			}
		}
	}
	
	public CSG getShownObject()
	{
		return csg;
	}
	
	protected MeshContainer mc;
	
	protected void doShow(CSG csg)
	{
		this.csg = csg;
		viewGroup.getChildren().clear();
		mc = csg.toJavaFXMesh();
		
		double length = 100.0;
		double thread = 0.5;
		//add axes
		viewGroup.getChildren().add(toMesh(Cal.cube(length, thread, thread), Color.RED));
		viewGroup.getChildren().add(toMesh(Cal.cube(thread, length, thread), Color.GREEN));
		viewGroup.getChildren().add(toMesh(Cal.cube(thread, thread, length), Color.BLUE));
		
		viewGroup.getChildren().add(toMesh(mc, Color.GREENYELLOW));
	}
	
	protected static MeshView toMesh(MeshContainer obj, Color c)
	{
		MeshView meshView = obj.getAsMeshViews().get(0);
		meshView.setCullFace(CullFace.BACK);
		meshView.setMaterial(new PhongMaterial(c));
		return meshView;
	}
	
	protected static MeshView toMesh(CSG obj, Color c)
	{
		return toMesh(obj.toJavaFXMesh(), c);
	}
	
	public Bounds determineObjectDimensions()
	{
		if(null == mc)
		{
			return null;
		}
		return mc.getBounds();
	}
	
	protected static double dist(double a, double b)
	{
		return Math.sqrt(a*a + b*b);
	}
	
	public void viewIsometric()
	{
		Bounds bs = determineObjectDimensions();
		if(null == bs)
		{
			return;
		}
		
		Vector3d min = bs.getMin();
		Vector3d max = bs.getMax();
		
		nav.rotateZ.setAngle(-135);
		nav.rotateX.setAngle(135);
		
		double sx = max.getX() - min.getX();
		double sy = max.getY() - min.getY();
		double sz = max.getZ() - min.getZ();
		
		double size = Math.sqrt(sx*sx + sy*sy + sz*sz);
			
		double fmin = Math.min(frame.getWidth(), frame.getHeight());
		double scale = fmin/size;
		scale *= 0.8;
		nav.setScale(scale);
		
		Vector3d c = bs.getCenter();
		
		//3D vector projection to a 2d pane
		double ox = -c.getX() + c.getY();
		double oy = -c.getX() - c.getY() + c.getZ();
		
		ox *= scale;
		oy *= scale;
		
		nav.translate.setX(frame.getWidth()/2  - ox/2);
		nav.translate.setY(frame.getHeight()/2 + oy/2);
	}
	
	public static CalCadInspector openNewWindow(String title, int width, int height, CSG object)
	{
		CalCadInspector cci = new CalCadInspector();
		if(null != title)
		{
			cci.frame.setTitle(title);
		}
		
		if(width > 0 && height > 0)
		{
			cci.frame.setSize(width, height);
		}
		
		if(null != object)
		{
			cci.show(object);
		}
		
		cci.viewIsometric();
		cci.setInspectorVisible(true);
		
		return cci;
	}
	
	public CalCadInspectionNavigator getNavigator()
	{
		return nav;
	}
	
	public JPanel getTopPanel()
	{
		return topPanel;
	}
	
	public JPanel getLeftPanel()
	{
		return leftPanel;
	}
	
	public JPanel getRightPanel()
	{
		return rightPanel;
	}
	
	public JPanel getBottomPanel()
	{
		return bottomPanel;
	}
	
	protected CalCadInspectionNavigator addNavigationBehavior(JFXPanel fxPanel, Group grp)
	{
		CalCadInspectionNavigator nav = new CalCadInspectionNavigator(grp, MouseButton.PRIMARY, MouseButton.MIDDLE);
		nav.installListeners(fxPanel);
		return nav;
	}
	
	public void setExitOnClose()
	{
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void addViewHotkeys(CalCadInspector inspector)
	{
		inspector.nav.getKeyTypeMediator().addEventListener(e->
		{
			if(!e.isAltDown() && !e.isControlDown() && !e.isShiftDown())
			{
				switch(e.getKeyChar())
				{
					case '0':
						inspector.resetView();
					break;
				
					case '5':
						inspector.viewIsometric();
					break;
					
					case 'p':
						System.out.println("View informations:");
						System.out.println("3D Screen: "+inspector.scene.getWidth()+"x"+inspector.scene.getHeight());
						System.out.println("Translate: "+inspector.nav.translate);
						System.out.println("Rotate: X: "+inspector.nav.rotateX.getAngle()+", Z: "+inspector.nav.rotateZ.getAngle());
						System.out.println("Scale: "+inspector.nav.scale);
					break;
					
					case 'c':
						inspector.switchCamera();
					break;
				}
			}
		});
	}
	
	public void switchCamera()
	{
		if(parallelCamera == scene.getCamera())
		{
			scene.setCamera(perspectiveCamera);
		}
		else
		{
			scene.setCamera(parallelCamera);
			parallelCamera.setFarClip(1000.0);
		}
	}
	
	public void setCameraParallel()
	{
		scene.setCamera(parallelCamera);
	}
	
	public void setCameraPerspective()
	{
		scene.setCamera(perspectiveCamera);
	}
	
	public void resetView()
	{
		nav.rotateZ.setAngle(0);
		nav.rotateX.setAngle(0);
		nav.setScale(1);
		nav.translate.setX(0);
		nav.translate.setY(0);
	}
}
