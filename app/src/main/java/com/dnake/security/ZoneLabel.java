package com.dnake.security;

import com.dnake.v700.security;
import com.dnake.v700.slaves;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ZoneLabel extends BaseLabel {
	private Spinner [] sp_type = new Spinner[8];
	private Spinner [] sp_delay = new Spinner[8];
	private Spinner [] sp_sensor = new Spinner[8];
	private Spinner [] sp_mode = new Spinner[8];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zone);

		ArrayAdapter<CharSequence> ad = ArrayAdapter.createFromResource(this, R.array.zone_type_arrays, R.layout.spinner_text);
		ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<CharSequence> ad2 = ArrayAdapter.createFromResource(this, R.array.zone_delay_arrays, R.layout.spinner_text);
		ad2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<CharSequence> ad3 = ArrayAdapter.createFromResource(this, R.array.zone_sensor_arrays, R.layout.spinner_text);
		ad3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<CharSequence> ad4 = ArrayAdapter.createFromResource(this, R.array.zone_mode_arrays, R.layout.spinner_text);
		ad4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		for(int i=0; i<8; i++) {
			sp_type[i] = (Spinner)this.findViewById(R.id.zone_type_0+i*5);
			sp_delay[i] = (Spinner)this.findViewById(R.id.zone_delay_0+i*5);
			sp_sensor[i] = (Spinner)this.findViewById(R.id.zone_sensor_0+i*5);
			sp_mode[i] = (Spinner)this.findViewById(R.id.zone_mode_0+i*5);

			sp_type[i].setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
					int z = 0;
					for(int i=0; i<8; i++) {
						if (arg0.getId() == sp_type[i].getId()) {
							z = i;
							break;
						}
					}
					if (pos > 0) {
						sp_delay[z].setSelection(0);
						sp_delay[z].setEnabled(false);
					} else {
						sp_delay[z].setEnabled(true);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
			sp_type[i].setAdapter(ad);
			sp_type[i].setSelection(security.zone[i].type);

			sp_delay[i].setAdapter(ad2);
			sp_delay[i].setSelection(security.zone[i].delay);

			sp_sensor[i].setAdapter(ad3);
			sp_sensor[i].setSelection(security.zone[i].sensor);

			sp_mode[i].setAdapter(ad4);
			sp_mode[i].setSelection(security.zone[i].mode);
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		for(int i=0; i<8; i++) {
			security.zone[i].type = sp_type[i].getSelectedItemPosition();
			security.zone[i].delay = sp_delay[i].getSelectedItemPosition();
			security.zone[i].sensor = sp_sensor[i].getSelectedItemPosition();
			security.zone[i].mode = sp_mode[i].getSelectedItemPosition();
		}
		security.save();
		slaves.setMarks(0x01);
	}
}
