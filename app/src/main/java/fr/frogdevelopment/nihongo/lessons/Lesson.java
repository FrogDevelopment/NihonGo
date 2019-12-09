package fr.frogdevelopment.nihongo.lessons;

import androidx.annotation.NonNull;

public class Lesson implements Comparable<Lesson> {

    String code;
    String title;
    boolean isPresent;

    Lesson(String code, String suffix, boolean isPresent) {
        this.code = code;
        this.title = suffix + " " + code;
        this.isPresent = isPresent;
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
