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

public class MainActivity extends AppCompatActivity implements OnCheckedChangeListener, OnClickListener{

	private Switch mSwitchDisable;
	private DevicePolicyManager mPolicyManger;
	private ComponentName mAdminReceiver;
	private Button mButtonStartTime;
	private Button mButtonEndTime;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSwitchDisable = (Switch) findViewById(R.id.switchDisable);
		mSwitchDisable.setOnCheckedChangeListener(this);

		mButtonStartTime = (Button) findViewById(R.id.buttonStartTime);
		mButtonStartTime.setOnClickListener(this);

		mButtonEndTime = (Button) findViewById(R.id.buttonEndTime);
		mButtonEndTime.setOnClickListener(this);

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
}
