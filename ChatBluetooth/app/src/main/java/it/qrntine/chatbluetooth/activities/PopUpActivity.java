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

import java.util.List;

import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.emoticons.EmoticonsManager;

public class PopUpActivity extends Activity {

    List<Integer> listaEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;

        //int heightEmojiBar = getIntent().getIntExtra("heightEmojiBar", height);

        getWindow().setLayout(width, (int)(height*.2));

        WindowManager.LayoutParams params = getWindow().getAttributes();

        params.gravity = Gravity.BOTTOM;
        params.x = 0;
        params.y = 0;
        //params.alpha = 0.3f;

        getWindow().setAttributes(params);

        Holder holder = new Holder();
    }

    class Holder {
        RecyclerView recyclerView;

        Holder() {
            recyclerView = findViewById(R.id.rvEmoji2);
            listaEmoji = EmoticonsManager.getListEmojiRes();

            RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(PopUpActivity.this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            RecyclerView.Adapter emojiRvAdapter = new EmojiRvAdapter(listaEmoji);
            recyclerView.setAdapter(emojiRvAdapter);
        }
    }

    public class EmojiRvAdapter extends RecyclerView.Adapter<EmojiRvAdapter.ViewHolder> implements View.OnClickListener {
        private List<Integer> listaDrawablesInt;

        EmojiRvAdapter(List<Integer> mIngr){
            listaDrawablesInt = mIngr;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View nameView = inflater.inflate(R.layout.layout_pop, parent, false);

            nameView.setOnClickListener(this);

            return new ViewHolder(nameView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ImageView iv = holder.ivEmoji;
            iv.setImageDrawable(getDrawable(listaDrawablesInt.get(position)));
        }

        @Override
        public int getItemCount() {
            return listaDrawablesInt.size();
        }

        @Override
        public void onClick(View v) {
            int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);
            String emoji = EmoticonsManager.selectKeywordByEmoji(listaEmoji.get(position));
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
