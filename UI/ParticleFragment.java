package net.hyhend.spsam.UI;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import net.hyhend.spsam.MotionDataPoint;
import net.hyhend.spsam.R;
import net.hyhend.spsam.ParticleFilter.MovementValues;
import net.hyhend.spsam.ParticleFilter.Particle;
import net.hyhend.spsam.ParticleFilter.Particles;
import net.hyhend.spsam.Utils.Constants;
import net.hyhend.spsam.Utils.Tuple;
import android.support.v4.app.Fragment;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ParticleFragment extends Fragment {
	private View rootView;
	private long startTimestamp;
	private long lastTimestamp;
	private double totalDistance;
	private double lastAzimuth;
	private Bitmap cleanMapBmp;

	/**
	 * Constructor
	 */
	public ParticleFragment() {
		startTimestamp = System.currentTimeMillis();
		lastTimestamp = System.currentTimeMillis();
		totalDistance = 0;
		lastAzimuth = Double.MIN_VALUE;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_particle, container, false);

		// Handle button
		handleAnalyzeButton();

        // Get and store cleanMapBmp
        String currentUIMap = ((MainActivity) getActivity()).getParticleFilter().getCurrentUIMap();
		this.cleanMapBmp = getBitmapFromAsset(currentUIMap);
    
		// Draw cleanMapBmp (empty)
		drawBitmap(this.cleanMapBmp);
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
	   super.onStop();
	}
	
    @Override
    public void onPause() {
        super.onPause();
    }
	
	/**
	 * Retrieves bitmap from assets folder
	 * @param strName
	 * @return
	 */
	private Bitmap getBitmapFromAsset(String strName)
	{
	    AssetManager assetManager = MainActivity.getAppContext().getAssets();
	    InputStream istr = null;
	    try {
	        istr = assetManager.open(strName);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    BitmapFactory.Options bmo = new BitmapFactory.Options();
        bmo.inPreferredConfig = Config.ARGB_8888;
	    Bitmap bitmap = BitmapFactory.decodeStream(istr);
	    return bitmap;
	}
	
	/**
	 * Draws given bitmap in mapImageView
	 * @param bmp
	 */
	private void drawBitmap(Bitmap bmp) {
		try {
            ImageView mapImageView = (ImageView) rootView.findViewById(R.id.mapImageView);
            mapImageView.setImageBitmap(bmp);
        }
        catch(Exception ex) {
            Log.e("ParticleFragment drawBitmap()", "Could not draw bitmap.");
        }
	}
	
	/**
	 * Draws given particles in mapImageView
	 * @param particles
	 */
	public void drawParticles(Particles particles) {
		// Draw cleanMapBmp
		Bitmap editBmp = this.cleanMapBmp.copy(this.cleanMapBmp.getConfig(), true);

		// Sets pixel for each particle
		for(Particle p : particles.getParticles()) {
			// Get x and y values (scaled and translated from collision to UI map
			int x = Double.valueOf(p.getX()*Constants.collision_map_ui_scale).intValue() + Constants.collision_ui_map_x_offset;
			int y = Double.valueOf(p.getY()*Constants.collision_map_ui_scale).intValue() + Constants.collision_ui_map_y_offset;
			
			// Set pixel value
			editBmp.setPixel(x, y, Color.argb(255, 255, 0, 0));
		}
		
		drawBitmap(editBmp);
	}
	
	/**
	 * Analyzes motion
	 */
	public void motionResult (int analyzedIndex) {
		MotionDataPoint[] points = ((MainActivity) getActivity()).getMotionHandler().getCollectedValues();
		double totalAzimuth = 0;
		
		// Extract current part from points and analyze
		MotionDataPoint[] pointsPart = Arrays.copyOfRange(points, analyzedIndex, points.length);
		ArrayList<Tuple<Long, MovementValues>> movementValues = ((MainActivity) getActivity()).getRealTimeMotionDataAnalyzer().getMovementForPoints(pointsPart);
		
		// Process found velocities
		if(movementValues.size() >= 1) {
			double distance = 0;

			// Choose first azimuth randomly between min or max (either one is the correct one)
			// Only when there is no previous last azimuth
			// Use lastAzimuth when difference between current and last > maxAzimuthDifference
			double maxAzimuthDifference = 0.6*Math.PI;
			if (lastAzimuth < (Double.MAX_VALUE+1) 
					&& Math.random() > 0.5
					&& maxAzimuthDifference > Math.abs(lastAzimuth -  movementValues.get(0).value.getMinAzimuth())) {
				lastAzimuth = movementValues.get(0).value.getMinAzimuth();
			}
			else if (lastAzimuth < (Double.MAX_VALUE+1)
					&& maxAzimuthDifference > Math.abs(lastAzimuth -  movementValues.get(0).value.getMaxAzimuth())) {
				lastAzimuth = movementValues.get(0).value.getMaxAzimuth();
			}
			
			// Calculate totalDistance, totalAzimuth and show in UI
			for(Tuple<Long, MovementValues> movement : movementValues) {
				distance += movement.value.getDistance() * (System.currentTimeMillis() - lastTimestamp) / 1000;
				totalDistance += distance;
				
				// Choose min or max azimuth. Closest to last azimuth is chosen
				if(Math.abs(lastAzimuth - movement.value.getMinAzimuth()) < Math.abs(lastAzimuth - movement.value.getMaxAzimuth())) {
					totalAzimuth = totalAzimuth + movement.value.getMinAzimuth();
					lastAzimuth = movement.value.getMinAzimuth();
				}
				else {
					totalAzimuth = totalAzimuth + movement.value.getMaxAzimuth();
					lastAzimuth = movement.value.getMaxAzimuth();
				}
				
				// Show in UI
				addToList(totalDistance, ((totalAzimuth + Math.PI) / (2 * Math.PI)) * 360, "M", "Deg");
				
				// Update last time
				lastTimestamp = System.currentTimeMillis();
			}
			
			// Get angle, combine velocities and call particleFilter
			final double finalDistance = distance;
			final double finalAzimuth = totalAzimuth / movementValues.size();
	        new Thread(new Runnable() {
	            @Override
	            public void run() {
	            	((MainActivity) getActivity()).getParticleFilter().motion((finalDistance*100), finalAzimuth);
	            	
	            	// Retrieve and draw particles (on UI thread)
	            	final Particles particles = ((MainActivity) getActivity()).getParticleFilter().getParticles();
	            	((MainActivity) getActivity()).runOnUiThread(new Runnable() {
	            	    public void run() {
	    	            	drawParticles(particles);
	            	    }
	            	});
	            }
	        }).start();
	        
	        // Set in UI
	        String distanceString = (String.format( "%.1f", finalDistance));
	        String headingString = (String.format( "%.0f",  (((finalAzimuth + Constants.collision_map_north_offset) + Math.PI) / (2 * Math.PI)) * 360));
			setCurrent(distanceString + "m/s, " + headingString + "d");
		}
	}
	
	/**
     * Handles Analyze button events
     */
    private void handleAnalyzeButton () {
    	final Button button = (Button) rootView.findViewById(R.id.analyzeButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	// Analyze motion depending on state
	        	if(((MainActivity) getActivity()).getMotionHandler().getActive()) {
	        		// MotionHandler is busy, turn off
	        		((MainActivity) getActivity()).getMotionHandler().stop();
	        		((MainActivity) getActivity()).getMotionHandler().setRealTime(false);
	        		button.setText(R.string.particle_fragment_analyze_button);
	        		
	        		// Reset the particles
	        		((MainActivity) getActivity()).getParticleFilter().resetParticles();
	        	}
	        	else {
	        		// MotionHandler is idle, turn on
	        		((MainActivity) getActivity()).getMotionHandler().start();
	        		((MainActivity) getActivity()).getMotionHandler().setRealTime(true);
	        		button.setText(R.string.particle_fragment_analyze_button_busy);
	        		
	        		// Remove all elements in list
	    			((ViewGroup) rootView.findViewById(R.id.velocityList)).removeAllViews();
	    			
	    			// Remember start times	        		
	        		startTimestamp = System.currentTimeMillis();
	        		lastTimestamp = System.currentTimeMillis();
	        		
	        		// Reset distance and azimuth
	        		totalDistance = 0;
	        		lastAzimuth = Double.MIN_VALUE;
	        		
	        	}
	        }
	    });
    }

	/**
	 * Sets given value in Current Speed text field
	 * @param speed
	 */
	public void setCurrent (String value) {
		try {
			TextView currentSpeedTextView = (TextView) rootView.findViewById(R.id.currentVelocityTextView);
			currentSpeedTextView.setText(value);
		}
		catch (Exception e) {
			Log.i("ParticleFragment.setCurrentSpeed","Could not set text");
		}
	}
    
    /**
     * Adds velocity to list
     * @param velocity
     */
    public void addToList (Double value1, Double value2, String units1, String units2) {
    	try {
	    	// Create layout element for this velocity
			RelativeLayout velocityElement = new RelativeLayout(MainActivity.getAppContext());
			RelativeLayout.LayoutParams velocityElementParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			velocityElementParams.setMargins(0, 10, 0, 0);		// L, T, R, B
			velocityElement.setLayoutParams(velocityElementParams);
			
			// Create time textview
			long timeElapsed = System.currentTimeMillis() - startTimestamp;
			TextView timeTextView = new TextView(MainActivity.getAppContext());
			timeTextView.setText(String.format( "%.0f", Double.valueOf(timeElapsed/1000)) + " Seconds");
			LayoutParams timeTextViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			timeTextView.setPadding(80,0,0,0);			// L, T, R, B
			timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
			timeTextView.setLayoutParams(timeTextViewParams);
			
			// Create velocity textview
			TextView velocityTextView = new TextView(MainActivity.getAppContext());
			velocityTextView.setText(String.format( "%.2f", value1) + " " + units1 + " (" + String.format( "%.2f", value2) + " " + units2 + ")");
			LayoutParams movementTextViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			velocityTextView.setPadding(450,0,0,0);			// L, T, R, B
			velocityTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
			velocityTextView.setLayoutParams(movementTextViewParams);
	
			// Add to VelocityElement
			velocityElement.addView(timeTextView);
			velocityElement.addView(velocityTextView);
			
			// Add to UI list
			LinearLayout velocityList = (LinearLayout) rootView.findViewById(R.id.velocityList);
			velocityList.addView(velocityElement);
		}
		catch (Exception e) {
			Log.i("ParticleFragment.addVelocityToList","Could not set velocity list content");
		}
    }
	
    /**
     * Disables buttons in this fragment
     */
    public void disableButtons () {
    	try {
			rootView.findViewById(R.id.analyzeButton).setEnabled(false);
		}
		catch (Exception e) {
			Log.i("ParticleFragment.disableButtons()","Could not disable buttons");
		}
    }
	
    /**
     * Enables buttons in this fragment
     */
    public void enableButtons () {
		try {
			rootView.findViewById(R.id.analyzeButton).setEnabled(true);
		}
		catch (Exception e) {
			Log.i("ParticleFragment.disableButtons()","Could not enable buttons");
		}
    }
}
