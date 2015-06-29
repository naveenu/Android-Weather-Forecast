package com.trackthebird.weatherforecast.adapter;
/*	By Naveenu Perumunda	*/
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.trackthebird.weatherforecast.R;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * 
 */
public class WeatherForecastRecyclerViewAdapter extends RecyclerView.Adapter<WeatherForecastRecyclerViewAdapter.ViewHolder>{
	private static Context	context;
	public static String DATE_FORMAT		= 	"MMM dd, yyyy hh:mm:ss a";
	private JSONArray	jsonArrayWeatherdata=	null;
	private String title					=	null;
	private LayoutInflater	 inflater		=   null;
	
    /*
     * Creating a ViewHolder which extends the RecyclerView View Holder
     * ViewHolder are used to to store the inflated views in order to recycle them
     */
	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		private LinearLayout	linearLayoutWeatherBackground	=	null;
		
		private TextView 	tvLastUpdatedTime		=	null;
		
		private ImageView 	ivWeatherStatusIcon		=	null;
		private TextView 	tvWeatherSummary		=	null; 
		private TextView 	tvTemparature			=	null; 
		 
		private TextView 	tvHumidity				=	null;
		private TextView 	tvDewPoint				=	null; 
		private TextView 	tvPressure				=	null; 
		
		
		private TextView 	tvWindSpeed				=	null;
		private TextView	tvVisibility			=	null;
		private TextView	tvOzone					=	null;
		
		private TextView tvNearstStormDistance		=	null;
		
        private int mDispayViewOriginalHeight		= 	0;
        private boolean mIsViewExpanded 			= 	false;
        
		public ViewHolder(View itemView,int ViewType) {                 // Creating a ViewHolder Constructor with View and viewType As a parameter
			super(itemView);
			itemView.setClickable(true);
			linearLayoutWeatherBackground	= (LinearLayout) itemView.findViewById(R.id.linearLayoutWeatherForecast);
			// Set the appropriate view in accordance with the the view type as passed when the holder object is created
			tvLastUpdatedTime				= (TextView) itemView.findViewById(R.id.tvLastUpdatedTime);
			
			ivWeatherStatusIcon				= (ImageView) itemView.findViewById(R.id.ivWeatherStatusIcon); 	
			tvWeatherSummary 				= (TextView) itemView.findViewById(R.id.tvWeatherSummary);
			tvTemparature 					= (TextView) itemView.findViewById(R.id.tvTemparature);
			
			tvHumidity						= (TextView) itemView.findViewById(R.id.tvHumidity);
			tvDewPoint	 					= (TextView) itemView.findViewById(R.id.tvDewPoint);
			tvPressure		 				= (TextView) itemView.findViewById(R.id.tvPressure); 
			
			tvWindSpeed						= (TextView) itemView.findViewById(R.id.tvWindSpeed);	
			tvVisibility					= (TextView) itemView.findViewById(R.id.tvVisibility);
			tvOzone	 						= (TextView) itemView.findViewById(R.id.tvOzone);

			tvNearstStormDistance	 		= (TextView) itemView.findViewById(R.id.tvNearstStormDistance);
			
			// Set listener
			itemView.setOnClickListener(this);
			itemView.setLongClickable(true);
		}

