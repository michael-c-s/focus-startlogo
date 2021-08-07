package com.assessbyphone.zambia.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.assessbyphone.zambia.Adapter.ChooseAssessmentAdapter;
import com.assessbyphone.zambia.CallbackUtils.CustomProgressBar;
import com.assessbyphone.zambia.CallbackUtils.DefaultCallback;
import com.assessbyphone.zambia.CallbackUtils.Defaults;
import com.assessbyphone.zambia.Models.MasterCSVModel;
import com.assessbyphone.zambia.Models.SerializableManager;
import com.assessbyphone.zambia.Models.Students;
import com.assessbyphone.zambia.R;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.LoadRelationsQueryBuilder;
import com.flurry.android.FlurryAgent;
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
import java.util.List;

import static com.assessbyphone.zambia.CallbackUtils.UiUtil.getIsLogin;
import static com.assessbyphone.zambia.CallbackUtils.UiUtil.getPassword;
import static com.assessbyphone.zambia.CallbackUtils.UiUtil.getUserID;
import static com.assessbyphone.zambia.CallbackUtils.UiUtil.setIsAdmin;

public class GetMasterTemplateActivity extends Activity {
    //	public static String BASE_URL = "http://192.168.44.7/pbp/";
    //	public static String BASE_URL = "http://172.40.0.103/pbp/";
    static final int BLUETOOTH_ADMIN = 100;
    static final int BLUETOOTH = 101;
    static final int INTERNET = 102;
    static final int WRITE_EXTERNAL_STORAGE = 103;
    static final int READ_EXTERNAL_STORAGE = 104;
    static final int ACCESS_NETWORK_STATE = 105;
    //static final int ACCESS_FINE_LOCATION = 106;
    public static String TAG = "PhonicsByPhone";
    public static String PREFS_RESPONSE_KEY = "MasterTemplateCsvResponse";
    public static SharedPreferences sPrefs;
    public static ArrayList<MasterCSVModel> sMasterCsvAssessments = new ArrayList<>();
    public static ArrayList<MasterCSVModel> sMasterCsvStories = new ArrayList<>();
    public static MasterCSVModel sTemplate = null;
    public static BackendlessUser myBackendlessUser;
    public static String sNews = "";
    private CardView mProgressBar;
    private TextView mTextView;
    private Button mTryAgainBtn;
    private Button mProceedBtn;
    private RelativeLayout templateLay;
    private LinearLayout chooseAssessmentLayout;
    private RecyclerView chooseAssessmentRecycler;
    private TextView versionNameTv;
    private ArrayList<String> assessmentALL;
    private LayoutInflater layoutInflater;
    private int width;
    private File myFile;
    private CustomProgressBar customProgressBar;
    private ImageView launchWebsiteIcon;

    public static void d(String msg) {
        //Log.d(TAG, msg);1
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        FlurryAgent.setLogEnabled(false);
        FlurryAgent.init(this, "RZ3TRJ4J88NG54R8MKY8");
        FlurryAgent.logEvent("Country", new HashMap<String, String>() {{
            put("Country", Defaults.getCountry(getApplicationContext()) + "");
        }});

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("", Context.MODE_PRIVATE);
        myFile = new File(directory, "master.csv");

        if (!myFile.exists()) {
            Log.d("path", myFile.toString());
        }

        sPrefs = getPreferences(MODE_PRIVATE);
        sMasterCsvAssessments.clear();
        sMasterCsvStories.clear();
        sTemplate = null;

        setContentView(R.layout.get_template);

        customProgressBar = new CustomProgressBar();

        boolean isLogin = sPrefs.getBoolean("isLogin", false);
        String isLog = getIsLogin(GetMasterTemplateActivity.this);
        Log.e("isLogin", String.valueOf(isLogin));
        Log.e("isMyLogin", isLog);

        initView();

        checkPermissions();
    }

