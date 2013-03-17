package info.dreamingfish123.wavetransdemo;

import info.dreamingfish123.wavetransdemo.proto.Constant;
import info.dreamingfish123.wavetransdemo.proto.DynamicAverageAnalyzer;
import info.dreamingfish123.wavetransdemo.proto.Util;
import info.dreamingfish123.wavetransdemo.proto.WTPPacket;
import info.dreamingfish123.wavetransdemo.proto.WaveEncodeTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final static String TAG = "MAIN";
	private AudioTrack sender = null;
	private RecordTestRunnable recordTestRunnable = null;
	private DecodeTestRunnable decodeTestRunnable = null;
	private boolean isRecording = false;
	private boolean isTesting = false;
	private boolean isTestingFile = false;
	private List<WTPPacket> foundPackets = new ArrayList<WTPPacket>();
	private Handler recorderCallbackHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0 && msg.obj != null) {
				foundPackets.add((WTPPacket) msg.obj);
				if (foundPacketTextView != null) {
					foundPacketTextView.setText(String.valueOf(foundPackets
							.size()));
				}
			} else if (msg.what == 1) {
				isTestingFile = false;
			}
			super.handleMessage(msg);
		}

	};

	private TextView logTextView;
	private TextView foundPacketTextView;
	private TextView rightPacketTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
		if (sender != null) {
			sender.stop();
			sender.release();
		}
		stopRecord();
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

		final Button recordButton = (Button) findViewById(R.id.Button01);
		recordButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isRecording) {
					isRecording = false;
					stopRecord();
					recordButton.setText("Start Record");
				} else {
					isRecording = true;
					startRecord();
					recordButton.setText("Stop Record");
				}
			}
		});

		Button compareButton = (Button) findViewById(R.id.Button02);
		compareButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				compareResult();
			}
		});

		final Button testButton = (Button) findViewById(R.id.Button03);
		testButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isTesting) {
					isTesting = false;
					stopTest();
					testButton.setText("Strat Test");
				} else {
					isTesting = true;
					startTest();
					testButton.setText("Stop Test");
				}
			}
		});

		Button testFileButton = (Button) findViewById(R.id.Button04);
		testFileButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isTestingFile) {
					return;
				} else {
					isTestingFile = true;
					startTestFile();
				}
			}
		});

		foundPacketTextView = (TextView) findViewById(R.id.textView1);
		rightPacketTextView = (TextView) findViewById(R.id.TextView02);
		logTextView = (TextView) findViewById(R.id.textView3);
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
			e.printStackTrace();
		}
	}

	private void startRecord() {
		if (recordTestRunnable == null) {
			recordTestRunnable = new RecordTestRunnable();
			Thread thread = new Thread(recordTestRunnable);
			thread.start();
		}
	}

	private void stopRecord() {
		if (recordTestRunnable != null) {
			recordTestRunnable.isRunning = false;
			recordTestRunnable = null;
		}
	}

	private void compareResult() {
		int right = 0;
		for (WTPPacket packet : foundPackets) {
			if (WaveEncodeTest.compareSData(packet.getPacketBytes())) {
				right++;
			}
		}
		if (rightPacketTextView != null) {
			rightPacketTextView.setText(String.valueOf(right));
		}
	}

	private void clean() {
		if (foundPackets != null) {
			foundPackets.clear();
		}
		if (foundPacketTextView != null) {
			foundPacketTextView.setText("0");
		}
		if (rightPacketTextView != null) {
			rightPacketTextView.setText("0");
		}
		if (logTextView != null) {
			logTextView.setText("0");
		}
	}

	private void startTest() {
		clean();
		if (decodeTestRunnable == null) {
			decodeTestRunnable = new DecodeTestRunnable(
					this.recorderCallbackHandler);
			Thread thread = new Thread(decodeTestRunnable);
			thread.start();
		}
	}

	private void stopTest() {
		if (decodeTestRunnable != null) {
			decodeTestRunnable.isRunning = false;
			decodeTestRunnable = null;
		}
	}

	private void startTestFile() {
		clean();
		FileDecodeTestRunnable fileDecodeTestRunnable = new FileDecodeTestRunnable(
				this.recorderCallbackHandler);
		Thread thread = new Thread(fileDecodeTestRunnable);
		thread.start();
	}

	class RecordTestRunnable implements Runnable {

		public AudioRecord recorder = null;
		int bufferSize;
		public boolean isRunning = false;
		public String fileName = null;

		@Override
		public void run() {
			isRunning = true;

			bufferSize = AudioRecord
					.getMinBufferSize(Constant.WAVE_RATE_INHZ,
							AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
			System.out.println("BufferSize:" + bufferSize);
			byte[] buffer = new byte[bufferSize];
			FileOutputStream fos = null;
			int readSize = 0;
			fileName = Environment.getExternalStorageDirectory() + "/wavein_"
					+ Calendar.getInstance().getTimeInMillis();
			File file = new File(fileName + ".raw");
			try {
				fos = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}

			startRecord();
			while (isRunning) {
				readSize = recorder.read(buffer, 0, bufferSize);
				if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
					try {
						fos.write(buffer, 0, readSize);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			stopRecord();

			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			writeWaveFile();
		}

		public void startRecord() {
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					Constant.WAVE_RATE_INHZ, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			recorder.startRecording();
		}

		public void stopRecord() {
			if (recorder != null) {
				recorder.stop();
				recorder.release();
				recorder = null;
			}
		}

		public void writeWaveFile() {
			FileInputStream fis = null;
			FileOutputStream fos = null;
			byte[] buffer = new byte[bufferSize];
			byte[] header = new byte[Constant.WAVE_HEADER.length];
			System.arraycopy(Constant.WAVE_HEADER, 0, header, 0,
					Constant.WAVE_HEADER.length);
			int readSize = 0;
			try {
				fis = new FileInputStream(fileName + ".raw");
				fos = new FileOutputStream(fileName + ".wav");
				int dataLen = (int) fis.getChannel().size();
				Util.int2byte(dataLen, header, Constant.WAVE_DATA_LEN_OFFSET);
				Util.int2byte(dataLen + 36, header,
						Constant.WAVE_FILE_LEN_OFFSET);
				header[28] = (byte) 0x88;
				header[29] = 0x58;
				header[30] = 0x01;
				header[31] = 0x00;
				header[32] = 0x02;
				header[34] = 0x10;
				fos.write(header);

				readSize = fis.read(buffer);
				while (readSize != -1) {
					fos.write(buffer, 0, readSize);
					readSize = fis.read(buffer);
				}

				fis.close();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class PlayTestRunnable implements Runnable {

		public boolean isRunning = false;
		public InputStream is = null;
		public int repeatTimes = 1;
		public long sleepInterval = 0l;
		byte[] wavein = new byte[Constant.WAVEOUT_BUF_SIZE];

		public PlayTestRunnable(InputStream inputStream, int repeat, long sleep) {
			is = inputStream;
			repeatTimes = repeat;
			sleepInterval = sleep;
		}

		@Override
		public void run() {
			isRunning = true;

			while (isRunning) {

			}
		}

		public void prepareWavein() {

		}

	}

	class DecodeTestRunnable implements Runnable {

		public Handler handler;
		public AudioRecord recorder = null;
		int bufferSize;
		public boolean isRunning = false;
		private DynamicAverageAnalyzer analyzer = null;

		public DecodeTestRunnable(Handler handler) {
			this.handler = handler;
			this.analyzer = new DynamicAverageAnalyzer();
		}

		@Override
		public void run() {
			isRunning = true;

			bufferSize = AudioRecord
					.getMinBufferSize(Constant.WAVE_RATE_INHZ,
							AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
			Log.i(TAG, "Audio Recorder Buffer Size:" + bufferSize);
			byte[] buffer = new byte[bufferSize];
			int readSize = 0;

			startRecord();
			while (isRunning) {
				readSize = recorder.read(buffer, 0, bufferSize);
				if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
					if (!analyzer.appendBuffer(buffer, 0, readSize)) {
						Log.w(TAG,
								"Append buffer to analyzer failed. Not enough space.");
					}
					if (analyzer.analyze()) {
						Log.d(TAG, "analyze SUCC!");
						handler.obtainMessage(0, analyzer.getPacket());
						analyzer.resetForNext();
					} else {
						// Log.d(TAG, "analyze failed..");
					}
				}
			}
			stopRecord();
		}

		public void startRecord() {
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					Constant.WAVE_RATE_INHZ, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize);
			recorder.startRecording();
		}

		public void stopRecord() {
			if (recorder != null) {
				recorder.stop();
				recorder.release();
				recorder = null;
			}
		}
	}

	class FileDecodeTestRunnable implements Runnable {

		private Handler handler;
		private String path = "wavein_AC3_S5570_r_33.wav";

		public FileDecodeTestRunnable(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			try {
				InputStream is = getResources().getAssets().open(path);
				byte[] wavein = new byte[Constant.WAVEOUT_BUF_SIZE];
				is.skip(44);
				DynamicAverageAnalyzer analyzer = new DynamicAverageAnalyzer();
				while (true) {
					int read = is.read(wavein);
					if (read < 0) {
						break;
					}
					if (analyzer.appendBuffer(wavein, 0, read)) {
						break;
					}
					if (analyzer.analyze()) {
						handler.obtainMessage(0, analyzer.getPacket());
						analyzer.resetForNext();
					}
				}
				is.close();
				this.handler.obtainMessage(1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
