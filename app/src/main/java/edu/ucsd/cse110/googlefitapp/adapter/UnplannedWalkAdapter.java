package edu.ucsd.cse110.googlefitapp.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.googlefitapp.Activity;
import edu.ucsd.cse110.googlefitapp.FriendChatActivity;
import edu.ucsd.cse110.googlefitapp.LoginActivity;
import edu.ucsd.cse110.googlefitapp.MainActivity;
import edu.ucsd.cse110.googlefitapp.R;
import edu.ucsd.cse110.googlefitapp.chatroom.models.ChatPojo;
import edu.ucsd.cse110.googlefitapp.chatroom.utils.MyUtils;
import edu.ucsd.cse110.googlefitapp.chatroom.views.ChatActivity;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;

import static android.media.CamcorderProfile.get;
import static android.view.View.INVISIBLE;

public class UnplannedWalkAdapter implements FitnessService {
    /*           .addField("ActiveSteps", Field.FORMAT_INT32)
                .addField("ActiveMin", Field.FORMAT_INT32)
                .addField("ActiveSec", Field.FORMAT_INT32)
                .addField("ActiveDistance", Field.FORMAT_FLOAT)
                .addField("ActiveSpeed", Field.FORMAT_FLOAT)
                */
    public static final int ACTIVE_STEP_INDEX = 0;
    public static final int ACTIVE_MIN_INDEX = 1;
    public static final int ACTIVE_SEC_INDEX = 2;
    public static final int ACTIVE_DIST_INDEX = 3;
    public static final int ACTIVE_SPEED_INDEX = 4;

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
    private GoogleSignInAccount gsa;
    public static final int RC_SIGN_IN = 9001;
    private CollectionReference friendship;

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

//        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)) {
//            Toast.makeText(activity, "Authorization is needed to use this app", Toast.LENGTH_SHORT).show();
////            GoogleSignIn.requestPermissions(
////                    activity, // your activity
////                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
////                    GoogleSignIn.getLastSignedInAccount(activity),
////                    fitnessOptions);
//            Intent intent = new Intent(activity, LoginActivity.class);
//            activity.startActivity(intent);
////            activity.finish();
//        } else {



        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)) {
            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
        } else if(GoogleSignIn.getLastSignedInAccount(activity).getEmail() == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);

        } else {
            updateStepCount();
            startRecording();


            try {
                gsa = GoogleSignIn.getLastSignedInAccount(activity);
//                ((TextView) activity.findViewById(R.id.TextCurrentAccount)).setText(gsa.getEmail());
                Log.i(TAG, "Last Signed Account is: " + gsa);
                Log.i(TAG, "Last Signed email is: " + gsa.getEmail());
                Log.i(TAG, "Last Signed id is: " + gsa.getId());
                Fitness.getConfigClient(activity, Objects.requireNonNull(gsa)).readDataType(ACTIVE_DT_NAME).
                        addOnSuccessListener(dataType -> {
                            Log.d(TAG, "Found data type: " + dataType);
                            activeDataType = dataType;
                            //                        CreateCustomDataType(gsa);
                        })
                        .addOnFailureListener(e -> {
                            Log.d(TAG, "Datatype not found.");

                            CreateCustomDataType(gsa);
                        });
                //            Fitness.getConfigClient(activity, Objects.requireNonNull(gsa)).readDataType(this, "com.app.custom_data_type");


            } catch (Exception e) {
                e.printStackTrace();
            }
            startAsync();

            Map<String, Object> user = new HashMap<>();
            user.put("email", gsa.getEmail());
            user.put("id", gsa.getId());

            FirebaseFirestore chat = FirebaseFirestore.getInstance();
//                    .collection(activity.COLLECTION_KEY)
//                    .document(activity.DOCUMENT_KEY)
//                    .collection(activity.MESSAGES_KEY);

            chat.collection("users").document(gsa.getId())
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "User information successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing User information", e);
                        }
                    });
            if(friendship == null) {
                setUpFriendlist();
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
                            activity.findViewById(R.id.spin_kit_steps_left).setVisibility(View.GONE);
                            activity.findViewById(R.id.spin_kit_steps_taken).setVisibility(View.GONE);
                            activity.findViewById(R.id.stepsLeft).setVisibility(View.VISIBLE);
                            activity.findViewById(R.id.textStepsMain).setVisibility(View.VISIBLE);

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
    public void addActiveSteps(final int step, final int min, final int sec, final float stride) {
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
                            Log.d(TAG, "Fetched active data from google cloud. dataSet.isEmpty() = " + dataSet.isEmpty());
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
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_STEP_INDEX)).setInt(step);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_MIN_INDEX)).setInt(min);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_SEC_INDEX)).setInt(sec);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_SPEED_INDEX)).setFloat(step * stride / 63360.0f / (min / 60.0f + sec / 3600.0f));
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_DIST_INDEX)).setFloat(step * stride / 63360.0f);

