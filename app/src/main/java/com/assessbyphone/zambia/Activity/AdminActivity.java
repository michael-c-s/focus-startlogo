package com.assessbyphone.zambia.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.assessbyphone.zambia.Models.MathsResults;
import com.assessbyphone.zambia.Models.PhonicsResults;
import com.assessbyphone.zambia.Models.SerializableManager;
import com.assessbyphone.zambia.Models.Students;
import com.assessbyphone.zambia.R;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.assessbyphone.zambia.CallbackUtils.UiUtil.getIsAdmin;

public class AdminActivity extends Activity {
    public SharedPreferences sPrefs;
    Spinner spinner;
    EditText firstname;
    EditText lastname;
    RadioGroup sex;
    RadioButton male;
    RadioButton female;
    EditText upn;
    TextView scoresCompleted;
    TextView phonicsAvg;
    TextView compAvg;
    TextView overallAvg;
    TextView mathsScoresCompleted;
    TextView numbersAvg;
    TextView wordProblemsAvg;
    TextView overallMathsAvg;
    BackendlessUser teacher;
    boolean isStatsAdmin = false;
    Button update;
    Button add;
    int currentTerm = 1;
    Students[] students;
    Students currentStudent;
    BackendlessUser[] adminClasses;
    BarChart chart1;
    BarChart chart2;
    BarChart chart3;
    BarChart chart4;
    BarChart chart1m;
    BarChart chart2m;
    BarChart chart3m;
    BarChart chart4m;
    private RadioGroup termRG;
    private ImageView adminBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.admin_screen);

        sPrefs = getPreferences(MODE_PRIVATE);

        Log.e("AdminResults", "AdminResults");

        adminBack = findViewById(R.id.adminBack_ids);

        //teacher = GetMasterTemplateActivity.myBackendlessUser;
        teacher = (BackendlessUser) getIntent().getSerializableExtra("backendlessUser");

        isStatsAdmin = getIsAdmin(AdminActivity.this);

        if (!isStatsAdmin) {
            try {
                ArrayList<Students> s = ((ArrayList<Students>) teacher.getProperty("students"));
                students = new Students[s.size()];
                s.toArray(students);
                if (students.length <= 0)
                    throw new InvalidParameterException("No students assigned");
            } catch (ClassCastException e) {
                students = (Students[]) teacher.getProperty("students");
                if (students.length <= 0)
                    throw new InvalidParameterException("No students assigned");
            } catch (Exception e) {
                new AlertDialog.Builder(AdminActivity.this)
                        .setTitle("Notice")
                        .setMessage("This teacher has no students assigned to them. Please add some.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            if (students != null) {
                Log.d(MenuActivity.TAG, "got " + students.length + " students for " + teacher.getProperty("name"));
            }
            initStudentAreaUI();
        } else {
            try {
                ArrayList<BackendlessUser> ac = ((ArrayList<BackendlessUser>) teacher.getProperty("adminClasses"));
                adminClasses = new BackendlessUser[ac.size()];
                ac.toArray(adminClasses);
                if (adminClasses.length <= 0)
                    throw new InvalidParameterException("No schools/classes assigned");
            } catch (ClassCastException e) {
                adminClasses = (BackendlessUser[]) teacher.getProperty("adminClasses");
                if (adminClasses.length <= 0)
                    throw new InvalidParameterException("No schools/classes assigned");
            } catch (Exception e) {
                new AlertDialog.Builder(AdminActivity.this)
                        .setTitle("Notice")
                        .setMessage("You have no schools/classes assigned to you that you can view stats for.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            if (adminClasses != null) {
                Log.d(MenuActivity.TAG, "got " + adminClasses.length + " admin classes for " + teacher.getProperty("name"));
            }
            initStatsAdminUI();
        }
    }

    void initStatsAdminUI() {
        findViewById(R.id.student_area).setVisibility(View.GONE);
        findViewById(R.id.adminclasses_area).setVisibility(View.VISIBLE);

        termRG = findViewById(R.id.readingTerm);
        termRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                currentTerm = Integer.parseInt(((RadioButton) findViewById(i)).getText().toString());
                calcHTScores();
            }
        });

        scoresCompleted = findViewById(R.id.admResultsCompleted);
        phonicsAvg = findViewById(R.id.admAveragePhonicsScore);
        compAvg = findViewById(R.id.admAverageCompScore);
        overallAvg = findViewById(R.id.admOverallScore);

        mathsScoresCompleted = findViewById(R.id.admMathsResultsCompleted);
        numbersAvg = findViewById(R.id.admAverageNumbersScore);
        wordProblemsAvg = findViewById(R.id.admAverageWordProblemsScore);
        overallMathsAvg = findViewById(R.id.admOverallMathsScore);

        spinner = findViewById(R.id.adminClassesList);
        final List<BackendlessUser> adminClassesList = new ArrayList<BackendlessUser>();
        BackendlessUser allPlaces = new BackendlessUser();
        allPlaces.setProperty("name", "Total overview");
        adminClassesList.add(allPlaces);
        if (adminClasses != null && adminClasses.length > 0) {
            List<BackendlessUser> sortedPups = Arrays.asList(adminClasses);
            Collections.sort(sortedPups, new Comparator<BackendlessUser>() {
                @Override
                public int compare(BackendlessUser lhs, BackendlessUser rhs) {
                    if (lhs.getProperty("name") == null || rhs.getProperty("name") == null) {
                        return 0;
                    }
                    return lhs.getProperty("name").toString().compareToIgnoreCase(rhs.getProperty("name").toString());
                }
            });
            adminClassesList.addAll(sortedPups);
        }

        spinner = findViewById(R.id.adminClassesList);
        spinner.setAdapter(new AdminClassesAdapter(getBaseContext(), adminClassesList));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayList<Students> studentsToCal = new ArrayList<Students>();
                if (i == 0) {
                    for (BackendlessUser adminClass : adminClasses) {
                        studentsToCal.addAll(((ArrayList<Students>) adminClass.getProperty("students")));
                    }
                    students = studentsToCal.toArray(new Students[studentsToCal.size()]);
                    calcHTScores();
                } else {
                    BackendlessUser adminClass = (BackendlessUser) spinner.getAdapter().getItem(i);
                    studentsToCal.addAll(((ArrayList<Students>) adminClass.getProperty("students")));
                    students = studentsToCal.toArray(new Students[studentsToCal.size()]);
                    calcSingleClassScores();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        calcHTScores();
    }

    void initStudentAreaUI() {
        findViewById(R.id.student_area).setVisibility(View.VISIBLE);
        findViewById(R.id.adminclasses_area).setVisibility(View.GONE);

        male = findViewById(R.id.admSexMale);
        female = findViewById(R.id.admSexFemale);

        termRG = findViewById(R.id.readingTerm);
        termRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                currentTerm = Integer.parseInt(((RadioButton) findViewById(i)).getText().toString());
                calcSingleClassScores();
            }
        });

        spinner = findViewById(R.id.studentList);
        final List<Students> studentsList = new ArrayList<Students>();
        Students dummyStudent = new Students();
        dummyStudent.setLastName("<ADD");
        dummyStudent.setFirstName(" NEW>");
        dummyStudent.setUpn("");
        studentsList.add(dummyStudent);
        if (students != null && students.length > 0) {
            List<Students> sortedPups = Arrays.asList(students);
            Collections.sort(sortedPups, new Comparator<Students>() {
                @Override
                public int compare(Students lhs, Students rhs) {
                    return lhs.getUpn().compareToIgnoreCase(rhs.getUpn());
                }
            });
//            Collections.sort(sortedPups, new Comparator<Students>() {
//                @Override
//                public int compare(Students o1, Students o2) {
//                    if ("male".equals(o1.getSex().toLowerCase()) && !"male".equals(o2.getSex().toLowerCase()))
//                        return 1;
//                    else if ("male".equals(o1.getSex().toLowerCase()) && "male".equals(o2.getSex().toLowerCase()))
//                        return 0;
//                    else
//                        return -1;
//                }
//            });
            studentsList.addAll(sortedPups);
        }

        spinner = findViewById(R.id.studentList);
        spinner.setAdapter(new StudentsAdapter(getBaseContext(), studentsList));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    firstname.setText("");
                    lastname.setText("");
                    upn.setText("");
                    male.setChecked(true);
                    return; // ignore dummy
                }

                currentStudent = (Students) spinner.getItemAtPosition(i);

                firstname.setText(currentStudent.getFirstName());
                lastname.setText(currentStudent.getLastName());
                upn.setText(currentStudent.getUpn());
                if ("male".equals(currentStudent.getSex())) {
                    female.setChecked(false);
                    male.setChecked(true);
                } else {
                    male.setChecked(false);
                    female.setChecked(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        firstname = findViewById(R.id.admFirstname);
        lastname = findViewById(R.id.admLastname);
        upn = findViewById(R.id.admUPN);
        sex = findViewById(R.id.admSex);

        scoresCompleted = findViewById(R.id.admResultsCompleted);
        phonicsAvg = findViewById(R.id.admAveragePhonicsScore);
        compAvg = findViewById(R.id.admAverageCompScore);
        overallAvg = findViewById(R.id.admOverallScore);

        mathsScoresCompleted = findViewById(R.id.admMathsResultsCompleted);
        numbersAvg = findViewById(R.id.admAverageNumbersScore);
        wordProblemsAvg = findViewById(R.id.admAverageWordProblemsScore);
        overallMathsAvg = findViewById(R.id.admOverallMathsScore);

        update = findViewById(R.id.admUpdate);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spinner.getSelectedItemPosition() == 0)
                    return; // ignore dummy

                update.setEnabled(false);
                update.setText("Updating...");

                Students student = (Students) spinner.getSelectedItem();
                student.setFirstName(firstname.getText().toString().trim());
                student.setLastName(lastname.getText().toString().trim());
                student.setUpn(upn.getText().toString().trim());
                student.setSex(male.isChecked() ? "male" : "female");

                SerializableManager.saveSerializable(getBaseContext(), teacher, "backendlessUser_" + teacher.getProperty("utn"));

                student.saveAsync(new AsyncCallback<Students>() {
                    @Override
                    public void handleResponse(Students students) {
                        update.setEnabled(true);
                        update.setText("Update");

                        Toast.makeText(getApplicationContext(), "Updated changes", Toast.LENGTH_LONG).show();

                        Intent newIntent = new Intent(getBaseContext(), AdminActivity.class);

                        newIntent.putExtra("backendlessUser", teacher);
                        finish();
                        startActivity(newIntent);
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        update.setEnabled(true);
                        update.setText("Update");

                        if (backendlessFault != null && backendlessFault.getMessage() != null
                                && (backendlessFault.getMessage().contains("Unable to resolve")
                                || backendlessFault.getMessage().contains("timeout"))
                        ) {
                            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("needSync_" + teacher.getProperty("utn"), true).apply();
                            Toast.makeText(getApplicationContext(), "Changes saved locally because you're offline!", Toast.LENGTH_LONG).show();
                            Intent newIntent = new Intent(getBaseContext(), AdminActivity.class);
                            newIntent.putExtra("backendlessUser", teacher);
                            finish();
                            startActivity(newIntent);
                            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to update changes! " + backendlessFault.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        add = findViewById(R.id.admAddAsNew);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spinner.getSelectedItemPosition() != 0) {
                    Toast.makeText(getApplicationContext(), "Select <ADD NEW> first!", Toast.LENGTH_LONG).show();
                    return; // ignore non-dummy
                }

                if ("".equals(firstname.getText().toString()) || "".equals(lastname.getText().toString()) || "".equals(upn.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Enter all information", Toast.LENGTH_LONG).show();
                    return;
                }

                add.setEnabled(false);
                add.setText("Adding...");

                final Students student = new Students();
                student.setFirstName(firstname.getText().toString().trim());
                student.setLastName(lastname.getText().toString().trim());
                student.setUpn(upn.getText().toString().trim());
                student.setSex(male.isChecked() ? "male" : "female");

                Backendless.Data.of(Students.class).save(student, new AsyncCallback<Students>() {
                    @Override
                    public void handleResponse(final Students newStudentUpdated) {
                        Backendless.Data.of(BackendlessUser.class).addRelation(teacher, "students", new ArrayList<Students>() {{
                            add(student);
                        }}, new AsyncCallback<Integer>() {
                            @Override
                            public void handleResponse(Integer response) {
                                add.setEnabled(true);
                                add.setText("ADD AS NEW");

                                if (students != null && students.length > 0) {
                                    ArrayList<Students> newStudentList = new ArrayList<Students>(studentsList);
                                    newStudentList.remove(0); // remove "<ADD NEW>"
                                    newStudentList.add(newStudentUpdated);
                                    teacher.setProperty("students", newStudentList);
                                } else {
                                    teacher.setProperty("students", new ArrayList<Students>() {{
                                        add(newStudentUpdated);
                                    }});
                                }

                                SerializableManager.saveSerializable(getBaseContext(), teacher, "backendlessUser_" + teacher.getProperty("utn"));
                                Toast.makeText(getApplicationContext(), "Updated teacher with new student", Toast.LENGTH_LONG).show();
                                Intent newIntent = new Intent(getBaseContext(), AdminActivity.class);
                                newIntent.putExtra("backendlessUser", teacher);
                                finish();
                                startActivity(newIntent);
                                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                            }

                            @Override
                            public void handleFault(BackendlessFault backendlessFault) {
                                add.setEnabled(true);
                                add.setText("ADD AS NEW");

                                if (backendlessFault != null && backendlessFault.getMessage() != null
                                        && (backendlessFault.getMessage().contains("Unable to resolve")
                                        || backendlessFault.getMessage().contains("timeout"))
                                ) {
                                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("needSync_" + teacher.getProperty("utn"), true).apply();
                                    Toast.makeText(getApplicationContext(), "Teacher updated with new student locally because you're offline!", Toast.LENGTH_LONG).show();
                                    Intent newIntent = new Intent(getBaseContext(), AdminActivity.class);
                                    newIntent.putExtra("backendlessUser", teacher);
                                    finish();
                                    startActivity(newIntent);
                                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to update teacher with new student! " + backendlessFault.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        add.setEnabled(true);
                        add.setText("ADD AS NEW");

                        if (backendlessFault != null && backendlessFault.getMessage() != null
                                && (backendlessFault.getMessage().contains("Unable to resolve")
                                || backendlessFault.getMessage().contains("timeout"))
                        ) {
                            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("needSync_" + teacher.getProperty("utn"), true).apply();
                            Toast.makeText(getApplicationContext(), "Teacher updated with new student locally because you're offline!", Toast.LENGTH_LONG).show();
                            Intent newIntent = new Intent(getBaseContext(), AdminActivity.class);
                            newIntent.putExtra("backendlessUser", teacher);
                            finish();
                            startActivity(newIntent);
                            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to update teacher with new student! " + backendlessFault.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        calcSingleClassScores();
    }

    @SuppressLint("SetTextI18n")
    void calcSingleClassScores() {
        chart1 = findViewById(R.id.plot1);
        chart1.setVisibility(View.GONE);
        chart2 = findViewById(R.id.plot2);
        chart2.setVisibility(View.GONE);
        chart3 = findViewById(R.id.plot3);
        chart3.setVisibility(View.GONE);
        chart4 = findViewById(R.id.plot4);
        chart4.setVisibility(View.GONE);

        chart1m = findViewById(R.id.plot1m);
        chart1m.setVisibility(View.GONE);
        chart2m = findViewById(R.id.plot2m);
        chart2m.setVisibility(View.GONE);
        chart3m = findViewById(R.id.plot3m);
        chart3m.setVisibility(View.GONE);
        chart4m = findViewById(R.id.plot4m);
        chart4m.setVisibility(View.GONE);

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(0);

        int totalNumber = 0;
        float phonicsTotal = 0;
        float compTotal = 0;

        int mathsTotalNumber = 0;
        int numbersTotal = 0;
        int wordProblemsTotal = 0;

        if (students != null) {
            for (Students student : students) {
                if (student.getResults() != null) {
                    for (PhonicsResults result : student.getResults()) {
                        if (currentTerm == result.getTerm()) {
                            totalNumber++;
                            phonicsTotal += Integer.parseInt((result.getPhonics_common_plus_vowel_digraph() == null || result.getPhonics_common_plus_vowel_digraph().equals("Null")) ? "0" : result.getPhonics_common_plus_vowel_digraph()) +
                                    Integer.parseInt((result.getPhonics_high_frequency_words() == null || result.getPhonics_high_frequency_words().equals("Null")) ? "0" : result.getPhonics_high_frequency_words()) +
                                    Integer.parseInt((result.getPhonics_four_letters_plus_digraph() == null || result.getPhonics_four_letters_plus_digraph().equals("Null")) ? "0" : result.getPhonics_four_letters_plus_digraph()) +
                                    Integer.parseInt((result.getPhonics_four_letters_plus_blend() == null || result.getPhonics_four_letters_plus_blend().equals("Null")) ? "0" : result.getPhonics_four_letters_plus_blend()) +
                                    Integer.parseInt((result.getPhonics_five_letters_plus_blend() == null || result.getPhonics_five_letters_plus_blend().equals("Null")) ? "0" : result.getPhonics_five_letters_plus_blend()) +
                                    Integer.parseInt((result.getPhonics_letter_sounds() == null || result.getPhonics_letter_sounds().equals("Null")) ? "0" : result.getPhonics_letter_sounds()) +
                                    Integer.parseInt((result.getPhonics_pictures() == null || result.getPhonics_pictures().equals("Null")) ? "0" : result.getPhonics_pictures()) +
                                    Integer.parseInt((result.getPhonics_cvc_words() == null || result.getPhonics_cvc_words().equals("Null")) ? "0" : result.getPhonics_cvc_words()) +
                                    Integer.parseInt((result.getPhonics_cvc_nonwords() == null || result.getPhonics_cvc_nonwords().equals("Null")) ? "0" : result.getPhonics_cvc_nonwords());

                            compTotal += Integer.parseInt((result.getComprehension_oral() == null || result.getComprehension_oral().equals("Null")) ? "0" : result.getComprehension_oral()) +
                                    +Integer.parseInt((result.getComprehension_reading() == null || result.getComprehension_reading().equals("Null")) ? "0" : result.getComprehension_reading()) +
                                    +Integer.parseInt((result.getComprehension_words_read_int() == null || result.getComprehension_words_read_int().equals("Null")) ? "0" : result.getComprehension_words_read_int());
                        }
                    }
                }

                if (student.getMathsResults() != null) {
                    for (MathsResults result : student.getMathsResults()) {
                        if (currentTerm == Integer.parseInt(result.getTerm())) {
                            mathsTotalNumber++;
                            numbersTotal +=
                                    Integer.parseInt((result.getCounting_objects_to_10() == null || result.getCounting_objects_to_10().equals("Null")) ? "0" : result.getCounting_objects_to_10()) +
                                            Integer.parseInt((result.getIdentify_number_to_10() == null || result.getIdentify_number_to_10().equals("Null")) ? "0" : result.getIdentify_number_to_10()) +
                                            Integer.parseInt((result.getIdentify_greatest_number_to_20() == null || result.getIdentify_greatest_number_to_20().equals("Null")) ? "0" : result.getIdentify_greatest_number_to_20()) +
                                            Integer.parseInt((result.getCounting_in_ones_missing_numbers() == null || result.getCounting_in_ones_missing_numbers().equals("Null")) ? "0" : result.getCounting_in_ones_missing_numbers()) +
                                            Integer.parseInt((result.getAdd_numbers_to_10() == null || result.getAdd_numbers_to_10().equals("Null")) ? "0" : result.getAdd_numbers_to_10()) +
                                            Integer.parseInt((result.getSubtract_numbers_to_10() == null || result.getSubtract_numbers_to_10().equals("Null")) ? "0" : result.getSubtract_numbers_to_10()) +
                                            Integer.parseInt((result.getIdentify_number_to_20() == null || result.getIdentify_number_to_20().equals("Null")) ? "0" : result.getIdentify_number_to_20()) +
                                            Integer.parseInt((result.getIdentify_greatest_number_to_50() == null || result.getIdentify_greatest_number_to_50().equals("Null")) ? "0" : result.getIdentify_greatest_number_to_50()) +
                                            Integer.parseInt((result.getCounting_in_tens_missing_numbers() == null || result.getCounting_in_tens_missing_numbers().equals("Null")) ? "0" : result.getCounting_in_tens_missing_numbers()) +
                                            Integer.parseInt((result.getAdd_numbers_to_20() == null || result.getAdd_numbers_to_20().equals("Null")) ? "0" : result.getAdd_numbers_to_20()) +
                                            Integer.parseInt((result.getSubtract_numbers_to_20() == null || result.getSubtract_numbers_to_20().equals("Null")) ? "0" : result.getSubtract_numbers_to_20()) +
                                            Integer.parseInt((result.getMultiply_1_digit_numbers() == null || result.getMultiply_1_digit_numbers().equals("Null")) ? "0" : result.getMultiply_1_digit_numbers()) +
                                            Integer.parseInt((result.getDivide_2_digit_by_1_digit_numbers_to_20() == null || result.getDivide_2_digit_by_1_digit_numbers_to_20().equals("Null")) ? "0" : result.getDivide_2_digit_by_1_digit_numbers_to_20()) +
                                            Integer.parseInt((result.getIdentify_number_to_99() == null || result.getIdentify_number_to_99().equals("Null")) ? "0" : result.getIdentify_number_to_99()) +
                                            Integer.parseInt((result.getIdentify_greatest_number_to_150() == null || result.getIdentify_greatest_number_to_150().equals("Null")) ? "0" : result.getIdentify_greatest_number_to_150()) +
                                            Integer.parseInt((result.getCounting_in_2s_5s_missing_numbers() == null || result.getCounting_in_2s_5s_missing_numbers().equals("Null")) ? "0" : result.getCounting_in_2s_5s_missing_numbers()) +
                                            Integer.parseInt((result.getAdd_two_2_digit_numbers() == null || result.getAdd_two_2_digit_numbers().equals("Null")) ? "0" : result.getAdd_two_2_digit_numbers()) +
                                            Integer.parseInt((result.getSubtract_1_digit_from_2_digits_to_99() == null || result.getSubtract_1_digit_from_2_digits_to_99().equals("Null")) ? "0" : result.getSubtract_1_digit_from_2_digits_to_99()) +
                                            Integer.parseInt((result.getMultiply_1_digit_by_2_digit_numbers() == null || result.getMultiply_1_digit_by_2_digit_numbers().equals("Null")) ? "0" : result.getMultiply_1_digit_by_2_digit_numbers()) +
                                            Integer.parseInt((result.getDivide_2_digit_by_1_digit_numbers_over_20() == null || result.getDivide_2_digit_by_1_digit_numbers_over_20().equals("Null")) ? "0" : result.getDivide_2_digit_by_1_digit_numbers_over_20()) +
                                            Integer.parseInt((result.getIdentify_number_to_999() == null || result.getIdentify_number_to_999().equals("Null")) ? "0" : result.getIdentify_number_to_999()) +
                                            Integer.parseInt((result.getIdentify_greatest_number_to_1100() == null || result.getIdentify_greatest_number_to_1100().equals("Null")) ? "0" : result.getIdentify_greatest_number_to_1100()) +
                                            Integer.parseInt((result.getCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers() == null || result.getCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers().equals("Null")) ? "0" : result.getCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers()) +
                                            Integer.parseInt((result.getAdd_two_and_three_digit_numbers() == null || result.getAdd_two_and_three_digit_numbers().equals("Null")) ? "0" : result.getAdd_two_and_three_digit_numbers()) +
                                            Integer.parseInt((result.getSubtract_2_digits_from_2_digits() == null || result.getSubtract_2_digits_from_2_digits().equals("Null")) ? "0" : result.getSubtract_2_digits_from_2_digits()) +
                                            Integer.parseInt((result.getMultiply_2_digit_by_2_digit_numbers_to_20() == null || result.getMultiply_2_digit_by_2_digit_numbers_to_20().equals("Null")) ? "0" : result.getMultiply_2_digit_by_2_digit_numbers_to_20()) +
                                            Integer.parseInt((result.getDivide_2_digit_by_1_digit_numbers_to_99() == null || result.getDivide_2_digit_by_1_digit_numbers_to_99().equals("Null")) ? "0" : result.getDivide_2_digit_by_1_digit_numbers_to_99()) +
                                            Integer.parseInt((result.getIdentify_number_to_9999999() == null || result.getIdentify_number_to_9999999().equals("Null")) ? "0" : result.getIdentify_number_to_9999999()) +
                                            Integer.parseInt((result.getIdentify_greatest_number_to_9999() == null || result.getIdentify_greatest_number_to_9999().equals("Null")) ? "0" : result.getIdentify_greatest_number_to_9999()) +
                                            Integer.parseInt((result.getCounting_in_various_missing_numbers() == null || result.getCounting_in_various_missing_numbers().equals("Null")) ? "0" : result.getCounting_in_various_missing_numbers()) +
                                            Integer.parseInt((result.getAdd_two_three_digit_numbers_including_deci() == null || result.getAdd_two_three_digit_numbers_including_deci().equals("Null")) ? "0" : result.getAdd_two_three_digit_numbers_including_deci()) +
                                            Integer.parseInt((result.getSubtract_2_digit_from_3_digits_including_deci() == null || result.getSubtract_2_digit_from_3_digits_including_deci().equals("Null")) ? "0" : result.getSubtract_2_digit_from_3_digits_including_deci()) +
                                            Integer.parseInt((result.getMultiply_2_digit_by_2_digit_numbers_over_20() == null || result.getMultiply_2_digit_by_2_digit_numbers_over_20().equals("Null")) ? "0" : result.getMultiply_2_digit_by_2_digit_numbers_over_20()) +
                                            Integer.parseInt((result.getDivide_2_and_3_digit_by_2_digit_numbers() == null || result.getDivide_2_and_3_digit_by_2_digit_numbers().equals("Null")) ? "0" : result.getDivide_2_and_3_digit_by_2_digit_numbers());

                            wordProblemsTotal +=
                                    Integer.parseInt((result.getWp_addition() == null || result.getWp_addition().equals("Null")) ? "0" : result.getWp_addition()) +
                                            Integer.parseInt((result.getWp_subtraction() == null || result.getWp_subtraction().equals("Null")) ? "0" : result.getWp_subtraction()) +
                                            Integer.parseInt((result.getWp_multiplication() == null || result.getWp_multiplication().equals("Null")) ? "0" : result.getWp_multiplication()) +
                                            Integer.parseInt((result.getWp_division() == null || result.getWp_division().equals("Null")) ? "0" : result.getWp_division());
                        }
                    }
                }
            }

            if (totalNumber > 0) {
                scoresCompleted.setText(totalNumber + "/" + students.length + " (" + df.format(((float) totalNumber) / students.length * 100) + "%)");
                phonicsAvg.setText(df.format(phonicsTotal / totalNumber) + "/27");
                compAvg.setText(df.format(compTotal / totalNumber) + "/15");
                overallAvg.setText(df.format((phonicsTotal + compTotal) / totalNumber) + "/42");
            } else {
                scoresCompleted.setText("no data");
                phonicsAvg.setText("no data");
                compAvg.setText("no data");
                overallAvg.setText("no data");
            }

            if (mathsTotalNumber > 0) {
                mathsScoresCompleted.setText(mathsTotalNumber + "/" + students.length + " (" + df.format(((float) mathsTotalNumber) / students.length * 100) + "%)");
                numbersAvg.setText(df.format(numbersTotal / mathsTotalNumber) + "/97");
                wordProblemsAvg.setText(df.format(wordProblemsTotal / mathsTotalNumber) + "/8");
                overallMathsAvg.setText(df.format((numbersTotal + wordProblemsTotal) / mathsTotalNumber) + "/105");
            } else {
                mathsScoresCompleted.setText("no data");
                numbersAvg.setText("no data");
                wordProblemsAvg.setText("no data");
                overallMathsAvg.setText("no data");
            }
        }
    }

    void calcHTScores() {
        chart1 = findViewById(R.id.plot1);
        chart1.setVisibility(View.VISIBLE);
        chart2 = findViewById(R.id.plot2);
        chart2.setVisibility(View.VISIBLE);
        chart3 = findViewById(R.id.plot3);
        chart3.setVisibility(View.VISIBLE);
        chart4 = findViewById(R.id.plot4);
        chart4.setVisibility(View.VISIBLE);

        chart1m = findViewById(R.id.plot1m);
        chart1m.setVisibility(View.VISIBLE);
        chart2m = findViewById(R.id.plot2m);
        chart2m.setVisibility(View.VISIBLE);
        chart3m = findViewById(R.id.plot3m);
        chart3m.setVisibility(View.VISIBLE);
        chart4m = findViewById(R.id.plot4m);
        chart4m.setVisibility(View.VISIBLE);

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(0);

        int totalNumber = 0;
        float phonicsTotal = 0;
        float compTotal = 0;

        int mathsTotalNumber = 0;
        float numbersTotal = 0;
        float wordProblemsTotal = 0;

        ArrayList<String> schools = new ArrayList<>();
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        ArrayList<IBarDataSet> dataSets2 = new ArrayList<IBarDataSet>();
        ArrayList<IBarDataSet> dataSets3 = new ArrayList<IBarDataSet>();
        ArrayList<IBarDataSet> dataSets4 = new ArrayList<IBarDataSet>();

        ArrayList<IBarDataSet> dataSets5 = new ArrayList<IBarDataSet>();
        ArrayList<IBarDataSet> dataSets6 = new ArrayList<IBarDataSet>();
        ArrayList<IBarDataSet> dataSets7 = new ArrayList<IBarDataSet>();
        ArrayList<IBarDataSet> dataSets8 = new ArrayList<IBarDataSet>();

        int i = 0;
        for (BackendlessUser adminClass : adminClasses) {
            String schoolName = /*adminClass.getProperty("school") + " / " + */"" + adminClass.getProperty("class");
            schools.add(schoolName);

            int tcTotalNumber = 0;
            float tcPhonicsTotal = 0;
            float tcCompTotal = 0;

            int tcMathsTotalNumber = 0;
            float tcNumbersTotal = 0;
            float tcWordsTotal = 0;

            ArrayList<Students> tcStudents = (ArrayList<Students>) adminClass.getProperty("students");
            ArrayList<BarEntry> valueSet1 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> valueSet2 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> valueSet3 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> valueSet4 = new ArrayList<BarEntry>();

            ArrayList<BarEntry> valueSet5 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> valueSet6 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> valueSet7 = new ArrayList<BarEntry>();
            ArrayList<BarEntry> valueSet8 = new ArrayList<BarEntry>();

            for (Students student : tcStudents) {
                if (student.getResults() != null) {
                    for (PhonicsResults result : student.getResults()) {
                        if (currentTerm == result.getTerm()) {
                            totalNumber++;
                            tcTotalNumber++;

                            int pho = Integer.parseInt(result.getPhonics_common_plus_vowel_digraph()) +
                                    Integer.parseInt(result.getPhonics_high_frequency_words()) +
                                    Integer.parseInt(result.getPhonics_four_letters_plus_digraph()) +
                                    Integer.parseInt(result.getPhonics_four_letters_plus_blend()) +
                                    Integer.parseInt(result.getPhonics_five_letters_plus_blend()) +
                                    Integer.parseInt(result.getPhonics_letter_sounds()) +
                                    Integer.parseInt(result.getPhonics_pictures()) +
                                    Integer.parseInt(result.getPhonics_cvc_words()) +
                                    Integer.parseInt(result.getPhonics_cvc_nonwords());
                            tcPhonicsTotal += pho;
                            phonicsTotal += pho;

                            int ora = Integer.parseInt(result.getComprehension_oral()) + Integer.parseInt(result.getComprehension_reading()) + Integer.parseInt(result.getComprehension_words_read_int());
                            tcCompTotal += ora;
                            compTotal += ora;
                        }
                    }
                }
            }

            for (Students student : tcStudents) {
                if (student.getMathsResults() != null) {
                    for (MathsResults result : student.getMathsResults()) {
                        if (currentTerm == Integer.parseInt(result.getTerm())) {
                            mathsTotalNumber++;
                            tcMathsTotalNumber++;
                            //resultsToShow.getWp_addition() == null ? "Null" : resultsToShow.getWp_addition()

                            int mth =
                                    Integer.parseInt(result.getCounting_objects_to_10() == null || result.getCounting_objects_to_10().equals("Null") ? "0" : result.getCounting_objects_to_10()) +
                                            Integer.parseInt(result.getIdentify_number_to_10() == null || result.getIdentify_number_to_10().equals("Null") ? "0" : result.getIdentify_number_to_10()) +
                                            Integer.parseInt(result.getIdentify_greatest_number_to_20() == null || result.getIdentify_greatest_number_to_20().equals("Null") ? "0" : result.getIdentify_greatest_number_to_20()) +
                                            Integer.parseInt(result.getCounting_in_ones_missing_numbers() == null || result.getCounting_in_ones_missing_numbers().equals("Null") ? "0" : result.getCounting_in_ones_missing_numbers()) +
                                            Integer.parseInt(result.getAdd_numbers_to_10() == null || result.getAdd_numbers_to_10().equals("Null") ? "0" : result.getAdd_numbers_to_10()) +
                                            Integer.parseInt(result.getSubtract_numbers_to_10() == null || result.getSubtract_numbers_to_10().equals("Null") ? "0" : result.getSubtract_numbers_to_10()) +
                                            Integer.parseInt(result.getIdentify_number_to_20() == null || result.getIdentify_number_to_20().equals("Null") ? "0" : result.getIdentify_number_to_20()) +
                                            Integer.parseInt(result.getIdentify_greatest_number_to_50() == null || result.getIdentify_greatest_number_to_50().equals("Null") ? "0" : result.getIdentify_greatest_number_to_50()) +
                                            Integer.parseInt(result.getCounting_in_tens_missing_numbers() == null || result.getCounting_in_tens_missing_numbers().equals("Null") ? "0" : result.getCounting_in_tens_missing_numbers()) +
                                            Integer.parseInt(result.getAdd_numbers_to_20() == null || result.getAdd_numbers_to_20().equals("Null") ? "0" : result.getAdd_numbers_to_20()) +
                                            Integer.parseInt(result.getSubtract_numbers_to_20() == null || result.getSubtract_numbers_to_20().equals("Null") ? "0" : result.getSubtract_numbers_to_20()) +
                                            Integer.parseInt(result.getMultiply_1_digit_numbers() == null || result.getMultiply_1_digit_numbers().equals("Null") ? "0" : result.getMultiply_1_digit_numbers()) +
                                            Integer.parseInt(result.getDivide_2_digit_by_1_digit_numbers_to_20() == null || result.getDivide_2_digit_by_1_digit_numbers_to_20().equals("Null") ? "0" : result.getDivide_2_digit_by_1_digit_numbers_to_20()) +
                                            Integer.parseInt(result.getIdentify_number_to_99() == null || result.getIdentify_number_to_99().equals("Null") ? "0" : result.getIdentify_number_to_99()) +
                                            Integer.parseInt(result.getIdentify_greatest_number_to_150() == null || result.getIdentify_greatest_number_to_150().equals("Null") ? "0" : result.getIdentify_greatest_number_to_150()) +
                                            Integer.parseInt(result.getCounting_in_2s_5s_missing_numbers() == null || result.getCounting_in_2s_5s_missing_numbers().equals("Null") ? "0" : result.getCounting_in_2s_5s_missing_numbers()) +
                                            Integer.parseInt(result.getAdd_two_2_digit_numbers() == null || result.getAdd_two_2_digit_numbers().equals("Null") ? "0" : result.getAdd_two_2_digit_numbers()) +
                                            Integer.parseInt(result.getSubtract_1_digit_from_2_digits_to_99() == null || result.getSubtract_1_digit_from_2_digits_to_99().equals("Null") ? "0" : result.getSubtract_1_digit_from_2_digits_to_99()) +
                                            Integer.parseInt(result.getMultiply_1_digit_by_2_digit_numbers() == null || result.getMultiply_1_digit_by_2_digit_numbers().equals("Null") ? "0" : result.getMultiply_1_digit_by_2_digit_numbers()) +
                                            Integer.parseInt(result.getDivide_2_digit_by_1_digit_numbers_over_20() == null || result.getDivide_2_digit_by_1_digit_numbers_over_20().equals("Null") ? "0" : result.getDivide_2_digit_by_1_digit_numbers_over_20()) +
                                            Integer.parseInt(result.getIdentify_number_to_999() == null || result.getIdentify_number_to_999().equals("Null") ? "0" : result.getIdentify_number_to_999()) +
                                            Integer.parseInt(result.getIdentify_greatest_number_to_1100() == null || result.getIdentify_greatest_number_to_1100().equals("Null") ? "0" : result.getIdentify_greatest_number_to_1100()) +
                                            Integer.parseInt(result.getCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers() == null || result.getCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers().equals("Null") ? "0" : result.getCounting_in_3s_4s_6s_7s_8s_9s_missing_numbers()) +
                                            Integer.parseInt(result.getAdd_two_and_three_digit_numbers() == null || result.getAdd_two_and_three_digit_numbers().equals("Null") ? "0" : result.getAdd_two_and_three_digit_numbers()) +
                                            Integer.parseInt(result.getSubtract_2_digits_from_2_digits() == null || result.getSubtract_2_digits_from_2_digits().equals("Null") ? "0" : result.getSubtract_2_digits_from_2_digits()) +
                                            Integer.parseInt(result.getMultiply_2_digit_by_2_digit_numbers_to_20() == null || result.getMultiply_2_digit_by_2_digit_numbers_to_20().equals("Null") ? "0" : result.getMultiply_2_digit_by_2_digit_numbers_to_20()) +
                                            Integer.parseInt(result.getDivide_2_digit_by_1_digit_numbers_to_99() == null || result.getDivide_2_digit_by_1_digit_numbers_to_99().equals("Null") ? "0" : result.getDivide_2_digit_by_1_digit_numbers_to_99()) +
                                            Integer.parseInt(result.getIdentify_number_to_9999999() == null || result.getIdentify_number_to_9999999().equals("Null") ? "0" : result.getIdentify_number_to_9999999()) +
                                            Integer.parseInt(result.getIdentify_greatest_number_to_9999() == null || result.getIdentify_greatest_number_to_9999().equals("Null") ? "0" : result.getIdentify_greatest_number_to_9999()) +
                                            Integer.parseInt(result.getCounting_in_various_missing_numbers() == null || result.getCounting_in_various_missing_numbers().equals("Null") ? "0" : result.getCounting_in_various_missing_numbers()) +
                                            Integer.parseInt(result.getAdd_two_three_digit_numbers_including_deci() == null || result.getAdd_two_three_digit_numbers_including_deci().equals("Null") ? "0" : result.getAdd_two_three_digit_numbers_including_deci()) +
                                            Integer.parseInt(result.getSubtract_2_digit_from_3_digits_including_deci() == null || result.getSubtract_2_digit_from_3_digits_including_deci().equals("Null") ? "0" : result.getSubtract_2_digit_from_3_digits_including_deci()) +
                                            Integer.parseInt(result.getMultiply_2_digit_by_2_digit_numbers_over_20() == null || result.getMultiply_2_digit_by_2_digit_numbers_over_20().equals("Null") ? "0" : result.getMultiply_2_digit_by_2_digit_numbers_over_20()) +
                                            Integer.parseInt(result.getDivide_2_and_3_digit_by_2_digit_numbers() == null || result.getDivide_2_and_3_digit_by_2_digit_numbers().equals("Null") ? "0" : result.getDivide_2_and_3_digit_by_2_digit_numbers());
                            tcNumbersTotal += mth;
                            numbersTotal += mth;

                            int wp = Integer.parseInt(result.getWp_addition()) + Integer.parseInt(result.getWp_subtraction()) + Integer.parseInt(result.getWp_multiplication()) + Integer.parseInt(result.getWp_division());
                            tcWordsTotal += wp;
                            wordProblemsTotal += wp;
                        }
                    }
                }
            }

            float completed = (((float) tcTotalNumber) / ((float) tcStudents.size()) * 100);
            valueSet1.add(new BarEntry(completed, i));
            BarDataSet barDataSet1 = new BarDataSet(valueSet1, schoolName);
            barDataSet1.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSets.add(barDataSet1);

            valueSet2.add(new BarEntry(tcPhonicsTotal / tcTotalNumber, i));
            BarDataSet barDataSet2 = new BarDataSet(valueSet2, schoolName);
            barDataSet2.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSets2.add(barDataSet2);

            valueSet3.add(new BarEntry(tcCompTotal / tcTotalNumber, i));
            BarDataSet barDataSet3 = new BarDataSet(valueSet3, schoolName);
            barDataSet3.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSets3.add(barDataSet3);

            valueSet4.add(new BarEntry((tcPhonicsTotal + tcCompTotal) / tcTotalNumber, i));
            BarDataSet barDataSet4 = new BarDataSet(valueSet4, schoolName);
            barDataSet4.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSets4.add(barDataSet4);

            float mathsCompleted = (((float) tcMathsTotalNumber) / ((float) tcStudents.size()) * 100);
            valueSet5.add(new BarEntry(mathsCompleted, i));
            BarDataSet barDataSet5 = new BarDataSet(valueSet5, schoolName);
            barDataSet5.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSets5.add(barDataSet5);

            valueSet6.add(new BarEntry(tcNumbersTotal / tcMathsTotalNumber, i));
            BarDataSet barDataSet6 = new BarDataSet(valueSet6, schoolName);
            barDataSet6.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSets6.add(barDataSet6);

            valueSet7.add(new BarEntry(tcWordsTotal / tcMathsTotalNumber, i));
            BarDataSet barDataSet7 = new BarDataSet(valueSet7, schoolName);
            barDataSet7.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSets7.add(barDataSet7);

            valueSet8.add(new BarEntry((tcNumbersTotal + tcWordsTotal) / tcMathsTotalNumber, i));
            BarDataSet barDataSet8 = new BarDataSet(valueSet8, schoolName);
            barDataSet8.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSets8.add(barDataSet8);

            i++;
        }

        BarData data = new BarData(schools, dataSets);
        chart1.setData(data);
        chart1.animateXY(2000, 2000);
        chart1.setDescription("% submitted");    // Hide the description
        chart1.getAxisRight().setDrawLabels(false);
        chart1.getLegend().setEnabled(false);   // Hide the legend
        chart1.invalidate();

        BarData data2 = new BarData(schools, dataSets2);
        chart2.setData(data2);
        chart2.animateXY(2000, 2000);
        chart2.setDescription("");    // Hide the description
        chart2.getAxisRight().setDrawLabels(false);
        chart2.getLegend().setEnabled(false);   // Hide the legend
        chart2.invalidate();

        BarData data3 = new BarData(schools, dataSets3);
        chart3.setData(data3);
        chart3.animateXY(2000, 2000);
        chart3.setDescription("");    // Hide the description
        chart3.getAxisRight().setDrawLabels(false);
        chart3.getLegend().setEnabled(false);   // Hide the legend
        chart3.invalidate();

        BarData data4 = new BarData(schools, dataSets4);
        chart4.setData(data4);
        chart4.animateXY(2000, 2000);
        chart4.setDescription("");    // Hide the description
        chart4.getAxisRight().setDrawLabels(false);
        chart4.getLegend().setEnabled(false);   // Hide the legend
        chart4.invalidate();

        // BarData data5 = new BarData(schools, dataSets5);
        BarData data5 = new BarData(schools, dataSets5);
        chart1m.setData(data5);
        chart1m.animateXY(2000, 2000);
        chart1m.setDescription("% submitted");    // Hide the description
        chart1m.getAxisRight().setDrawLabels(false);
        chart1m.getLegend().setEnabled(false);   // Hide the legend
        chart1m.invalidate();

        BarData data6 = new BarData(schools, dataSets6);
        chart2m.setData(data6);
        chart2m.animateXY(2000, 2000);
        chart2m.setDescription("");    // Hide the description
        chart2m.getAxisRight().setDrawLabels(false);
        chart2m.getLegend().setEnabled(false);   // Hide the legend
        chart2m.invalidate();

        BarData data7 = new BarData(schools, dataSets7);
        chart3m.setData(data7);
        chart3m.animateXY(2000, 2000);
        chart3m.setDescription("");    // Hide the description
        chart3m.getAxisRight().setDrawLabels(false);
        chart3m.getLegend().setEnabled(false);   // Hide the legend
        chart3m.invalidate();

        BarData data8 = new BarData(schools, dataSets8);
        chart4m.setData(data8);
        chart4m.animateXY(2000, 2000);
        chart4m.setDescription("");    // Hide the description
        chart4m.getAxisRight().setDrawLabels(false);
        chart4m.getLegend().setEnabled(false);   // Hide the legend
        chart4m.invalidate();

        if (students != null) {
            scoresCompleted.setText(totalNumber + "/" + students.length + " (" + df.format(((float) totalNumber) / students.length * 100) + "%)");
            phonicsAvg.setText(df.format(phonicsTotal / totalNumber) + "/27");
            compAvg.setText(df.format(compTotal / totalNumber) + "/15");
            overallAvg.setText(df.format((phonicsTotal + compTotal) / totalNumber) + "/42");

            mathsScoresCompleted.setText(mathsTotalNumber + "/" + students.length + " (" + df.format(((float) mathsTotalNumber) / students.length * 100) + "%)");
            numbersAvg.setText(df.format(numbersTotal / mathsTotalNumber) + "/97");
            wordProblemsAvg.setText(df.format(wordProblemsTotal / mathsTotalNumber) + "/8");
            overallMathsAvg.setText(df.format((numbersTotal + wordProblemsTotal) / mathsTotalNumber) + "/105");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        adminBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    static class AdminClassesAdapter extends ArrayAdapter<BackendlessUser> {
        public AdminClassesAdapter(Context context, List<BackendlessUser> objects) {
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
            String nameshort = "" + getItem(position).getProperty("name");
//            if (nameshort.length() >= 3)
//                nameshort = getItem(position).getProperty("name").toString().substring(getItem(position).getProperty("name").toString().length() - 3);
            if (position == 0) {
                tvText1.setText("" + getItem(position).getProperty("name"));
            } else {
                tvText1.setText(getItem(position).getProperty("school") + " / " + getItem(position).getProperty("class"));
            }
            return convertView;
        }
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
}