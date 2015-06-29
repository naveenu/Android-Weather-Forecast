package com.trackthebird.weatherforecast;
/*	By Naveenu Perumunda	*/
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.trackthebird.weatherforecast.adapter.WeatherForecastRecyclerViewAdapter;

public class WPMainActivity extends ActionBarActivity implements LocationListener{

	private Context context								=	null;
    private RecyclerView 				mRecyclerView	=	null;
    private RecyclerView.Adapter 		mAdapter		=	null;
    private RecyclerView.LayoutManager	mLayoutManager	=	null;
    private String 	 					mApiKey 		= 	null;
    double mLatitude 									= 	0;
    double mLongitude 									= 	0;
    private ProgressDialog mShowProgressDlg				=	null;
    private String mForecastData						=	null;

    /*
     * Initialize the applicaton variable and create object 
     */ 
	private void init(){
		context				=	this;
		mRecyclerView 		=	(RecyclerView) findViewById(R.id.weather_forecast_recyclerview_item);	
		 
	    // Setting the adapter to RecyclerView
	    mLayoutManager 		=   new LinearLayoutManager(context);                 	// Creating a layout Manager
	    mRecyclerView.setLayoutManager(mLayoutManager);                 	// Setting the layout Manager
		    
	    // Get and set API keys
	    mApiKey				=	getApikey("API_KEY");    // Add API Key in Manifest file
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wpmain);
		// Init app
		init();
		// get location detail
		getLocation();
		// Get Weather details
		getWeatherDeatils(getResources().getString(R.string.action_settings_currently));
	}
    /*
     * Calls Recycler view adapter after creating Data JSONArray
     */
	private void displayWeatherDetails(Bundle params){
		JSONArray	jsonArray	=	new JSONArray();
		try {
			if(params.getString("weather_type").equalsIgnoreCase(getResources().getString(R.string.action_settings_minutely))){
				if(new JSONObject(params.getString("response")).has("minutely")){
					jsonArray	=	new JSONArray(new JSONObject(params.getString("response")).getJSONObject("minutely").getString("data"));
				}
				else{
					Toast.makeText(context, getResources().getString(R.string.minuely_forecast_not_available), Toast.LENGTH_SHORT).show();
				}
			}
			else if(params.getString("weather_type").equalsIgnoreCase(getResources().getString(R.string.action_settings_hourly))){
				jsonArray	=	new JSONArray(new JSONObject(params.getString("response")).getJSONObject("hourly").getString("data"));
			}
			else if(params.getString("weather_type").equalsIgnoreCase(getResources().getString(R.string.action_settings_daily))){
				jsonArray	=	new JSONArray(new JSONObject(params.getString("response")).getJSONObject("daily").getString("data"));
			}
			else {
				jsonArray.put(new JSONObject(params.getString("response")).getJSONObject("currently"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// Calls Recyclerview adapter
		if(jsonArray.length() > 0){
			// Set Dynamic title based on the weather forecast
			String title	=	params.getString("weather_type");
			if(title.equalsIgnoreCase(getResources().getString(R.string.action_settings_refresh))){
				title		=	getResources().getString(R.string.app_name);
			}
			this.setTitle(title);
			mAdapter			=	new WeatherForecastRecyclerViewAdapter(context, jsonArray);
			mRecyclerView.setAdapter(mAdapter); 
		}
		else{
			//Show Error message
			Toast.makeText(context, getResources().getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
		}
	}
    /*
     * 	Get ApiKey from Manifest files
     */
	private String getApikey(String type) {
		String keys = null;
		try {
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			keys 					= (String)appInfo.metaData.get(type);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return keys;
	}
    /*
     * Gets the weather details from server based on Hourly or currently settings. Default is Currently.
     */
	private void getWeatherDeatils(String weatherType){
		// Get fresh weather data only if user action is "Refresh" or on launch time
		if((mForecastData != null) && (!weatherType.equalsIgnoreCase(getResources().getString(R.string.action_settings_refresh)))){
			Bundle params	=	new Bundle();
		    params.putString("response",mForecastData);
		    params.putString("weather_type", weatherType);
		    displayWeatherDetails(params);
		}
		else if(isNetworkAvailable()){
			//Show Popup
			ShowPopUp();
			//Call Asynchronus task and download weather details
			new GetWheatherForecaseAsyncTask().execute(weatherType);
		}
		else{
			Toast.makeText(context, getResources().getString(R.string.internet_not_available), Toast.LENGTH_SHORT).show();
		}
	}
	
    /*
     * Async task class to get the Weather data from forecast server.
     */
	private class GetWheatherForecaseAsyncTask extends AsyncTask<String, Void, Bundle>{
		@Override
		protected Bundle doInBackground(String... paramType) {
			Bundle params			=	new Bundle();
			StringBuilder forecastWeatherUrl = new StringBuilder(getResources().getString(R.string.weather_fore_cast_url));
				forecastWeatherUrl.append(mApiKey+ "/");
				forecastWeatherUrl.append(mLatitude+ ",");
				forecastWeatherUrl.append(mLongitude);
				//Calls HTTP class
		        Http http 				= 	new Http();
		        try {
		        	mForecastData		=	http.read(forecastWeatherUrl.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
		    params.putString("response",mForecastData);
		    params.putString("weather_type", paramType[0].toString());
		    return params;
		}

		@Override
		protected void onPostExecute(Bundle params){ // This is in UI thread
			if(params.size() > 1){
				displayWeatherDetails(params);
			}
			else{
				Toast.makeText(context, getResources().getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
			}
			DismissPopUp();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wpmain, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_current_weather) {
			getWeatherDeatils(getResources().getString(R.string.action_settings_currently));
			return true;
		}
		else if (id == R.id.action_minutely_weathers) {
			getWeatherDeatils(getResources().getString(R.string.action_settings_minutely));
			return true;
		}
		else if(id == R.id.action_hourly_weathers){
			getWeatherDeatils(getResources().getString(R.string.action_settings_hourly));
			return true;
		}
		else if (id == R.id.action_daily_weathers) {
			getWeatherDeatils(getResources().getString(R.string.action_settings_daily));
			return true;
		}
		else if (id == R.id.action_refresh_weathers) {
			getWeatherDeatils(getResources().getString(R.string.action_settings_refresh));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    
    /*
     * Check whether Network is available or not
     */
	public boolean isNetworkAvailable(){
		try{
			ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		    // if no network is available networkInfo will be null
		    // otherwise check if we are connected
		    if (networkInfo != null && networkInfo.isConnected()) {
		        return true;
		    }
		} catch (Exception e){
			e.printStackTrace();
		}
		 return false;
	}
    /*
     * 	Gets location lat and long
     */
	private void getLocation(){
		LocationManager locManager 	= 	(LocationManager)getSystemService(Context.LOCATION_SERVICE); 
        Criteria criteria 			= 	new Criteria();
        String bestProvider 		= 	locManager.getBestProvider(criteria, true);
        Location location 			= 	locManager.getLastKnownLocation(bestProvider);
        if(location == null){
        	location				=	locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if(location != null){
    		mLatitude 					= 	location.getLatitude();
    		mLongitude 					=  	location.getLongitude();
        }
	}
	
    /*
     * Location change listener
     */
    @Override
    public void onLocationChanged(Location location) {
    	if((location.getLatitude() != 0) && (location.getLongitude() != 0)){
        	mLatitude 		= location.getLatitude();
        	mLongitude 		= location.getLongitude();
    	}
    }
    
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
    /*
     * Show popup message while downloading search items
     */
	protected void ShowPopUp(){
        this.runOnUiThread(new Runnable() {
            public void run() {
            	mShowProgressDlg = new ProgressDialog(context);
            	mShowProgressDlg.setTitle(getResources().getString(R.string.forecast_weather_popup_title));
            	mShowProgressDlg.setMessage(getResources().getString(R.string.please_wait));
        		mShowProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        		mShowProgressDlg.setIcon(R.drawable.ic_launcher);
        		mShowProgressDlg.setCancelable(true);
        		mShowProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
        		    @Override
        		    public void onClick(DialogInterface dialog, int which) {
        		    	try{
        			        dialog.cancel();
        			        dialog= null;
        			        mShowProgressDlg.dismiss();
        		    	}
        		    	catch(Exception e){
        		    	}
        		    }
        		});
        		mShowProgressDlg.show();
            }
        });
	}
	
    /*
     * 	Dismiss popup
     */
	protected void DismissPopUp(){
		if(mShowProgressDlg != null && mShowProgressDlg.isShowing()){
			mShowProgressDlg.dismiss();
		}
	}
	
	@Override
	public void onStop(){
		super.onStop();
		//Make sure that Popup dialog is closed before closing the app.
		if(mShowProgressDlg != null){
			DismissPopUp();
		}
	}
}
