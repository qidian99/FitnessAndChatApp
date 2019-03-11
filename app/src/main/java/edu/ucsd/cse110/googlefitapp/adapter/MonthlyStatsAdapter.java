package edu.ucsd.cse110.googlefitapp.adapter;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.googlefitapp.MainActivity;
import edu.ucsd.cse110.googlefitapp.MonthlyStatsActivity;
import edu.ucsd.cse110.googlefitapp.MonthlyStatsActivity;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;

public class MonthlyStatsAdapter implements FitnessService {
    public static final int ACTIVE_STEP_INDEX = 0;
    public static final int ACTIVE_MIN_INDEX = 1;
    public static final int ACTIVE_SEC_INDEX = 2;
    public static final int ACTIVE_DIST_INDEX = 3;
    public static final int ACTIVE_SPEED_INDEX = 4;

    private static String ACTIVE_DT_NAME = "edu.ucsd.cse110.googlefitapp.active";
    private static String APP_PACKAGE_NAME = "edu.ucsd.cse110.googlefitapp";
    public static Calendar calendar = MainActivity.calendar;
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private final String TAG = "MonthlyStatsAdapter";
    private boolean isCancelled = false;
    private FitnessOptions fitnessOptions;
    private MonthlyStatsActivity activity;
    private DataType activeDataType;

    public MonthlyStatsAdapter(MonthlyStatsActivity activity) {
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
        } else {
            try {
                GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);

                Fitness.getConfigClient(activity, Objects.requireNonNull(gsa)).readDataType(ACTIVE_DT_NAME).
                        addOnSuccessListener(dataType -> {
                            Log.d(TAG, "Found data type: " + dataType);
                            activeDataType = dataType;
                            getLast28DaysSteps(activity.getMonthlyTotalSteps(), activity.getMonthlyActiveSteps());

                        })
                        .addOnFailureListener(e -> {
                            Log.d(TAG, "Datatype not found.");
                            CreateCustomDataType(gsa);
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                            Log.d(TAG, "Sucessfully created new datatype: " + dataType.toString());
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
                            getLast28DaysSteps(activity.getMonthlyTotalSteps(), activity.getMonthlyActiveSteps());
                        })
                        .addOnFailureListener(err -> Log.e(TAG, "There was a problem creating new datatype: " + err));
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
    public void updateStepCount() {

    }

    @Override
    public void addInactiveSteps(int extraStep) {

    }

    @Override
    public void addActiveSteps(final int step, final int min, final int sec, final float stride) {

    }

    @Override
    public String getUID() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    public DataReadRequest buildTotalStepRequest(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        // Get last Sunday
        tempCal.add(Calendar.DATE, -27);
        long startTime = tempCal.getTimeInMillis();
        // Get next Saturday
        tempCal.add(Calendar.DATE, 28);
        tempCal.add(Calendar.SECOND, -1);
        long endTime = tempCal.getTimeInMillis();
        Log.d(TAG, "TotalStepRequest Initialize Success");
        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                        DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    public DataReadRequest buildActiveStepRequest(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        // Get last Sunday
        tempCal.add(Calendar.DATE, -6);
        long startTime = tempCal.getTimeInMillis();
        // Get next Saturday
        tempCal.add(Calendar.DATE, 28);
        tempCal.add(Calendar.SECOND, -1);
        long endTime = tempCal.getTimeInMillis();
        DataSource activeDataSource = new DataSource.Builder()
                .setAppPackageName(APP_PACKAGE_NAME)
                .setDataType(activeDataType)
                .setName(ACTIVE_DT_NAME)
                .setType(DataSource.TYPE_RAW)
                .build();
        Log.d(TAG, "ActiveStepRequest Initialize Success");
        return new DataReadRequest.Builder()
                .read(activeDataType)
                .read(activeDataSource)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    public DataReadRequest getLast28DaysSteps(Calendar cal) {
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);
        DataReadRequest dataReadRequest = buildTotalStepRequest(cal);
        DataReadRequest dataReadRequest2 = buildActiveStepRequest(cal);

        // Total step data read response
        Fitness.getHistoryClient(activity, Objects.requireNonNull(gsa))
                .readData(dataReadRequest)
                .addOnSuccessListener(
                        dataReadResponse -> {
                            Log.d(TAG, "" + dataReadResponse.getBuckets().size());
                            for (int i = 0; i < 28; i++) {
                                Log.d(TAG, String.format("Total Step - dataReadResponse value at %d = " + dataReadResponse.getBuckets().get(i), i));
                                Bucket bucket = dataReadResponse.getBuckets().get(i);
                                DataSet dtSet = bucket.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA);
                                if (dtSet != null && !dtSet.isEmpty()) {
                                    int totalStep = dtSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                    Log.d(TAG, "getLast28DaysSteps - dtSet steps = " + totalStep);
                                    activity.getMonthlyTotalSteps()[i] = totalStep;
                                } else {
                                    activity.getMonthlyTotalSteps()[i] = 0;
                                }
                                activity.setInActiveStepRead(true);
                            }
                        })
                .addOnFailureListener(
                        e -> Log.e(TAG, "Fail to get the last 28 day total steps"));

        // Active steps data read response
        Fitness.getHistoryClient(activity, Objects.requireNonNull(gsa))
                .readData(dataReadRequest2)
                .addOnSuccessListener(
                        dataReadResponse -> {
                            for (int i = 0; i < 28; i++) {
                                Log.d(TAG, String.format("Active Step - dataReadResponse value at %d = " + dataReadResponse.getBuckets().get(i), i));
                                Bucket bucket = dataReadResponse.getBuckets().get(i);
                                DataSet activeStepDataSet = bucket.getDataSets().get(1);
                                Log.d(TAG, "" + (activeStepDataSet != null));
                                Log.d(TAG, activeStepDataSet.toString());

                                Log.d(TAG, String.format("getLast28DaysSteps - dataReadResponse value at %d = " + dataReadResponse.getBuckets().get(i), i));

                                if (activeStepDataSet != null && !activeStepDataSet.isEmpty()) {
                                    Log.d(TAG, "getLast28DaysSteps - dtSet2 steps = " + activeStepDataSet);
                                    activity.getMonthlyActiveSteps()[i] = activeStepDataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(ACTIVE_STEP_INDEX)).asInt();
                                    activity.getMonthlyActiveDistance()[i] = activeStepDataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(ACTIVE_DIST_INDEX)).asFloat();
                                    activity.getMonthlyActiveSpeed()[i] = activeStepDataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(ACTIVE_SPEED_INDEX)).asFloat();
                                } else {
                                    activity.getMonthlyActiveSteps()[i] = 0;
                                    activity.getMonthlyActiveDistance()[i] = 0;
                                    activity.getMonthlyActiveSpeed()[i] = 0;
                                }
                                activity.setActiveStepRead(true);
                            }
                        })
                .addOnFailureListener(
                        e -> {Log.e(TAG, "Fail to get the last 28 day active steps");
                        });
        return null;
    }

    public DataReadRequest getLast28DaysSteps(int[] MonthlyInactiveSteps, int[] MonthlyActiveSteps) {
        return getLast28DaysSteps(StepCalendar.getInstance());
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
