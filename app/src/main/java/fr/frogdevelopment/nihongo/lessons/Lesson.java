package fr.frogdevelopment.nihongo.lessons;

import androidx.annotation.NonNull;

public class Lesson implements Comparable<Lesson> {

    public String code;
    public String title;
    public boolean isPresent;

    public Lesson(String code, String title, boolean isPresent) {
        this.code = code;
        this.title = title;
        this.isPresent = isPresent;
    }

    public String getCode() {
        return code;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }

    @Override
    public int compareTo(@NonNull Lesson another) {
        return code.compareTo(another.code);
    }
}
