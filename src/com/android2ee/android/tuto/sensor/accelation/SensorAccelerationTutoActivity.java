/**
 * <ul>
 * <li>SensorAccelerationTuto</li>
 * <li>com.android2ee.android.tuto.sensor.accelation</li>
 * <li>17 Novembre 2011</li>
 * 
 * <li>======================================================</li>
 * 
 * <li>Projet : Mathias Seguy (Android2EE)</li>
 * <li>Produit par MSE.</li>
 *
 * <ul>
 * Android Tutorial, An <strong>Android2EE</strong>'s project.</br> 
 * Produced by <strong>Dr. Mathias SEGUY</strong>.</br>
 * Delivered by <strong>http://android2ee.com/</strong></br>
 *  Belongs to <strong>Mathias Seguy</strong></br>
 ****************************************************************************************************************</br>
 * This code is free for any usage but can't be distribute.</br>
 * The distribution is reserved to the site <strong>http://android2ee.com</strong>.</br>
 * * The intelectual property belongs to <strong>Mathias Seguy</strong>.</br>
 * <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * 
 * *****************************************************************************************************************</br>
 *  Ce code est libre de toute utilisation mais n'est pas distribuable.</br>
 *  Sa distribution est reservée au site <strong>http://android2ee.com</strong>.</br> 
 *  Sa propriété intellectuelle appartient à <strong>Mathias Seguy</strong>.</br>
 *  <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * *****************************************************************************************************************</br>
 */
package com.android2ee.android.tuto.sensor.accelation;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class is the main activity and aims to :
 *        <ul>
 *        <li>Listen for sensors' values for the Accelerometer, Gravity, Linear Acceleration sensors.</li>
 *        <li>Display those values</li>
 *        <li>Give the ability to change the listened sensor by using a spinner</li>
 *        </ul>
 */
public class SensorAccelerationTutoActivity extends Activity implements SensorEventListener {
	// see :http://developer.android.com/reference/android/hardware/SensorEvent.html
	// see also:http://developer.android.com/reference/android/hardware/SensorManager.html
	/**
	 * The Tag for the Log
	 */
	private static final String LOG_TAG = "SensorsAccelerometer";

	/******************************************************************************************/
	/** Current sensor value **************************************************************************/
	/******************************************************************************************/
	/**
	 * Current value of the accelerometer
	 */
	float x, y, z;
	/**
	 * Current max value for the x,y,z
	 */
	float maxX = 0, maxY = 0, maxZ = 0;
	/**
	 * Current min value for the x,y,z
	 */
	float minX = 0, minY = 0, minZ = 0;
	/**
	 * Max range of the sensor
	 */
	float maxRange;
	/******************************************************************************************/
	/** View **************************************************************************/
	/******************************************************************************************/
	/**
	 * The Layout Parameter used to add Name in LilContent
	 */
	LinearLayout.LayoutParams lParamsName;
	/**
	 * The layout within the graphic is draw
	 */
	LinearLayout xyAccelerationLayout;
	/**
	 * The view that draw the graphic
	 */
	XYAceleromterView xyAccelerationView;
	/**
	 * The spinner used to select the sensors
	 */
	Spinner s;
	/**
	 * The progress bar that displays the X, Y, Z value of the vector
	 */
	ProgressBar pgbX,pgbY,pgbZ;
	/******************************************************************************************/
	/** Sensors and co **************************************************************************/
	/******************************************************************************************/
	/**
	 * The one that know what is the orientation of the device
	 */
	private Display mDisplay;
	/** * The sensor manager */
	SensorManager sensorManager;
	/**
	 * The accelerometer
	 */
	Sensor accelerometer;
	/**
	 * The gravity
	 */
	Sensor gravity;
	/**
	 * The linear accelerometer
	 */
	Sensor linearAcc;
	/******************************************************************************************/
	/** Sensors Type Constant **************************************************************************/
	/******************************************************************************************/

	/**
	 * The sensor type can be one of those below
	 */
	private int sensorType;
	/**
	 * Sensor type Accelerometer
	 */
	private static final int ACCELE = 0;
	/**
	 * Sensor type Gravity
	 */
	private static final int Gravity = 1;
	/**
	 * Sensor type linear acceleration
	 */
	private static final int LINEAR_ACCELE = 2;

