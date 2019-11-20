package fr.frogdevelopment.nihongo.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

import java.io.Serializable;

public class Row implements Serializable {

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

    @ColumnInfo(name = "TAGS")
    public String tags;

}