    public void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this);

        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;

        launchWebsiteIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openHtmlFile();
            }
        });
    }

    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    private void initView() {
        templateLay = findViewById(R.id.templateLay_ids);
        launchWebsiteIcon = findViewById(R.id.launchWebsiteIcon_id);
        chooseAssessmentLayout = findViewById(R.id.chooseAssessmentLayout_ids);
        chooseAssessmentRecycler = findViewById(R.id.chooseAssessmentRecycler_ids);
        versionNameTv = findViewById(R.id.versionName);
        mProgressBar = findViewById(R.id.mainCardView_ids);
        mTextView = findViewById(R.id.progress_text);
        mTryAgainBtn = findViewById(R.id.try_get_template_again_btn);
        mProceedBtn = findViewById(R.id.proceed_with_cached_template_btn);

        chooseAssessmentRecycler.setLayoutManager(new LinearLayoutManager(GetMasterTemplateActivity.this, RecyclerView.VERTICAL, false));
        chooseAssessmentRecycler.hasFixedSize();

        try {
            String versionName = "Version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionNameTv.setText(versionName);
        } catch (PackageManager.NameNotFoundException nfe) {
            nfe.printStackTrace();
        }

        mProceedBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    new GetTemplate(getApplicationContext()).execute(false);
                } catch (Exception ex) {
                    Log.e(GetMasterTemplateActivity.TAG, ex.getMessage());
                }
            }
        });

        getTemplateFromServer();
    }

    void checkPermissions() {
        int num = 0;
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    BLUETOOTH_ADMIN);
            return;
        } else {
            // Permission has already been granted
            num++;
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH},
                    BLUETOOTH);
            return;
        } else {
            // Permission has already been granted
            num++;
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    INTERNET);
            return;
        } else {
            // Permission has already been granted
            num++;
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE);
            return;
        } else {
            // Permission has already been granted
            num++;
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE);
            return;
        } else {
            // Permission has already been granted
            num++;
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    ACCESS_NETWORK_STATE);
            return;
        } else {
            // Permission has already been granted
            num++;
        }

//        if (ContextCompat.checkSelfPermission(getApplicationContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    ACCESS_FINE_LOCATION);
//            return;
//        } else {
//            // Permission has already been granted
//            num++;
//        }

        initView();
        //OnReCreate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        checkPermissions();
    }

    void getTemplateFromServer() {
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
                    getTemplateFromServer();
                }
            });

        } else {
            new GetTemplate(getApplicationContext()).execute(true);
        }
    }

    void nextActivity(String assessmentName) {
        Intent myIntent = new Intent(this, ConnectBluetoothPrinterActivity.class);
        myIntent.putExtra("assessmentName", assessmentName);
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    void chooseAssessmentOrStory() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(GetMasterTemplateActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(getString(R.string.choose_assessmentOrStory));
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                GetMasterTemplateActivity.this,
                android.R.layout.select_dialog_singlechoice);

        arrayAdapter.add("Assessments");
        if (Defaults.getCountry(getApplicationContext()).equals(Defaults.COUNTRY.GHANA)) {
            arrayAdapter.add("Stories"); // only show stories in Ghana
        }

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            chooseAssessment();
                        } else {
                            chooseStory();
                        }
                    }
                });
        builderSingle.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                sMasterCsvAssessments.clear();
                sMasterCsvStories.clear();

                mTryAgainBtn.setVisibility(View.VISIBLE);
                mProceedBtn.setVisibility(View.VISIBLE);
                mTryAgainBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTryAgainBtn.setVisibility(View.INVISIBLE);
                        mProceedBtn.setVisibility(View.INVISIBLE);
                        mTextView.setText(getString(R.string.getting_template));
                        getTemplateFromServer();
                    }
                });
            }
        });
        try {
            builderSingle.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
            startActivity(new Intent(getBaseContext(), GetMasterTemplateActivity.class));
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
    }

    void chooseAssessment() {
        chooseAssessmentLayout.setVisibility(View.VISIBLE);
        templateLay.setVisibility(View.GONE);
        FlurryAgent.logEvent("chooseAssessment");
        assessmentALL = new ArrayList<>();
        assessmentALL.clear();

        int mCount = 1;
        if (!assessmentALL.contains(getString(R.string.admin)))
            assessmentALL.add(getString(R.string.admin));
        for (MasterCSVModel model : sMasterCsvAssessments) {
            if (model.assessmentName == null || model.assessmentName.equals(""))
                assessmentALL.add("Error assessment #" + mCount);
            else {
                if (!assessmentALL.contains(model.assessmentName))
                    assessmentALL.add(model.assessmentName);
            }

            mCount++;
        }

        ChooseAssessmentAdapter mAdapter = new ChooseAssessmentAdapter(assessmentALL);
        chooseAssessmentRecycler.setAdapter(mAdapter);
        mAdapter.onItemClicked(new ChooseAssessmentAdapter.onItemClickedInterface() {
            @Override
            public void getCurrentAssessment(String assessmentName, int position) {
                // openHtmlFile();
                if (!isNetworkAvailable()) {
                    Toast.makeText(GetMasterTemplateActivity.this, "Please Check internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    intentAction(assessmentName, position);
                }
            }
        });
    }

    private void openHtmlFile() {
        Uri uri2 = FileProvider.getUriForFile(GetMasterTemplateActivity.this, this.getApplicationContext().getPackageName() + ".provider", new File("/sdcard/my_html/launcher.html"));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri2);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.android.chrome", "com.google.android.apps.chrome.Main");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                intent.setPackage(null);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Exception", Toast.LENGTH_SHORT).show();
            }
        }

