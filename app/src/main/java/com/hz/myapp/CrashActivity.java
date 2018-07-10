package com.hz.myapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.Button;

/**
 * Created by zeee on 16-04-2018.
 */

public class CrashActivity extends Activity {

    private static final String TAG = CrashActivity.class.getSimpleName();

    private Button okButton;
    private AppCompatCheckBox emailCheckBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        okButton = findViewById(R.id.ok_button);
        emailCheckBox = findViewById(R.id.crash_email_checkbox);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(emailCheckBox.isChecked()){
                    String[] address = {getString(R.string.email_address)};
                    Intent openEmail = new Intent(Intent.ACTION_SENDTO);
                    openEmail.setData(Uri.parse("mailto:")); // only email apps should handle this
                    openEmail.putExtra(Intent.EXTRA_EMAIL, address);
                    openEmail.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crash_email_subject));
                    if (openEmail.resolveActivity(getPackageManager()) != null) {
                        startActivity(openEmail);
                    }
                    else{
                        //Log.e(TAG, "email app not found! source = crash CheckBox");
                    }
                }

                moveTaskToBack(true);
                finish();
                Process.killProcess(Process.myPid());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        moveTaskToBack(true);
        finish();
        Process.killProcess(Process.myPid());
    }
}
