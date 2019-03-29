package edu.ucsd.cse110.googlefitapp.adapter;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Calendar;
import java.util.Map;

import edu.ucsd.cse110.googlefitapp.FriendStatsActivity;
import edu.ucsd.cse110.googlefitapp.MainActivity;
import edu.ucsd.cse110.googlefitapp.fitness.FitnessService;
import edu.ucsd.cse110.googlefitapp.mock.StepCalendar;

public class FriendStatsAdapter implements FitnessService {
    public static final int ACTIVE_STEP_INDEX = 0;
    public static final int ACTIVE_MIN_INDEX = 1;
    public static final int ACTIVE_SEC_INDEX = 2;
    public static final int ACTIVE_DIST_INDEX = 3;
    public static final int ACTIVE_SPEED_INDEX = 4;
    public static Calendar calendar = MainActivity.calendar;
    private static String ACTIVE_DT_NAME = "edu.ucsd.cse110.googlefitapp.active";
    private static String APP_PACKAGE_NAME = "edu.ucsd.cse110.googlefitapp";
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private final String TAG = "FriendStatsAdapter";
    private boolean isCancelled = false;
    private FitnessOptions fitnessOptions;
    private FriendStatsActivity activity;
    private DataType activeDataType;
    private String friendEmail;
    private CollectionReference stepStorage;

    public FriendStatsAdapter(FriendStatsActivity activity, String friendEmail) {
        this.activity = activity;
        this.friendEmail = friendEmail;
    }

    public void setup() {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Firebase authentication failed, please check your internet connection");
                } else {
                    Log.e(TAG, "Authentication succeeded");
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(activity, new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            setupStepStorage();
                            getLast28DaysSteps();
                        }
                    });
                }
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

    public void getLast28DaysSteps(Calendar cal) {
        Calendar tempCal = (Calendar) cal.clone();
        tempCal.add(Calendar.DATE, -27);

        // Get Id from user list first
        FirebaseFirestore.getInstance()
                .collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String friendId = null;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String email = (String) document.getData().get("email");
                            if (email != null && email.equals(friendEmail)) {
                                friendId = (String) document.getData().get("id");
                            }
                        }

                        // Retrieve data from steps db
                        if (friendId == null) { // If friend is not in user db
                            Log.d(TAG, "friend does not exist");
                            for (int i = 0; i < 28; i++) {
                                activity.getMonthlyTotalSteps()[i] = 0;
                                activity.getMonthlyActiveSteps()[i] = 0;
                                activity.getMonthlyActiveDistance()[i] = 0;
                                activity.getMonthlyActiveSpeed()[i] = 0;
                                activity.setInActiveStepRead(i, true);
                                activity.setActiveStepRead(i, true);
                            }
                        } else { // If friend is in user db
                            Log.d(TAG, "friend exists");
                            CollectionReference activeStepDB = stepStorage.document(friendId).collection("activeStep");
                            CollectionReference totalStepDB = stepStorage.document(friendId).collection("totalStep");
                            CollectionReference userInfoDB = stepStorage.document(friendId).collection("userInfo");

                            userInfoDB.document("goal").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.getResult() == null || task.getResult().getData() == null) {
                                        activity.setFriendGoal(5000);
                                    } else {
                                        Map<String, Object> map = task.getResult().getData();
                                        Log.e(TAG, map.toString());
                                        activity.setFriendGoal((int) (long) map.get("goal"));
                                    }
                                    activity.setInActiveStepRead(28, true);
                                }
                            });

                            userInfoDB.document("strideLength").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.getResult() == null || task.getResult().getData() == null) {
                                        activity.setFriendStrideLength(0);
                                    } else {
                                        Map<String, Object> map = task.getResult().getData();
                                        Log.e(TAG, map.toString());
                                        activity.setFriendStrideLength((float) (double) map.get("strideLength"));
                                    }
                                    activity.setInActiveStepRead(29, true);
                                }
                            });

                            for (int i = 0; i < 28; i++) {
                                int year = tempCal.get(Calendar.YEAR);
                                int month = tempCal.get(Calendar.MONTH) + 1;
                                int day = tempCal.get(Calendar.DAY_OF_MONTH);
                                String dateKey = year + "." + month + "." + day;
                                final int finalI = i;
                                totalStepDB.document(dateKey).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.getResult() == null || task.getResult().getData() == null) {
                                            activity.getMonthlyTotalSteps()[finalI] = 0;
                                        } else {
                                            Map<String, Object> map = task.getResult().getData();
                                            Log.e(TAG, map.toString());
                                            activity.getMonthlyTotalSteps()[finalI] = (int) (long) map.get("totalStep");
                                        }
                                        activity.setInActiveStepRead(finalI, true);
                                    }
                                });
                                activeStepDB.document(dateKey).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.getResult() == null || task.getResult().getData() == null) {
                                            activity.getMonthlyActiveSteps()[finalI] = 0;
                                            activity.getMonthlyActiveDistance()[finalI] = 0.0f;
                                            activity.getMonthlyActiveSpeed()[finalI] = 0.0f;
                                        } else {
                                            Map<String, Object> map = task.getResult().getData();
                                            Log.e(TAG, map.toString());
                                            activity.getMonthlyActiveSteps()[finalI] = (int) (long) map.get("activeStep");
                                            activity.getMonthlyActiveDistance()[finalI] = (float) (double) map.get("distance");
                                            activity.getMonthlyActiveSpeed()[finalI] = (float) (double) map.get("speed");
                                        }
                                        activity.setActiveStepRead(finalI, true);
                                    }
                                });
                                tempCal.add(Calendar.DATE, 1);
                            }
                        }
                    }
                });
    }

    public void getLast28DaysSteps() {
        getLast28DaysSteps(StepCalendar.getInstance());
    }

    @Override
    public int getRequestCode() {
        return GOOGLE_FIT_PERMISSIONS_REQUEST_CODE;
    }

    private void setupStepStorage() {
        if (stepStorage == null) {
            stepStorage = FirebaseFirestore.getInstance()
                    .collection("steps");
        }
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
