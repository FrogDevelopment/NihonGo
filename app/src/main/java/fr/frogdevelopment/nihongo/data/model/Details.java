package fr.frogdevelopment.nihongo.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "dico")
public class Details implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public Integer id;

    @NonNull
    @ColumnInfo(name = "input")
    public String input;

    @NonNull
    @ColumnInfo(name = "sort_letter")
    public String sortLetter;

    @ColumnInfo(name = "kanji")
    public String kanji;

    @ColumnInfo(name = "kana")
    public String kana;

    @ColumnInfo(name = "tags")
    public String tags;

    @ColumnInfo(name = "details")
    public String details;

    @ColumnInfo(name = "example")
    public String example;

    @ColumnInfo(name = "favorite", defaultValue = "0")
    public boolean bookmark;

    @ColumnInfo(name = "learned", defaultValue = "0")
    public int learned;

    @ColumnInfo(name = "success", defaultValue = "0")
    public int success;

    @ColumnInfo(name = "failed", defaultValue = "0")
    public int failed;

    public void switchBookmark() {
        bookmark = !bookmark;
    }

}
