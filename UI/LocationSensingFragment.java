package net.hyhend.spsam.UI;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.hyhend.spsam.R;
import net.hyhend.spsam.Localization.Location;
import net.hyhend.spsam.Localization.RssiAnalyzer;
import net.hyhend.spsam.Localization.RssiFingerPrint;
import net.hyhend.spsam.Utils.Constants;
import net.hyhend.spsam.Utils.Tuple;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LocationSensingFragment extends Fragment {
	private View rootView;
	private Boolean buttonsEnabled;
	private List<RssiFingerPrint> wifiSample;
	private int totalAPSFound;
	
	public LocationSensingFragment() {
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Set current button state (re show)
		if(buttonsEnabled == null || buttonsEnabled == true) {
			enableButtons();
		}
		else {
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_locationsensing,
				container, false);
		handleInitialBeliefButton();
		handleSenseAPButton();
		handleSenseNewScanButton();
		wifiSample = new ArrayList<RssiFingerPrint>();
		buttonsEnabled = true;

		return rootView;
	}

	/**
	 * Handler Initial Belief button This method differs from sense new scan in
	 * the fact that the initial belief resets the status completely, where
	 * sense new scan updates its data based on movement by the user (particle
	 * filtering), and then does a new wifi polling
	 */
	private void handleInitialBeliefButton() {
		final Button button = (Button) rootView
				.findViewById(R.id.initial_belief_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				// Resets prior to initial belief (equal probability for each
				// location)
				RssiAnalyzer analyzer =  ((MainActivity) getActivity()).getRSSIAnalyzer();
				TextView apLabel = (TextView) rootView
						.findViewById(R.id.APStatus);
				apLabel.setText("0 / 0 / -");
				analyzer.setToInitialBelief();
				DecimalFormat df = new DecimalFormat("#.##");
				//Show this initial belief on the map:
				for (Location loc : Location.values()) {
					
					TextView correspondingLabel = (TextView) rootView
							.findViewById(Constants
									.getIdForLocation(loc));
					if (correspondingLabel != null) {
						correspondingLabel.setText(df.format(analyzer.InitialBelief));
					}
				}		
			}
		});
	}

	/**
	 * Handler Sense new AP
	 * 
	 * This button takes the current belief (so for the first time the initial
	 * belief, and after that the then known belief) and updates this belief
	 * based on the most dominant access point not yet included in the belief
	 * 
	 * NOTE: this method does not poll the wifi networks, but uses the sample
	 * that has been taken during the press on the initial belief button, or the
	 * new scan button
	 */
	private void handleSenseAPButton() {
		final Button button = (Button) rootView
				.findViewById(R.id.sense_new_AP_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				disableButtons();
				
				//No access points available from scan (or not scan done yet)
				if (wifiSample.size() ==0)
				{
						((MainActivity) getActivity()).getRSSIHandler().startWifiSampling();
				}
				else
				{
					//AP Data is available, update the prior with the next access point
					updateBeliefWithAP();
				}
			}
		});
	}

	/**
	 * Handler Sense new Scan
	 * 
	 * This button updates the current belief based on the movement (particle
	 * filtering), and then polls for the wifi access points currently there,
	 * overriding the old access point data
	 * 
	 */
	
	private void handleSenseNewScanButton() 
	{
		final Button button = (Button) rootView
				.findViewById(R.id.new_scan_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
			disableButtons();
				wifiSample.clear();
				
				//Start a new wifi scan
				((MainActivity) getActivity()).getRSSIHandler().startWifiSampling();
				
			}
	});
	}
	
	
	public void setSamplingResult(List<RssiFingerPrint> results)
	{
		//Update the counter in the ui to show how many access points have been found in total
		wifiSample = results;
		for(RssiFingerPrint print : results)
		{
			System.out.println(print.getIdentifier() + " " + print.getRssi());
		}
		totalAPSFound = results.size();

		//Update the prior for the first time based on the AP data
		updateBeliefWithAP();
	}

	private void updateBeliefWithAP()
	{
		enableButtons();
		
		TextView apLabel = (TextView) rootView
				.findViewById(R.id.APStatus);
		
		if (wifiSample.size() > 0)
		{			
			RssiAnalyzer analyzer = ((MainActivity) getActivity()).getRSSIAnalyzer();
			// Update belief with the first next AP in the list
			List<Tuple<Location, Double>> locationProbabilities = analyzer
					.getLocationProbabilitiesForFingerprintsUsingBayes(wifiSample.get(0));
			//Remove the access point added to the data from the list	
			wifiSample.remove(0);
			//Update Counter on access points used
			if (wifiSample.size() > 0)
			{
				DecimalFormat df = new DecimalFormat("#");
				apLabel.setText(totalAPSFound - wifiSample.size() + " / " + totalAPSFound + " / " + df.format(wifiSample.get(0).getRssi()));
			}
			else
			{
				apLabel.setText(totalAPSFound - wifiSample.size() + " / " + totalAPSFound + " /  - " );
			}
			//Update the UI to show the new beliefs
			DecimalFormat df = new DecimalFormat("#.##");
			for (int i = 0; i < Location.values().length; i++) {
				Tuple<Location, Double> probabilityTuple = locationProbabilities
						.get(i);
				System.out.println("probability for:" + probabilityTuple.key + " is " + probabilityTuple.value);
				TextView correspondingLabel = (TextView) rootView
						.findViewById(Constants
								.getIdForLocation(probabilityTuple.key));
				if (correspondingLabel != null) {
					correspondingLabel.setText(df
							.format(probabilityTuple.value));
				}
			}
		}
		else
		{
			apLabel.setText("0 / 0 / -");
		}
	}
	/**
	 * 
	 * @param results
	 */
