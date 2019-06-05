/**<ul>
 * <li>SensorAccelerationTuto</li>
 * <li>com.android2ee.android.tuto.sensor.accelation</li>
 * <li>17 nov. 2011</li>
 * 
 * <li>======================================================</li>
 *
 * <li>Projet : Mathias Seguy Project</li>
 * <li>Produit par MSE.</li>
 *
 /**
 * <ul>
 * Android Tutorial, An <strong>Android2EE</strong>'s project.</br> 
 * Produced by <strong>Dr. Mathias SEGUY</strong>.</br>
 * Delivered by <strong>http://android2ee.com/</strong></br>
 *  Belongs to <strong>Mathias Seguy</strong></br>
 ****************************************************************************************************************</br>
 * This code is free for any usage but can't be distribute.</br>
 * The distribution is reserved to the site <strong>http://android2ee.com</strong>.</br>
 * The intelectual property belongs to <strong>Mathias Seguy</strong>.</br>
 * <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * 
 * *****************************************************************************************************************</br>
 *  Ce code est libre de toute utilisation mais n'est pas distribuable.</br>
 *  Sa distribution est reservée au site <strong>http://android2ee.com</strong>.</br> 
 *  Sa propriété intellectuelle appartient a <strong>Mathias Seguy</strong>.</br>
 *  <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * *****************************************************************************************************************</br>
 */
package com.android2ee.android.tuto.sensor.accelation;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.view.View;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to draw a vector (x,y,z) that represents the sensor value. This vector is
 *        represents by a point (x,y) and the change of the background colors. It also draws:
 *        <ul>
 *        <li>A point (Accelerate Point In Void) that is the point that moves according to the
 *        strength of the vector considered as a accelerator vector</li>
 *        <li>A fake point (Fake Point) that is the point that moves according to the
 *        strength of the vector considered as a speed vector</li>
 *        <li>The X and Y axis</li>
 *        <li>The Min and Max rectangle of the sensor reached values</li>
 *        </ul>
 *        This class use a Thread to redraw the screen (30 redrawing by second).
 *        The use of an handler for the thread and the gui thread communication is used too.
 */
public class XYAceleromterView extends View {
	/******************************************************************************************/
	/** Private constant **************************************************************************/
	/******************************************************************************************/
	/**
	 * The tag for the log
	 */
	private static final String tag = "SensorsAccelerometer";
	/******************************************************************************************/
	/** Attributes associated to the main activity and the screen *****************************/
	/******************************************************************************************/
	/**
	 * Main activity
	 */
	private SensorAccelerationTutoActivity activity;
	/**
	 * The canvas width
	 */
	private int width;
	/**
	 * The canvas height
	 */
	private int height;
	/******************************************************************************************/
	/** Attributes associated to canvas **************************************************************************/
	/******************************************************************************************/
	/**
	 * The paint to draw the view
	 */
	private Paint paint = new Paint();	
	/**
	 * The Canvas to draw within
	 */
	private Canvas canvas;
	/******************************************************************************************/
	/** Attributes used to manage the points coordinates **************************************/
	/******************************************************************************************/
	/**
	 * The attributes to manage points' trajectory
	 * (x,y) is the value of the coordinate of the AcceleratePoint
	 * (vx,vy) is its speed
	 * t and dt are the time and the delta of time between two calculus
	 */
	private float x = 0, y = 0, vx, vy;
	private long t = 0, dt;
	/**
	 * (xLin,yLin) the value are use for the coordinate of the FakePoint
	 */
	private float xLin = 0, yLin = 0;
	
	/******************************************************************************************/
	/** Handler and Thread attribute **********************************************************/
	/******************************************************************************************/
	/**
	 * The boolean to initialize the data upon
	 */
	boolean init = false;
	/** * An atomic boolean to manage the external thread's destruction */
	AtomicBoolean isRunning = new AtomicBoolean(false);
	/** * An atomic boolean to manage the external thread's destruction */
	AtomicBoolean isPausing = new AtomicBoolean(false);	
	/**
	 * The handler used to slow down the re-drawing of the view, else the device's battery is
	 * consumed
	 */
	private final Handler slowDownDrawingHandler;
	/**
	 * The thread that call the redraw
	 */
	private Thread background;

