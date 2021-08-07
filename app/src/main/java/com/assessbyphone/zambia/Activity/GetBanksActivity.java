package com.assessbyphone.zambia.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.assessbyphone.zambia.CallbackUtils.Defaults;
import com.assessbyphone.zambia.Models.MasterCSVModel;
import com.assessbyphone.zambia.R;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GetBanksActivity extends Activity {
    public static String TAG = "PhonicsByPhone";
    public static String PREFS_RESPONSE_KEY = "CachedBanks";
    public static SharedPreferences sPrefs;
    private CardView mProgressBar;
    private TextView mTextView;
    private Button mTryAgainBtn;
    private Button mProceedBtn;
    private String assessmentName;
    private File myFile;

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("", Context.MODE_PRIVATE);
        myFile = new File(directory, "");

//        if (!myFile.exists()) {
//            Log.d("path", myFile.toString());
//        }

        if (GetMasterTemplateActivity.sTemplate == null) {
            finish();
            return;
        }

        sPrefs = getPreferences(MODE_PRIVATE);

        if (GetMasterTemplateActivity.sTemplate != null && GetMasterTemplateActivity.sTemplate.frames != null) {
            for (MasterCSVModel.OneFrame frame : GetMasterTemplateActivity.sTemplate.frames) {
                frame.data.clear();
            }
        }

        setContentView(R.layout.get_banks);

        assessmentName = getIntent().getStringExtra("assessmentName");

        mProgressBar = (CardView) findViewById(R.id.blankCardView_ids);
        mTextView = (TextView) findViewById(R.id.progress_text);
        mTryAgainBtn = (Button) findViewById(R.id.try_get_banks_again_btn);
        mProceedBtn = (Button) findViewById(R.id.proceed_with_cached_banks_btn);

        // TODO: testing disabling hide proceed button logic
//		if (sPrefs.contains(GetMasterTemplateActivity.sTemplate.dataBankFilename)) {

        mProceedBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    new GetBanks(getApplicationContext()).execute(false);
                } catch (Exception ex) {
                    Log.e(GetBanksActivity.TAG, ex.getMessage());
                }
            }
        });

        getBanksDataFromServer();
    }

    public final void loadDataBankCSV(BufferedReader reader) throws Exception {
        CSVReader csvReader = null;

        try {
            csvReader = new CSVReader(reader);
            String[] line;

            // Load headers (column names) and form a map based on them
            String columnNamesStr = reader.readLine();
            ArrayList<String> columnNamesMap = new ArrayList<String>();
            HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
            for (String columnName : columnNamesStr.split(",")) {
                map.put(columnName, new ArrayList<String>());
                columnNamesMap.add(columnName);
            }

            // Load the data banks data into the map's array with the key that its corresponding column name
            while ((line = csvReader.readNext()) != null) {
                for (int i = 0; i < line.length; i++) {
                    if (!line[i].equals("")) {
                        line[i] = line[i].replace("¨", "'"); // weird apostrophe showing as diamond with ? in it hack
                        line[i] = line[i].replace("�", "'"); // weird apostrophe showing as diamond with ? in it hack
                        map.get(columnNamesMap.get(i)).add(line[i]);
                    }
                }
            }

            if (GetMasterTemplateActivity.sTemplate.isStory) {
                // is story holder
                for (int j = 0; j < GetMasterTemplateActivity.sTemplate.frames.size(); j++) {
                    MasterCSVModel.OneFrame frame = GetMasterTemplateActivity.sTemplate.frames.get(j);
                    frame.data = map.get(frame.question); // frame.question is the language. map contains this language's translations for each page
                    //for (int k = 0; k < frame.dataBankColumnNames.size(); k++) {
                    //}
                }
            } else {
                // is normal assessment
                for (int j = 0; j < GetMasterTemplateActivity.sTemplate.frames.size(); j++) {
                    MasterCSVModel.OneFrame frame = GetMasterTemplateActivity.sTemplate.frames.get(j);
                    for (int k = 0; k < frame.dataBankColumnNames.size(); k++) {
                        String columnName = frame.dataBankColumnNames.get(k);
                        //d("columnName: " + columnName);
                        Random randomGenerator = new Random();
                        int rndIdx = randomGenerator.nextInt(map.get(columnName).size());
                        frame.data.add(map.get(columnName).get(rndIdx));

                        // Get matching comprehension questions
                        if (frame.isComprehension) {
                            frame = GetMasterTemplateActivity.sTemplate.frames.get(++j);
                            for (; k < frame.dataBankColumnNames.size(); k++) {
                                columnName = frame.dataBankColumnNames.get(k);
                                try {
                                    frame.data.add(map.get(columnName).get(rndIdx));
                                } catch (IndexOutOfBoundsException e) {
                                    // Skip
                                }
                            }
                        }
                    }
                }
            }
            d("done");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to parse data bank " + GetMasterTemplateActivity.sTemplate.dataBankFilename);
        } finally {
            if (csvReader != null)
                try {
                    csvReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw (e);
                }
        }
    }

    void getBanksDataFromServer() {
        if (!isNetworkAvailable()) {
            warningOffline();

            mProgressBar.setVisibility(View.INVISIBLE);
            mTextView.setText("Not connected to the internet.");
            mTryAgainBtn.setVisibility(View.VISIBLE);
            mProceedBtn.setVisibility(View.VISIBLE);
            mTryAgainBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mTryAgainBtn.setVisibility(View.INVISIBLE);
                    mProceedBtn.setVisibility(View.INVISIBLE);
                    mTextView.setText(getString(R.string.getting_template));
                    getBanksDataFromServer();
                }
            });

        } else {
            new GetBanks(getApplicationContext()).execute(true);
        }
    }

    void nextActivity() {
        Intent myIntent = new Intent(this, MenuActivity.class);
        myIntent.putExtra("assessmentName", assessmentName);
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void warningOffline() {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("It seems you are not connected to the internet. You can proceed if you have a saved copy already downloaded.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @SuppressLint("StaticFieldLeak")
    public class GetBanks extends AsyncTask {
        public GetBanks(Context context) {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            String resultStr = "";
            if (!(result instanceof String)) {
                resultStr = "Non-string Object result error";
            } else {
                resultStr = (String) result;
            }

            mProgressBar.setVisibility(View.INVISIBLE);
            if ("done".equals(resultStr)) {
                nextActivity();
            } else {
                mTextView.setText(resultStr);
                mTryAgainBtn.setVisibility(View.VISIBLE);
                mProceedBtn.setVisibility(View.VISIBLE);
                mTryAgainBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTryAgainBtn.setVisibility(View.INVISIBLE);
                        mProceedBtn.setVisibility(View.INVISIBLE);
                        mTextView.setText(getString(R.string.getting_banks));
                        getBanksDataFromServer();
                    }
                });
            }
        }

        @Override
        protected Object doInBackground(Object... params) {
            boolean doDownload = (Boolean) params[0];

            String masterString = "";
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {

                ////
                // Secondly, get the data bank csv with all actual data
                ////

				/*
				// Create a new HTTP Client
				DefaultHttpClient defaultClient = new DefaultHttpClient();
				// Setup the get request
				HttpGet httpGetRequest = new HttpGet(Defaults.getBaseUrl(getApplicationContext()) + GetMasterTemplateActivity.sTemplate.dataBankFilename);

				// Execute the request in the client
				HttpResponse httpResponse = defaultClient.execute(httpGetRequest);
				if (httpResponse.getStatusLine().getStatusCode() != 200) {
					Log.e(TAG, "data bank " + GetMasterTemplateActivity.sTemplate.dataBankFilename + " get error: " + httpResponse.getStatusLine().toString());
					throw new Exception("Data bank " + GetMasterTemplateActivity.sTemplate.dataBankFilename + " get error: " + httpResponse.getStatusLine().toString());
				}
				*/
                if (doDownload) {
                    String dataUrl = Defaults.getBaseUrl(getApplicationContext()) + GetMasterTemplateActivity.sTemplate.dataBankFilename;
                    URL url = new URL(dataUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly update error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        // return "Server returned HTTP " + connection.getResponseCode()
                        //        + " " + connection.getResponseMessage();
                        return "Error HTTP response code: " + connection.getResponseCode();
                    }

                    // download the file
                    input = connection.getInputStream();
                    // output = new FileOutputStream("/sdcard/" + GetMasterTemplateActivity.sTemplate.dataBankFilename);
                    output = new FileOutputStream(myFile + GetMasterTemplateActivity.sTemplate.dataBankFilename);

                    byte[] data = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return "Download error/cancelled";
                        }
                        output.write(data, 0, count);
                    }
                }  // Assumes has saved copy


                // Grab the response
                // BufferedReader reader = new BufferedReader(new FileReader("/sdcard/" + GetMasterTemplateActivity.sTemplate.dataBankFilename));
                BufferedReader reader = new BufferedReader(new FileReader(myFile + GetMasterTemplateActivity.sTemplate.dataBankFilename));
                loadDataBankCSV(reader);
                d("doneLoadDataBankCSV");
                sPrefs.edit().putString(GetMasterTemplateActivity.sTemplate.dataBankFilename, GetMasterTemplateActivity.sTemplate.dataBankFilename).apply();
            } catch (Exception ex) {
                if (masterString.toLowerCase().contains("timeout")) {
                    Log.e(GetBanksActivity.TAG, ex.getMessage());
                    return getString(R.string.err_timeout);
                } else {
                    Log.e(GetBanksActivity.TAG, ex.getMessage());
                    return ex.getMessage();
                }
            }
            return "done";
        }
    }
}
