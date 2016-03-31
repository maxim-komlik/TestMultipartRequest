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

//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;

/**
 * Created by adm on 29.03.16.
 */
public class DreamscopeRequest {

    private final String LOG_TAG = "RequestLog";
    private final String SERVER_ADDRESS = "http://dreamscopeapp.com/";


    DreamscopeRequestListener dreamscopeRequestListener;
    DreamscopeRequestListenerOnGetImage dreamscopeRequestListenerOnGetImage;
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
                try {
                    dreamscopeRequestListener.onGet((Integer) parseThisObjectToJSON(response).get("processing_status"), (String) parseThisObjectToJSON(response).get("filtered_url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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


        final MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        entityBuilder.addBinaryBody("image", image, ContentType.create("image/png"), "image.png"); //Take care of contentType and filename!
        entityBuilder.addTextBody("filter", filter);

        final HttpEntity httpEntity = entityBuilder.build();

        StringRequest r = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    dreamscopeRequestListener.onPostImage((String)parseThisObjectToJSON(response).get("uuid"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dreamscopeRequestListener.onFail();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", httpEntity.getContentType().getValue());
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    httpEntity.writeTo(byteArrayOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return byteArrayOutputStream.toByteArray();

            }
            @Override
            public String getBodyContentType()
            {
                return httpEntity.getContentType().getValue(); //посмотри здесь сам, как я понял ты что-то находил как правильно это делать
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
                dreamscopeRequestListenerOnGetImage.onGetImage();
            }
        }, 1000, 1000, Bitmap.Config.RGB_565, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dreamscopeRequestListener.onFail();

            }
        });

        mQueue.add(IR);
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

    public void setDreamscopeRequestListenerOnGetImage(DreamscopeRequestListenerOnGetImage listenerOnGetImage){
        this.dreamscopeRequestListenerOnGetImage = listenerOnGetImage;
    }
}

interface DreamscopeRequestListener{
    void onFail();
    void onGet(int processStatus, String urlFiltredImg);
    void onGetImage(Bitmap bitmap);
    void onPostImage(String uuid);
}

interface DreamscopeRequestListenerOnGetImage{
    void onGetImage();
}