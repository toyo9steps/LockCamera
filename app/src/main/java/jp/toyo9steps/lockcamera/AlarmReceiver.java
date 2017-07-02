package jp.toyo9steps.lockcamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * AlarmManagerのタイマー発火を受け取るクラス
 */

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_REQUEST_CODE = "AlarmReceiver.extra.REQUEST_CODE";
    public static final String EXTRA_TIME_HOUR = "AlarmReceiver.extra.TIME_HOUR";
    public static final String EXTRA_TIME_MINUTE = "AlarmReceiver.extra.TIME_MINUTE";
    public static final String EXTRA_TIME_DOW_BITS = "AlarmReceiver.extra.TIME_DOW_BITS";
    public static final int REQUEST_DISABLE_START_TIME = 0;
    public static final int REQUEST_DISABLE_END_TIME = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
		CameraManager cameraManager = new CameraManager(context);
		if(!cameraManager.isAdminActive()){
            return;
        }

		PreciseTimer timer = new PreciseTimer(context);
        int request = intent.getIntExtra(EXTRA_REQUEST_CODE, REQUEST_DISABLE_START_TIME);
		int hour = intent.getIntExtra(EXTRA_TIME_HOUR, -1);
		int minute = intent.getIntExtra(EXTRA_TIME_MINUTE, -1);
		int dowBits = intent.getIntExtra(EXTRA_TIME_DOW_BITS, SettingLoader.TIMER_DOW_EVERYDAY);
		if(hour < 0 || minute < 0){
			return;
		}

		/* 今日の曜日を確認してカメラの無効有効を切り替える */
		Calendar today = Calendar.getInstance();
		int todayDowBit = SettingLoader.calendarDowToDowBit(today.get(Calendar.DAY_OF_WEEK));

		if(request == REQUEST_DISABLE_START_TIME){
			/* 今日が無効対象の日か確認する */
			if((dowBits & todayDowBit) != 0){
				cameraManager.setDisabled(true);
			}
			else{
				/* 非対称だったら、カメラを有効にする */
				cameraManager.setDisabled(false);
			}

			/* 次のタイマーを設定 */
			timer.set(request, hour, minute, true, dowBits);
        }
        else if(request == REQUEST_DISABLE_END_TIME){
			/* カメラの有効化は曜日を気にせずに実施する */
			cameraManager.setDisabled(false);
			/* 次のタイマーを設定 */
			timer.set(request, hour, minute, true, dowBits);
        }
    }
}
