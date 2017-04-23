package com.maxim.denisov.tranlator;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    //Проверка на корректоность ответа сервера
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Test
    public void isCorrectTranslate() throws Exception {
        String template = "{\"code\":200,\"lang\":\"en-ru\",\"text\":[\"Привет\"]}";
        MainActivity ma  = new MainActivity();
        URL url = ma.createURLForTranslate("Привет");
        URL []params = new URL[1];
        params[0] =url;
        String answer =  ma.getJsonObject(params);
        assertTrue(template.equals(answer));
    }
}