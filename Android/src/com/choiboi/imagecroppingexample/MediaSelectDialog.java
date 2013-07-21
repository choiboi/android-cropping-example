package com.choiboi.imagecroppingexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MediaSelectDialog extends Activity {
    
    public static final String CAMERA = "Camera";
    public static final String GALLERY = "Gallery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.select_dialog);

        // Set result CANCELED in-case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Setup array with programs
        String[] media = new String[] { CAMERA, GALLERY };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.select_dialog_textview_layout, media);

        // Setup the title of the dialog.
        TextView textView = (TextView) findViewById(R.id.select_title_text);
        textView.setText(getResources().getText(R.string.msd_select_media_text));

        // Setup dialog to display list of programs.
        ListView listView = (ListView) findViewById(R.id.select_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mediaSelected = ((TextView) view).getText().toString();

                // Create result Intent and include the name of media.
                Intent intent = new Intent();
                intent.putExtra(CropActivity.SELECTED_MEDIA, mediaSelected);

                // Set result and finish this Activity.
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
