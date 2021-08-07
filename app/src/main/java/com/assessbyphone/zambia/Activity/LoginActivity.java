package com.assessbyphone.zambia.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
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
import android.widget.ImageView;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    public SharedPreferences sPrefs;
    MasterCSVModel.AgeGroup ageGroup = MasterCSVModel.AgeGroup.NotSpecified;
    private EditText identityField, passwordField;
    private Button loginButton;
    private boolean adminMode = false;
    private CustomProgressBar customProgressBar;
    private CheckBox showPassCheck;
    private ImageView loginBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        if (getIntent().hasExtra("admin")) {
            adminMode = true;
        }

        if (getIntent().hasExtra("ageGroup")) {
            ageGroup = (MasterCSVModel.AgeGroup) getIntent().getSerializableExtra("ageGroup");
        }

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
                        Backendless.UserService.findById(currentUserId, new DefaultCallback<BackendlessUser>(LoginActivity.this, "Logging in...") {
                            @Override
                            public void handleResponse(BackendlessUser currentUser) {
                                super.handleResponse(currentUser);
                                Backendless.UserService.setCurrentUser(currentUser);
                                Intent newIntent;
                                if (!adminMode) {
                                    newIntent = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
                                } else {
                                    newIntent = new Intent(getBaseContext(), AdminActivity.class);
                                }

                                newIntent.putExtra("ageGroup", ageGroup);
                                newIntent.putExtra("myUTN", identity);
                                newIntent.putExtra("backendlessUser", currentUser);
                                startActivity(newIntent);
                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
//                                finish();
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
        loginBack = findViewById(R.id.loginBack_ids);
//    rememberLoginBox = (CheckBox) findViewById( jp.funx.jp.funx.phonicsbyphone.R.id.rememberLoginBox );

        String tempString = getResources().getString(R.string.register_text);
        SpannableString underlinedContent = new SpannableString(tempString);
        underlinedContent.setSpan(new UnderlineSpan(), 0, tempString.length(), 0);
//    registerLink.setText( underlinedContent );
        tempString = getResources().getString(R.string.restore_link);
        underlinedContent = new SpannableString(tempString);
        underlinedContent.setSpan(new UnderlineSpan(), 0, tempString.length(), 0);
//    restoreLink.setText( underlinedContent );

        loginBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

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

        if (!isNetworkAvailable()) {
            doOfflineLogin(identity, password);
        } else {
            customProgressBar.showMe(LoginActivity.this, "Please Wait...");
            Backendless.UserService.login(identity, password, new DefaultCallback<BackendlessUser>(LoginActivity.this) {
                public void handleResponse(final BackendlessUser backendlessUser) {
                    Intent newIntent = null;
                    if (!adminMode) {
                        newIntent = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
                    } else {
                        newIntent = new Intent(getBaseContext(), AdminActivity.class);
                    }

                    newIntent.putExtra("ageGroup", ageGroup);
                    newIntent.putExtra("myUTN", identity);
                    backendlessUser.setProperty("localPassword", password);

                    int numStudents = 0;
                    if (backendlessUser.getProperty("students") != null) {
                        try {
                            numStudents = ((Students[]) backendlessUser.getProperty("students")).length;
                        } catch (ClassCastException e) {
                            // do nothing
                        }
                    }

                    int isStatsViewer;

                    final int finalNumStudents = numStudents;

                    if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("needSync_" + identity, false)) {
                        Toast.makeText(getApplicationContext(), "There are offline changes to be saved on the server but this functionality hasn't been implemented yet!", Toast.LENGTH_LONG);
                        /* 2017/10/08: started an attempt below to sync offline changes with the server but gave up coz too tired and too busy.
                        progressDialog.setTitle("Synchronizing local offline changes to server. It may take some time.");
                        BackendlessUser backendlessUserToSync = SerializableManager.readSerializable(getBaseContext(), "backendlessUser_" + identity);
                        // Loop through all the students, update their results relations, then update the students relations to the teacher
                        final LinkedList<Students> studentsLinkedList = new LinkedList(Arrays.asList((Students[]) backendlessUserToSync.getProperty("students")));
                        AsyncCallback<Integer> recursiveCallback = new AsyncCallback<Integer>() {
                            Students _currentStudent = null;
                            @Override
                            public void handleResponse(Integer response) {
                                Students nextStudent = studentsLinkedList.poll();
                                _currentStudent = nextStudent;
                                if (studentsLinkedList.size() == 0) {
                                    // Last, update the students relation to the teacher
                                    Log.d("PhonicsByPhone", "Synching teacher " + backendlessUser.getObjectId() + " with " + finalNumStudents + " students");
                                    Backendless.Data.of(BackendlessUser.class).setRelation(backendlessUser, "students", Arrays.asList((Students[]) backendlessUser.getProperty("students")), new AsyncCallback<Integer>() {
                                        @Override
                                        public void handleResponse(Integer response) {
                                            dialog.cancel();

                                            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("needSync_" + identity, false).commit();

                                            Toast.makeText(getApplicationContext(), "Synchronized locally saved changes with the server", Toast.LENGTH_LONG).show();

                                            Intent newIntent2;
                                            if (!adminMode) {
                                                newIntent2 = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
                                            } else {
                                                newIntent2 = new Intent(getBaseContext(), AdminActivity.class);
                                            }
                                            newIntent2.putExtra("backendlessUser", backendlessUser);
                                            startActivity(newIntent2);
                                            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault backendlessFault) {
                                            dialog.cancel();
                                            Toast.makeText(getApplicationContext(), "Unable to synchronize locally saved students with teacher on the server"
                                                   + ": " + backendlessFault.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    String resultObjIds = "";
                                    if (nextStudent.getResults() != null) {
                                        for (PhonicsResults result : nextStudent.getResults()) {
                                            resultObjIds += result.getObjectId() + ", ";
                                        }
                                    }
                                    Log.d("PhonicsByPhone", "Synching student " + nextStudent.getObjectId() + " with results: " + resultObjIds);
                                    Backendless.Data.of(Students.class).setRelation(nextStudent, "results", nextStudent.getResults(), localSyncCallback);
                                }
                            }

                            @Override

                            public void handleFault(BackendlessFault backendlessFault) {
                                Toast.makeText(getApplicationContext(), "Unable to synchronize locally saved results with server for "
                                        + _currentStudent.getFirstName() + " " + _currentStudent.getLastName() + ": " + backendlessFault.getMessage(), Toast.LENGTH_LONG).show();
                                Students nextStudent = studentsLinkedList.poll();

                                Log.d("PhonicsByPhone", "Synching student " + _currentStudent.getObjectId() + " with results: " + resultObjIds);
                                Backendless.Data.of(Students.class).setRelation(_currentStudent, "results", _currentStudent.getResults(), localSyncCallback);
                            }
                        };
                        final Students firstStudent = studentsLinkedList.poll();
                        localSyncCallback = recursiveCallback;
                        if (firstStudent.getObjectId() == null) {
                            Backendless.Data.of(Students.class).save(firstStudent, new AsyncCallback<Students>() {
                                @Override
                                public void handleResponse(Students response) {
                                    firstStudent.setObjectId(response.getObjectId());
                                    Backendless.Data.of(Students.class).setRelation(firstStudent, "results", firstStudent.getResults(), localSyncCallback);
                                }

                                @Override
                                public void handleFault(BackendlessFault backendlessFault) {
                                    String error = "Unable to synchronize locally saved results with server for "
                                            + firstStudent.getFirstName() + " " + firstStudent.getLastName() + ": " + backendlessFault.getMessage();
                                    Log.e("PhonicsByPhone", error);
                                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();

                                }
                            });
                        } else {
                            Backendless.Data.of(Students.class).setRelation(firstStudent, "results", firstStudent.getResults(), recursiveCallback);
                        }
                        */

                        /* old Backendless v3 code below
                        Backendless.UserService.update(backendlessUserToSync, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser backendlessUser) {
                                dialog.cancel();

                                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("needSync_" + identity, false).commit();

                                Toast.makeText(getApplicationContext(), "Synchronized locally saved changes with the server", Toast.LENGTH_LONG).show();

                                Intent newIntent2;
                                if (!adminMode) {
                                    newIntent2 = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
                                } else {
                                    newIntent2 = new Intent(getBaseContext(), AdminActivity.class);
                                }
                                newIntent2.putExtra("backendlessUser", backendlessUser);
                                startActivity(newIntent2);
                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                            }

                            @Override
                            public void handleFault(BackendlessFault backendlessFault) {
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(), "Unable to synchronize locally saved changes with server: " + backendlessFault.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        */
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

                                @Override
                                protected void onPostExecute(List<BackendlessUser> adminClasses) {
                                    customProgressBar.dismissMe();
                                    if (adminClasses != null) {
                                        backendlessUser.setProperty("adminClasses", adminClasses);
                                        Intent newIntent2 = new Intent(getBaseContext(), AdminActivity.class);
                                        newIntent2.putExtra("ageGroup", ageGroup);
                                        newIntent2.putExtra("backendlessUser", backendlessUser);
                                        newIntent2.putExtra("isStatsAdmin", true);
                                        newIntent2.putExtra("myUTN", identity);
                                        startActivity(newIntent2);
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
                                                        Intent newIntent2 = null;
                                                        if (!adminMode) {
                                                            newIntent2 = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
                                                        } else {
                                                            newIntent2 = new Intent(getBaseContext(), AdminActivity.class);
                                                        }
                                                        SerializableManager.saveSerializable(getBaseContext(), backendlessUser, "backendlessUser_" + identity);
                                                        newIntent2.putExtra("backendlessUser", backendlessUser);
                                                        newIntent2.putExtra("ageGroup", ageGroup);
                                                        newIntent2.putExtra("myUTN", identity);
                                                        startActivity(newIntent2);
                                                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {
                                                        customProgressBar.dismissMe();
                                                        Toast.makeText(getApplicationContext(), "Unable to get students' results: " + fault.getMessage(), Toast.LENGTH_LONG).show();
                                                        Log.e("PBP", "Unable to get students' results - " + fault.getMessage());
                                                    }
                                                });
//                                                } else {
//                                                    // Maths PhonicsResults
//                                                    resultsQuery.setRelated("mathsResults");
//                                                    Backendless.Data.of(Students.class).find(resultsQuery, new AsyncCallback<List<Students>>() {
//                                                        @Override
//                                                        public void handleResponse(List<Students> response) {
//                                                            dialog.cancel();
//                                                            students.clear();
//                                                            students.addAll(response);
//                                                            backendlessUser.setProperty("students", students);
//                                                            Intent newIntent2 = null;
//                                                            if (!adminMode) {
//                                                                newIntent2 = new Intent(getBaseContext(), PostLoginRecordMathsResultsManagerActivity.class);
//                                                            } else {
//                                                                newIntent2 = new Intent(getBaseContext(), AdminActivity.class);
//                                                            }
//                                                            SerializableManager.saveSerializable(getBaseContext(), backendlessUser, "backendlessUser_" + identity);
//                                                            newIntent2.putExtra("backendlessUser", backendlessUser);
//                                                            startActivity(newIntent2);
//                                                        }
//
//                                                        @Override
//                                                        public void handleFault(BackendlessFault fault) {
//                                                            dialog.cancel();
//                                                            Toast.makeText(getApplicationContext(), "Unable to get students' maths results: " + fault.getMessage(), Toast.LENGTH_LONG).show();
//                                                            Log.e("PBP", "Unable to get students' maths results - " + fault.getMessage());
//                                                        }
//                                                    });
//                                                }
                                            } else {
                                                customProgressBar.dismissMe();
                                                if (students != null) {
                                                    students.clear();
                                                    backendlessUser.setProperty("students", students);  // students is zero
                                                }
                                                Intent newIntent2 = null;
                                                if (!adminMode) {
                                                    newIntent2 = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
                                                } else {
                                                    newIntent2 = new Intent(getBaseContext(), AdminActivity.class);
                                                }
                                                SerializableManager.saveSerializable(getBaseContext(), backendlessUser, "backendlessUser_" + identity);
                                                newIntent2.putExtra("backendlessUser", backendlessUser);
                                                newIntent2.putExtra("ageGroup", ageGroup);
                                                newIntent2.putExtra("myUTN", identity);
                                                startActivity(newIntent2);
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
//                finish();
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    customProgressBar.dismissMe();
                    if (fault != null && fault.getMessage() != null
                            && (fault.getMessage().contains("Unable to resolve")
                            || fault.getMessage().contains("timeout"))
                    ) {
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
            new AlertDialog.Builder(LoginActivity.this)
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
            new AlertDialog.Builder(LoginActivity.this)
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

        Intent newIntent;
        if (!adminMode) {
            newIntent = new Intent(getBaseContext(), GetMasterTemplateActivity.sTemplate.isMathAssessment() ? PostLoginRecordMathsResultsManagerActivity.class : PostLoginRecordResultsManagerActivity.class);
        } else {
            newIntent = new Intent(getBaseContext(), AdminActivity.class);
        }
        Toast.makeText(getApplicationContext(), "Using locally saved data because no internet connection!", Toast.LENGTH_LONG).show();
        newIntent.putExtra("ageGroup", ageGroup);
        newIntent.putExtra("backendlessUser", backendlessUser);
        newIntent.putExtra("myUTN", identity);
        startActivity(newIntent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public void onRegisterLinkClicked() {
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public void onRestoreLinkClicked() {
        startActivity(new Intent(this, RestorePasswordActivity.class));
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
