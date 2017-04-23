package com.maxim.denisov.tranlator;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import data.DictionaryContract;
import data.DictionaryDbHelper;

public class MainActivity extends AppCompatActivity {

    private final String apiKey = "trnsl.1.1.20150326T063842Z.5b7ebe22648f52c9.88c258a74fbc813d3f11c2a997fb65b3c48a364f";
    private final String urlApi = "https://translate.yandex.net/api/v1.5/";
    Map<String, String> allLanguagesMap = new HashMap<>();
    ArrayList<String> allLanguages = new ArrayList<>();
    ArrayList<TranslatedWord> translatedWordList = new ArrayList<>();
    TranslatedWordArrayAdapter TranslatedWordsArrayAdapter;
    ListView translatedWordsListView;
    EditText wordForTranslateEditText;
    DictionaryDbHelper dDbHelper;
    Spinner leftLanguage;
    Spinner rightLanguage;
    String leftLanguageShort = "en";
    String rightLanguageShort = "ru";

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
                String translatedWord = ((TextView) findViewById(R.id.translatedWordTextView)).getText().toString();
                String word = wordForTranslateEditText.getText().toString();
                if (translatedWord !=null)
                {
                    saveTranslatedWord(word,translatedWord,1);
                }
            }
        });

        EditText locationEditText = (EditText) findViewById(R.id.wordForTranslateEditText);
        locationEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0)
                {
                    EditText locationEditText = (EditText) findViewById(R.id.wordForTranslateEditText);
                    String wordForTranslate = locationEditText.getText().toString();
                    URL url = createURLForTranslate(wordForTranslate);

                    if (url != null) {
                        //Скрытие клавиатуры
                        //dismissKeyboard(locationEditText);
                        //Получение перевода в отдельном потоке
                        GetTranslateTask getLocalTranslateTask = new GetTranslateTask();
                        getLocalTranslateTask.execute(url);
                    } else {
                        //Если ошибка  - выводим сообщение
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });



        dDbHelper = new DictionaryDbHelper(this);

        leftLanguage = (Spinner) findViewById(R.id.spinner_left_language);
        rightLanguage = (Spinner) findViewById(R.id.spinner_right_language);
        setupSpinners();
    }

    private void setupSpinners() {
        URL url = createURLForGetLanguages();
        //Получение перевода в отдельном потоке
        GetLanguages getLanguagesTask = new GetLanguages();
        getLanguagesTask.execute(url);
    }

    //Настройка спиннеров для отображения языков
    private void fillSpinners() {
        ArrayAdapter genderSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, allLanguages);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        leftLanguage.setAdapter(genderSpinnerAdapter);
        leftLanguage.setSelection(2);
        rightLanguage.setAdapter(genderSpinnerAdapter);
        rightLanguage.setSelection(3);

        leftLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                leftLanguageShort = allLanguagesMap.get(selection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                leftLanguageShort = "en";
            }
        });

        rightLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                rightLanguageShort = allLanguagesMap.get(selection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                rightLanguageShort = "ru";
            }
        });
    }

    /* Получение URL для перевода */
    public URL createURLForTranslate(String word) {
        //формат вывода
        String format = "tr.json/";
        //направление перевода
        String area = "&lang="+ leftLanguageShort +"-"+ rightLanguageShort +"&";
        try {
            String urlString = urlApi + format + "translate?key=" + apiKey + area + "text=" + URLEncoder.encode(word, "UTF-8");
            return new URL(urlString);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*Получение URL для списка языков */
    private URL createURLForGetLanguages() {

        String format = "tr.json/getLangs?ui=ru&key=";
        try {
            String urlString = urlApi + format + apiKey;
            return new URL(urlString);
        } catch ( MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Перейти в избранное
    public void onFavoritesClick(View view) {
        Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
        startActivity(intent);
    }

    //Перейти в историю
    public void onHistoryClick(View view) {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        startActivity(intent);
    }

    //Изменить язык
    public void onChangeLanClick(View view) {
        int leftLan = (int) leftLanguage.getSelectedItemId();
        leftLanguage.setSelection((int) rightLanguage.getSelectedItemId());
        rightLanguage.setSelection(leftLan);
    }

    //Сохранение переведенного слова
    public  void saveTranslatedWord(String word, String translation, int favorites) {
        // Gets the database in write mode
        SQLiteDatabase db = dDbHelper.getWritableDatabase();
        // Создаем объект ContentValues, где имена столбцов ключи,
        // а информация о госте является значениями ключей
        ContentValues values = new ContentValues();
        values.put(DictionaryContract.DictionaryEntry.COLUMN_WORD, word);
        values.put(DictionaryContract.DictionaryEntry.COLUMN_TRANSLATION, translation);
        values.put(DictionaryContract.DictionaryEntry.COLUMN_FAVORITES, favorites);
        db.insert(DictionaryContract.DictionaryEntry.TABLE_NAME, null, values);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    public String getJsonObject(URL[] params) {
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

                return builder.toString();
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

    //Получить перевод в отдельном потоке
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private class GetTranslateTask extends AsyncTask<URL, Void, JSONObject> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected JSONObject doInBackground(URL... params) {
            String temp =  getJsonObject(params);
            try {
                return (new JSONObject(temp));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            parseAnswerAboutTranslateWord(jsonObject);
            TranslatedWordsArrayAdapter.notifyDataSetChanged();
            translatedWordsListView.smoothScrollToPosition(0);
        }
    }

    //Получить список языков
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private class GetLanguages extends AsyncTask<URL, Void, JSONObject> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected JSONObject doInBackground(URL... params) {
            String temp =  getJsonObject(params);
            try {
                return (new JSONObject(temp));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            parseAnswerAboutAvailableLanguages(jsonObject);

        }
    }

    /*Парсинг ответа о переведнном слове*/
    private String parseAnswerAboutTranslateWord(JSONObject translatedWords) {
        //Очищаем предудыщие переведенные слова
        translatedWordList.clear();
        try {
            // Получение свойства text из json
            JSONArray list = translatedWords.getJSONArray("text");


            String translatedWord = list.getString(0);
            translatedWordList.add(new TranslatedWord(translatedWord));
            EditText locationEditText = (EditText) findViewById(R.id.wordForTranslateEditText);
            String wordForTranslate = locationEditText.getText().toString();
            saveTranslatedWord(wordForTranslate, translatedWord,0);
            return translatedWord;

        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    /*Парсинг ответа о доступных языках*/
    private void parseAnswerAboutAvailableLanguages(JSONObject translatedWords) {


        try {
            // Получение свойства text из json
            JSONObject jsonObject = translatedWords.getJSONObject("langs");
            Iterator<?> iterator = jsonObject.keys();
            allLanguagesMap.clear();
            while (iterator.hasNext()) {
                String shortLanguageName = iterator.next().toString();
                String languageName = jsonObject.get(shortLanguageName).toString();
                allLanguagesMap.put(languageName,shortLanguageName);
                allLanguages.add(languageName);
            }
            Collections.sort(allLanguages);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        fillSpinners();
    }


}
