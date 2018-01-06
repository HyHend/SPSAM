package net.hyhend.spsam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.hyhend.spsam.Localization.GaussCurve;
import net.hyhend.spsam.Localization.Location;
import net.hyhend.spsam.Localization.RssiFingerPrint;
import net.hyhend.spsam.Utils.Constants;
import net.hyhend.spsam.Utils.FeatureVector;
import net.hyhend.spsam.Utils.MovementType;
import net.hyhend.spsam.Utils.Tuple;
import android.os.Environment;

public class TrainingDataFileIO {
	/**
	 * Creates new file if file does not exist
	 */
	private static void createMovementTrainingDataFileIfNotExists () {
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
        
		String currentFile = dir+"/"+Constants.training_MovementType_data_file_name;
		
		File fileCheck = new File(currentFile);
        if(fileCheck.exists()) {
            // Do nothing
        }
        else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile, true));
			
                // CSV header format
                String line = "\"movement_type\"; \"frequency_0\"; \"frequency_1\"; \"frequency_2\"; \"average_acceleration\"; \"max_acceleration\"; \"min_acceleration\"; ";
                writer.write(line);
                writer.newLine();
	
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
	/**
	 * Creates (Fingerprint) file if non existent
	 */
	private static void createFingerPrintTrainingDataFileIfNotExists()
	{
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
        
		String currentFile = dir+"/"+Constants.training_Fingerprint_data_file_name;
		
		File fileCheck = new File(currentFile);
        if(fileCheck.exists()) {
            // Do nothing
        }
        else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile, true));
			
                // CSV header format
                String line = "\"SSID\"; \"RSSI\"; \"LocationId\";";
                writer.write(line);
                writer.newLine();
	
                writer.flush();
                writer.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
	}
    
	/**
	 * Creates (Gauss) file if non existent
	 */
	private static void createGaussDataFileIfNotExists(String filename)
	{
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
        
		String currentFile = dir+"/"+filename;
		
		File fileCheck = new File(currentFile);
        if(fileCheck.exists()) {
            // Do nothing
        }
        else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile, true));
			
                // CSV header format
                String line = "\"Location\"; \"AP\"; \"Mean\"; \"StandardDeviation\";";
                writer.write(line);
                writer.newLine();
	
                writer.flush();
                writer.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
	}
	
	public static String getMovementDataAsString()
	{
		String result = "";
	
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
		String currentFile = dir+"/"+Constants.training_MovementType_data_file_name;
    	File inputFile = new File(currentFile);
    	
    	// If file exists, read and add to datastring
    	if(inputFile.exists()) {
    		try {
        		BufferedReader reader = new BufferedReader(new FileReader(currentFile)); 			
    			
        		//Remove header 
        		 reader.readLine();
        		 
        		// Read each line and add to trainingDataString
        		String line;
    			while ((line = reader.readLine()) != null) {
    			
    				result += line + "{}";
    			}
    			reader.close();
    		}
    		catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    		return result;
	}
	/**
	 * 
	 * @param data
	 */
	public static void storeNewMovementData(String data)
	{
		// Create new File if not existent
        createMovementTrainingDataFileIfNotExists();
     
    	// Get file location
    	File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
		String currentFile = dir+"/"+Constants.training_MovementType_data_file_name;
        	
        // Add actual line to file
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile, true));
				writer.write(data.replaceAll("\\{\\}", "\n"));
	        writer.flush();
	        writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Add movement line to file
	 * @param toAdd
	 */
	public static void addMovementTypeTrainingDataToFile (Tuple<MovementType,FeatureVector> toAdd) {
    	// Create new File if not existent
        createMovementTrainingDataFileIfNotExists();

    	// Get file location
    	File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
		String currentFile = dir+"/"+Constants.training_MovementType_data_file_name;
        
        // Check number of fundamental frequencies matches at least 3
        if(toAdd.value.fundamentalFrequencies.size() < 3) {
            return; // TODO throw error?
        }
    	
        // Add actual line to file
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile, true));
					
			String line = "";
			line = line + "\""+toAdd.key.toString()+"\"; ";
			line = line + "\""+toAdd.value.fundamentalFrequencies.get(0)+"\"; ";
			line = line + "\""+toAdd.value.fundamentalFrequencies.get(1)+"\"; ";
			line = line + "\""+toAdd.value.fundamentalFrequencies.get(2)+"\"; ";
			line = line + "\""+toAdd.value.averageAcceleration+"\"; ";
			line = line + "\""+toAdd.value.maxAmpl+"\"; ";
			line = line + "\""+toAdd.value.minAmpl+"\"";
			 
			writer.write(line);
			writer.newLine();
	
	        writer.flush();
	        writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	/**
	 * Add gauss line to file
	 * @param toAdd
	 */
	public static void addGaussDataToFile (String filename, Location location, String accessPoint, GaussCurve curve) {
    	// Create new File if not existent
        createGaussDataFileIfNotExists(filename);
    	// Get file location
    	File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
		String currentFile = dir+"/"+filename;
        
        // Add actual line to file
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile, true));
					
			String line = "";
			line = line + "\""+location.toString()+"\"; ";
			line = line + "\""+accessPoint+"\"; ";
			line = line + "\""+curve.getMean()+"\"; ";
			line = line + "\""+curve.getStandardDeviation()+"\"; ";
			 
			writer.write(line);
			writer.newLine();
	
	        writer.flush();
	        writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	 
	/**
	 * Adds given Tuple<MovementType,FeatureVector> to file.
	 * Creates new file if not existent
	 * @param toAdd
	 */
    public static void addFingerprintTrainingDataToFile (RssiFingerPrint  toAdd) {
    	// Create new File if not existent
    	createFingerPrintTrainingDataFileIfNotExists();

    	// Get file location
    	File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
		String currentFile = dir+"/"+Constants.training_Fingerprint_data_file_name;

    	
        // Add actual line to file
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile, true));
					
			String line = "";
			line = line + "\""+toAdd.getIdentifier()+"\"; ";
			line = line + "\""+toAdd.getRssi()+"\"; ";
			line = line + "\""+toAdd.getLocation()+"\"; ";			 
			writer.write(line);
			writer.newLine();
	
	        writer.flush();
	        writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * Retrieves and returns training data from file
     * @return ArrayList<Tuple<MovementType,FeatureVector>>
     */
    public static ArrayList<Tuple<MovementType,FeatureVector>> getMovementTypeTrainingDataFromFile () {
    	ArrayList<Tuple<MovementType,FeatureVector>> trainingData = new ArrayList<Tuple<MovementType,FeatureVector>>();
   
    	// Open file
    	File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
		String currentFile = dir+"/"+Constants.training_MovementType_data_file_name;
    	File inputFile = new File(currentFile);
    	
    	// If file exists, read and add to trainingData
        if(inputFile.exists()) {
    		try {
        		BufferedReader reader = new BufferedReader(new FileReader(currentFile));
    			
        		String line;
    			line = reader.readLine();	// Skip first line (CSV header)
    			
    			// Read each line and add to trainingData
    			while ((line = reader.readLine()) != null) {
    				line = line.substring(1, line.length()-1);
    				String[] rowData = line.split("\"; \"");

    				// Verify line length
    				if(rowData.length >= 7) {
	    				// Get fundamental frequencies
	    				ArrayList<Double> fundamentalFrequencies = new ArrayList<Double>();
	    				fundamentalFrequencies.add(Double.parseDouble(rowData[1]));
	    				fundamentalFrequencies.add(Double.parseDouble(rowData[2]));
	    				fundamentalFrequencies.add(Double.parseDouble(rowData[3]));
	    				
	    				// Create featureVector
	    				FeatureVector featureVector = new FeatureVector(
	    						fundamentalFrequencies,
	    						Double.parseDouble(rowData[4]),
	    						Double.parseDouble(rowData[5]),
	    						Double.parseDouble(rowData[6]),
	    						0,
	    						0
	    						);
	
		             	// Add combination of MovementType and FeatureVector to trainingData
	    				Tuple<MovementType,FeatureVector> trainingDataVector = new Tuple<MovementType,FeatureVector>(
	    							MovementType.valueOf(rowData[0]),
	    							featureVector
	    						);
		             	trainingData.add(trainingDataVector);
    				}
    				else {
    				
    				}
    			}

    			reader.close();
    		}
    		catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	    
    	return trainingData;
    }
    
    
    
    public static ArrayList<RssiFingerPrint> getMockedWifiScanFromFile()
    {
    	ArrayList<RssiFingerPrint> fingerprintData = new ArrayList<RssiFingerPrint>();
    	   
    	// Open file
    	File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
		String currentFile = dir+"/"+Constants.mocked_wifi_sample;
    	File inputFile = new File(currentFile);
    	
    	// If file exists, read and add to trainingData
        if(inputFile.exists()) {
    		try {
        		BufferedReader reader = new BufferedReader(new FileReader(currentFile));
    			
        		String line;
    			line = reader.readLine();	// Skip first line (CSV header)
    			
    			// Read each line and add to trainingData
    			while ((line = reader.readLine()) != null) {
    				line = line.substring(1, line.length()-1);
    				String[] rowData = line.split("\"; \"");

    				// Verify line length
    				if(rowData.length >= 3) {	
    					// Trim last " from location (it's in the text file..)
    					rowData[2] = rowData[2].split("\"")[0];
    					
	    				RssiFingerPrint fingerPrint = new RssiFingerPrint(rowData[0], Double.parseDouble(rowData[1]), null);
	    				fingerprintData.add(fingerPrint);
    				}
    				else {
    			
    				}
    			}

    			reader.close();
    		}
    		catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	    
    	return fingerprintData;
    }
    /**
     * Reads fingerprints from file
     * @return
     */
    public static ArrayList<RssiFingerPrint> getFingerPrintDataFromFile () {
    	ArrayList<RssiFingerPrint> fingerprintData = new ArrayList<RssiFingerPrint>();
   
    	// Open file
    	File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
		dir.mkdirs();
		String currentFile = dir+"/"+Constants.training_Fingerprint_data_file_name;
    	File inputFile = new File(currentFile);
    	
    	// If file exists, read and add to trainingData
        if(inputFile.exists()) {
    		try {
        		BufferedReader reader = new BufferedReader(new FileReader(currentFile));
    			
        		String line;
    			line = reader.readLine();	// Skip first line (CSV header)
    			
    			// Read each line and add to trainingData
    			while ((line = reader.readLine()) != null) {
    				line = line.substring(1, line.length()-1);
    				String[] rowData = line.split("\"; \"");

    				// Verify line length
    				if(rowData.length >= 3) {	
    					// Trim last " from location (it's in the text file..)
    					rowData[2] = rowData[2].split("\"")[0];
    					
	    				RssiFingerPrint fingerPrint = new RssiFingerPrint(rowData[0], Double.parseDouble(rowData[1]), Location.valueOf(rowData[2]));
	    				fingerprintData.add(fingerPrint);
    				}
    				else {
    					// TODO throw exception
    				}
    			}

    			reader.close();
    		}
    		catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	    
    	return fingerprintData;
    }
    
    /**
     * Destroys file and it's contents. All training data will be lost.
     */
    public static void destroyMovementTypeTrainingDataFile () {
    	destroyFile(Constants.training_MovementType_data_file_name);        
    }
    
    /**
     * Destroys file and it's contents. All training data will be lost.
     */
    public static void destroyFingerPrintsTrainingDataFile () {
    	destroyFile(Constants.training_Fingerprint_data_file_name);        
    }
    
    /**
     * Destroys file and it's contents. All gauss data will be lost.
     */
    public static void destroyGaussDataFiles () {
    	destroyFile(Constants.gauss_data_file_name);   
    	destroyFile(Constants.gauss_location_data_file_name);        
    }
    
    /**
     * Destroys given file
     * @param fileName
     */
    private static void destroyFile(String fileName)
    {
    	File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/sps");
    	String currentFile = dir+"/"+ fileName;
    	dir.mkdirs();
		File file = new File(currentFile);
        if(file.exists()) {
        	file.delete();
        }
    }
}
