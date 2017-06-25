package jp.toyo9steps.lockcamera;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
		implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, OnClickListener{

	private Switch mSwitchDisable;
	private CameraManager mCameraManager;
	private PreciseTimer mTimer;
	private SettingLoader mSettings;
	private RadioGroup mRadioGroup;
	private Button mButtonStartTime;
	private Button mButtonEndTime;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSettings = new SettingLoader(this);
		mTimer = new PreciseTimer(this);
		mCameraManager = new CameraManager(this);

		mSwitchDisable = (Switch) findViewById(R.id.switchDisable);
		mSwitchDisable.setOnCheckedChangeListener(this);

		mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		mRadioGroup.setOnCheckedChangeListener(this);

		mButtonStartTime = (Button) findViewById(R.id.buttonStartTime);
		mButtonStartTime.setOnClickListener(this);
		setDisableStartTime(mSettings.startTimeHour, mSettings.startTimeMinute);

		mButtonEndTime = (Button) findViewById(R.id.buttonEndTime);
		mButtonEndTime.setOnClickListener(this);
		setDisableEndTime(mSettings.endTimeHour, mSettings.endTimeMinute);

		if(mCameraManager.isAdminActive()){
			setRadioButtonItems(mSettings.settingMode, true);
		}
		else{
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mCameraManager.getAdminComponent());
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "カメラをOFF/ONします");
			startActivityForResult(intent, 1);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		/* 別アプリでカメラ無効状態が変わっているかもしれないので、
		 * アプリがフォアグラウンドに来る度に状態を取得してUIを更新する */
		if(mCameraManager.isAdminActive()){
			setRadioButtonItems(mSettings.settingMode, false);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){
			setRadioButtonItems(mSettings.settingMode, true);
		}
	}

	private void setRadioButtonItems(int settingMode, boolean checkRadio){
		if(settingMode == SettingLoader.SETTING_MODE_MANUAL){
			if(checkRadio){
				mRadioGroup.check(R.id.radioManual);
			}

			mSwitchDisable.setEnabled(true);
			/* setCheckedでリスナーが呼ばれてしまうので、リスナーを一旦解除する */
			mSwitchDisable.setOnCheckedChangeListener(null);
			mSwitchDisable.setChecked(mCameraManager.getDisabled());
			mSwitchDisable.setOnCheckedChangeListener(this);
		}
		else{
			mSwitchDisable.setEnabled(false);
			mCameraManager.setDisabled(false);
			/* setCheckedでリスナーが呼ばれてしまうので、リスナーを一旦解除する */
			mSwitchDisable.setOnCheckedChangeListener(null);
			mSwitchDisable.setChecked(false);
			mSwitchDisable.setOnCheckedChangeListener(this);
		}

		if(settingMode == SettingLoader.SETTING_MODE_TIMER){
			if(checkRadio){
				mRadioGroup.check(R.id.radioTimeer);
			}
			setRepeatingAlarms(mSettings, mTimer, mCameraManager);
			mButtonStartTime.setEnabled(true);
			mButtonEndTime.setEnabled(true);
		}
		else{
			clearRepeatingAlarms();
			mButtonStartTime.setEnabled(false);
			mButtonEndTime.setEnabled(false);
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, @IdRes int id){
		if(id == R.id.radioManual){
			mSettings.saveSettingMode(SettingLoader.SETTING_MODE_MANUAL);
		}
		else if(id == R.id.radioTimeer){
			mSettings.saveSettingMode(SettingLoader.SETTING_MODE_TIMER);
		}
		setRadioButtonItems(mSettings.settingMode, false);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
		if (buttonView == mSwitchDisable) {
			mCameraManager.setDisabled(isChecked);
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
		if(mSettings.settingMode == SettingLoader.SETTING_MODE_TIMER){
			setRepeatingAlarms(mSettings, mTimer, mCameraManager);/* システムにアラームを登録 */
		}
	}

	/* TimePickerFragmentから本APIを介して、時刻が設定されたことを通知する */
	public void setDisableEndTime(int hour, int minute) {
		if (hour < 0 || minute < 0) {
			return;
		}
		mSettings.saveEndTime(hour, minute);
		setTimeButtonText(mButtonEndTime, hour, minute);
		if(mSettings.settingMode == SettingLoader.SETTING_MODE_TIMER){
			setRepeatingAlarms(mSettings, mTimer, mCameraManager);/* システムにアラームを登録 */
		}
	}

	private void setTimeButtonText(Button button, int hour, int minute) {
		button.setText(String.format(Locale.JAPAN, "%02d", hour) + ":" + String.format(Locale.JAPAN, "%02d", minute));
	}

	public static void setRepeatingAlarms(SettingLoader settings, PreciseTimer timer, CameraManager cameraManager) {
		if (!settings.timeIsValid()) {
			return;
		}

		/* タイマーを設定すると同時に、タイマーが明日に設定された否かを取得する */
		boolean startTommorow = timer.set(AlarmReceiver.REQUEST_DISABLE_START_TIME, settings.startTimeHour, settings.startTimeMinute);
		boolean endTommorow = timer.set(AlarmReceiver.REQUEST_DISABLE_END_TIME, settings.endTimeHour, settings.endTimeMinute);

		/* 現在時刻が開始時刻よりも遅く、終了時刻よりも前ならば即座にカメラを無効化する */
		if(startTommorow && !endTommorow){
			cameraManager.setDisabled(true);
		}
	}

	private void clearRepeatingAlarms() {
		mTimer.cancel(AlarmReceiver.REQUEST_DISABLE_START_TIME);
		mTimer.cancel(AlarmReceiver.REQUEST_DISABLE_END_TIME);
	}
}
