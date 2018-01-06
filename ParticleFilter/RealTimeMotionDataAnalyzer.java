package net.hyhend.spsam.ParticleFilter;

import java.util.ArrayList;
import java.util.Collections;

import net.hyhend.spsam.FFT;
import net.hyhend.spsam.MotionDataPoint;
import net.hyhend.spsam.Utils.Constants;
import net.hyhend.spsam.Utils.FeatureVector;
import net.hyhend.spsam.Utils.Tuple;
import net.hyhend.spsam.comparators.FrequencyTupleComparator;

public class RealTimeMotionDataAnalyzer {
	private static FFT FastFourier = new FFT(Constants.realtimeWindowSize);
	
	/**
	 * Constructor
	 */
	public RealTimeMotionDataAnalyzer() {
	}

	/**
	 * Calculates velocity per window. Returns window start time and velocity per window
	 * @param double[] points
	 * @return ArrayList<Tuple<Long, Double>> (velocity in m/s)
	 */
	public ArrayList<Tuple<Long, MovementValues>> getMovementForPoints(MotionDataPoint[] points) {
		ArrayList<Tuple<Long, MovementValues>> movementPerWindow = new ArrayList<Tuple<Long, MovementValues>>();
		ArrayList<FeatureVector> vectorsForPoints = getFeatureVectorsForPoints(points);

		// Determine the length of one sample (milliseconds)
		int sampleLength = Constants.realtimeWindowSize * Constants.sample_rate;

		// Analyse vectors, calculate estimated velocity for given vector
		for (int i=0; i<vectorsForPoints.size(); i++) {
			FeatureVector vector = vectorsForPoints.get(i);
			
			// Calculate start
			long start = i*sampleLength;
			
			// Calculate velocity
			double baseFrequency = vector.fundamentalFrequencies.get(0);
			double minAccelleration = vector.minAmpl;
			double maxAccelleration = vector.maxAmpl;
			
			double velocity = 0;
			
			if((maxAccelleration - minAccelleration) < 8 || baseFrequency >= 6) {	
				velocity = 0;				// m/s
				System.out.println("Not moving");
			}
			else {		
				double stepSize = Constants.stepSize; 			
				double frequencyPenalty = 0.99; 	// Higher frequencies could equal shorter steps.
				double steps = baseFrequency * (sampleLength/1000);
				double distance = steps * stepSize * Math.pow(frequencyPenalty,baseFrequency);
				velocity = (distance/100) / (sampleLength/1000);		// m/s
				System.out.println(velocity+" m/s");
			}
				
			// Add to output
			movementPerWindow.add(new Tuple<Long, MovementValues>(start, new MovementValues(velocity, vector.avgMinAzimuth, vector.avgMaxAzimuth)));
		}		

		// Output: one velocity per window. Will be translated to meters in calling function
		return movementPerWindow;
	}

	/**
	 * This method retrieves the feature vectors for a set of points
	 * METHOD NOT EQUAL TO IDENTICALLY NAMED IN MOTIONDATAANALYZER
	 * @param points (Any size set of points will do, elements on the end will, if not fitting, be discarded)
	 * @return ArrayList<FeatureVector>
	 */
	private ArrayList<FeatureVector> getFeatureVectorsForPoints(MotionDataPoint[] points) {
		ArrayList<FeatureVector> resultList = new ArrayList<FeatureVector>();
		// Calculate number of windows to be extracted from sample
		int amountOfWindows = Math.max(0,(int) Math.floor(points.length / Constants.realtimeWindowSize));

		// Init current index
		int currentIndex = 0;

		// Calculate the individual feature vectors for each window and put them
		// in the list to return
		for (int i = 0; i < amountOfWindows; i++) {
			// Create a window data container
			MotionDataPoint[] window = new MotionDataPoint[Constants.realtimeWindowSize];
			for (int j = 0; j < Constants.realtimeWindowSize; j++) {
				window[j] = points[currentIndex + j];
			}
			// Get the feature vector for this window
			FeatureVector vectorForWindow = GetVectorForWindow(window);

			// Add this vector to the results
			resultList.add(vectorForWindow);

			// And increase the index to the next set
			currentIndex += Constants.realtimeWindowSize;
		}
		return resultList;
	}

