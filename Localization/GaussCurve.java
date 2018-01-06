package net.hyhend.spsam.Localization;

import java.util.ArrayList;
import java.util.List;

public class GaussCurve {
	private double mean;
	private double standardDeviation;
	
	/**
	 * Constructor using mean and standard deviation
	 * @param mean
	 * @param standardDeviation
	 */
	public GaussCurve(double mean, double standardDeviation) {
		this.mean = mean;
		this.standardDeviation = standardDeviation;
	}
	
	/**
	 * Constructor using histogram. Calculates Gaussian curve
	 * @param histogram
	 */
	public GaussCurve(List<Double> histogram) {
		// Find mean
		this.mean = calculateAverage(histogram);
		
		// Find deviation
		double variance = calculateVariance(this.mean, histogram);
		this.standardDeviation = Math.sqrt(variance);
	
	}
	
	/**
	 * Combines given curve and this into this
	 * @param curve
	 */
	public void combine(GaussCurve curve) {
		// Set mean
		this.mean = (this.mean + curve.getMean()) / 2;
		
		// Set standard deviation
		double variance = Math.pow(this.standardDeviation, 2) + Math.pow(curve.getStandardDeviation(), 2);
		this.standardDeviation = Math.sqrt(variance);
	}
	
	/**
	 * @return mean
	 */
	public double getMean() {
		return this.mean;
	}
	
	/**
	 * @return standardDeviation
	 */
	public double getStandardDeviation() {
		return this.standardDeviation;
	}
	
	/**
	 * Returns the value of the distribution at point x
	 * @param x
	 * @return double
	 */
	public double getHeightAt(double x) {
		// Fix when standard deviation is 0. 
		if(this.standardDeviation == 0) {
			this.standardDeviation = 0.001;
		}

		double elem = 1 / (this.standardDeviation * Math.sqrt(2 * Math.PI));
		double exp = -(Math.pow((x - this.mean), 2) / (2 * Math.pow(this.standardDeviation, 2)));
		double y = elem * Math.pow(Math.E, exp);
		
		System.out.println("X: " + x + ", Y: "+ y + ", Mean: "+ this.mean + ", STDev: "+this.standardDeviation);
		
		return y;
	}
	
	/**
	 * Calculates average
	 * @param values
	 * @return double average
	 */
	private double calculateAverage(List<Double> values) {
		double sum = 0;
		
		if(!values.isEmpty()) {
			for (Double value : values) {
				sum += value;
			}
			return sum / values.size();
		}
		
		return sum;
	}
	
	/**
	 * Calculates variance
	 * @param mean
	 * @param values
	 * @return double variance
	 */
	private double calculateVariance(double mean, List<Double> values) {
		ArrayList<Double> squaredDifferences = new ArrayList<Double>();
		
		if(!values.isEmpty()) {
			// Variance: The average of the squared differences from the mean
			for (Double value : values) {
				double squaredDifference = Math.pow((value - mean), 2);
				squaredDifferences.add(squaredDifference);
			}
			return calculateAverage(squaredDifferences);
		}
		
		return 0;
	}
}
