package com.example.pathexample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.util.Log;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	
	// set this to 256 for actual tile size, 512 to show larger and cause PathOverlay to not be displayed
	int tileSize = 512; 
	
	private MapView mapView;
	
	// area of offline tiles
	double north = 40.739063;
	double south =  40.708361;
	double west  =  -73.967171;
    double east  =  -73.936272;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		// center of offline tiles
		double centerlat = (double) ((north+south)/2);
		double centerlon = (double) ((west+east)/2);

		// copy tiles to sd location for offline map
		putMapOnSD();
	
		// create mapView and show layout
		mapView = new MapView(this,tileSize);
		final LinearLayout layout = new LinearLayout(this);
		final LinearLayout.LayoutParams mapViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		layout.addView(mapView, mapViewLayoutParams);
		setContentView(layout);
		
		// set map to use offline tiles and display
		mapView.setTileSource  (new XYTileSource ("tiles", ResourceProxy.string.offline_mode, 13, 17, tileSize, ".png", "http://127.0.0.1")); 
		mapView.setUseDataConnection(false);
		mapView.setClickable(false);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(false);
		mapView.getController().setZoom(15);
		mapView.getController().setCenter(new GeoPoint(centerlat,centerlon));
		
		// show pathOverlay
		PathOverlay pathOverlay = new PathOverlay(Color.RED, this);
		pathOverlay.addPoint(new GeoPoint(centerlat,centerlon));
		centerlat += 0.005;
		pathOverlay.addPoint(new GeoPoint(centerlat,centerlon));
		centerlon += 0.005;
		pathOverlay.addPoint(new GeoPoint(centerlat,centerlon));
		pathOverlay.getPaint().setStrokeWidth(10.0f);
		mapView.getOverlays().add(pathOverlay);
		
		// refresh map, is this needed?
		mapView.invalidate();
	}

	
	// this copies the offline tiles to the proper location for OSMDroid to use them offline
	private void putMapOnSD() {
		new File("/mnt/sdcard/osmdroid/").mkdir();
		AssetManager assetManager = getAssets();
		String[] files = null;
		try{
			files = assetManager.list("");  
		}
		catch(IOException e){
			Log.e("debug", e.getMessage());
		}
		File mapsdfile = new File("/mnt/sdcard/osmdroid/tiles.zip");
		if(!mapsdfile.exists()){
			for(String filename : files){
				if(filename.contains("tiles")){
					InputStream in = null;
					OutputStream out = null;
					try{
						in = assetManager.open(filename);
						out = new FileOutputStream("/sdcard/osmdroid/" + filename);
						byte[] buffer = new byte[1024];
					    int read;
					    while((read = in.read(buffer)) != -1) {
					      out.write(buffer, 0, read);
					    }
						in.close(); 
						in = null;
						out.flush(); 
						out.close();
						out = null;
						Log.v("debug","Map file added to "+ mapsdfile);
					} 
					catch(Exception e) {
						Log.e("debug", e.getMessage());
					}    	
				}
			}
		}
	}
}
