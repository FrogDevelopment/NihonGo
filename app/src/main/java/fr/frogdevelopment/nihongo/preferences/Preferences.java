package fr.frogdevelopment.nihongo.preferences;

public enum Preferences {

    HELP_DETAILS("help_details"),
    HELP_DICO("help_dico"),
    HELP_REVIEW("help_review"),
    SKIP_WARNING_IME("remember_warning_ime"),
    LESSONS("lessons");

    public final String code;

    Preferences(String code) {
        this.code = code;
    }
}
