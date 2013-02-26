package com.yuyizhao.notepad_yuyizhao;

import com.yuyizhao.notepad_yuyizhao.NotepadContract;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class Notepad extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String[] PROJECTION = new String[] {
			NotepadContract.Notes._ID, // 0
			NotepadContract.Notes.COLUMN_NAME_TITLE, // 1
			NotepadContract.Notes.COLUMN_NAME_NOTE, // 2
	};

	/** The index of the title column */
	private static final int COLUMN_INDEX_TITLE = 1;
	SimpleCursorAdapter adapter;
	private static final String TAG = "Notepad";
	Uri noteUri;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notes_list);
		registerForContextMenu(getListView());
		String[] dataColumns = { NotepadContract.Notes.COLUMN_NAME_TITLE,
				NotepadContract.Notes.COLUMN_NAME_NOTE };

		int[] viewIDs = { R.id.text_title, R.id.text_body };

		adapter = new SimpleCursorAdapter(this, R.layout.notes_row, null,
				dataColumns, viewIDs, 0);
		getLoaderManager().initLoader(0, null, this);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_notepad, menu);
		/*
		 * Intent intent = new Intent(null, getIntent().getData());
		 * intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		 * menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new
		 * ComponentName(this, Notepad.class), null, intent, 0, null);
		 */

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_insert:
			startActivity(new Intent(Intent.ACTION_INSERT, getIntent()
					.getData()));
			return true;

		case R.id.menu_about:
			startActivity(new Intent(this, About.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// The data from the menu item.
		AdapterView.AdapterContextMenuInfo info;

		try {

			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}

		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			return;
		}

		getMenuInflater().inflate(R.menu.activity_notepad_contextmenu, menu);
		// Sets the menu header to be the title of the selected note.
		menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));
		/*
		 * Intent intent = new Intent(null, Uri.withAppendedPath(getIntent()
		 * .getData(), Integer.toString((int) info.id)));
		 * intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		 * menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new
		 * ComponentName(this, Notepad.class), null, intent, 0, null);
		 */
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		// The data from the menu item.
		AdapterView.AdapterContextMenuInfo info;

		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {

			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

		/*
		 * Gets the menu item's ID and compares it to known actions.
		 */
		switch (item.getItemId()) {

		case R.id.menu_delete:
			new DeleteItem().execute(noteUri);

			Toast.makeText(getApplicationContext(), "Deleted",
					Toast.LENGTH_LONG).show();

			return true;
		case R.id.menu_edit:
			startActivity(new Intent(Intent.ACTION_EDIT, noteUri));

			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private class DeleteItem extends AsyncTask<Uri, Void, Void> {

		@Override
		protected Void doInBackground(Uri... Uri) {
			getContentResolver().delete(noteUri, null, null);
			return null;
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		// System.out.println(uri);
		startActivity(new Intent(Intent.ACTION_VIEW, uri));

	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(NotepadContract.Notes.CONTENT_URI);
		}

		return new CursorLoader(this, getIntent().getData(), PROJECTION, null,
				null, NotepadContract.Notes.DEFAULT_SORT_ORDER);

	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		adapter.swapCursor(data);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		adapter.swapCursor(null);
	}

}
