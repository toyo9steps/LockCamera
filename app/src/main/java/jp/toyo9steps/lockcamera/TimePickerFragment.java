package jp.toyo9steps.lockcamera;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * TimePickerDialogを呼び出すためのフラグメント
 */

public class TimePickerFragment extends DialogFragment implements OnTimeSetListener{

	public static final String TAG_START_TIME = "TimePickerFragment.START_TIME";
	public static final String TAG_END_TIME = "TimePickerFragment.END_TIME";
	public static final String EXTRA_INIT_TIME_HOUR = "EXTRA_INIT_TIME_HOUR";
	public static final String EXTRA_INIT_TIME_MINUTE = "EXTRA_INIT_TIME_MINUTE";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Bundle args = getArguments();
		int hour = args.getInt(EXTRA_INIT_TIME_HOUR, -1);
		int minute = args.getInt(EXTRA_INIT_TIME_MINUTE, -1);
		/* 呼び出し元から初期設定時刻がもらえなかった場合は、現在時刻を初期値として設定する */
		if (hour < 0 || minute < 0) {
			Calendar calendar = Calendar.getInstance();
			hour = calendar.get(Calendar.HOUR_OF_DAY);
			minute = calendar.get(Calendar.MINUTE);
		}

		return new TimePickerDialog(getActivity(), this, hour, minute, true);
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute){
		if(TAG_START_TIME.equals(getTag())){
			MainActivity activity = (MainActivity) getActivity();
			activity.setDisableStartTime(hourOfDay, minute);
		}
		else{
			MainActivity activity = (MainActivity) getActivity();
			activity.setDisableEndTime(hourOfDay, minute);
		}
	}
}