	/**
	 * This method extracts the feature vector from a window of points
	 * @param points
	 * @return FeatureVector
	 */
	public static FeatureVector GetVectorForWindow(MotionDataPoint[] points) {
		if (points.length != Constants.realtimeWindowSize) {
			throw new RuntimeException("MotionDataAnalyzer: given amount of points to calculate with is invalid ");
		}

		double maxAmpl = 0;
		double minAmpl = Double.MAX_VALUE;
		double avgMinAzimuth = -Math.PI;
		double avgMaxAzimuth = Math.PI;	
		double totalAcceleration = 0;

		// Calculate min and max ampl, and average acceleration
		for (int i = 0; i < points.length; i++) {
			double currentPointValue = points[i].getValue();

			totalAcceleration += currentPointValue;

			if (maxAmpl < currentPointValue) {
				maxAmpl = currentPointValue;
			}
			if (minAmpl > currentPointValue) {
				minAmpl = currentPointValue;
			}
		}
		double averageAcceleration = totalAcceleration / points.length;
		
		// Calculate min and max azimuth
		double minAzimuth = Math.PI;
		double maxAzimuth = -Math.PI;
		for (int i = 0; i < points.length; i++) {
			double currentPointAzimuth = points[i].getAzimuth();
			
			if (maxAzimuth < currentPointAzimuth) {
				maxAzimuth = currentPointAzimuth;
			}
			if (minAzimuth > currentPointAzimuth) {
				minAzimuth = currentPointAzimuth;
			}
		}
		
		// Calculate avgMin and avg max azimuth
		double avgMinAzimuthCount = 0;
		double avgMaxAzimuthCount = 0;
		double similarAzimuth = 0.3*Math.PI;
		for (int i = 0; i < points.length; i++) {
			double currentPointAzimuth = points[i].getAzimuth();
			
			if (currentPointAzimuth > (maxAzimuth - similarAzimuth)) {
				avgMaxAzimuth += currentPointAzimuth;
				avgMaxAzimuthCount++;
			}
			if (currentPointAzimuth < (minAzimuth + similarAzimuth)) {
				avgMinAzimuth += currentPointAzimuth;
				avgMinAzimuthCount++;
			}
		}
		avgMinAzimuth = avgMinAzimuth / avgMinAzimuthCount;
		avgMaxAzimuth = avgMaxAzimuth / avgMaxAzimuthCount;
		
		// Put all MotionDataPoints values into double[]
		double[] values = new double[points.length];
		for(int j=0; j<points.length; j++) {
			values[j] = points[j].getValue();
		}
		
		/*
		//
		// Get x, y, z values
		double[] xValues = new double[points.length];
		double[] yValues = new double[points.length];
		double[] zValues = new double[points.length];
		for(int j=0; j<points.length; j++) {
			xValues[j] = points[j].getX();
			yValues[j] = points[j].getY();
			zValues[j] = points[j].getZ();
		}
		// Do FFT on x, y, z
		ArrayList<Tuple<Integer, Double>> xFreq = FastFourier.getFrequencies(xValues);
		Collections.sort(xFreq, new FrequencyTupleComparator());
		ArrayList<Tuple<Integer, Double>> yFreq = FastFourier.getFrequencies(yValues);
		Collections.sort(yFreq, new FrequencyTupleComparator());
		ArrayList<Tuple<Integer, Double>> zFreq = FastFourier.getFrequencies(zValues);
		Collections.sort(zFreq, new FrequencyTupleComparator());
		
		int last = xFreq.size()-2;
		//System.out.println("X[0,1], Y[0,1], Z[0,1]: ["+xFreq.get(last).key+", "+xFreq.get(last-1).key+"],["+yFreq.get(last).key+", "+yFreq.get(last-1).key+"],["+zFreq.get(last).key+", "+zFreq.get(last-1).key+"]");
		boolean xPerp = false;
		if(xFreq.get(last).key != 0) {
			xPerp = xFreq.get(last-1).key / xFreq.get(last).key == 2;
		}
		boolean yPerp = false;
		if(yFreq.get(last).key != 0) {
			yPerp = yFreq.get(last-1).key / yFreq.get(last).key == 2;
		}
		boolean zPerp = false;
		if(zFreq.get(last).key != 0) {
			zPerp = zFreq.get(last-1).key / zFreq.get(last).key == 2;
		}
		
		if(!xPerp && !yPerp && !zPerp) {
			System.out.println("None perpendicular");
		}
		else {
			System.out.println("x, y, z: "+xPerp+", "+yPerp+", "+zPerp);
		}
		// END
		 */

		// Do the FFT after analysis of the data because it is pass by reference
		// and not pass by value and FFT adjusts the values!
		ArrayList<Tuple<Integer, Double>> frequencies = FastFourier.getFrequencies(values);
		Collections.sort(frequencies, new FrequencyTupleComparator());
		
		// Create an offset of 1 to prevent frequency 0 from popping up in the results
		int start = Math.max(0, frequencies.size()
				- Constants.max_Relevant_Frequencies - 1);
		ArrayList<Double> fundamentalFrequencies = new ArrayList<Double>();

		for (int i = start; i < frequencies.size(); i++) {
			if ((double) frequencies.get(i).key != 0) {
				fundamentalFrequencies.add((double) frequencies.get(i).key);
			}
		}
		
		// Return feature vector
		return new FeatureVector(fundamentalFrequencies, averageAcceleration,
				maxAmpl, minAmpl, avgMinAzimuth, avgMaxAzimuth);
	}
}
