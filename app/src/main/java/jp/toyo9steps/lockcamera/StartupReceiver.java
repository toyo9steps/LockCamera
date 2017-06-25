package jp.toyo9steps.lockcamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * システムの起動イベントをふっくして、アラームを再設定するクラス
 */

public class StartupReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent){
		SettingLoader settings = new SettingLoader(context);
		PreciseTimer timer = new PreciseTimer(context);
		CameraManager cameraManager = new CameraManager(context);
		MainActivity.setRepeatingAlarms(settings, timer, cameraManager);
	}
}