/*	public void setSamplingResult(List<RssiFingerPrint> results) {
		if (results != null) {
			for (RssiFingerPrint print : results) {
				System.out.println("\t Input location: "
						+ print.getLocation() + " RSSI" + print.getRssi()
						+ "ssid: " + print.getIdentifier());
			}

			RssiAnalyzer analyzer = ((MainActivity) getActivity())
					.getRSSIAnalyzer();
			Location myLocation = analyzer.getLocationUsingBayes(results);
			List<Tuple<Location, Double>> locationProbabilities = analyzer
					.getLocationProbabilitiesForFingerprintsUsingBayes(results);

			for (Tuple<Location, Double> tuple : locationProbabilities) {
				System.out.println("\t\tLocation:" + tuple.key.toString()
						+ " Probability: " + tuple.value.toString());
			}
			DecimalFormat df = new DecimalFormat("#.##");
			for (int i = 0; i < Location.values().length; i++) {
				Tuple<Location, Double> probabilityTuple = locationProbabilities
						.get(i);
				TextView correspondingLabel = (TextView) rootView
						.findViewById(Constants
								.getIdForLocation(probabilityTuple.key));
				if (correspondingLabel != null) {
					correspondingLabel.setText(df
							.format(probabilityTuple.value));
				}
			}
			
			// Check if we want to resume scanning (threshold has been met for at least one Location)
			for (Tuple<Location, Double> probability : locationProbabilities) {
				if(keepScanning == true 
						&& probability.value >= threshold) {
					keepScanning = false;
				}
			}
			
			// If we want to resume scanning, do so
			if(keepScanning) {
				Log.v("LocationSensingFragment setSamplingResult()", "Performing another scan.");
				((MainActivity) getActivity()).getRSSIHandler().startWifiSampling();
			}
			else {
				Button button = (Button) rootView
					.findViewById(R.id.new_scan_button);
				button.setText(R.string.locationsensing_new_scan_button);
				enableButtons();
			}
		}
	}*/
	
    /**
     * Disables buttons in this fragment
     */
    public void disableButtons () {
    	buttonsEnabled = false;
		
		try {
			rootView.findViewById(R.id.initial_belief_button).setEnabled(false);
			rootView.findViewById(R.id.sense_new_AP_button).setEnabled(false);
			rootView.findViewById(R.id.new_scan_button).setEnabled(false);
		}
		catch (Exception e) {
			Log.i("LocationSensingFragment.disableButtons()","Could not disable buttons");
		}
    }
	
    /**
     * Enables buttons in this fragment
     */
    public void enableButtons () {
		buttonsEnabled = true;
		
		try {
			rootView.findViewById(R.id.initial_belief_button).setEnabled(true);
			rootView.findViewById(R.id.sense_new_AP_button).setEnabled(true);
			rootView.findViewById(R.id.new_scan_button).setEnabled(true);
		}
		catch (Exception e) {
			Log.i("initial_belief_button.disableButtons()","Could not enable buttons");
		}
    }
}
