package com.assessbyphone.zambia.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.assessbyphone.zambia.CallbackUtils.CustomProgressBar;
import com.assessbyphone.zambia.CallbackUtils.DefaultCallback;
import com.assessbyphone.zambia.Models.MasterCSVModel;
import com.assessbyphone.zambia.Models.MathsResults;
import com.assessbyphone.zambia.Models.SerializableManager;
import com.assessbyphone.zambia.Models.Students;
import com.assessbyphone.zambia.R;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.zj.btsdk.PrintPic;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.assessbyphone.zambia.CallbackUtils.UiUtil.clearSharePreferencesDetails;
import static com.assessbyphone.zambia.CallbackUtils.UiUtil.setIsLogin;

public class PostLoginRecordMathsResultsManagerActivity extends Activity {
    static String NEED_BEFORE_SCORE = "Sorry, the previous score must be higher than zero";
    public SharedPreferences sPrefs;
    Bitmap banksBitmap = null, banksBitmap2 = null;
    Students[] students;
    Students currentStudent;
    boolean ignore = false;
    MasterCSVModel.AgeGroup ageGroup = MasterCSVModel.AgeGroup.NotSpecified;
    private RadioGroup termRG;
    private RadioGroup counting_objects_to_10_result;
    private RadioGroup identify_number_to_10_result;
    private RadioGroup identify_number_to_20_result;
    private RadioGroup identify_number_to_99_result;
    private RadioGroup identify_greatest_number_to_20_result;
    private RadioGroup identify_greatest_number_to_50_result;
    private RadioGroup identify_greatest_number_to_150_result;
    private RadioGroup identify_greatest_number_to_1100_result;
    private RadioGroup add_numbers_to_10_result;
    private RadioGroup subtract_numbers_to_10_result;
    private RadioGroup counting_in_ones_missing_numbers_result;
    private RadioGroup identify_number_to_999_result;
    private RadioGroup counting_in_tens_missing_numbers_result;
    private RadioGroup counting_in_2s_5s_missing_numbers_result;
    private RadioGroup counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result;
    private RadioGroup add_numbers_to_20_result;
    private RadioGroup add_two_and_three_digit_numbers_result;
    private RadioGroup add_two_2_digit_numbers_result;
    private RadioGroup subtract_numbers_to_20_result;
    private RadioGroup subtract_1_digit_from_2_digits_to_99_result;
    private RadioGroup subtract_2_digit_from_2_digits_result;
    private RadioGroup multiply_1_digit_numbers_result;
    private RadioGroup multiply_1_digit_by_2_digit_numbers_result;
    private RadioGroup multiply_2_digit_by_2_digit_numbers_to_20_result;
    private RadioGroup divide_2_digit_by_1_digit_numbers_to_20_result;
    private RadioGroup divide_2_digit_by_1_digit_numbers_over_20_result;
    private RadioGroup divide_2_digit_by_1_digit_numbers_to_99_result;
    private RadioGroup addition_result;
    private RadioGroup subtraction_result;
    private RadioGroup multiplication_result;
    private RadioGroup division_result;
    private RadioGroup identify_number_to_9999999_result;
    private RadioGroup identify_greatest_number_to_9999_result;
    private RadioGroup counting_in_various_missing_numbers_result;
    private RadioGroup add_two_three_digit_numbers_including_decimals_result;
    private RadioGroup subtract_2_digit_from_3_digits_including_decimals_result;
    private RadioGroup multiply_2_digit_by_2_digit_numbers_over_20_result;
    private RadioGroup divide_2_and_3_digit_by_2_digit_numbers_result;
    private TextView numbersSubtotal;
    private TextView wordsSubtotalTv;
    private TextView overallTotal;
    private Button saveResultsButton;
    private Button printResultsButton;
    private LinearLayout logoutLay;
    private ProgressBar mProgressBar;
    private Spinner studentsSpinner;
    private BackendlessUser backendlessUser;
    private FrameLayout currentStatusLay;
    private TextView currentStatusTv;
    private ListPopupWindow statusPopupList;
    private LayoutInflater layoutInflater;
    private int width;
    private ImageView resultBack;
    private CustomProgressBar customProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_maths_results_manager);

        Log.e("MathsProblems", "MathsProblems");

        sPrefs = getPreferences(MODE_PRIVATE);

        //backendlessUser = GetMasterTemplateActivity.myBackendlessUser;
        backendlessUser = (BackendlessUser) getIntent().getSerializableExtra("backendlessUser");

        try {
            ArrayList<Students> s = ((ArrayList<Students>) backendlessUser.getProperty("students"));
            students = new Students[s.size()];
            s.toArray(students);
        } catch (ClassCastException e) {
            students = (Students[]) backendlessUser.getProperty("students");
        } catch (Exception e) {
            new AlertDialog.Builder(PostLoginRecordMathsResultsManagerActivity.this)
                    .setTitle("Error")
                    .setCancelable(false)
                    .setMessage("This teacher has no students assigned to them. Please add some.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            dialog.dismiss();
                            PostLoginRecordMathsResultsManagerActivity.this.finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }
        if (students != null)
            Log.d(MenuActivity.TAG, "got " + students.length + " students for " + backendlessUser.getProperty("name"));

        ignore = true;
        initUI();
        ignore = false;
    }

    private void initUI() {
        resultBack = findViewById(R.id.resultBack_ids);
        saveResultsButton = findViewById(R.id.saveResultsButton);
        saveResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentStudent != null) {
                    customProgressBar.showMe(PostLoginRecordMathsResultsManagerActivity.this, "Please Wait...");
                    onSaveResultsButtonClicked();
                } else
                    Toast.makeText(PostLoginRecordMathsResultsManagerActivity.this, "No user found!", Toast.LENGTH_SHORT).show();
            }
        });

        resultBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        printResultsButton = findViewById(R.id.printResultsButton);
        printResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printResults();
            }
        });

        mProgressBar = findViewById(R.id.menuProgressBar2);

        logoutLay = findViewById(R.id.logoutLay_ids);
        logoutLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutDialogAlert();
            }
        });

        numbersSubtotal = findViewById(R.id.numbers_subtotal);
        wordsSubtotalTv = findViewById(R.id.words_subtotal);
        overallTotal = findViewById(R.id.overall_total);

        List<Students> studentsList = Arrays.asList(students);
        Collections.sort(studentsList, new Comparator<Students>() {
            @Override
            public int compare(Students o1, Students o2) {
                return o1.getUpn().compareToIgnoreCase(o2.getUpn());
//                if ("male".equals(o1.getSex().toLowerCase()) && !"male".equals(o2.getSex().toLowerCase()))
//                    return 1;
//                else if ("male".equals(o1.getSex().toLowerCase()) && "male".equals(o2.getSex().toLowerCase()))
//                    return 0;
//                else
//                    return -1;
            }
        });

        studentsSpinner = findViewById(R.id.studentList);
        currentStatusTv = findViewById(R.id.currentStatusTv_ids);
        StudentsAdapter adapter = new StudentsAdapter(getBaseContext(), studentsList);
        adapter.sort(new Comparator<Students>() {
            @Override
            public int compare(Students lhs, Students rhs) {
                return lhs.getUpn().compareToIgnoreCase(rhs.getUpn());
            }
        });
        studentsSpinner.setAdapter(adapter);
        studentsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showStudentDetails(i, "1");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

