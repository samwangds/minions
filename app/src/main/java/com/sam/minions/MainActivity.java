package com.sam.minions;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {
    private MinionView mMinionView;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMinionView.isShown()) {
                    mMinionView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mMinionView.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }
            }
        });


        mMinionView = (MinionView) findViewById(R.id.minion);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(new MainAdapter());
    }

    private static class MainAdapter extends RecyclerView.Adapter<MinionsHolder> {

        @Override
        public MinionsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final MinionView minionView = new MinionView(parent.getContext());
            parent.addView(minionView);
            return new MinionsHolder(minionView);
        }

        @Override
        public void onBindViewHolder(MinionsHolder holder, int position) {
            holder.randomBodyColor();
        }

        @Override
        public int getItemCount() {
            return 100;
        }
    }

    private static class MinionsHolder extends RecyclerView.ViewHolder {
        private MinionView mMinionView;

        public MinionsHolder(View itemView) {
            super(itemView);
            mMinionView = (MinionView) itemView;
        }

        public void randomBodyColor() {
            mMinionView.randomBodyColor();
        }
    }

}
