package com.cateye.android.vtm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.vondear.rxtool.RxAnimationTool;


/**
 * Created by xiaoxiao on 2018/7/19.
 */

public class LoginActivity extends Activity {
    private EditText edt_name,edt_pwd;
    private TextView btn_login;
    private ImageView img_logo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

        //动画加载logo
        RxAnimationTool.popup(img_logo,1200);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }

    private void initView(){
        img_logo= (ImageView) findViewById(R.id.img_login_logo);
        edt_name= (EditText) findViewById(R.id.edt_login_userName);
        edt_pwd= (EditText) findViewById(R.id.edt_login_pwd);
        btn_login= (TextView) findViewById(R.id.btn_login);
    }
}
