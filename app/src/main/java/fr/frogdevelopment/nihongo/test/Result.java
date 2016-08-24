/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

import fr.frogdevelopment.nihongo.dico.input.InputUtils;

class Result implements Parcelable {

    public boolean success;
    public boolean almost;
    private final boolean inputTest;
    public final String test;
    public final String answerExpected;
    public String answerGiven;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (success ? 1 : 0));
        out.writeByte((byte) (almost ? 1 : 0));
        out.writeByte((byte) (inputTest ? 1 : 0));
        out.writeString(test);
        out.writeString(answerExpected);
        out.writeString(answerGiven);
    }

    public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

    private Result(Parcel in) {
        success = in.readByte() != 0;
        almost = in.readByte() != 0;
        inputTest = in.readByte() != 0;
        test = in.readString();
        answerExpected = in.readString();
        answerGiven = in.readString();
    }

    Result(CharSequence test, CharSequence answerExpected, boolean inputTest) {
        super();
        this.test = test.toString();
        this.answerExpected = answerExpected.toString();
        this.inputTest = inputTest;
    }

    boolean setAnswerGiven(CharSequence answerGiven) {
        this.answerGiven = answerGiven.toString();

        if (InputUtils.containsJapanese(answerExpected)) {
            // fixme
            this.answerGiven = this.answerGiven.replace('~', InputUtils.WAVE_DASH);
        }

        if (StringUtils.equalsIgnoreCase(answerExpected, this.answerGiven)) {
            success = true;
        } else if (inputTest) {
            String splitChar;
            if (this.answerGiven.contains(String.valueOf(InputUtils.TOTEN))) {
                splitChar = String.valueOf(InputUtils.TOTEN);
            } else {
                splitChar = ",";
            }

            final String[] answersGiven = this.answerGiven.split(splitChar);
            final String[] answersExpected = answerExpected.split(splitChar);

            for (String givenToken : answersGiven) {

                givenToken = StringUtils.trimToEmpty(givenToken);
                givenToken = StringUtils.stripToEmpty(givenToken);

                for (String expectedToken : answersExpected) {

                    // Deletion of leading and trailing spaces
                    expectedToken = StringUtils.trimToEmpty(expectedToken);
                    expectedToken = StringUtils.stripToEmpty(expectedToken);

                    double similarity = computeSimilarity(expectedToken, givenToken);
                    if (similarity == 1) {
                        success = true;
                        break;
                    } else if (similarity > 0.8) { // FIXME fixer des seuils
                        almost = true;
                        break;
                    }
                }

                if (success || almost) {
                    break;
                }
            }
        }

        return success;
    }

    // http://rosettacode.org/wiki/Levenshtein_distance#Java
    private static double computeSimilarity(String s1, String s2) {
        s1 = new String(s1.getBytes(StandardCharsets.UTF_8));
        s2 = new String(s2.getBytes(StandardCharsets.UTF_8));

        if (s1.length() < s2.length()) { // s1 should always be bigger
            String swap = s1;
            s1 = s2;
            s2 = swap;
        }
        int bigLen = s1.length();
        if (bigLen == 0) {
            return 1.0; /* both strings are zero length */
        }
        return (bigLen - computeEditDistance(s1, s2)) / (double) bigLen;
    }

    // http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
    private static int computeEditDistance(String s0, String s1) {

        int len0 = s0.length() + 1;
        int len1 = s1.length() + 1;

        // the array of distances
        int[] oldcost = new int[len0]; // j -2
        int[] cost = new int[len0]; // j-1
        int[] newcost = new int[len0]; // j

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) {
            cost[i] = i;
        }

        // dynamicaly computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {

            // initial cost of skipping prefix in String s1
            newcost[0] = j - 1;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {

                // matching current letters in both strings
                int match = s0.charAt(i - 1) == s1.charAt(j - 1) ? 0 : 1;

                // computing cost for each transformation
                int cost_delete = newcost[i - 1] + 1; // deletion
                int cost_insert = cost[i] + 1; // insertion
                int cost_replace = cost[i - 1] + match; // substitution

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);

                // http://fr.wikipedia.org/wiki/Distance_de_Damerau-Levenshtein
                if (i > 1 && j > 1 && s0.charAt(i - 1) == s1.charAt(j - 2) && s0.charAt(i - 1) == s1.charAt(j - 1)) {
                    int cost_transposition = oldcost[i - 2] + match; // transposition

                    newcost[i] = Math.min(newcost[i], cost_transposition);
                }
            }

            // swap cost/newcost arrays
            oldcost = cost;
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }
}