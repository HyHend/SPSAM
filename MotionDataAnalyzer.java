package net.hyhend.spsam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.hyhend.spsam.Utils.Constants;
import net.hyhend.spsam.Utils.FeatureVector;
import net.hyhend.spsam.Utils.MovementType;
import net.hyhend.spsam.Utils.Tuple;
import net.hyhend.spsam.comparators.DistanceToMovementTypeComparator;
import net.hyhend.spsam.comparators.FrequencyTupleComparator;

public class MotionDataAnalyzer {

	private static FFT FastFourier = new FFT(Constants.windowSize);
	
	private ArrayList<Tuple<MovementType,FeatureVector>> trainData;
	
	public MotionDataAnalyzer()
	{
		// Read train data from file, if set to in Constants
		if(Constants.use_training_data_file) {
			trainData = TrainingDataFileIO.getMovementTypeTrainingDataFromFile();
		}
		else {
			trainData = new ArrayList<Tuple<MovementType,FeatureVector>>();
		}
	}
	
	public void updateMotionDataAnalyzer()
	{
		trainData = TrainingDataFileIO.getMovementTypeTrainingDataFromFile();
	}
	
	
	/**
	 * This method retrieves the activities performed for a set of points including their time span in ordered sequence
	 * @param points ArrayList<Tuple<Double,MovementType>> (the double is time in MS)
	 * @return
	 */
	public ArrayList<Tuple<Double,MovementType>> getMovementTypesForPoints(MotionDataPoint[] points)
	{
		
		ArrayList<Tuple<Double,MovementType>> resultSet = new ArrayList<Tuple<Double,MovementType>>();
		
		ArrayList<FeatureVector> vectorsForPoints = getFeatureVectorsForPoints(points);
		
		//Determine the duration of one feature vectors movement type
		int singleSequenceDuration = Constants.windowSize * Constants.sample_rate;
		
		//Keep track of the last feature vector evaluated to be able to merge multiple with the same movement type.
		double currentMovementTypeduration = 0;
		MovementType lastMovementType = MovementType.Unknown;
	
		
		for(FeatureVector vector : vectorsForPoints)
		{
			//Determine movement type for current vector
			MovementType vectorMovementType = getMovementTypeForFeatureVector(vector);
			
			//Check whether the movement type was the same as the last movement type
			if (vectorMovementType == lastMovementType)
			{
				//It was, increase the duration
				currentMovementTypeduration += singleSequenceDuration;
			}	
			else if (currentMovementTypeduration != 0) //Dont add the first one
			{
				//Add the old one to the list,
				Tuple<Double,MovementType> result = new Tuple<Double,MovementType>(currentMovementTypeduration,lastMovementType);
				resultSet.add(result);		
				
				//And set the values to the new movementtype
				currentMovementTypeduration = singleSequenceDuration;
				lastMovementType = vectorMovementType;
			}
			else
			{
				//Do not add the former one, but do update the values of the new one
				currentMovementTypeduration = singleSequenceDuration;
				lastMovementType = vectorMovementType;
			}			
		}
		
		//The last one will never be added by the loop here above, so finally add the last activity to the list
		Tuple<Double,MovementType> result = new Tuple<Double,MovementType>(currentMovementTypeduration,lastMovementType);
		resultSet.add(result);				
		
		return resultSet;		
	}
	
	
	/**
	 * This method returns the most dominant movement type extracted from a data set of points
	 * @param points
	 * @return MovementType
	 */
	public MovementType getMovementTypeForPoints(MotionDataPoint[] points)
	{
		
		//Extract the feature vectors from the points
		ArrayList<FeatureVector> vectorsForPoints = getFeatureVectorsForPoints(points);		
		
		//Create a list to keep track of all movement types associated to the feature vectors
		ArrayList<MovementType> movementTypes =  new ArrayList<MovementType> ();		
		
		//For each vector, add the associated movement type to the list
		for(FeatureVector vector: vectorsForPoints)
		{
			MovementType  movementTypeForVector = getMovementTypeForFeatureVector(vector);
			movementTypes.add(movementTypeForVector);			
		}
		
		//Create a set of movement types that have been found in the signal
		Set<MovementType> uniqueMovementTypesAvailable = new HashSet<MovementType>(movementTypes);
		
		//initialize to be able to find the leading Movement type
		int occurences = 0;
		MovementType leadingType = MovementType.Unknown;
		
		//Find the leading movement type
		for (MovementType key : uniqueMovementTypesAvailable) {
			int frequency = Collections.frequency(movementTypes, key);
			if (frequency > occurences)
			{
				occurences =frequency;
				leadingType = key;
			}
		}
		return leadingType;
	}
	