//                                    currentStep = stepCountDelta;
                                dataSet2.add(dataPoint);

                                Log.d(TAG, String.format("addActiveSteps - Added %d active steps", step));
                                Log.d(TAG, dataSet2.toString());

                                Task<Void> response = Fitness.getHistoryClient(activity, gsa).insertData(dataSet2);
                            } else {
                                /*
                                        int totalActiveSteps = stepPref.getInt(String.valueOf(day + 7), 0) + activeSteps;
                                        SharedPreferences.Editor editor = stepPref.edit();
                                        editor.putInt(String.valueOf(day + 7), totalActiveSteps);
                                        editor.apply();

                                        // update avg speed and total distance
                                        float currActiveSpeed = statsPref.getFloat(String.valueOf(day), 0.0f);
                                        float totalActiveDist = totalActiveSteps * strideLength / 63360.0f;
                                        Log.d(TAG, "Today's total active distance: " + totalActiveDist);

                                        SharedPreferences.Editor statsEditor = statsPref.edit();
                                        statsEditor.putFloat(String.valueOf(day), (currActiveSpeed + activeSpeed) / 2.0f);
                                        Log.d(TAG, "Today's average active speed: " + (currActiveSpeed + activeSpeed) / 2.0f);
                                 */
                                DataPoint dtPoint = dataSet.getDataPoints().get(0);
                                int newActiveStep = dtPoint.getValue(activeDataType.getFields().get(ACTIVE_STEP_INDEX)).asInt() + step;
                                int newActiveMin = dtPoint.getValue(activeDataType.getFields().get(ACTIVE_MIN_INDEX)).asInt() + min;
                                int newActiveSec = dtPoint.getValue(activeDataType.getFields().get(ACTIVE_SEC_INDEX)).asInt() + sec;
                                float newActiveDist = dtPoint.getValue(activeDataType.getFields().get(ACTIVE_DIST_INDEX)).asFloat() + step * stride / 63360.0f;
                                Log.d(TAG, "New Active dist: " + newActiveDist);
                                float newActiveSpeed = newActiveDist / (newActiveMin / 60.0f + newActiveSec / 3600.0f);
                                dataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(0)).setInt(newActiveStep);
                                Log.d(TAG, "Total active steps in addActiveSteps: " + dataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(0)).asInt());

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
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_STEP_INDEX)).setInt(newActiveStep);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_MIN_INDEX)).setInt(newActiveMin);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_SEC_INDEX)).setInt(newActiveSec);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_SPEED_INDEX)).setFloat(newActiveSpeed);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_DIST_INDEX)).setFloat(newActiveDist);
                                dataSet2.add(dataPoint);
                                Log.d(TAG, dataSet2.toString());

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

    @Override
    public String getUID() {
        return this.gsa.getId();
    }

    @Override
    public String getEmail() {
        return this.gsa.getEmail();
    }

    public DataReadRequest buildTotalStepRequest(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR, 0);
        // Get last Sunday
