package net.hyhend.spsam.UI;


import java.util.List;

import net.hyhend.spsam.R;

import net.hyhend.spsam.Localization.Location;
import net.hyhend.spsam.Localization.RSSIHandler;
import net.hyhend.spsam.Localization.RssiFingerPrint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class FingerPrintFragment extends Fragment {
	private View rootView;
	private RSSIHandler rssiHandler;
	private Boolean buttonsEnabled;
	private int currentResultValue;
	
	/**
	 * Constructor
	 */
	public FingerPrintFragment()
	{
		currentResultValue = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_fingerprint, container, false);
		
		// Button handlers
		handleRSSIMeasuringButton();
		
		// Start RSSI Handler
		rssiHandler = ((MainActivity) getActivity()).getRSSIHandler();
		
		// Set spinner (dropdown) content
		Spinner mySpinner = (Spinner) rootView.findViewById(R.id.LocationSpinner);
		mySpinner.setAdapter(new ArrayAdapter<Location>(rootView.getContext(), android.R.layout.simple_list_item_1, Location.values()));
		
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
	 * Updates results in UI
	 * @param results
	 */
	public void updateWifiResults(List<RssiFingerPrint> results)
	{
		String result = "";
		currentResultValue = currentResultValue + 1; 		// Remember which result value we're at
		
		// Set current results text
		TextView currentResults = (TextView) rootView.findViewById(R.id.mainTextView);
		currentResults.setText("Current results (" + currentResultValue + "):");
		
		// Set textview
        for (int i=0; i<results.size(); i++) {
        	RssiFingerPrint fp = results.get(i);
        	result += " BSSID:" + fp.getIdentifier() + " RSSI: " + fp.getRssi() + "\n" ;     	  
        }
		
		TextView RSSIListTextView = (TextView) rootView.findViewById(R.id.RSSIListTextView);
        RSSIListTextView.setText(result);

        // Blink once
        Animation anim = new AlphaAnimation(0.2f, 1.0f);
        anim.setDuration(800); //You can manage the time of the blink with this parameter
        anim.setStartOffset(0);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setRepeatMode(Animation.ABSOLUTE);
        anim.setRepeatCount(0);
        RSSIListTextView.startAnimation(anim);
	}
	
	/**
	 * Handler RSSI button
	 */
	private void handleRSSIMeasuringButton () {
		final Button button = (Button) rootView.findViewById(R.id.fingerPrintButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	if(rssiHandler.getActive()) {
					rssiHandler.stopWifiPolling();
	        		button.setText(R.string.fingerprint_button);

	        		// Enable main button
	        		((MainFragment) MainActivity.getMainFragment()).enableButtons();
	        	}
	        	else {
	                // Get location from spinner
	                Spinner mySpinner = (Spinner) rootView.findViewById(R.id.LocationSpinner);
	        		Location loc = (Location)mySpinner.getSelectedItem();
	        		
	        		// Start polling
		        	rssiHandler.startWifiPollingForLocation(loc);
	        		button.setText(R.string.fingerprint_button_busy);
	        		currentResultValue = 0;

	        		// Disable main button
	        		((MainFragment) MainActivity.getMainFragment()).enableButtons();
	        	}
	        }
		});
	}
	
	/**
	 * Enables all buttons in this view
	 */
	public void enableButtons () {
		buttonsEnabled = true;
		
		try {
			rootView.findViewById(R.id.fingerPrintButton).setEnabled(true);
			rootView.findViewById(R.id.LocationSpinner).setEnabled(true);
			rootView.findViewById(R.id.LocationSpinner).setClickable(true);
		}
		catch (Exception e) {
			Log.i("FingerPrintFragment.disableButtons()","Could not enable buttons");
		}
	}
	
	/**
	 * Disables all buttons in this view
	 */
	public void disableButtons () {
		buttonsEnabled = false;
		
		try {
			rootView.findViewById(R.id.fingerPrintButton).setEnabled(false);
			rootView.findViewById(R.id.LocationSpinner).setEnabled(false);
			rootView.findViewById(R.id.LocationSpinner).setClickable(false);
		}
		catch (Exception e) {
			Log.i("FingerPrintFragment.disableButtons()","Could not disable buttons");
		}
	}

}