	/**
	 * This method extracts feature vectors and stores them with the given movement annotation in the training data set
	 * @param points
	 * @param type
	 */
	public void train(MotionDataPoint[] points, MovementType type)
	{
		//Get the feature vectors belonging to the points given to train with
		ArrayList<FeatureVector> vectorsForPoints = getFeatureVectorsForPoints(points);
		
		//Store these feature vectors with an annotation as their movementType  in the training data set
		for(FeatureVector vector : vectorsForPoints)
		{
			Tuple<MovementType,FeatureVector> trainingDataPoint = new Tuple<MovementType,FeatureVector>(type, vector);
			trainData.add(trainingDataPoint);
			
			// Also write Feature vector to File, if set to on in Constants
			if(Constants.use_training_data_file) {
				TrainingDataFileIO.addMovementTypeTrainingDataToFile(trainingDataPoint);
			}
		}	
	}	
	
	
	/**
	 * This method retrieves the movement type for a given feature vector based on the training set that is available
	 * @param vector
	 * @return MovementType (In case no training set is available, unknown will be returned)
	 */
	private MovementType getMovementTypeForFeatureVector(FeatureVector vector)
	{
		MovementType leadingType = MovementType.Unknown;
		if (trainData.size() ==0)
		{
			//There is no training data, simply return the unknown type.
			return MovementType.Unknown;
		}
		else
		{
			ArrayList<Tuple<Double,MovementType>> distancesToMovementTypes = new ArrayList<Tuple<Double,MovementType>>();
			
			for(Tuple<MovementType,FeatureVector> trainingDataPoint : trainData)
			{
				double distance = trainingDataPoint.value.getDistance(vector);
				distancesToMovementTypes.add(new Tuple<Double,MovementType>(distance,trainingDataPoint.key));	
			}
			
			Collections.sort(distancesToMovementTypes, new DistanceToMovementTypeComparator());
			
			//Create a list to keep track of all movement types associated to the top N feature vectors
			ArrayList<MovementType> movementTypes =  new ArrayList<MovementType> ();		
			
			//Should get the ones with the minimal distance, so not the ones closest
			int end =  Math.min(distancesToMovementTypes.size() -1, Constants.knnNValue);
			for( int i =0; i < end; i++)
			{
				movementTypes.add(distancesToMovementTypes.get(i).value);	
			}
		
			//Create a set of movement types that have been found in the signal
			Set<MovementType> uniqueMovementTypesAvailable = new HashSet<MovementType>(movementTypes);
			
			//initialize to be able to find the leading Movement type
			int occurences = 0;
		
			
			//Find the leading movement type
			for (MovementType key : uniqueMovementTypesAvailable) {
				int frequency = Collections.frequency(movementTypes, key);
				if (frequency > occurences)
				{
					occurences =frequency;
					leadingType = key;
				}
			}
			
		}
		return leadingType;
	}
	
	
	/**
	 * This method retrieves the feature vectors for a set of points
	 * @param points (Any size set of points will do)
	 * @return ArrayList<FeatureVector> 
	 */
	private ArrayList<FeatureVector> getFeatureVectorsForPoints(MotionDataPoint[] points)
	{
		ArrayList<FeatureVector>  resultList = new ArrayList<FeatureVector>();
		//Calculate the possible amount of windows to extract from the collection of points (minus 1 to remove the first and last two seconds from the data set)
		int amountOfWindows = Math.max(0, (int)Math.floor(points.length/Constants.windowSize) -1);
	
		//Calculate the amount of points that will be unused to center the data (this reduces the impact of the enabling and disabling of the measuring)
		int unusedPoints = (points.length - (amountOfWindows * Constants.windowSize)); 
		
		//Calculate the index of the point to start with (can never be lower than 0, but reduced by 1 to make sure the offset is right
		int currentIndex =  Math.max((int)Math.floor(unusedPoints /2) -1, 0);
		
		//Calculate the individual feature vectors for each window and put them in the list to return
		for(int i =0; i < amountOfWindows; i++)
		{
			//Create a window data container
			MotionDataPoint[] window = new MotionDataPoint[Constants.windowSize];
			for (int j = 0; j < Constants.windowSize; j++)
			{
				window[j] = points[currentIndex + j];
			}
			//Get the feature vector for this window
			FeatureVector vectorForWindow = GetVectorForWindow(window);
			
			//Add this vector to the results
			resultList.add(vectorForWindow);
			
			//And increase the index to the next set
			currentIndex += Constants.windowSize;
		}
		return resultList;
	}
	
	/**
	 * This method extracts the feature vector from a window of points 
	 * @param points
	 * @return FeatureVector
	 */
	public static FeatureVector GetVectorForWindow(MotionDataPoint[] points)
	{
		if (points.length != Constants.windowSize)
		{
			   throw new RuntimeException("MotionDataAnalyzer: given amount of points to calculate with is invalid ");
		}
		
		double maxAmpl = 0;
		double minAmpl = Double.MAX_VALUE;
		double totalAcceleration = 0;
		
		//Calculate min and max ampl, and average acceleration
		for(int i = 0; i < points.length; i ++)
		{
			double currentPointValue = points[i].getValue();
			
			totalAcceleration += currentPointValue;
			
			if (maxAmpl < currentPointValue)
			{
				maxAmpl = currentPointValue;
			}
			if (minAmpl > currentPointValue)
			{
				minAmpl = currentPointValue;
			}
		}
		
		double averageAcceleration = totalAcceleration/points.length;
		
		// Put all MotionDataPoints values into double[]
		double[] values = new double[points.length];
		for(int j=0; j<points.length; j++) {
			values[j] = points[j].getValue();
		}
		
		//Do the FFT after analysis of the data because it is pass by reference and not pass by value and FFT adjusts the values!
		ArrayList<Tuple<Integer,Double>> frequencies = FastFourier.getFrequencies(values);
		Collections.sort(frequencies, new FrequencyTupleComparator());
		//Create an offset of 1 to prevent frequency 0 from popping up in the results
		int start = Math.max(0 , frequencies.size() -Constants.max_Relevant_Frequencies -1);
		ArrayList<Double> fundamentalFrequencies = new ArrayList<Double>() ;
		
		for(int i = start; i < frequencies.size(); i++)
		{
			if ((double)frequencies.get(i).key != 0)
			{
				fundamentalFrequencies.add((double)frequencies.get(i).key);
			}
		}
	for(int i =0; i < frequencies.size(); i++)
		{
			//System.out.println("frequency= "+frequencies.get(i).key+" strength= "+ frequencies.get(i).value);
		}
		//Extract fundamental frequencies from all frequencies 
	
		
		return new FeatureVector(fundamentalFrequencies,averageAcceleration,maxAmpl,minAmpl,0,0);		//Azimuth is only in use for realtimemotiondataanalyzer
	}
}
