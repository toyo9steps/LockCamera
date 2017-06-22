package jp.toyo9steps.lockcamera;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnCheckedChangeListener, OnClickListener{

	private Switch mSwitchDisable;
	private DevicePolicyManager mPolicyManger;
	private ComponentName mAdminReceiver;
	private SettingLoader mSettings;
	private CheckBox mCheckAutoTimer;
	private Button mButtonStartTime;
	private Button mButtonEndTime;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSettings = new SettingLoader(this);

		mSwitchDisable = (Switch) findViewById(R.id.switchDisable);
		mSwitchDisable.setOnCheckedChangeListener(this);

		mCheckAutoTimer = (CheckBox) findViewById(R.id.checkAutoTimer);
		mCheckAutoTimer.setOnCheckedChangeListener(this);
		mCheckAutoTimer.setChecked(mSettings.autoTimer);

		mButtonStartTime = (Button) findViewById(R.id.buttonStartTime);
		mButtonStartTime.setOnClickListener(this);
		mButtonStartTime.setEnabled(mSettings.autoTimer);
		setDisableStartTime(mSettings.startTimeHour, mSettings.startTimeMinute);

		mButtonEndTime = (Button) findViewById(R.id.buttonEndTime);
		mButtonEndTime.setOnClickListener(this);
		mButtonEndTime.setEnabled(mSettings.autoTimer);
		setDisableEndTime(mSettings.endTimeHour, mSettings.endTimeMinute);

		mPolicyManger = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		mAdminReceiver = new ComponentName(this, MyDeviceAdminReceiver.class);

		if(mPolicyManger.isAdminActive(mAdminReceiver)){
			if (mSettings.autoTimer) {
				setRepeatingAlarms();/* システムにアラームを登録 */
			}
		}
		else{
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminReceiver);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "カメラをOFF/ONします");
			startActivityForResult(intent, 1);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		/* 別アプリでカメラ無効状態が変わっているかもしれないので、
		 * アプリがフォアグラウンドに来る度に状態を取得してUIを更新する */
		if(mPolicyManger.isAdminActive(mAdminReceiver)){
			enableSwitch();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){
			enableSwitch();
		}
	}

	private void enableSwitch(){
		mSwitchDisable.setEnabled(true);
		mSwitchDisable.setChecked(mPolicyManger.getCameraDisabled(mAdminReceiver));
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
		if (buttonView == mCheckAutoTimer) {
			mSettings.saveAutoTimer(isChecked);
			mButtonStartTime.setEnabled(isChecked);
			mButtonEndTime.setEnabled(isChecked);
			if (isChecked) {
				setRepeatingAlarms();
			}
			else{
				clearRepeatingAlarms();
			}
		}
		else if (buttonView == mSwitchDisable) {
			mPolicyManger.setCameraDisabled(mAdminReceiver, isChecked);
		}
	}

	@Override
	public void onClick(View v){
		if(v == mButtonStartTime){
			TimePickerFragment fragment = new TimePickerFragment();
			Bundle args = new Bundle();
			args.putInt(TimePickerFragment.EXTRA_INIT_TIME_HOUR, mSettings.startTimeHour);
			args.putInt(TimePickerFragment.EXTRA_INIT_TIME_MINUTE, mSettings.startTimeMinute);
			fragment.setArguments(args);
			fragment.show(getFragmentManager(), TimePickerFragment.TAG_START_TIME);
		}
		else if(v == mButtonEndTime){
			TimePickerFragment fragment = new TimePickerFragment();
			Bundle args = new Bundle();
			args.putInt(TimePickerFragment.EXTRA_INIT_TIME_HOUR, mSettings.endTimeHour);
			args.putInt(TimePickerFragment.EXTRA_INIT_TIME_MINUTE, mSettings.endTimeMinute);
			fragment.setArguments(args);
			fragment.show(getFragmentManager(), TimePickerFragment.TAG_END_TIME);
		}
	}

	/* TimePickerFragmentから本APIを介して、時刻が設定されたことを通知する */
	public void setDisableStartTime(int hour, int minute) {
		if (hour < 0 || minute < 0) {
			return;
		}
		mSettings.saveStartTime(hour, minute);
		setTimeButtonText(mButtonStartTime, hour, minute);
		setRepeatingAlarms();/* システムにアラームを登録 */
	}

	public void setDisableEndTime(int hour, int minute) {
		if (hour < 0 || minute < 0) {
			return;
		}
		mSettings.saveEndTime(hour, minute);
		setTimeButtonText(mButtonEndTime, hour, minute);
		setRepeatingAlarms();/* システムにアラームを登録 */
	}

	private void setTimeButtonText(Button button, int hour, int minute) {
		button.setText(String.format(Locale.JAPAN, "%02d", hour) + ":" + String.format(Locale.JAPAN, "%02d", minute));
	}

	private void setRepeatingAlarms() {
		if (!mSettings.timeIsValid()) {
			return;
		}

		setRepeatingAlarm(AlarmReceiver.REQUEST_DISABLE_START_TIME, mSettings.startTimeHour, mSettings.startTimeMinute);
		setRepeatingAlarm(AlarmReceiver.REQUEST_DISABLE_END_TIME, mSettings.endTimeHour, mSettings.endTimeMinute);
	}

	private void setRepeatingAlarm(int requestCode, int hour, int minute) {
		Intent intent = new Intent(this, AlarmReceiver.class);
		intent.putExtra(AlarmReceiver.EXTRA_REQUEST_CODE, requestCode);
		PendingIntent pending = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		Calendar calendar = Calendar.getInstance();

		/* 指定した時刻が今現在の時刻より前か後ろか判定し、前だったら明日の指定時刻に設定する */
		int todayTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
		int targetTime = hour * 60 + minute;
		if (targetTime <= todayTime) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		/* 24時間をmsecに換算する */
		long interval = 24 * 60 * 60 * 1000;
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pending);
	}

	private void clearRepeatingAlarms() {
		clearRepeatingAlarm(AlarmReceiver.REQUEST_DISABLE_START_TIME);
		clearRepeatingAlarm(AlarmReceiver.REQUEST_DISABLE_END_TIME);
	}

	private void clearRepeatingAlarm(int requestCode) {
		Intent intent = new Intent(this, AlarmReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarm.cancel(pending);
	}
}
