package jp.toyo9steps.lockcamera;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * AlarmManagerのタイマー発火を受け取るクラス
 */

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_REQUEST_CODE = "AlarmReceiver.extra";
    public static final int REQUEST_DISABLE_START_TIME = 0;
    public static final int REQUEST_DISABLE_END_TIME = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        DevicePolicyManager policyManger = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminName = new ComponentName(context, MyDeviceAdminReceiver.class);
        if(!policyManger.isAdminActive(adminName)){
            return;
        }

        int request = intent.getIntExtra(EXTRA_REQUEST_CODE, REQUEST_DISABLE_START_TIME);
        if (request == REQUEST_DISABLE_START_TIME) {
            policyManger.setCameraDisabled(adminName, true);
        }
        else if (request == REQUEST_DISABLE_END_TIME) {
            policyManger.setCameraDisabled(adminName, false);
        }
    }
}
