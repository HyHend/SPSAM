package net.hyhend.spsam;

import java.util.ArrayList;
import java.util.List;

import net.hyhend.spsam.Utils.Constants;

public class MergedMotionHistory {
	private List<MotionDataPoint> rawPoints;

	/**
	 * Constructor
	 */
	public MergedMotionHistory()
	{
		rawPoints = new ArrayList<MotionDataPoint>();
	}
	
	/**
	 * Adds data point to Motion History
	 * @param p
	 */
	public void addDataPoint(MotionDataPoint p)
	{
		// Verify and update last added point
		verifyAndAlterLastAddedPoint(p);
		
		// Check if intermediate points have to added
		// Calculate and add these points (interpolate)
		interpolateIntermediatePoints(p);

		// Add new point
		rawPoints.add(p);	
	}
	
	/**
	 * Returns size of rawPoints
	 * @return long
	 */
	public long getRawPointsSize() {
		return rawPoints.size();
	}
	
	/**
	 * Returns size of rawPoints adjusted with run on and off of running average filter
	 * @return long
	 */
	public long getRawPointsSizeAdjusted() {
		int filterLength = (int) Constants.polling_average_samples + 1;
		int filterRuns = 3;
		return rawPoints.size() - (2 * (filterLength * filterRuns));
	}
	
