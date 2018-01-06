package net.hyhend.spsam.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FeatureVector {
	public ArrayList<Double> fundamentalFrequencies;
	public double averageAcceleration;
	public double maxAmpl;
	public double minAmpl;
	public double avgMinAzimuth;
	public double avgMaxAzimuth;

	public FeatureVector(ArrayList<Double> fundamentalFrequencies, double averageAcceleration, double maxAmpl, double minAmpl, double avgMinAzimuth, double avgMaxAzimuth) {
		// Sort fundamental frequencies
		Collections.sort(fundamentalFrequencies, new Comparator<Double>() {
			@Override
			public int compare(Double arg0, Double arg1) {
		        return new Double(arg0).compareTo(arg1);
			}
	    });
		
		// Fill FeatureVector
		this.fundamentalFrequencies = fundamentalFrequencies;
		this.averageAcceleration = averageAcceleration;
		this.maxAmpl = maxAmpl;
		this.minAmpl = minAmpl;
		this.avgMinAzimuth = avgMinAzimuth;
		this.avgMaxAzimuth = avgMaxAzimuth;
		
		//System.out.println("FeatureVector (fft 0, fft 1, fft 2, avgacc, maxacc, minacc): " + fundamentalFrequencies.get(0) + " - " + fundamentalFrequencies.get(1) +  " - " + fundamentalFrequencies.get(2) + " - " + averageAcceleration + " - " + maxAmpl + " - " + minAmpl);
	}
	
	/**
	 * Compares given FeatureVector to this FeatureVector
	 * Returns double indicating the distance (Euclidean) between these two vectors 
	 * @param compare
	 * @return double
	 */
	public double getDistance(FeatureVector compare) {
		double frequenciesDifferences = 0;
		
		// Calculate differences
		double avgAccellerationDifference = Math.pow(this.averageAcceleration - compare.averageAcceleration, 2); 
		double minAmplDifference = Math.pow(this.minAmpl - compare.minAmpl, 2); 
		double maxAmplDifference = Math.pow(this.maxAmpl - compare.maxAmpl, 2);
		
		// Calculate frequenciesDifference (use length of shortest list in this or compare)
		double maxSize = this.fundamentalFrequencies.size();
		if(this.fundamentalFrequencies.size() > compare.fundamentalFrequencies.size()) {
			maxSize = compare.fundamentalFrequencies.size();
		}
		for(int i=0; i<maxSize; i++) {
			frequenciesDifferences += Math.pow(this.fundamentalFrequencies.get(i) - compare.fundamentalFrequencies.get(i), 2);
		}
		
		return Math.sqrt(
				(frequenciesDifferences * Constants.fft_per_frequency_impact)
				+ (avgAccellerationDifference * Constants.avg_acceleration_impact)
				+ (minAmplDifference * Constants.min_acceleration_impact)
				+ (maxAmplDifference * Constants.max_acceleration_impact));
	}
}
