package cn.com.startai.sharedhanger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

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
        Toast.makeText(getApplicationContext(), " hello world ", Toast.LENGTH_SHORT).show();
    }

    public void bleTest(View view) {
        startActivity(new Intent(this, BleScanActivity.class));
    }
}
