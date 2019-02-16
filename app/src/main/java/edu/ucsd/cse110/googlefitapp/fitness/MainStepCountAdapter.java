package edu.ucsd.cse110.googlefitapp.fitness;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.ConfigApi;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.googlefitapp.MainActivity;

public class MainStepCountAdapter implements FitnessService {
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private final String TAG = "MainStepCountAdapter";
    private FitnessOptions fitnessOptions;
    private MainActivity activity;
    private DataType activeDataType;
    public static String ACTIVE_DT_NAME = "edu.ucsd.cse110.googlefitapp.activedata";
    private int step;
    boolean isCancelled = false;
    private Calendar calendar = Calendar.getInstance();

    public MainStepCountAdapter(MainActivity activity) {
        this.activity = activity;
    }


    public void setup() {
        if( fitnessOptions == null ) {
            fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                    .build();

            if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)) {
                Toast.makeText(activity, "Authorization is needed to use this app", Toast.LENGTH_SHORT).show();
                GoogleSignIn.requestPermissions(
                        activity, // your activity
                        GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                        GoogleSignIn.getLastSignedInAccount(activity),
                        fitnessOptions);
            } else {
                updateStepCount();
                startRecording();
            }

            Thread reqThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // We will create a custom data type, namely active data for this particular app
                        // 1. Build a request to create a new data type
                        DataTypeCreateRequest request = new DataTypeCreateRequest.Builder()
                                // The prefix of your data type name must match your app's package name
                                .setName(ACTIVE_DT_NAME)
                                // Add some custom fields, both int and float
                                .addField("active data", Field.FORMAT_INT32)
                                // Add some common fields
                                .addField(Field.FIELD_ACTIVITY)
                                .build();

