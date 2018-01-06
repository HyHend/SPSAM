package net.hyhend.spsam.Localization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class AccessPointTable {
	private String accessPoint;
	private HashMap<Location, GaussCurve> gaussCurves;
	private HashMap<Location, ArrayList<Double>> histograms;
	
	/**
	 * Constructor
	 */
	public AccessPointTable(String accessPoint) {
		gaussCurves = new HashMap<Location, GaussCurve>();
		histograms = new HashMap<Location, ArrayList<Double>>();
		this.setAccessPoint(accessPoint);
	}
	
	/**
	 * @param location
	 * @return GaussCurve
	 */
	public GaussCurve getForLocation(Location location) {
		return gaussCurves.get(location);
	}
	
	/**
	 * Sets curve for given location by using its histogram
	 * @param location
	 * @param histogram
	 */
	public void setForLocation(Location location, List<Double> histogram) {
		//GaussCurve curve = new GaussCurve(normalizeHistogram(histogram));	// Create curve from normalized histogram
		GaussCurve curve = new GaussCurve(histogram);	// Create curve from histogram
		gaussCurves.put(location, curve);
		
		// Add gauss curve to file testing purposes
		//TrainingDataFileIO.addGaussDataToFile(Constants.gauss_data_file_name, location, accessPoint, curve);
	}
	
	/**
	 * Sets curve for given location
	 * @param location
	 * @param curve
	 */
	public void setForLocation(Location location, GaussCurve curve) {
		gaussCurves.put(location, curve);
		
		// Add gauss curve to file (testing purposes)
		// TrainingDataFileIO.addGaussDataToFile(Constants.gauss_data_file_name, location, accessPoint, curve);
	}

	/**
	 * Adds to curve of given location by using its histogram
	 * @param location
	 * @param curve
	 */
	public void addToLocation(Location location, List<Double> histogram) {
		//GaussCurve curve = new GaussCurve(normalizeHistogram(histogram));	// Create curve from normalized histogram
		GaussCurve curve = new GaussCurve(histogram);	// Create curve from histogram
		addToLocation(location, curve);
		
		// Add gauss curve to file (testing purposes)
		//TrainingDataFileIO.addGaussDataToFile(Constants.gauss_data_file_name, location, accessPoint, curve);
	}
	
	/**
	 * Adds to curve of given location
	 * @param location
	 * @param curve
	 */
	public void addToLocation(Location location, GaussCurve curve) {
		GaussCurve existingCurve = gaussCurves.get(location);
		if(existingCurve != null) {
			// Add to existing curve
			existingCurve.combine(curve);
			gaussCurves.put(location, existingCurve);		
		}
		else {
			// Add new curve
			gaussCurves.put(location, curve);
		}
	}
	
	/**
	 * Adds value to histogram of location sets new location if not existent
	 * @param location
	 * @param value
	 */
	public void addToHistogramForLocation(Location location, double value) {
		ArrayList<Double> existing = histograms.get(location);
		if(existing != null) {
			// Add to existing
			existing.add(value);
			histograms.put(location, existing);
		}
		else {
			// Add new 
			ArrayList<Double> forLocation = new ArrayList<Double>();
			forLocation.add(value);
			histograms.put(location, forLocation);
		}
	}
	
	/**
	 * Converts all known histograms to Gauss Curves
	 * Removes all known Gauss Curves
	 */
	public void convertHistogramsToCurves() {
		// Re init gaussCurves
		gaussCurves = new HashMap<Location, GaussCurve>();
		
		// Add each histogram
		Iterator<Entry<Location, ArrayList<Double>>> it = this.histograms.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Location, ArrayList<Double>> pairs = it.next();

			// Get location and histogram. Set gaussCurve for this location 
			Location location = (Location) pairs.getKey();
			List<Double> histogram = (List<Double>) pairs.getValue();
			setForLocation(location, histogram);
		}
	}
	
	public String getAccessPoint() {
		return accessPoint;
	}

	public void setAccessPoint(String accessPoint) {
		this.accessPoint = accessPoint;
	}
}
