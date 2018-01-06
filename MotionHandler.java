package net.hyhend.spsam;

import net.hyhend.spsam.UI.MainActivity;
import net.hyhend.spsam.UI.ParticleFragment;
import net.hyhend.spsam.Utils.Constants;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MotionHandler extends Activity implements SensorEventListener {
	private boolean active;
	private SensorManager sensorManager; 
	private Sensor accelerometer; 
	private Sensor magnetometer; 
	private int sampleRate; 
	private MergedMotionHistory mergedHistory;
	private int analyzedIndex;
	private boolean isRealTime;
	private float[] latestMagenetoValues;
	
	/**
	 * Constructor
	 */
	public MotionHandler(int sampleRate) {
		this.sampleRate = sampleRate;
		this.active = false;

		mergedHistory = new MergedMotionHistory();
		
		// Init realtime as false
		isRealTime = false;
		
		// Nothing has been analyzed yet
		analyzedIndex = 0;
		
		// Init Sensor
		sensorManager = (SensorManager) MainActivity.getAppContext().getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	/**
	 * Enable or disable realTime functionality
	 * @param realTime
	 */
	public void setRealTime(boolean realTime) {
		this.isRealTime = realTime;
	}
	
	/**
	 * @return isRealTime
	 */
	public boolean getRealTime() {
		return this.isRealTime;
	}

	/**
	 * Returns true when motion is being captured, false otherwise
	 * @return active
	 */
	public boolean getActive () {
		return this.active;
	}
	
	/**
	 * Returns the list of collected values
	 * @return MotionDataPoint[]
	 */
	public MotionDataPoint[] getCollectedValues() {
		return this.mergedHistory.getTimeDistributedPoints();
	}
	
	/**
	 * Starts accelerometer retrieval
	 */
	public void start() {
		this.active = true;
		analyzedIndex = 0;
		mergedHistory = new MergedMotionHistory();
		sensorManager.registerListener(this, accelerometer, sampleRate);
		sensorManager.registerListener(this, magnetometer, sampleRate);
	}

	/**
	 * Stops accelerometer retrieval
	 */
	public void stop() {
		this.active = false;
		sensorManager.unregisterListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		this.active = true;
		sensorManager.registerListener(this, accelerometer, sampleRate);
		sensorManager.registerListener(this, magnetometer, sampleRate);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.active = false;
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor s, int a) {
		// Do Nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//System.out.println("x: " + event.values[0] + "  y: "  + event.values[1] + "  z: " + event.values[2]);
		
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			double x = event.values[0];	
			double y = event.values[1];	
			double z = event.values[2];
			
			// Calculate azimuth from x, y, z and latestMagenetoValues
			double azimuth = 0;
			if(latestMagenetoValues != null) {
				float[] orientation = new float[3];
				float[] mRotationMatrix = new float[9];
				SensorManager.getRotationMatrix(mRotationMatrix, null, event.values, latestMagenetoValues);
	            SensorManager.getOrientation(mRotationMatrix, orientation);
	            azimuth = Float.valueOf(orientation[0]).doubleValue();
			}
				
			// Calculate combined motion (x,y,z)
			double powX = Math.pow(x, 2);
			double powY = Math.pow(y, 2);
			double powZ = Math.pow(z, 2);
			double mergedSignal = Math.sqrt(powX + powY + powZ);
			long timestampinMS = event.timestamp/1000000;
			MotionDataPoint dataPoint  = new MotionDataPoint(timestampinMS,mergedSignal, x, y, z, azimuth);
			mergedHistory.addDataPoint(dataPoint);
			
			// Init realtime analysis functionality (if set to on)
			if(this.isRealTime) {
				handleRealTimeAnalysis();
			}
		}
		else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			latestMagenetoValues = event.values;
		}
	}
	
	/**
	 * Handles real time analysis. Calls motionResult in ParticleFragment
	 */
	private void handleRealTimeAnalysis () {
		// If the amount of points is divisible by the realtime window size
		//    or the amount of point supersedes the last analysis+window size
		if(mergedHistory.getRawPointsSizeAdjusted() != 0
				&& ((mergedHistory.getRawPointsSizeAdjusted() % Constants.realtimeWindowSize == 0)
						|| analyzedIndex + Constants.realtimeWindowSize < mergedHistory.getRawPointsSizeAdjusted())) {
			// Init realtime analysis functionality
			ParticleFragment particleFragment = (ParticleFragment) MainActivity.getParticleFragment();
			particleFragment.motionResult(analyzedIndex);	
			
			// Remember end point of analyzed history
			int analyzableSamples = Constants.realtimeWindowSize * Double.valueOf(Math.floor((mergedHistory.getRawPointsSizeAdjusted() - analyzedIndex) / Constants.realtimeWindowSize)).intValue();
			analyzedIndex = analyzedIndex + analyzableSamples;
			
			// Make sure mergedHistory is not too long
			if(analyzedIndex > (Constants.realtimeWindowSize * 8)) {
				mergedHistory.removeSamplesFromStart(Constants.realtimeWindowSize * 8);
				analyzedIndex = analyzedIndex - Constants.realtimeWindowSize * 8;
			}
		}
	}

}
