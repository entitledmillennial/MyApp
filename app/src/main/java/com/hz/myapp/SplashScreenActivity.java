package com.hz.myapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import java.io.File;


/**
 * Created by zeee on 21-04-2018.
 */

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private static final long THRESHOLD_DIR_SIZE_FOR_CLEANUP = 103809024L; //Approx. 99 MB

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_SplashScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        progressBar = findViewById(R.id.splash_progressbar);

        if(getFileOrFolderSize(getFilesDir()) > THRESHOLD_DIR_SIZE_FOR_CLEANUP){
            new DeleteFileOrFolderTask().execute(getFilesDir());
        }
        else{
            Intent intent = new Intent(SplashScreenActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private class DeleteFileOrFolderTask extends AsyncTask<File,Integer,Boolean> {
        private int progress =0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Log.e(TAG, "INSIDE DELETE...");
            progressBar.setMax(getFilesDir().listFiles().length);
        }

        @Override
        protected Boolean doInBackground(File... dirs) {
            for (File file : dirs[0].listFiles()) {
                if (file.isFile()) {
                    file.delete();
                    publishProgress(progress++);
                    //Log.e(TAG, "deleted "+ progress);
                } else {
                    deleteFileOrFolder(file);
                    publishProgress(progress++);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            Intent intent = new Intent(SplashScreenActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private long getFileOrFolderSize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                } else
                    size += getFileOrFolderSize(file);
            }
        } else if (dir.isFile()) {
            size += dir.length();
        }
        return size;
    }

    //Only called when recursion is required to deal with dir within dir.
    private void deleteFileOrFolder(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else
                deleteFileOrFolder(file);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
