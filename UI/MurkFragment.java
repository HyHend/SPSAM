package net.hyhend.spsam.UI;


import net.hyhend.spsam.R;
import net.hyhend.spsam.Utils.MovementType;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MurkFragment extends Fragment {
	private View rootView;
	private Boolean buttonsEnabled;

	/**
	 * Constructor
	 */
	public MurkFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_murk, container, false);

		TextView mainTextView = (TextView) rootView.findViewById(R.id.mainTextView);
		mainTextView.setText("Train your device");

		// Handle the buttons
		handleWalkingButton();
		handleRunningButton();
		handleSittingButton();
		handleStairsDownButton();
		handleStairCLimbingButton();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Set current activity (re show)
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
	 * Enables all buttons in this view
	 */
	public void enableButtons () {
		buttonsEnabled = true;
		
		try {
			rootView.findViewById(R.id.trainWalkingButton).setEnabled(true);
			rootView.findViewById(R.id.trainRunningButton).setEnabled(true);
			rootView.findViewById(R.id.trainSittingButton).setEnabled(true);
			rootView.findViewById(R.id.trainStairClimbingButton).setEnabled(true);
			rootView.findViewById(R.id.trainCyclingButton).setEnabled(true);
		}
		catch (Exception e) {
			Log.i("MurkFragment.disableButtons()","Could not enable buttons");
		}
	}
	
	/**
	 * Disables all buttons in this view
	 */
	public void disableButtons () {
		buttonsEnabled = false;
		
		try {
			rootView.findViewById(R.id.trainWalkingButton).setEnabled(false);
			rootView.findViewById(R.id.trainRunningButton).setEnabled(false);
			rootView.findViewById(R.id.trainSittingButton).setEnabled(false);
			rootView.findViewById(R.id.trainStairClimbingButton).setEnabled(false);
			rootView.findViewById(R.id.trainCyclingButton).setEnabled(false);
		}
		catch (Exception e) {
			Log.i("MurkFragment.disableButtons()","Could not disable buttons");
		}
	}
	
	/**
     * Handles Walk button events
     */
    private void handleWalkingButton () {
    	final Button button = (Button) rootView.findViewById(R.id.trainWalkingButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	if(((MainActivity) getActivity()).getMotionHandler().getActive()) {
	        		// MotionHandler is busy, turn off
	        		((MainActivity) getActivity()).getMotionHandler().stop();
	        		button.setText(R.string.train_walking_button);
	        		
	        		// Enable all buttons
	        		enableButtons();
	        		((MainFragment) MainActivity.getMainFragment()).enableButtons();
	        		
	        		// Save training data
	        		((MainActivity) getActivity()).getMotionDataAnalyzer().train(((MainActivity) getActivity()).getMotionHandler().getCollectedValues(), MovementType.Walking);
	        	}
	        	else {
	        		// MotionHandler is idle, turn on
	        		((MainActivity) getActivity()).getMotionHandler().start();
	        		button.setText(R.string.train_walking_button_busy);
	        		
	        		// Disable all other buttons
	        		((MainFragment) MainActivity.getMainFragment()).disableButtons();
	        		rootView.findViewById(R.id.trainRunningButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainSittingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainStairClimbingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainCyclingButton).setEnabled(false);
	        	}
	        }
	    });
    }
	
	/**
     * Handles Run button events
     */
    private void handleRunningButton () {
    	final Button button = (Button) rootView.findViewById(R.id.trainRunningButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	if(((MainActivity) getActivity()).getMotionHandler().getActive()) {
	        		// MotionHandler is busy, turn off
	        		((MainActivity) getActivity()).getMotionHandler().stop();
	        		button.setText(R.string.train_running_button);
	        		
	        		// Enable all buttons
	        		enableButtons();
	        		((MainFragment) MainActivity.getMainFragment()).enableButtons();
	        		
	        		// Save training data
	        		((MainActivity) getActivity()).getMotionDataAnalyzer().train(((MainActivity) getActivity()).getMotionHandler().getCollectedValues(), MovementType.Running);
	        	}
	        	else {
	        		// MotionHandler is idle, turn on
	        		((MainActivity) getActivity()).getMotionHandler().start();
	        		button.setText(R.string.train_running_button_busy);
	        		
	        		// Disable all other buttons
	        		((MainFragment) MainActivity.getMainFragment()).disableButtons();
	        		rootView.findViewById(R.id.trainWalkingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainSittingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainStairClimbingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainCyclingButton).setEnabled(false);
	        	}
	        }
	    });
    }
	
	/**
     * Handles Sit button events
     */
    private void handleSittingButton () {
    	final Button button = (Button) rootView.findViewById(R.id.trainSittingButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	if(((MainActivity) getActivity()).getMotionHandler().getActive()) {
	        		// MotionHandler is busy, turn off
	        		((MainActivity) getActivity()).getMotionHandler().stop();
	        		button.setText(R.string.train_sitting_button);
	        		
	        		// Enable all buttons
	        		enableButtons();
	        		((MainFragment) MainActivity.getMainFragment()).enableButtons();
	        		
	        		// Save training data
	        		((MainActivity) getActivity()).getMotionDataAnalyzer().train(((MainActivity) getActivity()).getMotionHandler().getCollectedValues(), MovementType.Sitting);
	        	}
	        	else {
	        		// MotionHandler is idle, turn on
	        		((MainActivity) getActivity()).getMotionHandler().start();
	        		button.setText(R.string.train_sitting_button_busy);
	        		
	        		// Disable all other buttons
	        		((MainFragment) MainActivity.getMainFragment()).disableButtons();
	        		rootView.findViewById(R.id.trainWalkingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainRunningButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainStairClimbingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainCyclingButton).setEnabled(false);
	        	}
	        }
	    });
    }
	
	/**
     * Handles Cycle button events
     */
    private void handleStairsDownButton () {
    	final Button button = (Button) rootView.findViewById(R.id.trainCyclingButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	if(((MainActivity) getActivity()).getMotionHandler().getActive()) {
	        		// MotionHandler is busy, turn off
	        		((MainActivity) getActivity()).getMotionHandler().stop();
	        		button.setText(R.string.train_stairdown_button);
	        		
	        		// Enable all buttons
	        		enableButtons();
	        		((MainFragment) MainActivity.getMainFragment()).enableButtons();
	        		
	        		// Save training data
	        		((MainActivity) getActivity()).getMotionDataAnalyzer().train(((MainActivity) getActivity()).getMotionHandler().getCollectedValues(), MovementType.StairsDown);
	        	}
	        	else {
	        		// MotionHandler is idle, turn on
	        		((MainActivity) getActivity()).getMotionHandler().start();
	        		button.setText(R.string.train_cycling_button_busy);
	        		
	        		// Disable all other buttons
	        		((MainFragment) MainActivity.getMainFragment()).disableButtons();
	        		rootView.findViewById(R.id.trainWalkingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainRunningButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainSittingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainStairClimbingButton).setEnabled(false);
	        	}
	        }
	    });
    }
	
	/**
     * Handles Stair button events
     */
    private void handleStairCLimbingButton () {
    	final Button button = (Button) rootView.findViewById(R.id.trainStairClimbingButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	if(((MainActivity) getActivity()).getMotionHandler().getActive()) {
	        		// MotionHandler is busy, turn off
	        		((MainActivity) getActivity()).getMotionHandler().stop();
	        		button.setText(R.string.train_stairclimbing_button);
	        		
	        		// Enable all buttons
	        		enableButtons();
	        		((MainFragment) MainActivity.getMainFragment()).enableButtons();
	        		
	        		// Save training data
	        		((MainActivity) getActivity()).getMotionDataAnalyzer().train(((MainActivity) getActivity()).getMotionHandler().getCollectedValues(), MovementType.StairsUp);
	        	}
	        	else {
	        		// MotionHandler is idle, turn on
	        		((MainActivity) getActivity()).getMotionHandler().start();
	        		button.setText(R.string.train_stairclimbing_button_busy);
	        		
	        		// Disable all other buttons
	        		((MainFragment) MainActivity.getMainFragment()).disableButtons();
	        		rootView.findViewById(R.id.trainWalkingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainRunningButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainSittingButton).setEnabled(false);
	        		rootView.findViewById(R.id.trainCyclingButton).setEnabled(false);
	        	}
	        }
	    });
    }
	
    @Override
    public void onPause() {
        super.onPause();
    }
}
