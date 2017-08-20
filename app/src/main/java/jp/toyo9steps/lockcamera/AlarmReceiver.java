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
    public static final int REQUEST_DISABLE_START_TIME = 0;
    public static final int REQUEST_DISABLE_END_TIME = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
		CameraManager cameraManager = new CameraManager(context);
		if(!cameraManager.isAdminActive()){
            return;
        }

		PreciseTimer timer = new PreciseTimer(context);
		SettingLoader settings = new SettingLoader(context);
		int request = intent.getIntExtra(EXTRA_REQUEST_CODE, REQUEST_DISABLE_START_TIME);
		int hour;
		int minute;
		if(request == REQUEST_DISABLE_START_TIME){
			hour = settings.startTimeHour;
			minute = settings.startTimeMinute;
		}
		else{
			hour = settings.endTimeHour;
			minute = settings.endTimeMinute;
		}
		if(hour < 0 || minute < 0){
			return;
		}

		/* 今日の曜日を確認してカメラの無効有効を切り替える */
		Calendar today = Calendar.getInstance();
		int todayDowBit = SettingLoader.calendarDowToDowBit(today.get(Calendar.DAY_OF_WEEK));

		if(request == REQUEST_DISABLE_START_TIME){
			/* 今日が無効対象の日か確認する */
			if((settings.timerDowBits & todayDowBit) != 0){
				cameraManager.setDisabled(true, settings.showNotification);
			}
			else{
				/* 非対称だったら、カメラを有効にする */
				cameraManager.setDisabled(false, settings.showNotification);
			}

			/* 次のタイマーを設定 */
			timer.set(request, hour, minute, true);
        }
        else if(request == REQUEST_DISABLE_END_TIME){
			/* カメラの有効化は曜日を気にせずに実施する */
			cameraManager.setDisabled(false, settings.showNotification);
			/* 次のタイマーを設定 */
			timer.set(request, hour, minute, true);
        }
    }
}
