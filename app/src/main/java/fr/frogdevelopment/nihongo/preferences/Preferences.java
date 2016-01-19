/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.preferences;

public enum Preferences {

    REMEMBER_WARNING_IME("rememberWarningIME"),
    PACKS("packs"),
    NO_ADVERTISING("sku_no_advertissing");

    public final String code;

    Preferences(String code) {
        this.code = code;
    }
}
