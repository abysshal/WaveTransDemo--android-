package info.dreamingfish123.wavetransdemo;

import info.dreamingfish123.WaveTransProto.codec.Constant;

import java.io.IOException;
import java.io.InputStream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private final static String TAG = "MAIN";
	private AudioTrack sender = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// int minBuf = AudioTrack.getMinBufferSize(44100,
		// AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
		// Log.d(TAG, "min buffer size:" + minBuf);
		// sender = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
		// AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT,
		// minBuf * 10, AudioTrack.MODE_STREAM);
		// sender.play();

		setEvents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// sender.stop();
		// sender.release();
		super.onDestroy();
	}

	private void setEvents() {
		Button playButton = (Button) findViewById(R.id.button1);
		playButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				playSample();
			}
		});
	}

	private void playSample() {
		try {
			InputStream is = getResources().getAssets().open("sample.wav");
			byte[] wavein = new byte[Constant.WAVEOUT_BUF_SIZE];
			int read = is.read(wavein);
			is.close();

			Log.d(TAG, "buffer size:" + (read - Constant.WAVE_HEAD_LEN));
			if (sender != null) {
				sender.release();
			}

			sender = new AudioTrack(AudioManager.STREAM_MUSIC,
					Constant.WAVE_RATE_INHZ, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_8BIT,
					Constant.WAVEOUT_BUF_SIZE * 2, AudioTrack.MODE_STATIC);
			sender.write(wavein, Constant.WAVE_HEAD_LEN, read
					- Constant.WAVE_HEAD_LEN);
			sender.play();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