	/**
	 * Removes num samples from beginning of list
	 * @param num
	 */
	public boolean removeSamplesFromStart(int num) {
		for(int i=0; i<num; i++) {
			if(rawPoints.size() > 0) {
				rawPoints.remove(0);
			}
			else {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Get the time distributed datapoints that are average and rounded
	 * @return MotionDataPoint[]
	 */
	public MotionDataPoint[] getTimeDistributedPoints()
	{
		// Take the rawPoints, do the averaging and rounding and put it into an double array
		MotionDataPoint[] points = new MotionDataPoint[rawPoints.size()];
		
		// Calculate sliding average over list (three times)
		List<MotionDataPoint> averagedPoints = performSlidingAverage(rawPoints);
		averagedPoints = performSlidingAverage(averagedPoints);
		averagedPoints = performSlidingAverage(averagedPoints);
		
		// Enter all points into array
		for(int i=0; i<averagedPoints.size(); i++) {
			points[i] = averagedPoints.get(i);
		}
		
		return points;
	}
	
	/**
	 * Get the time distributed datapoints that are average and rounded
	 * Clipped to remove sliding average run on and off
	 * @return MotionDataPoint[] 
	 */
	public MotionDataPoint[] getTimeDistributedPointsClipped()
	{
		// Calculate amount to discard because of filtering
		int filterLength = (int) Constants.polling_average_samples + 1;
		int filterRuns = 3;
		int discardAmount = filterLength * filterRuns;
				
		// Take the rawPoints, do the averaging and rounding and put it into an double array
		MotionDataPoint[] points = new MotionDataPoint[rawPoints.size()-(2*discardAmount)];
		
		// Calculate sliding average over list (three times)
		List<MotionDataPoint> averagedPoints = performSlidingAverage(rawPoints);
		averagedPoints = performSlidingAverage(averagedPoints);
		averagedPoints = performSlidingAverage(averagedPoints);
		
		// Enter all points into array
		for(int i=discardAmount; i<averagedPoints.size()-discardAmount; i++) {
			points[i] = averagedPoints.get(i);
		}
		
		return points;
	}
	
	/**
	 * Verifies last point in List.
	 * Results in last point to be linearly mapped to expected time (position in ArrayList * sample_rate)
	 * Expects new point to be able to linearly map when last point has been sampled before expected time
	 * @param newPoint
	 */
	private void verifyAndAlterLastAddedPoint(MotionDataPoint newPoint) {
		// We can only do this when there is at least one point in the list
		if(rawPoints.size() >= 1) {
			MotionDataPoint lastAddedPoint = rawPoints.get(rawPoints.size()-1);
			//long lastAddedDesiredTime = (rawPoints.size() * Constants.sample_rate) + rawPoints.get(0).getTime();
			long lastAddedDesiredTime = ((Math.round(lastAddedPoint.getTime() - rawPoints.get(0).getTime()) / Constants.sample_rate) * Constants.sample_rate) + rawPoints.get(0).getTime();
			if((lastAddedPoint.getTime() - Constants.allowed_deviation) > lastAddedDesiredTime
					&& (lastAddedPoint.getTime() + Constants.allowed_deviation) > lastAddedDesiredTime) {
				// Point is not mapped to the sample rate, linearly calculate new value using current point
				if(lastAddedPoint.getTime() > lastAddedDesiredTime) {
					// We can only do this when there is a predecessor in the list
					// Else, nothing is being done
					if(rawPoints.size() >= 2) {
						// Point has been polled later than desired
						// Calculate new position with point before (i-1)
						MotionDataPoint before = rawPoints.get(rawPoints.size()-2);
						long timeRange = lastAddedPoint.getTime() - before.getTime();
						double valueRange = lastAddedPoint.getValue() - before.getValue();
						double xRange = lastAddedPoint.getX() - before.getValue();
						double yRange = lastAddedPoint.getY() - before.getValue();
						double zRange = lastAddedPoint.getZ() - before.getValue();
						double azimuthRange = lastAddedPoint.getAzimuth() - before.getAzimuth();
						double timeDifference = lastAddedDesiredTime - lastAddedPoint.getTime();
						
						long newTime = lastAddedDesiredTime;
						double newValue = lastAddedPoint.getValue() - (timeDifference * (valueRange / timeRange));
						double newX = lastAddedPoint.getX() - (timeDifference * (xRange / timeRange));
						double newY = lastAddedPoint.getY() - (timeDifference * (yRange / timeRange));
						double newZ = lastAddedPoint.getZ() - (timeDifference * (zRange / timeRange));
						double newAzimuth = lastAddedPoint.getAzimuth() - (timeDifference * (azimuthRange / timeRange));	

						// Actually set new time and value
						rawPoints.set(rawPoints.size()-1, new MotionDataPoint(newTime, newValue, newX, newY, newZ, newAzimuth));
					}
				}
				else {
					// Point has been polled before desired
					// Calculate new position with point after (i+1)
					MotionDataPoint after = newPoint;
					long timeRange = after.getTime() - lastAddedPoint.getTime();
					double valueRange = after.getValue() - lastAddedPoint.getValue();
					double xRange = after.getX() - lastAddedPoint.getX();
					double yRange = after.getY() - lastAddedPoint.getZ();
					double zRange = after.getZ() - lastAddedPoint.getZ();
					double azimuthRange = after.getAzimuth() - lastAddedPoint.getAzimuth();
					double timeDifference = lastAddedPoint.getTime() - lastAddedDesiredTime;
					
					long newTime = lastAddedDesiredTime;
					double newValue = lastAddedPoint.getValue() + (timeDifference * (valueRange / timeRange));
					double newX = lastAddedPoint.getX() + (timeDifference * (xRange / timeRange));
					double newY = lastAddedPoint.getY() + (timeDifference * (yRange / timeRange));
					double newZ = lastAddedPoint.getZ() + (timeDifference * (zRange / timeRange));
					double newAzimuth = lastAddedPoint.getAzimuth() + (timeDifference * (azimuthRange / timeRange));
	
					// Actually set new time and value
					rawPoints.set(rawPoints.size()-1, new MotionDataPoint(newTime, newValue, newX, newY, newZ, newAzimuth));
				}
			}
			else {
				// Point is deviating less than the threshold, do nothing
			}
		}
	}
	
	/**
	 * Checks if intermediate points have to added
	 * Calculate and add these points (interpolate, linear)
	 * @param newPoint
	 */
	private void interpolateIntermediatePoints (MotionDataPoint newPoint) {
		// We can only do this when there is at least one point in the list
		// And we only have to do it when the distance between the points it too large
		if(rawPoints.size() >= 1
				&& ((newPoint.getTime() - rawPoints.get(rawPoints.size()-1).getTime()) * 0.8) > Constants.sample_rate) {
			MotionDataPoint lastAddedPoint = rawPoints.get(rawPoints.size()-1);
			long timeRange = newPoint.getTime() - lastAddedPoint.getTime();
			double valueRange = newPoint.getValue() - lastAddedPoint.getValue();
			double xRange = newPoint.getX() - lastAddedPoint.getX();
			double yRange = newPoint.getY() - lastAddedPoint.getZ();
			double zRange = newPoint.getZ() - lastAddedPoint.getZ();
			double azimuthRange = newPoint.getAzimuth() - lastAddedPoint.getAzimuth();
			
			// While intermediate points have to be calculated, calculate and add
			while(((newPoint.getTime() - rawPoints.get(rawPoints.size()-1).getTime()) * 0.8) > Constants.sample_rate) {
				long newTime = rawPoints.get(rawPoints.size()-1).getTime() + Constants.sample_rate;
				double timeDifference = newTime - lastAddedPoint.getTime();
				double newValue = lastAddedPoint.getValue() + (timeDifference * (valueRange / timeRange));
				double newX = lastAddedPoint.getX() + (timeDifference * (xRange / timeRange));
				double newY = lastAddedPoint.getY() + (timeDifference * (yRange / timeRange));
				double newZ = lastAddedPoint.getZ() + (timeDifference * (zRange / timeRange));
				double newAzimuth = lastAddedPoint.getAzimuth() + (timeDifference * (azimuthRange / timeRange));
				
				// Add new (interpolated) point
				rawPoints.add(new MotionDataPoint(newTime, newValue, newX, newY, newZ, newAzimuth));
			}
		}
	}
	
	/**
	 * Performs a sliding average over rawPoints and returns it in a new List
	 */
	private List<MotionDataPoint> performSlidingAverage(List<MotionDataPoint> inputList) {
		List<MotionDataPoint> averagedPoints = new ArrayList<MotionDataPoint>();
		
		for(int i=0; i<inputList.size(); i++) {
			long newKey = inputList.get(i).getTime();
			
			// new Value is average of last Constants.polling_average_samples (non averaged) values
			MotionDataPoint averageDataPoint = getAverageDataPoint(
						newKey,
						inputList.subList(
								(int) Math.max(0, i-Constants.polling_average_samples), 
								(int) Math.min(inputList.size(), i))
					);
			averagedPoints.add(averageDataPoint);
		}
		
		// Remove first elements (walk on range for window)
		averagedPoints = averagedPoints.subList((int) Constants.polling_average_samples, averagedPoints.size());
		
		// Return result
		return averagedPoints;
	}
	
	/**
	 * Calculates average value over the given list of dataPoints
	 * Creates motionDataPoint with average values
	 */
	private MotionDataPoint getAverageDataPoint(long time, List<MotionDataPoint> dataPoints) {
	    double totalValue = 0;
	    double totalX = 0;
	    double totalY = 0;
	    double totalZ = 0;
	    double totalAzimuth = 0;
	    for (int i=0; i<dataPoints.size(); i++) {
	    	totalValue += dataPoints.get(i).getValue();
	    	totalX += dataPoints.get(i).getX();
	    	totalY += dataPoints.get(i).getY();
	    	totalZ += dataPoints.get(i).getZ();
	    	totalAzimuth += dataPoints.get(i).getAzimuth();
	    }
	    double value = Math.abs(totalValue/Constants.polling_average_samples);
	    double x = totalX/Constants.polling_average_samples;
	    double y = totalY/Constants.polling_average_samples;
	    double z = totalZ/Constants.polling_average_samples;
	    double azimuth = totalAzimuth/Constants.polling_average_samples;
	    
	    return new MotionDataPoint(time, value, x, y, z, azimuth);
	}
}