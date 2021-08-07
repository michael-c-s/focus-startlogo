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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.assessbyphone.zambia.CallbackUtils.CustomProgressBar;
import com.assessbyphone.zambia.CallbackUtils.DefaultCallback;
import com.assessbyphone.zambia.Models.MasterCSVModel;
import com.assessbyphone.zambia.Models.PhonicsResults;
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

public class PostLoginRecordResultsManagerActivity extends Activity {
    static String NEED_BEFORE_SCORE = "Sorry, the previous score must be higher than zero";
    private final Bitmap banksBitmap2 = null;
    private final MasterCSVModel.AgeGroup ageGroup = MasterCSVModel.AgeGroup.NotSpecified;
    public SharedPreferences sPrefs;
    private Bitmap banksBitmap = null;
    private Students[] students;
    private Students currentStudent;
    private boolean ignore = false;
    private boolean isShowToast = true;
    private RadioGroup termRG;
    private RadioGroup picturesResultRG;
    private RadioGroup letterSoundsResultRG;
    private RadioGroup cvcWordResultRG;
    private RadioGroup nonCVCWordsResultRG;
    private RadioGroup _4letterblendResultRG;
    private RadioGroup _4LetterDigraphResultRG;
    private RadioGroup _5letterblendResultRG;
    private RadioGroup CommonPlusVowelDigraphResultRG;
    private RadioGroup highFrequencyWordsResultRG;
    private RadioGroup oralCompResultRG;
    private Spinner wordsReadSpinner;
    private RadioGroup readingCompResultRG;
    private TextView phonicsTotals;
    private TextView comphrensionTotals;
    private TextView overallTotal;
    private FrameLayout comprehensionLevelLay;
    private TextView comprehensionLevelTv;
    private Button saveResultsButton;
    private Button printResultsButton;
    private LinearLayout logoutLay;
    private ProgressBar mProgressBar;
    private Spinner studentsSpinner;
    private BackendlessUser backendlessUser;
    private ListPopupWindow comprehensionPopup;
    private LayoutInflater layoutInflater;
    private int width;
    private ImageView resultBack;
    private CustomProgressBar customProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_results_manager);

        Log.e("AgeProblemEnglish", "AgeProblemEnglish");

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
            new AlertDialog.Builder(PostLoginRecordResultsManagerActivity.this)
                    .setTitle("Error")
                    .setCancelable(false)
                    .setMessage("This teacher has no students assigned to them. Please add some.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            dialog.dismiss();
                            PostLoginRecordResultsManagerActivity.this.finish();
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

        comprehensionPopupWindow();
    }

    private void initUI() {

        saveResultsButton = findViewById(R.id.saveResultsButton);
        saveResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentStudent != null) {
                    customProgressBar.showMe(PostLoginRecordResultsManagerActivity.this, "Please Wait...");
                    onSaveResultsButtonClicked();
                } else
                    Toast.makeText(PostLoginRecordResultsManagerActivity.this, "No user found!", Toast.LENGTH_SHORT).show();
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
        comprehensionLevelLay = findViewById(R.id.comprehensionLevelLay_ids);
        comprehensionLevelTv = findViewById(R.id.comprehensionLevelTv_ids);

        wordsReadSpinner = findViewById(R.id.wordsReadSpinner);
        ArrayAdapter<String> wordsReadSpinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.list_textview, getResources().getStringArray(R.array.wordsReadScores));
        wordsReadSpinner.setAdapter(wordsReadSpinnerArrayAdapter);

        logoutLay = findViewById(R.id.logoutLay_ids);

        logoutLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutDialogAlert();
            }
        });

        phonicsTotals = findViewById(R.id.phonicsTotalScore);
        comphrensionTotals = findViewById(R.id.comprehensionTotalScore);
        overallTotal = findViewById(R.id.overallTotalScore);

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
                showStudentDetails(i, 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        termRG = findViewById(R.id.readingTerm);
        termRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                ignore = true;
                showStudentDetails(studentsSpinner.getSelectedItemPosition(), Integer.parseInt(((RadioButton) findViewById(termRG.getCheckedRadioButtonId())).getText().toString()));
                ignore = false;
            }
        });

        //set Choose comprehension leve
        comprehensionLevelLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comprehensionPopup.show();
            }
        });

        picturesResultRG = findViewById(R.id.pictureResult);
        picturesResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        letterSoundsResultRG = findViewById(R.id.letterSoundsResult);

        letterSoundsResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAndGetTotals();
            }
        });
        cvcWordResultRG = findViewById(R.id.cvcWordsResult);
        cvcWordResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (ignore) return;
                String letterSoundsResult1 = ((RadioButton) findViewById(letterSoundsResultRG.getCheckedRadioButtonId())).getText().toString();
                if (i != R.id.cvcWordsResultN && findViewById(letterSoundsResultRG.getCheckedRadioButtonId()) != null && (letterSoundsResult1.equalsIgnoreCase("N") || letterSoundsResult1.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;
                    }
                    cvcWordResultRG.check(R.id.cvcWordsResultN);
                }
                nonCVCWordsResultRG.check(R.id.nonCVCWordsResultN);
                highFrequencyWordsResultRG.check(R.id.highFrequencyWordsResultN);
                _4letterblendResultRG.check(R.id._4letterblendResultN);
                _4LetterDigraphResultRG.check(R.id._4LetterDigraphResultN);
                _5letterblendResultRG.check(R.id._5letterblendResultN);
                CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResultN);
                comprehensionLevelTv.setText("N");
                oralCompResultRG.check(R.id.oralCompResultN);
                wordsReadSpinner.setSelection(0);
                readingCompResultRG.check(R.id.readingCompResultN);
                refreshAndGetTotals();
            }
        });
        nonCVCWordsResultRG = findViewById(R.id.nonCVCWordsResult);
        nonCVCWordsResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (ignore) return;
                String cvcWordResult1 = ((RadioButton) findViewById(cvcWordResultRG.getCheckedRadioButtonId())).getText().toString();
                if (i != R.id.nonCVCWordsResultN && findViewById(cvcWordResultRG.getCheckedRadioButtonId()) != null && (cvcWordResult1.equalsIgnoreCase("N") || cvcWordResult1.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;
                    }
                    nonCVCWordsResultRG.check(R.id.nonCVCWordsResultN);
                }

                highFrequencyWordsResultRG.check(R.id.highFrequencyWordsResultN);
                _4letterblendResultRG.check(R.id._4letterblendResultN);
                _4LetterDigraphResultRG.check(R.id._4LetterDigraphResultN);
                _5letterblendResultRG.check(R.id._5letterblendResultN);
                CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResultN);
                comprehensionLevelTv.setText("N");
                oralCompResultRG.check(R.id.oralCompResultN);

                wordsReadSpinner.setSelection(0);
                readingCompResultRG.check(R.id.readingCompResultN);
                refreshAndGetTotals();
            }
        });
        highFrequencyWordsResultRG = findViewById(R.id.highFrequencyWordsResult);
        highFrequencyWordsResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (ignore) return;
                String nonCVCWordsResult1 = ((RadioButton) findViewById(nonCVCWordsResultRG.getCheckedRadioButtonId())).getText().toString();
                if (i != R.id.highFrequencyWordsResultN && findViewById(nonCVCWordsResultRG.getCheckedRadioButtonId()) != null && (nonCVCWordsResult1.equalsIgnoreCase("N") || nonCVCWordsResult1.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;

                    }
                    highFrequencyWordsResultRG.check(R.id.highFrequencyWordsResultN);
                }
                _4letterblendResultRG.check(R.id._4letterblendResultN);
                _4LetterDigraphResultRG.check(R.id._4LetterDigraphResultN);
                _5letterblendResultRG.check(R.id._5letterblendResultN);
                CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResultN);
                comprehensionLevelTv.setText("N");
                oralCompResultRG.check(R.id.oralCompResultN);
                wordsReadSpinner.setSelection(0);
                readingCompResultRG.check(R.id.readingCompResultN);
                refreshAndGetTotals();
            }
        });
        _4letterblendResultRG = findViewById(R.id._4letterblendResult);
        _4letterblendResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (ignore) return;
                String highFrequencyWordsResult1 = ((RadioButton) findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId())).getText().toString();
                if (i != R.id._4letterblendResultN && findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId()) != null && (highFrequencyWordsResult1.equalsIgnoreCase("N") || highFrequencyWordsResult1.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;
                    }
                    _4letterblendResultRG.check(R.id._4letterblendResultN);
                }
                _4LetterDigraphResultRG.check(R.id._4LetterDigraphResultN);
                _5letterblendResultRG.check(R.id._5letterblendResultN);
                CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResultN);
                comprehensionLevelTv.setText("N");
                oralCompResultRG.check(R.id.oralCompResultN);
                wordsReadSpinner.setSelection(0);
                readingCompResultRG.check(R.id.readingCompResultN);
                refreshAndGetTotals();
            }
        });
        _4LetterDigraphResultRG = findViewById(R.id._4LetterDigraphResult);
        _4LetterDigraphResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (ignore) return;
                String highFrequencyWordsResult1 = ((RadioButton) findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId())).getText().toString();
                if (i != R.id._4LetterDigraphResultN && findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId()) != null && (highFrequencyWordsResult1.equalsIgnoreCase("N") || highFrequencyWordsResult1.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;
                    }
                    _4LetterDigraphResultRG.check(R.id._4LetterDigraphResultN);
                }
                _5letterblendResultRG.check(R.id._5letterblendResultN);
                CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResultN);
                comprehensionLevelTv.setText("N");
                oralCompResultRG.check(R.id.oralCompResultN);
                wordsReadSpinner.setSelection(0);
                readingCompResultRG.check(R.id.readingCompResultN);
                refreshAndGetTotals();
            }
        });
        _5letterblendResultRG = findViewById(R.id._5letterblendResult);
        _5letterblendResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (ignore) return;
                String highFrequencyWordsResult1 = ((RadioButton) findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId())).getText().toString();
                if (i != R.id._5letterblendResultN && findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId()) != null && (highFrequencyWordsResult1.equalsIgnoreCase("N") || highFrequencyWordsResult1.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;
                    }
                    _5letterblendResultRG.check(R.id._5letterblendResultN);
                }
                CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResultN);
                comprehensionLevelTv.setText("N");
                oralCompResultRG.check(R.id.oralCompResultN);
                wordsReadSpinner.setSelection(0);
                readingCompResultRG.check(R.id.readingCompResultN);
                refreshAndGetTotals();
            }
        });
        CommonPlusVowelDigraphResultRG = findViewById(R.id.CommonPlusVowelDigraphResult);
        CommonPlusVowelDigraphResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (ignore) return;
                String highFrequencyWordsResult1 = ((RadioButton) findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId())).getText().toString();
                if (i != R.id.CommonPlusVowelDigraphResultN && findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId()) != null && (highFrequencyWordsResult1.equalsIgnoreCase("N") || highFrequencyWordsResult1.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;
                    }
                    CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResultN);
                }
                comprehensionLevelTv.setText("N");
                oralCompResultRG.check(R.id.oralCompResultN);
                wordsReadSpinner.setSelection(0);
                readingCompResultRG.check(R.id.readingCompResultN);
                refreshAndGetTotals();
            }
        });
        oralCompResultRG = findViewById(R.id.oralCompResult);
        oralCompResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (ignore) return;
                String _4letterblendResult1 = ((RadioButton) findViewById(_4letterblendResultRG.getCheckedRadioButtonId())).getText().toString();
                String _4LetterDigraphResult = ((RadioButton) findViewById(_4LetterDigraphResultRG.getCheckedRadioButtonId())).getText().toString();
                String _5letterblendResult = ((RadioButton) findViewById(_5letterblendResultRG.getCheckedRadioButtonId())).getText().toString();
                String CommonPlusVowelDigraphResult = ((RadioButton) findViewById(CommonPlusVowelDigraphResultRG.getCheckedRadioButtonId())).getText().toString();
                String comprehensionLevel = comprehensionLevelTv.getText().toString();

                if (i != R.id.oralCompResultN
                        && findViewById(_4letterblendResultRG.getCheckedRadioButtonId()) != null
                        && findViewById(_4LetterDigraphResultRG.getCheckedRadioButtonId()) != null
                        && findViewById(_5letterblendResultRG.getCheckedRadioButtonId()) != null
                        && findViewById(CommonPlusVowelDigraphResultRG.getCheckedRadioButtonId()) != null
                        && (
                        (_4letterblendResult1.equalsIgnoreCase("N") || _4letterblendResult1.equalsIgnoreCase("0"))
                                && (_4LetterDigraphResult.equalsIgnoreCase("N") || _4LetterDigraphResult.equalsIgnoreCase("0"))
                                && (_5letterblendResult.equalsIgnoreCase("N") || _5letterblendResult.equalsIgnoreCase("0"))
                                && (CommonPlusVowelDigraphResult.equalsIgnoreCase("N") || CommonPlusVowelDigraphResult.equalsIgnoreCase("0")))
                        || (comprehensionLevel.equalsIgnoreCase("N") || comprehensionLevel.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;

                    }
                    oralCompResultRG.check(R.id.oralCompResultN);
                }
                wordsReadSpinner.setSelection(0);
                readingCompResultRG.check(R.id.readingCompResultN);
                refreshAndGetTotals();
            }
        });

        wordsReadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (ignore) return;
                String oralCompResult = ((RadioButton) findViewById(oralCompResultRG.getCheckedRadioButtonId())).getText().toString();
                if (i != 0 && findViewById(oralCompResultRG.getCheckedRadioButtonId()) != null && (oralCompResult.equalsIgnoreCase("N") || oralCompResult.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;
                    }
                    wordsReadSpinner.setSelection(0);
                }
                //readingCompResultRG.check(R.id.readingCompResultN);
                refreshAndGetTotals();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        readingCompResultRG = findViewById(R.id.readingCompResult);
        readingCompResultRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (ignore) return;
                String wordComp = wordsReadSpinner.getSelectedItem().toString();
                int prevTotal = wordsReadCompResultTotal();
                if (i != R.id.readingCompResultN && prevTotal == 11 || (wordComp.equalsIgnoreCase("N") || wordComp.equalsIgnoreCase("0"))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;
                    }
                    readingCompResultRG.check(R.id.readingCompResultN);
                }
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

        resultBack = (ImageView) findViewById(R.id.resultBack_ids);
        resultBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @SuppressLint("NewApi")
    private void comprehensionPopupWindow() {
        List<String> comprehensionALL = new ArrayList<>();
        comprehensionALL.add("Reading Age 5 to 6");
        comprehensionALL.add("Reading Age 6 to 7");
        comprehensionALL.add("Reading Age 7 to 8");
        comprehensionALL.add("Reading Age 8 to 9");
        comprehensionALL.add("Reading Age 9 to 10");
        comprehensionALL.add("Reading Age 10 to 11");

        comprehensionPopup = new ListPopupWindow(PostLoginRecordResultsManagerActivity.this);
        comprehensionPopup.setWidth(656);
        comprehensionPopup.setBackgroundDrawable(null);
        comprehensionPopup.setDropDownGravity(Gravity.CENTER_HORIZONTAL);
        ArrayAdapter adapter = new ArrayAdapter<>(PostLoginRecordResultsManagerActivity.this, R.layout.world_problem_list_design, R.id.statusTv_ids, comprehensionALL);
        comprehensionPopup.setAnchorView(comprehensionLevelLay);
        comprehensionPopup.setAdapter(adapter);
        comprehensionPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //setWlLevelContent(status.get(position));
                comprehensionPopup.dismiss();
                if (ignore) return;
                String _4letterblendResult1 = ((RadioButton) findViewById(_4letterblendResultRG.getCheckedRadioButtonId())).getText().toString();
                String _4LetterDigraphResult = ((RadioButton) findViewById(_4LetterDigraphResultRG.getCheckedRadioButtonId())).getText().toString();
                String _5letterblendResult = ((RadioButton) findViewById(_5letterblendResultRG.getCheckedRadioButtonId())).getText().toString();
                String CommonPlusVowelDigraphResult = ((RadioButton) findViewById(CommonPlusVowelDigraphResultRG.getCheckedRadioButtonId())).getText().toString();

                if (findViewById(_4letterblendResultRG.getCheckedRadioButtonId()) != null
                        && findViewById(_4LetterDigraphResultRG.getCheckedRadioButtonId()) != null
                        && findViewById(_5letterblendResultRG.getCheckedRadioButtonId()) != null
                        && findViewById(CommonPlusVowelDigraphResultRG.getCheckedRadioButtonId()) != null
                        && (
                        (_4letterblendResult1.equalsIgnoreCase("N") || _4letterblendResult1.equalsIgnoreCase("0"))
                                && (_4LetterDigraphResult.equalsIgnoreCase("N") || _4LetterDigraphResult.equalsIgnoreCase("0"))
                                && (_5letterblendResult.equalsIgnoreCase("N") || _5letterblendResult.equalsIgnoreCase("0"))
                                && (CommonPlusVowelDigraphResult.equalsIgnoreCase("N") || CommonPlusVowelDigraphResult.equalsIgnoreCase("0")))) {
                    if (isShowToast) {
                        showMyToast();
                        isShowToast = false;
                    }
                } else
                    comprehensionLevelTv.setText(comprehensionALL.get(position));
                refreshAndGetTotals();
            }
        });
    }

    void showStudentDetails(int pos, int term) {
        Students student = (Students) studentsSpinner.getItemAtPosition(pos);
        currentStudent = student;

        if (student != null && student.getResults() != null) {
            List<PhonicsResults> results = student.getResults();

            PhonicsResults phonicsResultsToShow = null;
            outter:
            if (results != null && results.size() > 0) {
                // Find the reading term's results to show, with "int term" argument taking priority
                for (PhonicsResults result : results) {
                    if (result != null && result.getTerm() == term) {
                        phonicsResultsToShow = result;
                        break outter;
                    }
                }
                // else, blank slate
                phonicsResultsToShow = new PhonicsResults();
                phonicsResultsToShow.setTerm(term);
            } else {
                Log.e(MenuActivity.TAG, "No results for student to show");
                // else, blank slate
                phonicsResultsToShow = new PhonicsResults();
                phonicsResultsToShow.setTerm(term);
            }

            switch (phonicsResultsToShow.getTerm()) {
                case 1:
                    termRG.check(R.id.readingTerm1);
                    break;
                case 2:
                    termRG.check(R.id.readingTerm2);
                    break;
                case 3:
                    termRG.check(R.id.readingTerm3);
                    break;
            }

            switch (phonicsResultsToShow.getPhonics_pictures() == null ? "Null" : phonicsResultsToShow.getPhonics_pictures()) {
                case "0":
                    picturesResultRG.check(R.id.pictureResult0);
                    break;
                case "1":
                    picturesResultRG.check(R.id.pictureResult1);
                    break;
                case "2":
                    picturesResultRG.check(R.id.pictureResult2);
                    break;
                case "3":
                    picturesResultRG.check(R.id.pictureResult3);
                    break;
                default:
                    picturesResultRG.check(R.id.pictureResultN);
                    break;
            }

            switch (phonicsResultsToShow.getPhonics_letter_sounds() == null ? "Null" : phonicsResultsToShow.getPhonics_letter_sounds()) {
                case "0":
                    letterSoundsResultRG.check(R.id.letterSoundsResult0);
                    break;
                case "1":
                    letterSoundsResultRG.check(R.id.letterSoundsResult1);
                    break;
                case "2":
                    letterSoundsResultRG.check(R.id.letterSoundsResult2);
                    break;
                case "3":
                    letterSoundsResultRG.check(R.id.letterSoundsResult3);
                    break;
                default:
                    letterSoundsResultRG.check(R.id.letterSoundsResultN);
                    break;
            }

            switch (phonicsResultsToShow.getPhonics_cvc_words() == null ? "Null" : phonicsResultsToShow.getPhonics_cvc_words()) {
                case "0":
                    cvcWordResultRG.check(R.id.cvcWordsResult0);
                    break;
                case "1":
                    cvcWordResultRG.check(R.id.cvcWordsResult1);
                    break;
                case "2":
                    cvcWordResultRG.check(R.id.cvcWordsResult2);
                    break;
                case "3":
                    cvcWordResultRG.check(R.id.cvcWordsResult3);
                    break;
                default:
                    cvcWordResultRG.check(R.id.cvcWordsResultN);
                    break;
            }

            switch (phonicsResultsToShow.getPhonics_cvc_nonwords() == null ? "Null" : phonicsResultsToShow.getPhonics_cvc_nonwords()) {
                case "0":
                    nonCVCWordsResultRG.check(R.id.nonCVCWordsResult0);
                    break;
                case "1":
                    nonCVCWordsResultRG.check(R.id.nonCVCWordsResult1);
                    break;
                case "2":
                    nonCVCWordsResultRG.check(R.id.nonCVCWordsResult2);
                    break;
                case "3":
                    nonCVCWordsResultRG.check(R.id.nonCVCWordsResult3);
                    break;
                default:
                    nonCVCWordsResultRG.check(R.id.nonCVCWordsResultN);
                    break;
            }

            switch (phonicsResultsToShow.getPhonics_four_letters_plus_blend() == null ? "Null" : phonicsResultsToShow.getPhonics_four_letters_plus_blend()) {
                case "0":
                    _4letterblendResultRG.check(R.id._4letterblendResult0);
                    break;
                case "1":
                    _4letterblendResultRG.check(R.id._4letterblendResult1);
                    break;
                case "2":
                    _4letterblendResultRG.check(R.id._4letterblendResult2);
                    break;
                case "3":
                    _4letterblendResultRG.check(R.id._4letterblendResult3);
                    break;
                default:
                    _4letterblendResultRG.check(R.id._4letterblendResultN);
                    break;
            }

            switch (phonicsResultsToShow.getPhonics_four_letters_plus_digraph() == null ? "Null" : phonicsResultsToShow.getPhonics_four_letters_plus_digraph()) {
                case "0":
                    _4LetterDigraphResultRG.check(R.id._4LetterDigraphResult0);
                    break;
                case "1":
                    _4LetterDigraphResultRG.check(R.id._4LetterDigraphResult1);
                    break;
                case "2":
                    _4LetterDigraphResultRG.check(R.id._4LetterDigraphResult2);
                    break;
                case "3":
                    _4LetterDigraphResultRG.check(R.id._4LetterDigraphResult3);
                    break;
                default:
                    _4LetterDigraphResultRG.check(R.id._4LetterDigraphResultN);
                    break;
            }

            switch (phonicsResultsToShow.getPhonics_five_letters_plus_blend() == null ? "Null" : phonicsResultsToShow.getPhonics_five_letters_plus_blend()) {
                case "0":
                    _5letterblendResultRG.check(R.id._5letterblendResult0);
                    break;
                case "1":
                    _5letterblendResultRG.check(R.id._5letterblendResult1);
                    break;
                case "2":
                    _5letterblendResultRG.check(R.id._5letterblendResult2);
                    break;
                case "3":
                    _5letterblendResultRG.check(R.id._5letterblendResult3);
                    break;
                default:
                    _5letterblendResultRG.check(R.id._5letterblendResultN);
                    break;
            }

            switch (phonicsResultsToShow.getPhonics_common_plus_vowel_digraph() == null ? "Null" : phonicsResultsToShow.getPhonics_common_plus_vowel_digraph()) {
                case "0":
                    CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResult0);
                    break;
                case "1":
                    CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResult1);
                    break;
                case "2":
                    CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResult2);
                    break;
                case "3":
                    CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResult3);
                    break;
                default:
                    CommonPlusVowelDigraphResultRG.check(R.id.CommonPlusVowelDigraphResultN);
                    break;
            }

            switch (phonicsResultsToShow.getPhonics_high_frequency_words() == null ? "Null" : phonicsResultsToShow.getPhonics_high_frequency_words()) {
                case "0":
                    highFrequencyWordsResultRG.check(R.id.highFrequencyWordsResult0);
                    break;
                case "1":
                    highFrequencyWordsResultRG.check(R.id.highFrequencyWordsResult1);
                    break;
                case "2":
                    highFrequencyWordsResultRG.check(R.id.highFrequencyWordsResult2);
                    break;
                case "3":
                    highFrequencyWordsResultRG.check(R.id.highFrequencyWordsResult3);
                    break;
                default:
                    highFrequencyWordsResultRG.check(R.id.highFrequencyWordsResultN);
                    break;
            }

            switch (phonicsResultsToShow.getAge_group() == null ? "Null" : phonicsResultsToShow.getAge_group()) {
                case "1":
                    comprehensionLevelTv.setText("Reading Age 5 to 6");
                    break;
                case "2":
                    comprehensionLevelTv.setText("Reading Age 6 to 7");
                    break;
                case "3":
                    comprehensionLevelTv.setText("Reading Age 7 to 8");
                    break;
                case "4":
                    comprehensionLevelTv.setText("Reading Age 8 to 9");
                    break;
                case "5":
                    comprehensionLevelTv.setText("Reading Age 9 to 10");
                    break;
                case "6":
                    comprehensionLevelTv.setText("Reading Age 10 to 11");
                    break;
                default:
                    comprehensionLevelTv.setText("N");
                    break;
            }

            switch (phonicsResultsToShow.getComprehension_oral() == null ? "Null" : phonicsResultsToShow.getComprehension_oral()) {
                case "0":
                    oralCompResultRG.check(R.id.oralCompResult0);
                    break;
                case "1":
                    oralCompResultRG.check(R.id.oralCompResult1);
                    break;
                case "2":
                    oralCompResultRG.check(R.id.oralCompResult2);
                    break;
                case "3":
                    oralCompResultRG.check(R.id.oralCompResult3);
                    break;
                case "4":
                    oralCompResultRG.check(R.id.oralCompResult4);
                    break;
                case "5":
                    oralCompResultRG.check(R.id.oralCompResult5);
                    break;
                default:
                    oralCompResultRG.check(R.id.oralCompResultN);
                    break;
            }

            switch (phonicsResultsToShow.getComprehension_words_read() == null ? "Null" : phonicsResultsToShow.getComprehension_words_read()) {
                case "0":
                    wordsReadSpinner.setSelection(1);
                    break;
                case "1":
                    wordsReadSpinner.setSelection(2);
                    break;
                case "2":
                    wordsReadSpinner.setSelection(3);
                    break;
                case "3":
                    wordsReadSpinner.setSelection(4);
                    break;
                case "4":
                    wordsReadSpinner.setSelection(5);
                    break;
                case "5":
                    wordsReadSpinner.setSelection(6);
                    break;
                case "6":
                    wordsReadSpinner.setSelection(7);
                    break;
                case "7":
                    wordsReadSpinner.setSelection(8);
                    break;
                case "8":
                    wordsReadSpinner.setSelection(9);
                    break;
                default:
                    wordsReadSpinner.setSelection(0);
                    break;
            }

            //Log.e("Comprehension", phonicsResultsToShow.getComprehension_reading());
            switch (phonicsResultsToShow.getComprehension_reading() == null ? "Null" : phonicsResultsToShow.getComprehension_reading()) {
                case "0":
                    readingCompResultRG.check(R.id.readingCompResult0);
                    break;
                case "1":
                    readingCompResultRG.check(R.id.readingCompResult1);
                    break;
                case "2":
                    readingCompResultRG.check(R.id.readingCompResult2);
                    break;
                case "3":
                    readingCompResultRG.check(R.id.readingCompResult3);
                    break;
                case "4":
                    readingCompResultRG.check(R.id.readingCompResult4);
                    break;
                case "5":
                    readingCompResultRG.check(R.id.readingCompResult5);
                    break;
                default:
                    readingCompResultRG.check(R.id.readingCompResultN);
                    break;
            }

            refreshAndGetTotals();
        } else
            Toast.makeText(this, "No student found!", Toast.LENGTH_SHORT).show();
    }

    //  set server user live details
    private void refreshAndGetTotals() {
        String picturesResult1 = ((RadioButton) findViewById(picturesResultRG.getCheckedRadioButtonId())).getText().toString();
        int picturesResultTotal = (picturesResult1.equalsIgnoreCase("N") ? 0 : Integer.parseInt(picturesResult1));

        String letterSoundsResult = ((RadioButton) findViewById(letterSoundsResultRG.getCheckedRadioButtonId())).getText().toString();
        int letterSoundsResultTotal = (letterSoundsResult.equalsIgnoreCase("N") ? 0 : Integer.parseInt(letterSoundsResult));

        String cvcWordsResult = ((RadioButton) findViewById(cvcWordResultRG.getCheckedRadioButtonId())).getText().toString();
        int cvcWordsResultTotal = (cvcWordsResult.equalsIgnoreCase("N") ? 0 : Integer.parseInt(cvcWordsResult));

        String nonCVCWordsResult = ((RadioButton) findViewById(nonCVCWordsResultRG.getCheckedRadioButtonId())).getText().toString();
        int nonCVCWordsResultTotal = (nonCVCWordsResult.equalsIgnoreCase("N") ? 0 : Integer.parseInt(nonCVCWordsResult));

        String _4letterblendResult = ((RadioButton) findViewById(_4letterblendResultRG.getCheckedRadioButtonId())).getText().toString();
        int _4letterblendResultTotal = (_4letterblendResult.equalsIgnoreCase("N") ? 0 : Integer.parseInt(_4letterblendResult));

        String _4LetterDigraphResult = ((RadioButton) findViewById(_4LetterDigraphResultRG.getCheckedRadioButtonId())).getText().toString();
        int _4LetterDigraphResultTotal = (_4LetterDigraphResult.equalsIgnoreCase("N") ? 0 : Integer.parseInt(_4LetterDigraphResult));

        String _5letterblendResul = ((RadioButton) findViewById(_5letterblendResultRG.getCheckedRadioButtonId())).getText().toString();
        int _5letterblendResultTotal = (_5letterblendResul.equalsIgnoreCase("N") ? 0 : Integer.parseInt(_5letterblendResul));

        String CommonPlusVowelDigraphResult = ((RadioButton) findViewById(CommonPlusVowelDigraphResultRG.getCheckedRadioButtonId())).getText().toString();
        int CommonPlusVowelDigraphResultTotal = (CommonPlusVowelDigraphResult.equalsIgnoreCase("N") ? 0 : Integer.parseInt(CommonPlusVowelDigraphResult));

        String highFrequencyWordsResult = ((RadioButton) findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId())).getText().toString();
        int highFrequencyWordsResultTotal = (highFrequencyWordsResult.equalsIgnoreCase("N") ? 0 : Integer.parseInt(highFrequencyWordsResult));

        String oralCompResult = ((RadioButton) findViewById(oralCompResultRG.getCheckedRadioButtonId())).getText().toString();
        int oralCompResultTotal = (oralCompResult.equalsIgnoreCase("N") ? 0 : Integer.parseInt(oralCompResult));

        int wordsReadCompResultTotal = wordsReadCompResultTotal();

        String readingCompResult = ((RadioButton) findViewById(readingCompResultRG.getCheckedRadioButtonId())).getText().toString();
        int readingCompResultTotal = (readingCompResult.equalsIgnoreCase("N") ? 0 : Integer.parseInt(readingCompResult));


        int phonicsTotalsI = picturesResultTotal + letterSoundsResultTotal + cvcWordsResultTotal + nonCVCWordsResultTotal
                + _4letterblendResultTotal + _4LetterDigraphResultTotal + _5letterblendResultTotal + CommonPlusVowelDigraphResultTotal
                + highFrequencyWordsResultTotal;
        int compTotalsI = oralCompResultTotal + wordsReadCompResultTotal + readingCompResultTotal;
        int total = phonicsTotalsI + compTotalsI;

        phonicsTotals.setText(getString(R.string.phonicsTotalScore) + " " + phonicsTotalsI);

        comphrensionTotals.setText(getString(R.string.comphrensionTotalScore) + " " + compTotalsI);

        overallTotal.setText(getString(R.string.overallTotalScore) + " " + total);

    }

    private int wordsReadCompResultTotal() {
        String worReadValue = wordsReadSpinner.getSelectedItem().toString();
        int wordsReadCompResultTotal = 0;
        if (worReadValue.equalsIgnoreCase("0")) {
            wordsReadCompResultTotal = 0;
        } else if (worReadValue.equalsIgnoreCase("1–10")) {
            wordsReadCompResultTotal = 1;
        } else if (worReadValue.equalsIgnoreCase("11–20")) {
            wordsReadCompResultTotal = 2;
        } else if (worReadValue.equalsIgnoreCase("21–30")) {
            wordsReadCompResultTotal = 3;
        } else if (worReadValue.equalsIgnoreCase("31–40")) {
            wordsReadCompResultTotal = 4;
        } else if (worReadValue.equalsIgnoreCase("41–50")) {
            wordsReadCompResultTotal = 5;
        } else if (worReadValue.equalsIgnoreCase("51–60")) {
            wordsReadCompResultTotal = 6;
        } else if (worReadValue.equalsIgnoreCase("61–70")) {
            wordsReadCompResultTotal = 7;
        } else if (worReadValue.equalsIgnoreCase("71–80+")) {
            wordsReadCompResultTotal = 8;
        } else if (worReadValue.equalsIgnoreCase("Null"))
            wordsReadCompResultTotal = 0;

        return wordsReadCompResultTotal;
    }

    private void saveResultsToUser() {
        if (currentStudent == null)
            return;

        int currentTerm = Integer.parseInt(((RadioButton) findViewById((termRG.getCheckedRadioButtonId()))).getText().toString());

        List<PhonicsResults> results = currentStudent.getResults();
        boolean newResults = true;
        PhonicsResults phonicsResultsToSave = new PhonicsResults();
        outter:
        if (results != null && results.size() > 0) {
            // Find the reading term's results to show, with "int term" argument taking priority
            for (PhonicsResults result : results) {
                if (result != null && result.getTerm() == currentTerm) {
                    newResults = false;
                    phonicsResultsToSave = result;
                    break outter;
                }
            }
            newResults = true;
            phonicsResultsToSave = new PhonicsResults();
            phonicsResultsToSave.setTerm(currentTerm);
        } else {
            Log.e(MenuActivity.TAG, "No existing results for student to modify for term " + currentTerm);
            phonicsResultsToSave.setTerm(currentTerm);
            newResults = true;
        }

        switch (phonicsResultsToSave.getTerm()) {
            case 1:
                termRG.check(R.id.readingTerm1);
                break;
            case 2:
                termRG.check(R.id.readingTerm2);
                break;
            case 3:
                termRG.check(R.id.readingTerm3);
                break;
        }

        // set utn no
        // phonicsResultsToSave.setUPN(myUTN);
        phonicsResultsToSave.setUPN(currentStudent.getUpn());

        phonicsResultsToSave.setTerm(currentTerm);
        phonicsResultsToSave.setLanguage(GetMasterTemplateActivity.sTemplate.language);

        String picturesResult = ((RadioButton) findViewById(picturesResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setPhonics_pictures(picturesResult.equalsIgnoreCase("N") ? "Null" : picturesResult);

        String letterSoundsResult = ((RadioButton) findViewById(letterSoundsResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setPhonics_letter_sounds(letterSoundsResult.equalsIgnoreCase("N") ? "Null" : letterSoundsResult);

        String cvcWordResult = ((RadioButton) findViewById(cvcWordResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setPhonics_cvc_words(cvcWordResult.equalsIgnoreCase("N") ? "Null" : cvcWordResult);

        String nonCVCWordsResult = ((RadioButton) findViewById(nonCVCWordsResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setPhonics_cvc_nonwords(nonCVCWordsResult.equalsIgnoreCase("N") ? "Null" : nonCVCWordsResult);

        String _4letterblendResult = ((RadioButton) findViewById(_4letterblendResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setPhonics_four_letters_plus_blend(_4letterblendResult.equalsIgnoreCase("N") ? "Null" : _4letterblendResult);

        String _4LetterDigraphResult = ((RadioButton) findViewById(_4LetterDigraphResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setPhonics_four_letters_plus_digraph(_4LetterDigraphResult.equalsIgnoreCase("N") ? "Null" : _4LetterDigraphResult);

        String _5letterblendResult = ((RadioButton) findViewById(_5letterblendResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setPhonics_five_letters_plus_blend(_5letterblendResult.equalsIgnoreCase("N") ? "Null" : _5letterblendResult);

        String CommonPlusVowelDigraphResult = ((RadioButton) findViewById(CommonPlusVowelDigraphResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setPhonics_common_plus_vowel_digraph(CommonPlusVowelDigraphResult.equalsIgnoreCase("N") ? "Null" : CommonPlusVowelDigraphResult);

        String highFrequencyWordsResult = ((RadioButton) findViewById(highFrequencyWordsResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setPhonics_high_frequency_words(highFrequencyWordsResult.equalsIgnoreCase("N") ? "Null" : highFrequencyWordsResult);

        String oralCompResult = ((RadioButton) findViewById(oralCompResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setComprehension_oral(oralCompResult.equalsIgnoreCase("N") ? "Null" : oralCompResult);

        // Age Group
        String comprehensionLevel = comprehensionLevelTv.getText().toString();

        switch (comprehensionLevel) {
            case "Reading Age 5 to 6":
                phonicsResultsToSave.setAge_group("1");
                break;
            case "Reading Age 6 to 7":
                phonicsResultsToSave.setAge_group("2");
                break;
            case "Reading Age 7 to 8":
                phonicsResultsToSave.setAge_group("3");
                break;
            case "Reading Age 8 to 9":
                phonicsResultsToSave.setAge_group("4");
                break;
            case "Reading Age 9 to 10":
                phonicsResultsToSave.setAge_group("5");
                break;
            case "Reading Age 10 to 11":
                phonicsResultsToSave.setAge_group("6");
                break;
            default:
                phonicsResultsToSave.setAge_group("Null");
                break;
        }

        // 2018/09/15: add comprehension_words_read_int field to PhonicsResults so we could have a 0-5 int representation of our wordsRead test (which is a string, for old reasons)
        String selectedWordsRead = wordsReadSpinner.getSelectedItem().toString();
        phonicsResultsToSave.setComprehension_words_read(selectedWordsRead.equalsIgnoreCase("N") ? "Null" : selectedWordsRead);

        switch (selectedWordsRead) {
            case "1–10":
                phonicsResultsToSave.setComprehension_words_read_int("1");
                break;
            case "11–20":
                phonicsResultsToSave.setComprehension_words_read_int("2");
                break;
            case "21–30":
                phonicsResultsToSave.setComprehension_words_read_int("3");
                break;
            case "31–40":
                phonicsResultsToSave.setComprehension_words_read_int("4");
                break;
            case "41–50":
                phonicsResultsToSave.setComprehension_words_read_int("5");
                break;
            case "51–60":
                phonicsResultsToSave.setComprehension_words_read_int("6");
                break;
            case "61–70":
                phonicsResultsToSave.setComprehension_words_read_int("7");
                break;
            case "71–80+":
                phonicsResultsToSave.setComprehension_words_read_int("8");
                break;
            default:
                phonicsResultsToSave.setComprehension_words_read_int("0");
                break;
        }

        String readingCompResult = ((RadioButton) findViewById(readingCompResultRG.getCheckedRadioButtonId())).getText().toString();
        phonicsResultsToSave.setComprehension_reading(readingCompResult.equalsIgnoreCase("N") ? "Null" : readingCompResult);

        if (newResults) {
            if (currentStudent.getResults() == null) {
                currentStudent.setResults(new ArrayList<>());
            }
            currentStudent.getResults().add(phonicsResultsToSave);
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

        if (currentStudent != null) {
            PhonicsResults noObjectIdResult = null;
            int i = 0;
            for (; i < currentStudent.getResults().size(); i++) {
                PhonicsResults result = currentStudent.getResults().get(i);
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
                PhonicsResults phonicsResultsToSave = null;
                for (PhonicsResults result : currentStudent.getResults()) {
                    if (result.getTerm() - 1 == i) {
                        phonicsResultsToSave = result;
                        break;
                    }
                }
                phonicsResultsToSave.saveAsync(new AsyncCallback<PhonicsResults>() {
                    @Override
                    public void handleResponse(PhonicsResults response) {
                        int n = 0;
                        boolean updated = false;
                        for (PhonicsResults result : currentStudent.getResults()) {
                            if (result.getTerm() == i2) {
                                currentStudent.getResults().set(n, response);
                                updated = true;
                                break;
                            }
                            n += 1;
                        }
                        if (!updated)
                            currentStudent.getResults().add(response);
                        doSaveStuff2();
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        saveResultsButton.setEnabled(true);
                        saveResultsButton.setText("Save PhonicsResults");
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
                noObjectIdResult.saveAsync(new AsyncCallback<PhonicsResults>() {
                    @Override
                    public void handleResponse(PhonicsResults response) {
                        try {
                            currentStudent.getResults().set(i2, response);
                            doSaveStuff2();
                        } catch (Exception e) {
                            // TODO:remove this after Sheena gets us the error
                            Toast.makeText(getApplicationContext(), "Report Phonics Error: currentStudent: " + currentStudent.getObjectId() + ", i2: " + i2 + ", response: " + (response != null ? response.getObjectId() : "null") + ", " + e.getMessage() + (e.getCause() != null ? " | " + e.getCause().getMessage() : ""), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        saveResultsButton.setEnabled(true);
                        saveResultsButton.setText("Save PhonicsResults");
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
        } else
            Toast.makeText(this, "No user found!", Toast.LENGTH_SHORT).show();
    }

    private void doSaveStuff2() {
        //Backendless.Data.of(Students.class).setRelation(currentStudent, "results", currentStudent.getResults(), new AsyncCallback<Integer>() {
        Backendless.Data.of(Students.class).setRelation(currentStudent, "phonicsResults", currentStudent.getResults(), new AsyncCallback<Integer>() {
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
                saveResultsButton.setText("Save PhonicsResults");
                customProgressBar.dismissMe();

                Toast.makeText(getApplicationContext(), "Saved changes", Toast.LENGTH_LONG).show();
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                saveResultsButton.setEnabled(true);
                saveResultsButton.setText("Save PhonicsResults");
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void onLogoutButtonClicked() {
        customProgressBar.showMe(PostLoginRecordResultsManagerActivity.this, "Please Wait...");
        Backendless.UserService.logout(new DefaultCallback<Void>(this) {
            @Override
            public void handleResponse(Void response) {
                super.handleResponse(response);
                customProgressBar.dismissMe();
                //setIsLogin(PostLoginRecordResultsManagerActivity.this, "1");
                //clearSharePreferencesDetails(PostLoginRecordResultsManagerActivity.this);
                Intent i = new Intent(PostLoginRecordResultsManagerActivity.this, GetMasterTemplateActivity.class);
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

        AlertDialog alertDialog = alertDialogBuilder.create();
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

    private void showMyToast() {
        Toast.makeText(PostLoginRecordResultsManagerActivity.this, NEED_BEFORE_SCORE, Toast.LENGTH_SHORT).show();

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(700);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    isShowToast = true;
                }
            }
        };
        timerThread.start();
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
                convertView = View.inflate(getContext(), R.layout.list_textview, null);
            TextView tvText1 = convertView.findViewById(R.id.listTextView);
            String upnshort = "" + getItem(position).getUpn();
            if (upnshort.length() >= 3)
                upnshort = getItem(position).getUpn().substring(getItem(position).getUpn().length() - 3);
            tvText1.setText(getItem(position).getLastName() + ", " + getItem(position).getFirstName() + " (" + upnshort + ")");
            return convertView;
        }
    }

    @SuppressLint("StaticFieldLeak")
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

            if (result != null && "error".equals(result)) {
                errorFailedPrinting();
            }

            mProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Object doInBackground(Object... params) {
            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            File dir = new File(file_path);
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
            new AlertDialog.Builder(PostLoginRecordResultsManagerActivity.this)
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
                        PostLoginRecordResultsManagerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PostLoginRecordResultsManagerActivity.this, "Bluetooth is not ready. Restart app.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(PostLoginRecordResultsManagerActivity.this, "Bluetooth is not ready. Restart app.", Toast.LENGTH_LONG).show();
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
