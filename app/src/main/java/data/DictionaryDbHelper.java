package data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Максим on 22.04.2017.
 */

public class DictionaryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = DictionaryDbHelper.class.getSimpleName();

    /**
     * Имя файла базы данных
     */
    private static final String DATABASE_NAME = "dictionary.db";

    /**
     * Версия базы данных. При изменении схемы увеличить на единицу
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Конструктор {@link DictionaryDbHelper}.
     *
     * @param context Контекст приложения
     */
    public DictionaryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Вызывается при создании базы данных
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Строка для создания таблицы
        String SQL_CREATE_DICTIONARY_TABLE = "CREATE TABLE " + DictionaryContract.DictionaryEntry.TABLE_NAME + " ("
                + DictionaryContract.DictionaryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DictionaryContract.DictionaryEntry.COLUMN_WORD + " TEXT NOT NULL DEFAULT '', "
                + DictionaryContract.DictionaryEntry.COLUMN_FAVORITES + " INTEGER NOT NULL DEFAULT 0, "
                + DictionaryContract.DictionaryEntry.COLUMN_TRANSLATION + " TEXT NOT NULL DEFAULT '');";

        // Запускаем создание таблицы
        db.execSQL(SQL_CREATE_DICTIONARY_TABLE);
    }

    /**
     * Вызывается при обновлении схемы базы данных
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

