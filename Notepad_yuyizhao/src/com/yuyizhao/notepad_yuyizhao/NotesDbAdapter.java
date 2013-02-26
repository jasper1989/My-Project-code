package com.yuyizhao.notepad_yuyizhao;

import com.yuyizhao.notepad_yuyizhao.NotepadContract;
import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class NotesDbAdapter extends ContentProvider {

	public static final String DATABASE_NAME = "note_pad.db";
	public static final int DATABASE_VERSION = 2;
	private static final String TAG = "NotesDbAdapter";
	private DbHelper mDbHelper;

	// The incoming URI matches the Notes URI pattern
	private static final int NOTES = 1;

	// The incoming URI matches the Note ID URI pattern
	private static final int NOTE_ID = 2;
	private static final UriMatcher sUriMatcher;
	private static HashMap<String, String> sNotesProjectionMap;

	/**
	 * A block that instantiates and sets static objects
	 */
	static {

		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(NotepadContract.AUTHORITY, "notes", NOTES);
		sUriMatcher.addURI(NotepadContract.AUTHORITY, "notes/#", NOTE_ID);

		sNotesProjectionMap = new HashMap<String, String>();
		// Maps the string "_ID" to the column name "_ID"
		sNotesProjectionMap.put(NotepadContract.Notes._ID,
				NotepadContract.Notes._ID);

		// Maps "title" to "title"
		sNotesProjectionMap.put(NotepadContract.Notes.COLUMN_NAME_TITLE,
				NotepadContract.Notes.COLUMN_NAME_TITLE);

		// Maps "note" to "note"
		sNotesProjectionMap.put(NotepadContract.Notes.COLUMN_NAME_NOTE,
				NotepadContract.Notes.COLUMN_NAME_NOTE);

		// Maps "created" to "created"
		sNotesProjectionMap.put(NotepadContract.Notes.COLUMN_NAME_CREATE_DATE,
				NotepadContract.Notes.COLUMN_NAME_CREATE_DATE);

		// Maps "modified" to "modified"
		sNotesProjectionMap.put(
				NotepadContract.Notes.COLUMN_NAME_MODIFICATION_DATE,
				NotepadContract.Notes.COLUMN_NAME_MODIFICATION_DATE);

	}

	public class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + NotepadContract.Notes.TABLE_NAME
					+ " (" + NotepadContract.Notes._ID
					+ " INTEGER PRIMARY KEY,"
					+ NotepadContract.Notes.COLUMN_NAME_TITLE + " TEXT,"
					+ NotepadContract.Notes.COLUMN_NAME_NOTE + " TEXT,"
					+ NotepadContract.Notes.COLUMN_NAME_CREATE_DATE
					+ " INTEGER,"
					+ NotepadContract.Notes.COLUMN_NAME_MODIFICATION_DATE
					+ " INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			// Logs that the database is being upgraded
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");

			// Kills the table and existing data
			db.execSQL("DROP TABLE IF EXISTS notes");

			// Recreates the database with a new version
			onCreate(db);
		}

	}

	@Override
	public boolean onCreate() {
		mDbHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(NotepadContract.Notes.TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		// If the incoming URI is for notes, chooses the Notes projection
		case NOTES:
			qb.setProjectionMap(sNotesProjectionMap);
			break;

		/*
		 * If the incoming URI is for a single note identified by its ID,
		 * chooses the note ID projection, and appends "_ID = <noteID>" to the
		 * where clause, so that it selects that single note
		 */
		case NOTE_ID:
			qb.setProjectionMap(sNotesProjectionMap);
			qb.appendWhere(NotepadContract.Notes._ID + // the name of the ID
														// column
					"=" +
					// the position of the note ID itself in the incoming URI
					uri.getPathSegments().get(
							NotepadContract.Notes.NOTE_ID_PATH_POSITION));
			break;

		default:
			// If the URI doesn't match any of the known patterns, throw an
			// exception.
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		String orderBy;
		// If no sort order is specified, uses the default
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = NotepadContract.Notes.DEFAULT_SORT_ORDER;
		} else {
			// otherwise, uses the incoming sort order
			orderBy = sortOrder;
		}

		// Opens the database object in "read" mode, since no writes need to be
		// done.
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		/*
		 * Performs the query. If no problems occur trying to read the database,
		 * then a Cursor object is returned; otherwise, the cursor variable
		 * contains null. If no records were selected, then the Cursor object is
		 * empty, and Cursor.getCount() returns 0.
		 */
		Cursor c = qb.query(db, // The database to query
				projection, // The columns to return from the query
				selection, // The columns for the where clause
				selectionArgs, // The values for the where clause
				null, // don't group the rows
				null, // don't filter by row groups
				orderBy // The sort order
				);

		// Tells the Cursor what URI to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != NOTES) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;

		// If the incoming values map is not null, uses it for the new values.
		if (initialValues != null) {
			values = new ContentValues(initialValues);

		} else {
			// Otherwise, create a new value map
			values = new ContentValues();
		}

		if (values.containsKey(NotepadContract.Notes.COLUMN_NAME_NOTE) == false) {
			values.put(NotepadContract.Notes.COLUMN_NAME_NOTE, "");
		}

		if (values.containsKey(NotepadContract.Notes.COLUMN_NAME_TITLE) == false) {
			values.put(NotepadContract.Notes.COLUMN_NAME_TITLE, "");
		}
		// Gets the current system time in milliseconds
		Long now = Long.valueOf(System.currentTimeMillis());

		// If the values map doesn't contain the creation date, sets the value
		// to the current time.
		if (values.containsKey(NotepadContract.Notes.COLUMN_NAME_CREATE_DATE) == false) {
			values.put(NotepadContract.Notes.COLUMN_NAME_CREATE_DATE, now);
		}
		// If the values map doesn't contain the modification date, sets the
		// value to the current
		// time.
		if (values
				.containsKey(NotepadContract.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
			values.put(NotepadContract.Notes.COLUMN_NAME_MODIFICATION_DATE, now);
		}

		// Opens the database object in "write" mode.
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// Performs the insert and returns the ID of the new note.
		long rowId = db.insert(NotepadContract.Notes.TABLE_NAME, // The table to
																	// insert
																	// into.
				NotepadContract.Notes.COLUMN_NAME_NOTE, // A hack, SQLite sets
														// this column value to
														// null if values is
														// empty.
				values // A map of column names, and the values to insert
						// into the columns.
				);

		// If the insert succeeded, the row ID exists.
		if (rowId > 0) {
			// Creates a URI with the note ID pattern and the new row ID
			// appended to it.
			Uri noteUri = ContentUris.withAppendedId(
					NotepadContract.Notes.CONTENT_ID_URI_BASE, rowId);

			// Notifies observers registered against this provider that the data
			// changed.
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		// If the insert didn't succeed, then the rowID is <= 0. Throws an
		// exception.
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {

		// Opens the database object in "write" mode.
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		String finalWhere;

		// Does the delete based on the incoming URI pattern.
		switch (sUriMatcher.match(uri)) {

		// If the incoming pattern matches the general pattern for notes, does a
		// delete
		// based on the incoming "where" columns and arguments.
		case NOTES:
			count = db.delete(NotepadContract.Notes.TABLE_NAME, // The database
																// table name
					where, // The incoming where clause column names
					whereArgs // The incoming where clause values
					);
			break;

		// If the incoming URI matches a single note ID, does the delete based
		// on the
		// incoming data, but modifies the where clause to restrict it to the
		// particular note ID.
		case NOTE_ID:
			/*
			 * Starts a final WHERE clause by restricting it to the desired note
			 * ID.
			 */
			finalWhere = NotepadContract.Notes._ID + // The ID column name
					" = " + // test for equality
					uri.getPathSegments(). // the incoming note ID
							get(NotepadContract.Notes.NOTE_ID_PATH_POSITION);

			// If there were additional selection criteria, append them to the
			// final
			// WHERE clause
			if (where != null) {
				finalWhere = finalWhere + " AND " + where;
			}

			// Performs the delete.
			count = db.delete(NotepadContract.Notes.TABLE_NAME, // The database
																// table
					// name.
					finalWhere, // The final WHERE clause
					whereArgs // The incoming where clause values.
					);
			break;

		// If the incoming pattern is invalid, throws an exception.
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		/*
		 * Gets a handle to the content resolver object for the current context,
		 * and notifies it that the incoming URI changed. The object passes this
		 * along to the resolver framework, and observers that have registered
		 * themselves for the provider are notified.
		 */
		getContext().getContentResolver().notifyChange(uri, null);

		// Returns the number of rows deleted.
		return count;
	}

	@Override
	public String getType(Uri uri) {

		/**
		 * Chooses the MIME type based on the incoming URI pattern
		 */
		switch (sUriMatcher.match(uri)) {

		// If the pattern is for notes or live folders, returns the general
		// content type.
		case NOTES:
			return NotepadContract.Notes.CONTENT_TYPE;

			// If the pattern is for note IDs, returns the note ID content type.
		case NOTE_ID:
			return NotepadContract.Notes.CONTENT_ITEM_TYPE;

			// If the URI pattern doesn't match any permitted patterns, throws
			// an exception.
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {

		// Opens the database object in "write" mode.
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		String finalWhere;

		// Does the update based on the incoming URI pattern
		switch (sUriMatcher.match(uri)) {

		// If the incoming URI matches the general notes pattern, does the
		// update based on
		// the incoming data.
		case NOTES:

			// Does the update and returns the number of rows updated.
			count = db.update(NotepadContract.Notes.TABLE_NAME, // The database
																// table
					// name.
					values, // A map of column names and new values to use.
					where, // The where clause column names.
					whereArgs // The where clause column values to select on.
					);
			break;

		// If the incoming URI matches a single note ID, does the update based
		// on the incoming
		// data, but modifies the where clause to restrict it to the particular
		// note ID.
		case NOTE_ID:
			/*
			 * Starts creating the final WHERE clause by restricting it to the
			 * incoming note ID.
			 */
			finalWhere = NotepadContract.Notes._ID + // The ID column name
					" = " + // test for equality
					uri.getPathSegments(). // the incoming note ID
							get(NotepadContract.Notes.NOTE_ID_PATH_POSITION);

			// If there were additional selection criteria, append them to the
			// final WHERE
			// clause
			if (where != null) {
				finalWhere = finalWhere + " AND " + where;
			}

			// Does the update and returns the number of rows updated.
			count = db.update(NotepadContract.Notes.TABLE_NAME, // The database
																// table
					// name.
					values, // A map of column names and new values to use.
					finalWhere, // The final WHERE clause to use
								// placeholders for whereArgs
					whereArgs // The where clause column values to select on, or
								// null if the values are in the where argument.
					);
			break;
		// If the incoming pattern is invalid, throws an exception.
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		// Returns the number of rows updated.
		return count;
	}

}
