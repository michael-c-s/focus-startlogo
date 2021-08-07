package com.assessbyphone.zambia.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.assessbyphone.zambia.CallbackUtils.CustomProgressBar;
import com.assessbyphone.zambia.CallbackUtils.Defaults;
import com.assessbyphone.zambia.CallbackUtils.pmTextEdit;
import com.assessbyphone.zambia.ImageLibrary.SmartImageView;
import com.assessbyphone.zambia.Models.MasterCSVModel;
import com.assessbyphone.zambia.R;
import com.zj.btsdk.PrintPic;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

public class MenuActivity extends Activity {
    public static String TAG = "PhonicsByPhone";
    public static String sStory = "";
    public static boolean isAcknowledgement = false;
    final List<TextView> textViews = new ArrayList<TextView>();
    public ArrayList<ArrayList<ArrayList<String>>> textBanks = new ArrayList<ArrayList<ArrayList<String>>>();
    Button btnPrint;
    HorizontalScrollView scrollView;
    LinearLayout scrollViewLL;
    Bitmap banksBitmap = null, banksBitmap2 = null;
    SeekBar mFontSizeChanger;
    int progressChanged = 0;
    SeekBar mWidthChanger;
    int widthSeekProgress = 0;
    List<LinearLayout> adjustableWidthFrameLinearLayouts = new ArrayList<LinearLayout>();
    MasterCSVModel.AgeGroup ageGroup = MasterCSVModel.AgeGroup.NotSpecified;
    private CustomProgressBar customProgressBar;
    private ImageView quizBack;
    private String assessmentName = "";
    private LinearLayout adjCompWidthsLay;
    private File myFile;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("", Context.MODE_PRIVATE);
        myFile = new File(directory, "master.csv");

        adjCompWidthsLay = findViewById(R.id.adjCompWidthsLay_ids);

        assessmentName = getIntent().getStringExtra("assessmentName");

        customProgressBar = new CustomProgressBar();
    }

    @Override
    public void onStart() {
        super.onStart();


        if (assessmentName != null && assessmentName.equalsIgnoreCase("Phonics Check"))
            adjCompWidthsLay.setVisibility(View.GONE);
        else
            adjCompWidthsLay.setVisibility(View.VISIBLE);

        if (GetMasterTemplateActivity.sTemplate == null) {
            finish();
            return;
        }

        quizBack = (ImageView) findViewById(R.id.quizBack_ids);
        mFontSizeChanger = (SeekBar) findViewById(R.id.seekBar);

        quizBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mFontSizeChanger.setProgress(101);

        mFontSizeChanger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean first = true;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (first) {
                    first = false;
                    return;
                }

                progressChanged = progress;
                for (TextView tv : textViews) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, progress / 2);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
