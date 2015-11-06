package be.ac.ulb.iridia.empaticadatastream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends ActionBarActivity implements EmpaDataDelegate, EmpaStatusDelegate,
        CountDownTimerPicker.CountDownTimerDialogListener {

    private static final String TAG = "MainActivity";

    private String mCurrentUser;
    private String mCurrentSession = "session_1";
    private int mLastSessionID = 1;

    private EmpaDeviceManager mDeviceManager;
    private boolean mWristbandConnected = false;
    private static final int REQUEST_ENABLE_BT = 1;

    private Button mDisconnect;
    private Toolbar mToolbar;
    private TextView mWarningTextView;
    private boolean mWarningTextViewDisplayed = false;
    private float mLastBatteryLevel = -1;

    private ProgressBar mConnectingProgressBar;
    private CardView mPhysioValueCV;
    private CardView mCounterCV;
    private CardView mHistoryCV;

    private TextView mTempTextView;
    private TextView mEDATextView;
    private TextView mHRTextView;
    private TextView mRecordingSessionStatusTextView;

    private Button mBaselineButton;
    private Button mSessionButton;
    private Button mTagButton;
    private TextView mBaselineCounterTextView;
    private TextView mSessionCounterTextView;
    private TextView mTagSessionCounterTextView;

    private LinearLayout mHistoryLinearLayout;

    private Float mLastEDA = 0.0f;
    private Float mLastTemp = 0.0f;
    private Float mLastHR = 0.0f;

    private Double mFirstRecordedIBITimestamp = -1.0;
    private ArrayList<PhysioData> mBVPBaselineList = new ArrayList<>();
    private ArrayList<PhysioData> mTempBaselineList = new ArrayList<>();
    private ArrayList<PhysioData> mEDABaselineList = new ArrayList<>();
    private ArrayList<PhysioData> mHRBaselineList = new ArrayList<>();
    private ArrayList<PhysioData> mIBIBaselineList = new ArrayList<>();

    private ArrayList<PhysioData> mBVPSessionList = new ArrayList<>();
    private ArrayList<PhysioData> mTempSessionList = new ArrayList<>();
    private ArrayList<PhysioData> mEDASessionList = new ArrayList<>();
    private ArrayList<PhysioData> mHRSessionList = new ArrayList<>();
    private ArrayList<PhysioData> mIBISessionList = new ArrayList<>();

    private ArrayList<Pair<Long, String>> mTimeOfSessionTags = new ArrayList<>();

    private CountDownTimer mBaselineCountDownTimer;
    private CountDownTimer mSessionCountDownTimer;

    private boolean mRecordingBaseline = false;
    private boolean mBaselineRecorded = false;
    private boolean mRecordingSession = false;

    private String mTypeCounter;

    private long mSessionDurationInMillisec = 0;
    private String mSessionDurationAsString = "00:00";
    private long mSessionDurationPassedInMillisec = 0;
    private long mBaselineDurationInMillisec = 0;
    private String mBaselineDurationAsString = "00:00";
    private long mBaselineDurationPassedInMillisec = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEmpatica();

        initViews();
        updateViews();
        createNewUser();
        initToolbar();
    }

    private void initEmpatica() {
        mDeviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
        // Replace the key in res/values/privatekey.xml by your valid key
        mDeviceManager.authenticateWithAPIKey(getResources().getString(R.string.empatica_key));
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setElevation(10);
        getSupportActionBar().setTitle(mCurrentUser);
        getSupportActionBar().setSubtitle(mCurrentSession.replace("_", " "));
    }

    private void updateViews() {
        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                handler.postDelayed(this, 1000);

                if (!mWristbandConnected) {
                    mTempTextView.setText("");
                    mEDATextView.setText("");
                    mHRTextView.setText("");
                    mRecordingSessionStatusTextView.setText("Off");
                    mWarningTextView.setVisibility(View.INVISIBLE);
                }
                else {
                    if (mLastTemp != 0.0f)
                        mTempTextView.setText(String.format("%.1f", mLastTemp));
                    if (mLastEDA != 0.0f)
                        mEDATextView.setText(String.format("%.2f", mLastEDA));
                    if (mLastHR != 0.0f)
                        mHRTextView.setText(String.format("%2.0f", mLastHR));
                    if (mLastBatteryLevel != -1 && mLastBatteryLevel <= 20.0) {
                        mWarningTextView.setText("Battery level low (" + mLastBatteryLevel + "%)");
                        if (!mWarningTextViewDisplayed) {
                            mWarningTextViewDisplayed = true;
                            showWarningTextView();
                        }
                    }
                }
            }

        };
        handler.postDelayed(r, 1000);
    }


    private void initViews() {
        initCardViews();
        initPhysioViews();
        initBaselineView();
        initSessionView();
        initTagView();
        initIconFont();
        initPogressBar();
        mHistoryLinearLayout = (LinearLayout)findViewById(R.id.history_ll);
        mWarningTextView = (TextView)findViewById(R.id.battery_level_textview);
        mWarningTextView.setVisibility(View.INVISIBLE);
    }

    private void initCardViews() {
        mCounterCV = (CardView)findViewById(R.id.counter_card_view);
        mCounterCV.setAlpha(.3f);
        mPhysioValueCV = (CardView)findViewById(R.id.physio_value_card_view);
        mPhysioValueCV.setAlpha(.3f);
        mHistoryCV = (CardView)findViewById(R.id.log_card_view);
        mHistoryCV.setAlpha(.3f);
    }

    private void initPogressBar() {
        mConnectingProgressBar = (ProgressBar)findViewById(R.id.loading_spinner);
        mConnectingProgressBar.bringToFront();
    }

    private void initPhysioViews() {
        mTempTextView = (TextView) findViewById(R.id.temp_value);
        mEDATextView = (TextView) findViewById(R.id.eda_value);
        mHRTextView = (TextView) findViewById(R.id.hr_value);
        mRecordingSessionStatusTextView = (TextView) findViewById(R.id.record_value);
        mRecordingSessionStatusTextView.setText("Off");
    }
    private void initTagView() {
        mTagSessionCounterTextView = (TextView)findViewById(R.id.tag_timestamp_textview);
        mTagSessionCounterTextView.setText("0");
        mTagButton = (Button)findViewById(R.id.tag_button);

        setButtonDisable(mTagButton, getResources().getString(R.string.tag_icon));

        mTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWarningTextView();
                tagLastPhysioData();
                addTagTimeToList();
                mTagSessionCounterTextView.setText(Integer.toString(mTimeOfSessionTags.size()));
                addHistory("Tag added at " + mTimeOfSessionTags.get(mTimeOfSessionTags.size() - 1).second);
            }
        });
    }

    private void addTagTimeToList() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");
        String currentTime = df.format(c.getTime());

        mTimeOfSessionTags.add(
                new Pair<>(mSessionDurationPassedInMillisec / 1000, currentTime));

    }

    private void tagLastPhysioData() {
        if (!mBVPSessionList.isEmpty())
            mBVPSessionList.get(mBVPSessionList.size()-1).setTagged();
        if (!mEDASessionList.isEmpty())
            mEDASessionList.get(mEDASessionList.size()-1).setTagged();
        if (!mHRSessionList.isEmpty())
            mHRSessionList.get(mHRSessionList.size()-1).setTagged();
        if (!mTempSessionList.isEmpty())
            mTempSessionList.get(mTempSessionList.size() - 1).setTagged();
        if (!mIBISessionList.isEmpty())
            mIBISessionList.get(mIBISessionList.size() -1).setTagged();
    }

    private void initSessionView() {
        mSessionCounterTextView = (TextView)findViewById(R.id.session_counter_textview);
        mSessionCounterTextView.setText("00:00");

        mSessionButton = (Button)findViewById(R.id.session_button);
        mSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked session button");
                if (!mRecordingSession) {
                    mSessionButton.setText(getResources().getString(R.string.stop_icon));
                    mRecordingSessionStatusTextView.setText("On");
                    mSessionCountDownTimer.start();
                    setButtonEnable(mTagButton,
                            getResources().getDrawable(R.drawable.rippleroundedbutton),
                            getResources().getString(R.string.tag_icon));
                    mRecordingSession = true;
                }
                else { // user pressed stop
                    mSessionButton.setText(getResources().getString(R.string.play_icon));
                    mRecordingSessionStatusTextView.setText("Off");
                    mSessionCountDownTimer.cancel();
                    mSessionCounterTextView.setText(mSessionDurationAsString);
                    clearSessionData();
                    mRecordingSession = false;
                    Toast.makeText(MainActivity.this, "Recording session stopped. No data saved !",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSessionCounterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked counter");
                mTypeCounter = "session";
                showCountDownTimerDialog();

            }
        });
        setButtonDisable(mSessionButton, getResources().getString(R.string.play_icon));
    }

    private void initBaselineView() {
        mBaselineCounterTextView = (TextView)findViewById(R.id.baseline_counter_textview);
        mBaselineCounterTextView.setText("00:00");

        mBaselineButton = (Button)findViewById(R.id.baseline_button);
        mBaselineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRecordingBaseline) { // user pressed stop
                    clearBaselineData();
                    mBaselineCountDownTimer.cancel();
                    mBaselineButton.setText(getResources().getString(R.string.play_icon));
                    mBaselineCounterTextView.setText(mBaselineDurationAsString);
                    mRecordingBaseline = false;
                    Toast.makeText(MainActivity.this, "Baseline stopped. No data saved !",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    if (mBaselineRecorded) { // we re-record the baseline
                        eraseLastBaselineFromFlashMemory();
                    }
                    mBaselineButton.setText(getResources().getString(R.string.stop_icon));
                    mRecordingBaseline = true;
                    mBaselineCountDownTimer.start();
                    mRecordingSessionStatusTextView.setText("On");
                }

            }
        });

        mBaselineCounterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTypeCounter = "baseline";
                showCountDownTimerDialog();

            }
        });

        setButtonDisable(mBaselineButton, getResources().getString(R.string.play_icon));
    }

    private void setButtonDisable(Button button, String icon) {
        button.setText(icon);
        button.setClickable(false);
        button.setBackgroundColor(getResources().getColor(R.color.grey_500));
    }

    private void setButtonEnable(Button button, Drawable background, String icon) {
        button.setClickable(true);
        button.setBackground(background);
        button.setText(icon);
    }

    private void clearBaselineData() {
        mEDABaselineList.clear();
        mBVPBaselineList.clear();
        mHRBaselineList.clear();
        mTempBaselineList.clear();
        mIBIBaselineList.clear();
        mFirstRecordedIBITimestamp = -1.0;
    }
    private void clearSessionData() {
        mEDASessionList.clear();
        mBVPSessionList.clear();
        mHRSessionList.clear();
        mTempSessionList.clear();
        mTimeOfSessionTags.clear();
        mIBISessionList.clear();
        mFirstRecordedIBITimestamp = -1.0;
    }
    private void eraseLastBaselineFromFlashMemory() {
        SaveDataUtils.eraseData(mCurrentUser, "baseline", "eda");
        SaveDataUtils.eraseData(mCurrentUser, "baseline", "hr");
        SaveDataUtils.eraseData(mCurrentUser, "baseline", "bvp");
        SaveDataUtils.eraseData(mCurrentUser, "baseline", "temp");

    }

    private void saveBaselineToFlashMemory() {
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, "baseline", "eda", mEDABaselineList);
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, "baseline", "hr", mHRBaselineList);
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, "baseline", "bvp", mBVPBaselineList);
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, "baseline", "temp", mTempBaselineList);
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, "baseline", "ibi", mIBIBaselineList);


    }

    private void saveSessionToFlashMemory() {
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, mCurrentSession, "eda", mEDASessionList);
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, mCurrentSession, "hr", mHRSessionList);
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, mCurrentSession, "bvp", mBVPSessionList);
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, mCurrentSession, "ibi", mIBISessionList);
        SaveDataUtils.writePhysioArrayToFlash(mCurrentUser, mCurrentSession, "temp", mTempSessionList);
        if (!mTimeOfSessionTags.isEmpty()) {
            SaveDataUtils.writeStringArrayToFlash(mCurrentUser, mCurrentSession, "tags", mTimeOfSessionTags);
        }
    }

    private void initIconFont() {
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        ((TextView) findViewById(R.id.heart_icon)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.eda_icon)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.temp_icon)).setTypeface(custom_font);
        ((TextView) findViewById(R.id.save_icon)).setTypeface(custom_font);
        mTagButton.setTypeface(custom_font);
        mSessionButton.setTypeface(custom_font);
        mBaselineButton.setTypeface(custom_font);
    }


    private boolean createNewUser() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        mLastSessionID = 0;
        mCurrentUser = df.format(c.getTime());
        mBaselineRecorded = false;
        if (!setNewUserSession())
            return false;

        return true;
    }

    private boolean setNewUserSession() {
        if (mSessionDurationInMillisec == 0) {
            mLastSessionID = 1;
            return false;
        }

        mLastSessionID += 1;
        mSessionCounterTextView.setText(mSessionDurationAsString);
        mCurrentSession = "session_"+Integer.toString(mLastSessionID);

        clearSessionData();
        mTagSessionCounterTextView.setText("0");

        if (mSessionDurationInMillisec != 0) {
            setButtonEnable(mSessionButton,
                    getResources().getDrawable(R.drawable.rippleroundedbutton),
                    getResources().getString(R.string.play_icon));

            getSupportActionBar().setTitle(mCurrentUser);
            getSupportActionBar().setSubtitle(mCurrentSession.replace("_", " "));
        }
        if (mBaselineDurationInMillisec != 0) {
            //mBaselineButton.setBackground(getResources().getDrawable(R.drawable.rippleroundedbutton));
            //mBaselineButton.setText(getResources().getString(R.string.play_icon));
            setButtonEnable(mBaselineButton,
                    getResources().getDrawable(R.drawable.rippleroundedbutton),
                    getResources().getString(R.string.play_icon));
            mBaselineCounterTextView.setText(mBaselineDurationAsString);
        }

        return true;

    }

    @Override
    public void didReceiveGSR(float eda, double timestamp) {
        mLastEDA = eda;
        if (mRecordingBaseline) {
            mEDABaselineList.add(new PhysioData("eda", eda, Double.toString(timestamp), false));
        }
        else if (mRecordingSession) {
            mEDASessionList.add(new PhysioData("eda", eda, Double.toString(timestamp), false));
        }
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        //Log.i(TAG, "BVP: " + bvp + " (" + timestamp +")");
        if (mRecordingBaseline) {
            mBVPBaselineList.add(new PhysioData("bvp", bvp, Double.toString(timestamp), false));
        }
        else if (mRecordingSession) {
            mBVPSessionList.add(new PhysioData("bvp", bvp, Double.toString(timestamp), false));
        }
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        mLastHR = (60 / ibi);

        String IBITimeInterval = "";
        if (mFirstRecordedIBITimestamp == -1.0) {
            mFirstRecordedIBITimestamp = timestamp;
        }
        else {
            IBITimeInterval = Double.toString(timestamp - mFirstRecordedIBITimestamp);
        }
        if (mRecordingBaseline) {
            mHRBaselineList.add(new PhysioData("hr", mLastHR, Double.toString(timestamp), false));
            mIBIBaselineList.add(new PhysioData("ibi", ibi, IBITimeInterval, false));
        }
        else if (mRecordingSession) {
            mHRSessionList.add(new PhysioData("hr", mLastHR, Double.toString(timestamp), false));
            mIBISessionList.add(new PhysioData("ibi", ibi, IBITimeInterval, false));
        }

    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        //  Log.i(TAG, "Temp: " + temp + " °C(" + timestamp +")");
        mLastTemp = temp;
        if (mRecordingBaseline) {
            mTempBaselineList.add(new PhysioData("temp", temp, Double.toString(timestamp), false));
        }
        else if (mRecordingSession) {
            mTempSessionList.add(new PhysioData("temp", temp, Double.toString(timestamp), false));
        }
    }

    @Override
    public void didReceiveAcceleration(int i, int i2, int i3, double timestamp) {

    }

    @Override
    public void didReceiveBatteryLevel(float v, double timestamp) {
        //Log.i(TAG, "Battery: " + v + " (" + timestamp +")");
        mLastBatteryLevel = v*100;
    }

    @Override
    public void didUpdateStatus(EmpaStatus empaStatus) {
        if (empaStatus == EmpaStatus.READY) {
            // Start scanning
            Log.d(TAG, "Start scanning");
            mDeviceManager.startScanning();
        } else if (empaStatus == EmpaStatus.CONNECTED) {
            Log.d(TAG, "Device status: connected");
            mWristbandConnected = true;
            runOnUiThread(new Runnable(){
                public void run() {
                    removeProgressBar();
                }
            });
        } else if (empaStatus == EmpaStatus.DISCONNECTING) {
            mWristbandConnected = false;
        }


    }

    private void removeProgressBar() {
        int animationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mCounterCV.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(null);
        mPhysioValueCV.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(null);
        mHistoryCV.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(null);

        mConnectingProgressBar.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mConnectingProgressBar.setVisibility(View.GONE);
                    }
                });

    }

    @Override
    public void didUpdateSensorStatus(EmpaSensorStatus empaSensorStatus,
                                      EmpaSensorType empaSensorType) {}

    @Override
    public void didDiscoverDevice(BluetoothDevice bluetoothDevice, int i, boolean allowed) {
        if (allowed) {
            mDeviceManager.stopScanning();
            // Connect to the device
            try {
                mDeviceManager.connectDevice(bluetoothDevice);
            } catch (ConnectionNotAllowedException e) {
                Toast.makeText(MainActivity.this, "Sorry, can't connect to this device",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem i = menu.findItem(R.id.action_adduser);
        i.setActionView(R.layout.menu_add_user_textview);
        TextView itemuser = (TextView) i.getActionView();

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        itemuser.setTypeface(custom_font);

        i.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createNewUser()) {
                    resetHistory();
                    Toast.makeText(MainActivity.this, "New user created!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.new_session) {
            if (setNewUserSession())
                Toast.makeText(this, "New session created!", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showCountDownTimerDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        CountDownTimerPicker newFragment = new CountDownTimerPicker();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment)
                .addToBackStack(null).commit();
    }

    @Override
    public void onDialogPositiveClick(int durationInMillisec) {
        if (durationInMillisec == 0) {
            Toast.makeText(this, "The duration must be strictly greater than 0 second",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mTypeCounter.equals("session")) {
            mSessionDurationInMillisec = durationInMillisec;
            mSessionDurationAsString = getMillisecInReadableString(durationInMillisec);
            mSessionCounterTextView.setText(mSessionDurationAsString);
            mSessionCountDownTimer = new CountDownTimer(durationInMillisec, 1000) {
                public void onTick(long millisUntilFinished) {
                    String timeString = getMillisecInReadableString(millisUntilFinished);
                    mSessionCounterTextView.setText(timeString);
                    mSessionDurationPassedInMillisec = mSessionDurationInMillisec - millisUntilFinished;
                }

                public void onFinish() {
                    mSessionCounterTextView.setText(mSessionDurationAsString);
                    setButtonDisable(mSessionButton, getResources().getString(R.string.play_icon));
                    setButtonDisable(mTagButton, getResources().getString(R.string.tag_icon));
                    mRecordingSessionStatusTextView.setText("Off");
                    mRecordingSession = false;
                    mSessionDurationPassedInMillisec = 0;
                    addHistory(mCurrentSession.replace("_"," ") + " successfully recorded.");
                    saveSessionToFlashMemory();
                }
            };

            setButtonEnable(mSessionButton,
                    getResources().getDrawable(R.drawable.rippleroundedbutton),
                    getResources().getString(R.string.play_icon));
        }
        else if (mTypeCounter.equals("baseline")) {
            mBaselineDurationInMillisec = durationInMillisec;
            mBaselineDurationAsString = getMillisecInReadableString(durationInMillisec);
            mBaselineCounterTextView.setText(mBaselineDurationAsString);
            mBaselineCountDownTimer = new CountDownTimer(durationInMillisec, 1000) {
                public void onTick(long millisUntilFinished) {
                    String timeString = getMillisecInReadableString(millisUntilFinished);
                    mBaselineCounterTextView.setText(timeString);
                    mBaselineDurationPassedInMillisec = mSessionDurationInMillisec - millisUntilFinished;
                }

                public void onFinish() {
                    mRecordingBaseline = false;
                    mBaselineRecorded = true;
                    mBaselineCounterTextView.setText(mBaselineDurationAsString);
                    mBaselineButton.setText(getResources().getString(R.string.restart_icon));
                    mRecordingSessionStatusTextView.setText("Off");
                    mBaselineDurationPassedInMillisec = 0;
                    addHistory("baseline successfully recorded.");
                    saveBaselineToFlashMemory();
                }
            };

            setButtonEnable(mBaselineButton,
                    getResources().getDrawable(R.drawable.rippleroundedbutton),
                    getResources().getString(R.string.play_icon));

        }
    }

    private String getMillisecInReadableString(long millisec) {
        long millisecInSecond = millisec / 1000;
        int minutes = (int) (millisecInSecond % 3600) / 60;
        int seconds = (int) millisecInSecond % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void addHistory(String historyMessage) {
        final TextView history = new TextView(this);
        history.setText(historyMessage);
        history.setTextSize(16);
        history.setTextColor(getResources().getColor(R.color.blue_grey_600));
        mHistoryLinearLayout.addView(history);
        mHistoryLinearLayout.getChildAt(mHistoryLinearLayout.getChildCount()-1)
                .setVisibility(View.INVISIBLE);
        Animation fadeIn = new AlphaAnimation(0.00f, 1.00f);
        fadeIn.setDuration(1000);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
                mHistoryLinearLayout.getChildAt(mHistoryLinearLayout.getChildCount()-1)
                        .setVisibility(View.VISIBLE);
            }
        });
        history.setAnimation(fadeIn);
        history.startAnimation(fadeIn);

    }

    private void resetHistory() {
        Animation fadeOut = new AlphaAnimation(1.00f, 0.00f);
        fadeOut.setDuration(1500);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
               // mHistoryLinearLayout.removeViews(0, mHistoryLinearLayout.getChildCount()-1);
                mHistoryLinearLayout.removeAllViews();
            }
        });

        for (int i=0; i<mHistoryLinearLayout.getChildCount(); ++i) {
            TextView tv = (TextView)mHistoryLinearLayout.getChildAt(i);
            tv.setAnimation(fadeOut);
            tv.startAnimation(fadeOut);
        }
    }

    private void showWarningTextView() {
        Animation slideDown = new TranslateAnimation(0,0,0,0, Animation.RELATIVE_TO_SELF,-1.0f,
                Animation.RELATIVE_TO_SELF,0.0f);

        slideDown.setDuration(1000);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mWarningTextView.bringToFront();
                mWarningTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mWarningTextView.bringToFront();
        mWarningTextView.setAnimation(slideDown);
        mWarningTextView.startAnimation(slideDown);
    }

    private void hideWarningTextView() {
        Animation slideDown = new TranslateAnimation(0,0,0,0, Animation.RELATIVE_TO_SELF,0.0f,
                Animation.RELATIVE_TO_SELF,-1.0f);

        slideDown.setDuration(1000);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mWarningTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mWarningTextView.setAnimation(slideDown);
        mWarningTextView.startAnimation(slideDown);
    }

}
