package com.okeefm.geoscanner;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;


public class GeoScanner extends Activity {
    /** Called when the activity is first created. */
	private EditText text;
	private Integer index;
	private Location currentLocation;
	private Boolean usingLastKnownLocation;
	private LocationListener locationListener;
	private LocationManager lm;
	
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final long GPS_UPDATE_TIME = 1000 * 60;
	
	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (usingLastKnownLocation) {
	        // A new location is always better than the "last known location"
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        index = 0;
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
        	public void onLocationChanged(Location location) {
        		updateCurrentLocation(location);
        	}
        	
        	public void onStatusChanged(String provider, int status, Bundle extras) {}
        	
        	public void onProviderEnabled(String provider) {}
        	
        	public void onProviderDisabled(String provider) {
        		Toast.makeText(getApplicationContext(), "Please enable GPS for location to work", Toast.LENGTH_LONG).show();
        	}
        };
        
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_TIME, 0, locationListener);
        currentLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        usingLastKnownLocation = true;
    }
    
    @Override
    /**overrides the onPause() function provided */
    protected void onPause() {
    	super.onPause();
    	lm.removeUpdates(locationListener);
    }
    
    @Override
    /** overrides the onResume() function provided */
    protected void onResume() {
    	super.onResume();
    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_TIME, 0, locationListener);
    	currentLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
    
    /** updates the current location, based on if the new location is better */
    public void updateCurrentLocation(Location location) {
    	if (isBetterLocation(location, currentLocation)) {
    		currentLocation = location;
    		usingLastKnownLocation = false;
    	}
    }
    
    /** the button listener */
    public void onScanBarcodeClick(View v) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.setPackage("com.google.zxing.client.android");
        startActivityForResult(intent, 0);
    }
    
    /** set the text in the "location" editText box */
    private void setLocationText() {
    	text = (EditText) findViewById(R.id.locationEditText);
    	text.setText(Location.convert(currentLocation.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(currentLocation.getLongitude(), Location.FORMAT_DEGREES));
    }
    
    /** the intent return function from Barcode Scanner */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                text = (EditText) findViewById(R.id.barcodeEditText);
                text.setText(contents);
                setLocationText();
            } else if (resultCode == RESULT_CANCELED) {
            	Toast.makeText(getApplicationContext(), "Barcode Scanner did not return a valid barcode", Toast.LENGTH_LONG).show();
            }
        }
    }
}