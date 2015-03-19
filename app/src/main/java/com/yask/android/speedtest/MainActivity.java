package com.yask.android.speedtest;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends ListActivity {

    private static String fileURL = "http://web.mit.edu/21w.789/www/papers/griswold2004.pdf";
    private static String newfileURL = "http://www.theinquirer.net/IMG/726/297726/apple-watch-sports-band-white-540x334.png?1410346517";
    private static String longfileURL = "http://2012books.lardbucket.org/pdfs/principles-of-general-chemistry-v1.0.pdf";
    protected ArrayList<String> speedListItems = new ArrayList<String>();
    private ListView mainList;
    private TextView latencyTextView;
    ArrayAdapter<String> adapter;
    int bytesRead = 0;
    int prebytesRead = 0;
    int nextSecond = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,speedListItems);
        mainList = (ListView) findViewById(android.R.id.list);
        setListAdapter(adapter);

        Button downloadButton = (Button) findViewById(R.id.button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownLoadTask().execute(fileURL);
            }
        });

        Button clearButton = (Button) findViewById(R.id.button_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedListItems.clear();
                adapter.notifyDataSetChanged();
                latencyTextView.setText("Latency");
            }
        });

        latencyTextView = (TextView) findViewById(R.id.text_latency);

    }

    private class DownLoadTask extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                Log.d("speedtest", "startDownloading");
                URL url = new URL(sUrl[0]);
                long zeroTime = System.currentTimeMillis();
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
                final int fileLength = connection.getContentLength();



                // download the file
                input = connection.getInputStream();



                byte data[] = new byte[512];
                int count = 0;

                long connectTime = System.currentTimeMillis();
                Log.d("latency",String.valueOf(connectTime-zeroTime));
                publishProgress("Latency: " + String.valueOf(connectTime - zeroTime) + " ms");

                final Handler handler= new Handler(Looper.getMainLooper());
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Log.d("timer", String.valueOf(nextSecond));
                        if (bytesRead < fileLength){
                            handler.postDelayed(this,1000);
                            int downloadSize = bytesRead - prebytesRead;
                            Log.d("download", String.valueOf(downloadSize));
                            String update = String.format("Second: %d Throughput: %.3f kb/second",nextSecond,((float)downloadSize)/1000);
                            speedListItems.add(update);
                            adapter.notifyDataSetChanged();
                            mainList.setSelection(speedListItems.size()-1);
                            nextSecond += 1;
                            prebytesRead = bytesRead;
                        } else {
                            bytesRead = 0;
                            prebytesRead = 0;
                            nextSecond = 1;
                        }
                    }
                };
                handler.postDelayed(r,1000);
                long startTime = System.currentTimeMillis();
                Log.d("delay", String.valueOf(startTime - connectTime));

                while ((count = input.read(data)) != -1) {
                    bytesRead += count;
                }

                long endTime = System.currentTimeMillis();
                int downloadSize = bytesRead - prebytesRead;
                long totalTime = endTime - startTime;
                long timeLapse = totalTime - 1000*(nextSecond-1);

                Log.d("speed total time", String.valueOf(endTime - startTime));
                Log.d("speed download size", String.valueOf(downloadSize));
                input.close();
                if (timeLapse > 0){
                    return String.format("Second: %d Throughput: %.3f kb/second",nextSecond,((float)downloadSize)/timeLapse);
                }
                return null;
            } catch (Exception e) {
                Log.e("error",e.getLocalizedMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null){
                Log.d("speedtest", "something wrong");
            } else {
                speedListItems.add(s);
                speedListItems.add("Done");
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onProgressUpdate(String... update) {
            latencyTextView.setText(update[0]);
        }
    }
}
