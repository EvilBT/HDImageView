package xyz.zpayh.original;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import xyz.zpayh.adapter.BaseAdapter;
import xyz.zpayh.adapter.BaseViewHolder;
import xyz.zpayh.adapter.OnItemClickListener;

public class MainActivity extends AppCompatActivity {

    public static final String url = "http://7xi8d6.com1.z0.glb.clouddn.com/2017-03-24-17438359_1470934682925012_1066984844010979328_n.jpg";

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final BaseAdapter<String> adapter = new BaseAdapter<String>() {
            @Override
            public int getLayoutRes(int index) {
                return R.layout.item_activity;
            }

            @Override
            public void convert(BaseViewHolder holder, String data, int index) {
                holder.setText(R.id.tv_name,data);
            }

            @Override
            public void bind(BaseViewHolder holder, int layoutRes) {

            }
        };
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull View view, int adapterPosition) {
                String action = adapter.getData(adapterPosition);
                startActivity(action);
            }
        });

        String activityName[] = getResources().getStringArray(R.array.activity_list);
        List<String> data = new ArrayList<>(activityName.length);
        for (String name : activityName) {
            data.add(name);
        }
        adapter.setData(data);
    }

    private void startActivity(String action) {
        Intent intent = new Intent(action);
        startActivity(intent);
    }
}
