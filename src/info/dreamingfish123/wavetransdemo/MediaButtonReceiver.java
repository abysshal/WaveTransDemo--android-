package info.dreamingfish123.wavetransdemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

/**
 * The media button events receiver.
 * 
 * @author Hui
 * 
 */
public class MediaButtonReceiver extends BroadcastReceiver {

	private static final BroadcastReceiver INSTANCE = new MediaButtonReceiver();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("MR", "on recv");

		if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
			Log.i("MR", "media button action recv.");
			abortBroadcast();
		} else {
			return;
		}

		int keyCode = ((KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT)).getKeyCode();

		switch (keyCode) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
			Log.i("MR", "headset hook.");
			break;
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			Log.i("MR", "play and pause.");
			break;
		case KeyEvent.KEYCODE_MEDIA_NEXT:
			Log.i("MR", "play next.");
			break;
		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			Log.i("MR", "play previous.");
			break;
		default:
			break;
		}
	}

	public static void register(Context context) {
		IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
		filter.setPriority(Integer.MAX_VALUE);
		context.registerReceiver(INSTANCE, filter);
		Log.i("MR", "on register");
	}

	public static void unregister(Context context) {
		context.unregisterReceiver(INSTANCE);
		Log.i("MR", "on unregister");
	}

	public static void registerAudioManager(Context context) {
		Log.i("MR", "on register");
		AudioManager mAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		ComponentName recvCN = new ComponentName(context.getPackageName(),
				MediaButtonReceiver.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(recvCN);
	}

	public static void unregisterAudioManager(Context context) {
		Log.i("MR", "on unregister");
		AudioManager mAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		ComponentName recvCN = new ComponentName(context.getPackageName(),
				MediaButtonReceiver.class.getName());
		mAudioManager.unregisterMediaButtonEventReceiver(recvCN);
	}

}
