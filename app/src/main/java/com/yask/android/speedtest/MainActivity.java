package com.yask.android.speedtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    private static String fileURL = "http://web.mit.edu/21w.789/www/papers/griswold2004.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_download){
            new DownLoadTask().execute(fileURL);
        }

        return super.onOptionsItemSelected(item);
    }


    private class DownLoadTask extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            HttpURLConnection connection = null;
            try {
                Log.d("speedtest", "startDownloading");
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                byte[] fileBlob = new byte[fileLength];


                // download the file
                input = connection.getInputStream();

                int bytesRead = 0;

                long startTime = System.currentTimeMillis();
                while (bytesRead < fileLength) {
                    int n = input.read(fileBlob, bytesRead, fileLength - bytesRead);
                    if (n <= 0) {
                        return null;
                    }

                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    bytesRead += n;
                    Log.d("speedTest progress", String.valueOf(bytesRead));
                }
                long endTime = System.currentTimeMillis();
                Log.d("speedTest download time", String.valueOf(endTime-startTime));
                input.close();
                return String.valueOf(bytesRead);
            } catch (Exception e) {
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null){
                Log.d("speedtest", "something wrong");
            } else {
                Log.d("speedtest result", s);
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
