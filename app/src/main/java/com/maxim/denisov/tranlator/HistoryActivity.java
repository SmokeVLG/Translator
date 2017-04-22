package com.maxim.denisov.tranlator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import data.DictionaryContract;
import data.DictionaryDbHelper;

public class HistoryActivity extends AppCompatActivity {
    private  DictionaryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbHelper = new DictionaryDbHelper(this);
        displayDatabaseInfo();

    }

    private void displayDatabaseInfo() {
        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Зададим условие для выборки - список столбцов
        String[] projection = {
                DictionaryContract.DictionaryEntry._ID,
                DictionaryContract.DictionaryEntry.COLUMN_WORD,
                DictionaryContract.DictionaryEntry.COLUMN_TRANSLATION };

        // Делаем запрос
        Cursor cursor = db.query(
                DictionaryContract.DictionaryEntry.TABLE_NAME,   // таблица
                projection,            // столбцы
                null,                  // столбцы для условия WHERE
                null,                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // порядок сортировки

        TextView displayTextView = (TextView) findViewById(R.id.text_view_info);

        try {
            displayTextView.setText("Словарь содержит " + cursor.getCount() + " слов.\n\n");
            // Узнаем индекс каждого столбца
            int idColumnIndex = cursor.getColumnIndex(DictionaryContract.DictionaryEntry._ID);
            int wordColumnIndex = cursor.getColumnIndex(DictionaryContract.DictionaryEntry.COLUMN_WORD);
            int translationColumnIndex = cursor.getColumnIndex(DictionaryContract.DictionaryEntry.COLUMN_TRANSLATION);


            while (cursor.moveToNext()) {
                // Используем индекс для получения строки или числа
                int currentID = cursor.getInt(idColumnIndex);
                String currentWord = cursor.getString(wordColumnIndex);
                String currentTranslation = cursor.getString(translationColumnIndex);

                // Выводим значения каждого столбца
                displayTextView.append(("\n" + currentID + " - " +
                        currentWord + " - " +
                        currentTranslation));
            }
        } finally {
            // Всегда закрываем курсор после чтения
            cursor.close();
        }
    }

}
