package com.maxim.denisov.tranlator;

import android.annotation.TargetApi;
import android.content.Context;
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

public class MainActivity extends AppCompatActivity {

    private ArrayList<TranslatedWord> translatedWordList = new ArrayList<>();
    private TranslatedWordArrayAdapter TranslatedWordsArrayAdapter;
    private ListView translatedWordsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //View на отображение переведенного предложения
        translatedWordsListView = (ListView) findViewById(R.id.translatedWordsListView);
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
