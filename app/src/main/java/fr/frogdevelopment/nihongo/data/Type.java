/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.data;

import android.net.Uri;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;

public enum Type {

    WORD(DicoContract.Type.WORD.code, R.drawable.background_word, NihonGoContentProvider.URI_WORD),
    EXPRESSION(DicoContract.Type.EXPRESSION.code, R.drawable.background_expression, NihonGoContentProvider.URI_EXPRESSION);

    public final String code;
    public final int background;
    public final Uri uri;

    Type(String code, int background, Uri uri) {
        this.code = code;
        this.background = background;
        this.uri = uri;
    }
}
