package com.dnake.security;

import com.dnake.v700.login;
import com.dnake.v700.security;
import com.dnake.v700.sys;
import com.dnake.widget.Button2;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

@SuppressLint("NewApi")
public class MainActivity extends BaseLabel {

	private Activity ctx;
	private CheckBox[] cbItem = new CheckBox[8];

	@Override
	public void onResume() {
		super.onResume();
		
		TextView t = (TextView)this.findViewById(R.id.main_text_status);
		switch (security.defence)
		{
			case 0:
				t.setText(R.string.main_text_withdraw);
				this.findViewById(R.id.scene_info).setVisibility(RelativeLayout.GONE);
				break;
			case 1:
				t.setText(R.string.main_text_out);
				showSceneInfo(0, R.string.main_text_out);
				break;
			case 2:
				t.setText(R.string.main_text_home);
				showSceneInfo(1, R.string.main_text_home);
				break;
			case 3:
				t.setText(R.string.main_text_sleep);
				showSceneInfo(2, R.string.main_text_sleep);
				break;
		}		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ctx = this;

		Button2 btn;
		btn = (Button2) this.findViewById(R.id.main_btn_defence);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (security.defence == 0 || login.ok()) {
					Intent intent = new Intent(MainActivity.this, DefenceLabel.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} else {
					Intent i = new Intent(MainActivity.this, DefenceLabel.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					LoginLabel login = new LoginLabel();
					login.start(MainActivity.this, i);
				}
			}
		});

		btn = (Button2) this.findViewById(R.id.main_btn_ipc);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, IpcLabel.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		btn = (Button2) this.findViewById(R.id.main_btn_zone);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (security.defence == 0) {
					if (login.ok()) {
						Intent intent = new Intent(MainActivity.this, ZoneLabel.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					} else {
						Intent i = new Intent(MainActivity.this, ZoneLabel.class);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

						LoginLabel login = new LoginLabel();
						login.start(MainActivity.this, i);
					}
				} else {
					Builder b = new AlertDialog.Builder(ctx);
					b.setTitle(R.string.main_prompt_title);
					b.setMessage(R.string.main_prompt_defence);
					b.setPositiveButton(R.string.main_prompt_ok, null);
					b.show();
				}
			}
		});
		btn = (Button2) this.findViewById(R.id.main_btn_scene);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (security.defence == 0) {
					if (login.ok()) {
						Intent intent = new Intent(MainActivity.this, SceneLabel.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					} else {
						Intent i = new Intent(MainActivity.this, SceneLabel.class);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

						LoginLabel login = new LoginLabel();
						login.start(MainActivity.this, i);
					}
				} else {
					Builder b = new AlertDialog.Builder(ctx);
					b.setTitle(R.string.main_prompt_title);
					b.setMessage(R.string.main_prompt_defence);
					b.setPositiveButton(R.string.main_prompt_ok, null);
					b.show();
				}
			}
		});

		btn = (Button2) this.findViewById(R.id.main_btn_setup);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SetupLabel.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		for (int i = 0; i < 8; i++)
		{
			cbItem[i] = (CheckBox) this.findViewById(R.id.scene_item_m0_0 + i);
			//cbItem[i].Checked = true;
		}

		DisplayMetrics dm = new DisplayMetrics();
		android.view.Display d = getWindowManager().getDefaultDisplay();
		d.getMetrics(dm);
		sys.scaled = dm.scaledDensity;

		Intent intent = new Intent(this, security.class);
		this.startService(intent);
	}

	@Override
    public void onStart() {
		super.onStart();

		TextView t = (TextView)this.findViewById(R.id.main_text_status);
		switch(security.defence) {
		case 0:
			t.setText(R.string.main_text_withdraw);
			break;
		case 1:
			t.setText(R.string.main_text_out);
			break;
		case 2:
			t.setText(R.string.main_text_home);
			break;
		case 3:
			t.setText(R.string.main_text_sleep);
			break;
		}
	}

	private void showSceneInfo(int defence,int textId)
	{
		View info = this.findViewById(R.id.scene_info);
		info.setVisibility(RelativeLayout.VISIBLE);

		TextView t = (TextView)info.findViewById(R.id.scene_item_text);
		t.setText(textId);

		for (int i = 0; i < 8; i++)
		{
			cbItem[i].setChecked(security.zone[i].scene[defence] != 0 ? true : false);
		}
	}
}
