package com.jianqingc.nectar.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;


import com.jianqingc.nectar.R;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "" ;
    private EditText entertenantName, enterusername, enterpassword;
    private CheckBox rememberUser;


    /**
     * Hide keyboard when clicking on the screen except for the input boxes
     * @param view
     */
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void onClickSignInButton(View view) {

        rememberUser = (CheckBox) findViewById(R.id.rememberUser);
        entertenantName = ((EditText) findViewById(R.id.tenantNameEditText));
        enterusername = ((EditText) findViewById(R.id.usernameEditText));
        enterpassword = ((EditText) findViewById(R.id.passwordEditText));

        if (rememberUser.isChecked())
        {
            String tenant = entertenantName.getText().toString();
            String user = enterusername.getText().toString();
            String pwd = enterpassword.getText().toString();
            rememberInfo(tenant, user, pwd);
        }
        else
        {
            SharedPreferences.Editor et = getSharedPreferences("data", 0).edit();
            et.clear();
            et.commit();
        }

        String tenantName = entertenantName.getText().toString();
        String username = enterusername.getText().toString();
        String password = enterpassword.getText().toString();


        Log.i("tenantName: ", tenantName);
        Log.i("username: ", username);
        Log.i("password: ", password);
        /**
         * Call LoginHttp Function defined in controller/HttpRequest
         */
        HttpRequest.getInstance(this).loginHttp(tenantName,username,password,this);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.i(TAG,"---------- Longin Page Opern in LoginActivity ----------");
        findViewById(R.id.mainLayout).setSoundEffectsEnabled(false);
        findViewById(R.id.passwordEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        enterRem();


    }
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences =  getApplicationContext().getSharedPreferences("nectar_android", 0);
        String tokenExpires = sharedPreferences.getString("expires","1970-01-01T00:00:00Z");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Log.i("Expires",tokenExpires);
        try {
            /**
             *Autto login function. When login token hasn't expired and the user wasn't signed out last time.
             */
            Date tokenExpireTime = sdf.parse(tokenExpires);
            Date currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
            if((!sharedPreferences.getBoolean("isSignedOut", true)) & (tokenExpireTime.after(currentTime))){
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }



    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"---------- Longin Page Stop in LoginActivity ----------");
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.i(TAG,"---------- Longin Page Resume in LoginActivity ----------");
//        this.finish();
//    }

    private void rememberInfo (String tenant, String user, String pwd){
        SharedPreferences.Editor editor = getSharedPreferences("data",0).edit();
        editor.putString("tenantname", tenant);
        editor.putString("username",user);
        editor.putString("password", pwd);
        editor.commit();
    }

    private void enterRem (){

        SharedPreferences sp = getSharedPreferences("data", 0);
        String test = sp.getString("username", "");


            if (test != null)
            {
                //Log.d("remember","value")
                entertenantName = ((EditText) findViewById(R.id.tenantNameEditText));
                enterusername = ((EditText) findViewById(R.id.usernameEditText));
                enterpassword = ((EditText) findViewById(R.id.passwordEditText));

                entertenantName.setText(sp.getString("tenantname", ""));
                enterusername.setText(sp.getString("username", ""));
                enterpassword.setText(sp.getString("password", ""));
            }
            }



}


