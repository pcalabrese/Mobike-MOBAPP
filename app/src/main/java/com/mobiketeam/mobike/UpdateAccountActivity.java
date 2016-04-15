package com.mobiketeam.mobike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UpdateAccountActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mNickname, mBikemodel;
    private Button mDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_account);

        mNickname = (EditText) findViewById(R.id.account_nickname_text);
        mBikemodel = (EditText) findViewById(R.id.account_bikemodel_text);
        mDone = (Button) findViewById(R.id.done_button);

        Intent intent = getIntent();
        String nickname = intent.getStringExtra(AccountDetailsActivity.ACCOUNT_NICKNAME);
        String bikemodel = intent.getStringExtra(AccountDetailsActivity.ACCOUNT_BIKEMODEL);

        mNickname.setText(nickname);
        mBikemodel.setText(bikemodel);
        mDone.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.done_button:
                sendUpdatedAccount();
                break;
        }
    }

    private void sendUpdatedAccount() {
        if (mNickname.getText().toString().length() == 0) {
            Toast.makeText(this, "Insert a nickname", Toast.LENGTH_SHORT).show();
        } else if (mBikemodel.getText().toString().length() == 0) {
            Toast.makeText(this, "Insert the bikemodel", Toast.LENGTH_SHORT).show();
        } else {

        }
    }
}
