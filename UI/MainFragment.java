package net.hyhend.spsam.UI;

import java.util.ArrayList;

import net.hyhend.spsam.MotionDataPoint;
import net.hyhend.spsam.R;
import net.hyhend.spsam.Utils.MovementType;
import net.hyhend.spsam.Utils.Tuple;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainFragment extends Fragment {
	private View rootView;
	private String currentActivity;
	private ArrayList<Tuple<Double,MovementType>> currentActivityData;
	private Boolean buttonsEnabled;

	/**
	 * Constructor
	 */
	public MainFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_main, container, false);

		handleAnalyzeButton();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Set current activity (re show)
		if(currentActivity != null) {
			setCurrentActivity(currentActivity);
		}
		
		// Set current activity list (re show)
		if(currentActivityData != null) {
			setActivityList(currentActivityData);
		}
		
		// Set current button state (re show)
		if(buttonsEnabled == null || buttonsEnabled == true) {
			enableButtons();
		}
		else {
			disableButtons();
		}
	}
	
	@Override
	public void onStop() {
	   super.onStop();
	}

	/**
	 * Sets given string in Current Activity text field
	 * @param activity
	 */
	public void setCurrentActivity (String activity) {
		currentActivity = activity;
		try {
			TextView currentActivityTextView = (TextView) rootView.findViewById(R.id.currentActivityTextView);
			currentActivityTextView.setText(activity);
		}
		catch (Exception e) {
			Log.i("MainFragment.setCurrentActivity","Could not set activity text");
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
	        		button.setText(R.string.main_fragment_analyze_button);
	        		
	        		// Enable all other buttons
	        		MurkFragment murk = (MurkFragment) MainActivity.getMurkFragment();
	        		murk.enableButtons(); 
	        		FingerPrintFragment fp = (FingerPrintFragment) MainActivity.getFingerPrintingFragment();
	        		fp.enableButtons(); 
	        		
	        		// Find closest movement type and list of movement types
	        		MotionDataPoint[] collectedValues = ((MainActivity) getActivity()).getMotionHandler().getCollectedValues();
	        		MovementType result = ((MainActivity) getActivity()).getMotionDataAnalyzer().getMovementTypeForPoints(collectedValues);
	        		ArrayList<Tuple<Double,MovementType>> results = ((MainActivity) getActivity()).getMotionDataAnalyzer().getMovementTypesForPoints(collectedValues);

	        		// Set latest activity in UI (the one result)
	        		// And set list of activities in UI
	        		setCurrentActivity(result.toString()); 
	        		setActivityList(results); 
	        	}
	        	else {
	        		// MotionHandler is idle, turn on
	        		((MainActivity) getActivity()).getMotionHandler().start();
	        		button.setText(R.string.main_fragment_analyze_button_busy);
	        		
	        		// Disable all other buttons
	        		MurkFragment murk = (MurkFragment) MainActivity.getMurkFragment();
	        		murk.disableButtons(); 
	        		FingerPrintFragment fp = (FingerPrintFragment) MainActivity.getFingerPrintingFragment();
	        		fp.disableButtons(); 
	        	}
	        }
	    });
    }
	
    /**
     * Disables buttons in this fragment
     */
    public void disableButtons () {
    	buttonsEnabled = false;
		
		try {
			rootView.findViewById(R.id.analyzeButton).setEnabled(false);
		}
		catch (Exception e) {
			Log.i("MainFragment.disableButtons()","Could not disable buttons");
		}
    }
	
    /**
     * Enables buttons in this fragment
     */
    public void enableButtons () {
		buttonsEnabled = true;
		
		try {
			rootView.findViewById(R.id.analyzeButton).setEnabled(true);
		}
		catch (Exception e) {
			Log.i("MainFragment.disableButtons()","Could not enable buttons");
		}
    }
    
	/**
	 * Fills activityList in UI with given data
	 * @param activityData
	 */
	public void setActivityList (ArrayList<Tuple<Double,MovementType>> activityData) {
		currentActivityData = activityData;
		try {
			// Remove all elements in list
			((ViewGroup) rootView.findViewById(R.id.activityList)).removeAllViews();
			
			// Add new elements
			for(int i=0; i<activityData.size(); i++) {
				Tuple<Double,MovementType> activity = activityData.get(i);
				
				// Create layout element for this activity
				RelativeLayout activityElement = new RelativeLayout(MainActivity.getAppContext());
				RelativeLayout.LayoutParams activityElementParams = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				activityElementParams.setMargins(0, 10, 0, 0);		// L, T, R, B
				activityElement.setLayoutParams(activityElementParams);
				
				// Create time textview
				TextView timeTextView = new TextView(MainActivity.getAppContext());
				timeTextView.setText(String.valueOf(Math.round((Double) activity.key / 1000)) + " Seconds");
				LayoutParams timeTextViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				timeTextView.setPadding(80,0,0,0);			// L, T, R, B
				timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
				timeTextView.setLayoutParams(timeTextViewParams);
				
				// Create movement textview
				TextView movementTextView = new TextView(MainActivity.getAppContext());
				movementTextView.setText(activity.value.toString());
				LayoutParams movementTextViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				movementTextView.setPadding(450,0,0,0);			// L, T, R, B
				movementTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
				movementTextView.setLayoutParams(movementTextViewParams);
				//movementTextView.setTextColor("#000000");
	
				// Add to activityElement
				activityElement.addView(timeTextView);
				activityElement.addView(movementTextView);
				
				// Add to UI list
				LinearLayout activityList = (LinearLayout) rootView.findViewById(R.id.activityList);
				activityList.addView(activityElement);
			}	
		}
		catch (Exception e) {
			Log.i("MainFragment.setActivityList","Could not set activity list content");
		}
	}
	
    @Override
    public void onPause() {
        super.onPause();
    }
}