//				Toast.makeText(MenuActivity.this,"seek bar progress:"+progressChanged,
//						Toast.LENGTH_LONG).show();
            }
        });

        Button recordResultsBtn = (Button) this.findViewById(R.id.btnRecordResults);
        if (!GetMasterTemplateActivity.sTemplate.isStory) {
            recordResultsBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent myIntent = new Intent(MenuActivity.this, LoginActivity.class);
                    myIntent.putExtra("ageGroup", ageGroup);
//					finish();
                    startActivity(myIntent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });
        } else {
            recordResultsBtn.setVisibility(View.GONE);
        }

        Button rerandomizeBtn = (Button) this.findViewById(R.id.btnRerandomize);
        if (!GetMasterTemplateActivity.sTemplate.isStory) {
            rerandomizeBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent myIntent = new Intent(MenuActivity.this, GetBanksActivity.class);
                    finish();
                    startActivity(myIntent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            });
        } else {
            rerandomizeBtn.setVisibility(View.GONE);
        }

        Button doOtherAss = (Button) findViewById(R.id.btnOtherAssessment);
        if (GetMasterTemplateActivity.sTemplate.isStory) {
            doOtherAss.setText("Choose another assessment/story");
        }
        doOtherAss.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MenuActivity.this, GetMasterTemplateActivity.class);
                if (ConnectBluetoothPrinterActivity.mService != null) {
                    ConnectBluetoothPrinterActivity.mService.stop();
                    ConnectBluetoothPrinterActivity.mService = null;
                }
                finish();
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);
        scrollViewLL = (LinearLayout) findViewById(R.id.scrollViewLL);

        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        Typeface custom_font;

        LinearLayout ll = null;
        final LinearLayout lls = (LinearLayout) li.inflate(R.layout.bank_view_stories, null);
        boolean hasComprehensions = false;
        boolean hasMath = false;

        if (GetMasterTemplateActivity.sTemplate.isStory) {
            custom_font = Typeface.createFromAsset(getAssets(), "ABeeZee-Regular.ttf");
            hasComprehensions = false;
            hasMath = false;

            ((TextView) lls.findViewWithTag("t1")).setTypeface(custom_font);
            ((TextView) lls.findViewWithTag("t2")).setTypeface(custom_font);
            ((TextView) lls.findViewWithTag("t3")).setTypeface(custom_font);
            scrollViewLL.addView(lls);

            final Spinner pageSpinner = (Spinner) findViewById(R.id.page);
            final FrameLayout language1Lay = (FrameLayout) findViewById(R.id.language1Lay_ids);
            final Spinner language1spinner = (Spinner) findViewById(R.id.language1);
            final Spinner language2spinner = (Spinner) findViewById(R.id.language2);

            final ArrayAdapter<String> dataAdapter1, dataAdapter2, pageAdapator;

            final TextView t1 = ((TextView) lls.findViewWithTag("t1"));
            textViews.add(t1);
            final TextView t2 = ((TextView) lls.findViewWithTag("t2"));
            textViews.add(t2);
            final TextView t3 = ((TextView) lls.findViewWithTag("t3"));
            textViews.add(t3);

            //
            // Populate language and page spinners
            //

            pageSpinner.setVisibility(View.VISIBLE);
            ArrayList<String> pagesList = new ArrayList<String>(GetMasterTemplateActivity.sTemplate.frames.get(0).dataBankColumnNames);
            pagesList.add("* Acknowledgements");
            pagesList.add("* Company Logos");
            pageAdapator = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pagesList);
            pageAdapator.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            pageSpinner.setAdapter(pageAdapator);

            //language1spinner.setVisibility(View.VISIBLE);
            language1Lay.setVisibility(View.VISIBLE);

            List<String> list = new ArrayList<String>();
            list.add(0, "Language 1");
            for (int j = 0; j < GetMasterTemplateActivity.sTemplate.frames.size(); j++) {
                list.add(GetMasterTemplateActivity.sTemplate.frames.get(j).question);
            }

            dataAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            language1spinner.setAdapter(dataAdapter1);
            language1spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    String str = parent.getItemAtPosition(pos).toString();

                    if (!sStory.equals("")) // from texteditor
                    {
                        setText(t3, sStory, false);
                        t1.setVisibility(View.GONE);
                        t2.setVisibility(View.GONE);
                        t3.setVisibility(View.VISIBLE);
                        if (isAcknowledgement) {
                            // smaller font for acknowledgement
                            t3.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.tfont_size_ack));
                            isAcknowledgement = false;
                        }
                        sStory = "";
                        return;
                    }

                    int pos2 = pageSpinner.getSelectedItemPosition();

                    if (GetMasterTemplateActivity.sTemplate.frames.size() >= 2) { // 2 or more languages?
                        if (language1spinner.getSelectedItem().toString().equals(("Language 1")) || language2spinner.getSelectedItem().toString().equals(("Language 2"))) {
                            t1.setVisibility(View.GONE);
                            t2.setVisibility(View.GONE);
                            t3.setVisibility(View.VISIBLE);
                            if (!"Language 1".equals(str)) {
                                t1.setText(GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2)); // smaller font when showing two languages
                                setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2), false); // bigger font when only showing one language
                            } else if ((language2spinner.getSelectedItemPosition() - 1) >= 1) {
                                setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(language2spinner.getSelectedItemPosition() - 1).data.get(pos2), false);
                            }
                        } else {
                            t1.setVisibility(View.VISIBLE);
                            t2.setVisibility(View.VISIBLE);
                            t3.setVisibility(View.GONE);
                            if (!"Language 1".equals(str)) {
                                t1.setText(GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2)); // smaller font when showing two languages
                                setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2), false); // bigger font when only showing one language
                            } else if ((language2spinner.getSelectedItemPosition() - 1) >= 1) {
                                setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(language2spinner.getSelectedItemPosition() - 1).data.get(pos2), false);
                            }
                        }
                    } else {
                        t1.setVisibility(View.GONE);
                        t2.setVisibility(View.GONE);
                        t3.setVisibility(View.VISIBLE);
                        if (!"Language 1".equals(str)) {
                            t1.setText(GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2)); // smaller font when showing two languages
                            setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2), false); // bigger font when only showing one language
                        }
                    }

                    if (!sStory.equals("")) // from texteditor
                    {
                        t1.setText(sStory);
                        sStory = "";
                    }

