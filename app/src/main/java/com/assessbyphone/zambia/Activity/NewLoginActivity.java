package com.assessbyphone.zambia.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.method.PasswordTransformationMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.assessbyphone.zambia.CallbackUtils.CustomProgressBar;
import com.assessbyphone.zambia.CallbackUtils.DefaultCallback;
import com.assessbyphone.zambia.CallbackUtils.Defaults;
import com.assessbyphone.zambia.Models.MasterCSVModel;
import com.assessbyphone.zambia.Models.PhonicsResults;
import com.assessbyphone.zambia.Models.SerializableManager;
import com.assessbyphone.zambia.Models.Students;
import com.assessbyphone.zambia.R;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.LoadRelationsQueryBuilder;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import static com.assessbyphone.zambia.CallbackUtils.UiUtil.setIsAdmin;
import static com.assessbyphone.zambia.CallbackUtils.UiUtil.setIsLogin;
import static com.assessbyphone.zambia.CallbackUtils.UiUtil.setPassword;
import static com.assessbyphone.zambia.CallbackUtils.UiUtil.setUserID;

public class NewLoginActivity extends Activity {
    public SharedPreferences sPrefs;
    MasterCSVModel.AgeGroup ageGroup = MasterCSVModel.AgeGroup.NotSpecified;
    private EditText identityField, passwordField;
    private Button loginButton;
    private CustomProgressBar customProgressBar;
    private CheckBox showPassCheck;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_login);

        sPrefs = getPreferences(MODE_PRIVATE);

        customProgressBar = new CustomProgressBar();

        initUI();

        Backendless.setUrl(Defaults.SERVER_URL);
        Backendless.initApp(this, Defaults.getBackendlessAppId(getApplicationContext()), Defaults.getBackendlessAppKey(getApplicationContext()));

        Backendless.Data.mapTableToClass("Students", Students.class);
        //Backendless.Data.mapTableToClass("Results", Results.class);
        Backendless.Data.mapTableToClass("PhonicsResults", PhonicsResults.class);
        String identity = identityField.getText().toString().toLowerCase();
        Backendless.UserService.isValidLogin(new DefaultCallback<Boolean>(this) {
            @Override
            public void handleResponse(Boolean isValidLogin) {
                if (isValidLogin && Backendless.UserService.CurrentUser() == null) {
                    String currentUserId = Backendless.UserService.loggedInUser();

                    if (!currentUserId.equals("")) {
                        Backendless.UserService.findById(currentUserId, new DefaultCallback<BackendlessUser>(NewLoginActivity.this, "Logging in...") {
                            @Override
                            public void handleResponse(BackendlessUser currentUser) {
                                super.handleResponse(currentUser);
                                Backendless.UserService.setCurrentUser(currentUser);
//                                Intent newIntent;
//                                if (!adminMode) {
//                                    newIntent = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
//                                } else {
//                                    newIntent = new Intent(getBaseContext(), AdminActivity.class);
//                                }
//
//                                newIntent.putExtra("ageGroup", ageGroup);
//                                newIntent.putExtra("myUTN", identity);
//                                newIntent.putExtra("backendlessUser", currentUser);
//                                startActivity(newIntent);
//                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

                                Gson gson = new Gson();
                                String json = gson.toJson(currentUser.getClass());
                                setIsLogin(NewLoginActivity.this, "1");
                                startActivity(new Intent(NewLoginActivity.this, GetMasterTemplateActivity.class));
                                finish();
                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                            }
                        });
                    }
                }
                super.handleResponse(isValidLogin);
            }
        });
    }

    private void initUI() {
//    registerLink = (TextView) findViewById( jp.funx.jp.funx.phonicsbyphone.R.id.registerLink );
//    restoreLink = (TextView) findViewById( jp.funx.jp.funx.phonicsbyphone.R.id.restoreLink );
        identityField = findViewById(R.id.identityField);
        if (sPrefs.contains("utn")) {
            identityField.setText(sPrefs.getString("utn", ""));
        }
        passwordField = findViewById(R.id.passwordField);
//        passwordField.setText( "testtest");
        loginButton = findViewById(R.id.loginButton);
        showPassCheck = findViewById(R.id.showPassCheck_ids);
        // loginBack = findViewById(R.id.loginBack_ids);
//    rememberLoginBox = (CheckBox) findViewById( jp.funx.jp.funx.phonicsbyphone.R.id.rememberLoginBox );

        String tempString = getResources().getString(R.string.register_text);
        SpannableString underlinedContent = new SpannableString(tempString);
        underlinedContent.setSpan(new UnderlineSpan(), 0, tempString.length(), 0);
//    registerLink.setText( underlinedContent );
        tempString = getResources().getString(R.string.restore_link);
        underlinedContent = new SpannableString(tempString);
        underlinedContent.setSpan(new UnderlineSpan(), 0, tempString.length(), 0);
//    restoreLink.setText( underlinedContent );

//        loginBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLoginButtonClicked();
            }
        });

        showPassCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    passwordField.setTransformationMethod(null);
                else
                    passwordField.setTransformationMethod(new PasswordTransformationMethod());

                passwordField.setSelection(passwordField.length());
            }
        });

