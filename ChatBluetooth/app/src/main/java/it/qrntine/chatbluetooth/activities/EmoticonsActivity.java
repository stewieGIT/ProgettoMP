package it.qrntine.chatbluetooth.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hardik.clickshrinkeffect.ClickShrinkEffectKt;

import java.io.IOException;
import java.util.List;

import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.emoticons.EmoticonsManager;

public class EmoticonsActivity extends Activity {

    List<String> listaEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        // dimensionamento activity
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        getWindow().setLayout(width, (int)(height*.3));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.x = 0;
        params.y = 0;

        getWindow().setAttributes(params);

        Holder holder = new Holder();
    }

    class Holder {
        RecyclerView rvEmoji;

        Holder() {
            rvEmoji = findViewById(R.id.rvEmoji2);
            listaEmoji = EmoticonsManager.listKeywords();
            RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(EmoticonsActivity.this, LinearLayoutManager.HORIZONTAL, false);
            rvEmoji.setLayoutManager(layoutManager);
            RecyclerView.Adapter emojiRvAdapter = new EmojiRvAdapter(listaEmoji);
            rvEmoji.setAdapter(emojiRvAdapter);
        }
    }

    public class EmojiRvAdapter extends RecyclerView.Adapter<EmojiRvAdapter.ViewHolder> implements View.OnClickListener {
        private List<String> listaDrawablesInt;

        EmojiRvAdapter(List<String> mIngr){
            listaDrawablesInt = mIngr;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View nameView = inflater.inflate(R.layout.layout_pop, parent, false);

            nameView.setOnClickListener(this);
            ClickShrinkEffectKt.applyClickShrink(nameView);
            return new ViewHolder(nameView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ImageView iv = holder.ivEmoji;
            try {
                iv.setImageDrawable(EmoticonsManager.selectEmojiByKeyword(listaDrawablesInt.get(position), EmoticonsActivity.this));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return listaDrawablesInt.size();
        }

        @Override
        public void onClick(View v) {
            int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);
            String emoji = listaEmoji.get(position);
            //Toast.makeText(PopUpActivity.this, "Select: " + position + " " + emoji, Toast.LENGTH_LONG).show();

            Intent intent = new Intent();
            intent.putExtra("kwEmoji", emoji);
            setResult(RESULT_OK, intent);
            finish();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            private ImageView ivEmoji;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivEmoji =itemView.findViewById(R.id.ivEmojiPop);
            }

        }   //end Holder

    } // END emojiRvAdapter

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setResult(RESULT_CANCELED);
        finish();
    }
}
