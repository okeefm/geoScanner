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
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class BarcodeAdder extends Activity{
	private EditText text;
	private EditText dateText;
	private EditText timeText;
	private Location currentLocation;
	private Location savedLocation;
	private Boolean usingLastKnownLocation;
	private LocationListener locationListener;
	private LocationManager lm;
	
	private Button saveButton, cancelButton;
	
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;
	
	private int index;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final long GPS_UPDATE_TIME = 1000 * 60;
	
	static final int DATE_DIALOG_ID = 0;
	static final int TIME_DIALOG_ID = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			index = extras.getInt("index");
		}
		
		dateText = (EditText) findViewById(R.id.dateEditText);
        timeText = (EditText) findViewById(R.id.timeEditText);
        
        dateText.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		setCurrentDate();
        		showDialog(DATE_DIALOG_ID);
        		updateDateDisplay();
        	}
        });
        
        timeText.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		setCurrentTime();
        		showDialog(TIME_DIALOG_ID);
        		updateTimeDisplay();
        	}
        });
        
        setCurrentDate();
        updateDateDisplay();
        
        setCurrentTime();
        updateTimeDisplay();
        
        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        
        Intent intent = this.getIntent();
        
        saveButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				saveData();
			}
		});
        
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
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        case TIME_DIALOG_ID:
        	return new TimePickerDialog(this,
        				mTimeSetListener,
        				mHour, mMinute, true);
        }
        return null;
    }
    
    public void setCurrentDate() {
    	final Calendar c = Calendar.getInstance();
    	mYear = c.get(Calendar.YEAR);
    	mMonth = c.get(Calendar.MONTH);
    	mDay = c.get(Calendar.DAY_OF_MONTH);
    }
    
    public void setCurrentTime() {
    	final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
    }
    
    public void updateDateDisplay() {
    	EditText editText = (EditText) findViewById(R.id.dateEditText);
    	editText.setText(
    			new StringBuilder()
    				//Month is 0-based so add 1
    				.append(mMonth + 1).append("-")
    				.append(mDay).append("-")
    				.append(mYear));
    }
    
    private void updateTimeDisplay() {
        EditText editText = (EditText) findViewById(R.id.timeEditText);
        editText.setText(
            new StringBuilder()
                    .append(pad(mHour)).append(":")
                    .append(pad(mMinute)));
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
    
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDateDisplay();
                }
            };
            
 // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
    	
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
                updateTimeDisplay();
            }
        };
    
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
    
    public void saveData() {
    	EditText editText = (EditText) findViewById(R.id.barcodeEditText);
    	String barcodeString = editText.getText().toString();
    	editText = (EditText) findViewById(R.id.labelEditText);
    	String label = editText.getText().toString();
    	Time gc = new Time();
    	gc.set(0, mMinute, mHour, mMonth, mDay, mYear);
    	Barcode barcode = new Barcode(barcodeString, savedLocation, gc, label);
    	Intent intent = this.getIntent();
    	intent.putExtra("NEW_BARCODE", barcode);
    	this.setResult(RESULT_OK, intent);
    	finish();
    }
    
    public void cancelAdd() {
    	this.setResult(RESULT_CANCELED);
    	finish();
    }
    
    /** set the text in the "location" editText box */
    private void setLocationText() {
    	savedLocation = currentLocation;
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
