package it.qrntine.chatbluetooth.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import it.qrntine.chatbluetooth.R;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us_activity);

        Holder holder = new Holder();

    }

    class Holder {
        TextView tvAboutUs, tvAboutUsDescr;
        ImageView ivAboutUs;

        public Holder() {
            tvAboutUs = findViewById(R.id.tvAboutUs);
            tvAboutUsDescr = findViewById(R.id.tvNomeGruppo);
            ivAboutUs = findViewById(R.id.ivAboutUs);
        }
    }

}