	/******************************************************************************************/
	/** Manage life cycle **************************************************************************/
	/******************************************************************************************/
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// build the GUI
		setContentView(R.layout.main);
		// Initialise the sensorType and the spinner of sensor selection
		sensorType = ACCELE;
		//instantiate the progress bars
		pgbX = (ProgressBar) findViewById(R.id.progressBarX);
		pgbY = (ProgressBar) findViewById(R.id.progressBarY);
		pgbZ = (ProgressBar) findViewById(R.id.progressBarZ);
		// Instantiate the spinner
		s = (Spinner) findViewById(R.id.spinner);
		// Instantiate the array adapter of the spinner
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.sensors, R.layout.spinnertext);
		// define how the spinner react when click to make a selection
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// attach the array_adapter to the spinner
		s.setAdapter(adapter);
		// when an item of the spinner is selected call the selectSensor method
		s.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectSensor(parent.getItemAtPosition(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		// Then manage the sensors and listen for changes
		// Instantiate the SensorManager
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// Instantiate the accelerometer
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// Instantiate the gravity
		gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		// Instantiate the accelerometer
		linearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		// and instantiate the display to know the device orientation
		mDisplay = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		// Then build the GUI:
		// Build the acceleration view
		// first retrieve the layout:
		xyAccelerationLayout = (LinearLayout) findViewById(R.id.layoutOfXYAcceleration);
		// then build the view
		xyAccelerationView = new XYAceleromterView(this);
		// define the layout parameters and add the view to the layout
		LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		// add the view in the layout
		xyAccelerationLayout.addView(xyAccelerationView, layoutParam);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// unregister every body
		sensorManager.unregisterListener(this, accelerometer);
		sensorManager.unregisterListener(this, gravity);
		sensorManager.unregisterListener(this, linearAcc);
		// and don't forget to pause the thread that redraw the xyAccelerationView
		xyAccelerationView.isPausing.set(true);
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		/*
		 * It is not necessary to get accelerometer events at a very high
		 * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
		 * automatic low-pass filter, which "extracts" the gravity component
		 * of the acceleration. As an added benefit, we use less power and
		 * CPU resources.
		 */
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, linearAcc, SensorManager.SENSOR_DELAY_UI);
		// and don't forget to re-launch the thread that redraws the xyAccelerationView
		xyAccelerationView.isPausing.set(false);
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// Log.d(LOG_TAG, "onDestroy()");
		// kill the thread
		xyAccelerationView.isRunning.set(false);
		super.onDestroy();
	}

	/******************************************************************************************/
	/** Item selection management **************************************************************************/
	/******************************************************************************************/
	/**
	 * @param item
	 */
	private void selectSensor(Object item) {
		// Log.d(LOG_TAG, item.toString()+" class "+item.getClass().getSimpleName());
		String itemName = (String) item;
		if (itemName.equalsIgnoreCase("Accelerometer")) {
			sensorType = ACCELE;
			maxRange = accelerometer.getMaximumRange();
		} else if (itemName.equalsIgnoreCase("Gravity")) {
			sensorType = Gravity;
			maxRange = gravity.getMaximumRange();
		} else if (itemName.equalsIgnoreCase("Linear Accelerometer")) {
			sensorType = LINEAR_ACCELE;
			maxRange = linearAcc.getMaximumRange();
		}
		setProgressBarLenght();
	}

	/**
	 * Define the Max of the progress bar according to the max range of the sensor
	 */
	private void setProgressBarLenght() {
		pgbX.setMax((int)maxRange);
		pgbY.setMax((int)maxRange);
		pgbZ.setMax((int)maxRange);
	}
	/******************************************************************************************/
	/** ProgressBar update **************************************************************************/
	/******************************************************************************************/

	/**
	 * Update the value of the progressbar according to the value of the sensor
	 * we use the secondary progress to display negative value
	 */
	private void updateProgressBar() {
		if(x>0) {
			pgbX.setProgress((int)x);
		}else {
			pgbX.setSecondaryProgress(-1*(int)x);
		}
		if(y>0) {
			pgbY.setProgress((int)y);
		}else {
			pgbY.setSecondaryProgress(-1*(int)y);
		}
		if(z>0) {
			pgbZ.setProgress((int)z);
		}else {
			pgbZ.setSecondaryProgress(-1*(int)z);
		}
	}
	/******************************************************************************************/
	/** SensorEventListener **************************************************************************/
	/******************************************************************************************/

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		// update only when your are in the right case:
		if (((event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) && (sensorType == ACCELE))
				|| ((event.sensor.getType() == Sensor.TYPE_GRAVITY) && (sensorType == Gravity))
				|| ((event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) && (sensorType == LINEAR_ACCELE))) {
			// Log.d(LOG_TAG, "TYPE_ACCELEROMETER");
			// Depending on the device orientation get the x,y value of the acceleration
			switch (mDisplay.getRotation()) {
			case Surface.ROTATION_0:
				x = event.values[0];
				y = event.values[1];
				break;
			case Surface.ROTATION_90:
				x = -event.values[1];
				y = event.values[0];
				break;
			case Surface.ROTATION_180:
				x = -event.values[0];
				y = -event.values[1];
				break;
			case Surface.ROTATION_270:
				x = event.values[1];
				y = -event.values[0];
				break;
			}
			// the z value
			z = event.values[2];
			// and update the min max value of the acceleration
			updateMinAndMax();
			//update the progressBar
			updateProgressBar();
			Log.d(LOG_TAG, "Sensor's values ("+x+","+y+","+z+") and maxRange : "+maxRange);
		}
	}

	/**
	 * Update the min and max value reached by the sensor
	 */
	private void updateMinAndMax() {
		// set the min and max
		if (x < minX) {
			minX = x;
		} else if (x > maxX) {
			maxX = x;
		}
		if (y < minY) {
			minY = y;
		} else if (y > maxY) {
			maxY = y;
		}
		if (z < minZ) {
			minZ = z;
		} else if (z > maxZ) {
			maxZ = z;
		}
	}
}