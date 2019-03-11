package edu.ucsd.cse110.googlefitapp.adapter;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.googlefitapp.Activity;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;

public class PlannedWalkAdapter implements FitnessService {
    private static final String APP_PACKAGE_NAME = "edu.ucsd.cse110.googlefitapp";
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private final String TAG = "PlannedWalkAdapter";
    private Activity plannedWalkActivity;
    private boolean isCancelled = false;
    private int step = 0;

    public PlannedWalkAdapter(Activity activity) {
        this.plannedWalkActivity = activity;
    }

    public void setup() {
//        FitnessOptions fitnessOptions = FitnessOptions.builder()
//                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
//                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
//                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
//                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
//                .build();
//
//        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(plannedWalkActivity), fitnessOptions)) {
//            GoogleSignIn.requestPermissions(
//                    plannedWalkActivity, // your activity
//                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
//                    GoogleSignIn.getLastSignedInAccount(plannedWalkActivity),
//                    fitnessOptions);
//        } else {
            updateStepCount();
            startRecording();
            //create the async task here to refresh every 2 seconds
            new CountToTenAsyncTask().execute(String.valueOf(2000));
//        }
    }

    public void stopAsync() {
        isCancelled = true;

    }

    public void startAsync() {
        isCancelled = false;
        new CountToTenAsyncTask().execute(String.valueOf(2000));

    }

    @Override
    public boolean hasPermission() {
        return false;
    }

    @Override
    public void addInactiveSteps(int extraStep) {
    }

    @Override
    public void addActiveSteps(int step, int min, int sec, float stride) {

    }

    @Override
    public String getUID() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    private void startRecording() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(plannedWalkActivity);
        if (lastSignedInAccount == null) {
            return;
        }

        Fitness.getRecordingClient(plannedWalkActivity, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(plannedWalkActivity)))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "Successfully subscribed!"))
                .addOnFailureListener(e -> Log.i(TAG, "There was a problem subscribing."));
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    public void updateStepCount() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(plannedWalkActivity);
        if (lastSignedInAccount == null) {
            return;
        }
        GetDayRange getDayRange = new GetDayRange().invoke();
        long startTime = getDayRange.getStartTime();
        long endTime = getDayRange.getEndTime();

        Fitness.getHistoryClient(plannedWalkActivity, lastSignedInAccount)
                .readData(new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                                DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(
                        dataReadResponse -> {
                            DataSet dataSet = dataReadResponse.getBuckets().get(0).getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA);
                            Log.d(TAG, "Aggregate step count before adding active data in updateStepCount: " + Objects.requireNonNull(dataSet).toString());
                            int total =
                                    dataSet.isEmpty()
                                            ? 0
                                            : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                            plannedWalkActivity.updateAll(total);
                            Log.d(TAG, "Total steps in updateStepCount: " + total);
                        })
                .addOnFailureListener(
                        e -> Log.d(TAG, "There was a problem getting the step count.", e));
    }

    public void mockDataPoint() {
        GetDayRange getDayRange = new GetDayRange().invoke();
        long startTime = getDayRange.getStartTime();
        long endTime = getDayRange.getEndTime();
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(plannedWalkActivity);
        Fitness.getHistoryClient(plannedWalkActivity, Objects.requireNonNull(gsa))
                .readData(new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                                DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(
                        dataReadResponse -> {
                            DataSet dataSet = dataReadResponse.getBuckets().get(0).getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA);
                            Log.d(TAG, "mockDataPoint dataSet.isEmpty() = " + Objects.requireNonNull(dataSet).isEmpty());
                            if (dataSet.isEmpty()) {
                                mockEmptyDataSet(gsa);
                            } else {
                                mockNonemptyDataSet(dataSet);
                            }
                            updateStepCount();
                        })
                .addOnFailureListener(e -> {
                });
    }

    private void mockNonemptyDataSet(DataSet dataSet) {
        step = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt() + 500;
        dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).setInt(step);
        Log.d(TAG, "Total steps in mockDataPoint: " + dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());

        // Create a data source
        DataSource dataSource =
                new DataSource.Builder()
                        .setAppPackageName(APP_PACKAGE_NAME)
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setStreamName(TAG + " - step count")
                        .setType(DataSource.TYPE_RAW)
                        .build();
        DataSet dataSet2 = DataSet.create(dataSource);
        DataPoint dataPoint =
                dataSet2.createDataPoint().setTimeInterval(dataSet.getDataPoints().get(0)
                        .getStartTime(TimeUnit.MILLISECONDS), dataSet.getDataPoints().get(0)
                        .getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(step);
        dataSet2.add(dataPoint);
        Log.d(TAG, "mockDataPoint - Newly created dataSet: " + dataSet2);

        DataUpdateRequest request = new DataUpdateRequest.Builder()
                .setDataSet(dataSet2)
                .setTimeInterval(dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS),
                        dataSet.getDataPoints().get(0).getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(plannedWalkActivity, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(plannedWalkActivity))).
                updateData(request);
    }

    private void mockEmptyDataSet(GoogleSignInAccount gsa) {
        int stepCountDelta = 500;
        Calendar cal = StepCalendar.getInstance();
        long endTime1 = cal.getTimeInMillis();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        long startTime = cal.getTimeInMillis();

        DataSource dataSource =
                new DataSource.Builder()
                        .setAppPackageName(APP_PACKAGE_NAME)
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setStreamName(TAG + " - step count")
                        .setType(DataSource.TYPE_RAW)
                        .build();
        DataSet newDataSet = DataSet.create(dataSource);
        DataPoint dataPoint =
                newDataSet.createDataPoint().setTimeInterval(startTime, endTime1, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
        step = stepCountDelta;
        newDataSet.add(dataPoint);
        Log.d(TAG, "mockDataPoint - Newly created dataSet: " + newDataSet);

        Fitness.getHistoryClient(plannedWalkActivity, gsa).insertData(newDataSet);
    }

    @Override
    public int getRequestCode() {
        return GOOGLE_FIT_PERMISSIONS_REQUEST_CODE;
    }

    @SuppressLint("StaticFieldLeak")
    private class CountToTenAsyncTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... sleepTime) {
            while (!isCancelled) {

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
                Log.d(TAG, "onProgressUpdate Success");

                updateStepCount();
            }
        }
    }

    private class GetDayRange {
        private long startTime;
        private long endTime;

        long getStartTime() {
            return startTime;
        }

        long getEndTime() {
            return endTime;
        }

        GetDayRange invoke() {
            Calendar tempCal = StepCalendar.getInstance();
            tempCal.set(Calendar.SECOND, 0);
            tempCal.set(Calendar.MINUTE, 0);
            tempCal.set(Calendar.HOUR_OF_DAY, 0);
            startTime = tempCal.getTimeInMillis();
            tempCal.set(Calendar.SECOND, 59);
            tempCal.set(Calendar.MINUTE, 59);
            tempCal.set(Calendar.HOUR_OF_DAY, 23);
            endTime = tempCal.getTimeInMillis();
            return this;
        }
    }
}
