package edu.ucsd.cse110.googlefitapp.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.ConfigClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.HistoryClient;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import edu.ucsd.cse110.googlefitapp.Activity;
import edu.ucsd.cse110.googlefitapp.LoginActivity;
import edu.ucsd.cse110.googlefitapp.MainActivity;
import edu.ucsd.cse110.googlefitapp.MyFirebaseMessagingService;
import edu.ucsd.cse110.googlefitapp.R;
import edu.ucsd.cse110.googlefitapp.dialog.UserProfileDialog;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;

import static android.content.Context.MODE_PRIVATE;
import static edu.ucsd.cse110.googlefitapp.MainActivity.KEY_GOAL;
import static edu.ucsd.cse110.googlefitapp.MainActivity.KEY_STRIDE;
import static edu.ucsd.cse110.googlefitapp.MainActivity.SHARED_PREFERENCE_NAME;
import static edu.ucsd.cse110.googlefitapp.MainActivity.SHOW_GOAL;

public class UnplannedWalkAdapter implements FitnessService {
    public static final int RC_SIGN_IN = 9001;
    private static final int ACTIVE_STEP_INDEX = 0;
    private static final int ACTIVE_MIN_INDEX = 1;
    private static final int ACTIVE_SEC_INDEX = 2;
    private static final int ACTIVE_DIST_INDEX = 3;
    private static final int ACTIVE_SPEED_INDEX = 4;
    public static Calendar calendar = MainActivity.calendar;
    private static String ACTIVE_DT_NAME = "edu.ucsd.cse110.googlefitapp.active";
    private static String APP_PACKAGE_NAME = "edu.ucsd.cse110.googlefitapp";
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private final String TAG = "UnplannedWalkAdapter";
    private boolean isCancelled = false;
    private FitnessOptions fitnessOptions;
    private Activity activity;
    private DataType activeDataType;
    private int currentStep;
    private GoogleSignInAccount gsa;
    private CollectionReference friendship;
    private CollectionReference stepStorage;
    private HistoryClient historyClient;
    private ConfigClient configClient;
    private boolean backedUp = false;
    private DateFormat simple = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");

