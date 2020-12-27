package eu.javaexperience.calcad.lib.preview;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;

import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;
import eu.javaexperience.patterns.behavioral.mediator.EventMediator;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class CalCadInspectionNavigator implements EventHandler<MouseEvent>, KeyListener, MouseWheelListener, MouseListener, MouseMotionListener
{
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("CalCadInspectionNavigator"));
	
	protected double anchorX;
	protected double anchorY;
	protected Rotate rotateX = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
	protected Rotate rotateZ = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
	protected Translate translate = new Translate();
	protected Scale scale = new Scale();
	
	public MouseButton move;
	public MouseButton rotate;
	public double rotateSpeed = 0.7;
	public double moveSpeed = 0.7;
	public double zoomSpeed = 0.1;
	
	protected Node controlledNode;
	
	protected EventMediator<KeyEvent> keyTyped = new EventMediator<>();
	
	public CalCadInspectionNavigator(Node n, MouseButton move, MouseButton rotate)
	{
		controlledNode = n;
		controlledNode.getTransforms().addAll(translate, scale, rotateX, rotateZ);
		this.move = move;
		this.rotate = rotate;
	}
	
	protected void accumulateAnchor(double x, double y)
	{
		anchorX = x;
		anchorY = y;
	}
	
	protected void applyRotateDiff(double x, double y)
	{
		rotateZ.setAngle(rotateZ.getAngle() - (anchorX - x) * rotateSpeed);
		rotateX.setAngle(rotateX.getAngle() - (anchorY - y) * rotateSpeed);
	}
	
	protected void applyMoveDiff(double x, double y)
	{
		translate.setX(translate.getX() - (anchorX - x) * moveSpeed);
		translate.setY(translate.getY() - (anchorY - y) * moveSpeed);
	}
	
	protected void applyZoomDiff(double diff)
	{
		setScale(this.scale.getX()-diff);
	}
	
	@Override
	public void handle(MouseEvent t)
	{
		if(move.equals(t.getButton()))
		{
			boolean accAnchor = MouseEvent.MOUSE_PRESSED.equals(t.getEventType());
			if(MouseEvent.MOUSE_DRAGGED.equals(t.getEventType()))
			{
				applyMoveDiff(t.getSceneX(), t.getSceneY());
				accAnchor = true;
			}
			
			if(accAnchor)
			{
				accumulateAnchor(t.getSceneX(), t.getSceneY());
				t.consume();
			}
		}
		else if(rotate.equals(t.getButton()))
		{
			boolean accAnchor = MouseEvent.MOUSE_PRESSED.equals(t.getEventType());
			
			if(MouseEvent.MOUSE_DRAGGED.equals(t.getEventType()))
			{
				applyRotateDiff(t.getSceneX(), t.getSceneY());
				accAnchor = true;
			}
			
			if(accAnchor)
			{
				accumulateAnchor(t.getSceneX(), t.getSceneY());
				t.consume();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0)
	{
		LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "keyTyped: %s", arg0);
		keyTyped.dispatchEvent(arg0);
	}
	
	public void setScale(double scale)
	{
		if(scale > 0)
		{
			this.scale.setX(scale);
			this.scale.setY(scale);
			this.scale.setZ(scale);
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0)
	{
		applyZoomDiff(arg0.getPreciseWheelRotation()*zoomSpeed);
	}

	@Override
	public void mouseClicked(java.awt.event.MouseEvent arg0) {}

	@Override
	public void mouseEntered(java.awt.event.MouseEvent arg0) {}

	@Override
	public void mouseExited(java.awt.event.MouseEvent arg0) {}
	
	protected int button = -1;
	
	@Override
	public void mousePressed(java.awt.event.MouseEvent arg0)
	{
		button = arg0.getButton();
		LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "mouse pressed: %s", button);
		accumulateAnchor(arg0.getX(), arg0.getY());
	}

	@Override
	public void mouseReleased(java.awt.event.MouseEvent arg0)
	{
		LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "mouse released: %s", button);
		button = -1;
	}

	@Override
	public void mouseDragged(java.awt.event.MouseEvent arg0)
	{
		if(move.ordinal() == button)
		{
			applyMoveDiff(arg0.getX(), arg0.getY());
		}
		else if(rotate.ordinal() == button)
		{
			applyRotateDiff(arg0.getX(), arg0.getY());
		}
		
		accumulateAnchor(arg0.getX(), arg0.getY());
	}

	@Override
	public void mouseMoved(java.awt.event.MouseEvent arg0) {}

	public void installListeners(JComponent comp)
	{
		comp.addKeyListener(this);
		comp.addMouseWheelListener(this);
		comp.addMouseListener(this);
		comp.addMouseMotionListener(this);
	}
	
	public Node getControllerNode()
	{
		return controlledNode;
	}
	
	public EventMediator<KeyEvent> getKeyTypeMediator()
	{
		return keyTyped;
	}

}

