package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

import org.w3c.dom.Text;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int WEATHER_ITEM_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_PRESSURE = 6;
    static final int COL_WEATHER_WIND = 7;
    static final int COL_WEATHER_DEGREES = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;


    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private Uri mForecastURI;
    private String mForecast;
    private ShareActionProvider mShareActionProvider;


    private TextView dayView;
    private TextView highView;
    private TextView lowView;
    private TextView descView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;
    private ImageView iconView;
    private TextView dateView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(Uri uri) {
        DetailFragment f = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("data", uri.toString());
        f.setArguments(args);
        return f;
    }

    public String getShownData() {
        if (this.getArguments() != null) {
            return this.getArguments().getString("data");
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        dateView = (TextView) rootView.findViewById(R.id.fragment_detail_date_textview);
        dayView = (TextView) rootView.findViewById(R.id.fragment_detail_day_textview);
        highView = (TextView) rootView.findViewById(R.id.fragment_detail_high_textview);
        lowView = (TextView) rootView.findViewById(R.id.fragment_detail_low_textview);
        descView = (TextView) rootView.findViewById(R.id.fragment_detail_desc_textview);
        humidityView = (TextView) rootView.findViewById(R.id.fragment_detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.fragment_detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.fragment_detail_pressure_textview);
        iconView = (ImageView) rootView.findViewById(R.id.fragment_detail_icon_imageview);

        mForecastURI = getShownData() != null ? Uri.parse(getShownData()) : null;

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(WEATHER_ITEM_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    public Loader<Cursor> onCreateLoader (int id,
                                          Bundle args) {

        if (mForecastURI != null) {
            Loader<Cursor> loader = new CursorLoader(getActivity(), mForecastURI,DETAIL_COLUMNS, null,null,null);
            return loader;
        }
        return null;
    }

    public void onLoadFinished (Loader<Cursor> loader,
                                Cursor data){
        if(data.moveToFirst()) {

            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            long date = data.getLong(COL_WEATHER_DATE);
            String day = Utility.getDayName(getActivity(), date);
            String monthDay = Utility.getFormattedMonthDay(getActivity(), date);
            dayView.setText(day);
            dateView.setText(monthDay);

            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highString = Utility.formatTemperature(getActivity(), high, Utility.isMetric(getActivity()));
            highView.setText(highString);

            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowString = Utility.formatTemperature(getActivity(), low, Utility.isMetric(getActivity()));
            TextView lowView = (TextView) getActivity().findViewById(R.id.fragment_detail_low_textview);
            lowView.setText(lowString);

            String desc = data.getString(COL_WEATHER_DESC);
            descView.setText(desc);

            double humidity = data.getDouble(COL_WEATHER_HUMIDITY);
            String humidityString = String.format(getActivity().getString(R.string.format_humidity), humidity);
            humidityView.setText(humidityString);

            float wind = data.getFloat(COL_WEATHER_WIND);
            float degrees = data.getFloat(COL_WEATHER_DEGREES);
            String windString = Utility.getFormattedWind(getActivity(),wind, degrees);
            windView.setText(windString);

            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            String pressureString = String.format(getActivity().getString(R.string.format_pressure),pressure);
            pressureView.setText(pressureString);

            if(mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    public void onLoaderReset (Loader<Cursor> loader) {
        return;
    }


    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    public void onLocationChanged(String locationSetting) {
        long date = WeatherContract.WeatherEntry.getDateFromUri(mForecastURI);
        mForecastURI = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, date);
        getLoaderManager().restartLoader(WEATHER_ITEM_LOADER,null,null);
    }
}
