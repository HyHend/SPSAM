package net.hyhend.spsam.Localization;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.hyhend.spsam.TrainingDataFileIO;
import net.hyhend.spsam.Utils.Constants;


public class CurvePerLocation {
	private HashMap<Location, GaussCurve> curves;
	
	/**
	 * Constructor
	 */
	public CurvePerLocation() {
		curves = new HashMap<Location, GaussCurve>();
	}
	
	/**
	 * @param location
	 * @param curve
	 */
	public void addCurveToLocation(Location location, GaussCurve curve) {
		GaussCurve existingCurve = curves.get(location);
		if (existingCurve == null) {
			existingCurve = curve;
		}
		else {
			existingCurve.combine(curve);
		}
		curves.put(location, existingCurve);
	}
	
	/**
	 * Writes curves to file (testing purposes)
	 */
	public void writeToFile() {
		Iterator<Entry<Location, GaussCurve>> it = curves.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Location, GaussCurve> pairs = it.next();
			Location location = (Location) pairs.getKey();
			GaussCurve curve = (GaussCurve) pairs.getValue();
			TrainingDataFileIO.addGaussDataToFile(Constants.gauss_location_data_file_name, location, "-", curve);
		}
	}
}
