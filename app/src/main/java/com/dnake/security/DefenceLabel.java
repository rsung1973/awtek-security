package com.dnake.security;

import com.dnake.v700.security;
import com.dnake.v700.slaves;
import com.dnake.widget.Button2;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class DefenceLabel extends BaseLabel {
	private Button2 btn_out, btn_home, btn_sleep, btn_withdraw;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.defence);

		btn_out = (Button2)this.findViewById(R.id.defence_btn_out);
		btn_out.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				security.setDefence(security.OUT);
				slaves.setMarks(0x01);
				load_st();
			}
		});
		btn_home = (Button2)this.findViewById(R.id.defence_btn_home);
		btn_home.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				security.setDefence(security.HOME);
				slaves.setMarks(0x01);
				load_st();
			}
		});
		btn_sleep = (Button2)this.findViewById(R.id.defence_btn_sleep);
		btn_sleep.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				security.setDefence(security.SLEEP);
				slaves.setMarks(0x01);
				load_st();
			}
		});
		btn_withdraw = (Button2)this.findViewById(R.id.defence_btn_withdraw);
		btn_withdraw.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				security.setDefence(security.WITHDRAW);
				slaves.setMarks(0x01);
				load_st();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		this.load_st();
	}

	public void load_st() {
		btn_out.setBackgroundDrawable(getResources().getDrawable(R.drawable.defence_btn_out_a));
		btn_home.setBackgroundDrawable(getResources().getDrawable(R.drawable.defence_btn_home_a));
		btn_sleep.setBackgroundDrawable(getResources().getDrawable(R.drawable.defence_btn_sleep_a));
		btn_withdraw.setBackgroundDrawable(getResources().getDrawable(R.drawable.defence_btn_withdraw_a));

		switch(security.defence) {
		case 0:
			btn_withdraw.setBackgroundDrawable(getResources().getDrawable(R.drawable.defence_btn_withdraw_b));
			break;
		case 1:
			btn_out.setBackgroundDrawable(getResources().getDrawable(R.drawable.defence_btn_out_b));
			break;
		case 2:
			btn_home.setBackgroundDrawable(getResources().getDrawable(R.drawable.defence_btn_home_b));
			break;
		case 3:
			btn_sleep.setBackgroundDrawable(getResources().getDrawable(R.drawable.defence_btn_sleep_b));
			break;
		}
	}
}
