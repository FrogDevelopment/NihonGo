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
    @ColumnInfo(name = "INPUT")
    public String input;

    @NonNull
    @ColumnInfo(name = "SORT_LETTER")
    public String sort_letter;

    @ColumnInfo(name = "KANJI")
    public String kanji;

    @ColumnInfo(name = "KANA")
    public String kana;

    @NonNull
    @ColumnInfo(name = "TYPE")
    public String type;

    @ColumnInfo(name = "TAGS")
    public String tags;

    @ColumnInfo(name = "DETAILS")
    public String details;

    @ColumnInfo(name = "EXAMPLE")
    public String example;

    @ColumnInfo(name = "FAVORITE", defaultValue = "0")
    public boolean bookmark;

    @ColumnInfo(name = "LEARNED", defaultValue = "0")
    public int learned;

    @ColumnInfo(name = "SUCCESS", defaultValue = "0")
    public int success;

    @ColumnInfo(name = "FAILED", defaultValue = "0")
    public int failed;

    public void switchBookmark() {
        bookmark = !bookmark;
    }

}