//					Toast.makeText(parent.getContext(),
//							"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
//							Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            if (GetMasterTemplateActivity.sTemplate.frames.size() >= 2) { // 2 or more languages?
                language2spinner.setVisibility(View.VISIBLE);

                List<String> list2 = new ArrayList<String>(list);

                list2.set(0, "Language 2");
                dataAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list2);
                dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                language2spinner.setAdapter(dataAdapter2);
                language2spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        String str = parent.getItemAtPosition(pos).toString();

//						if ("Language 2".equals(str)) {
//							t2.setText("");
//							return;
//						}

                        int pos2 = pageSpinner.getSelectedItemPosition();

                        if (language1spinner.getSelectedItem().toString().equals(("Language 1")) || language2spinner.getSelectedItem().toString().equals(("Language 2"))) {
                            t1.setVisibility(View.GONE);
                            t2.setVisibility(View.GONE);
                            t3.setVisibility(View.VISIBLE);
                            if (!"Language 2".equals(str)) {
                                t2.setText(GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2)); // smaller font when showing two languages
                                setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2), false); // bigger font when only showing one language
                            } else if ((language1spinner.getSelectedItemPosition() - 1) >= 1) {
                                setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(language1spinner.getSelectedItemPosition() - 1).data.get(pos2), false);
                            }
                        } else {
                            t1.setVisibility(View.VISIBLE);
                            t2.setVisibility(View.VISIBLE);
                            t3.setVisibility(View.GONE);
                            if (!"Language 2".equals(str)) {
                                t2.setText(GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2)); // smaller font when showing two languages
                                setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(pos - 1).data.get(pos2), false); // bigger font when only showing one language
                            }
                        }

//						Toast.makeText(parent.getContext(),
//								"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
//								Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            pageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    lls.findViewById(R.id.stories).setVisibility(View.VISIBLE);
                    lls.findViewById(R.id.logosLL).setVisibility(View.GONE);
                    lls.findViewById(R.id.logosLL2).setVisibility(View.GONE);

                    if ("* Acknowledgements".equals(pageSpinner.getSelectedItem().toString())) {
							/*
							This Big Book was translated into [eg Mampruli - or Mampruli & English] by a local teacher, [Abdul Razak] of [name of his village], working with PhonicsGhana and the Ghana Education Service. Software by FUNX. Date: [Feb 2016]
							 */
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat fullMonthFormat = new SimpleDateFormat("MMMM, yyyy");
                        String date = fullMonthFormat.format(Calendar.getInstance().getTime());
                        sStory = "This Big Book was translated into [LANGUAGE1 and LANGUAGE2] by a local teacher, [INSERT YOUR NAME] of [INSERT YOUR VILLAGE]," +
                                " working with PhonicsGhana and the Ghana Education Service. Software by FUNX. Date: " + date;
                        isAcknowledgement = true;
                        editor();
                        return;
                    } else if ("* Company Logos".equals(pageSpinner.getSelectedItem().toString())) {
                        lls.findViewById(R.id.stories).setVisibility(View.GONE);
                        lls.findViewById(R.id.logosLL).setVisibility(View.VISIBLE);
                        lls.findViewById(R.id.logosLL2).setVisibility(View.VISIBLE);
                        return;
                    }

                    String str = language1spinner.getSelectedItem().toString();
                    if (!"Language 1".equals(str)) {
                        t1.setText(GetMasterTemplateActivity.sTemplate.frames.get(language1spinner.getSelectedItemPosition() - 1).data.get(pageSpinner.getSelectedItemPosition()));
                        setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(language1spinner.getSelectedItemPosition() - 1).data.get(pageSpinner.getSelectedItemPosition()), false);
                    }
                    if (GetMasterTemplateActivity.sTemplate.frames.size() >= 2
                            && !"Language 2".equals(language2spinner.getSelectedItem().toString())) {
                        t2.setText(GetMasterTemplateActivity.sTemplate.frames.get(language2spinner.getSelectedItemPosition() - 1).data.get(pageSpinner.getSelectedItemPosition()));
                        setText(t3, GetMasterTemplateActivity.sTemplate.frames.get(language2spinner.getSelectedItemPosition() - 1).data.get(pageSpinner.getSelectedItemPosition()), false);
                    }