//        tempCal.add(Calendar.DATE, -tempCal.get(Calendar.DAY_OF_WEEK) + 1);
        tempCal.add(Calendar.DATE, -6);
        long startTime = tempCal.getTimeInMillis();
        // Get next Saturday
        tempCal.add(Calendar.DATE, 7);
        tempCal.add(Calendar.SECOND, -1);
        long endTime = tempCal.getTimeInMillis();
//        DataSource activeDataSource = new DataSource.Builder()
//                .setAppPackageName(APP_PACKAGE_NAME)
//                .setDataType(activeDataType)
//                .setName(ACTIVE_DT_NAME)
//                .setType(DataSource.TYPE_RAW)
//                .build();
        Log.d(TAG, "getLast7DaysSteps Initialize Success");
        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                        DataType.AGGREGATE_STEP_COUNT_DELTA)
//                .read(activeDataType)
//                .read(activeDataSource)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    public DataReadRequest buildActiveStepRequest(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR, 0);
        // Get last Sunday
        tempCal.add(Calendar.DATE, -6);
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
//                .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
//                        DataType.AGGREGATE_STEP_COUNT_DELTA)
                .read(activeDataType)
                .read(activeDataSource)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps, Calendar cal) {
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);
        DataReadRequest dataReadRequest = buildTotalStepRequest(cal);
        DataReadRequest dataReadRequest2 = buildActiveStepRequest(cal);

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
                        e -> {Log.e(TAG, "Fail to get the last 7 day total steps");
                        });


        Task<DataReadResponse> dataReadResponseTask2 = Fitness.getHistoryClient(activity, Objects.requireNonNull(gsa))
                .readData(dataReadRequest2)
                .addOnSuccessListener(
                        dataReadResponse -> {
                            for (int i = 0; i < 7; i++) {
                                Log.d(TAG, String.format("getLast7DaysSteps - dataReadResponse value at %d = " + dataReadResponse.getBuckets().get(i), i));
                                Bucket bucket = dataReadResponse.getBuckets().get(i);
                                DataSet dtSet2 = bucket.getDataSet(activeDataType);
                                if (dtSet2 != null && !dtSet2.isEmpty()) {
                                    Log.d(TAG, "getLast7DaysSteps - dtSet2 steps = " + dtSet2);
                                }
                            }
                        })
                .addOnFailureListener(
                        e -> {Log.e(TAG, "Fail to get the last 7 day active steps");
                        });
        return null;
    }

    public DataReadRequest getLast7DaysSteps(double[] weeklyInactiveSteps, double[] weeklyActiveSteps) {
//        return getLast7DaysSteps(StepCalendar.getInstance());
        return getLast7DaysSteps(weeklyInactiveSteps, weeklyActiveSteps, StepCalendar.getInstance());
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
//                setUpFriendlist();
            }
        }
    }


    private void setUpFriendlist() {
        setFriendListListener();
        String uid = getUID();
//        DocumentReference friendship = FirebaseFirestore.getInstance()
//                .collection("friendship")
//                .document(uid);
//        friendship.get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            List<String> singleWayFriendList = new ArrayList<>();
//                            Map<String, Object> map = task.getResult().getData();
//                            for (Map.Entry<String, Object> entry : map.entrySet()) {
//                                singleWayFriendList.add(entry.getKey());
//                                Log.e("TAG", "Your friend request sent: " + entry.getKey());
//                            }
//                            //Do what you want to do with your list
//                        } else {
//                            Log.e(TAG, "Error getting documents: ", task.getException());
//                        }
//                    }
//
//                });
        // requests sent by you
        List<String> singleWayFriendList = new ArrayList<>();
        // both way
        List<String> twoWayFriendList = new ArrayList<>();
        // your friend sent you
        List<String> friendRequestList = new ArrayList<>();

        Map<String, Integer> IDMap = new HashMap<>();


        friendship.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int index = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Document: " + document.getData().toString());
                            Log.d(TAG, "Document ID: " + document.getId());
                            Map<String, Object> map = document.getData();
                            if(document.getId().equals(uid)){
                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    if(!entry.getKey().equals("email") && (boolean)entry.getValue()) {
                                        singleWayFriendList.add(entry.getKey());
                                        Log.e("TAG", "Your friend request sent: " + entry.getKey());
                                    }
                                }
                            } else {
                                if(document.get(uid) != null && (boolean)document.get(uid)){
                                    friendRequestList.add(document.getId());
                                }
                            }

                            IDMap.put(document.getId(), index);
                            index++;
                        }

                        for(String yourFriendRequest: singleWayFriendList) {
                            friendRequestList.remove(yourFriendRequest);
                        }

                        //if(task.getResult().getDocuments())
                        for(String singleFriend : singleWayFriendList){
                            Log.e(TAG, ""+(task.getResult().getDocuments()==null));
                            Log.e(TAG, "single friend"+singleFriend);
                            Log.e(TAG, "ID map" + IDMap);
                            if(IDMap.get(singleFriend) == null) {
                                continue;
                            }
                            DocumentSnapshot friendSFriendlist = task.getResult().getDocuments().get(IDMap.get(singleFriend));
                            Log.e(TAG, "friend: " + singleFriend + ", friend list: " + friendSFriendlist);
                            Map<String, Object> map = friendSFriendlist.getData();
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                Log.e(TAG, "Your friend has friend " + entry.getKey());
                                if(entry.getKey().equals(uid) && (boolean)entry.getValue()) {
                                    Log.e("TAG", "Luckily, you are on your friend's friend list: " + friendSFriendlist.getId());
                                    twoWayFriendList.add(singleFriend);
                                }
                            }

                        }
                        ((TextView) activity.findViewById(R.id.TextCurrentAccount)).setText(gsa.getDisplayName());
                        Log.d(TAG, "Photo URL: " + gsa.getPhotoUrl());
                        if(gsa.getPhotoUrl() != null) {
                            new DownloadImageTask((ImageView) activity.findViewById(R.id.yourImage))
                                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(gsa.getPhotoUrl()));
                        }
