package data;

import android.provider.BaseColumns;

/**
 * Created by Максим on 22.04.2017.
 */

public final class DictionaryContract {

    private DictionaryContract() {
    };

    public static final class DictionaryEntry implements BaseColumns {

        public final static String TABLE_NAME = "dictionary";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_WORD = "word";
        public final static String COLUMN_TRANSLATION = "translation";
        public final static String COLUMN_FAVORITES = "favorites";

    }
}
