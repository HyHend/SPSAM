package net.hyhend.spsam.Utils;

import net.hyhend.spsam.R;
import net.hyhend.spsam.Localization.Location;

public class Constants {
	
	// Motion analysis constants
	public static int sample_rate = 8;        					// Ms
	public static int history_length = 600;              		// Max number of DataPoints in History
	public static float polling_average_samples = 5;
	public static int windowSize = 512;
	public static int knnNValue = 3;
	public static int knnNValueRssi = 8;
	public static int allowed_deviation = 2;					// Ms
	public static int fft_per_frequency_impact = 1;
	public static int avg_acceleration_impact = 1;
	public static int min_acceleration_impact = 1;
	public static int max_acceleration_impact = 1;
	public static int max_Relevant_Frequencies = 3;
	
	// Bayesian filter constants
	public static int location_Probabilities_to_show = 3; 		//amount of locations that get their probabilities shown
	
	// Particle filter constants
	public static int realtimeWindowSize = 128;					// as short as possible without losing the base frequencies (4Hz, one peak necessary 1/4 second frame = 32. Be safe, take 128?)
	public static double stepSize = 70*2;						//70 cm per step. Two steps per period
	public static int numParticles = 1000;
	public static double distanceDeviation = 0.25;				// 25% both ways
	public static double angleDeviation = 0.25;					// 0.25PI both sides
	
	// Collision maps constants
	public static String collision_map_file_name = "EWI_HB9.png";
	public static String collision_ui_map_file_name = "EWI_HB9_UI.png";
	public static double collision_map_distance_per_pixel = 0.107;		// = avg of w_px/w_cm, h_px/h_cm for hall and third room top left
	public static int collision_ui_map_x_offset = 5;					// Scaled collisionMap + offset = UI map
	public static int collision_ui_map_y_offset = 15;					// Scaled collisionMap + offset = UI map		
	public static double collision_map_ui_scale = 1.093;				// Collision map * scale = ui map
	public static double collision_map_north_offset = 0.5759;			// Radians. Offset of map to north offset of 0 is north to top of map (ewi: 237 deg to radians)

	// File IO constants
	public static boolean use_training_data_file = true;
	public static String training_MovementType_data_file_name = "spsam_Movement_training_data_v1.csv";
	public static String training_Fingerprint_data_file_name = "spsam_FingerPrint_training_data_v1.csv";
	public static String gauss_data_file_name = "spsam_gauss_data_v1.csv";
	public static String gauss_location_data_file_name = "spsam_gauss_data_per_location_v1.csv";
	public static String mocked_wifi_sample = "spsam_Conference_2.csv";
	
	// Location ID's (Bayesian Filter)
	public static int getIdForLocation(Location L)
	{
		switch(L)
		{
		case coffee_room:
			return R.id.coffee_room_TextView;
		case conference_room_1:
			return R.id.conference_room_1_TextView;
		case conference_room_2:
			return R.id.conference_room_2_TextView;
		case Flex_Room_1:
			return R.id.flex_room_1_TextView;
		case Flex_Room_2:
			return R.id.flex_room_2_TextView;
		case Hall_1:
			return R.id.hall_1_TextView;
		case Hall_10:
			return R.id.hall_10_TextView;
		case Hall_2:
			return R.id.hall_2_TextView;
		case Hall_3:
			return R.id.hall_3_TextView;
		case Hall_4:
			return R.id.hall_4_TextView;
		case Hall_5:
			return R.id.hall_5_TextView;
		case Hall_6:
			return R.id.hall_6_TextView;
		case Hall_7:
			return R.id.hall_7_TextView;
		case Hall_8:
			return R.id.hall_8_TextView;
		case Hall_9:
			return R.id.hall_9_TextView;
		case mgt_room:
			return R.id.mgt_room_TextView;
		case pleasure_room:
			return R.id.ple_room_TextView;
		default: return 0;
			
		}
	}
	
	// Helper function
	// Returns nearest power of two
	public static int findLowerNearestPowerOfTwo(int a) {
		if (a == 0)
		{
			//Cover the base case;
			return 0;
		}
		//Calculate the nearest power of two
		int subresult = a == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(a - 1);

		//Verify if it is the lower nearest
		if (Math.pow(2, subresult) == a)
		{
			return subresult;
		}
		else if (Math.pow(2, subresult-1) < a)
		{
			return subresult -1;
		}
		else
		{
			throw new RuntimeException("The power could not be properly calculated for this value:" + a);
		}
	}
}