package fr.frogdevelopment.nihongo;

public enum Preferences {

    PREFS_NAME("NihonGoPref"),
    REMEMBER_WARNING_IME("rememberWarningIME"),
    PACKS("packs");

    public String value;

    private Preferences(java.lang.String value) {
        this.value = value;
    }
}
