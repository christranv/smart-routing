package com.example.smrt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

public class RoutingInputActivity extends AppCompatActivity {

    ImageView backIcon;
    String start, end;
    AutoCompleteTextView startTextView, endTextView;
    TextView selectOnMap;
    String activeInput = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing_input);

        backIcon = findViewById(R.id.backIcon);
        selectOnMap = findViewById(R.id.selectOnMapTextView);
        startTextView = findViewById(R.id.startTextView2);
        endTextView = findViewById(R.id.endTextView2);
        Intent intent = getIntent();
        startTextView.setText(intent.getStringExtra("start"));
        endTextView.setText(intent.getStringExtra("end"));

        activeInput = intent.getStringExtra("activeInput");
        if(activeInput.equals("start"))
            startTextView.requestFocus();
        else
            endTextView.requestFocus();
        startTextView.setOnTouchListener(
                (view, motionEvent) -> {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) activeInput="start";
                    return false;
                }
        );
        endTextView.setOnTouchListener(
                (view, motionEvent) -> {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) activeInput="end";
                    return false;
                }
        );

        backIcon.setOnClickListener(view -> back2Main(1));
        selectOnMap.setOnClickListener(view -> back2Main(2));

        startTextView.setAdapter(MainActivity.adapStreetName);
        endTextView.setAdapter(MainActivity.adapStreetName);
    }

    private void back2Main(int mode) {
        Intent i = new Intent(RoutingInputActivity.this, MainActivity.class);
        if(mode==1){
            i.putExtra("mode",1);
        } else if(mode==2){ //mode for choose point

            i.putExtra("mode",2);
            i.putExtra("activeInput",activeInput);
        }
        start = startTextView.getText().toString();
        end = endTextView.getText().toString();
        i.putExtra("start", start);
        i.putExtra("end", end);
        setResult(RESULT_OK, i);
        finish();
    }
}
