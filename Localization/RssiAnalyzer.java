package net.hyhend.spsam.Localization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.hyhend.spsam.Utils.Constants;
import net.hyhend.spsam.Utils.Tuple;
import net.hyhend.spsam.comparators.DistanceToLocationComparator;
import net.hyhend.spsam.comparators.LocationProbabilityComparator;

public class RssiAnalyzer {
	private ArrayList<RssiFingerPrint> fingerprints;
	private AccessPointTables accessPointTables;
	private HashMap<Location, Double> priorLocationProbabilities;
	public double InitialBelief;
	/**
	 * Constructor
	 * @param fingerprints
	 */
	public RssiAnalyzer() {
		this.fingerprints = new ArrayList<RssiFingerPrint>();
		this.accessPointTables = new AccessPointTables();
		InitialBelief = 0;
		// Set inital belief
		setToInitialBelief();
	}
	
	/**
	 * Set fingerprints. Also creates tables for bayesian analysis
	 * @param fingerprints
	 */
	public void setFingerPrints(ArrayList<RssiFingerPrint> fingerprints) {
		this.fingerprints = fingerprints;
		
		// First empty, then fill accessPointTables
		this.accessPointTables = new AccessPointTables();
		fillAccessPointTables(fingerprints);
	}
	
	/**
	 * Fills accesspointtables with given fingerprints
	 * @param fingerprints
	 */
	public void fillAccessPointTables (ArrayList<RssiFingerPrint> fingerprints) {
		// Add each fingerprint
		for(int i=0; i<fingerprints.size(); i++) {
			RssiFingerPrint fingerprint = fingerprints.get(i);
			
			AccessPointTable accessPointTable = accessPointTables.getTable(fingerprint.getIdentifier());
			if(accessPointTable == null) {
				// Table does not exist, create new
				accessPointTable = new AccessPointTable(fingerprint.getIdentifier());
				accessPointTables.setTable(fingerprint.getIdentifier(), accessPointTable);
			}
			
			// Add to table (if strength good enough)
			//if(fingerprint.getRssi() >= -75) {
				accessPointTable.addToHistogramForLocation(fingerprint.getLocation(), fingerprint.getRssi());
			//}
		}
		
		// Convert all known histograms to Gauss Curves
		accessPointTables.convertHistogramsInTables();
	}
	
	/**
	 * 
	 * @param fingerprints
	 * @return sortedPoints
	 */
	public List<Tuple<Location, Double>> getLocationProbabilitiesForFingerprintsUsingBayes(RssiFingerPrint fingerPrint) 
	{
		 Tuple<String, Integer> apRssi =new Tuple<String, Integer>(fingerPrint.getIdentifier(), (int)fingerPrint.getRssi());
		
		
		// Calculate probabilities
		HashMap<Location, Double> probabilities = accessPointTables.calculateProbabilities(apRssi, priorLocationProbabilities);

		List<Tuple<Location, Double>> sortedPoints = new ArrayList<Tuple<Location, Double>>();
		Iterator<Entry<Location, Double>> it = probabilities.entrySet().iterator();
	    while (it.hasNext()) {
	        Entry<Location, Double> pairs = it.next();
	        Location key = pairs.getKey();
	        double value = pairs.getValue();	        
	        sortedPoints.add(new Tuple<Location, Double>(key, value));	        
	    }
		Collections.sort(sortedPoints, new LocationProbabilityComparator());
	    return sortedPoints;
	}

	/**
	 * Uses KNN to find the location for the given fingerprint
	 * @param fingerprint
	 * @return Location
	 */
	public Location getLocationUsingKNN(RssiFingerPrint fingerprint) {
		ArrayList<Tuple<Double, Location>> distancesToLocations = new ArrayList<Tuple<Double,Location>>();
		
		// Calculate all distances
		for(int i=0; i<fingerprints.size(); i++) {
			RssiFingerPrint compareFingerPrint = fingerprints.get(i);
			Tuple<Double, Location> distance = new Tuple<Double, Location>(
					fingerprint.compare(compareFingerPrint),
					compareFingerPrint.getLocation()
				);
			distancesToLocations.add(distance);
		}
		
		// Retrieve k closest distances
		Collections.sort(distancesToLocations, new DistanceToLocationComparator());
		ArrayList<Tuple<Double, Location>> closestLocations = new ArrayList<Tuple<Double, Location>>();
		for(int i=0; i<Math.min(Constants.knnNValueRssi, distancesToLocations.size()); i++) {
			closestLocations.add(distancesToLocations.get(i));
		}
		
		// Get location which is most in closest distances
		HashMap<Location, Integer> counts = new HashMap<Location, Integer>();
		for(int i=0; i<closestLocations.size(); i++) {
			Location location = closestLocations.get(i).value;
			if(counts.containsKey(location)) {
				counts.put(location, (counts.get(location)+1));
			}
			else {
				counts.put(location, 1);
			}
		}
		
		// Retrieve best location from counts (hashmap)
		Iterator<Entry<Location, Integer>> it = counts.entrySet().iterator();
		int highest = 0;
		Location best = null;
	    while (it.hasNext()) {
	        Entry<Location, Integer> pairs = it.next();
	        if((Integer) pairs.getValue() > highest) {
	        	best = (Location) pairs.getKey();
	        }
	    }
		
		return best;
	}
	
	/**
	 * Sets initial belief. Equal probability for each location
	 */
	public void setToInitialBelief () {
		// Set initial probability for each available location
		this.priorLocationProbabilities = new HashMap<Location, Double>();
		Double initialProbability = 1.0 / Location.values().length;
		InitialBelief = initialProbability;
		for (Location loc : Location.values()) {
			this.priorLocationProbabilities.put(loc, initialProbability);
		}
	}
}
