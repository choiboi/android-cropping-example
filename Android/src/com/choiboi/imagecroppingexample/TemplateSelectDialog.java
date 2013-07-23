package com.choiboi.imagecroppingexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TemplateSelectDialog extends Activity {
    
    private ListView mListView;
    
    public static final String POSITION = "POSITION";
    public static final String[] FACE_TEMPLATE_TEXT = new String[] {
            "Oblong Face", "Oval Face", "Round Face", "Square Face",
            "Triangular Face" };
    public static final Integer[] FACE_TEMPLATE_IMAGE = new Integer[] {
            R.drawable.face_oblong, R.drawable.face_oval,
            R.drawable.face_round, R.drawable.face_square,
            R.drawable.face_triangular };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.select_dialog);

        // Set result CANCELED in-case the user backs out
        setResult(Activity.RESULT_CANCELED);
        
        mListView = (ListView) findViewById(R.id.select_list);
        mListView.setAdapter(new FaceTemplateListViewAdapter(this, FACE_TEMPLATE_TEXT, FACE_TEMPLATE_IMAGE));
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(POSITION, position);

                // Set result and finish this Activity.
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
    
    // Adapter that will setup the layout of each line in ListView.
    private class FaceTemplateListViewAdapter extends BaseAdapter {
        private Context mmContext;
        private String[] mmTypes;
        private Integer[] mmImages;
        private LayoutInflater layoutInflator;

        public FaceTemplateListViewAdapter(Context context, String[] types, Integer[] imgs) {
            mmContext = context;
            mmTypes = types;
            mmImages = imgs;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            layoutInflator = (LayoutInflater) mmContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflator.inflate(R.layout.face_template_listivew_layout, parent, false);

            TextView textview = (TextView) row.findViewById(R.id.ft_face_template_label);
            ImageView imageview = (ImageView) row.findViewById(R.id.ft_face_template_image);

            textview.setText(mmTypes[position]);
            imageview.setImageResource(mmImages[position]);

            return (row);
        }

        @Override
        public int getCount() {
            return mmTypes.length;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }
    }
}