//					Toast.makeText(parent.getContext(),
//							"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
//							Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else
            custom_font = Typeface.createFromAsset(getAssets(), "ABeeZee-Regular.ttf");

        String assessmentName = GetMasterTemplateActivity.sTemplate.frames.get(0).assessmentName;
        // Select age group based on assessment name
        if (assessmentName.toLowerCase().contains("age 5 to 6")) {
            ageGroup = MasterCSVModel.AgeGroup.a56;
        } else if (assessmentName.toLowerCase().contains("age 6 to 7")) {
            ageGroup = MasterCSVModel.AgeGroup.a67;
        } else if (assessmentName.toLowerCase().contains("age 7 to 8")) {
            ageGroup = MasterCSVModel.AgeGroup.a78;
        } else if (assessmentName.toLowerCase().contains("age 8 to 9")) {
            ageGroup = MasterCSVModel.AgeGroup.a89;
        } else if (assessmentName.toLowerCase().contains("age 9 to 10")) {
            ageGroup = MasterCSVModel.AgeGroup.a910;
        } else if (assessmentName.toLowerCase().contains("age 10 to 11")) {
            ageGroup = MasterCSVModel.AgeGroup.a1011;
        }
        // extended for math assessments - basic, intermediate, advanced
        else if (assessmentName.toLowerCase().contains("basic")) {
            ageGroup = MasterCSVModel.AgeGroup.Basic;
        } else if (assessmentName.toLowerCase().contains("intermediate")) {
            ageGroup = MasterCSVModel.AgeGroup.Intermediate;
        } else if (assessmentName.toLowerCase().contains("advanced")) {
            ageGroup = MasterCSVModel.AgeGroup.Advanced;
        }

        adjustableWidthFrameLinearLayouts.clear();

        if (!GetMasterTemplateActivity.sTemplate.isStory) { // Frames will hold story translations
            for (int j = 0; j < GetMasterTemplateActivity.sTemplate.frames.size(); j++) {
                MasterCSVModel.OneFrame frame = GetMasterTemplateActivity.sTemplate.frames.get(j);
                if (frame.question.equals(""))
                    continue;

                if (frame.isComprehension) {
                    hasComprehensions = true;
                }

                if (frame.isMath) {
                    hasMath = true;
                }

                ll = (LinearLayout) li.inflate(R.layout.bank_view, null);
                ((TextView) ll.findViewWithTag("t1")).setTypeface(custom_font);
                ((TextView) ll.findViewWithTag("t1")).setText(frame.question);
                textViews.add((TextView) ll.findViewWithTag("t1"));

                int i = 0;

                if (!frame.isImage && !frame.isComprehension) {
                    for (String data : frame.data) {
                        i++;
                        TextView tv = (TextView) ll.findViewWithTag("t" + (i + 1));
                        textViews.add(tv);
                        if (tv != null) {
                            tv.setTypeface(custom_font);
                            tv.setText((frame.isMath ? "" : /*i + ". "*/"") + data);
                        }
                    }
                    scrollViewLL.addView(ll);
                    adjustableWidthFrameLinearLayouts.add(ll);
                } else if (frame.isImage) {
                    // Load image(s) and cache it
                    if (frame.data.size() == 1) {
                        ll = (LinearLayout) li.inflate(R.layout.image_view, null);
                    } else {
                        ll = (LinearLayout) li.inflate(R.layout.image_view_multiple, null);
                    }
                    ((TextView) ll.findViewWithTag("t1")).setTypeface(custom_font);
                    ((TextView) ll.findViewWithTag("t1")).setText(frame.question);
                    textViews.add((TextView) ll.findViewWithTag("t1"));
                    SmartImageView siv = (SmartImageView) ll.findViewById(R.id.i1);
                    siv.setImageUrl(Defaults.getBaseUrl(getApplicationContext()) + "images/" + frame.data.get(0));
                    if (frame.data.size() >= 2) {
                        siv = (SmartImageView) ll.findViewById(R.id.i2);
                        siv.setImageUrl(Defaults.getBaseUrl(getApplicationContext()) + "images/" + frame.data.get(1));
                    }
                    if (frame.data.size() >= 3) {
                        siv = (SmartImageView) ll.findViewById(R.id.i3);
                        siv.setImageUrl(Defaults.getBaseUrl(getApplicationContext()) + "images/" + frame.data.get(2));
                    }
                    if (frame.data.size() >= 4) {
                        siv = (SmartImageView) ll.findViewById(R.id.i4);
                        siv.setImageUrl(Defaults.getBaseUrl(getApplicationContext()) + "images/" + frame.data.get(3));
                    }
                    scrollViewLL.addView(ll);
                } else if (frame.isComprehension) {
                    // Comprehension text
                    ll = (LinearLayout) li.inflate(R.layout.bank_view_questions, null);
                    ((TextView) ll.findViewWithTag("t1")).setTypeface(custom_font);
                    ((TextView) ll.findViewWithTag("t1")).setText(frame.question);
                    textViews.add((TextView) ll.findViewWithTag("t1"));

                    TextView tv = (TextView) ll.findViewWithTag("t2");
                    tv.setTypeface(custom_font);
                    tv.setText(frame.data.get(0));
                    textViews.add(tv);
                    scrollViewLL.addView(ll);
                    adjustableWidthFrameLinearLayouts.add(ll);

                    // Questions
                    frame = GetMasterTemplateActivity.sTemplate.frames.get(++j);
                    ll = (LinearLayout) li.inflate(R.layout.bank_view_questions, null);
                    ((TextView) ll.findViewWithTag("t1")).setTypeface(custom_font);
                    ((TextView) ll.findViewWithTag("t1")).setText(frame.question);
                    textViews.add((TextView) ll.findViewWithTag("t1"));
                    for (String data : frame.data) {
                        i++;
                        TextView tv2 = (TextView) ll.findViewWithTag("t" + (i + 1));
                        textViews.add(tv2);
                        tv2.setTypeface(custom_font);
                        tv2.setText((frame.isMath ? "" : /*i + ". "*/"") + data);
                    }
                    scrollViewLL.addView(ll);
                    adjustableWidthFrameLinearLayouts.add(ll);
                }
            }
        }


        btnPrint = (Button) this.findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(new ClickEvent());

        final LinearLayout llz = ll;

        if (hasComprehensions || hasMath) {
            findViewById(R.id.comprehensionWidthText).setVisibility(View.VISIBLE);
            if (hasMath) {
                ((TextView) findViewById(R.id.comprehensionWidthText)).setText("Adjust maths problem widths:");
            }
            findViewById(R.id.seekBar2).setVisibility(View.VISIBLE);
            findViewById(R.id.fontWidthText).setVisibility(View.VISIBLE);

            final ViewGroup.LayoutParams params = llz.getLayoutParams();
            mWidthChanger = (SeekBar) findViewById(R.id.seekBar2);
            mWidthChanger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                boolean first = true;

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (first) {
                        first = false;
                        return;
                    }

                    widthSeekProgress = progress / 2;
                    final float scale = getResources().getDisplayMetrics().density;
                    int pixels = (int) (widthSeekProgress * scale + 0.5f);
                    params.width = pixels;
                    llz.setLayoutParams(params);


                    for (LinearLayout ll : adjustableWidthFrameLinearLayouts) {
                        for (int i = 0; i < ll.getChildCount(); i++) {
                            View v = ll.getChildAt(i);
                            if (v instanceof TextView) {
                                v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.FILL_PARENT));
                            }
                        }

                        ViewGroup.LayoutParams params = ll.getLayoutParams();
                        pixels = (int) (widthSeekProgress * scale + 0.5f);
                        params.width = pixels;
                        ll.setLayoutParams(params);
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
//				Toast.makeText(MenuActivity.this,"seek bar progress:"+widthSeekProgress,
//						Toast.LENGTH_LONG).show();
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
//				Toast.makeText(MenuActivity.this,"seek bar progress:"+widthSeekProgress,
//						Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        for (TextView tv : textViews) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 56);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void printBanks() {
        new PrintBanksTask(getApplicationContext()).execute();
    }

    @Override
    public void onBackPressed() {
        if (ConnectBluetoothPrinterActivity.mService != null)
            ConnectBluetoothPrinterActivity.mService.stop();
        ConnectBluetoothPrinterActivity.mService = null;
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //editor();

        return true;
    }

    void editor() {
        if (GetMasterTemplateActivity.sTemplate != null && GetMasterTemplateActivity.sTemplate.isStory) {
            scrollViewLL.removeAllViews();
            Intent i = new Intent(this, pmTextEdit.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        editor();
        return super.onMenuOpened(featureId, menu);
    }

    public void setText(TextView tv, String text, boolean addNewLines) {
        if (tv == null || text == null || "".equals(text)) {
            return;
        }

        StringBuilder newString = new StringBuilder();
        if (!addNewLines) {
            newString = new StringBuilder(text);
        } else {
            String[] tokedStr = addLinebreaks(text, 36);
            int numLines = tokedStr.length;
            int maxLen = 0;
            for (String s : tokedStr) {
                if (s.length() > maxLen)
                    maxLen = s.length();
                if (s.equals("") || s.equals("\n"))
                    continue;
                newString.append(s).append("\n");
            }

            if (numLines >= 4) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.tfont_size_story4lines));
                mFontSizeChanger.setMax(((int) getResources().getDimension(R.dimen.tfont_size_story4lines)) * 2);
            } else {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.tfont_size_story2or3lines));
                mFontSizeChanger.setMax(((int) getResources().getDimension(R.dimen.tfont_size_story2or3lines)) * 2);
            }

            newString = new StringBuilder(newString.substring(0, newString.lastIndexOf("\n"))); // remove last new line char
        }

        tv.setText(newString.toString());
    }

    public String[] addLinebreaks(String input, int maxLineLength) {
        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();

            if (lineLen + word.length() > maxLineLength) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word).append(" ");
            lineLen += word.length();
        }
        return output.toString().split("\n");
    }

    class ClickEvent implements OnClickListener {
        public void onClick(View v) {
            if (v == btnPrint) {
                printBanks();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class PrintBanksTask extends AsyncTask {
        public PrintBanksTask(Context context) {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            customProgressBar.showMe(MenuActivity.this, "Please Wait...");
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (result != null && result.equals("error")) {
                errorFailedPrinting();
            }

            customProgressBar.dismissMe();
        }

        @Override
        protected Object doInBackground(Object... params) {
            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            File dir = new File(file_path);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, "assessment.png");

            // Get bitmap from linear layout
            int viewWidth = scrollViewLL.getWidth();
            int viewHeight = scrollViewLL.getHeight();
            banksBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(banksBitmap);
            scrollViewLL.draw(c);

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
            Log.d(TAG, "Print image in one chunk: " + viewWidth);

            String result = null;

            if (!doPrint(file, viewWidth, viewHeight)) {
                result = "error";
            }
            //}
            return result;
        }

        private void errorFailedPrinting() {
            new AlertDialog.Builder(MenuActivity.this)
                    .setTitle("Error")
                    .setMessage("Error printing. Perhaps the chunk is too long?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
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
                RectF targetRect = new RectF(0, topPadding, banksBitmap.getWidth(), banksBitmap.getHeight());
                Bitmap paddedBitmap = Bitmap.createBitmap(banksBitmap.getWidth() + rightPadding, banksBitmap.getHeight(), banksBitmap.getConfig());
                Canvas canvas = new Canvas(paddedBitmap);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(banksBitmap, null, targetRect, null);
                banksBitmap.recycle();

                // Rotate
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(paddedBitmap, 0, 0, paddedBitmap.getWidth(), paddedBitmap.getHeight(), matrix, true);
                paddedBitmap.recycle();

                // Convert to B&W
                int newWidth = rotatedBitmap.getWidth();
                int newHeight = rotatedBitmap.getHeight();
                Bitmap bmpMonochrome = Bitmap.createBitmap((int) newWidth, newHeight, Bitmap.Config.ARGB_8888);
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
                            bmpMonochrome.setPixel(x, y, Color.BLACK);
                        else
                            bmpMonochrome.setPixel(x, y, Color.WHITE);
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
                        MenuActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MenuActivity.this, "Bluetooth is not ready. Restart app.", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MenuActivity.this, "Bluetooth is not ready. Restart app.", Toast.LENGTH_LONG).show();
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