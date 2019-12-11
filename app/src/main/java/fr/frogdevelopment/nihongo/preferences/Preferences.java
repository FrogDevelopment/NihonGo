package fr.frogdevelopment.nihongo.preferences;

public enum Preferences {

    ON_BOARDING_DONE("ON_BOARDING"),
    SKIP_WARNING_IME("remember_warning_ime"),
    LESSONS("lessons");

    public final String code;

    Preferences(String code) {
        this.code = code;
    }
}
