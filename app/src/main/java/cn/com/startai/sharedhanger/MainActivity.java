package cn.com.startai.sharedhanger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * author Guoqiang_Sun
 * date 2019/5/24
 * desc
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void scanBle(View view) {
        startActivity(new Intent(this, BleScanActivity.class));
    }
}
