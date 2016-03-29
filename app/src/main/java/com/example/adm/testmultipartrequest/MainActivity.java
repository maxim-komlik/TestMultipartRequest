package com.example.adm.testmultipartrequest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "MainLog";

    private String imagesUuid;

    DreamscopeRequest drRequest;
    DreamscopeRequestListener listener  = new DreamscopeRequestListener() {
        @Override
        public void onFail() {
            Log.d(LOG_TAG, "Action failed.");
        }

        @Override
        public void onGet(int processStatus) {
            if (processStatus == 2){
                Log.d(LOG_TAG, "Image is Ready to be uploaded.");
                drRequest.getImage(imagesUuid);
            }else {

                Log.d(LOG_TAG, "Image is not ready to be uploaded.");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drRequest.get(imagesUuid);
            }
        }

        @Override
        public void onGetImage(final Bitmap bitmap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mImage.setImageBitmap(bitmap);
                }
            });
        }

        @Override
        public void onPostImage(String uuid) {
            imagesUuid = uuid;
            drRequest.get(imagesUuid);
        }
    };

    ImageView mImage;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImage = (ImageView) findViewById(R.id.image);
        mButton = (Button) findViewById(R.id.button);

        drRequest = new DreamscopeRequest(this, listener);

    }


    public void changeImage(View v){
        Log.d(LOG_TAG, "Changing image...");

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    public void send(View v){
        Log.d(LOG_TAG, "Sending image...");

        Bitmap bitmap = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        drRequest.postImage(byteArray, "confetti");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;

        if (resultCode == RESULT_OK) {

            Uri selectedImage = data.getData();
            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                mImage.setImageBitmap(bitmap);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

}
