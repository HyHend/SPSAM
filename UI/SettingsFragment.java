package net.hyhend.spsam.UI;

import net.hyhend.spsam.MotionDataAnalyzer;
import net.hyhend.spsam.R;
import net.hyhend.spsam.cloud.CloudManager;
import net.hyhend.spsam.TrainingDataFileIO;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
	private View rootView;
	
	/**
	 * Constructor
	 */
	public SettingsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_settings, container, false);

		//TextView mainTextView = (TextView) rootView.findViewById(R.id.mainTextView);
		//mainTextView.setText("Nothing to see here.");
		
		// Handle buttons
		handleRemoveFileButton();
		handleLoginButton();
		handleUploadButton();
		handleDownloadButton();
		return rootView;
	}
	
	/**
     * Handles Remove file button events
     */
    private void handleRemoveFileButton () {
    	final Button button = (Button) rootView.findViewById(R.id.removeFileButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	new AlertDialog.Builder(getActivity())
		            .setTitle("Remove Training Data")
		            .setMessage("Are you sure you want to remove the Training Data?")
		            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) { 
		                    // Continue with delete
		                	TrainingDataFileIO.destroyMovementTypeTrainingDataFile();
		                }
		             })
		            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) { 
		                    // Do nothing
		                }
		             })
		            .setIcon(android.R.drawable.ic_dialog_alert)
		             .show();
	        }
	    });
    }
 
    
	/**
     * Handles login Button
     */
    private void handleLoginButton () {
    	final Button button = (Button) rootView.findViewById(R.id.loginButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	EditText usernameText = (EditText) rootView.findViewById(R.id.UsernameField);
	        	EditText passwordText = (EditText) rootView.findViewById(R.id.PasswordField);
	        	String username = usernameText.getText().toString();
	        	String password =  passwordText.getText().toString();       
	        	
	        	InputMethodManager imm = (InputMethodManager)((MainActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
	    			imm.hideSoftInputFromWindow(usernameText.getWindowToken(), 0);
	    			imm.hideSoftInputFromWindow(passwordText.getWindowToken(), 0);
	       if (username.equals("") || password.equals(""))
	       {
	    	   new AlertDialog.Builder(v.getContext())
	            .setTitle("You have to fill in username and password")
	            .setMessage("")
	            .show();
	       }
	       else
	       {
	    	CloudManager manager = 	((MainActivity) getActivity()).getCloudManager();
	    	String result = manager.login(username, password, "not implemented");
	    	TextView message = (TextView) rootView.findViewById(R.id.messageLabel);
	    	message.setText(result);
	    	if (manager.hasHashToken())
	    	{
	    		
	    			
	    		//Hide all no longer needed controls
	    		Button loginButton = (Button) rootView.findViewById(R.id.loginButton);
	    		TextView usernameLabel = (TextView) rootView.findViewById(R.id.usernameLabel);
	    		TextView passwordLabel = (TextView) rootView.findViewById(R.id.passwordLabel);
	    		usernameText.setVisibility(View.GONE);
	    		usernameLabel.setVisibility(View.GONE);
	    		passwordText.setVisibility(View.GONE);
	    		passwordLabel.setVisibility(View.GONE);
	    		loginButton.setVisibility(View.GONE);
	    		
	    		//Show the new options
	    		Button removeDataButton = (Button) rootView.findViewById(R.id.removeFileButton);
	    		Button uploadDataButton = (Button) rootView.findViewById(R.id.uploadTrainingDataButton);
	    		Button downloadDataButton = (Button) rootView.findViewById(R.id.downloadTrainingData);
	    		removeDataButton.setVisibility(View.VISIBLE);
	    		uploadDataButton.setVisibility(View.VISIBLE);
	    		downloadDataButton.setVisibility(View.VISIBLE);	    		
	    	}
	       }
	        }
	    });
    }
  
    
    private void handleDownloadButton()
    {
    	final Button button = (Button) rootView.findViewById(R.id.downloadTrainingData);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	new AlertDialog.Builder(getActivity())
		            .setTitle("Download Training Data")
		            .setMessage("Are you sure you want to download the Training Data? This will remove your old data")
		            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) { 
		                	CloudManager manager = 	((MainActivity) getActivity()).getCloudManager();
		                	String newData = manager.downloadMovementData();
		                	if (newData.equals("Failed"))
		                	{
		                		TextView message = (TextView) rootView.findViewById(R.id.messageLabel);
		            	    	message.setText("Failed to download the movement data from the cloud");
		                	}
		                	else		                		
		                	{
		                	TrainingDataFileIO.destroyMovementTypeTrainingDataFile();
		                	TrainingDataFileIO.storeNewMovementData(newData);
		                	MotionDataAnalyzer analyzer = 	((MainActivity) getActivity()).getMotionDataAnalyzer();
		                	analyzer.updateMotionDataAnalyzer();
		                	
		                	TextView message = (TextView) rootView.findViewById(R.id.messageLabel);
	            	    	message.setText("Succesfully downloaded the training data from the cloud and updated the motion data analyzer");
		                	}
		                }
		             })
		            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) { 
		                    // Do nothing
		                }
		             })
		            .setIcon(android.R.drawable.ic_dialog_alert)
		             .show();
	        }
	    });
    }
    
    private void handleUploadButton()
    {
    	final Button button = (Button) rootView.findViewById(R.id.uploadTrainingDataButton);
		button.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(final View v) {
	        	new AlertDialog.Builder(getActivity())
		            .setTitle("Upload Training Data")
		            .setMessage("Are you sure you want to upload the Training Data? This will remove your old data from the server")
		            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) { 
		                	CloudManager manager = 	((MainActivity) getActivity()).getCloudManager();		                	
		                	boolean success = manager.uploadMovementData(TrainingDataFileIO.getMovementDataAsString());
		                	if (success)
		                	{
		                		TextView message = (TextView) rootView.findViewById(R.id.messageLabel);
		            	    	message.setText("Movement data was succesfully uploaded to the cloud");
		                	}
		                	else
		                	{
		                		TextView message = (TextView) rootView.findViewById(R.id.messageLabel);
		            	    	message.setText("Failed to upload the movement data to the cloud");
		                	}
		                }
		             })
		            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) { 
		                    // Do nothing
		                }
		             })
		            .setIcon(android.R.drawable.ic_dialog_alert)
		             .show();
	        }
	    });
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
}
