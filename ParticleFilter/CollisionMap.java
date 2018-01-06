package net.hyhend.spsam.ParticleFilter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.hyhend.spsam.UI.MainActivity;
import net.hyhend.spsam.Utils.Constants;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

public class CollisionMap {
	boolean[][] map;		// map[rows][columns] 
	String uiMapname;
	int width;
	int height;
	double distancePerPixel;
	double northOffset;
	
	/** 
	 * Constructor
	 */
	public CollisionMap(String locationMap) {
		// Open image from given locationMap (add more maps in the future)
		Bitmap bitmap = null;
		//if(locationMap.equals("EWI_HB9")) {
			bitmap = getBitmapFromAsset(Constants.collision_map_file_name);
			uiMapname = Constants.collision_ui_map_file_name;
		//}
		//else {
			// Throw exception and quit
		//}
		
		// Get sizes
		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();
		this.distancePerPixel = Constants.collision_map_distance_per_pixel;		// Not directly from config to allow for multiple collisionmaps
		this.northOffset = Constants.collision_map_north_offset;				// Not directly from config to allow for multiple collisionmaps
		
		// Convert and save in map
		map = bitmapToBooleanArray(bitmap);
	}
	
	/**
	 * Returns boolean values
	 * @return boolean[][]
	 */
	public boolean[][] getMap() {
		return this.map;
	}
	
	/**
	 * Returns uiMapname
	 * @return String
	 */
	public String getUIMap() {
		return this.uiMapname;
	}
	
	/**
	 * Returns mapWidth
	 * @return int
	 */
	public int getWidth() {
		return this.width;
	}
	
	/**
	 * Returns mapHeight
	 * @return int
	 */
	public int getHeight() {
		return this.height;
	}
	
	/**
	 * Returns distancePerPixel
	 * @return double
	 */
	public double getDistancePerPixel() {
		return this.distancePerPixel;
	}
	
	/**
	 * Returns northOffset
	 * @return double
	 */
	public double getNorthOffset() {
		return this.northOffset;		
	}
	
	/**
	 * Returns value for given location. false if outside of map
	 * @param x
	 * @param y
	 * @return boolean
	 */
	public boolean getValueFor(int x, int y) {
		if(x < this.width && y < this.height
				&& x >= 0 && y >= 0) {
			return map[y][x];
		}
		return false;
	}
	
	/**
	 * Retrieves bitmap from assets folder
	 * @param strName
	 * @return
	 */
	private Bitmap getBitmapFromAsset(String strName)
	{
	    AssetManager assetManager = MainActivity.getAppContext().getAssets();
	    InputStream istr = null;
	    try {
	        istr = assetManager.open(strName);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    BitmapFactory.Options bmo = new BitmapFactory.Options();
        bmo.inPreferredConfig = Config.ARGB_8888;
	    Bitmap bitmap = BitmapFactory.decodeStream(istr);
	    return bitmap;
	}
	
	/**
	 * Processes bitmap and outputs it in boolean[][] (threshold: < 128 == 0)
	 * @param bm
	 * @return boolean [][]
	 */
	public boolean[][] bitmapToBooleanArray(Bitmap bm) {
        // Create the buffer with the correct size
        int iBytes = bm.getWidth() * bm.getHeight() * 4;
        ByteBuffer buffer = ByteBuffer.allocate(iBytes);

        // Copy to buffer and then into int array
        bm.copyPixelsToBuffer(buffer);
        byte[] values = buffer.array();
        
        // Process to boolean[][]
        boolean[][] booleanValues = new boolean[this.height][this.width];
        int row = 0;
        int col = 0;
        
        // Threshold all values and add to corresponding location in boolean[][] array
        for (int i = 0; i < values.length; i += 4) {
            int a = (((int) values[i] & 0xff) << 24); 			// alpha
            int b = ((int) values[i + 1] & 0xff); 				// blue
            int g = (((int) values[i + 2] & 0xff) << 8); 		// green
            int r = (((int) values[i + 3] & 0xff) << 16);		// red

            // Threshold and add
    		booleanValues[row][col] = (a+r+g+b) < 8355840;		// Threshold: 24 bit/2
    		
    		// Increment correct row/col
            col++;
            if (col == this.width) {
               col = 0;
               row++;
            }
        }
        
        return booleanValues;
    }
}
