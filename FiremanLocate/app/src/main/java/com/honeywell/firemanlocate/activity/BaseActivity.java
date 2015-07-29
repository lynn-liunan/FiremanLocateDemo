package com.honeywell.firemanlocate.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.honeywell.firemanlocate.R;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_base);
    }




    protected void showToast(String hint) {
        Toast.makeText(getApplicationContext(), hint, Toast.LENGTH_LONG).show();
    }
}
