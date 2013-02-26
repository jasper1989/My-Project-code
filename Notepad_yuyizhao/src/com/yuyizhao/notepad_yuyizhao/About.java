package com.yuyizhao.notepad_yuyizhao;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class About extends Activity {
	private Button button_log = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_about);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		button_log = (Button) findViewById(R.id.button_log);
		button_log.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent_log = new Intent(About.this, Log.class);
				startActivity(intent_log);

			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;

		/*case R.id.menu_welcome:
			Intent i_welcome = new Intent(this, Welcome.class);
			startActivity(i_welcome);
			return true;
*/
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_note_about, menu);
		return true;
	}

}
