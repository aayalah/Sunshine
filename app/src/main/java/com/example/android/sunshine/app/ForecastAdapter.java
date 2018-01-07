package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

import org.w3c.dom.Text;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mUseToday = false;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseToday) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public void setUseTodayLayout(boolean useToday) {
        mUseToday = useToday;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        } else if (viewType == VIEW_TYPE_FUTURE_DAY){
            layoutId = R.layout.list_item_forecast;
        }




        // TODO: Determine layoutId from viewType
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        holder.descView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        holder.iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        holder.highView = (TextView) view.findViewById(R.id.list_item_high_textview);
        holder.lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
        view.setTag(holder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder holder = (ViewHolder) view.getTag();

        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int viewType = getItemViewType(cursor.getPosition());
        if (viewType == VIEW_TYPE_TODAY) {
            holder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        } else if(viewType == VIEW_TYPE_FUTURE_DAY) {
            holder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
        }

        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        String dateString = Utility.getFriendlyDayString(context, date);
        holder.dateView.setText(dateString);

        String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        holder.descView.setText(desc);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        holder.highView.setText(Utility.formatTemperature(context, high, isMetric));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.lowView.setText(Utility.formatTemperature(context, low, isMetric));

    }

    public static class ViewHolder {
        ImageView iconView;
        TextView dateView;
        TextView descView;
        TextView highView;
        TextView lowView;
    }

}