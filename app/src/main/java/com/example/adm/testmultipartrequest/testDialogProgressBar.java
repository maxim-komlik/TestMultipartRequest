package com.example.adm.testmultipartrequest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class testDialogProgressBar extends AppCompatActivity {

    DreamscopeRequestListenerOnGetImage listener = new DreamscopeRequestListenerOnGetImage() {
        @Override
        public void onGetImage() {
            finishIt();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_dialog_progress_bar);
        MainActivity.drRequest.setDreamscopeRequestListenerOnGetImage(listener);
    }

    public void finishIt(){
        this.finish();
    }
}