    public UnplannedWalkAdapter(Activity activity) {
        this.activity = activity;
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastSignedInAccount == null) {
            return;
        }
        historyClient = Fitness.getHistoryClient(activity, lastSignedInAccount);
        activity.findViewById(R.id.backupBtn).setOnClickListener(v -> this.store28DaysSteps(StepCalendar.getInstance()));
    }

    public void setup() {
        buildFitnessOption();
        if (isPermissionNotGranted()) {
            startLoginActivity();
        } else if (isGSANotRetrieved()) {
            startGoogleSignIn();
        } else {
            setUpFirebase();
        }
    }

    private void setUpFirebase() {
        gsa = GoogleSignIn.getLastSignedInAccount(activity);
        assert gsa != null;
        assert gsa.getEmail() != null;
        assert gsa.getId() != null;

        configClient = Fitness.getConfigClient(activity, Objects.requireNonNull(gsa));

        Intent intent = new Intent(activity, MyFirebaseMessagingService.class);
        activity.startService(intent);

        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Firebase authentication failed, please check your internet connection");
            } else {
                Log.d(TAG, "Authentication succeeded");
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                assert currentFirebaseUser != null;
                String uid = currentFirebaseUser.getUid();
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(activity, instanceIdResult -> {
                    String newToken = instanceIdResult.getToken();
                    Log.d("newToken", newToken);
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", gsa.getEmail());
                    user.put("id", gsa.getId());
                    user.put("uid", uid);
                    user.put("token", newToken);

                    FirebaseFirestore chat = FirebaseFirestore.getInstance();

                    chat.collection("users").document(gsa.getId())
                            .set(user)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "User information successfully written!"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing User information", e));
                    if (friendship == null) {
                        setUpFriendlist();
                    }
                    setupStepStorage();

                    try {
                        Log.i(TAG, "Last Signed Account is: " + gsa);
                        Log.i(TAG, "Last Signed email is: " + gsa.getEmail());
                        Log.i(TAG, "Last Signed id is: " + gsa.getId());
                        configClient.readDataType(ACTIVE_DT_NAME).
                                addOnSuccessListener(dataType -> {
                                    Log.d(TAG, "Found data type: " + dataType);
                                    activeDataType = dataType;
                                    checkForBackup();
//                                                // Test use only
//                                                loadBackupData(StepCalendar.getInstance());
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "Datatype not found.");
                                    createCustomDataType();
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        updateStepCount();
    }

    private void startGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private boolean isGSANotRetrieved() {
        return GoogleSignIn.getLastSignedInAccount(activity) == null ||
                GoogleSignIn.getLastSignedInAccount(activity).getEmail() == null;
    }

    private void startLoginActivity() {
        Intent intent = new Intent(activity, LoginActivity.class);
        Log.d(TAG, "Start login activity");
        activity.startActivityForResult(intent, 1438);
    }

    private boolean isPermissionNotGranted() {
        return !GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions);
    }

    private void buildFitnessOption() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .build();
    }

    private void createCustomDataType() {
        DataTypeCreateRequest request = new DataTypeCreateRequest.Builder()
                .setName(ACTIVE_DT_NAME)
                .addField("ActiveSteps", Field.FORMAT_INT32)
                .addField("ActiveMin", Field.FORMAT_INT32)
                .addField("ActiveSec", Field.FORMAT_INT32)
                .addField("ActiveDistance", Field.FORMAT_FLOAT)
                .addField("ActiveSpeed", Field.FORMAT_FLOAT)
                .addField(Field.FIELD_ACTIVITY)
                .build();


        configClient.createCustomDataType(request)
                .addOnSuccessListener((DataType dataType) -> {
                    Log.d(TAG, "successfully created new datatype: " + dataType.toString());
                    activeDataType = dataType;
                    fitnessOptions = FitnessOptions.builder()
                            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                            .addDataType(activeDataType, FitnessOptions.ACCESS_READ)
                            .addDataType(activeDataType, FitnessOptions.ACCESS_WRITE)
                            .build();
                    if (isPermissionNotGranted()) {
                        Toast.makeText(activity, "Authorization is needed to use this app", Toast.LENGTH_SHORT).show();
                        GoogleSignIn.requestPermissions(
                                activity, // your activity
                                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                                GoogleSignIn.getLastSignedInAccount(activity),
                                fitnessOptions);
                    }

                    checkForBackup();
                })
                .addOnFailureListener(err -> Log.e(TAG, "There was a problem creating new datatype: " + err));
    }

    private void checkForBackup() {
        boolean backup = activity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).getBoolean("backup", false);
        if (backup) {
            updateStepCount();
            startRecording();
            startAsync();
        } else {
            loadBackupData(StepCalendar.getInstance());
        }
    }

    private void startRecording() {
        if (gsa == null) {
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
        Calendar tempCal = StepCalendar.getInstance();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        long startTime = tempCal.getTimeInMillis() / 1000 * 1000;
        tempCal.set(Calendar.SECOND, 59);
        tempCal.set(Calendar.MINUTE, 59);
        tempCal.set(Calendar.HOUR_OF_DAY, 23);
        long endTime = tempCal.getTimeInMillis() / 1000 * 1000;

        // Re-check if current client is null
        if (historyClient == null) {
            GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
            if (lastSignedInAccount == null) {
                return;
            }
            historyClient = Fitness.getHistoryClient(activity, lastSignedInAccount);
        }

        historyClient.readData(new DataReadRequest.Builder()
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
                            activity.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt("step", currentStep).apply();

                            activity.updateAll(total);
                            activity.notifyObservers();
                            activity.findViewById(R.id.spin_kit_steps_left).setVisibility(View.GONE);
                            activity.findViewById(R.id.spin_kit_steps_taken).setVisibility(View.GONE);
                            activity.findViewById(R.id.stepsLeft).setVisibility(View.VISIBLE);
                            activity.findViewById(R.id.textStepsMain).setVisibility(View.VISIBLE);
                            if (activity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).getBoolean("changing day", false)) {
                                activity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit().putBoolean("changing day", false).apply();
                                ((MainActivity) activity).setGoalChangeable(true);
                            }
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
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastSignedInAccount == null) {
            return;
        }
        new UpdateStepAsyncTask().execute(String.valueOf(2000));
    }

    @Override
    public boolean hasPermission() {
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions);
    }

    @Override
    public void addInactiveSteps(int extraStep) {
        Calendar tempCal = StepCalendar.getInstance();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        long startTime2 = tempCal.getTimeInMillis() / 1000 * 1000;

        tempCal.set(Calendar.HOUR_OF_DAY, 1);
        long startTime = tempCal.getTimeInMillis() / 1000 * 1000;
        tempCal.set(Calendar.SECOND, 59);
        tempCal.set(Calendar.MINUTE, 59);
        tempCal.set(Calendar.HOUR_OF_DAY, 22);
        long endTime = tempCal.getTimeInMillis() / 1000 * 1000;
        historyClient.readData(new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                        DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime2, endTime, TimeUnit.MILLISECONDS)
                .build())
                .addOnSuccessListener(
                        dataReadResponse -> {
                            Log.d(TAG, "Begin addInactiveSteps");
                            List<Bucket> buckets = dataReadResponse.getBuckets();
                            DataSet dataSet = buckets.get(0).getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA);
                            Log.d(TAG, Objects.requireNonNull(dataSet).toString());
                            if (dataSet.isEmpty()) {
                                Calendar cal = StepCalendar.getInstance();
                                long endTime1 = cal.getTimeInMillis() / 1000 * 1000;
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.HOUR_OF_DAY, 1);
                                long startTime1 = cal.getTimeInMillis() / 1000 * 1000;
                                if (startTime1 > endTime1) {
                                    endTime1 = startTime1 + 300000;
                                }
                                DataSource dataSource =
                                        new DataSource.Builder()
                                                .setAppPackageName(APP_PACKAGE_NAME)
                                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                                .setStreamName("PersonalBest - currentStep count")
                                                .setType(DataSource.TYPE_RAW)
                                                .build();
                                DataSet dataSet2 = DataSet.create(dataSource);
                                DataPoint dataPoint =
                                        dataSet2.createDataPoint().setTimeInterval(startTime1, endTime1, TimeUnit.MILLISECONDS);
                                dataPoint.getValue(Field.FIELD_STEPS).setInt(extraStep);
                                dataSet2.add(dataPoint);

                                Log.d(TAG, "addInactiveSteps added: " + dataSet2.toString());

                                historyClient.insertData(dataSet2).addOnCompleteListener(v -> updateStepCount());
                            } else {
                                int step = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt() + extraStep;
                                long dtStartTime = dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS);
                                long dtEndTime = dataSet.getDataPoints().get(0).getEndTime(TimeUnit.MILLISECONDS);

                                Calendar cal = StepCalendar.getInstance();
                                cal.set(Calendar.SECOND, 59);
                                cal.set(Calendar.MINUTE, 59);
                                cal.set(Calendar.HOUR_OF_DAY, 22);
                                long endTime1 = cal.getTimeInMillis() / 1000 * 1000;
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.HOUR_OF_DAY, 1);
                                long startTime1 = cal.getTimeInMillis() / 1000 * 1000;
                                if (startTime1 > endTime1) {
                                    endTime1 = startTime1 + 300000;
                                }

                                DataSource dataSource =
                                        new DataSource.Builder()
                                                .setAppPackageName(APP_PACKAGE_NAME)
                                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                                .setStreamName("PersonalBest - currentStep count")
                                                .setType(DataSource.TYPE_RAW)
                                                .build();
                                DataSet dataSet2 = DataSet.create(dataSource);
                                DataPoint dataPoint =
                                        dataSet2.createDataPoint().setTimeInterval(dtStartTime, dtEndTime, TimeUnit.MILLISECONDS);
                                dataPoint.getValue(Field.FIELD_STEPS).setInt(step);
                                dataSet2.add(dataPoint);

                                Log.d(TAG, "addInactiveSteps added: " + dataSet2.toString());

                                DataUpdateRequest dataUpdateRequest = new DataUpdateRequest.Builder()
                                        .setDataSet(dataSet2)
                                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                                        .build();

                                historyClient.updateData(dataUpdateRequest)
                                        .addOnSuccessListener(v -> {
                                            Log.d(TAG, "Total step successfully updated.");
                                            updateStepCount();
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, e.getMessage()));
                            }
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
        tempCal.set(Calendar.HOUR_OF_DAY, 2);
        long startTime2 = tempCal.getTimeInMillis() / 1000 * 1000;
        tempCal.set(Calendar.HOUR_OF_DAY, 1);
        // Get next Saturday
        tempCal.set(Calendar.SECOND, 59);
        tempCal.set(Calendar.MINUTE, 59);
        tempCal.set(Calendar.HOUR_OF_DAY, 22);
        long endTime = tempCal.getTimeInMillis() / 1000 * 1000;
        // Read active data
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);

        historyClient.readData(new DataReadRequest.Builder()
                .setTimeRange(startTime2, endTime, TimeUnit.MILLISECONDS)
                .read(activeDataType)
                .build())
                .addOnSuccessListener(
                        dataReadResponse -> {
                            DataSet dataSet = dataReadResponse.getDataSet(activeDataType);
                            Log.d(TAG, "Fetched active data from google cloud. dataSet.isEmpty() = " + dataSet.isEmpty());
                            if (dataSet.isEmpty()) {
                                Calendar cal = StepCalendar.getInstance();
                                cal.set(Calendar.SECOND, 59);
                                cal.set(Calendar.MINUTE, 59);
                                cal.set(Calendar.HOUR_OF_DAY, 22);
                                long endTime1 = cal.getTimeInMillis() / 1000 * 1000;
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.HOUR_OF_DAY, 1);
                                long startTime1 = cal.getTimeInMillis() / 1000 * 1000;

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

                                dataSet2.add(dataPoint);

                                Log.d(TAG, String.format("addActiveSteps - Added %d active steps", step));
                                Log.d(TAG, dataSet2.toString());

                                historyClient.insertData(dataSet2)
                                        .addOnSuccessListener(v -> Log.d(TAG, "Active steps successfully added."))
                                        .addOnFailureListener(v -> Log.e(TAG, "There was a problem adding active steps"));
                            } else {
                                Calendar cal = StepCalendar.getInstance();
                                cal.set(Calendar.SECOND, 59);
                                cal.set(Calendar.MINUTE, 59);
                                cal.set(Calendar.HOUR_OF_DAY, 22);
                                long endTime1 = cal.getTimeInMillis() / 1000 * 1000;
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.HOUR_OF_DAY, 1);
                                long startTime1 = cal.getTimeInMillis() / 1000 * 1000;
                                DataPoint dtPoint = dataSet.getDataPoints().get(0);
                                int newActiveStep = dtPoint.getValue(activeDataType.getFields().get(ACTIVE_STEP_INDEX)).asInt() + step;
                                int newActiveMin = dtPoint.getValue(activeDataType.getFields().get(ACTIVE_MIN_INDEX)).asInt() + min;
                                int newActiveSec = dtPoint.getValue(activeDataType.getFields().get(ACTIVE_SEC_INDEX)).asInt() + sec;
                                float newActiveDist = dtPoint.getValue(activeDataType.getFields().get(ACTIVE_DIST_INDEX)).asFloat() + step * stride / 63360.0f;
                                Log.d(TAG, "New Active dist: " + newActiveDist);
                                float newActiveSpeed = newActiveDist / (newActiveMin / 60.0f + newActiveSec / 3600.0f);
                                dataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(0)).setInt(newActiveStep);

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
                                        dataSet2.createDataPoint().setTimeInterval(startTime1, endTime1, TimeUnit.MILLISECONDS);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_STEP_INDEX)).setInt(newActiveStep);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_MIN_INDEX)).setInt(newActiveMin);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_SEC_INDEX)).setInt(newActiveSec);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_SPEED_INDEX)).setFloat(newActiveSpeed);
                                dataPoint.getValue(activeDataType.getFields().get(ACTIVE_DIST_INDEX)).setFloat(newActiveDist);
                                dataSet2.add(dataPoint);
                                Log.d(TAG, dataSet2.toString());

                                DataUpdateRequest request = new DataUpdateRequest.Builder()
                                        .setDataSet(dataSet2)
                                        .setTimeInterval(startTime1, endTime1, TimeUnit.MILLISECONDS)
                                        .build();

                                historyClient.updateData(request);
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

    private DataReadRequest buildTotalStepRequest(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        tempCal.add(Calendar.DATE, -6);
        long startTime = tempCal.getTimeInMillis() / 1000 * 1000;
        // Get next Saturday
        tempCal.add(Calendar.DATE, 7);
        tempCal.add(Calendar.SECOND, -1);
        long endTime = tempCal.getTimeInMillis() / 1000 * 1000;
        Log.d(TAG, "getLast7DaysSteps Initialize Success");
        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                        DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }


    private DataReadRequest buildActiveStepRequest(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        // Get last Sunday
        tempCal.add(Calendar.DATE, -6);
        long startTime = tempCal.getTimeInMillis() / 1000 * 1000;
        // Get next Saturday
        tempCal.add(Calendar.DATE, 7);
        tempCal.add(Calendar.SECOND, -1);
        long endTime = tempCal.getTimeInMillis() / 1000 * 1000;
        DataSource activeDataSource = new DataSource.Builder()
                .setAppPackageName(APP_PACKAGE_NAME)
                .setDataType(activeDataType)
                .setName(ACTIVE_DT_NAME)
                .setType(DataSource.TYPE_RAW)
                .build();
        Log.d(TAG, "getLast7DaysSteps Initialize Success");
        return new DataReadRequest.Builder()
                .read(activeDataType)
                .read(activeDataSource)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    private DataReadRequest build28daysTotalStepRequest(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        tempCal.add(Calendar.DATE, -27);
        long startTime = tempCal.getTimeInMillis() / 1000 * 1000;
        // Get next Saturday
        tempCal.add(Calendar.DATE, 28);
        tempCal.add(Calendar.SECOND, -1);
        long endTime = tempCal.getTimeInMillis() / 1000 * 1000;
        Log.d(TAG, "getLast7DaysSteps Initialize Success");
        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA,
                        DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    private DataReadRequest build28daysActiveStepRequest(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        // Get last Sunday
        tempCal.add(Calendar.DATE, -27);
        long startTime = tempCal.getTimeInMillis() / 1000 * 1000;
        // Get next Saturday
        tempCal.add(Calendar.DATE, 28);
        tempCal.add(Calendar.SECOND, -1);
        long endTime = tempCal.getTimeInMillis() / 1000 * 1000;
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

    private void store28DaysSteps(Calendar cal) {
        // Test use only
//        if(true) return;
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);
        DataReadRequest dataReadRequest = build28daysTotalStepRequest(cal);
        DataReadRequest dataReadRequest2 = build28daysActiveStepRequest(cal);
        storeStepAndGoal();
        storeTotalSteps(cal, gsa, dataReadRequest);
        storeActiveSteps(cal, gsa, dataReadRequest2);
    }

    private void storeStepAndGoal() {
        CollectionReference userInfoDB = stepStorage.document(getUID()).collection("userInfo");
        Map<String, Object> goalMap = new HashMap<>();
        goalMap.put("goal", activity.getGoal());
        userInfoDB.document("goal").set(goalMap);

        Map<String, Object> strideLengthMap = new HashMap<>();
        strideLengthMap.put("strideLength", activity.getStrideLength());
        userInfoDB.document("strideLength").set(strideLengthMap);

        Log.d(TAG, "Successfully store goal and stride length");
    }

    private void storeActiveSteps(Calendar cal, GoogleSignInAccount gsa, DataReadRequest dataReadRequest2) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.add(Calendar.DATE, -27);

        // Active steps data and store to firebase
        historyClient.readData(dataReadRequest2)
                .addOnSuccessListener(
                        dataReadResponse -> {
                            Log.d(TAG, "UIDDDD: " + getUID());
                            CollectionReference activeStepDB = stepStorage.document(getUID()).collection("activeStep");
                            for (int i = 0; i < 28; i++) {
                                Log.d(TAG, String.format("Active Step - dataReadResponse value at %d = " + dataReadResponse.getBuckets().get(i), i));
                                Bucket bucket = dataReadResponse.getBuckets().get(i);
                                DataSet activeStepDataSet = bucket.getDataSets().get(1);
                                Log.d(TAG, "" + (activeStepDataSet != null));
                                if (activeStepDataSet != null) {
                                    Log.d(TAG, activeStepDataSet.toString());
                                }

                                Log.d(TAG, String.format("loadBackupData - dataReadResponse value at %d = " + dataReadResponse.getBuckets().get(i), i));
                                int activeStep;
                                float distance;
                                float speed;
                                if ((activeStepDataSet != null) && !activeStepDataSet.isEmpty()) {
                                    Log.d(TAG, "loadBackupData - dtSet2 steps = " + activeStepDataSet);
                                    activeStep = activeStepDataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(ACTIVE_STEP_INDEX)).asInt();
                                    distance = activeStepDataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(ACTIVE_DIST_INDEX)).asFloat();
                                    speed = activeStepDataSet.getDataPoints().get(0).getValue(activeDataType.getFields().get(ACTIVE_SPEED_INDEX)).asFloat();
                                } else {
                                    activeStep = 0;
                                    distance = 0;
                                    speed = 0;
                                }
                                Map<String, Object> map = new HashMap<>();
                                map.put("activeStep", activeStep);
                                map.put("distance", distance);
                                map.put("speed", speed);
                                int year = tempCal.get(Calendar.YEAR);
                                int month = tempCal.get(Calendar.MONTH) + 1;
                                int day = tempCal.get(Calendar.DAY_OF_MONTH);
                                String dateKey = year + "." + month + "." + day;
                                activeStepDB.document(dateKey).set(map)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully store active step"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error store active step", e));
                                tempCal.add(Calendar.DATE, 1);
                            }
                        })
                .addOnFailureListener(
                        e -> Log.e(TAG, "Fail to get the last 28 day active steps"));
    }

    private void storeTotalSteps(Calendar cal, GoogleSignInAccount gsa, DataReadRequest dataReadRequest) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.add(Calendar.DATE, -27);

        // Total step data store to firebase
        historyClient.readData(dataReadRequest)
                .addOnSuccessListener(
                        dataReadResponse -> {
                            CollectionReference totalStepDB = stepStorage.document(getUID()).collection("totalStep");
                            Log.d(TAG, "" + dataReadResponse.getBuckets().size());
                            for (int i = 0; i < 28; i++) {
                                Log.d(TAG, String.format("Total Step - dataReadResponse value at %d = " + dataReadResponse.getBuckets().get(i), i));
                                Bucket bucket = dataReadResponse.getBuckets().get(i);
                                DataSet dtSet = bucket.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA);
                                int totalStep;
                                if (dtSet != null && !dtSet.isEmpty()) {
                                    totalStep = dtSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                    Log.d(TAG, "loadBackupData - dtSet steps = " + totalStep);
                                } else {
                                    totalStep = 0;
                                }
                                Map<String, Object> map = new HashMap<>();
                                map.put("totalStep", totalStep);
                                int year = tempCal.get(Calendar.YEAR);
                                int month = tempCal.get(Calendar.MONTH) + 1;
                                int day = tempCal.get(Calendar.DAY_OF_MONTH);
                                String dateKey = year + "." + month + "." + day;
                                totalStepDB.document(dateKey).set(map)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully store total step"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error store total step", e));
                                tempCal.add(Calendar.DATE, 1);
                            }
                        })
                .addOnFailureListener(
                        e -> Log.e(TAG, "Fail to get the last 28 day total steps"));
    }


    private DataReadRequest getLast7DaysSteps(Calendar cal) {
        final GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(activity);
        DataReadRequest dataReadRequest = buildTotalStepRequest(cal);
        DataReadRequest dataReadRequest2 = buildActiveStepRequest(cal);

        historyClient.readData(dataReadRequest)
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
                        e -> Log.e(TAG, "Fail to get the last 7 day total steps"));


        historyClient.readData(dataReadRequest2)
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
                        e -> Log.e(TAG, "Fail to get the last 7 day active steps"));
        return null;
    }

    public DataReadRequest getLast7DaysSteps() {
//        return getLast7DaysSteps(StepCalendar.getInstance());
        return getLast7DaysSteps(StepCalendar.getInstance());
    }

    @Override
    public int getRequestCode() {
        return GOOGLE_FIT_PERMISSIONS_REQUEST_CODE;
    }

    private void setUpFriendlist() {
        setFriendListListener();
        String uid = getUID();

        // requests sent by you
        List<String> userToOtherList = new ArrayList<>();
        // your friend sent you
        List<String> otherToUserList = new ArrayList<>();
        // both way
        List<String> twoWayFriendList = new ArrayList<>();

        Map<String, Integer> IDMap = new HashMap<>();


        friendship.get()
                .addOnCompleteListener(task -> {
                    int index = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "Document: " + document.getData().toString());
                        Log.d(TAG, "Document ID: " + document.getId());
                        Map<String, Object> map = document.getData();
                        if (document.getId().equals(uid)) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                if (!entry.getKey().equals("email") && (boolean) entry.getValue()) {
                                    userToOtherList.add(entry.getKey());
                                    Log.d(TAG, "User to other id: " + entry.getKey());
                                }
                            }
                        } else {
                            if (document.get(uid) != null && (boolean) document.get(uid)) {
                                otherToUserList.add(document.getId());
                                Log.d(TAG, "Other to user id: " + document.getId());
                            }
                        }

                        IDMap.put(document.getId(), index);
                        index++;
                    }

                    // Get Pure friend request from others
                    for (String userToOtherRequest : userToOtherList) {
                        otherToUserList.remove(userToOtherRequest);
                    }

                    for (String userToOtherRequest : userToOtherList) {
                        Log.d(TAG, "single friend: " + userToOtherRequest);
                        Log.d(TAG, "ID map " + IDMap);
                        if (IDMap.get(userToOtherRequest) == null) {
                            continue;
                        }
                        DocumentSnapshot friendSFriendlist = task.getResult().getDocuments().get(IDMap.get(userToOtherRequest));
                        Log.d(TAG, "friend: " + userToOtherRequest + ", friend list: " + friendSFriendlist);
                        Map<String, Object> map = friendSFriendlist.getData();
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            Log.d(TAG, "Your friend has friend " + entry.getKey());
                            if (entry.getKey().equals(uid) && (boolean) entry.getValue()) {
                                Log.d(TAG, "Luckily, you are on your friend's friend list: " + friendSFriendlist.getId());
                                twoWayFriendList.add(userToOtherRequest);
                            }
                        }
                    }

                    for (String twoWayFriend : twoWayFriendList) {
                        userToOtherList.remove(twoWayFriend);
                        otherToUserList.remove(twoWayFriend);
                    }

                    ((TextView) activity.findViewById(R.id.TextCurrentAccount)).setText(gsa.getDisplayName());
                    Log.d(TAG, "Photo URL: " + gsa.getPhotoUrl());
                    if (gsa.getPhotoUrl() != null) {
                        new DownloadImageTask(activity.findViewById(R.id.yourImage))
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(gsa.getPhotoUrl()));
                    }

                    // Set up drawer item
                    NavigationView navView = activity.findViewById(R.id.nav_view);
                    DrawerLayout drawerLayout = activity.findViewById(R.id.drawer_layout);
                    Menu m = navView.getMenu();
                    m.clear();
                    Map<String, String> emailToID = new HashMap<>();
                    boolean newFriendRequest = false;

                    // Request friend view (User to Other)
                    SubMenu reqFriendMenu = m.addSubMenu("Friend Request Sent");
                    for (String friend : userToOtherList) {
                        FirebaseFirestore.getInstance()
                                .collection("users").get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        int count = 0;
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String friendId = document.getId();
                                            if (friendId.equals(friend)) {
                                                String friendEmail = (String) task.getResult().getDocuments().get(count).get("email");
                                                emailToID.put(friendEmail, friend);
                                                MenuItem item = reqFriendMenu.add(friendEmail);
                                            }
                                            count++;
                                        }
                                    }
                                });
                    }

                    // Friend request view (Other to user)
                    SubMenu friendReqMenu = m.addSubMenu("Friend Request Received");
                    for (String friend : otherToUserList) {
                        if (IDMap.get(friend) != null) {
                            String friendEmail = (String) task.getResult().getDocuments().get(IDMap.get(friend)).get("email");
                            emailToID.put(friendEmail, friend);
                            MenuItem item = friendReqMenu.add(friendEmail);
                            newFriendRequest = true;
                        }
                    }

                    ImageView friendHint = activity.findViewById(R.id.hintFriend);
                    if (newFriendRequest) {
                        friendHint.setVisibility(View.VISIBLE);
                    } else {
                        friendHint.setVisibility(View.INVISIBLE);
                    }

                    // Friend list view
                    SubMenu friendListMenu = m.addSubMenu("Friend List");
                    for (String friend : twoWayFriendList) {
                        if (IDMap.get(friend) != null) {
                            String friendEmail = (String) task.getResult().getDocuments().get(IDMap.get(friend)).get("email");
                            emailToID.put(friendEmail, friend);
                            MenuItem item = friendListMenu.add(friendEmail);
                        }
                    }

                    navView.setNavigationItemSelectedListener(
                            new NavigationView.OnNavigationItemSelectedListener() {
                                @Override
                                public boolean onNavigationItemSelected(MenuItem menuItem) {
                                    if (otherToUserList.indexOf(emailToID.get(menuItem.getTitle())) != -1) {
                                        Log.d(TAG, "Clicked friend request!");
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
                                    } else if (twoWayFriendList.indexOf(emailToID.get(menuItem.getTitle())) != -1) {
                                        showUserProfilePrompt(getEmail(), menuItem.getTitle().toString());
                                    }

                                    // close navigation drawer
                                    // close drawer when item is tapped
                                    // TODO: if you want to use a custom dialog for chatting, then may need not close the drawer

                                    return true;
                                }

                                private void acceptFriendRequest(String friendUid, MenuItem menuItem) {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(friendUid, true);
                                    map.put("email", getEmail());

                                    FirebaseFirestore.getInstance().collection("friendship").document(getUID()).set(map, SetOptions.merge())
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "successfully added friend!");
                                                menuItem.setVisible(false);
                                            })
                                            .addOnFailureListener(e -> Log.w(TAG, "Error adding friend", e));
                                }

                                private void declineFriendRequest(String friendUid, MenuItem menuItem) {
                                    Log.d(TAG, "friend's UID: " + friendUid);
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(getUID(), false);
                                    FirebaseFirestore.getInstance().collection("friendship").document(friendUid).set(map, SetOptions.merge())
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "successfully declined friend request.");
                                                menuItem.setVisible(false);
                                            })
                                            .addOnFailureListener(e -> Log.w(TAG, "Error declining friend request", e));
                                }

                            });

                });
    }

    private void setFriendListListener() {
        if (friendship == null) {
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

    private void setupStepStorage() {
        if (stepStorage == null) {
            stepStorage = FirebaseFirestore.getInstance()
                    .collection("steps");
        }
    }

    private void showUserProfilePrompt(String userEmail, String friendEmail) {
        FragmentManager fm = activity.getSupportFragmentManager();
        UserProfileDialog setStepDialogFragment =
                UserProfileDialog.newInstance(activity.getString(R.string.user_profile), userEmail, friendEmail, activity);
        setStepDialogFragment.show(fm, "fragment_user_profile");
    }

    private void loadBackupData(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.add(Calendar.DATE, -27);
        // Get Id from user list first
        FirebaseFirestore.getInstance()
                .collection("users").get()
                .addOnCompleteListener(task -> {
                    String myEmail = getEmail();
                    String myId = null;

                    int[] monthlyActiveSteps = new int[28];
                    int[] monthlyTotalSteps = new int[28];
                    float[] monthlyActiveSpeed = new float[28];
                    float[] monthlyActiveDistance = new float[28];
                    int[] goal = new int[1];
                    float[] strideLength = new float[1];
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String email = (String) document.getData().get("email");
                        if (email != null && email.equals(myEmail)) {
                            myId = (String) document.getData().get("id");
                        }
                    }

                    // Retrieve data from steps db
                    if (myId != null) { // If friend is in user db
                        Log.d(TAG, "Backup data exists");
                        CollectionReference activeStepDB = stepStorage.document(myId).collection("activeStep");
                        CollectionReference totalStepDB = stepStorage.document(myId).collection("totalStep");
                        CollectionReference userInfoDB = stepStorage.document(myId).collection("userInfo");

                        userInfoDB.document("goal").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.getResult() == null || task.getResult().getData() == null) {
                                    goal[0] = 5000;
                                } else {
                                    Map<String, Object> map = task.getResult().getData();
                                    Log.d(TAG, map.toString());
                                    goal[0] = ((int) (long) map.get("goal"));
                                }
                                reloadGoal(goal[0]);
                            }
                        });

                        userInfoDB.document("strideLength").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.getResult() == null || task.getResult().getData() == null) {
                                    strideLength[0] = 0;
                                } else {
                                    Map<String, Object> map = task.getResult().getData();
                                    Log.d(TAG, map.toString());
                                    strideLength[0] = ((float) (double) map.get("strideLength"));
                                }
                                reloadStrideLength(strideLength[0]);
                            }
                        });

                        for (int i = 0; i < 28; i++) {
                            int year = tempCal.get(Calendar.YEAR);
                            int month = tempCal.get(Calendar.MONTH) + 1;
                            int day = tempCal.get(Calendar.DAY_OF_MONTH);
                            String dateKey = year + "." + month + "." + day;
                            final int finalI = i;
                            Calendar queryCal = (Calendar) tempCal.clone();
                            totalStepDB.document(dateKey).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.getResult() == null || task.getResult().getData() == null) {
                                        monthlyTotalSteps[finalI] = 0;
                                    } else {
                                        Map<String, Object> map = task.getResult().getData();
                                        Log.d(TAG, map.toString());
                                        monthlyTotalSteps[finalI] = (int) (long) map.get("totalStep");
                                    }
                                    queryCal.set(Calendar.SECOND, 0);
                                    queryCal.set(Calendar.MINUTE, 0);
                                    queryCal.set(Calendar.HOUR_OF_DAY, 1);
                                    // Get last Sunday
                                    long startTime = queryCal.getTimeInMillis() / 1000 * 1000;
                                    // Get next Saturday
                                    queryCal.set(Calendar.SECOND, 59);
                                    queryCal.set(Calendar.MINUTE, 59);
                                    queryCal.set(Calendar.HOUR_OF_DAY, 23);
                                    long endTime = queryCal.getTimeInMillis() / 1000 * 1000;
                                    int totalStep = monthlyTotalSteps[finalI];
                                    reloadTotalSteps(startTime, endTime, totalStep);
                                }
                            });
                            activeStepDB.document(dateKey).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.getResult() == null || task.getResult().getData() == null) {
                                        monthlyActiveSteps[finalI] = 0;
                                        monthlyActiveDistance[finalI] = 0.0f;
                                        monthlyActiveSpeed[finalI] = 0.0f;
                                    } else {
                                        Map<String, Object> map = task.getResult().getData();
                                        Log.d(TAG, map.toString());
                                        monthlyActiveSteps[finalI] = (int) (long) map.get("activeStep");
                                        monthlyActiveDistance[finalI] = (float) (double) map.get("distance");
                                        monthlyActiveSpeed[finalI] = (float) (double) map.get("speed");
                                    }
                                    queryCal.set(Calendar.SECOND, 0);
                                    queryCal.set(Calendar.MINUTE, 0);
                                    queryCal.set(Calendar.HOUR_OF_DAY, 1);
                                    // Get last Sunday
                                    long startTime = queryCal.getTimeInMillis() / 1000 * 1000;
                                    // Get next Saturday
                                    queryCal.set(Calendar.SECOND, 59);
                                    queryCal.set(Calendar.MINUTE, 59);
                                    queryCal.set(Calendar.HOUR_OF_DAY, 23);
                                    long endTime = queryCal.getTimeInMillis() / 1000 * 1000;
                                    int activeStep = monthlyActiveSteps[finalI];
                                    float distance = monthlyActiveDistance[finalI];
                                    float speed = monthlyActiveSpeed[finalI];
                                    float duration = distance / speed;
                                    reloadActiveSteps(startTime, endTime, activeStep, distance, speed, duration);
                                }
                            });
                            tempCal.add(Calendar.DATE, 1);
                        }
                    }
                });
        activity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit().putBoolean("backup", true).apply();
        updateStepCount();
        startRecording();
        startAsync();
    }

    private void reloadTotalSteps(long startTime, long endTime, int totalStep) {
        DataSource dataSource =
                new DataSource.Builder()
                        .setAppPackageName(APP_PACKAGE_NAME)
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setStreamName("PersonalBest - currentStep count")
                        .setType(DataSource.TYPE_RAW)
                        .build();
        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint =
                dataSet.createDataPoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(totalStep);
        dataSet.add(dataPoint);

        Log.i(TAG, String.format("Reloading total steps from %s to %s", simple.format(new Date(startTime)), simple.format(new Date(endTime))));
        Log.i(TAG, String.format("Reloading total steps from %d to %d", startTime, endTime));
        Log.i(TAG, "Reloading total dataSet: " + dataSet);

        historyClient.insertData(dataSet);
    }

    private void reloadActiveSteps(long startTime, long endTime, int activeStep, float distance, float speed, float duration) {
        DataSource dataSource =
                new DataSource.Builder()
                        .setAppPackageName(APP_PACKAGE_NAME)
                        .setDataType(activeDataType)
                        .setStreamName(TAG + " - active currentStep")
                        .setType(DataSource.TYPE_RAW)
                        .build();
        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint =
                dataSet.createDataPoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(activeDataType.getFields().get(ACTIVE_STEP_INDEX)).setInt(activeStep);
        dataPoint.getValue(activeDataType.getFields().get(ACTIVE_MIN_INDEX)).setInt(((int) duration / 60));
        dataPoint.getValue(activeDataType.getFields().get(ACTIVE_SEC_INDEX)).setInt(((int) duration) % 60);
        dataPoint.getValue(activeDataType.getFields().get(ACTIVE_SPEED_INDEX)).setFloat(speed);
        dataPoint.getValue(activeDataType.getFields().get(ACTIVE_DIST_INDEX)).setFloat(distance);
        dataSet.add(dataPoint);

        DateFormat simple = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");

        Log.i(TAG, String.format("Reloading active steps from %s to %s", simple.format(new Date(startTime)), simple.format(new Date(endTime))));

        historyClient.insertData(dataSet);
    }

    private void reloadStrideLength(float sl) {
        activity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit().putFloat(KEY_STRIDE, sl).apply();
    }

    private void reloadGoal(int goal) {
        activity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).edit().putInt(KEY_GOAL, goal).apply();
        TextView goalText = activity.findViewById(R.id.textGoal);
        goalText.setText(String.format(SHOW_GOAL, goal));

        // Save new goal
        ((MainActivity) activity).setGoal(goal);
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
                if (Calendar.getInstance().get(Calendar.MINUTE) % 30 == 0
                        && !backedUp && activeDataType != null
                        && activity.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE).getBoolean("backup", false)) {
                    store28DaysSteps(StepCalendar.getInstance());
                    backedUp = true;
                } else if (Calendar.getInstance().get(Calendar.MINUTE) % 30 != 0) {
                    backedUp = false;
                }
            }
        }
    }

    /**
     * Credit given to https://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
     * Example Use:
     * new DownloadImageTask((ImageView) activity.findViewById(R.id.yourImage))
     * .execute(someURL);
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView profileImage;

        DownloadImageTask(ImageView profileImage) {
            this.profileImage = profileImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap icon = null;
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                icon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return icon;
        }

        protected void onPostExecute(Bitmap result) {
            profileImage.setImageBitmap(result);
        }
    }
}