//                        ((ImageView) activity.findViewById(R.id.yourImage)).setImageURI(gsa.getPhotoUrl());
                        // Set up drawer item
                        NavigationView navView = (NavigationView) activity.findViewById(R.id.nav_view);
                        DrawerLayout drawerLayout = activity.findViewById(R.id.drawer_layout);
                        Menu m = navView.getMenu();
                        m.clear();
                        Map<String, String> emailToID = new HashMap<>();
                        boolean newFriendRequest = false;
                        SubMenu friendReqMenu = m.addSubMenu("Friend Request");
                        for(String friend : friendRequestList) {
                            String friendEmail = (String) task.getResult().getDocuments().get(IDMap.get(friend)).get("email");
                            emailToID.put(friendEmail, friend);
                            MenuItem item = friendReqMenu.add(friendEmail);
                            newFriendRequest = true;
                        }
                        ImageView friendHint = activity.findViewById(R.id.hintFriend);
                        if(newFriendRequest){
                            friendHint.setVisibility(View.VISIBLE);
                        } else {
                            friendHint.setVisibility(View.INVISIBLE);
                        }
                        SubMenu friendListMenu = m.addSubMenu("Friend List");
                        for(String friend : twoWayFriendList) {
                            String friendEmail = (String) task.getResult().getDocuments().get(IDMap.get(friend)).get("email");
                            emailToID.put(friendEmail, friend);
                            MenuItem item = friendListMenu.add(friendEmail);
                        }

                        navView.setNavigationItemSelectedListener(
                                new NavigationView.OnNavigationItemSelectedListener() {
                                    @Override
                                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                                        Log.e(TAG, "Menu Item selected 2: " + menuItem.getTitle() + "," + emailToID.get(menuItem.getTitle()) + ","+ singleWayFriendList.indexOf(emailToID.get(menuItem.getTitle())));
                                        if(singleWayFriendList.indexOf(emailToID.get(menuItem.getTitle()))==-1){
                                            Log.e(TAG, "Clicked friend request!");
                                            // Dialog to accept / decline
                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                                            builder1.setMessage("Accept the friend request?");
                                            builder1.setCancelable(false);

                                            builder1.setPositiveButton(
                                                    "Accept",
                                                    (dialog, id) -> {
                                                        dialog.cancel();
                                                        acceptFriendRequest(emailToID.get(menuItem.getTitle()), menuItem);
                                                    }).setNegativeButton("Decline",
                                                    (dialog, id) -> {
                                                        dialog.cancel();
                                                        declineFriendRequest(emailToID.get(menuItem.getTitle()), menuItem);
                                                    });

                                            AlertDialog alertInvalidInput = builder1.create();
                                            alertInvalidInput.show();
                                        } else {
                                            // open Chat
                                            String friendEmail = menuItem.getTitle().toString();
                                            String userEmail = getEmail();
                                            String chatroomName = userEmail.compareTo(friendEmail) > 0 ? friendEmail + "TO" + userEmail : userEmail + "TO" + friendEmail;
                                            FirebaseAuth.getInstance().signInAnonymously()
                                                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete( Task<AuthResult> task) {
                                                            if (!task.isSuccessful()) {
                                                                Log.e(TAG, "Error connecting to chat room");
                                                            } else {
                                                                Intent intent=new Intent(activity, ChatActivity.class);
                                                                intent.putExtra(MyUtils.EXTRA_ROOM_NAME, chatroomName);
                                                                intent.putExtra("friend", friendEmail);
                                                                intent.putExtra("from", userEmail);
                                                                intent.putExtra("to", friendEmail);
                                                                activity.startActivity(intent);                                                            }
                                                        }
                                                    });

//                                            Intent intent = new Intent(activity, FriendChatActivity.class);
//                                            activity.startActivity(intent);
//                                            drawerLayout.closeDrawers();

                                        }

                                        //close navigation drawer
                                        // close drawer when item is tapped
                                        // TODO: if you want to use a custom dialog for chatting, then may need not close the drawer

                                        return true;
                                    }

                                    private void acceptFriendRequest(String friendUid, MenuItem menuItem) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put(friendUid, true);
                                        map.put("email", getEmail());

                                        FirebaseFirestore.getInstance().collection("friendship").document(getUID()).set(map, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "Sucessfully added friend!");
                                                        menuItem.setVisible(false);
//                                                        setUpFriendlist();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error adding friend", e);
                                                    }
                                                });
                                    }

                                    private void declineFriendRequest(String friendUid, MenuItem menuItem) {
                                        Log.e(TAG, "friend's UID: " + friendUid);
                                        Map<String, Object> map = new HashMap<>();
                                        map.put(getUID(), false);
                                        FirebaseFirestore.getInstance().collection("friendship").document(friendUid).set(map, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "Sucessfully declined friend request.");
                                                        menuItem.setVisible(false);
//                                                        setUpFriendlist();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error declining friend request", e);
                                                    }
                                                });
                                    }

                                });

                    }
                });
    }

    private void setFriendListListener() {
        if(friendship == null) {
            friendship = FirebaseFirestore.getInstance()
                    .collection("friendship");
            friendship.addSnapshotListener((newChatSnapShot, error) -> {
                if (error != null) {
                    Log.e(TAG, error.getLocalizedMessage());
                    return;
                }

                setUpFriendlist();
            });
        }
    }

    /**
     * Credit given to https://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
     Example Use:
     new DownloadImageTask((ImageView) activity.findViewById(R.id.yourImage))
     .execute(someURL);
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
