package com.example.adm.testmultipartrequest;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adm on 29.03.16.
 */
public class DreamscopeRequest {

    private final String LOG_TAG = "RequestLog";
    private final String SERVER_ADDRESS = "http://dreamscopeapp.com/";


    DreamscopeRequestListener dreamscopeRequestListener;
    RequestQueue mQueue;


    public DreamscopeRequest (Context context, DreamscopeRequestListener listener){

        mQueue = Volley.newRequestQueue(context);
        this.dreamscopeRequestListener = listener;

    }

    public void get (String uuid){
        String url = SERVER_ADDRESS + uuid;
        StringRequest r = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dreamscopeRequestListener.onGet(Integer.parseInt(findKey(prepareForLogging(parseThisObjectToJSON(response)), "processing_status")));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dreamscopeRequestListener.onFail();
            }
        });
        mQueue.add(r);
    }

    public void postImage(final byte[] image, final String filter){
        String url = SERVER_ADDRESS + "api/images";
        Log.i(LOG_TAG, "POST request is called");


        final MultipartEntity entity = new MultipartEntity();

        StringRequest r = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dreamscopeRequestListener.onPostImage(findKey(prepareForLogging(parseThisObjectToJSON(response)), "uuid"));

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dreamscopeRequestListener.onFail();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("filter", filter);
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {

                return image;

            }
            @Override
            public String getBodyContentType()
            {
                return entity.getContentType().getValue(); //посмотри здесь сам, как я понял ты что-то находил как правильно это делать
            }
        };

        mQueue.add(r);

    }

    public void getImage(String uuid){
        String url = SERVER_ADDRESS + "/" + uuid;
        ImageRequest IR = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                dreamscopeRequestListener.onGetImage(bitmap);
            }
        }, 1000, 1000, Bitmap.Config.RGB_565, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dreamscopeRequestListener.onFail();

            }
        });

        mQueue.add(IR);
    }


























    @TargetApi(Build.VERSION_CODES.KITKAT)
    public ArrayList<ArrayList<Object>> prepareForLogging(JSONObject json){

        ArrayList<ArrayList<Object>> StringsAL = new ArrayList<ArrayList<Object>>();


        JSONArray jsonArray = json.names();


        for (int i = 0; i<jsonArray.length(); i++){
            ArrayList<Object> qwer = new ArrayList<Object>();

            try {
                JSONObject JSONobj = new JSONObject(json.getString(jsonArray.getString(i)));
                qwer.add(jsonArray.getString(i));
                qwer.add(prepareForLogging(JSONobj));
                Log.d(LOG_TAG, "After parsing JSONObject.");


            } catch (JSONException ex) {
                ex.printStackTrace();
                qwer.clear();
                try {
                    JSONArray JSONarray = new JSONArray(json.getJSONArray(jsonArray.getString(i)));
                    qwer.add(jsonArray.getString(i));
                    qwer.add(parseJSONArray(JSONarray));
                    Log.d(LOG_TAG, "after parsing JSONArray.");
                } catch (JSONException e) {

                    e.printStackTrace();
                    qwer.clear();
                    try {
                        qwer.add(jsonArray.getString(i));
                        qwer.add(json.getString(jsonArray.getString(i)));

                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        qwer.clear();
                        Log.d(LOG_TAG, "Parsing error.");
                    }
                }
            }

            StringsAL.add(qwer);


        }

        return StringsAL;
    }

    public ArrayList<Object> parseJSONArray(JSONArray jsonArray){
        Log.d(LOG_TAG, "Method parseJSONArray is called.");

        ArrayList<Object> AL = new ArrayList<Object>();


        for (int i = 0; i<jsonArray.length(); i++){
            try {
                AL.add(jsonArray.getString(i));
            }catch (JSONException e){
                try {
                    AL.add((ArrayList<Object>) parseJSONArray(((JSONArray) jsonArray.getJSONArray(i))));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return AL;
    }

    public String findKey(ArrayList<ArrayList<Object>> AL, String key){
        String s = "";

        for (int i = 0; i < AL.size(); i++){
            s = findKeyInArray((ArrayList) AL.get(i), key);

            if(!s.equals("")){
                return s;
            }

        }


        return s;
    }

    public JSONObject parseThisObjectToJSON(String s){

        JSONObject json;
        try {
            json = new JSONObject(s);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();

            return  null;
        }


    }

    public String findKeyInArray(ArrayList<Object> AL, String key){
        String s = "";
        for (int i = 0; i < AL.size(); i++){
            if (AL.get(i).getClass() == (new ArrayList<Object>()).getClass()){
                s = findKeyInArray((ArrayList) AL.get(i), key);
            }if (AL.get(i).getClass() == (new String()).getClass()) {
                if (((String) AL.get(i)).equals(key)) {
                    s = (String) AL.get(i+1);
                }
            }
            if (!s.equals("")){
                return s;
            }
        }

        return s;
    }
}

interface DreamscopeRequestListener{
    void onFail();
    void onGet(int processStatus);
    void onGetImage(Bitmap bitmap);
    void onPostImage(String uuid);
}