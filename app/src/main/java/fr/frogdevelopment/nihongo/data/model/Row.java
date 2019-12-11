package fr.frogdevelopment.nihongo.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

import java.io.Serializable;

public class Row implements Serializable {

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

}
