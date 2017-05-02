/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import fr.frogdevelopment.nihongo.contentprovider.DicoContract;

public class Item implements Row, Parcelable {

	public final String id;
	public String input;
	public String sort_letter;
	public String kanji;
	public String kana;
	public String tags;
	public String details;
	public String example;
	public boolean bookmark;
	public int learned;
	public int success;
	public int failed;

	public boolean isBookmarked() {
		return bookmark;
	}

	public void switchBookmark() {
		bookmark = !bookmark;
	}

	public Item(Cursor cursor) {
		id = cursor.getString(DicoContract.INDEX_ID);
		input = cursor.getString(DicoContract.INDEX_INPUT);
		sort_letter = cursor.getString(DicoContract.INDEX_SORT_LETTER);
		kanji = cursor.getString(DicoContract.INDEX_KANJI);
		kana = cursor.getString(DicoContract.INDEX_KANA);
		tags = cursor.getString(DicoContract.INDEX_TAGS);
		details = cursor.getString(DicoContract.INDEX_DETAILS);
		example = cursor.getString(DicoContract.INDEX_EXAMPLE);
		bookmark = cursor.getInt(DicoContract.INDEX_BOOKMARK) == 1;
		learned = cursor.getInt(DicoContract.INDEX_LEARNED);
		success = cursor.getInt(DicoContract.INDEX_SUCCESS);
		failed = cursor.getInt(DicoContract.INDEX_FAILED);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(id);
		out.writeString(input);
		out.writeString(sort_letter);
		out.writeString(kanji);
		out.writeString(kana);
		out.writeString(details);
		out.writeString(example);
		out.writeString(tags);
		out.writeInt(bookmark ? 1 : 0);
		out.writeInt(learned);
		out.writeInt(success);
		out.writeInt(failed);
	}

	public static final Creator<Item> CREATOR = new Creator<Item>() {
		public Item createFromParcel(Parcel in) {
			return new Item(in);
		}

		public Item[] newArray(int size) {
			return new Item[size];
		}
	};

	private Item(Parcel in) {
		id = in.readString();
		input = in.readString();
		sort_letter = in.readString();
		kanji = in.readString();
		kana = in.readString();
		details = in.readString();
		example = in.readString();
		tags = in.readString();
		bookmark = in.readInt() == 1;
		learned = in.readInt();
		success = in.readInt();
		failed = in.readInt();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Item item = (Item) o;

		return id.equals(item.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
