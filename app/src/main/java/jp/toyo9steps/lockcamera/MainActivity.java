package jp.toyo9steps.lockcamera;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnCheckedChangeListener, OnClickListener{

	private Switch mSwitchDisable;
	private DevicePolicyManager mPolicyManger;
	private ComponentName mAdminReceiver;
	private SettingLoader mSettings;
	private Button mButtonStartTime;
	private Button mButtonEndTime;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSettings = new SettingLoader(this);

		mSwitchDisable = (Switch) findViewById(R.id.switchDisable);
		mSwitchDisable.setOnCheckedChangeListener(this);

		mButtonStartTime = (Button) findViewById(R.id.buttonStartTime);
		mButtonStartTime.setOnClickListener(this);
		setDisableStartTime(mSettings.startTimeHour, mSettings.startTimeMinute);

		mButtonEndTime = (Button) findViewById(R.id.buttonEndTime);
		mButtonEndTime.setOnClickListener(this);
		setDisableEndTime(mSettings.endTimeHour, mSettings.endTimeMinute);

		mPolicyManger = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		mAdminReceiver = new ComponentName(this, MyDeviceAdminReceiver.class);

		if(mPolicyManger.isAdminActive(mAdminReceiver)){
			enableSwitch();
		}
		else{
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminReceiver);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "カメラをOFF/ONします");
			startActivityForResult(intent, 1);
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
		boolean current = mPolicyManger.getCameraDisabled(mAdminReceiver);
		mPolicyManger.setCameraDisabled(mAdminReceiver, !current);
	}

	@Override
	public void onClick(View v){
		if(v == mButtonStartTime){
			new TimePickerFragment().show(getFragmentManager(), TimePickerFragment.TAG_START_TIME);
		}
		else if(v == mButtonEndTime){
			new TimePickerFragment().show(getFragmentManager(), TimePickerFragment.TAG_END_TIME);
		}
	}

	/* TimePickerFragmentから本APIを介して、時刻が設定されたことを通知する */
	public void setDisableStartTime(int hour, int minute) {
		if (hour < 0 || minute < 0) {
			return;
		}
		mSettings.saveStartTime(hour, minute);
		setTimeButtonText(mButtonStartTime, hour, minute);
	}

	public void setDisableEndTime(int hour, int minute) {
		if (hour < 0 || minute < 0) {
			return;
		}
		mSettings.saveEndTime(hour, minute);
		setTimeButtonText(mButtonEndTime, hour, minute);
	}

	private void setTimeButtonText(Button button, int hour, int minute) {
		button.setText(String.format(Locale.JAPAN, "%02d", hour) + ":" + String.format(Locale.JAPAN, "%02d", minute));
	}
}
