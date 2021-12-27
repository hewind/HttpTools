package com.example.myhttputils;


import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.common.tools.http.HttpRequest;
import com.common.tools.http.ParamsBuild;
import com.common.tools.http.itl.OnHttpRequestListener;

public class MainActivity extends Activity {

    private Button btn_http;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        postTest();
    }

    private void initView() {
        btn_http = findViewById(R.id.btn_http);
        content = findViewById(R.id.content);
    }

    /**
     * post连接测试
     */
    private void postTest() {
        ParamsBuild params = ParamsBuild.build();
        params.put("group_id","1");
        params.put("game_id","1394628137031");
        params.put("role_id","1");
        params.put("role_name","ddd");
        params.put("role_level","1");
        params.put("party_name","we");
        params.put("balance","xc");
        params.put("vip_level","1");
        params.put("role_create_time",System.currentTimeMillis()+"");
        params.put("client_type","1");
        HttpRequest httpRequest = new HttpRequest(this);
        httpRequest.sendRequest(params, "http://121.4.214.235:9528/services/app.tlbb-mobile/report/requests", new OnHttpRequestListener() {
            @Override
            public void onStart() {

            }
            @Override
            public void onSuccess(String result) {

            }
            @Override
            public void onError(String msg) {

            }
        });
    }




}