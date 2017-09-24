package com.rnhotfix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;


public class MainActivity extends Activity implements View.OnClickListener{



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        findViewById(R.id.btn_download_patch).setOnClickListener(this);
        findViewById(R.id.btn_go_to_rn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_download_patch:
                downloadPatch();
                break;
            case R.id.btn_go_to_rn:
                Intent intent = new Intent(this, RNActivity.class);
                startActivity(intent);
                break;
        }
    }



    private void downloadPatch(){
        UpdateManager.getInstance(this).update();
    }
}