                        // 2. Invoke the Config API with:
                        // - The Google API client object
                        // - The create data type request
                        GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);
                        Task<DataType> response =
                                Fitness.getConfigClient(activity, gsa).createCustomDataType(request);
                        activeDataType = Tasks.await(response);
                        if(activeDataType == null) {
                            Task<DataType> pendingResult =
                                    Fitness.getConfigClient(activity, gsa).readDataType(ACTIVE_DT_NAME);
                            activeDataType = Tasks.await(pendingResult);
                            Log.d(TAG, activeDataType.toString());
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Log.println(Log.DEBUG, TAG,"Custom data type created.");
                    }

                }
            });
            reqThread.start();
        }
    }

    private void startRecording() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastSignedInAccount == null) {
            return;
        }

        Fitness.getRecordingClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
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
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastSignedInAccount == null) {
            return;
        }

        Fitness.getHistoryClient(activity, lastSignedInAccount)
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

                                step = total;
                                activity.updateAll(total);
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

    @Override
    public void stopAsync() {
        isCancelled = true;
    }

    @Override
    public void startAsync() {
        isCancelled = false;
        new UpdateStepAsyncTask().execute(String.valueOf(2000));
    }

    @Override
    public boolean hasPermission() {
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions);
    }

    @Override
    public void addInactiveSteps(int extraStep) {
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);
        Fitness.getHistoryClient(activity, gsa)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                System.out.println("begin mock data" + dataSet.isEmpty());
                                if(dataSet.isEmpty()) {
                                    int stepCountDelta = extraStep;
                                    Calendar cal = Calendar.getInstance();
                                    Date now = new Date();
                                    cal.setTime(now);
                                    long endTime = cal.getTimeInMillis();
                                    cal.add(Calendar.HOUR_OF_DAY, -1);
                                    long startTime = cal.getTimeInMillis();

                                    DataSource dataSource =
                                            new DataSource.Builder()
                                                    .setAppPackageName(activity)
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

                                    Log.d(TAG, "Added inactive steps");
                                    Log.d(TAG, dataSet.toString());

                                    Task<Void> response = Fitness.getHistoryClient(activity, gsa).insertData(dataSet2);
                                    System.out.println(response.isSuccessful());
                                } else {
                                    step = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt() + extraStep;
                                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).setInt(step);
                                    Log.d(TAG, "Total steps: " + dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());

                                    // Create a data source
                                    DataSource dataSource =
                                            new DataSource.Builder()
                                                    .setAppPackageName(activity)
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

                                    Task<Void> response = Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).updateData(request);
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
    public void addActiveSteps(final int step) {
        // Read active data
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);
        Fitness.getHistoryClient(activity, gsa)
                .readData( new DataReadRequest.Builder()
                        .setTimeRange(calendar.getTimeInMillis()-1L, calendar.getTimeInMillis(), TimeUnit.MILLISECONDS)
                        .read(activeDataType)
                        .build())
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                DataSet dataSet = dataReadResponse.getDataSet(activeDataType);
                                Log.d(TAG, "Fetched active data from google cloud. IsEmpty: " + dataSet.isEmpty());
                                if(dataSet.isEmpty()) {
                                    Calendar cal = Calendar.getInstance();
                                    Date now = new Date();
                                    cal.setTime(now);
                                    long endTime = cal.getTimeInMillis();
                                    cal.add(Calendar.SECOND, -1);
                                    long startTime = cal.getTimeInMillis();

                                    DataSource dataSource =
                                            new DataSource.Builder()
                                                    .setAppPackageName(activity)
                                                    .setDataType(activeDataType)
                                                    .setStreamName(TAG + " - active step")
                                                    .setType(DataSource.TYPE_RAW)
                                                    .build();
                                    DataSet dataSet2 = DataSet.create(dataSource);
                                    DataPoint dataPoint =
                                            dataSet2.createDataPoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
                                    dataPoint.getValue(activeDataType.getFields().get(0)).setInt(step);
//                                    step = stepCountDelta;
                                    dataSet2.add(dataPoint);

                                    Log.d(TAG, String.format("Added %d active steps", step));
                                    Log.d(TAG, dataSet2.toString());

                                    Task<Void> response = Fitness.getHistoryClient(activity, gsa).insertData(dataSet2);
                                } else {
                                    int newActiveStep = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt() + step;
                                    dataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(0)).setInt(newActiveStep);
                                    Log.d(TAG, "Total active steps: " + dataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(0)).asInt());

                                    // Create a data source
                                    DataSource dataSource =
                                            new DataSource.Builder()
                                                    .setAppPackageName(activity)
                                                    .setDataType(activeDataType)
                                                    .setStreamName(TAG + " - active step")
                                                    .setType(DataSource.TYPE_RAW)
                                                    .build();
                                    DataSet dataSet2 = DataSet.create(dataSource);
                                    DataPoint dataPoint =
                                            dataSet2.createDataPoint().setTimeInterval(dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS),dataSet.getDataPoints().get(0).getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
                                    dataPoint.getValue(activeDataType.getFields().get(0)).setInt(newActiveStep);
                                    dataSet2.add(dataPoint);
                                    DataUpdateRequest request = new DataUpdateRequest.Builder()
                                            .setDataSet(dataSet2)
                                            .setTimeInterval(dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS) ,dataSet.getDataPoints().get(0).getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS )
                                            .build();

                                    Task<Void> response = Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).updateData(request);
                                }
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

    private class UpdateStepAsyncTask extends AsyncTask<String, String, Void> {

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
                int today = calendar.get(Calendar.DAY_OF_WEEK);
                int day = activity.getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).getInt("day", -1);

                if(day != today) {
                    activity.setGoalChangeable(true);
                    activity.setCanShowHalfEncour(true);
                    activity.setCanShowOverPrevEncour(true);
                    activity.getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt("day", today).apply();
                }

                //call update steps here
                updateStepCount();

                if(step > activity.getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE).getInt(MainActivity.KEY_GOAL, 0) / 2 && activity.getCanShowHalfEncour()){
                    activity.showAchieveHalfEncouragement();
                }

                if(step > activity.getSharedPreferences(MainActivity.SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE).getInt(MainActivity.KEY_GOAL, 0) && activity.getGoalChangeable()){
                    activity.showNewGoalPrompt();
                }

                int yesterday = today - 1 >= 0 ? today - 1 : 6;
                if(step > activity.getSharedPreferences("weekly_steps",
                        Context.MODE_PRIVATE).getInt(String.valueOf(yesterday), 0) + 1000 && activity.getCanShowOverPrevEncour()) {
                    activity.showOverPrevEncouragement();
                }
            }
        }
    }
}
