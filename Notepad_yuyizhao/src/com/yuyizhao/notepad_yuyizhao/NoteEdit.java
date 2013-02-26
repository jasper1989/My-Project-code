package com.yuyizhao.notepad_yuyizhao;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class NoteEdit extends Activity {
	// For logging and debugging purposes
	private static final String TAG = "NoteEdit";
	private final int STATE_INSERT = 0;
	private final int STATE_EDIT = 0;
	private String[] projection = { NotepadContract.Notes.COLUMN_NAME_TITLE,
			NotepadContract.Notes.COLUMN_NAME_NOTE };
	private EditText mTitle;
	private EditText mBody;
	private int state;
	private Uri mUri;
	ContentValues value;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_edit);
		mTitle = (EditText) findViewById(R.id.title);
		mBody = (EditText) findViewById(R.id.body);

		Intent intent = getIntent();
		final String action = intent.getAction();
		if (Intent.ACTION_INSERT.equals(action)) {
			state = STATE_INSERT;
			new InsertNote().execute(intent);
		}

		else if (Intent.ACTION_EDIT.equals(action)) {
			state = STATE_EDIT;
			new EditNote().execute(intent);

		} else {
			Log.e(TAG, "Unknown action, exiting");
			finish();
			return;
		}

	}

	private class InsertNote extends AsyncTask<Intent, Void, Void> {

		@Override
		protected Void doInBackground(Intent... Intent) {
			mUri = getContentResolver().insert(Intent[0].getData(), null);
			return null;
		}

	}

	private class EditNote extends AsyncTask<Intent, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Intent... Intent) {
			mUri = Intent[0].getData();
			Cursor mCursor = getContentResolver().query(mUri, projection, null,
					null, null);
			return mCursor;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			int index_title = result
					.getColumnIndexOrThrow(NotepadContract.Notes.COLUMN_NAME_TITLE);
			int index_note = result
					.getColumnIndexOrThrow(NotepadContract.Notes.COLUMN_NAME_NOTE);
			if (result != null) {
				while (result.moveToNext()) {
					mTitle.setText(result.getString(index_title));
					mBody.setText(result.getString(index_note));
				}
			}
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.activity_note_edit, menu);
		/*
		 * Intent intent = new Intent(null, getIntent().getData());
		 * intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		 * menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new
		 * ComponentName(this, NoteEdit.class), null, intent, 0, null);
		 */

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:
			String title = mTitle.getText().toString();
			String body = mBody.getText().toString();
			updateNote(title, body);
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateNote(String title, String body) {
		value = new ContentValues();
		value.put(NotepadContract.Notes.COLUMN_NAME_TITLE, title);
		value.put(NotepadContract.Notes.COLUMN_NAME_NOTE, body);
		new UpdateNote().execute(value);

	}

	private class UpdateNote extends AsyncTask<ContentValues, Void, Void> {

		@Override
		protected Void doInBackground(ContentValues... ContentValues) {
			getContentResolver().update(mUri, value, null, null);
			return null;
		}

	}
}
