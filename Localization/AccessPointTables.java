package net.hyhend.spsam.Localization;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.util.Log;
import net.hyhend.spsam.Utils.Tuple;


public class AccessPointTables {
	private HashMap<String, AccessPointTable> accessPointTables;

	/**
	 * Constructor, no initial table
	 */
	public AccessPointTables() {
		accessPointTables = new HashMap<String, AccessPointTable>();
	}

	/**
	 * Constructor, with initial table
	 */
	public AccessPointTables(HashMap<String, AccessPointTable> accessPointTables) {
		this.accessPointTables = accessPointTables;
	}

	/**
	 * returns accessPointTables
	 * 
	 * @return HashMap<accessPointTable>
	 */
	public HashMap<String, AccessPointTable> getAccessPointTables() {
		return this.accessPointTables;
	}

	/**
	 * Sets table in accessPointTables
	 * 
	 * @param accessPointTable
	 */
	public void setTable(String accessPoint, AccessPointTable accessPointTable) {
		this.accessPointTables.put(accessPoint, accessPointTable);
	}

	/**
	 * Retrieves given table from accessPointTables
	 * 
	 * @param accessPoint
	 */
	public AccessPointTable getTable(String accessPoint) {
		return this.accessPointTables.get(accessPoint);
	}
	
	/**
	 * Calculates and writes GaussCurve sum per location
	 */
	public void calculateAndWriteGaussCurveSumPerLocation () {
		CurvePerLocation curvePerLocation = new CurvePerLocation();
		
		// Loop through all ap tables
		// For each table get curves for each location, add these to curvePerLocation
		Iterator<Entry<String, AccessPointTable>> it = accessPointTables.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, AccessPointTable> pairs = it.next();
			AccessPointTable table = pairs.getValue();
			
			for (Location loc : Location.values()) {
				if(table.getForLocation(loc) != null) {
					curvePerLocation.addCurveToLocation(loc, table.getForLocation(loc));
				}
			}
		}
		
		// Write to file (testing purposes)
		//curvePerLocation.writeToFile();
	}

	/**
	 * Converts all histograms in all known tables
	 */
	public void convertHistogramsInTables() {
		Iterator<Entry<String, AccessPointTable>> it = accessPointTables.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, AccessPointTable> pairs = it.next();
			AccessPointTable table = (AccessPointTable) pairs.getValue();
			table.convertHistogramsToCurves();
		}
	}

	/**
	 * Calculates the probability of each location given a list of access points
	 * and their respective strengths (probability I am in cell i given that I
	 * got an RSS measurement r from access point j)
	 * 
	 * @param apsRssi
	 * @return List<Tuple<Location, Double>>
	 */
	public HashMap<Location, Double> calculateProbabilities(
			Tuple<String, Integer> apRssi,
			HashMap<Location, Double> prior) {
		Log.v("CalculateProbabilities","apRssi: "+apRssi.key+" - "+apRssi.value);
		// Normalize locationsProbability
		Iterator<Entry<Location, Double>> itt = prior.entrySet().iterator();
		while (itt.hasNext()) {
			Entry<Location, Double> pairs = itt.next();
			Location key = pairs.getKey();
			Double value = pairs.getValue();
			Log.v("CalculateProbabilities","Prior: "+key+" - "+value);
		}
		
		// probabilities (You cannot teleport. You cannot walk through walls.
		// You've set five steps. Etc..)
		
		// Use prior as initial probability
		HashMap<Location, Double> locationsProbability = prior;

		// For each AP in view (until enough certainty has been reached)

		String ap = apRssi.key;
		int rssi = apRssi.value;

		// Get table for current access point
		AccessPointTable currentAccessPoint = accessPointTables.get(ap);
		if (currentAccessPoint != null) {
			// For each location calculate probability of Location given
			// RSSI of AP
			// Also remember sum of all values (for normalizing)
			Double sum = 0.0;
			for (Location loc : Location.values()) {
				Double probability = locationsProbability.get(loc);
				GaussCurve curve = currentAccessPoint.getForLocation(loc);
				Double apRssiForLocationProbability = 0.0;
				if (curve != null) {
					apRssiForLocationProbability = curve.getHeightAt(rssi);
				}
				probability = probability * apRssiForLocationProbability;
				locationsProbability.put(loc, probability);
				sum = sum + probability;
			}

			// Normalize locationsProbability
			Iterator<Entry<Location, Double>> it = locationsProbability.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Location, Double> pairs = it.next();
				Location key = pairs.getKey();
				Double value = pairs.getValue();
				if (sum == 0) {
					// there were no Locations with any propability so
					// return the prior value.
					value = prior.get(key);				
				} else {
					value = value / sum; // Normalize
				}
				locationsProbability.put(key, value);
			}
		}
		

		return locationsProbability;
	}
}
