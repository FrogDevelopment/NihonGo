/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico.input;

/**
 * @see www.rikai.com/library/kanjitables/kanji_codes.unicode.shtml
 * @see en.wikipedia.org/wiki/Japanese_writing_system
 */
public class InputUtils {

    // Japanese-style punctuation ( 3000 - 303f)
    private static final int RANGE_PUNCTUATION_START = 0x3000;
    private static final int RANGE_PUNCTUATION_END = 0x303F;

    // Hiragana ( 3040 - 309f)
    private static final int RANGE_HIRAGANA_START = 0x3040;
    private static final int RANGE_HIRAGANA_END = 0x309F;

    // Katakana ( 30a0 - 30ff)
    private static final int RANGE_KATAKANA_START = 0x30A0;
    private static final int RANGE_KATAKANA_END = 0x30FF;

    // Full-width roman characters and half-width katakana ( ff00 - ffef)
    private static final int RANGE_ROMAN_START = 0xFF00;
    private static final int RANGE_ROMAN_END = 0xFFEF;

    //    CJK unifed ideographs - Common and uncommon kanji ( 4e00 - 9fbf)
    private static final int RANGE_KANJI_START = 0x4E00;
    private static final int RANGE_KANJI_END = 0x9fbf;

    public static boolean isOnlyHiragana(final String input) {
        return isOnRange(input, RANGE_HIRAGANA_START, RANGE_HIRAGANA_END);
    }

    public static boolean isOnlyKatakana(final String input) {
        return isOnRange(input, RANGE_KATAKANA_START, RANGE_KATAKANA_END);
    }

    public static boolean isOnlyKanji(final String input) {
        return isOnRange(input, RANGE_KANJI_START, RANGE_KANJI_END);
    }

    private static boolean isOnRange(final String input, final int startRange, final int endRange) {
        for (char ch : input.toCharArray()) {
            if (ch >= RANGE_PUNCTUATION_START && ch <= RANGE_PUNCTUATION_END) { // PUNCTUATION
                continue;
            }

            if (ch < startRange || ch > endRange) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOnlyJapanese(String input) {
        boolean isOnlyJapanaese = true;
        for (Character ch : input.toCharArray()) {
            if (ch >= RANGE_PUNCTUATION_START && ch <= RANGE_PUNCTUATION_END) { // PUNCTUATION
                continue;
            } else if (ch >= RANGE_ROMAN_START && ch <= RANGE_ROMAN_END) { // ROMAN
                continue;
            } else if (ch >= RANGE_HIRAGANA_START && ch <= RANGE_HIRAGANA_END) { // HIRAGANA
                continue;
            } else if (ch >= RANGE_KATAKANA_START && ch <= RANGE_KATAKANA_END) { // KATAKANA
                continue;
            } else if (ch >= RANGE_KANJI_START && ch <= RANGE_KANJI_END) { // KANJI
                continue;
            } else if (ch.equals('~')) { // FIXME
                continue;
            } else {
                isOnlyJapanaese = false;
                break;
            }
        }
        return isOnlyJapanaese;
    }

    public static boolean isOnlyKana(final String input) {
        boolean isOnlyJapanaese = true;
        for (Character ch : input.toCharArray()) {
            if (ch >= RANGE_PUNCTUATION_START && ch <= RANGE_PUNCTUATION_END) { // PUNCTUATION
                continue;
            } else if (ch >= RANGE_ROMAN_START && ch <= RANGE_ROMAN_END) { // ROMAN
                continue;
            } else if (ch >= RANGE_HIRAGANA_START && ch <= RANGE_HIRAGANA_END) { // HIRAGANA
                continue;
            } else if (ch >= RANGE_KATAKANA_START && ch <= RANGE_KATAKANA_END) { // KATAKANA
                continue;
            } else {
                isOnlyJapanaese = false;
                break;
            }
        }
        return isOnlyJapanaese;
    }

    public static boolean containsJapanese(String input) {
        boolean containsJapanase = false;
        for (char ch : input.toCharArray()) {
            if (ch >= RANGE_HIRAGANA_START && ch <= RANGE_HIRAGANA_END) { // HIRAGANA
                containsJapanase = true;
                break;
            } else if (ch >= RANGE_KATAKANA_START && ch <= RANGE_KATAKANA_END) { // KATAKANA
                containsJapanase = true;
                break;
            } else if (ch >= RANGE_KANJI_START && ch <= RANGE_KANJI_END) { // KANJI
                containsJapanase = true;
                break;
            }
        }
        return containsJapanase;
    }

    public static boolean containsKanji(String input) {
        boolean containsKanji = false;
        for (char ch : input.toCharArray()) {
            if (ch >= RANGE_KANJI_START && ch <= RANGE_KANJI_END) { // KANJI
                containsKanji = true;
                break;
            }
        }
        return containsKanji;
    }

}
