package com.yuyizhao.notepad_yuyizhao;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NotepadContract {
	// This class cannot be instantiated
	private NotepadContract() {
	}

	public static final String AUTHORITY = "com.yuyizhao.notepad_yuyizhao.provider";

	public static final class Notes implements BaseColumns {
		// This class cannot be instantiated
		private Notes() {
		}

		// The scheme part for this provider's URI
		private static final String SCHEME = "content://";

		public static final String TABLE_NAME = "notes";
		public static final String PATH_NOTES = "/notes";
		public static final String PATH_NOTE_ID = "/notes/";

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_NOTES);

		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME
				+ AUTHORITY + PATH_NOTE_ID);

		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME
				+ AUTHORITY + PATH_NOTE_ID + "/#");

		/**
		 * 0-relative position of a note ID segment in the path part of a note
		 * ID URI
		 */
		public static final int NOTE_ID_PATH_POSITION = 1;

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yuyizhao.notepad_yuyizhao.note";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.yuyizhao.notepad_yuyizhao.note";
		public static final String DEFAULT_SORT_ORDER = "modified DESC";

		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_NOTE = "note";
		public static final String COLUMN_NAME_CREATE_DATE = "created";
		public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";

	}
}