//        Intent i = new Intent("android.intent.action.View");
//        //i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
//        i.addCategory("android.intent.category.LAUNCHER");
//        i.setData(Uri.parse("/sdcard/Presentation with dot exe launcher file_new/launcher.html"));
//        startActivity(i);

//        Intent in = new Intent(Intent.ACTION_VIEW);
//        File f=new File("/sdcard/Presentation with dot exe launcher file_new/launcher.html");
//        in.setDataAndType(Uri.fromFile(f), "text/html");
//        startActivity(in);
    }

    private void intentAction(String assessmentName, int position) {
        if (getString(R.string.admin).equals(assessmentName)) {
            gotoAdmin();
        } else {
            sTemplate = sMasterCsvAssessments.get(position - 1); // -1 because PhonicsResults Admin is at index 0
            nextActivity(assessmentName);
        }
    }

    // get user data from login
    public void getUserDetails(String assessmentName, int position) {
        String identity = getUserID(GetMasterTemplateActivity.this);
        String password = getPassword(GetMasterTemplateActivity.this);
        customProgressBar.showMe(GetMasterTemplateActivity.this, "Please Wait...");
        Backendless.UserService.login(identity, password, new DefaultCallback<BackendlessUser>(GetMasterTemplateActivity.this) {
            public void handleResponse(BackendlessUser backendlessUser) {
                backendlessUser.setProperty("localPassword", password);

                if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("needSync_" + identity, false)) {
                    //Toast.makeText(getApplicationContext(), "There are offline changes to be saved on the server but this functionality hasn't been implemented yet!", Toast.LENGTH_LONG).show();
                } else {
                    if ("true".equals("" + backendlessUser.getProperty("isStatsAdmin"))) // 2018/09/08 started implementing a read only admin for people who just need to view stats of several classes/schools
                    {
                        new AsyncTask<Void, Void, List<BackendlessUser>>() {
                            @Override
                            protected void onPreExecute() {
                            }

                            @Override
                            protected List<BackendlessUser> doInBackground(Void... voids) {
                                try {
                                    // Get adminClasses
                                    LoadRelationsQueryBuilder<BackendlessUser> loadRelationsQueryBuilder;
                                    loadRelationsQueryBuilder = LoadRelationsQueryBuilder.of(BackendlessUser.class);
                                    loadRelationsQueryBuilder.setRelationName("adminClasses");
                                    loadRelationsQueryBuilder.setPageSize(100);
                                    final List<BackendlessUser> adminClasses = new ArrayList<BackendlessUser>();
                                    List<BackendlessUser> adminClassesRes = Backendless.Data.of(BackendlessUser.class).loadRelations(backendlessUser.getObjectId(), loadRelationsQueryBuilder);
                                    do {
                                        adminClasses.addAll(adminClassesRes);
                                        loadRelationsQueryBuilder = loadRelationsQueryBuilder.prepareNextPage();
                                        adminClassesRes = Backendless.Data.of(BackendlessUser.class).loadRelations(backendlessUser.getObjectId(), loadRelationsQueryBuilder);
                                    } while (adminClassesRes.size() >= 100);

                                    // Get each adminClasses' students
                                    final List<Students> allStudents = new ArrayList<Students>();
                                    for (BackendlessUser adminClass : adminClasses) {
                                        List<Students> students = new ArrayList<Students>();
                                        LoadRelationsQueryBuilder<Students> loadRelationsQueryBuilder2 = LoadRelationsQueryBuilder.of(Students.class);
                                        loadRelationsQueryBuilder2.setRelationName("students");
                                        loadRelationsQueryBuilder2.setPageSize(100);
                                        List<Students> studentsRes = Backendless.Data.of(BackendlessUser.class).loadRelations(adminClass.getObjectId(), loadRelationsQueryBuilder2);
                                        do {
                                            students.addAll(studentsRes);
                                            loadRelationsQueryBuilder2 = loadRelationsQueryBuilder2.prepareNextPage();
                                            studentsRes = Backendless.Data.of(BackendlessUser.class).loadRelations(adminClass.getObjectId(), loadRelationsQueryBuilder2);
                                        } while (studentsRes.size() >= 100);
                                        adminClass.setProperty("students", students);
                                        allStudents.addAll(students);
                                    }

                                    // Get all the results
                                    String whereQuery = "upn IN (";
                                    for (final Students student : allStudents) {
                                        whereQuery += "'" + student.getUpn() + "',";
                                    }
                                    whereQuery = whereQuery.substring(0, whereQuery.length() - 1) + ")";
                                    Log.e("PGP", "whereQuery: " + whereQuery);
                                    DataQueryBuilder resultsQuery = DataQueryBuilder.create();
                                    resultsQuery.setWhereClause(whereQuery);
                                    resultsQuery.setPageSize(100);
                                    resultsQuery.setRelated("phonicsResults");
                                    List<Students> studentsWithResults = Backendless.Data.of(Students.class).find(resultsQuery);
                                    do {
                                        for (Students studentWithResults : studentsWithResults) {
                                            for (Students student : allStudents) {
                                                if (student.getObjectId().equals(studentWithResults.getObjectId())) {
                                                    student.setResults(studentWithResults.getResults());
                                                }
                                            }
                                        }
                                        resultsQuery = resultsQuery.prepareNextPage();
                                        studentsWithResults = Backendless.Data.of(Students.class).find(resultsQuery);
                                    } while (studentsWithResults.size() >= 100);
                                    for (Students studentWithResults : studentsWithResults) {
                                        for (Students student : allStudents) {
                                            if (student.getObjectId().equals(studentWithResults.getObjectId())) {
                                                student.setResults(studentWithResults.getResults());
                                            }
                                        }
                                    }

                                    // Get all the maths results
                                    String mathsWhereQuery = "upn IN (";
                                    for (final Students student : allStudents) {
                                        mathsWhereQuery += "'" + student.getUpn() + "',";
                                    }
                                    mathsWhereQuery = mathsWhereQuery.substring(0, mathsWhereQuery.length() - 1) + ")";
                                    Log.e("PGP", "mathsWhereQuery: " + mathsWhereQuery);
                                    DataQueryBuilder mathsResultsQuery = DataQueryBuilder.create();
                                    mathsResultsQuery.setWhereClause(mathsWhereQuery);
                                    mathsResultsQuery.setPageSize(100);
                                    mathsResultsQuery.setRelated("mathsResults");
                                    List<Students> studentsWithMathsResults = Backendless.Data.of(Students.class).find(mathsResultsQuery);
                                    do {
                                        for (Students studentWithMathsResults : studentsWithMathsResults) {
                                            for (Students student : allStudents) {
                                                if (student.getObjectId().equals(studentWithMathsResults.getObjectId())) {
                                                    student.setMathsResults(studentWithMathsResults.getMathsResults());
                                                }
                                            }
                                        }
                                        mathsResultsQuery = mathsResultsQuery.prepareNextPage();
                                        studentsWithMathsResults = Backendless.Data.of(Students.class).find(mathsResultsQuery);
                                    } while (studentsWithMathsResults.size() >= 100);
                                    for (Students studentWithMathsResults : studentsWithMathsResults) {
                                        for (Students student : allStudents) {
                                            if (student.getObjectId().equals(studentWithMathsResults.getObjectId())) {
                                                student.setMathsResults(studentWithMathsResults.getMathsResults());
                                            }
                                        }
                                    }

                                    return adminClasses;
                                } catch (Exception e) {
                                    customProgressBar.dismissMe();
                                    final Exception e2 = e;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Toast.makeText(getApplicationContext(), "Unable to get admin's classes with students and results: " + e2.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Log.e("PBP", "Unable to get admin's classes with students and results - " + e.getMessage());
                                }
                                return null;
                            }

                            @SuppressLint("StaticFieldLeak")
                            @Override
                            protected void onPostExecute(List<BackendlessUser> adminClasses) {
                                customProgressBar.dismissMe();
                                if (adminClasses != null) {
                                    backendlessUser.setProperty("adminClasses", adminClasses);
                                    myBackendlessUser = backendlessUser;
                                    setIsAdmin(GetMasterTemplateActivity.this, true);
                                    intentAction(assessmentName, position);
                                }
                            }

                        }.execute();

                    } else {
                        LoadRelationsQueryBuilder<Students> loadRelationsQueryBuilder;
                        loadRelationsQueryBuilder = LoadRelationsQueryBuilder.of(Students.class);
                        loadRelationsQueryBuilder.setRelationName("students");
                        loadRelationsQueryBuilder.setPageSize(100);

                        Backendless.Data.of(BackendlessUser.class).loadRelations(backendlessUser.getObjectId(),
                                loadRelationsQueryBuilder,
                                new AsyncCallback<List<Students>>() {
                                    @Override
                                    public void handleResponse(final List<Students> students) {
                                        if (students != null && students.size() > 0) {
                                            StringBuilder whereQuery = new StringBuilder("upn IN (");
                                            for (final Students student : students) {
                                                whereQuery.append("'").append(student.getUpn()).append("',");
                                            }
                                            whereQuery = new StringBuilder(whereQuery.substring(0, whereQuery.length() - 1) + ")");
                                            DataQueryBuilder resultsQuery = DataQueryBuilder.create();
                                            resultsQuery.setWhereClause(whereQuery.toString());
                                            resultsQuery.setPageSize(100);
                                            resultsQuery.setRelated(new ArrayList<String>() {{
                                                add("phonicsResults");
                                                add("mathsResults");
                                            }});
                                            Backendless.Data.of(Students.class).find(resultsQuery, new AsyncCallback<List<Students>>() {
                                                        @Override
                                                        public void handleResponse(List<Students> response) {
                                                            students.clear();
                                                            students.addAll(response);
                                                            backendlessUser.setProperty("students", students);
                                                            SerializableManager.saveSerializable(getBaseContext(), backendlessUser, "backendlessUser_" + identity);
                                                            customProgressBar.dismissMe();
                                                            myBackendlessUser = backendlessUser;
                                                            setIsAdmin(GetMasterTemplateActivity.this, false);
                                                            intentAction(assessmentName, position);
                                                        }

                                                        @Override
                                                        public void handleFault(BackendlessFault fault) {
                                                        }
                                                    }
                                            );
                                        } else {
                                            customProgressBar.dismissMe();
                                            if (students != null) {
                                                students.clear();
                                                backendlessUser.setProperty("students", students);  // students is zero
                                            }
                                            SerializableManager.saveSerializable(getBaseContext(), backendlessUser, "backendlessUser_" + identity);
                                            myBackendlessUser = backendlessUser;
                                            setIsAdmin(GetMasterTemplateActivity.this, false);
                                        }
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        customProgressBar.dismissMe();
                                    }
                                }
                        );
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                customProgressBar.dismissMe();
            }
        });
    }
    //end

    void chooseAssessmentOLD() {
        FlurryAgent.logEvent("chooseAssessment");
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                GetMasterTemplateActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(getString(R.string.choose_assessment));

        assessmentALL = new ArrayList<>();

        final ArrayAdapter<String> assessment = new ArrayAdapter<String>(
                GetMasterTemplateActivity.this,
                android.R.layout.select_dialog_singlechoice);

        int i = 1;
        assessmentALL.add(getString(R.string.admin));
        assessment.add(getString(R.string.admin));
        for (MasterCSVModel model : sMasterCsvAssessments) {
            if (model.assessmentName.equals("") || model.assessmentName == null) {
                assessment.add("Error assessment #" + i);
                assessmentALL.add("Error assessment #" + i);
            } else {
                assessment.add(model.assessmentName);
                assessmentALL.add(model.assessmentName);
            }
            i++;
        }

        builderSingle.setAdapter(assessment,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        d("selected assessment: " + assessment.getItem(which));
                        if (getString(R.string.admin).equals(assessment.getItem(which))) {
                            gotoAdmin();
                            return;
                        }
                        sTemplate = sMasterCsvAssessments.get(which - 1); // -1 because PhonicsResults Admin is at index 0
                        nextActivity(assessment.getItem(which));
                    }
                });
        builderSingle.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                sMasterCsvAssessments.clear();
                sMasterCsvStories.clear();

                mTryAgainBtn.setVisibility(View.VISIBLE);
                mProceedBtn.setVisibility(View.VISIBLE);
                mTryAgainBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTryAgainBtn.setVisibility(View.INVISIBLE);
                        mProceedBtn.setVisibility(View.INVISIBLE);
                        mTextView.setText(getString(R.string.getting_template));
                        getTemplateFromServer();
                    }
                });
            }
        });
        builderSingle.show();
    }

    void gotoAdmin() {
        FlurryAgent.logEvent("gotoAdmin");
        Intent myIntent = new Intent(GetMasterTemplateActivity.this, LoginActivity.class);
        //Intent myIntent = new Intent(GetMasterTemplateActivity.this, AdminActivity.class);
        myIntent.putExtra("admin", "yes");
        startActivity(myIntent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    void chooseStory() {
        FlurryAgent.logEvent("chooseStory");
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                GetMasterTemplateActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(getString(R.string.choose_story));
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                GetMasterTemplateActivity.this,
                android.R.layout.select_dialog_singlechoice);

        int i = 1;
        for (MasterCSVModel model : sMasterCsvStories) {
            if (model.assessmentName.equals("") || model.assessmentName == null)
                arrayAdapter.add("Error story #" + i);
            else
                arrayAdapter.add(model.assessmentName);
            i++;
        }

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        d("selected assessment: " + arrayAdapter.getItem(which));
                        sTemplate = sMasterCsvStories.get(which);
                        nextActivity(arrayAdapter.getItem(which));
                    }
                });
        builderSingle.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                sMasterCsvAssessments.clear();
                sMasterCsvStories.clear();

                mTryAgainBtn.setVisibility(View.VISIBLE);
                mProceedBtn.setVisibility(View.VISIBLE);
                mTryAgainBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTryAgainBtn.setVisibility(View.INVISIBLE);
                        mProceedBtn.setVisibility(View.INVISIBLE);
                        mTextView.setText(getString(R.string.getting_template));
                        getTemplateFromServer();
                    }
                });
            }
        });
        builderSingle.show();
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
        exitDialogAlert();
    }

    private void exitDialogAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GetMasterTemplateActivity.this);
        alertDialogBuilder.setCancelable(false);
        View mainView = layoutInflater.inflate(R.layout.logout_dialog_design, null, false);
        alertDialogBuilder.setView(mainView);

        TextView msgTv = mainView.findViewById(R.id.msgTv_ids);
        TextView cancelTv = mainView.findViewById(R.id.cancelTv_ids);
        TextView okTv = mainView.findViewById(R.id.okTv_ids);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertDialog.show();
        alertDialog.getWindow().setLayout((int) (width * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);

        msgTv.setText("Are you sure, you want to exit?");

        cancelTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        okTv.setOnClickListener(new OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                System.runFinalizersOnExit(true);
                finishAndRemoveTask();
                GetMasterTemplateActivity.super.finishAffinity();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public class GetTemplate extends AsyncTask {
        public GetTemplate(Context context) {
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
            if ("done".equals(resultStr) && Defaults.getCountry(getApplicationContext()).equals(Defaults.COUNTRY.GHANA)) {
                chooseAssessmentOrStory();
            } else if ("done".equals(resultStr)) {
                chooseAssessment();
            } else {
                mTextView.setText(resultStr);
                mTryAgainBtn.setVisibility(View.VISIBLE);
                mProceedBtn.setVisibility(View.VISIBLE);
                mTryAgainBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTryAgainBtn.setVisibility(View.INVISIBLE);
                        mProceedBtn.setVisibility(View.INVISIBLE);
                        mTextView.setText(getString(R.string.getting_template));
                        getTemplateFromServer();
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
                // First get the master.csv with all assessment name's and their corresponding templates
                ////

                /*
				// Create a new HTTP Client
				DefaultHttpClient defaultClient = new DefaultHttpClient();
				// Setup the get request
				HttpGet httpGetRequest = new HttpGet(BASE_URL + "master.csv");

				// Execute the request in the client
				HttpResponse httpResponse = defaultClient.execute(httpGetRequest);
				if (httpResponse.getStatusLine().getStatusCode() != 200) {
					Log.e(TAG, "master csv get error: " + httpResponse.getStatusLine().toString());
					throw new Exception("Master CSV get error: " + httpResponse.getStatusLine().toString());
				}

				// Grab the response
				BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
				masterString = reader.readLine();
				*/

                if (doDownload) {
                    String surl = Defaults.getBaseUrl(getApplicationContext()) + "master.csv";
                    URL url = new URL(surl);
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
                    //output = new FileOutputStream("/sdcard/master.csv");
                    output = new FileOutputStream(myFile);

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

                //BufferedReader reader = new BufferedReader(new FileReader("/sdcard/master.csv"));
                BufferedReader reader = new BufferedReader(new FileReader(myFile));
                masterString = reader.readLine();

                // Extract the latest news, remove the "" at beginning and end
                String secondLine = reader.readLine();
                sNews = secondLine.split(",")[0];
                if (sNews != null && sNews.charAt(0) == '"')
                    sNews = sNews.substring(1, sNews.length() - 1);
                if (sNews != null && sNews.charAt(sNews.length() - 1) == '"')
                    sNews = sNews.substring(0, sNews.length() - 2);

                processMasterCsv(reader);
                d("doneProcessMasterCSV");
                sPrefs.edit().putString(PREFS_RESPONSE_KEY, masterString).apply();
            } catch (Exception ex) {
                if (masterString.toLowerCase().contains("timeout")) {
                    Log.e(GetMasterTemplateActivity.TAG, ex.getMessage());
                    return getString(R.string.err_timeout);
                } else {
                    Log.e(GetMasterTemplateActivity.TAG, ex.getMessage());
                    return ex.getMessage();
                }
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }

            return "done";
        }


        void processMasterCsv(BufferedReader reader) throws Exception {
            CSVReader csvReader = null;
            try {
                csvReader = new CSVReader(reader);
                String[] line;
                ArrayList<String[]> contents = new ArrayList<String[]>();

                int lineNumber = 0;
                while ((line = csvReader.readNext()) != null) {
                    contents.add(line);

                    // Stories are a new addition to print out translations of stories in big fonts for multiple languages
                    boolean isStory = line[0].toLowerCase().startsWith("story:");

                    MasterCSVModel model = new MasterCSVModel();

                    if (line[0].toLowerCase().startsWith("term_locks")) {
                        MasterCSVModel.term1Locked = Boolean.parseBoolean(line[2]);
                        MasterCSVModel.term2Locked = Boolean.parseBoolean(line[4]);
                        MasterCSVModel.term3Locked = Boolean.parseBoolean(line[6]);
                    } else if (line[0].toLowerCase().startsWith("assessment:") || isStory) {
                        // Get assessment name
                        String[] assessmentNameArray = line[0].split(":");
                        String assessmentName = assessmentNameArray[1];
                        d("assessment name is " + assessmentName);

                        model.assessmentName = assessmentName;
                        model.isStory = isStory;
                        // Get questions/problem instructions
                        for (int i = 1; i < line.length; i++) {
                            MasterCSVModel.OneFrame newFrame = new MasterCSVModel.OneFrame();
                            newFrame.assessmentName = model.assessmentName;
                            newFrame.question = line[i];
                            if ("".equals(line[i]))
                                continue;
                            d("new question: " + newFrame.question);
                            model.frames.add(newFrame);
                        }

                        while ((line = csvReader.readNext()) != null) {
                            contents.add(line);

                            // Get data bank's file name (the next CSV file we process)
                            if (model.dataBankFilename == null && !line[0].equals("") && line[0] != null && line[0].toLowerCase().contains("csv")) {
                                d("data bank filename: " + line[0]);
                                model.dataBankFilename = line[0];
                            } else
                                // Check for language (if nothing specified, default is English)
                                if (line[0].toLowerCase().startsWith("language:")) {
                                    model.language = line[0].substring(line[0].indexOf(":") + 1);
                                    d("data bank language: " + model.language);
                                }
                            // Check for next assessment (model)
                            if (line[0].toLowerCase().startsWith("end")) {
                                d("breaking current assessment");
                                if (model.isStory)
                                    sMasterCsvStories.add(model);
                                else
                                    sMasterCsvAssessments.add(model);
                                break;
                            }

                            // Now get all the column names which directly reference column names in the corresponding data bank
                            for (int i = 1; i < line.length; i++) {
                                d("i : " + i + ", " + line[i]);

                                if (!line[i].equals("")) {
                                    // Add the name of the column
                                    model.frames.get(i - 1).dataBankColumnNames.add(line[i]);
                                }

                                if (line[i].toLowerCase().startsWith("images")) {
                                    // This column contains image filenames
                                    model.frames.get(i - 1).isImage = true;
                                }

                                if (line[i].toLowerCase().startsWith("comprehension")) {
                                    // This column contains comprehension text column name
                                    model.frames.get(i - 1).isComprehension = true;

                                    // This column contains the comprehension questions column name
                                    model.frames.get(i - 1).comprehensionQuestions.add(new MasterCSVModel.ComprehensionQuestion(line[i], lineNumber));
                                }

                                if (line[i].toLowerCase().startsWith("math")) {
                                    // This column contains math
                                    model.frames.get(i - 1).isMath = true;
                                }
                            }
                            lineNumber++;
                        }
                    }
                }
                lineNumber = 0;
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Unable to parse template CSV");
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
    }
}