	/******************************************************************************************/
	/** Constructors **************************************************************************/
	/******************************************************************************************/

	/**
	 * @param context
	 */
	public XYAceleromterView(Context context) {
		super(context);
		// instanciate the calling activity
		activity = (SensorAccelerationTutoActivity) context;
		// handler definition
		slowDownDrawingHandler = new Handler() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				redraw();
			}
		};
		// Launching the Thread to update draw
		background = new Thread(new Runnable() {
			/**
			 * The message exchanged between this thread and the handler
			 */
			Message myMessage;

			// Overriden Run method
			public void run() {
				try {
					while (isRunning.get()) {
						if (isPausing.get()) {
							Thread.sleep(2000);
						} else {
							// Redraw to have 30 images by second
							Thread.sleep(1000 / 30);
							// Send the message to the handler (the handler.obtainMessage is more
							// efficient that creating a message from scratch)
							// create a message, the best way is to use that method:
							myMessage = slowDownDrawingHandler.obtainMessage();
							// then send the message

							slowDownDrawingHandler.sendMessage(myMessage);
						}
					}
				} catch (Throwable t) {
					// just end the background thread
				}
			}
		});
		// Initialize the threadSafe booleans
		isRunning.set(true);
		isPausing.set(false);
		// and start it
		background.start();
	}

	/******************************************************************************************/
	/** Drawing method **************************************************************************/
	/******************************************************************************************/

	/**
	 * The method to redraw the view
	 */
	private void redraw() {
		// Log.d(tag, "redraw");
		// and make sure to redraw asap
		invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) {
		// Log.d(tag, "onDraw");
		// add a condition to slow down
		// retrieve the height and width
		width = this.getWidth();
		height = this.getHeight();
		if (!init) {
			// initialize the coordinates in the middle of the screen
			x = width / 2;
			y = height / 2;
			xLin = x;
			yLin = y;
			// initialize the speed to 0
			vx = vy = 0;
			// initialize the time to now
			t = System.nanoTime();
			// and said it's initiliazed
			init = true;
		}
		// the canvas in which we draw
		this.canvas = canvas;
		// Log.d(tag, "View width " + this.getWidth() + " height: " + this.getHeight());
		// Log.d(tag, "Canvas width " + width + " height: " + height);
		drawXYAcceleration();
	}

	/**
	 * Draw the View (circles, axis, points)
	 */
	private void drawXYAcceleration() {
		// Draw the background
		paint.setARGB(255, 255, 255, 255);
		canvas.drawRect(0, 0, width, 3 * height, paint);
		// Inistanciate the variables (acceleration vector, min and max acceleration point
		float xAcceleration = activity.x, yAcceleration = activity.y, zAcceleration = activity.z;
		float xMaxAcceleration = activity.maxX, yMaxAcceleration = activity.maxY;
		float xMinAcceleration = activity.minX, yMinAcceleration = activity.minY;
		float sensorMaxRange = activity.maxRange;
		int xCenter = width / 2, yCenter = height / 2;
		int maxRadiusSize = Math.max(width, height) / 2;
		// First draw circle (the background)
		drawCircles(xCenter, yCenter, maxRadiusSize, zAcceleration, sensorMaxRange);
		// draw the accelerator point
		drawAcceleratorPoint(xCenter, yCenter, maxRadiusSize, xAcceleration, yAcceleration);
		// then draw axis
		drawAxis(xCenter, yCenter);
		// and draw MaxX and MaxY
		drawMaxMinAccelerationAxis(xMaxAcceleration, yMaxAcceleration, xMinAcceleration, yMinAcceleration,
				sensorMaxRange, xCenter, yCenter, maxRadiusSize);
		// // draw the trajectory of a point that have such an acceleration
		drawAcceleratePointInVoid(xAcceleration, yAcceleration);
		// // show a point that move only when acceleration is applied on it
		drawFakePoint(xAcceleration, yAcceleration);

	}

	/**
	 * Draw n concentric circles (changing their colors depending of zAcceleration)
	 * 
	 * @param xCenter
	 *            the abscissa of the center of the circle
	 * @param yCenter
	 *            the ordinate of the center of the circle
	 * @param maxRadiusSize
	 *            the size of the max radius of the screen
	 * @param zAcceleration
	 *            the value of the acceleration along the z-axis
	 * @param sensorMaxRange
	 *            the maximal value of the sensor
	 */
	private void drawCircles(int xCenter, int yCenter, int maxRadiusSize, float zAcceleration, float sensorMaxRange) {
		// According to zAcceleration find the associate color
		// it's a random simple algorithm, you can choose what you need
		// declare the blue, green red colors
		int red = 0, blue = 0, green = 0;
		// Give them a color according the zAcceleration
		if (zAcceleration < 0) {
			green = (int) ((255 / sensorMaxRange) * zAcceleration + 255);
		} else {
			green = (int) ((-255 / sensorMaxRange) * zAcceleration + 255);
		}
		blue = (int) ((255 / (2 * sensorMaxRange)) * zAcceleration + (255 / 2));
		red = (int) ((-255 / (2 * sensorMaxRange)) * zAcceleration + (255 / 2));
		// Now fill full the screen with circles : step*15=maxRadiusSize*10
		// so define the variable step
		int step = maxRadiusSize / 10;
		// and draw the concentric circle (use Alpha for transparenyCenter)
		for (int i = 15; i > 0; i--) {
			// draw the border of the circle
			paint.setARGB(255 - 15 * i, 255, 255, 255);
			canvas.drawCircle(xCenter, yCenter, step * i, paint);
			// draw the background of the circle
			paint.setARGB(15 * i, red, green, blue);
			canvas.drawCircle(xCenter, yCenter, (step * i) - 3, paint);
		}
	}

	/**
	 * Draw x and y Axis in the middle of the screen
	 * 
	 * @param xCenter
	 *            the abscissa value of the vertical axis
	 * @param yCenter
	 *            the ordinate of the horizontal axis
	 */
	private void drawAxis(int xCenter, int yCenter) {
		//Select the paint to use (the color)
		paint.setARGB(255, 0, 0, 0);
		// draw an axis of 3 pixels
		for (int i = -1; i < 2; i++) {
			canvas.drawLine(xCenter + i, 0, xCenter + i, height, paint);
			canvas.drawLine(0, yCenter + i, width, yCenter + i, paint);
		}
	}

	/**
	 * Draw min and max Axis of the acceleration (a rectangle)
	 * 
	 * @param xMaxAcceleration
	 *            the max abscissa reach by the sensor
	 * @param yMaxAcceleration
	 *            the max ordinate reach by the sensor
	 * @param xMinAcceleration
	 *            the min abscissa reach by the sensor
	 * @param yMinAcceleration
	 *            the min ordinate reach by the sensor
	 * @param sensorMaxRange
	 *            the max value reachable by the sensor
	 * @param xCenter
	 *            middle of the screen abscissa
	 * @param yCenter
	 *            middle of the screen ordinate
	 * @param maxRadiusSize
	 *            max of (width,height)/2
	 */
	private void drawMaxMinAccelerationAxis(float xMaxAcceleration, float yMaxAcceleration, float xMinAcceleration,
			float yMinAcceleration, float sensorMaxRange, int xCenter, int yCenter, int maxRadiusSize) {
		// select the color to use
		paint.setARGB(100, 255, 255, 255);
		// To display:maxRange is equal to max(height,width)/2
		// then xToDisp=x*(max(height,width)/2)/maxRange
		float xToDisp = (xMaxAcceleration * maxRadiusSize) / sensorMaxRange;
		float yToDisp = (yMaxAcceleration * maxRadiusSize) / sensorMaxRange;
		// draw vertical max acceleration value'axis (according to the translation that bring (0,0)
		// in the middle of the screen
		canvas.drawLine(xCenter - xToDisp, 0, xCenter - xToDisp, height, paint);
		canvas.drawLine(0, yToDisp + yCenter, width, yToDisp + yCenter, paint);
		// same stuff but for minimal axis
		xToDisp = (xMinAcceleration * maxRadiusSize) / sensorMaxRange;
		yToDisp = (yMinAcceleration * maxRadiusSize) / sensorMaxRange;
		// draw horizontal max acceleration value'axis
		canvas.drawLine(xCenter - xToDisp, 0, xCenter - xToDisp, height, paint);
		canvas.drawLine(0, yToDisp + yCenter, width, yToDisp + yCenter, paint);
	}

	/**
	 * Draw a point that is submitted to the acceleration in void
	 * 
	 * @param xAcceleration
	 *            current acceleration on x
	 * @param yAcceleration
	 *            current acceleration on y
	 */
	private void drawAcceleratePointInVoid(float xAcceleration, float yAcceleration) {
		// calculate the new speed and so the new coordinates of the point:
		// get the time
		long t0 = System.nanoTime();
		// get the delta t
		dt = t0 - t;
		// constant a billion
		int billion = 100000000;
		// give dt in second (dts so)
		float dts = ((float) dt) / billion;
		// and define t as now
		t = t0;
		// calculate the new speed vector
		vx = vx - xAcceleration * dts;
		vy = vy + yAcceleration * dts;
		// and then the new coordinates of the point
		x = x + (vx * dts - (xAcceleration * dts * dts / 2)) / 100;
		y = y + (vy * dts - (yAcceleration * dts * dts / 2)) / 100;
		// if x or y are out of the screen then bring them back on the other side of the screen
		if (x > width) {
			x = 0;
		} else if (x < 0) {
			x = width;
		}
		if (y > height) {
			y = 0;
		} else if (y < 0) {
			y = height;
		}
		// then draw point
		paint.setARGB(60, 0, 0, 0);
		canvas.drawCircle(x, y, 10, paint);
		paint.setARGB(100, 255, 255, 255);
		canvas.drawCircle(x, y, 8, paint);
		paint.setARGB(100, 0, 0, 255);
		canvas.drawCircle(x, y, 8, paint);
		// Log.d(tag, "xCenter-x=" + (xCenter - x) + "yCenter+y=" + (yCenter + y));
	}

	/**
	 * Draw a fake point which have the same speed than the sensor value
	 * 
	 * @param xAcceleration
	 * @param yAcceleration
	 */
	private void drawFakePoint(float xAcceleration, float yAcceleration) {
		xLin = (xLin - xAcceleration);
		yLin = (yLin + yAcceleration);
		// To display:maxRange is equal to max(height,width)/2
		// then xToDisp=x*(max(height,width)/2)/maxRange
		if (xLin > width) {
			xLin = 0;
		} else if (xLin < 0) {
			xLin = width;
		}
		if (yLin > height) {
			yLin = 0;
		} else if (yLin < 0) {
			yLin = height;
		}
		// then draw point
		paint.setARGB(60, 0, 0, 0);
		canvas.drawCircle(xLin, yLin, 10, paint);
		paint.setARGB(60, 255, 255, 255);
		canvas.drawCircle(xLin, yLin, 8, paint);
		paint.setARGB(60, 0, 255, 0);
		canvas.drawCircle(xLin, yLin, 8, paint);
	}

	/**
	 * Draw the point that represents the sensor value
	 * 
	 * @param xCenter
	 *            Absissa of the center of the screen
	 * @param yCenter
	 *            Ordonnate of the center of the screen
	 * @param maxRadiusSize
	 *            The max radius of the screen
	 */
	private void drawAcceleratorPoint(int xCenter, int yCenter, int maxRadiusSize, float xAcceleration,
			float yAcceleration) {
		// then draw point
		paint.setARGB(250, 0, 0, 0);
		// To display:maxRange is equal to min(height,width)/2
		// then xToDisp=x*(min(height,width)/2)/maxRange
		float xToDisp = (activity.x * maxRadiusSize) / activity.maxRange;
		float yToDisp = (activity.y * maxRadiusSize) / activity.maxRange;

		// then draw the point using 3 circles (and translate the coordonates according to the
		// center of the screen
		canvas.drawCircle(xCenter - xToDisp, yToDisp + yCenter, 15, paint);
		paint.setARGB(255, 255, 255, 255);
		canvas.drawCircle(xCenter - xToDisp, yToDisp + yCenter, 13, paint);
		paint.setARGB(130, 0, 0, 255);
		canvas.drawCircle(xCenter - xToDisp, yToDisp + yCenter, 13, paint);
	}

}
