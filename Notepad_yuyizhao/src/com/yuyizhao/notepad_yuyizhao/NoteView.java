package com.yuyizhao.notepad_yuyizhao;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class NoteView extends FragmentActivity implements
		CustomDialogFragment.CustomDialogListener {
	private Uri mUri;
	private String[] projection = { NotepadContract.Notes.COLUMN_NAME_NOTE,
			NotepadContract.Notes.COLUMN_NAME_TITLE };
	private Cursor mCursor;
	private TextView mTextView;
	String mtitle;
	String mbody;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_view);
		mTextView = (TextView) findViewById(R.id.view_body);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent intent = getIntent();
		mUri = intent.getData();
		new ViewNotes().execute(mUri);

	}

	private class ViewNotes extends AsyncTask<Uri, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Uri... Uri) {
			mCursor = getContentResolver().query(mUri, projection, null, null,
					null);
			return mCursor;
		}

		protected void onPostExecute(Cursor cursor) {
			int index_note = cursor
					.getColumnIndexOrThrow(NotepadContract.Notes.COLUMN_NAME_NOTE);

			int index_title = cursor
					.getColumnIndexOrThrow(NotepadContract.Notes.COLUMN_NAME_TITLE);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					mbody = cursor.getString(index_note);
					mtitle = cursor.getString(index_title);
					mTextView.setText(mbody);
					setTitle(mtitle);

				}
			}
		}

	}

	public void showNoticeDialog() {
		// Create an instance of the dialog fragment and show it
		CustomDialogFragment dialog = new CustomDialogFragment();
		dialog.show(getSupportFragmentManager(), "CustomDialogFragment");
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		new DeleteNote().execute(mUri);
		Toast.makeText(getApplicationContext(),
				"the note " + mtitle + " is Deleted", Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		return;
	}

	private class DeleteNote extends AsyncTask<Uri, Void, Void> {

		@Override
		protected Void doInBackground(Uri... Uri) {
			getContentResolver().delete(mUri, null, null);
			return null;

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_note_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			NavUtils.navigateUpFromSameTask(this);
			return true;

		case R.id.menu_edit:
			startActivity(new Intent(Intent.ACTION_EDIT, mUri));
			finish();
			return true;
		case R.id.menu_delete:

			showNoticeDialog();

			return true;

		}
		return super.onOptionsItemSelected(item);
	}

}
