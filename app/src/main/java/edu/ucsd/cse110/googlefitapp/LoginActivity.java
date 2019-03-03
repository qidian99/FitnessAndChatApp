package edu.ucsd.cse110.googlefitapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

public class LoginActivity extends AppCompatActivity {
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private FitnessOptions fitnessOptions;
    public static final String TAG = "LOGIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .build();

        new CheckUserSignInAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(500));

    }

    public void googleSignin(View view) {
        if(GoogleSignIn.getLastSignedInAccount(this) == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 9001);
        }
    }

    private class CheckUserSignInAsyncTask extends AsyncTask<String, String, Void> {

        private boolean isCancelled = false;

        @Override
        protected Void doInBackground(String... sleepTime) {
            while (!isCancelled) {
                Log.d(TAG, "Login going on.");
                try {
                    Thread.sleep(Integer.valueOf(sleepTime[0]));
                    publishProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... text) {
            if(GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(LoginActivity.this), fitnessOptions)){
                isCancelled = true;
                Log.d(TAG, "Login suceeds.");
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                Log.d(TAG, "Async stopped");
//                startActivity(intent);
                LoginActivity.this.finish();
                overridePendingTransition(R.anim.slide_r_to_l_enter, R.anim.slide_r_to_l_exit);
            }
        }
    }

    @Override
    public void onBackPressed() {
//        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 9001) {
            if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
                Toast.makeText(this, "Authorization is needed to use this app", Toast.LENGTH_SHORT).show();
                GoogleSignIn.requestPermissions(
                        this, // your activity
                        GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                        GoogleSignIn.getLastSignedInAccount(this),
                        fitnessOptions);
            }
        }
    }
}