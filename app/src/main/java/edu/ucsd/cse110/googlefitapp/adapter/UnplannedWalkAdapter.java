package edu.ucsd.cse110.googlefitapp.adapter;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.ConfigApi;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.googlefitapp.Activity;
import edu.ucsd.cse110.googlefitapp.MainActivity;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;

public class UnplannedWalkAdapter implements FitnessService {
    private static String ACTIVE_DT_NAME = "edu.ucsd.cse110.googlefitapp.active";
    private static String APP_PACKAGE_NAME = "edu.ucsd.cse110.googlefitapp";
    public static Calendar calendar = MainActivity.calendar;
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private final String TAG = "UnplannedWalkAdapter";
    private boolean isCancelled = false;
    private FitnessOptions fitnessOptions;
    private Activity activity;
    private DataType activeDataType;
    private int currentStep;

    public UnplannedWalkAdapter(Activity activity) {
        this.activity = activity;
    }

    public void setup() {
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
        }

        updateStepCount();
        startRecording();


        try {
            GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);

            Fitness.getConfigClient(activity, Objects.requireNonNull(gsa)).readDataType(ACTIVE_DT_NAME).
                    addOnSuccessListener(dataType -> {
                        Log.e(TAG, "Found data type: " + dataType);
//                        activeDataType = dataType;
                        CreateCustomDataType(gsa);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Datatype not found.");

                        CreateCustomDataType(gsa);
                    });
//            Fitness.getConfigClient(activity, Objects.requireNonNull(gsa)).readDataType(this, "com.app.custom_data_type");


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void CreateCustomDataType(GoogleSignInAccount gsa) {
        DataTypeCreateRequest request = new DataTypeCreateRequest.Builder()
                .setName(ACTIVE_DT_NAME)
                .addField("ActiveSteps", Field.FORMAT_INT32)
                .addField("ActiveMin", Field.FORMAT_INT32)
                .addField("ActiveSec", Field.FORMAT_INT32)
                .addField("ActiveDistance", Field.FORMAT_FLOAT)
                .addField("ActiveSpeed", Field.FORMAT_FLOAT)
                .addField(Field.FIELD_ACTIVITY)
                .build();

        Task<DataType> response =
                Fitness.getConfigClient(activity, Objects.requireNonNull(gsa)).createCustomDataType(request)
                        .addOnSuccessListener((DataType dataType) -> {
                            Log.e(TAG, "Sucessfully created new datatype: " + dataType.toString());
                            activeDataType = dataType;
                            fitnessOptions = FitnessOptions.builder()
                                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                                    .addDataType(activeDataType, FitnessOptions.ACCESS_READ)
                                    .addDataType(activeDataType, FitnessOptions.ACCESS_WRITE)
                                    .build();
                            if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)) {
                                Toast.makeText(activity, "Authorization is needed to use this app", Toast.LENGTH_SHORT).show();
                                GoogleSignIn.requestPermissions(
                                        activity, // your activity
                                        GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                                        GoogleSignIn.getLastSignedInAccount(activity),
                                        fitnessOptions);
                            }
                        })
                        .addOnFailureListener(err -> Log.e(TAG, "There was a problem creating new datatype: " + err));
    }

    private void startRecording() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastSignedInAccount == null) {
            return;
        }

        Fitness.getRecordingClient(activity, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(activity)))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "Successfully subscribed!"))
                .addOnFailureListener(e -> Log.i(TAG, "There was a problem subscribing."));
    }

    /**
     * Reads the current daily currentStep total, computed from midnight of the current day on the device's
     * current timezone.
     */
    public void updateStepCount() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastSignedInAccount == null) {
            return;
        }
        Calendar tempCal = StepCalendar.getInstance();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR, 0);
        long startTime = tempCal.getTimeInMillis();
        // Get next Saturday
        tempCal.set(Calendar.SECOND, 59);
        tempCal.set(Calendar.MINUTE, 59);
        tempCal.set(Calendar.HOUR, 23);
        long endTime = tempCal.getTimeInMillis();

        Fitness.getHistoryClient(activity, lastSignedInAccount)
                .readData(new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                                DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(
                        dataReadResponse -> {
                            DataSet dataSet = dataReadResponse.getBuckets().get(0).getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA);
                            Log.d(TAG, Objects.requireNonNull(dataSet).toString());
                            int total =
                                    dataSet.isEmpty()
                                            ? 0
                                            : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                            currentStep = total;
                            activity.setStep(currentStep);
                            activity.updateAll(total);
                            activity.notifyObservers();
                            Log.d(TAG, "Total steps in updateStepCount: " + total);
                        })
                .addOnFailureListener(
                        e -> Log.d(TAG, "There was a problem getting the currentStep count.", e));
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
        Calendar tempCal = StepCalendar.getInstance();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR, 0);
        long startTime = tempCal.getTimeInMillis();
        // Get next Saturday
        tempCal.set(Calendar.SECOND, 59);
        tempCal.set(Calendar.MINUTE, 59);
        tempCal.set(Calendar.HOUR, 23);
        long endTime = tempCal.getTimeInMillis();
        Fitness.getHistoryClient(activity, Objects.requireNonNull(gsa))
                .readData(new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                                DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .bucketByTime(1, TimeUnit.DAYS)
//                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build())
                .addOnSuccessListener(
                        dataReadResponse -> {
                            Log.d(TAG, "Begin addInactiveSteps");
                            List<Bucket> buckets = dataReadResponse.getBuckets();
                            DataSet dataSet = buckets.get(0).getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA);
                            Log.d(TAG, Objects.requireNonNull(dataSet).toString());
                            if (dataSet.isEmpty()) {
                                Calendar cal = StepCalendar.getInstance();
                                long endTime1 = cal.getTimeInMillis();
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.HOUR, 0);
                                long startTime1 = cal.getTimeInMillis();

                                DataSource dataSource =
                                        new DataSource.Builder()
                                                .setAppPackageName(APP_PACKAGE_NAME)
                                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                                .setStreamName(TAG + " - currentStep count")
                                                .setType(DataSource.TYPE_RAW)
                                                .build();
                                DataSet dataSet2 = DataSet.create(dataSource);
                                DataPoint dataPoint =
                                        dataSet2.createDataPoint().setTimeInterval(startTime1, endTime1, TimeUnit.MILLISECONDS);
                                dataPoint.getValue(Field.FIELD_STEPS).setInt(extraStep);
                                dataSet2.add(dataPoint);

                                Log.d(TAG, "addInactiveSteps added: " + dataSet2.toString());

                                Task<Void> response = Fitness.getHistoryClient(activity, gsa).insertData(dataSet2);
                                Log.d(TAG, "response.isSuccessful() = " + response.isSuccessful());
                            } else {
                                DataDeleteRequest request =
                                        new DataDeleteRequest.Builder()
                                                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                                                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                                .build();

                                Fitness.getHistoryClient(activity, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(activity)))
                                        .deleteData(request);

                                int step = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt() + extraStep;
                                Calendar cal = StepCalendar.getInstance();
                                long endTime1 = cal.getTimeInMillis();
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.HOUR, 0);
                                long startTime1 = cal.getTimeInMillis();

                                DataSource dataSource =
                                        new DataSource.Builder()
                                                .setAppPackageName(APP_PACKAGE_NAME)
                                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                                .setStreamName(TAG + " - currentStep count")
                                                .setType(DataSource.TYPE_RAW)
                                                .build();
                                DataSet dataSet2 = DataSet.create(dataSource);
                                DataPoint dataPoint =
                                        dataSet2.createDataPoint().setTimeInterval(startTime1, endTime1, TimeUnit.MILLISECONDS);
                                dataPoint.getValue(Field.FIELD_STEPS).setInt(step);
                                dataSet2.add(dataPoint);

                                Log.d(TAG, "addInactiveSteps added: " + dataSet2.toString());

                                Task<Void> response = Fitness.getHistoryClient(activity, gsa).insertData(dataSet2);
                                Log.d(TAG, "response.isSuccessful() = " + response.isSuccessful());
                            }
                            updateStepCount();
                        })
                .addOnFailureListener(
                        e -> {
                        });
    }

    @Override
    public void addActiveSteps(final int step) {
        Calendar tempCal = StepCalendar.getInstance();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR, 0);
        long startTime = tempCal.getTimeInMillis();
        // Get next Saturday
        tempCal.set(Calendar.SECOND, 59);
        tempCal.set(Calendar.MINUTE, 59);
        tempCal.set(Calendar.HOUR, 23);
        long endTime = tempCal.getTimeInMillis();
        // Read active data
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);

        Fitness.getHistoryClient(activity, Objects.requireNonNull(gsa))
                .readData(new DataReadRequest.Builder()
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .read(activeDataType)
                        .build())
                .addOnSuccessListener(
                        dataReadResponse -> {
                            DataSet dataSet = dataReadResponse.getDataSet(activeDataType);
                            Log.e(TAG, "Fetched active data from google cloud. dataSet.isEmpty() = " + dataSet.isEmpty());
                            if (dataSet.isEmpty()) {
                                Calendar cal = StepCalendar.getInstance();
                                long endTime1 = cal.getTimeInMillis();
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.HOUR, 0);
                                long startTime1 = cal.getTimeInMillis();

                                DataSource dataSource =
                                        new DataSource.Builder()
                                                .setAppPackageName(APP_PACKAGE_NAME)
                                                .setDataType(activeDataType)
                                                .setStreamName(TAG + " - active currentStep")
                                                .setType(DataSource.TYPE_RAW)
                                                .build();
                                DataSet dataSet2 = DataSet.create(dataSource);
                                DataPoint dataPoint =
                                        dataSet2.createDataPoint().setTimeInterval(startTime1, endTime1, TimeUnit.MILLISECONDS);
                                dataPoint.getValue(activeDataType.getFields().get(0)).setInt(step);
//                                    currentStep = stepCountDelta;
                                dataSet2.add(dataPoint);

                                Log.e(TAG, String.format("addActiveSteps - Added %d active steps", step));
                                Log.e(TAG, dataSet2.toString());

                                Task<Void> response = Fitness.getHistoryClient(activity, gsa).insertData(dataSet2);
                            } else {
                                int newActiveStep = dataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(0)).asInt() + step;
                                dataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(0)).setInt(newActiveStep);
                                Log.e(TAG, "Total active steps in addActiveSteps: " + dataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(0)).asInt());
                                Log.e(TAG, dataSet.toString());

                                // Create a data source
                                DataSource dataSource =
                                        new DataSource.Builder()
                                                .setAppPackageName(APP_PACKAGE_NAME)
                                                .setDataType(activeDataType)
                                                .setStreamName(TAG + " - active currentStep")
                                                .setType(DataSource.TYPE_RAW)
                                                .build();
                                DataSet dataSet2 = DataSet.create(dataSource);
                                DataPoint dataPoint =
                                        dataSet2.createDataPoint().setTimeInterval(dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS), dataSet.getDataPoints().get(0).getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
                                dataPoint.getValue(activeDataType.getFields().get(0)).setInt(newActiveStep);
                                dataSet2.add(dataPoint);
                                DataUpdateRequest request = new DataUpdateRequest.Builder()
                                        .setDataSet(dataSet2)
                                        .setTimeInterval(dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS), dataSet.getDataPoints().get(0).getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                                        .build();

                                Task<Void> response = Fitness.getHistoryClient(activity, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(activity))).updateData(request);
                            }
                        })
                .addOnFailureListener(
                        e -> {
                        });
    }

    public DataReadRequest getLast7DaysSteps(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR, 0);
        // Get last Sunday
        tempCal.add(Calendar.DATE, -tempCal.get(Calendar.DAY_OF_WEEK + 1));
        long startTime = tempCal.getTimeInMillis();
        // Get next Saturday
        tempCal.add(Calendar.DATE, 7);
        tempCal.add(Calendar.SECOND, -1);
        long endTime = tempCal.getTimeInMillis();
        DataSource activeDataSource = new DataSource.Builder()
                .setAppPackageName(APP_PACKAGE_NAME)
                .setDataType(activeDataType)
                .setName(ACTIVE_DT_NAME)
                .setType(DataSource.TYPE_RAW)
                .build();
        Log.d(TAG, "getLast7DaysSteps Initialize Success");
        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                        DataType.AGGREGATE_STEP_COUNT_DELTA)
                .read(activeDataType)
                .read(activeDataSource)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps, Calendar cal) {
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);
        DataReadRequest dataReadRequest = getLast7DaysSteps(cal);
        Task<DataReadResponse> dataReadResponseTask = Fitness.getHistoryClient(activity, Objects.requireNonNull(gsa))
                .readData(dataReadRequest)
                .addOnSuccessListener(
                        dataReadResponse -> {
                            for (int i = 0; i < 7; i++) {
                                Log.d(TAG, String.format("getLast7DaysSteps - dataReadResponse value at %d = " + dataReadResponse.getBuckets().get(i), i));
                                Bucket bucket = dataReadResponse.getBuckets().get(i);
                                DataSet dtSet = bucket.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA);
                                if (dtSet != null && !dtSet.isEmpty()) {
                                    Log.d(TAG, "getLast7DaysSteps - dtSet steps = " + dtSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());
                                }

                                DataSet dtSet2 = bucket.getDataSet(activeDataType);
                                if (dtSet2 != null && !dtSet2.isEmpty()) {
                                    Log.d(TAG, "getLast7DaysSteps - dtSet2 steps = " + dtSet2.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());
                                }
                            }
                        })
                .addOnFailureListener(
                        e -> {
                        });
        return null;
    }

    public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps) {
        return getLast7DaysSteps(StepCalendar.getInstance());
    }

    @Override
    public int getRequestCode() {
        return GOOGLE_FIT_PERMISSIONS_REQUEST_CODE;
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateStepAsyncTask extends AsyncTask<String, String, Void> {

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
                updateStepCount();
            }
        }
    }

}
