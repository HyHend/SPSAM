package net.hyhend.spsam.Localization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.hyhend.spsam.TrainingDataFileIO;
import net.hyhend.spsam.UI.FingerPrintFragment;
import net.hyhend.spsam.UI.LocationSensingFragment;
import net.hyhend.spsam.UI.MainActivity;
import net.hyhend.spsam.comparators.FingerPrintComparator;
import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class RSSIHandler extends Activity {
	private WifiManager wifiManager;
	private boolean isActive;
	private long lastResultTimestamp;
	private BroadcastReceiver wifiBroadcastReceiver;
	private boolean isInTrainingMode;
	private Location currentLocation;
	private ArrayList<RssiFingerPrint> fingerprints;

	/**
	 * Constructor
	 */
	public RSSIHandler(ArrayList<RssiFingerPrint> fingerprints) {
		// analysis and thus does not write to file

		wifiManager = (WifiManager) MainActivity.getAppContext()
				.getSystemService(Context.WIFI_SERVICE);
		this.fingerprints = fingerprints;
		isActive = false;
		lastResultTimestamp = 0;

		// create broadcastReceiver
		wifiBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent intent) {
				Log.v("startWifiPolling", "Received WiFi data");
				List<ScanResult> results = wifiManager.getScanResults();
				ArrayList<RssiFingerPrint> fps = new ArrayList<RssiFingerPrint>();

				if (isInTrainingMode) {
					// For each found access point, add data
					for (int i = 0; i < results.size(); i++) {
						ScanResult ap = results.get(i);
						RssiFingerPrint fingerprint = new RssiFingerPrint(
								ap.BSSID, ap.level, currentLocation);
						fps.add(fingerprint);
						TrainingDataFileIO
								.addFingerprintTrainingDataToFile(fingerprint);
					}

					// Update UI
					FingerPrintFragment fragment = (FingerPrintFragment) MainActivity
							.getFingerPrintingFragment();
					fragment.updateWifiResults(fps);

					// Start next poll if enough time has elapsed (3 seconds)
					if (isActive
							&& lastResultTimestamp + 3000 < System
									.currentTimeMillis()) {
						wifiManager.startScan();
					} else {
						// Run after delay of 3000ms - elapsed time since last
						// result
						final Handler handler = new Handler();
						handler.postDelayed(
								new Runnable() {
									@Override
									public void run() {
										if (isActive) {
											wifiManager.startScan();
										}
									}
								},
								Math.max(
										0,
										(3000 - (System.currentTimeMillis() - lastResultTimestamp))));
					}

					// Remember timestamp
					lastResultTimestamp = System.currentTimeMillis();
				}
				else
				{
					//Not in training mode, thus in sampling mode
					// For each found access point, add data
					for (int i = 0; i < results.size(); i++) {
						ScanResult ap = results.get(i);
						RssiFingerPrint fingerprint = new RssiFingerPrint(
								ap.BSSID, ap.level, null);
						fps.add(fingerprint);						
					}
					LocationSensingFragment fragment = (LocationSensingFragment) MainActivity
							.getLocationSensingFragment();
					Collections.sort(fps, new FingerPrintComparator());
					fragment.setSamplingResult(fps);
				}
			}
		};
	}

	/**
	 * Returns last polled fingerprint
	 * 
	 * @return RssiFingerPrint
	 */
	public RssiFingerPrint getLatestFingerprint() {
		if (fingerprints.size() > 0) {
			return fingerprints.get(fingerprints.size() - 1);
		}
		return null;
	}

	public List<RssiFingerPrint> getLatestFingerprints(int amount) {
		if (fingerprints.size() > 0) {
			return fingerprints.subList(
					Math.max(0, (fingerprints.size() - amount - 400 - 1)),
					(fingerprints.size() - 400 - 1));
		}
		return null;
	}

	/**
	 * Returns all known fingerprints
	 * 
	 * @return ArrayList<RssiFingerPrint>
	 */
	public ArrayList<RssiFingerPrint> getFingerprints() {
		return this.fingerprints;
	}

	/**
	 * Stops the polling
	 */
	public void stopWifiPolling() {
		this.isActive = false;
		MainActivity.context.unregisterReceiver(wifiBroadcastReceiver);
	}

	/**
	 * Handles wifi polling
	 */
	public void startWifiSampling() {
		
		/*System.out.println(" Mocking mode enabled");
		ArrayList<RssiFingerPrint> fps = TrainingDataFileIO.getMockedWifiScanFromFile();
		LocationSensingFragment fragment = (LocationSensingFragment) MainActivity
				.getLocationSensingFragment();
		Collections.sort(fps, new FingerPrintComparator());
		fragment.setSamplingResult(fps);*/
		isInTrainingMode = false;
		this.isActive = true;
		wifiManager.startScan();
		MainActivity.context.registerReceiver(wifiBroadcastReceiver,
				new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

	}

	public void startWifiPollingForLocation(final Location location) {
		isInTrainingMode = true;
		this.isActive = true;
		this.currentLocation = location;
		wifiManager.startScan();
		MainActivity.context.registerReceiver(wifiBroadcastReceiver,
				new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	/**
	 * Returns rssihandler state
	 * 
	 * @return isActive
	 */
	public boolean getActive() {
		return this.isActive;
	}
}
