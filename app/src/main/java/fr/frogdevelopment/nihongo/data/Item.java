package fr.frogdevelopment.nihongo.data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;

public class Item implements Row, Parcelable {

    public final String id;
    public final String input;
    public final String sort_letter;
    public final String kanji;
    public final String kana;
    public final String tags;
    public final String details;
    public String favorite;

    public Item(Cursor cursor) {
        id = cursor.getString(DicoContract.INDEX_ID);
        input = cursor.getString(DicoContract.INDEX_INPUT);
        sort_letter = cursor.getString(DicoContract.INDEX_SORT_LETTER);
        kanji = cursor.getString(DicoContract.INDEX_KANJI);
        kana = cursor.getString(DicoContract.INDEX_KANA);
        tags = cursor.getString(DicoContract.INDEX_TAGS);
        details = cursor.getString(DicoContract.INDEX_DETAILS);
        favorite = cursor.getString(DicoContract.INDEX_FAVORITE);
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
        out.writeString(tags);
        out.writeString(favorite);
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
        tags = in.readString();
        favorite = in.readString();
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


    public Intent getUpdateIntent(Context packageContext, Type type) {
        Bundle args = new Bundle();
        args.putString(DicoContract._ID, id);
        args.putString(DicoContract.INPUT, input);
        args.putString(DicoContract.KANJI, kanji);
        args.putString(DicoContract.KANA, kana);
        args.putString(DicoContract.TAGS, tags);
        args.putString(DicoContract.DETAILS, details);
        args.putString(DicoContract.FAVORITE, favorite);

        Intent intent = new Intent(packageContext, InputActivity.class);
        intent.putExtras(args);
        intent.putExtra("type", type);

        return intent;
    }
}
