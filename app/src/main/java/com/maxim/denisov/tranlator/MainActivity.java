package com.maxim.denisov.tranlator;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import data.DictionaryContract;
import data.DictionaryDbHelper;

public class MainActivity extends AppCompatActivity {

    private ArrayList<TranslatedWord> translatedWordList = new ArrayList<>();
    private TranslatedWordArrayAdapter TranslatedWordsArrayAdapter;
    private ListView translatedWordsListView;
    private EditText wordForTranslateEditText;
    private  DictionaryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //View на отображение переведенного предложения
        translatedWordsListView = (ListView) findViewById(R.id.translatedWordsListView);
        wordForTranslateEditText = (EditText) findViewById(R.id.wordForTranslateEditText);



        //Получение данных о переводе
        TranslatedWordsArrayAdapter = new TranslatedWordArrayAdapter(this, translatedWordList);
        translatedWordsListView.setAdapter(TranslatedWordsArrayAdapter);


        //Кнопка
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText locationEditText = (EditText) findViewById(R.id.wordForTranslateEditText);
                String wordForTranslate = locationEditText.getText().toString();
                URL url = createURL(wordForTranslate);

                if (url != null) {
                    //Скрытие клавиатуры
                    dismissKeyboard(locationEditText);
                    //Получение погоды в отдельном потоке
                    GetTranslateTask getLocalTranslateTask = new GetTranslateTask();
                    getLocalTranslateTask.execute(url);
                } else {
                    //Если ошибка  - выводим сообщение
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                }
            }
        });
        mDbHelper = new DictionaryDbHelper(this);
    }

    /*
    * Получение URL для REST запроса */
    private URL createURL(String word) {

        String URL ="https://translate.yandex.net/api/v1.5/";
        //формат вывода
        String format = "tr.json/";
        //Ключ для работы с Yandex.API
        String apiKey = "trnsl.1.1.20150326T063842Z.5b7ebe22648f52c9.88c258a74fbc813d3f11c2a997fb65b3c48a364f";
        //направление перевода
        String area = "&lang=ru-en&";

        try {
            String urlString = URL + format + "translate?key=" + apiKey + area + "text=" + URLEncoder.encode(word, "UTF-8");
            return new URL(urlString);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void onFavoritesClick(View view) {

        Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
        startActivity(intent);

    }

    public void onAddFavoritesClick(View view) {

        String translatedWord = ((TextView) findViewById(R.id.translatedWordTextView)).getText().toString();
        String word = wordForTranslateEditText.getText().toString();
        if (translatedWord !=null)
        {
            insertWord(word,translatedWord,1);
        }

    }


    public void onHistoryClick(View view) {

        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        startActivity(intent);
    }

    public void onAddHistoryClick(View view) {

        String translatedWord = ((TextView) findViewById(R.id.translatedWordTextView)).getText().toString();
        String word = wordForTranslateEditText.getText().toString();
        if (translatedWord !=null)
        {
            insertWord(word,translatedWord,0);
        }
    }


    public  void insertWord(String word,String translation, int favorites) {
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Создаем объект ContentValues, где имена столбцов ключи,
        // а информация о госте является значениями ключей
        ContentValues values = new ContentValues();
        values.put(DictionaryContract.DictionaryEntry.COLUMN_WORD, word);
        values.put(DictionaryContract.DictionaryEntry.COLUMN_TRANSLATION, translation);
        values.put(DictionaryContract.DictionaryEntry.COLUMN_FAVORITES, favorites);
        db.insert(DictionaryContract.DictionaryEntry.TABLE_NAME, null, values);
    }

    public void onHistory(View view) {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        startActivity(intent);
    }

    private class GetTranslateTask extends AsyncTask<URL, Void, JSONObject> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } catch (IOException e) {
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    return new JSONObject(builder.toString());
                } else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            convertJSONtoArrayList(jsonObject);
            TranslatedWordsArrayAdapter.notifyDataSetChanged();
            translatedWordsListView.smoothScrollToPosition(0);
        }
    }

    /*Парсинг ответа от Яндекса о переведнном слове*/
    private void convertJSONtoArrayList(JSONObject translatedWords) {
        //Очищаем предудыщие переведенные слова
        translatedWordList.clear();
        try {
            // Получение свойства text из json
            JSONArray list = translatedWords.getJSONArray("text");

            for (int i = 0; i < list.length(); i++) {
                translatedWordList.add(new TranslatedWord(list.getString(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
