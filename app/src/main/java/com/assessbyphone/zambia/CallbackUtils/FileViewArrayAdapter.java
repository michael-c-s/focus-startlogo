package com.assessbyphone.zambia.CallbackUtils;

import android.content.Context;
import android.text.Spannable;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import com.assessbyphone.zambia.R;

/********************************************************************
 * class FileViewArrayAdapter
 * 		Extended Array adapter to get custom views on the list of 
 *      files for the file browser and recently opened lists */
public class FileViewArrayAdapter extends ArrayAdapter<String> {
    public static final int RECENT_FILE_MODE = 1;
    public static final int FILE_BROWSER_MODE = 2;
    private final Spannable.Factory sf = new Spannable.Factory();
    private final LayoutInflater factory = LayoutInflater.from(getContext());

    protected int mode = 0;

    // just a basic constructor
    public FileViewArrayAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        mode = RECENT_FILE_MODE;
    } // end constructor one

    public FileViewArrayAdapter(Context context, List<String> objects) {
        // layout is 0 because we do it ourselves below
        // android.R.layout.select_dialog_singlechoice
        // would be the one to use normally.
        super(context, 0, objects);
        mode = RECENT_FILE_MODE;
    } // end constructor two

    public FileViewArrayAdapter(Context context) {
        // layout is 0 because we do it ourselves below
        // android.R.layout.select_dialog_singlechoice
        // would be the one to use normally.
        super(context, 0);
        mode = RECENT_FILE_MODE;
    } // end constructor two

    /****************************************************************
     * getFileExtension */
    public static String getFileExtension(String f) {
        String ext = "";
        int i = f.lastIndexOf('.');
        if (i > 0 && i < f.length() - 1)
            ext = f.substring(i + 1).toLowerCase();

        return ext;
    } // getFileExtension()

    public static String formatFileSize(File file) {
        long size = file.length();

        if (file.isDirectory())
            return "";

        if (size < 0)
            return "\n0";
        else if (size == 1)
            return "\n1 Byte";
        else if (size < 2048)
            return "\n" + size + " Bytes";
        else if (size < 1024 * 1024 * 2)
            return "\n" + ((int) (size / 1024)) + " KB";
        else
            return "\n" + Math.round(100.0 * size / (1024 * 1024)) / 100.0 + " MB";
    }

    public void setMode(int m) {
        mode = m;
    }

    /****************************************************************
     * getView
     * 		the overridden getView method */
    public View getView(int position, View convertView, ViewGroup parent) {
        // update the text accordingly
        File f = new File(getItem(position));
        int imageid;

        if (f.isDirectory())
            imageid = R.drawable.fileicon_folder;
        else {

            String ext = getFileExtension(getItem(position));

            switch (ext) {
                case "c":
                    imageid = R.drawable.fileicon_c;
                    break;
                case "cpp":
                    imageid = R.drawable.fileicon_cpp;
                    break;
                case "f":
                    imageid = R.drawable.fileicon_f;
                    break;
                case "h":
                    imageid = R.drawable.fileicon_h;
                    break;
                case "htm":
                    imageid = R.drawable.fileicon_htm;
                    break;
                case "html":
                    imageid = R.drawable.fileicon_html;
                    break;
                case "java":
                    imageid = R.drawable.fileicon_java;
                    break;
                case "pl":
                    imageid = R.drawable.fileicon_pl;
                    break;
                case "py":
                    imageid = R.drawable.fileicon_py;
                    break;
                case "tex":
                    imageid = R.drawable.fileicon_tex;
                    break;
                case "txt":
                    imageid = R.drawable.fileicon_txt;
                    break;
                default:
                    imageid = R.drawable.fileicon_default;
                    break;
            }
        }

        // This really speeds things up. Because only the number of views displayed at one time
        // are actually inflated
        View textEntryView;
        if (convertView != null)
            textEntryView = convertView;
        else
            textEntryView = factory.inflate(R.layout.filebrowser_item, null);

        TextView tv = (TextView) textEntryView.findViewById(R.id.itemtext);
        tv.setText(getItem(position));

        ImageView iv = (ImageView) textEntryView.findViewById(R.id.itemimage);
        iv.setImageResource(imageid);

        if (mode == FILE_BROWSER_MODE) {
            Spannable text = sf.newSpannable(f.getName() + formatFileSize(f));
            text.setSpan(new AbsoluteSizeSpan(20), 0, f.getName().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            tv.setText(text);
        } else {    // default to RECENT_FILE_MODE
            Spannable text;
            if (f.getParent().equals("/"))
                text = sf.newSpannable(f.getName() + "\n/");
            else
                text = sf.newSpannable(f.getName() + "\n" + f.getParent() + "/");

            text.setSpan(new AbsoluteSizeSpan(20), 0, f.getName().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            tv.setText(text);
        }

        return textEntryView;

    } // end getView()

} // end class OpenRecentAdapter