//    registerLink.setOnClickListener( new View.OnClickListener()
//    {
//      @Override
//      public void onClick( View view )
//      {
//        onRegisterLinkClicked();
//      }
//    } );

//    restoreLink.setOnClickListener( new View.OnClickListener()
//    {
//      @Override
//      public void onClick( View view )
//      {
//        onRestoreLinkClicked();
//      }
//    } );
    }

    public void onLoginButtonClicked() {
        final String identity = identityField.getText().toString().toLowerCase();
        sPrefs.edit().putString("utn", identity).apply();
        sPrefs.edit().putString("newUTN", identity).apply();
        final String password = passwordField.getText().toString();
        //boolean rememberLogin = rememberLoginBox.isChecked();
        boolean rememberLogin = false;
        setUserID(NewLoginActivity.this,identity);
        setPassword(NewLoginActivity.this,password);

        if (!isNetworkAvailable()) {
            doOfflineLogin(identity, password);
        } else {
            customProgressBar.showMe(NewLoginActivity.this, "Please Wait...");
            Backendless.UserService.login(identity, password, new DefaultCallback<BackendlessUser>(NewLoginActivity.this) {
                public void handleResponse( BackendlessUser backendlessUser) {
                    sPrefs.edit().putString("utn", identity).apply();
//                    Intent newIntent = null;
//                    if (!adminMode) {
//                        newIntent = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
//                    } else {
//                        newIntent = new Intent(getBaseContext(), AdminActivity.class);
//                    }
                    //newIntent.putExtra("ageGroup", ageGroup);
                    //newIntent.putExtra("myUTN", identity);
                    backendlessUser.setProperty("localPassword", password);

                    if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("needSync_" + identity, false)) {
                        Toast.makeText(getApplicationContext(), "There are offline changes to be saved on the server but this functionality hasn't been implemented yet!", Toast.LENGTH_LONG).show();
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
                                        final Exception e2 = e;
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Unable to get admin's classes with students and results: " + e2.getMessage(), Toast.LENGTH_LONG).show();
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
                                        //backendlessUser.setProperty("adminClasses", adminClasses);
                                        backendlessUser.setProperty("adminClasses", adminClasses);
                                       // backendlessUser.setProperty("students", adminClasses);
//                                        Intent newIntent2 = new Intent(getBaseContext(), AdminActivity.class);
//                                        newIntent2.putExtra("ageGroup", ageGroup);
//                                        newIntent2.putExtra("backendlessUser", backendlessUser);
//                                        newIntent2.putExtra("isStatsAdmin", true);
//                                        newIntent2.putExtra("myUTN", identity);
//                                        startActivity(newIntent2);
//                                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                        setIsAdmin(NewLoginActivity.this,true);
                                        setIsLogin(NewLoginActivity.this, "1");
                                        startActivity(new Intent(NewLoginActivity.this, GetMasterTemplateActivity.class));
                                        finish();
                                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
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
                                                Log.e("PGP", "whereQuery: " + whereQuery);
                                                DataQueryBuilder resultsQuery = DataQueryBuilder.create();
                                                resultsQuery.setWhereClause(whereQuery.toString());
                                                resultsQuery.setPageSize(100);
//                                                if (!GetMasterTemplateActivity.sTemplate.isMathAssessment()) {
                                                // Phonics and Maths PhonicsResults
//                                                    resultsQuery.setRelated("results");
                                                resultsQuery.setRelated(new ArrayList<String>() {{
                                                    //add("results");
                                                    add("phonicsResults");
                                                    add("mathsResults");
                                                }});
                                                Backendless.Data.of(Students.class).find(resultsQuery, new AsyncCallback<List<Students>>() {
                                                            @Override
                                                            public void handleResponse(List<Students> response) {
                                                                customProgressBar.dismissMe();
                                                                students.clear();
                                                                students.addAll(response);
                                                                backendlessUser.setProperty("students", students);
//                                                                Intent newIntent2 = null;
//                                                                if (!adminMode) {
//                                                                    newIntent2 = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
//                                                                } else {
//                                                                    newIntent2 = new Intent(getBaseContext(), AdminActivity.class);
//                                                                }
                                                                SerializableManager.saveSerializable(getBaseContext(), backendlessUser, "backendlessUser_" + identity);
//                                                                newIntent2.putExtra("backendlessUser", backendlessUser);
//                                                                newIntent2.putExtra("ageGroup", ageGroup);
//                                                                newIntent2.putExtra("myUTN", identity);
//                                                                startActivity(newIntent2);
//                                                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
//                                                                Log.e("Hit2","Hit2");
//                                                                Gson gson = new Gson();
//                                                                myBackendlessUser = backendlessUser;
//                                                                //String json = gson.toJson(backendlessUser.getClass());
//                                                                //String json = gson.toJson(backendlessUser, BackendlessUser.class);
//                                                                String json = gson.toJson(myBackendlessUser);
//                                                                Log.e("json1",json);
//                                                                saveBackUser(NewLoginActivity.this, json);
                                                                setIsAdmin(NewLoginActivity.this,false);
                                                                setIsLogin(NewLoginActivity.this, "1");
                                                                startActivity(new Intent(NewLoginActivity.this, GetMasterTemplateActivity.class));
                                                                finish();
                                                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                                            }

                                                            @Override
                                                            public void handleFault(BackendlessFault fault) {
                                                                customProgressBar.dismissMe();
                                                                Toast.makeText(getApplicationContext(), "Unable to get students' results: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                                                Log.e("PBP", "Unable to get students' results - " + fault.getMessage());
                                                            }
                                                        }
                                                );
                                            } else {
                                                customProgressBar.dismissMe();
                                                if (students != null) {
                                                    students.clear();
                                                    backendlessUser.setProperty("students", students);  // students is zero
                                                }
//                                                Intent newIntent2 = null;
//                                                if (!adminMode) {
//                                                    newIntent2 = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
//                                                } else {
//                                                    newIntent2 = new Intent(getBaseContext(), AdminActivity.class);
//                                                }
                                                SerializableManager.saveSerializable(getBaseContext(), backendlessUser, "backendlessUser_" + identity);
//                                                newIntent2.putExtra("backendlessUser", backendlessUser);
//                                                newIntent2.putExtra("ageGroup", ageGroup);
//                                                newIntent2.putExtra("myUTN", identity);
//                                                startActivity(newIntent2);
//                                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

                                                setIsLogin(NewLoginActivity.this, "1");
                                                setIsAdmin(NewLoginActivity.this,false);
                                                startActivity(new Intent(NewLoginActivity.this, GetMasterTemplateActivity.class));
                                                finish();
                                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                            }
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault fault) {
                                            customProgressBar.dismissMe();
                                            Toast.makeText(getApplicationContext(), "Unable to get list students: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                            Log.e("PBP", "Unable to get students list - " + fault.getMessage());
                                        }
                                    });
                        }
                    }
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    if (fault != null && fault.getMessage() != null
                            && (fault.getMessage().contains("Unable to resolve")
                            || fault.getMessage().contains("timeout"))
                    ) {
                        customProgressBar.dismissMe();
                        doOfflineLogin(identity, password);
                    } else {
                        super.handleFault(fault);
                    }
                }
            }, rememberLogin);
        }
    }

    private void doOfflineLogin(String identity, String password) {
        BackendlessUser backendlessUser = SerializableManager.readSerializable(getBaseContext(), "backendlessUser_" + identity);
        if (backendlessUser == null) {
            new AlertDialog.Builder(NewLoginActivity.this)
                    .setTitle("Error")
                    .setMessage("You are offline and no matching locally saved data was found for the login: " + identity)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }

        if (!password.equals(backendlessUser.getProperty("localPassword"))) {
            new AlertDialog.Builder(NewLoginActivity.this)
                    .setTitle("Error")
                    .setMessage("You are offline and the locally saved password does not match the one you entered.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }

//        Intent newIntent;
//        if (!adminMode) {
//            newIntent = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
//        } else {
//            newIntent = new Intent(getBaseContext(), AdminActivity.class);
//        }
        Toast.makeText(getApplicationContext(), "Using locally saved data because no internet connection!", Toast.LENGTH_LONG).show();
//        newIntent.putExtra("ageGroup", ageGroup);
//        newIntent.putExtra("backendlessUser", backendlessUser);
//        newIntent.putExtra("myUTN", identity);
//        startActivity(newIntent);
//        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        setIsLogin(NewLoginActivity.this, "1");
        startActivity(new Intent(NewLoginActivity.this, GetMasterTemplateActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
