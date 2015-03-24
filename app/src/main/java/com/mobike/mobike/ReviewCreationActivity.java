package com.mobike.mobike;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobike.mobike.network.EditReviewTask;
import com.mobike.mobike.network.UploadNewReviewTask;


public class ReviewCreationActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String TAG = "ReviewCreationActivity";
    private String routeID;
    private int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_review_creation);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ((Button) findViewById(R.id.send)).setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        routeID = bundle.getString(SearchFragment.ROUTE_ID);

        requestCode = bundle.getInt(SearchFragment.REQUEST_CODE);

        if (requestCode == SummaryActivity.REVIEW_REQUEST) {
            TextView mTextView = ((TextView) findViewById(R.id.review_text_view));
            mTextView.setText(getResources().getString(R.string.review_after_upload_text));
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int dpValue = 15; // margin in dips
            float d = getResources().getDisplayMetrics().density;
            int margin = (int)(dpValue * d); // margin in pixels
            llp.setMargins(margin, margin, margin, margin); // llp.setMargins(left, top, right, bottom);
            mTextView.setLayoutParams(llp);
        }

        if (requestCode == RouteActivity.EDIT_REVIEW_REQUEST) {
            ((RatingBar) findViewById(R.id.rating_bar)).setRating(Float.parseFloat(bundle.getString(RouteActivity.USER_RATE)));
            ((EditText) findViewById(R.id.comment)).setText(bundle.getString(RouteActivity.USER_MESSAGE));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_review_creation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                String comment = ((EditText) findViewById(R.id.comment)).getText().toString();
                float rate = ((RatingBar) findViewById(R.id.rating_bar)).getRating();

                // upload della nuova review
                if (rate == 0) {
                    Toast.makeText(this, "Please rate the route", Toast.LENGTH_SHORT).show();
                } else if (comment.length() == 0) {
                    Toast.makeText(this, "Please insert a comment", Toast.LENGTH_SHORT).show();
                } else {

                    if (requestCode != RouteActivity.EDIT_REVIEW_REQUEST)
                        new UploadNewReviewTask(this, routeID, comment, rate).execute();
                    else
                        new EditReviewTask(this, routeID, comment, rate).execute();

                    setResult(RESULT_OK, null);
                    finish();
                }

                Log.v(TAG, "rate: " + rate + ", routeID: " + routeID);
                break;
        }
    }
}
