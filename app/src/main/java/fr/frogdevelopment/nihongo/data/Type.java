/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.data;

import android.net.Uri;

import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;

public enum Type {

    WORD(DicoContract.Type.WORD.code, NihonGoContentProvider.URI_WORD),
    EXPRESSION(DicoContract.Type.EXPRESSION.code, NihonGoContentProvider.URI_EXPRESSION);

    public final String code;
    public final Uri    uri;

    Type(String code, Uri uri) {
        this.code = code;
        this.uri = uri;
    }
}