		@SuppressLint("NewApi")
		@Override
		public void onClick(final View view) {
		    float subViewHeightFactor	=	0.65F;		    
		    LinearLayout layout			= 	(LinearLayout) view.findViewById(R.id.sub_list_item);
		    layout.setVisibility(View.VISIBLE);
		    notifyItemChanged(getAdapterPosition()-1);
        	if (mDispayViewOriginalHeight == 0) {
        		mDispayViewOriginalHeight = view.getHeight();
            }
           	ValueAnimator valueAnimator;
            if (!mIsViewExpanded) {
                mIsViewExpanded = true;
                valueAnimator = ValueAnimator.ofInt(mDispayViewOriginalHeight, mDispayViewOriginalHeight + (int) (mDispayViewOriginalHeight * subViewHeightFactor));
            } else {
                mIsViewExpanded = false;
                valueAnimator = ValueAnimator.ofInt(mDispayViewOriginalHeight + (int) (mDispayViewOriginalHeight *  subViewHeightFactor), mDispayViewOriginalHeight);
            }
            valueAnimator.setDuration(300);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    view.getLayoutParams().height = value.intValue();
                    view.requestLayout();
                }
            });
            valueAnimator.start();
        }
	}
    /*
     * 	Constructor
     */
	public WeatherForecastRecyclerViewAdapter(Context context, JSONArray jsonArrayWeatherdata){ //List<Information> data // MyAdapter Constructor with titles and icons parameter
		this.context	=	context;
		this.jsonArrayWeatherdata	=	jsonArrayWeatherdata;		
		inflater		=	LayoutInflater.from(context);
	}

	/*
	 * Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is(non-Javadoc)
	 * @see android.support.v7.widget.RecyclerView.Adapter#onCreateViewHolder(android.view.ViewGroup, int)
	 * Created, In this method we inflate the weather_forecast_recyclerview_item.xml layout
	 * and pass it to the view holder
	 */

	@Override
	public WeatherForecastRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view =	inflater.inflate(R.layout.weather_forecast_recyclerview_item,parent,false); //Inflating the layout
		ViewHolder vhItem = new ViewHolder(view,viewType); //Creating ViewHolder and passing the object of type view
		return vhItem; // Returning the created object
	}
	
    /*
     * Next we override a method which is called when the item in a row is needed to be displayed, here the int position
     * Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
     * which view type is being created 1 for item row
     */
	@Override
	public void onBindViewHolder(final WeatherForecastRecyclerViewAdapter.ViewHolder holder, int position) {
		if(jsonArrayWeatherdata.length() > 0){
			 try {
				 JSONObject itemJSONObject	=	jsonArrayWeatherdata.getJSONObject(position);
				 //Sets Last updated time
				 if(itemJSONObject.has("time"))
					 holder.tvLastUpdatedTime.setText(context.getResources().getString(R.string.last_updated_time)+" : "+getDateAndTime(itemJSONObject.getString("time"))); 
				 else
					 holder.tvLastUpdatedTime.setText(null); 	
				
				 //Sets Weather status icon and set the background status
				 if(itemJSONObject.has("icon")) {// Add the logic here later
					 holder.linearLayoutWeatherBackground.setBackgroundResource(getWeatherBackgroundGradient(itemJSONObject.getString("icon")));
					 holder.ivWeatherStatusIcon.setImageBitmap(getWeatherIcon(context, itemJSONObject.getString("icon"))); 
				 }
				 else{
					 holder.linearLayoutWeatherBackground.setBackgroundResource(R.drawable.gradient_background_default);
					 holder.ivWeatherStatusIcon.setImageBitmap(null); 	
				 }
					
				 //Set Summary
				 if(itemJSONObject.has("summary"))
					 holder.tvWeatherSummary.setText(itemJSONObject.getString("summary")); 
				 else
					 holder.tvWeatherSummary.setText(null); 
				 
				 //Sets Temparature
				 if(itemJSONObject.has("temperature"))
					 holder.tvTemparature.setText(context.getResources().getString(R.string.temperature)+" : "+convertFahrenheitToCelcius(itemJSONObject.getString("temperature")) +" \u2103"); 
				 else
					 holder.tvTemparature.setText(null); 
				 //Sets Last Humidity
				 if(itemJSONObject.has("humidity"))
					 holder.tvHumidity.setText(context.getResources().getString(R.string.humidity)+" : "+ getHumidityInpercent(itemJSONObject.getString("humidity"))+"%"); 
				 else
					 holder.tvHumidity.setText(null); 
				 
				 //Sets Dew Point
				 if(itemJSONObject.has("dewPoint"))
					 holder.tvDewPoint.setText(context.getResources().getString(R.string.dewPoint)+" : "+itemJSONObject.getString("dewPoint")+" \u2109"); 
				 else
					 holder.tvDewPoint.setText(null); 
				 
				 //Sets Presure
				 if(itemJSONObject.has("pressure"))
					 holder.tvPressure.setText(context.getResources().getString(R.string.pressure)+" : "+itemJSONObject.getString("pressure")+" M bars"); 
				 else
					 holder.tvPressure.setText(null); 
				 
				 //Sets Wind Speed
				 if(itemJSONObject.has("windSpeed"))
					 holder.tvWindSpeed.setText(context.getResources().getString(R.string.windSpeed)+" : "+getSpeedInKilloMeter(itemJSONObject.getString("windSpeed"))+" km"); 
				 else
					 holder.tvWindSpeed.setText(null); 
				 
				 //Sets Visibility
				 if(itemJSONObject.has("visibility"))
					 holder.tvVisibility.setText(context.getResources().getString(R.string.visibility)+" : "+getSpeedInKilloMeter(itemJSONObject.getString("visibility"))+" km"); 
				 else
					 holder.tvVisibility.setText(null); 
				 
				 //Sets Ozone
				 if(itemJSONObject.has("ozone"))
					 holder.tvOzone.setText(context.getResources().getString(R.string.ozone)+" : "+itemJSONObject.getString("ozone")+" Dobson"); 
				 else
					 holder.tvOzone.setText(null); 
				 
				 //Sets Nearest storm distance
				 if(itemJSONObject.has("nearestStormDistance"))
					 holder.tvNearstStormDistance.setText(context.getResources().getString(R.string.nearestStormDistance)+" : "+itemJSONObject.getString("nearestStormDistance")); 
				 else
					 holder.tvNearstStormDistance.setText(null); 
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
    /*
     * Convert speed in Miles to Km
     */
	private String getSpeedInKilloMeter(String speedInMiles){
		return String.format("%.2f", (Float.parseFloat(speedInMiles) *1.6));
	}
	
    /*
     * Get Humidity in percent
     */
	private String getHumidityInpercent(String humidity){
		return Float.toString(Float.parseFloat(humidity) *100);
	}
	
    /*
     * Converts to Celcius
     */
	private String convertFahrenheitToCelcius(String fahrenheit) {
		return String.format("%.2f", (Float.parseFloat(fahrenheit) - 32) * 5 / 9);
	}
	
    /*
     * Get String Date and Time
     */
	private String getDateAndTime(String timeInSeconds){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(timeInSeconds)*1000L);
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		return formatter.format(calendar.getTime());
	}
    /*
     * 	Get wheather Icos
     */
	private Bitmap getWeatherIcon(Context context, String iconType){
		if(iconType.equalsIgnoreCase("clear-day")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_clear_day);
		}
		else if(iconType.equalsIgnoreCase("clear-night")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_clear_night);
		}
		else if(iconType.equalsIgnoreCase("rain")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_rain);		
		}
		else if(iconType.equalsIgnoreCase("snow")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_snow);
		}
		else if(iconType.equalsIgnoreCase("sleet")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_sleet);
		}
		else if(iconType.equalsIgnoreCase("wind")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_wind);
		}
		else if(iconType.equalsIgnoreCase("fog")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_fog);
		}
		else if(iconType.equalsIgnoreCase("cloudy")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cloudy);
		}
		else if(iconType.equalsIgnoreCase("partly-cloudy-day")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_partly_cloudy);
		}
		else if(iconType.equalsIgnoreCase("partly-cloudy-night")){
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_partly_cloudy_night);
		}
		return null;
	}

    /*
     * 	Get weather background color
     */
	private int getWeatherBackgroundGradient(String iconType){
		if(iconType.equalsIgnoreCase("clear-day")){
			return R.drawable.gradient_background_clear_day;
		}
		else if(iconType.equalsIgnoreCase("clear-night")){
			return R.drawable.gradient_background_clear_night;
		}
		else if(iconType.equalsIgnoreCase("rain")){
			return R.drawable.gradient_background_cloudy;		
		}
		else if(iconType.equalsIgnoreCase("snow")){
			return R.drawable.gradient_background_snow;
		}
		else if(iconType.equalsIgnoreCase("sleet")){
			return R.drawable.gradient_background_sleet;
		}
		else if(iconType.equalsIgnoreCase("wind")){
			return R.drawable.gradient_background_default;
		}
		else if(iconType.equalsIgnoreCase("fog")){
			return R.drawable.gradient_background_fog;
		}
		else if(iconType.equalsIgnoreCase("cloudy")){
			return R.drawable.gradient_background_cloudy;
		}
		else if(iconType.equalsIgnoreCase("partly-cloudy-day")){
			return R.drawable.gradient_background_cloudy;
		}
		else if(iconType.equalsIgnoreCase("partly-cloudy-night")){
			return R.drawable.gradient_background_cloudy_night;
		}
		return 0;
	}

    /*
     * 	This method returns the number of items present in the list
     */
	@Override
	public int getItemCount() {
		return jsonArrayWeatherdata.length();
	}

	@Override
	public int getItemViewType(int position) { 
		return 0;
	}
}