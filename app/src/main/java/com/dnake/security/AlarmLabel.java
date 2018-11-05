package com.dnake.security;

import com.dnake.handler.DefenceHelper;
import com.dnake.v700.ioctl;
import com.dnake.v700.security;
import com.dnake.v700.slaves;
import com.dnake.v700.sound;
import com.dnake.widget.Button2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class AlarmLabel extends BaseLabel {
	public static Intent intent = null;

	private MediaPlayer mPlayer = null;
	private int iMode = 1000;

	private TextView mTitle;
	private ImageView mBkg;
	private EditText mPasswd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm);

		mTitle = (TextView)this.findViewById(R.id.alarm_title);
		mBkg = (ImageView)this.findViewById(R.id.alarm_bkg);

		mPasswd = (EditText)this.findViewById(R.id.alarm_passwd);
		mPasswd.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				if (arg1 == EditorInfo.IME_ACTION_DONE)
					withdraw();
				return false;
			}
		});

		Button2 b = (Button2) this.findViewById(R.id.alarm_btn_ok);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				withdraw();
			}
		});
	}

	public void withdraw() {
		String s = mPasswd.getText().toString();
		if (s.equals(security.passwd)) {
//			security.setDefence(security.WITHDRAW);
//			slaves.setMarks(0x01);
			DefenceHelper.setWithdraw();
			security.withdraw();
		} else {
			sound.play(sound.passwd_err, false);
		}
	}

	private void startPlayer(String url, Boolean looping) {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}

		OnCompletionListener listener = new OnCompletionListener() {
			public void onCompletion(MediaPlayer p) {
				mPlayer.release();
				mPlayer = null;
			}
		};

		mPlayer = sound.play(url, looping, listener);
	}

	private void stopPlayer() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	private int mTmIdx = 100;
	private int mBkgIdx = 0;

	public static int TIMEOUT = 5*60*1000;

	@Override
	public void onTimer() {
		super.onTimer();

		if (security.mHave == false) {
			this.tStop();
			finish();
		} else {
			if (Math.abs(System.currentTimeMillis()-security.mAts) < TIMEOUT) {
				int m = 0;
				for(int i=0; i<security.MAX; i++) {
					if (security.isZoneAlarm(i)) {
						m = 1;
						break;
					}
				}
				if (m != iMode) {
					if (m == 0) {
						this.startPlayer(sound.alarm_delay, true);
					} else if (m == 1) {
						this.startPlayer(sound.alarm, true);
						ioctl.hooter(1);
					}
					iMode = m;
				} else {
					if (mPlayer == null) {
						if (m == 0) {
							this.startPlayer(sound.alarm_delay, true);
						} else if (m == 1) {
							this.startPlayer(sound.alarm, true);
							ioctl.hooter(1);
						}
					}
				}
				if (mTmIdx%5 == 1) {
					if (mBkgIdx == 0) {
						mBkg.setBackground(this.getResources().getDrawable(R.drawable.alarm_bkg));
						mBkgIdx = 1;
					} else {
						mBkg.setBackground(this.getResources().getDrawable(R.drawable.alarm_bkg2));
						mBkgIdx = 0;
					}
				}

				if (mTmIdx++ >= 10) {
					mTmIdx = 0;

					String [] sensor = this.getResources().getStringArray(R.array.zone_sensor_arrays);
					String s = new String();
					for(int i=0; i<security.MAX; i++) {
						if (security.mIoSt[i]) {
							s += (i+1)+":"+sensor[security.zone[i].sensor];
						}
					}
					mTitle.setText(s);
				}
				WakeTask.acquire();
			} else {
				this.stopPlayer();
				ioctl.hooter(0);
			}
		}
	}

	@Override
    public void onStart() {
		bFinish = false;

		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		intent = null;

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.stopPlayer();
		ioctl.hooter(0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true;
		return super.onKeyDown(keyCode, event);
	}
}
