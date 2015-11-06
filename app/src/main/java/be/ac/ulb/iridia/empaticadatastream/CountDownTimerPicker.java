package be.ac.ulb.iridia.empaticadatastream;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class CountDownTimerPicker extends DialogFragment {
    private static final String TAG = "CountDownPicker";

    TextView mHoursSelectedTextView;
    TextView mMinutesSelectedTextView;
    TextView mSecondsSelectedTextView;

    boolean mHasAlreadySelected = false;

    int mSecU = -1;
    int mSecD = -1;
    int mMinU = -1;
    int mMinD = -1;
    int mHourU = -1;
    int mLastSelectedDigit;

    public interface CountDownTimerDialogListener {
        public void onDialogPositiveClick(int durationInMillisec);
    }

    CountDownTimerDialogListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_count_down_picker, container, false);
        initTimeTextView(v);
        initClearButton(v);
        initNumpadTable(v);
        initValidateButton(v);
        return v;
    }


    private void initTimeTextView(View v) {
        mHoursSelectedTextView = (TextView)v.findViewById(R.id.hour_value);
        mMinutesSelectedTextView = (TextView)v.findViewById(R.id.min_value);
        mSecondsSelectedTextView = (TextView)v.findViewById(R.id.sec_value);
    }

    private void resetTime(View v) {
        mHoursSelectedTextView.setText("0");
        mMinutesSelectedTextView.setText("00");
        mSecondsSelectedTextView.setText("00");
        mSecU = mSecD = mMinU = mMinD = mHourU = -1;
        mHasAlreadySelected = false;
    }

    private void initValidateButton(View v) {
        Button validateButton = (Button)v.findViewById(R.id.saveTime_button);
        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int totalSec = 0;
                 if (mSecU != -1 && mSecD == -1) {
                    totalSec = mSecU;

                }
                else if (mSecU != -1 && mMinU == -1) {
                     totalSec = Integer.parseInt(Integer.toString(mSecD) + Integer.toString(mSecU));
                }
                else if (mSecU != -1 && mMinD == -1) {
                     int sec = Integer.parseInt(Integer.toString(mSecD) + Integer.toString(mSecU));
                     int min = Integer.parseInt(Integer.toString(mMinU));
                     totalSec = sec + 60*min;

                 }
                 else if (mSecU != -1) {
                     int sec = Integer.parseInt(Integer.toString(mSecD) + Integer.toString(mSecU));
                     int min = Integer.parseInt(Integer.toString(mMinD) + Integer.toString(mMinU));
                     totalSec = sec + 60*min;
                 }

                mListener.onDialogPositiveClick(totalSec*1000);
                dismiss();

            }
        });

        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fontawesome-webfont.ttf");
        validateButton.setTypeface(custom_font);
    }

    private void initNumpadTable(View v) {
        final TableLayout numpadTable = (TableLayout)v.findViewById(R.id.numpad_tablelayout);
        for (int i=0; i<4; ++i) {
            TableRow row = (TableRow) numpadTable.getChildAt(i);
            if (i == 3) {
                final TextView item = (TextView) row.getChildAt(0);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mLastSelectedDigit = Integer.valueOf(item.getText().toString());
                        updateSelectedTime();
                    }
                });
            } else {
                for (int j = 0; j < 3; ++j) {
                    final TextView item = (TextView) row.getChildAt(j);
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mLastSelectedDigit = Integer.valueOf(item.getText().toString());
                            updateSelectedTime();
                        }
                    });
                }
            }
        }
    }

    private void updateSelectedTime() {
        if (mSecU == -1) {
            mSecU = mLastSelectedDigit;
            mSecondsSelectedTextView.setText("0" + Integer.toString(mSecU));
        }
        else if (mSecD == -1) {
            mSecD = mSecU;
            mSecU = mLastSelectedDigit;
            mSecondsSelectedTextView.setText(Integer.toString(mSecD) + Integer.toString(mSecU));
        }
        else if (mMinU == -1) {
            mMinU = mSecD;
            mSecD = mSecU;
            mSecU = mLastSelectedDigit;
            mSecondsSelectedTextView.setText(Integer.toString(mSecD) + Integer.toString(mSecU));
            mMinutesSelectedTextView.setText("0" + Integer.toString(mMinU));
        }
        else if (mMinD == -1) {
            mMinD = mMinU;
            mMinU = mSecD;
            mSecD = mSecU;
            mSecU = mLastSelectedDigit;
            mSecondsSelectedTextView.setText(Integer.toString(mSecD) + Integer.toString(mSecU));
            mMinutesSelectedTextView.setText(Integer.toString(mMinD) + Integer.toString(mMinU));
        }
    }

    private void initClearButton(final View v) {
        final TextView clearTimeButton = (TextView)v.findViewById(R.id.clear_time);
        clearTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTime(v);
            }
        });

        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fontawesome-webfont.ttf");
        clearTimeButton.setTypeface(custom_font);
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CountDownTimerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CountDownTimerDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


}