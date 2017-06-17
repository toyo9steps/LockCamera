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

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		return new TimePickerDialog(getActivity(), this, hour, minute, true);
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute){
		if(TAG_START_TIME.equals(getTag())){
			Button button = (Button) getActivity().findViewById(R.id.buttonStartTime);
			button.setText(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
		}
		else{
			Button button = (Button) getActivity().findViewById(R.id.buttonEndTime);
			button.setText(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
		}
	}
}