//        studentWlLevelAdapter = new StudentsLevelAdapter(getBaseContext(), studentLevelALL);
//        studentLevelSpinner.setAdapter(studentWlLevelAdapter);

        termRG = findViewById(R.id.readingTerm);
        termRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                ignore = true;
                showStudentDetails(studentsSpinner.getSelectedItemPosition(), ((RadioButton) findViewById(termRG.getCheckedRadioButtonId())).getText().toString());
                ignore = false;
            }
        });

        // level 1:
        counting_objects_to_10_result = findViewById(R.id.counting_objects_to_10_result);
        counting_objects_to_10_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        identify_number_to_10_result = findViewById(R.id.identify_number_to_10_result);
        identify_number_to_10_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        identify_greatest_number_to_20_result = findViewById(R.id.identify_greatest_number_to_20_result);
        identify_greatest_number_to_20_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        counting_in_ones_missing_numbers_result = findViewById(R.id.counting_in_ones_missing_numbers_result);
        counting_in_ones_missing_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        add_numbers_to_10_result = findViewById(R.id.add_numbers_to_10_result);
        add_numbers_to_10_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        subtract_numbers_to_10_result = findViewById(R.id.subtract_numbers_to_10_result);
        subtract_numbers_to_10_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });

        // level 2:
        identify_number_to_20_result = findViewById(R.id.identify_number_to_20_result);
        identify_number_to_20_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        identify_greatest_number_to_50_result = findViewById(R.id.identify_greatest_number_to_50_result);
        identify_greatest_number_to_50_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        counting_in_tens_missing_numbers_result = findViewById(R.id.counting_in_tens_missing_numbers_result);
        counting_in_tens_missing_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        add_numbers_to_20_result = findViewById(R.id.add_numbers_to_20_result);
        add_numbers_to_20_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        subtract_numbers_to_20_result = findViewById(R.id.subtract_numbers_to_20_result);
        subtract_numbers_to_20_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        multiply_1_digit_numbers_result = findViewById(R.id.multiply_1_digit_numbers_result);
        multiply_1_digit_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        divide_2_digit_by_1_digit_numbers_to_20_result = findViewById(R.id.divide_2_digit_by_1_digit_numbers_to_20_result);
        divide_2_digit_by_1_digit_numbers_to_20_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });

        // level 3:
        identify_number_to_99_result = findViewById(R.id.identify_number_to_99_result);
        identify_number_to_99_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        identify_greatest_number_to_150_result = findViewById(R.id.identify_greatest_number_to_150_result);
        identify_greatest_number_to_150_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        counting_in_2s_5s_missing_numbers_result = findViewById(R.id.counting_in_2s_5s_missing_numbers_result);
        counting_in_2s_5s_missing_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        add_two_2_digit_numbers_result = findViewById(R.id.add_two_2_digit_numbers_result);
        add_two_2_digit_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        subtract_1_digit_from_2_digits_to_99_result = findViewById(R.id.subtract_1_digit_from_2_digits_to_99_result);
        subtract_1_digit_from_2_digits_to_99_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        multiply_1_digit_by_2_digit_numbers_result = findViewById(R.id.multiply_1_digit_by_2_digit_numbers_result);
        multiply_1_digit_by_2_digit_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        divide_2_digit_by_1_digit_numbers_over_20_result = findViewById(R.id.divide_2_digit_by_1_digit_numbers_over_20_result);
        divide_2_digit_by_1_digit_numbers_over_20_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });

        // level 4:
        identify_number_to_999_result = findViewById(R.id.identify_number_to_999_result);
        identify_number_to_999_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        identify_greatest_number_to_1100_result = findViewById(R.id.identify_greatest_number_to_1100_result);
        identify_greatest_number_to_1100_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result = findViewById(R.id.counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result);
        counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        add_two_and_three_digit_numbers_result = findViewById(R.id.add_two_and_three_digit_numbers_result);
        add_two_and_three_digit_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        subtract_2_digit_from_2_digits_result = findViewById(R.id.subtract_2_digit_from_2_digits_result);
        subtract_2_digit_from_2_digits_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        multiply_2_digit_by_2_digit_numbers_to_20_result = findViewById(R.id.multiply_2_digit_by_2_digit_numbers_to_20_result);
        multiply_2_digit_by_2_digit_numbers_to_20_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        divide_2_digit_by_1_digit_numbers_to_99_result = findViewById(R.id.divide_2_digit_by_1_digit_numbers_to_99_result);
        divide_2_digit_by_1_digit_numbers_to_99_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });

        // level 5:
        identify_number_to_9999999_result = findViewById(R.id.identify_number_to_9999999_result);
        identify_number_to_9999999_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        identify_greatest_number_to_9999_result = findViewById(R.id.identify_greatest_number_to_9999_result);
        identify_greatest_number_to_9999_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        counting_in_various_missing_numbers_result = findViewById(R.id.counting_in_various_missing_numbers_result);
        counting_in_various_missing_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        add_two_three_digit_numbers_including_decimals_result = findViewById(R.id.add_two_and_three_digit_numbers_include_decimals_result);
        add_two_three_digit_numbers_including_decimals_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        subtract_2_digit_from_3_digits_including_decimals_result = findViewById(R.id.subtract_2_digit_from_3_digits_including_decimals_result);
        subtract_2_digit_from_3_digits_including_decimals_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        multiply_2_digit_by_2_digit_numbers_over_20_result = findViewById(R.id.multiply_2_digit_by_2_digit_numbers_over_20_result);
        multiply_2_digit_by_2_digit_numbers_over_20_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        divide_2_and_3_digit_by_2_digit_numbers_result = findViewById(R.id.divide_2_and_3_digit_by_2_digit_numbers_result);
        divide_2_and_3_digit_by_2_digit_numbers_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });

        // word problems:
        addition_result = findViewById(R.id.addition_result_ids);
        addition_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        subtraction_result = findViewById(R.id.subtraction_result);
        subtraction_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        multiplication_result = findViewById(R.id.multiplication_result);
        multiplication_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        division_result = findViewById(R.id.division_result);
        division_result.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        customProgressBar = new CustomProgressBar();

        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;

        listPopUpWindow();

        currentStatusLay = findViewById(R.id.currentStatusLay_ids);
        currentStatusLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusPopupList.show();
            }
        });
    }

    private void wlLevelPopup(TextView mTV) {
        PopupMenu popup = new PopupMenu(PostLoginRecordMathsResultsManagerActivity.this, mTV);
        popup.getMenuInflater().inflate(R.menu.wl_level_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                currentStatusTv.setText(item.getTitle().toString());
                setWlLevelContent(item.getTitle().toString());
                return true;
            }
        });
        popup.show();
    }

    private void setWlLevelContent(String mStatus) {
        if (mStatus.equalsIgnoreCase("Null")) {
            addition_result.clearCheck();
            subtraction_result.clearCheck();
            multiplication_result.clearCheck();
            division_result.clearCheck();

            addition_result.findViewById(R.id.addition_result0).setClickable(false);
            addition_result.findViewById(R.id.addition_result1).setClickable(false);
            addition_result.findViewById(R.id.addition_result2).setClickable(false);

            subtraction_result.findViewById(R.id.subtraction_result0).setClickable(false);
            subtraction_result.findViewById(R.id.subtraction_result1).setClickable(false);
            subtraction_result.findViewById(R.id.subtraction_result2).setClickable(false);

            multiplication_result.findViewById(R.id.multiplication_result0).setClickable(false);
            multiplication_result.findViewById(R.id.multiplication_result1).setClickable(false);
            multiplication_result.findViewById(R.id.multiplication_result2).setClickable(false);

            division_result.findViewById(R.id.division_result0).setClickable(false);
            division_result.findViewById(R.id.division_result1).setClickable(false);
            division_result.findViewById(R.id.division_result2).setClickable(false);
        } else {
            addition_result.findViewById(R.id.addition_result0).setClickable(true);
            addition_result.findViewById(R.id.addition_result1).setClickable(true);
            addition_result.findViewById(R.id.addition_result2).setClickable(true);

            subtraction_result.findViewById(R.id.subtraction_result0).setClickable(true);
            subtraction_result.findViewById(R.id.subtraction_result1).setClickable(true);
            subtraction_result.findViewById(R.id.subtraction_result2).setClickable(true);

            multiplication_result.findViewById(R.id.multiplication_result0).setClickable(true);
            multiplication_result.findViewById(R.id.multiplication_result1).setClickable(true);
            multiplication_result.findViewById(R.id.multiplication_result2).setClickable(true);

            division_result.findViewById(R.id.division_result0).setClickable(true);
            division_result.findViewById(R.id.division_result1).setClickable(true);
            division_result.findViewById(R.id.division_result2).setClickable(true);
        }
    }

    @SuppressLint({"NewApi"})
    private void listPopUpWindow() {
        final List<String> status = new ArrayList<>();
        status.add("Basic");
        status.add("Intermediate");
        status.add("Advanced");

        statusPopupList = new ListPopupWindow(PostLoginRecordMathsResultsManagerActivity.this);
        statusPopupList.setWidth(656);
        // statusPopupList.setBackgroundDrawable(PostLoginRecordMathsResultsManagerActivity.this.getResources().getDrawable(R.drawable.frame2));
        statusPopupList.setBackgroundDrawable(null);
        statusPopupList.setDropDownGravity(Gravity.CENTER_HORIZONTAL);
        ArrayAdapter adapter = new ArrayAdapter<>(PostLoginRecordMathsResultsManagerActivity.this, R.layout.world_problem_list_design, R.id.statusTv_ids, status);
        statusPopupList.setAnchorView(currentStatusTv);
        statusPopupList.setAdapter(adapter);
        statusPopupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentStatusTv.setText(status.get(position));
                setWlLevelContent(status.get(position));
                statusPopupList.dismiss();
            }
        });
    }

    void showStudentDetails(int pos, String term) {
        Students student = (Students) studentsSpinner.getItemAtPosition(pos);
        currentStudent = student;

        List<MathsResults> results = student.getMathsResults();
        MathsResults resultsToShow = null;
        outter:
        if (results != null && results.size() > 0) {
            // Find the reading term's results to show, with "int term" argument taking priority
            for (MathsResults result : results) {
                if (result != null && result.getTerm().equals(term)) {
                    resultsToShow = result;
                    break outter;
                }
            }
            // else, blank slate
            resultsToShow = new MathsResults();
            resultsToShow.setTerm(term);
        } else {
            Log.e(MenuActivity.TAG, "No results for student to show");
            // else, blank slate
            resultsToShow = new MathsResults();
            resultsToShow.setTerm(term);
        }

        switch (resultsToShow.getTerm()) {
            case "1":
                termRG.check(R.id.readingTerm1);
                break;
            case "2":
                termRG.check(R.id.readingTerm2);
                break;
            case "3":
                termRG.check(R.id.readingTerm3);
                break;
        }

        // level 1:

        switch (resultsToShow.getCounting_objects_to_10() == null ? "Null" : resultsToShow.getCounting_objects_to_10()) {
            case "0":
                counting_objects_to_10_result.check(R.id.counting_objects_to_10_result0);
                break;
            case "1":
                counting_objects_to_10_result.check(R.id.counting_objects_to_10_result1);
                break;
            case "2":
                counting_objects_to_10_result.check(R.id.counting_objects_to_10_result2);
                break;
            case "3":
                counting_objects_to_10_result.check(R.id.counting_objects_to_10_result3);
                break;
            default:
                counting_objects_to_10_result.check(R.id.counting_objects_to_10_resultN);
                break;
        }

        switch (resultsToShow.getIdentify_number_to_10() == null ? "Null" : resultsToShow.getIdentify_number_to_10()) {
            case "0":
                identify_number_to_10_result.check(R.id.identify_number_to_10_result0);
                break;
            case "1":
                identify_number_to_10_result.check(R.id.identify_number_to_10_result1);
                break;
            case "2":
                identify_number_to_10_result.check(R.id.identify_number_to_10_result2);
                break;
            case "3":
                identify_number_to_10_result.check(R.id.identify_number_to_10_result3);
                break;
            default:
                identify_number_to_10_result.check(R.id.identify_number_to_10_resultN);
                break;
        }

        switch (resultsToShow.getIdentify_greatest_number_to_20() == null ? "Null" : resultsToShow.getIdentify_greatest_number_to_20()) {
            case "0":
                identify_greatest_number_to_20_result.check(R.id.identify_greatest_number_to_20_result0);
                break;
            case "1":
                identify_greatest_number_to_20_result.check(R.id.identify_greatest_number_to_20_result1);
                break;
            case "2":
                identify_greatest_number_to_20_result.check(R.id.identify_greatest_number_to_20_result2);
                break;
            case "3":
                identify_greatest_number_to_20_result.check(R.id.identify_greatest_number_to_20_result3);
                break;
            default:
                identify_greatest_number_to_20_result.check(R.id.identify_greatest_number_to_20_resultN);
                break;
        }

        switch (resultsToShow.getCounting_in_ones_missing_numbers() == null ? "Null" : resultsToShow.getCounting_in_ones_missing_numbers()) {
            case "0":
                counting_in_ones_missing_numbers_result.check(R.id.counting_in_ones_missing_numbers_result0);
                break;
            case "1":
                counting_in_ones_missing_numbers_result.check(R.id.counting_in_ones_missing_numbers_result1);
                break;
            case "2":
                counting_in_ones_missing_numbers_result.check(R.id.counting_in_ones_missing_numbers_result2);
                break;
            default:
                counting_in_ones_missing_numbers_result.check(R.id.counting_in_ones_missing_numbers_resultN);
                break;
        }

        switch (resultsToShow.getAdd_numbers_to_10() == null ? "Null" : resultsToShow.getAdd_numbers_to_10()) {
            case "0":
                add_numbers_to_10_result.check(R.id.add_numbers_to_10_result0);
                break;
            case "1":
                add_numbers_to_10_result.check(R.id.add_numbers_to_10_result1);
                break;
            case "2":
                add_numbers_to_10_result.check(R.id.add_numbers_to_10_result2);
                break;
            case "3":
                add_numbers_to_10_result.check(R.id.add_numbers_to_10_result3);
                break;
            default:
                add_numbers_to_10_result.check(R.id.add_numbers_to_10_resultN);
                break;
        }

        switch (resultsToShow.getSubtract_numbers_to_10() == null ? "Null" : resultsToShow.getSubtract_numbers_to_10()) {
            case "0":
                subtract_numbers_to_10_result.check(R.id.subtract_numbers_to_10_result0);
                break;
            case "1":
                subtract_numbers_to_10_result.check(R.id.subtract_numbers_to_10_result1);
                break;
            case "2":
                subtract_numbers_to_10_result.check(R.id.subtract_numbers_to_10_result2);
                break;
            case "3":
                subtract_numbers_to_10_result.check(R.id.subtract_numbers_to_10_result3);
                break;
            default:
                subtract_numbers_to_10_result.check(R.id.subtract_numbers_to_10_resultN);
                break;
        }

        // level 2:

        switch (resultsToShow.getIdentify_number_to_20() == null ? "Null" : resultsToShow.getIdentify_number_to_20()) {
            case "0":
                identify_number_to_20_result.check(R.id.identify_number_to_20_result0);
                break;
            case "1":
                identify_number_to_20_result.check(R.id.identify_number_to_20_result1);
                break;
            case "2":
                identify_number_to_20_result.check(R.id.identify_number_to_20_result2);
                break;
            case "3":
                identify_number_to_20_result.check(R.id.identify_number_to_20_result3);
                break;
            default:
                identify_number_to_20_result.check(R.id.identify_number_to_20_resultN);
                break;
        }

        switch (resultsToShow.getIdentify_greatest_number_to_50() == null ? "Null" : resultsToShow.getIdentify_greatest_number_to_50()) {
            case "0":
                identify_greatest_number_to_50_result.check(R.id.identify_greatest_number_to_50_result0);
                break;
            case "1":
                identify_greatest_number_to_50_result.check(R.id.identify_greatest_number_to_50_result1);
                break;
            case "2":
                identify_greatest_number_to_50_result.check(R.id.identify_greatest_number_to_50_result2);
                break;
            case "3":
                identify_greatest_number_to_50_result.check(R.id.identify_greatest_number_to_50_result3);
                break;
            default:
                identify_greatest_number_to_50_result.check(R.id.identify_greatest_number_to_50_resultN);
                break;
        }

        switch (resultsToShow.getCounting_in_tens_missing_numbers() == null ? "Null" : resultsToShow.getCounting_in_tens_missing_numbers()) {
            case "0":
                counting_in_tens_missing_numbers_result.check(R.id.counting_in_tens_missing_numbers_result0);
                break;
            case "1":
                counting_in_tens_missing_numbers_result.check(R.id.counting_in_tens_missing_numbers_result1);
                break;
            case "2":
                counting_in_tens_missing_numbers_result.check(R.id.counting_in_tens_missing_numbers_result2);
                break;
            default:
                counting_in_tens_missing_numbers_result.check(R.id.counting_in_tens_missing_numbers_resultN);
                break;
        }

        switch (resultsToShow.getAdd_numbers_to_20() == null ? "Null" : resultsToShow.getAdd_numbers_to_20()) {
            case "0":
                add_numbers_to_20_result.check(R.id.add_numbers_to_20_result0);
                break;
            case "1":
                add_numbers_to_20_result.check(R.id.add_numbers_to_20_result1);
                break;
            case "2":
                add_numbers_to_20_result.check(R.id.add_numbers_to_20_result2);
                break;
            case "3":
                add_numbers_to_20_result.check(R.id.add_numbers_to_20_result3);
                break;
            default:
                add_numbers_to_20_result.check(R.id.add_numbers_to_20_resultN);
                break;
        }

        switch (resultsToShow.getSubtract_numbers_to_20() == null ? "Null" : resultsToShow.getSubtract_numbers_to_20()) {
            case "0":
                subtract_numbers_to_20_result.check(R.id.subtract_numbers_to_20_result0);
                break;
            case "1":
                subtract_numbers_to_20_result.check(R.id.subtract_numbers_to_20_result1);
                break;
            case "2":
                subtract_numbers_to_20_result.check(R.id.subtract_numbers_to_20_result2);
                break;
            case "3":
                subtract_numbers_to_20_result.check(R.id.subtract_numbers_to_20_result3);
                break;
            default:
                subtract_numbers_to_20_result.check(R.id.subtract_numbers_to_20_resultN);
                break;
        }

        switch (resultsToShow.getMultiply_1_digit_numbers() == null ? "Null" : resultsToShow.getMultiply_1_digit_numbers()) {
            case "0":
                multiply_1_digit_numbers_result.check(R.id.multiply_1_digit_numbers_result0);
                break;
            case "1":
                multiply_1_digit_numbers_result.check(R.id.multiply_1_digit_numbers_result1);
                break;
            case "2":
                multiply_1_digit_numbers_result.check(R.id.multiply_1_digit_numbers_result2);
                break;
            case "3":
                multiply_1_digit_numbers_result.check(R.id.multiply_1_digit_numbers_result3);
                break;
            default:
                multiply_1_digit_numbers_result.check(R.id.multiply_1_digit_numbers_resultN);
                break;
        }

        switch (resultsToShow.getDivide_2_digit_by_1_digit_numbers_to_20() == null ? "Null" : resultsToShow.getDivide_2_digit_by_1_digit_numbers_to_20()) {
            case "0":
                divide_2_digit_by_1_digit_numbers_to_20_result.check(R.id.divide_1_digit_numbers_result0);
                break;
            case "1":
                divide_2_digit_by_1_digit_numbers_to_20_result.check(R.id.divide_2_digit_by_1_digit_numbers_to_20_result1);
                break;
            case "2":
                divide_2_digit_by_1_digit_numbers_to_20_result.check(R.id.divide_2_digit_by_1_digit_numbers_to_20_result2);
                break;
            case "3":
                divide_2_digit_by_1_digit_numbers_to_20_result.check(R.id.divide_2_digit_by_1_digit_numbers_to_20_result3);
                break;
            default:
                divide_2_digit_by_1_digit_numbers_to_20_result.check(R.id.divide_1_digit_numbers_resultN);
                break;
        }

        // level 3:

        switch (resultsToShow.getIdentify_number_to_99() == null ? "Null" : resultsToShow.getIdentify_number_to_99()) {
            case "0":
                identify_number_to_99_result.check(R.id.identify_number_to_99_result0);
                break;
            case "1":
                identify_number_to_99_result.check(R.id.identify_number_to_99_result1);
                break;
            case "2":
                identify_number_to_99_result.check(R.id.identify_number_to_99_result2);
                break;
            case "3":
                identify_number_to_99_result.check(R.id.identify_number_to_99_result3);
                break;
            default:
                identify_number_to_99_result.check(R.id.identify_number_to_99_resultN);
                break;
        }

        switch (resultsToShow.getIdentify_greatest_number_to_150() == null ? "Null" : resultsToShow.getIdentify_greatest_number_to_150()) {
            case "0":
                identify_greatest_number_to_150_result.check(R.id.identify_greatest_number_to_150_result0);
                break;
            case "1":
                identify_greatest_number_to_150_result.check(R.id.identify_greatest_number_to_150_result1);
                break;
            case "2":
                identify_greatest_number_to_150_result.check(R.id.identify_greatest_number_to_150_result2);
                break;
            case "3":
                identify_greatest_number_to_150_result.check(R.id.identify_greatest_number_to_150_result3);
                break;
            default:
                identify_greatest_number_to_150_result.check(R.id.identify_greatest_number_to_150_resultN);
                break;
        }

        switch (resultsToShow.getCounting_in_2s_5s_missing_numbers() == null ? "Null" : resultsToShow.getCounting_in_2s_5s_missing_numbers()) {
            case "0":
                counting_in_2s_5s_missing_numbers_result.check(R.id.counting_in_2s_5s_missing_numbers_result0);
                break;
            case "1":
                counting_in_2s_5s_missing_numbers_result.check(R.id.counting_in_2s_5s_missing_numbers_result1);
                break;
            case "2":
                counting_in_2s_5s_missing_numbers_result.check(R.id.counting_in_2s_5s_missing_numbers_result2);
                break;
            default:
                counting_in_2s_5s_missing_numbers_result.check(R.id.counting_in_2s_5s_missing_numbers_resultN);
                break;
        }

        switch (resultsToShow.getAdd_two_2_digit_numbers() == null ? "Null" : resultsToShow.getAdd_two_2_digit_numbers()) {
            case "0":
                add_two_2_digit_numbers_result.check(R.id.add_two_2_digit_numbers_result0);
                break;
            case "1":
                add_two_2_digit_numbers_result.check(R.id.add_two_2_digit_numbers_result1);
                break;
            case "2":
                add_two_2_digit_numbers_result.check(R.id.add_two_2_digit_numbers_result2);
                break;
            case "3":
                add_two_2_digit_numbers_result.check(R.id.add_two_2_digit_numbers_result3);
                break;
            default:
                add_two_2_digit_numbers_result.check(R.id.add_two_2_digit_numbers_resultN);
                break;
        }

        switch (resultsToShow.getSubtract_1_digit_from_2_digits_to_99() == null ? "Null" : resultsToShow.getSubtract_1_digit_from_2_digits_to_99()) {
            case "0":
                subtract_1_digit_from_2_digits_to_99_result.check(R.id.subtract_1_digit_from_2_digits_to_99_result0);
                break;
            case "1":
                subtract_1_digit_from_2_digits_to_99_result.check(R.id.subtract_1_digit_from_2_digits_to_99_result1);
                break;
            case "2":
                subtract_1_digit_from_2_digits_to_99_result.check(R.id.subtract_1_digit_from_2_digits_to_99_result2);
                break;
            case "3":
                subtract_1_digit_from_2_digits_to_99_result.check(R.id.subtract_1_digit_from_2_digits_to_99_result3);
                break;
            default:
                subtract_1_digit_from_2_digits_to_99_result.check(R.id.subtract_1_digit_from_2_digits_to_99_resultN);
                break;
        }

        switch (resultsToShow.getMultiply_1_digit_by_2_digit_numbers() == null ? "Null" : resultsToShow.getMultiply_1_digit_by_2_digit_numbers()) {
            case "0":
                multiply_1_digit_by_2_digit_numbers_result.check(R.id.multiply_1_digit_by_2_digit_numbers_result0);
                break;
            case "1":
                multiply_1_digit_by_2_digit_numbers_result.check(R.id.multiply_1_digit_by_2_digit_numbers_result1);
                break;
            case "2":
                multiply_1_digit_by_2_digit_numbers_result.check(R.id.multiply_1_digit_by_2_digit_numbers_result2);
                break;
            case "3":
                multiply_1_digit_by_2_digit_numbers_result.check(R.id.multiply_1_digit_by_2_digit_numbers_result3);
                break;
            default:
                multiply_1_digit_by_2_digit_numbers_result.check(R.id.multiply_1_digit_by_2_digit_numbers_resultN);
                break;
        }

        switch (resultsToShow.getDivide_2_digit_by_1_digit_numbers_over_20() == null ? "Null" : resultsToShow.getDivide_2_digit_by_1_digit_numbers_over_20()) {
            case "0":
                divide_2_digit_by_1_digit_numbers_over_20_result.check(R.id.divide_2_digit_by_1_digit_numbers_over_20_result0);
                break;
            case "1":
                divide_2_digit_by_1_digit_numbers_over_20_result.check(R.id.divide_2_digit_by_1_digit_numbers_over_result1);
                break;
            case "2":
                divide_2_digit_by_1_digit_numbers_over_20_result.check(R.id.divide_2_digit_by_1_digit_numbers_over_result2);
                break;
            case "3":
                divide_2_digit_by_1_digit_numbers_over_20_result.check(R.id.divide_2_digit_by_1_digit_numbers_over_20_result3);
                break;
            default:
                divide_2_digit_by_1_digit_numbers_over_20_result.check(R.id.divide_2_digit_by_1_digit_numbers_over_20_resultN);
                break;
        }

        // level 4:

        switch (resultsToShow.getIdentify_number_to_999() == null ? "Null" : resultsToShow.getIdentify_number_to_999()) {
            case "0":
                identify_number_to_999_result.check(R.id.identify_number_to_999_result0);
                break;
            case "1":
                identify_number_to_999_result.check(R.id.identify_number_to_999_result1);
                break;
            case "2":
                identify_number_to_999_result.check(R.id.identify_number_to_999_result2);
                break;
            case "3":
                identify_number_to_999_result.check(R.id.identify_number_to_999_result3);
                break;
            default:
                identify_number_to_999_result.check(R.id.identify_number_to_999_resultN);
                break;
        }


        switch (resultsToShow.getIdentify_greatest_number_to_1100() == null ? "Null" : resultsToShow.getIdentify_greatest_number_to_1100()) {
            case "0":
                identify_greatest_number_to_1100_result.check(R.id.identify_greatest_number_to_1100_result0);
                break;
            case "1":
                identify_greatest_number_to_1100_result.check(R.id.identify_greatest_number_to_1100_result1);
                break;
            case "2":
                identify_greatest_number_to_1100_result.check(R.id.identify_greatest_number_to_1100_result2);
                break;
            case "3":
                identify_greatest_number_to_1100_result.check(R.id.identify_greatest_number_to_1100_result3);
                break;
            default:
                identify_greatest_number_to_1100_result.check(R.id.identify_greatest_number_to_1100_resultN);
                break;
        }

        switch (resultsToShow.getCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers() == null ? "Null" : resultsToShow.getCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers()) {
            case "0":
                counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result.check(R.id.counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result0);
                break;
            case "1":
                counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result.check(R.id.counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result1);
                break;
            case "2":
                counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result.check(R.id.counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result2);
                break;
            default:
                counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result.check(R.id.counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_resultN);
                break;
        }

        switch (resultsToShow.getAdd_two_and_three_digit_numbers() == null ? "Null" : resultsToShow.getAdd_two_and_three_digit_numbers()) {
            case "0":
                add_two_and_three_digit_numbers_result.check(R.id.add_two_and_three_digit_numbers_result0);
                break;
            case "1":
                add_two_and_three_digit_numbers_result.check(R.id.add_two_and_three_digit_numbers_result1);
                break;
            case "2":
                add_two_and_three_digit_numbers_result.check(R.id.add_two_and_three_digit_numbers_result2);
                break;
            case "3":
                add_two_and_three_digit_numbers_result.check(R.id.add_two_and_three_digit_numbers_result3);
                break;
            default:
                add_two_and_three_digit_numbers_result.check(R.id.add_two_and_three_digit_numbers_resultN);
                break;
        }

        switch (resultsToShow.getSubtract_2_digits_from_2_digits() == null ? "Null" : resultsToShow.getSubtract_2_digits_from_2_digits()) {
            case "0":
                subtract_2_digit_from_2_digits_result.check(R.id.subtract_2_digit_from_2_digits_result0);
                break;
            case "1":
                subtract_2_digit_from_2_digits_result.check(R.id.subtract_2_digit_from_2_digits_result1);
                break;
            case "2":
                subtract_2_digit_from_2_digits_result.check(R.id.subtract_2_digit_from_2_digits_result2);
                break;
            case "3":
                subtract_2_digit_from_2_digits_result.check(R.id.subtract_2_digit_from_2_digits_result3);
                break;
            default:
                subtract_2_digit_from_2_digits_result.check(R.id.subtract_2_digit_from_2_digits_resultN);
                break;
        }

        switch (resultsToShow.getMultiply_2_digit_by_2_digit_numbers_to_20() == null ? "Null" : resultsToShow.getMultiply_2_digit_by_2_digit_numbers_to_20()) {
            case "0":
                multiply_2_digit_by_2_digit_numbers_to_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_to_20_result0);
                break;
            case "1":
                multiply_2_digit_by_2_digit_numbers_to_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_to_20_result1);
                break;
            case "2":
                multiply_2_digit_by_2_digit_numbers_to_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_to_20_result2);
                break;
            case "3":
                multiply_2_digit_by_2_digit_numbers_to_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_to_20_result3);
                break;
            default:
                multiply_2_digit_by_2_digit_numbers_to_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_to_20_resultN);
                break;
        }

        switch (resultsToShow.getDivide_2_digit_by_1_digit_numbers_to_99() == null ? "Null" : resultsToShow.getDivide_2_digit_by_1_digit_numbers_to_99()) {
            case "0":
                divide_2_digit_by_1_digit_numbers_to_99_result.check(R.id.divide_2_digit_by_1_digit_numbers_to_99_result0);
                break;
            case "1":
                divide_2_digit_by_1_digit_numbers_to_99_result.check(R.id.divide_2_digit_by_1_digit_numbers_to_99_result1);
                break;
            case "2":
                divide_2_digit_by_1_digit_numbers_to_99_result.check(R.id.divide_2_digit_by_1_digit_numbers_to_99_result2);
                break;
            case "3":
                divide_2_digit_by_1_digit_numbers_to_99_result.check(R.id.divide_2_digit_by_1_digit_numbers_to_99_result3);
                break;
            default:
                divide_2_digit_by_1_digit_numbers_to_99_result.check(R.id.divide_2_digit_by_1_digit_numbers_to_99_resultN);
                break;
        }

        // level 5:

        switch (resultsToShow.getIdentify_number_to_9999999() == null ? "Null" : resultsToShow.getIdentify_number_to_9999999()) {
            case "0":
                identify_number_to_9999999_result.check(R.id.identify_number_to_9999999_result0);
                break;
            case "1":
                identify_number_to_9999999_result.check(R.id.identify_number_to_9999999_result1);
                break;
            case "2":
                identify_number_to_9999999_result.check(R.id.identify_number_to_9999999_result2);
                break;
            case "3":
                identify_number_to_9999999_result.check(R.id.identify_number_to_9999999_result3);
                break;
            default:
                identify_number_to_9999999_result.check(R.id.identify_number_to_9999999_resultN);
                break;
        }

        switch (resultsToShow.getIdentify_greatest_number_to_9999() == null ? "Null" : resultsToShow.getIdentify_greatest_number_to_9999()) {
            case "0":
                identify_greatest_number_to_9999_result.check(R.id.identify_greatest_number_to_9999_result0);
                break;
            case "1":
                identify_greatest_number_to_9999_result.check(R.id.identify_greatest_number_to_9999_result1);
                break;
            case "2":
                identify_greatest_number_to_9999_result.check(R.id.identify_greatest_number_to_9999_result2);
                break;
            case "3":
                identify_greatest_number_to_9999_result.check(R.id.identify_greatest_number_to_9999_result3);
                break;
            default:
                identify_greatest_number_to_9999_result.check(R.id.identify_greatest_number_to_9999_resultN);
                break;
        }

        switch (resultsToShow.getCounting_in_various_missing_numbers() == null ? "Null" : resultsToShow.getCounting_in_various_missing_numbers()) {
            case "0":
                counting_in_various_missing_numbers_result.check(R.id.counting_in_various_missing_numbers_result0);
                break;
            case "1":
                counting_in_various_missing_numbers_result.check(R.id.counting_in_various_missing_numbers_result1);
                break;
            case "2":
                counting_in_various_missing_numbers_result.check(R.id.counting_in_various_missing_numbers_result2);
                break;
            default:
                counting_in_various_missing_numbers_result.check(R.id.counting_in_various_missing_numbers_resultN);
                break;
        }

        switch (resultsToShow.getAdd_two_three_digit_numbers_including_deci() == null ? "Null" : resultsToShow.getAdd_two_three_digit_numbers_including_deci()) {
            case "0":
                add_two_three_digit_numbers_including_decimals_result.check(R.id.add_two_and_three_digit_numbers_include_decimals_result0);
                break;
            case "1":
                add_two_three_digit_numbers_including_decimals_result.check(R.id.add_two_and_three_digit_numbers_include_decimals_result1);
                break;
            case "2":
                add_two_three_digit_numbers_including_decimals_result.check(R.id.add_two_and_three_digit_numbers_include_decimals_result2);
                break;
            case "3":
                add_two_three_digit_numbers_including_decimals_result.check(R.id.add_two_and_three_digit_numbers_include_decimals_result3);
                break;
            default:
                add_two_three_digit_numbers_including_decimals_result.check(R.id.add_two_and_three_digit_numbers_include_decimals_resultN);
                break;
        }

        switch (resultsToShow.getSubtract_2_digit_from_3_digits_including_deci() == null ? "Null" : resultsToShow.getSubtract_2_digit_from_3_digits_including_deci()) {
            case "0":
                subtract_2_digit_from_3_digits_including_decimals_result.check(R.id.subtract_2_digit_from_3_digits_including_decimails_result0);
                break;
            case "1":
                subtract_2_digit_from_3_digits_including_decimals_result.check(R.id.subtract_2_digit_from_3_digits_including_decimails_result1);
                break;
            case "2":
                subtract_2_digit_from_3_digits_including_decimals_result.check(R.id.subtract_2_digit_from_3_digits_including_decimails_result2);
                break;
            case "3":
                subtract_2_digit_from_3_digits_including_decimals_result.check(R.id.subtract_2_digit_from_3_digits_including_decimails_result3);
                break;
            default:
                subtract_2_digit_from_3_digits_including_decimals_result.check(R.id.subtract_2_digit_from_3_digits_including_decimails_resultN);
                break;
        }

        switch (resultsToShow.getMultiply_2_digit_by_2_digit_numbers_over_20() == null ? "Null" : resultsToShow.getMultiply_2_digit_by_2_digit_numbers_over_20()) {
            case "0":
                multiply_2_digit_by_2_digit_numbers_over_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_over_20_result0);
                break;
            case "1":
                multiply_2_digit_by_2_digit_numbers_over_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_over_20_result1);
                break;
            case "2":
                multiply_2_digit_by_2_digit_numbers_over_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_over_20_result2);
                break;
            case "3":
                multiply_2_digit_by_2_digit_numbers_over_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_over_20_result3);
                break;
            default:
                multiply_2_digit_by_2_digit_numbers_over_20_result.check(R.id.multiply_2_digit_by_2_digit_numbers_over_20_resultN);
                break;
        }

        switch (resultsToShow.getDivide_2_and_3_digit_by_2_digit_numbers() == null || resultsToShow.getDivide_2_and_3_digit_by_2_digit_numbers().equals("Null") ? "Null" : resultsToShow.getDivide_2_and_3_digit_by_2_digit_numbers()) {
            case "0":
                divide_2_and_3_digit_by_2_digit_numbers_result.check(R.id.divide_2_and_3_digit_by_2_digit_numbers_result0);
                break;
            case "1":
                divide_2_and_3_digit_by_2_digit_numbers_result.check(R.id.divide_2_and_3_digit_by_2_digit_numbers_result1);
                break;
            case "2":
                divide_2_and_3_digit_by_2_digit_numbers_result.check(R.id.divide_2_and_3_digit_by_2_digit_numbers_result2);
                break;
            case "3":
                divide_2_and_3_digit_by_2_digit_numbers_result.check(R.id.divide_2_and_3_digit_by_2_digit_numbers_result3);
                break;
            default:
                divide_2_and_3_digit_by_2_digit_numbers_result.check(R.id.divide_2_and_3_digit_by_2_digit_numbers_resultN);
                break;
        }

        //WP_Level
        if (resultsToShow.getWp_level() != null) {
            switch (resultsToShow.getWp_level()) {
                case "1":
                    currentStatusTv.setText("Basic");
                    setWlLevelContent("Basic");
                    break;
                case "2":
                    currentStatusTv.setText("Intermediate");
                    setWlLevelContent("Intermediate");
                    break;
                case "3":
                    currentStatusTv.setText("Advanced");
                    setWlLevelContent("Advanced");
                    break;
                default:
                    currentStatusTv.setText("N");
                    setWlLevelContent("Null");
                    break;
            }
        } else {
            currentStatusTv.setText("N");
            setWlLevelContent("Null");
        }

        // word problems

        switch (resultsToShow.getWp_addition() == null ? "Null" : resultsToShow.getWp_addition()) {
            case "0":
                addition_result.check(R.id.addition_result0);
                break;
            case "1":
                addition_result.check(R.id.addition_result1);
                break;
            case "2":
                addition_result.check(R.id.addition_result2);
                break;
            default:
                addition_result.check(R.id.addition_resultN);
                break;
        }

        switch (resultsToShow.getWp_subtraction() == null ? "Null" : resultsToShow.getWp_subtraction()) {
            case "0":
                subtraction_result.check(R.id.subtraction_result0);
                break;
            case "1":
                subtraction_result.check(R.id.subtraction_result1);
                break;
            case "2":
                subtraction_result.check(R.id.subtraction_result2);
                break;
            default:
                subtraction_result.check(R.id.subtraction_resultN);
                break;

        }

        switch (resultsToShow.getWp_multiplication() == null ? "Null" : resultsToShow.getWp_multiplication()) {
            case "0":
                multiplication_result.check(R.id.multiplication_result0);
                break;
            case "1":
                multiplication_result.check(R.id.multiplication_result1);
                break;
            case "2":
                multiplication_result.check(R.id.multiplication_result2);
                break;
            default:
                multiplication_result.check(R.id.multiplication_resultN);
                break;
        }

        switch (resultsToShow.getWp_division() == null ? "Null" : resultsToShow.getWp_division()) {
            case "0":
                division_result.check(R.id.division_result0);
                break;
            case "1":
                division_result.check(R.id.division_result1);
                break;
            case "2":
                division_result.check(R.id.division_result2);
                break;
            default:
                division_result.check(R.id.division_resultN);
                break;
        }

        refreshAndGetTotals();
    }

    private void refreshAndGetTotals() {
        // level 1:

        String counting_objects_to_10 = ((RadioButton) findViewById(counting_objects_to_10_result.getCheckedRadioButtonId())).getText().toString();
        int counting_objects_to_10_total = (counting_objects_to_10.equalsIgnoreCase("N") ? 0 : Integer.parseInt(counting_objects_to_10));

        String identify_number_to_10 = ((RadioButton) findViewById(identify_number_to_10_result.getCheckedRadioButtonId())).getText().toString();
        int identify_number_to_10_total = (identify_number_to_10.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_number_to_10));

        String identify_greatest_number_to_20 = ((RadioButton) findViewById(identify_greatest_number_to_20_result.getCheckedRadioButtonId())).getText().toString();
        int identify_greatest_number_to_20_total = (identify_greatest_number_to_20.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_greatest_number_to_20));

        String counting_in_ones_missing_numbers = ((RadioButton) findViewById(counting_in_ones_missing_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int counting_in_ones_missing_numbers_total = (counting_in_ones_missing_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(counting_in_ones_missing_numbers));

        String add_numbers_to_10 = ((RadioButton) findViewById(add_numbers_to_10_result.getCheckedRadioButtonId())).getText().toString();
        int add_numbers_to_10_total = (add_numbers_to_10.equalsIgnoreCase("N") ? 0 : Integer.parseInt(add_numbers_to_10));

        String subtract_numbers_to_10 = ((RadioButton) findViewById(subtract_numbers_to_10_result.getCheckedRadioButtonId())).getText().toString();
        int subtract_numbers_to_10_total = (subtract_numbers_to_10.equalsIgnoreCase("N") ? 0 : Integer.parseInt(subtract_numbers_to_10));

        // level 2:

        String identify_number_to_20 = ((RadioButton) findViewById(identify_number_to_20_result.getCheckedRadioButtonId())).getText().toString();
        int identify_number_to_20_total = (identify_number_to_20.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_number_to_20));

        String identify_greatest_number_to_50 = ((RadioButton) findViewById(identify_greatest_number_to_50_result.getCheckedRadioButtonId())).getText().toString();
        int identify_greatest_number_to_50_total = (identify_greatest_number_to_50.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_greatest_number_to_50));

        String counting_in_tens_missing_numbers = ((RadioButton) findViewById(counting_in_tens_missing_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int counting_in_tens_missing_numbers_total = (counting_in_tens_missing_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(counting_in_tens_missing_numbers));

        String add_numbers_to_20 = ((RadioButton) findViewById(add_numbers_to_20_result.getCheckedRadioButtonId())).getText().toString();
        int add_numbers_to_20_total = (add_numbers_to_20.equalsIgnoreCase("N") ? 0 : Integer.parseInt(add_numbers_to_20));

        String subtract_numbers_to_20 = ((RadioButton) findViewById(subtract_numbers_to_20_result.getCheckedRadioButtonId())).getText().toString();
        int subtract_numbers_to_20_total = (subtract_numbers_to_20.equalsIgnoreCase("N") ? 0 : Integer.parseInt(subtract_numbers_to_20));

        String multiply_1_digit_numbers = ((RadioButton) findViewById(multiply_1_digit_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int multiply_1_digit_numbers_total = (multiply_1_digit_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(multiply_1_digit_numbers));

        String divide_2_digit_by_1_digit_numbers_to_20 = ((RadioButton) findViewById(divide_2_digit_by_1_digit_numbers_to_20_result.getCheckedRadioButtonId())).getText().toString();
        int divide_2_digit_by_1_digit_numbers_to_20_total = (divide_2_digit_by_1_digit_numbers_to_20.equalsIgnoreCase("N") ? 0 : Integer.parseInt(divide_2_digit_by_1_digit_numbers_to_20));

        // level 3:

        String identify_number_to_99 = ((RadioButton) findViewById(identify_number_to_99_result.getCheckedRadioButtonId())).getText().toString();
        int identify_number_to_99_total = (identify_number_to_99.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_number_to_99));

        String identify_greatest_number_to_150 = ((RadioButton) findViewById(identify_greatest_number_to_150_result.getCheckedRadioButtonId())).getText().toString();
        int identify_greatest_number_to_150_total = (identify_greatest_number_to_150.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_greatest_number_to_150));

        String counting_in_2s_5s_missing_numbers = ((RadioButton) findViewById(counting_in_2s_5s_missing_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int counting_in_2s_5s_missing_numbers_total = (counting_in_2s_5s_missing_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(counting_in_2s_5s_missing_numbers));

        String add_two_2_digit_numbers = ((RadioButton) findViewById(add_two_2_digit_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int add_two_2_digit_numbers_total = (add_two_2_digit_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(add_two_2_digit_numbers));

        String subtract_1_digit_from_2_digits_to_99 = ((RadioButton) findViewById(subtract_1_digit_from_2_digits_to_99_result.getCheckedRadioButtonId())).getText().toString();
        int subtract_1_digit_from_2_digits_to_99_total = (subtract_1_digit_from_2_digits_to_99.equalsIgnoreCase("N") ? 0 : Integer.parseInt(subtract_1_digit_from_2_digits_to_99));

        String multiply_1_digit_by_2_digit_numbers = ((RadioButton) findViewById(multiply_1_digit_by_2_digit_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int multiply_1_digit_by_2_digit_numbers_total = (multiply_1_digit_by_2_digit_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(multiply_1_digit_by_2_digit_numbers));

        String divide_2_digit_by_1_digit_numbers_over_20 = ((RadioButton) findViewById(divide_2_digit_by_1_digit_numbers_over_20_result.getCheckedRadioButtonId())).getText().toString();
        int divide_2_digit_by_1_digit_numbers_over_20_total = (divide_2_digit_by_1_digit_numbers_over_20.equalsIgnoreCase("N") ? 0 : Integer.parseInt(divide_2_digit_by_1_digit_numbers_over_20));

        // level 4:
        String identify_number_to_999 = ((RadioButton) findViewById(identify_number_to_999_result.getCheckedRadioButtonId())).getText().toString();
        int identify_number_to_999_total = (identify_number_to_999.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_number_to_999));

        String identify_greatest_number_to_1100 = ((RadioButton) findViewById(identify_greatest_number_to_1100_result.getCheckedRadioButtonId())).getText().toString();
        int identify_greatest_number_to_1100_total = (identify_greatest_number_to_1100.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_greatest_number_to_1100));

        String counting_in_3s_4s_6s_7s_8s_9s_missing_numbers = ((RadioButton) findViewById(counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_total = (counting_in_3s_4s_6s_7s_8s_9s_missing_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(counting_in_3s_4s_6s_7s_8s_9s_missing_numbers));

        String add_two_and_three_digit_numbers = ((RadioButton) findViewById(add_two_and_three_digit_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int add_two_and_three_digit_numbers_total = (add_two_and_three_digit_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(add_two_and_three_digit_numbers));

        String subtract_2_digit_from_2_digits = ((RadioButton) findViewById(subtract_2_digit_from_2_digits_result.getCheckedRadioButtonId())).getText().toString();
        int subtract_2_digit_from_2_digits_total = (subtract_2_digit_from_2_digits.equalsIgnoreCase("N") ? 0 : Integer.parseInt(subtract_2_digit_from_2_digits));

        String multiply_2_digit_by_2_digit_numbers_to_20 = ((RadioButton) findViewById(multiply_2_digit_by_2_digit_numbers_to_20_result.getCheckedRadioButtonId())).getText().toString();
        int multiply_2_digit_by_2_digit_numbers_to_20_total = (multiply_2_digit_by_2_digit_numbers_to_20.equalsIgnoreCase("N") ? 0 : Integer.parseInt(multiply_2_digit_by_2_digit_numbers_to_20));

        String divide_2_digit_by_1_digit_numbers_to_99 = ((RadioButton) findViewById(divide_2_digit_by_1_digit_numbers_to_99_result.getCheckedRadioButtonId())).getText().toString();
        int divide_2_digit_by_1_digit_numbers_to_99_total = (divide_2_digit_by_1_digit_numbers_to_99.equalsIgnoreCase("N") ? 0 : Integer.parseInt(divide_2_digit_by_1_digit_numbers_to_99));

        // level 5:

        String identify_number_to_9999999 = ((RadioButton) findViewById(identify_number_to_9999999_result.getCheckedRadioButtonId())).getText().toString();
        int identify_number_to_9999999_total = (identify_number_to_9999999.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_number_to_9999999));

        String identify_greatest_number_to_9999 = ((RadioButton) findViewById(identify_greatest_number_to_9999_result.getCheckedRadioButtonId())).getText().toString();
        int identify_greatest_number_to_9999_total = (identify_greatest_number_to_9999.equalsIgnoreCase("N") ? 0 : Integer.parseInt(identify_greatest_number_to_9999));

        String counting_in_various_missing_numbers = ((RadioButton) findViewById(counting_in_various_missing_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int counting_in_various_missing_numbers_total = (counting_in_various_missing_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(counting_in_various_missing_numbers));

        String add_two_three_digit_numbers_including_decimals = ((RadioButton) findViewById(add_two_three_digit_numbers_including_decimals_result.getCheckedRadioButtonId())).getText().toString();
        int add_two_and_three_digit_numbers_include_decimals_total = (add_two_three_digit_numbers_including_decimals.equalsIgnoreCase("N") ? 0 : Integer.parseInt(add_two_three_digit_numbers_including_decimals));

        String subtract_2_digit_from_3_digits_including_decimals = ((RadioButton) findViewById(subtract_2_digit_from_3_digits_including_decimals_result.getCheckedRadioButtonId())).getText().toString();
        int subtract_2_digit_from_3_digits_including_decimals_total = (subtract_2_digit_from_3_digits_including_decimals.equalsIgnoreCase("N") ? 0 : Integer.parseInt(subtract_2_digit_from_3_digits_including_decimals));

        String multiply_2_digit_by_2_digit_numbers_over_20 = ((RadioButton) findViewById(multiply_2_digit_by_2_digit_numbers_over_20_result.getCheckedRadioButtonId())).getText().toString();
        int multiply_2_digit_by_2_digit_numbers_over_20_result_total = (multiply_2_digit_by_2_digit_numbers_over_20.equalsIgnoreCase("N") ? 0 : Integer.parseInt(multiply_2_digit_by_2_digit_numbers_over_20));

        String divide_2_and_3_digit_by_2_digit_numbers = ((RadioButton) findViewById(divide_2_and_3_digit_by_2_digit_numbers_result.getCheckedRadioButtonId())).getText().toString();
        int divide_2_and_3_digit_by_2_digit_numbers_total = (divide_2_and_3_digit_by_2_digit_numbers.equalsIgnoreCase("N") ? 0 : Integer.parseInt(divide_2_and_3_digit_by_2_digit_numbers));

        // word problems
        RadioButton mButtonAdd = findViewById(addition_result.getCheckedRadioButtonId());
        int addition_total = 0;
        if (mButtonAdd != null) {
            addition_total = (mButtonAdd.getText().toString().equalsIgnoreCase("N") ? 0 : Integer.parseInt(mButtonAdd.getText().toString()));
        }

        RadioButton mButtonSub = findViewById(subtraction_result.getCheckedRadioButtonId());
        int subtraction_total = 0;
        if (mButtonSub != null) {
            subtraction_total = (mButtonSub.getText().toString().equalsIgnoreCase("N") ? 0 : Integer.parseInt(mButtonSub.getText().toString()));
        }

        RadioButton mButtonMul = findViewById(multiplication_result.getCheckedRadioButtonId());
        int multiplication_total = 0;
        if (mButtonMul != null) {
            multiplication_total = (mButtonMul.getText().toString().equalsIgnoreCase("N") ? 0 : Integer.parseInt(mButtonMul.getText().toString()));
        }

        RadioButton mButtonDiv = findViewById(division_result.getCheckedRadioButtonId());
        int division_total = 0;
        if (mButtonDiv != null) {
            division_total = (mButtonDiv.getText().toString().equalsIgnoreCase("N") ? 0 : Integer.parseInt(mButtonDiv.getText().toString()));
        }

        int numbersSubtotal =
                // level 1:
                counting_objects_to_10_total + identify_number_to_10_total + identify_greatest_number_to_20_total + counting_in_ones_missing_numbers_total + add_numbers_to_10_total + subtract_numbers_to_10_total
                        // level 2:
                        + identify_number_to_20_total + identify_greatest_number_to_50_total + counting_in_tens_missing_numbers_total + add_numbers_to_20_total + subtract_numbers_to_20_total + multiply_1_digit_numbers_total + divide_2_digit_by_1_digit_numbers_to_20_total
                        // level 3:
                        + identify_number_to_99_total + identify_greatest_number_to_150_total + counting_in_2s_5s_missing_numbers_total + add_two_2_digit_numbers_total + subtract_1_digit_from_2_digits_to_99_total + multiply_1_digit_by_2_digit_numbers_total + divide_2_digit_by_1_digit_numbers_over_20_total +
                        // level 4:
                        +identify_number_to_999_total + identify_greatest_number_to_1100_total + counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_total + add_two_and_three_digit_numbers_total + subtract_2_digit_from_2_digits_total + multiply_2_digit_by_2_digit_numbers_to_20_total + divide_2_digit_by_1_digit_numbers_to_99_total
                        // level 5:
                        + identify_number_to_9999999_total + identify_greatest_number_to_9999_total + counting_in_various_missing_numbers_total + add_two_and_three_digit_numbers_include_decimals_total + subtract_2_digit_from_3_digits_including_decimals_total + multiply_2_digit_by_2_digit_numbers_over_20_result_total + divide_2_and_3_digit_by_2_digit_numbers_total;
        int wordsSubtotal = addition_total + subtraction_total + multiplication_total + division_total;
        int total = numbersSubtotal + wordsSubtotal;

        this.numbersSubtotal.setText(getString(R.string.numbers_subtotal) + " " + numbersSubtotal + "/97");

        String wpLevel = "";
        String wp_level = currentStatusTv.getText().toString();
        switch (wp_level) {
            case "Basic":
                wpLevel = "(Basic)";
                break;
            case "Intermediate":
                wpLevel = "(Intermediate)";
                break;
            case "Advanced":
                wpLevel = "(Advanced)";
                break;
            default:
                wpLevel = "";
                break;
        }
        wordsSubtotalTv.setText(getString(R.string.words_subtotal) + " " + wordsSubtotal + "/8 " + wpLevel);

        overallTotal.setText(getString(R.string.overall_total) + " " + total + "/105");

    }

    private void saveResultsToUser() {
        if (currentStudent == null)
            return;

        String currentTerm = ((RadioButton) findViewById((termRG.getCheckedRadioButtonId()))).getText().toString();

        List<MathsResults> results = currentStudent.getMathsResults();
        boolean newResults = true;
        MathsResults resultsToSave = new MathsResults();
        outter:
        if (results != null && results.size() > 0) {
            // Find the reading term's results to show, with "int term" argument taking priority
            for (MathsResults result : results) {
                if (result != null && result.getTerm().equals(currentTerm)) {
                    newResults = false;
                    resultsToSave = result;
                    break outter;
                }
            }
            newResults = true;
            resultsToSave = new MathsResults();
            resultsToSave.setTerm(currentTerm);
        } else {
            Log.e(MenuActivity.TAG, "No existing results for student to modify for term " + currentTerm);
            resultsToSave.setTerm(currentTerm);
            newResults = true;
        }

        switch (resultsToSave.getTerm()) {
            case "1":
                termRG.check(R.id.readingTerm1);
                break;
            case "2":
                termRG.check(R.id.readingTerm2);
                break;
            case "3":
                termRG.check(R.id.readingTerm3);
                break;
        }

        resultsToSave.setTerm(currentTerm);

        // level 1:
        String mLevel1Math1 = ((RadioButton) findViewById((counting_objects_to_10_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setCounting_objects_to_10(mLevel1Math1.equalsIgnoreCase("N") ? "Null" : mLevel1Math1);

        String mLevel1Math2 = ((RadioButton) findViewById((identify_number_to_10_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_number_to_10(mLevel1Math2.equalsIgnoreCase("N") ? "Null" : mLevel1Math2);

        String mLevel1Math3 = ((RadioButton) findViewById((identify_greatest_number_to_20_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_greatest_number_to_20(mLevel1Math3.equalsIgnoreCase("N") ? "Null" : mLevel1Math3);

        String mLevel1Math4 = ((RadioButton) findViewById((counting_in_ones_missing_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setCounting_in_ones_missing_numbers(mLevel1Math4.equalsIgnoreCase("N") ? "Null" : mLevel1Math4);

        String mLevel1Math5 = ((RadioButton) findViewById((add_numbers_to_10_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setAdd_numbers_to_10(mLevel1Math5.equalsIgnoreCase("N") ? "Null" : mLevel1Math5);

        String mLevel1Math6 = ((RadioButton) findViewById((subtract_numbers_to_10_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setSubtract_numbers_to_10(mLevel1Math6.equalsIgnoreCase("N") ? "Null" : mLevel1Math6);

        // level 2:
        String mLevel2Math1 = ((RadioButton) findViewById((identify_number_to_20_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_number_to_20(mLevel2Math1.equalsIgnoreCase("N") ? "Null" : mLevel2Math1);

        String mLevel2Math2 = ((RadioButton) findViewById((identify_greatest_number_to_50_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_greatest_number_to_50(mLevel2Math2.equalsIgnoreCase("N") ? "Null" : mLevel2Math2);

        String mLevel2Math3 = ((RadioButton) findViewById((counting_in_tens_missing_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setCounting_in_tens_missing_numbers(mLevel2Math3.equalsIgnoreCase("N") ? "Null" : mLevel2Math3);

        String mLevel2Math4 = ((RadioButton) findViewById((add_numbers_to_20_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setAdd_numbers_to_20(mLevel2Math4.equalsIgnoreCase("N") ? "Null" : mLevel2Math4);

        String mLevel2Math5 = ((RadioButton) findViewById((subtract_numbers_to_20_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setSubtract_numbers_to_20(mLevel2Math5.equalsIgnoreCase("N") ? "Null" : mLevel2Math5);

        String mLevel2Math6 = ((RadioButton) findViewById((multiply_1_digit_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setMultiply_1_digit_numbers(mLevel2Math6.equalsIgnoreCase("N") ? "Null" : mLevel2Math6);

        String mLevel2Math7 = ((RadioButton) findViewById((divide_2_digit_by_1_digit_numbers_to_20_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setDivide_2_digit_by_1_digit_numbers_to_20(mLevel2Math7.equalsIgnoreCase("N") ? "Null" : mLevel2Math7);

        // level 3:
        String mLevel3Math1 = ((RadioButton) findViewById((identify_number_to_99_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_number_to_99(mLevel3Math1.equalsIgnoreCase("N") ? "Null" : mLevel3Math1);

        String mLevel3Math2 = ((RadioButton) findViewById((identify_greatest_number_to_150_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_greatest_number_to_150(mLevel3Math2.equalsIgnoreCase("N") ? "Null" : mLevel3Math2);

        String mLevel3Math3 = ((RadioButton) findViewById((counting_in_2s_5s_missing_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setCounting_in_2s_5s_missing_numbers(mLevel3Math3.equalsIgnoreCase("N") ? "Null" : mLevel3Math3);

        String mLevel3Math4 = ((RadioButton) findViewById((add_two_2_digit_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setAdd_two_2_digit_numbers(mLevel3Math4.equalsIgnoreCase("N") ? "Null" : mLevel3Math4);

        String mLevel3Math5 = ((RadioButton) findViewById((subtract_1_digit_from_2_digits_to_99_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setSubtract_1_digit_from_2_digits_to_99(mLevel3Math5.equalsIgnoreCase("N") ? "Null" : mLevel3Math5);

        String mLevel3Math6 = ((RadioButton) findViewById((multiply_1_digit_by_2_digit_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setMultiply_1_digit_by_2_digit_numbers(mLevel3Math6.equalsIgnoreCase("N") ? "Null" : mLevel3Math6);

        String mLevel3Math7 = ((RadioButton) findViewById((divide_2_digit_by_1_digit_numbers_over_20_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setDivide_2_digit_by_1_digit_numbers_over_20(mLevel3Math7.equalsIgnoreCase("N") ? "Null" : mLevel3Math7);

        // level 4:
        String mLevel4Math1 = ((RadioButton) findViewById((identify_number_to_999_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_number_to_999(mLevel4Math1.equalsIgnoreCase("N") ? "Null" : mLevel4Math1);

        String mLevel4Math2 = ((RadioButton) findViewById((identify_greatest_number_to_1100_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_greatest_number_to_1100(mLevel4Math2.equalsIgnoreCase("N") ? "Null" : mLevel4Math2);

        String mLevel4Math3 = ((RadioButton) findViewById((counting_in_3s_4s_6s_7s_8s_9s_missing_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers(mLevel4Math3.equalsIgnoreCase("N") ? "Null" : mLevel4Math3);

        String mLevel4Math4 = ((RadioButton) findViewById((add_two_and_three_digit_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setAdd_two_and_three_digit_numbers(mLevel4Math4.equalsIgnoreCase("N") ? "Null" : mLevel4Math4);

        String mLevel4Math5 = ((RadioButton) findViewById((subtract_2_digit_from_2_digits_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setSubtract_2_digits_from_2_digits(mLevel4Math5.equalsIgnoreCase("N") ? "Null" : mLevel4Math5);

        String mLevel4Math6 = ((RadioButton) findViewById((multiply_2_digit_by_2_digit_numbers_to_20_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setMultiply_2_digit_by_2_digit_numbers_to_20(mLevel4Math6.equalsIgnoreCase("N") ? "Null" : mLevel4Math6);

        String mLevel4Math7 = ((RadioButton) findViewById((divide_2_digit_by_1_digit_numbers_to_99_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setDivide_2_digit_by_1_digit_numbers_to_99(mLevel4Math7.equalsIgnoreCase("N") ? "Null" : mLevel4Math7);

        // level 5:
        String mLevel5Math1 = ((RadioButton) findViewById((identify_number_to_9999999_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_number_to_9999999(mLevel5Math1.equalsIgnoreCase("N") ? "Null" : mLevel5Math1);

        String mLevel5Math2 = ((RadioButton) findViewById((identify_greatest_number_to_9999_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setIdentify_greatest_number_to_9999(mLevel5Math2.equalsIgnoreCase("N") ? "Null" : mLevel5Math2);

        String mLevel5Math3 = ((RadioButton) findViewById((counting_in_various_missing_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setCounting_in_various_missing_numbers(mLevel5Math3.equalsIgnoreCase("N") ? "Null" : mLevel5Math3);

        String mLevel5Math4 = ((RadioButton) findViewById((add_two_three_digit_numbers_including_decimals_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setAdd_two_three_digit_numbers_including_deci(mLevel5Math4.equalsIgnoreCase("N") ? "Null" : mLevel5Math4);

        String mLevel5Math5 = ((RadioButton) findViewById((subtract_2_digit_from_3_digits_including_decimals_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setSubtract_2_digit_from_3_digits_including_deci(mLevel5Math5.equalsIgnoreCase("N") ? "Null" : mLevel5Math5);

        String mLevel5Math6 = ((RadioButton) findViewById((multiply_2_digit_by_2_digit_numbers_over_20_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setMultiply_2_digit_by_2_digit_numbers_over_20(mLevel5Math6.equalsIgnoreCase("N") ? "Null" : mLevel5Math6);

        String mLevel5Math7 = ((RadioButton) findViewById((divide_2_and_3_digit_by_2_digit_numbers_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setDivide_2_and_3_digit_by_2_digit_numbers(mLevel5Math7.equalsIgnoreCase("N") ? "Null" : mLevel5Math7);

        // word problems
        String mLevelWordMath1 = ((RadioButton) findViewById((addition_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setWp_addition(mLevelWordMath1.equalsIgnoreCase("N") ? "Null" : mLevelWordMath1);

        String mLevelWordMath2 = ((RadioButton) findViewById((subtraction_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setWp_subtraction(mLevelWordMath2.equalsIgnoreCase("N") ? "Null" : mLevelWordMath2);

        String mLevelWordMath3 = ((RadioButton) findViewById((multiplication_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setWp_multiplication(mLevelWordMath3.equalsIgnoreCase("N") ? "Null" : mLevelWordMath3);

        String mLevelWordMath4 = ((RadioButton) findViewById((division_result.getCheckedRadioButtonId()))).getText().toString();
        resultsToSave.setWp_division(mLevelWordMath4.equalsIgnoreCase("N") ? "Null" : mLevelWordMath4);

        // set UPN
        // resultsToSave.setUPN(myUTN);
        resultsToSave.setUPN(currentStudent.getUpn());

        // upload wp_level
        String currentLevel = currentStatusTv.getText().toString();

        switch (currentLevel) {
            case "Basic":
                resultsToSave.setWp_level("1");
                break;
            case "Intermediate":
                resultsToSave.setWp_level("2");
                break;
            case "Advanced":
                resultsToSave.setWp_level("3");
                break;
            default:
                resultsToSave.setWp_level("Null");
                break;
        }

        if (newResults) {
            if (currentStudent.getMathsResults() == null) {
                currentStudent.setMathsResults(new ArrayList<MathsResults>());
            }
            currentStudent.getMathsResults().add(resultsToSave);
            Log.d(MenuActivity.TAG, "saved new results");
        } else {
            Log.d(MenuActivity.TAG, "updated existing results");
        }
    }

    private void onSaveResultsButtonClicked() {
        int currentTerm = Integer.parseInt(((RadioButton) findViewById((termRG.getCheckedRadioButtonId()))).getText().toString());

        if (currentTerm == 1 && MasterCSVModel.term1Locked) {
            Toast.makeText(this, R.string.term1locked, Toast.LENGTH_SHORT).show();
            return;
        } else if (currentTerm == 2 && MasterCSVModel.term2Locked) {
            Toast.makeText(this, R.string.term2locked, Toast.LENGTH_SHORT).show();
            return;
        } else if (currentTerm == 3 && MasterCSVModel.term3Locked) {
            Toast.makeText(this, R.string.term3locked, Toast.LENGTH_SHORT).show();
            return;
        }

        saveResultsToUser();

        saveResultsButton.setEnabled(false);
        saveResultsButton.setText("Saving....");

        backendlessUser.setProperty("students", students);

        SerializableManager.saveSerializable(getBaseContext(), backendlessUser, "backendlessUser_" + backendlessUser.getProperty("utn"));

        MathsResults noObjectIdResult = null;
        int i = 0;
        for (; i < currentStudent.getMathsResults().size(); i++) {
            MathsResults result = currentStudent.getMathsResults().get(i);
            if (result.getObjectId() == null) { // takes the first null results object, ignores the rest
                noObjectIdResult = result;
                break;
            }
        }

        if (noObjectIdResult == null) {
            // find currently select term already saved to update it
            switch (termRG.getCheckedRadioButtonId()) {
                case R.id.readingTerm1:
                    i = 0;
                    break;
                case R.id.readingTerm2:
                    i = 1;
                    break;
                case R.id.readingTerm3:
                    i = 2;
                    break;
            }
            final int i2 = i;
            MathsResults resultsToSave = null;
            for (MathsResults result : currentStudent.getMathsResults()) {
                if (Integer.parseInt(result.getTerm()) - 1 == i) {
                    resultsToSave = result;
                    break;
                }
            }
            resultsToSave.saveAsync(new AsyncCallback<MathsResults>() {
                @Override
                public void handleResponse(MathsResults response) {
                    int n = 0;
                    boolean updated = false;
                    for (MathsResults result : currentStudent.getMathsResults()) {
                        if (Integer.parseInt(result.getTerm()) == i2) {
                            currentStudent.getMathsResults().set(n, response);
                            updated = true;
                            break;
                        }
                        n += 1;
                    }
                    if (!updated)
                        currentStudent.getMathsResults().add(response);
                    doSaveStuff2();
                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {
                    saveResultsButton.setEnabled(true);
                    saveResultsButton.setText("Save MathsResults");
                    customProgressBar.dismissMe();

                    if (backendlessFault != null && backendlessFault.getMessage() != null
                            && (backendlessFault.getMessage().contains("Unable to resolve")
                            || backendlessFault.getMessage().contains("timeout"))
                    ) {
                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("needSync_" + backendlessUser.getProperty("utn"), true).apply();
                        Toast.makeText(getApplicationContext(), "Changes saved locally because you're offline!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to save changes! " + backendlessFault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            final int i2 = i;
            noObjectIdResult.saveAsync(new AsyncCallback<MathsResults>() {
                @Override
                public void handleResponse(MathsResults response) {
                    try {
                        currentStudent.getMathsResults().set(i2, response);
                        doSaveStuff2();
                    } catch (Exception e) {
                        // TODO:remove this after Sheena gets us the error
                        Toast.makeText(getApplicationContext(), "Report Maths Error: currentStudent: " + currentStudent.getObjectId() + ", i2: " + i2 + ", response: " + (response != null ? response.getObjectId() : "null") + ", " + e.getMessage() + (e.getCause() != null ? " | " + e.getCause().getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {
                    saveResultsButton.setEnabled(true);
                    saveResultsButton.setText("Save MathsResults");
                    customProgressBar.dismissMe();

                    if (backendlessFault != null && backendlessFault.getMessage() != null
                            && (backendlessFault.getMessage().contains("Unable to resolve")
                            || backendlessFault.getMessage().contains("timeout"))
                    ) {
                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("needSync_" + backendlessUser.getProperty("utn"), true).apply();
                        Toast.makeText(getApplicationContext(), "Changes saved locally because you're offline!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to save changes! " + backendlessFault.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void doSaveStuff2() {
        Backendless.Data.of(Students.class).setRelation(currentStudent, "mathsResults", currentStudent.getMathsResults(), new AsyncCallback<Integer>() {
            //Backendless.Data.of(Students.class).setRelation(currentStudent, "Mathsresults", currentStudent.getMathsResults(), new AsyncCallback<Integer>() {
            @Override
            public void handleResponse(Integer response) {
                for (Students student : students) {
                    if (student.getObjectId().equals(currentStudent.getObjectId())) {
                        currentStudent = student;
                        break;
                    }
                }
                SerializableManager.saveSerializable(getBaseContext(), backendlessUser, "backendlessUser_" + backendlessUser.getProperty("utn"));

                saveResultsButton.setEnabled(true);
                saveResultsButton.setText("Save MathsResults");
                customProgressBar.dismissMe();

                Toast.makeText(getApplicationContext(), "Saved changes", Toast.LENGTH_LONG).show();
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                saveResultsButton.setEnabled(true);
                saveResultsButton.setText("Save MathsResults");
                customProgressBar.dismissMe();

                if (backendlessFault != null && backendlessFault.getMessage() != null
                        && (backendlessFault.getMessage().contains("Unable to resolve")
                        || backendlessFault.getMessage().contains("timeout"))
                ) {
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("needSync_" + backendlessUser.getProperty("utn"), true).apply();
                    Toast.makeText(getApplicationContext(), "Changes saved locally because you're offline!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to save changes! " + backendlessFault.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void printResults() {
        new PrintResultsTask(getApplicationContext()).execute();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void onLogoutButtonClicked() {
        customProgressBar.showMe(PostLoginRecordMathsResultsManagerActivity.this, "Please Wait...");
        Backendless.UserService.logout(new DefaultCallback<Void>(this) {
            @Override
            public void handleResponse(Void response) {
                super.handleResponse(response);
                customProgressBar.dismissMe();
                //setIsLogin(PostLoginRecordMathsResultsManagerActivity.this, "1");
                //clearSharePreferencesDetails(PostLoginRecordMathsResultsManagerActivity.this);
                Intent i = new Intent(PostLoginRecordMathsResultsManagerActivity.this,GetMasterTemplateActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                customProgressBar.dismissMe();
                if (fault.getCode().equals("3023")) // Unable to logout: not logged in (session expired, etc.)
                    handleResponse(null);
                else
                    super.handleFault(fault);
            }
        });

    }

    private void logoutDialogAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);
        View mainView = layoutInflater.inflate(R.layout.logout_dialog_design, null, false);
        alertDialogBuilder.setView(mainView);

        TextView msgTv = mainView.findViewById(R.id.msgTv_ids);
        TextView cancelTv = mainView.findViewById(R.id.cancelTv_ids);
        TextView okTv = mainView.findViewById(R.id.okTv_ids);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertDialog.show();
        alertDialog.getWindow().setLayout((int) (width * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);

        msgTv.setText("Are you sure want to Logout?");

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                onLogoutButtonClicked();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    static class StudentsAdapter extends ArrayAdapter<Students> {

        public StudentsAdapter(Context context, List<Students> objects) {
            super(context, R.layout.list_textview, objects);
        }

        @Override //don't override if you don't want the default spinner to be a two line view
        public View getView(int position, View convertView, ViewGroup parent) {
            return initView(position, convertView);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return initView(position, convertView);
        }

        private View initView(int position, View convertView) {
            if (convertView == null)
                convertView = View.inflate(getContext(),
                        R.layout.list_textview,
                        null);
            TextView tvText1 = convertView.findViewById(R.id.listTextView);
            String upnshort = "" + getItem(position).getUpn();
            if (upnshort.length() >= 3)
                upnshort = getItem(position).getUpn().substring(getItem(position).getUpn().length() - 3);
            tvText1.setText(getItem(position).getLastName() + ", " + getItem(position).getFirstName() + " (" + upnshort + ")");
            return convertView;
        }
    }

    static class StudentsLevelAdapter extends ArrayAdapter<String> {

        public StudentsLevelAdapter(Context context, ArrayList<String> objects) {
            super(context, R.layout.list_textview, objects);
        }

        @Override //don't override if you don't want the default spinner to be a two line view
        public View getView(int position, View convertView, ViewGroup parent) {
            return initView(position, convertView);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return initView(position, convertView);
        }

        private View initView(int position, View convertView) {
            if (convertView == null)
                convertView = View.inflate(getContext(), R.layout.list_textview, null);
            TextView tvText1 = convertView.findViewById(R.id.listTextView);
            tvText1.setText(getItem(position));
            return convertView;
        }
    }

    public class PrintResultsTask extends AsyncTask {
        public PrintResultsTask(Context context) {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (result.equals("error")) {
                errorFailedPrinting();
            }

            mProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Object doInBackground(Object... params) {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            File dir = new File(filePath);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, "assessment.png");

            // Get bitmap from linear layout
            LinearLayout printView = findViewById(R.id.resultsViewForPrint);
            int viewWidth = printView.getWidth();
            int viewHeight = printView.getHeight();
            banksBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(banksBitmap);
            printView.draw(c);

			/*
			if (viewWidth > 2800) {
				Log.d(TAG, "Print split image into chunks: " + viewWidth);
				Log.d(TAG, "Image 1");
				// Split the image into chunky prints if its too large
				Bitmap bm1 = Bitmap.createBitmap(banksBitmap,
						0,
						0,
						(banksBitmap.getWidth() / 2),
						banksBitmap.getHeight());
				viewWidth = bm1.getWidth();
				viewHeight = bm1.getHeight();

				Log.d(TAG, "Image 2");
				Bitmap bm2 = Bitmap.createBitmap(banksBitmap,
						(banksBitmap.getWidth() / 2),
						0,
						(banksBitmap.getWidth() / 2),
						banksBitmap.getHeight());
				viewWidth = bm2.getWidth();
				viewHeight = bm2.getHeight();

				banksBitmap = bm1;
				doPrint(file, viewWidth, viewHeight);

				banksBitmap = bm2;
				if (!doPrint(file, viewWidth, viewHeight)) {
					errorFailedPrinting();
				}
			} else {
			*/
            Log.d(MenuActivity.TAG, "Print image in one chunk: " + viewWidth);

            String result = null;

            if (!doPrint(file, viewWidth, viewHeight)) {
                result = "error";
            }
            //}
            return result;
        }

        private void errorFailedPrinting() {
            new AlertDialog.Builder(PostLoginRecordMathsResultsManagerActivity.this)
                    .setTitle("Error")
                    .setMessage("Error printing. Perhaps the chunk is too long?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        private boolean doPrint(File file, int viewWidth, int viewHeight) {
            try {
                FileOutputStream fOut = new FileOutputStream(file);

                // Pad with white on top so it prints the whole frame
                int topPadding = (int) ((float) viewHeight * 0.025f);
                int rightPadding = (int) ((float) viewHeight * 0.20f);
                topPadding = rightPadding = 0;
                RectF targetRect = new RectF(0, topPadding, banksBitmap.getWidth(), banksBitmap.getHeight());
                Bitmap paddedBitmap = Bitmap.createBitmap(banksBitmap.getWidth() + rightPadding, banksBitmap.getHeight(), banksBitmap.getConfig());
                Canvas canvas = new Canvas(paddedBitmap);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(banksBitmap, null, targetRect, null);
//                banksBitmap.recycle();

                // Rotate
//                Matrix matrix = new Matrix();
//                matrix.postRotate(90);
//                Bitmap rotatedBitmap =  Bitmap.createBitmap(paddedBitmap, 0, 0, paddedBitmap.getWidth(), paddedBitmap.getHeight(), matrix, true);
//                paddedBitmap.recycle();
                Bitmap rotatedBitmap = banksBitmap;

                // Convert to B&W
                int newWidth = rotatedBitmap.getWidth();
                int newHeight = rotatedBitmap.getHeight();
                Bitmap bmpMonochrome = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas1 = new Canvas(bmpMonochrome);
                ColorMatrix ma = new ColorMatrix();
                ma.setSaturation(0);
                Paint paint1 = new Paint();
                //paint.setColorFilter(new ColorMatrixColorFilter(ma));
                canvas1.drawBitmap(rotatedBitmap, 0, 0, paint1);
                rotatedBitmap.recycle();
                int width2 = bmpMonochrome.getWidth();
                int height2 = bmpMonochrome.getHeight();
                int[] pixels = new int[width2 * height2];
                bmpMonochrome.getPixels(pixels, 0, width2, 0, 0, width2, height2);
                // Iterate over height
                for (int y = 0; y < height2; y++) {
                    int offset = y * height2;
                    // Iterate over width
                    for (int x = 0; x < width2; x++) {
                        int pixel = bmpMonochrome.getPixel(x, y);
                        int lowestBit = pixel & 0xff;
                        if (lowestBit < 128)
                            bmpMonochrome.setPixel(x, y, Color.WHITE);
                        else
                            bmpMonochrome.setPixel(x, y, Color.BLACK);
                    }
                }

                // Scale to size appropriate for the bluetooth printer
                float aspectRatio = (float) bmpMonochrome.getHeight() / (float) bmpMonochrome.getWidth();
                int width = 384;
                int height = Math.round(width * aspectRatio);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmpMonochrome, width, height, false);
                bmpMonochrome.recycle();

                // Write to file
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();

                PrintPic pg = new PrintPic();
                pg.initCanvas(width);
                pg.initPaint();
                pg.drawImage(0, 0, Environment.getExternalStorageDirectory().getAbsolutePath() + "/assessment.png");
                byte[] sendData = pg.printDraw();
                if (ConnectBluetoothPrinterActivity.mService != null) {
                    if (!ConnectBluetoothPrinterActivity.mService.isAvailable()) {
                        PostLoginRecordMathsResultsManagerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PostLoginRecordMathsResultsManagerActivity.this, "Bluetooth is not ready. Restart app.", Toast.LENGTH_LONG).show();
                            }
                        });
                        return false;
                    } else {
                        // Test text
//						byte[] cmd = new byte[3];
//						cmd[0] = 0x1b;
//						cmd[1] = 0x21;
//							cmd[2] |= 0x10;
//						ConnectBluetoothPrinterActivity.mService.write(cmd);
//						ConnectBluetoothPrinterActivity.mService.sendMessage("Congratulations!\n", "GBK");
//							cmd[2] &= 0xEF;
//						ConnectBluetoothPrinterActivity.mService.write(cmd);
//						String	msg = "  You have sucessfully created communications between your device and our bluetooth printer.\n\n"
//									+"  Shenzhen Zijiang Electronics Co..Ltd is a high-tech enterprise which specializes" +
//									" in R&D,manufacturing,marketing of thermal printers and barcode scanners.\n\n"
//									+"  Please go to our website and see details about our company :\n" +"     http://www.zjiang.com\n\n";
//
//						ConnectBluetoothPrinterActivity.mService.sendMessage(msg,"GBK");
                        ConnectBluetoothPrinterActivity.mService.write(sendData);
                    }
                } else {
                    Toast.makeText(PostLoginRecordMathsResultsManagerActivity.this, "Bluetooth is not ready. Restart app.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }
}
