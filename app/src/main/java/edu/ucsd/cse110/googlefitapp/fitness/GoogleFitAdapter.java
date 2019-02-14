package edu.ucsd.cse110.googlefitapp.fitness;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.googlefitapp.Encouragement;
import edu.ucsd.cse110.googlefitapp.MainActivity;
import edu.ucsd.cse110.googlefitapp.StepCountActivity;

public class GoogleFitAdapter implements FitnessService {
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private final String TAG = "GoogleFitAdapter";
//    GoogleSignInOptionsExtension fitnessOptions =
//            FitnessOptions.builder()
//                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
//                    .build();

    boolean isCancelled = false;
    private Encouragement encouragement;

    protected StepCountActivity stepCountActivity;
    private int step = 0;
    protected int totalSteps = 0;
    public GoogleFitAdapter(StepCountActivity activity) {
        this.stepCountActivity = activity;
    }



    public void setup() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(stepCountActivity), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    stepCountActivity, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(stepCountActivity),
                    fitnessOptions);
        } else {
            encouragement = new Encouragement(stepCountActivity, true);
            updateStepCount();

            startRecording();

            //create the async task here to refresh every five seconds
            //after every 7 seconds refresh the total step count 7718 is "Bill" upside down
            new CountToTenAsyncTask().execute(String.valueOf(2000));


        }
    }

    public void stopAsync() {
        isCancelled = true;

    }

    public void startAsync() {
        isCancelled = false;
        new CountToTenAsyncTask().execute(String.valueOf(2000));

    }

    private class CountToTenAsyncTask extends AsyncTask<String, String, Void> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(String... sleepTime) {
            while(!isCancelled) {

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

            if (isCancelled) {
                cancel(true);
            } else {
                //call update steps here
                System.out.println("SEVEN SECONDS PASSED REFRESH PERIOD RESTARTED");

                updateStepCount();
            }
        }
    }

    private void startRecording() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(stepCountActivity);
        if (lastSignedInAccount == null) {
            return;
        }

        Fitness.getRecordingClient(stepCountActivity, GoogleSignIn.getLastSignedInAccount(stepCountActivity))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
    }


    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    public void updateStepCount() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(stepCountActivity);
        if (lastSignedInAccount == null) {
            return;
        }

        Fitness.getHistoryClient(stepCountActivity, lastSignedInAccount)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                Log.d(TAG, dataSet.toString());
                                int total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                                totalSteps = total;

                                stepCountActivity.updateAll(total);
                                Log.d(TAG, "Total steps: " + total);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "There was a problem getting the step count.", e);
                            }
                        });


    }

    public void getStepCount(final TextView stepText){
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(stepCountActivity);
        if (lastSignedInAccount == null) {
            return;
        }

        Fitness.getHistoryClient(stepCountActivity, lastSignedInAccount)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                Log.d(TAG, dataSet.toString());
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                stepText.setText(String.format(MainActivity.SHOW_STEP, total));
                                Log.d(TAG, "Total steps: " + total);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "There was a problem getting the step count.", e);
                            }
                        });
    }

    public void mockDataPoint(){
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(stepCountActivity);

        Fitness.getHistoryClient(stepCountActivity, gsa)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                System.out.println("begin mock data" + dataSet.isEmpty());
                                if(dataSet.isEmpty()) {
                                    int stepCountDelta = 950;
                                    Calendar cal = Calendar.getInstance();
                                    Date now = new Date();
                                    cal.setTime(now);
                                    long endTime = cal.getTimeInMillis();
                                    cal.add(Calendar.HOUR_OF_DAY, -1);
                                    long startTime = cal.getTimeInMillis();

                                    DataSource dataSource =
                                            new DataSource.Builder()
                                                    .setAppPackageName(stepCountActivity)
                                                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                                    .setStreamName(TAG + " - step count")
                                                    .setType(DataSource.TYPE_RAW)
                                                    .build();
                                    DataSet dataSet2 = DataSet.create(dataSource);
                                    DataPoint dataPoint =
                                            dataSet2.createDataPoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
                                    dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
                                    step = stepCountDelta;
                                    dataSet2.add(dataPoint);


                                    System.out.println("Added!  ");
                                    Log.d(TAG, dataSet.toString());

                                    Task<Void> response = Fitness.getHistoryClient(stepCountActivity, gsa).insertData(dataSet2);
                                    System.out.println(response.isSuccessful());
                                } else {
                                    step = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt() + 50;
                                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).setInt(step);
                                    Log.d(TAG, "Total steps: " + step);
                                    Log.d(TAG, "Total steps: " + dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());

                                    // Create a data source
                                    DataSource dataSource =
                                            new DataSource.Builder()
                                                    .setAppPackageName(stepCountActivity)
                                                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                                    .setStreamName(TAG + " - step count")
                                                    .setType(DataSource.TYPE_RAW)
                                                    .build();
                                    DataSet dataSet2 = DataSet.create(dataSource);
                                    DataPoint dataPoint =
                                            dataSet2.createDataPoint().setTimeInterval(dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS),dataSet.getDataPoints().get(0).getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
                                    dataPoint.getValue(Field.FIELD_STEPS).setInt(step);
                                    dataSet2.add(dataPoint);
                                    DataUpdateRequest request = new DataUpdateRequest.Builder()
                                            .setDataSet(dataSet2)
                                            .setTimeInterval(dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS) ,dataSet.getDataPoints().get(0).getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS )
                                            .build();

                                    Task<Void> response = Fitness.getHistoryClient(stepCountActivity, GoogleSignIn.getLastSignedInAccount(stepCountActivity)).updateData(request);
                                }
                                updateStepCount();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
    }

    @Override
    public int getRequestCode() {
        return GOOGLE_FIT_PERMISSIONS_REQUEST_CODE;
    }

    @Override
    public void updateActivity() {


    }

}
