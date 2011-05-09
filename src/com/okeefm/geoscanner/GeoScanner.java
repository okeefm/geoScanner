package com.okeefm.geoscanner;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class GeoScanner extends Activity {
    /** Called when the activity is first created. */
	private Integer index;
	
	private static final String TAG = "geoScanner";
	private static final int NEW_BARCODE_REQUEST = 1;
	
	private ArrayList<Barcode> barcodes;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        index = 0;
        Button button = (Button) findViewById(R.id.newBarcodeButton);
        button.setOnClickListener(newBarcodeListener);
        barcodes = new ArrayList<Barcode>();
    }
    
    @Override
    /**overrides the onPause() function provided */
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    /** overrides the onResume() function provided */
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == NEW_BARCODE_REQUEST) {
    		if (resultCode == RESULT_OK) {
    			index++;
    			
    			barcodes.add(data.getExtras().getParcelable("NEW_BARCODE"));
    		}
    	}
    }
    
    private OnClickListener newBarcodeListener = new OnClickListener() {
	    public void onClick(View view) {
	    	Intent intent = new Intent(getApplicationContext(), BarcodeAdder.class);
	    	intent.putExtra("index", index);
	    	startActivityForResult(intent, NEW_BARCODE_REQUEST);
	    }
    };
    
}