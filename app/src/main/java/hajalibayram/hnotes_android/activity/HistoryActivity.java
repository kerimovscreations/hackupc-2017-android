package hajalibayram.hnotes_android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import hajalibayram.hnotes_android.R;
import hajalibayram.hnotes_android.adapter.HistoryAdapter;
import hajalibayram.hnotes_android.model.HistoryItem;

public class HistoryActivity extends AppCompatActivity {

    private Context mContext;

    private RecyclerView mRv;
    private HistoryItem mItem;
    private HistoryAdapter mAdapter;
    private ArrayList<HistoryItem> mList;
    private SwipeRefreshLayout mSwipeRLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mContext = this;

        initVars();
    }

    private void initVars() {
        mSwipeRLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
        mList = new ArrayList<>();
        mRv = (RecyclerView) findViewById(R.id.rv_history);
        mAdapter = new HistoryAdapter(mContext, mList);
        mRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRv.setAdapter(mAdapter);
        mRv.addItemDecoration(new ItemDecoration(24));

        mAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                startActivity(new Intent(mContext, DocumentActivity.class)
                        .putExtra("PARAM", mList.get(position).getImg_url()));
            }

            @Override
            public void onDeleteClick(View itemView, int position) {

            }

            @Override
            public void onShareClick(View itemView, int position) {

            }
        });

        findViewById(R.id.history_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getData() {
        mList.clear();
        mList.add(new HistoryItem("Get ducky", "31 nov 2017", "https://octodex.github.com/images/daftpunktocat-thomas.gif"));
        mList.add(new HistoryItem("Han Solo sucks", "29 feb 2018", "https://octodex.github.com/images/stormtroopocat.png"));
        mList.add(new HistoryItem("There could be your advertisement", "32 mar 2017", "https://octodex.github.com/images/femalecodertocat.png"));

        mAdapter.notifyDataSetChanged();
        mSwipeRLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    private class ItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpace;

        ItemDecoration(int space) {
            this.mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1)
                outRect.bottom = mSpace;
        }
    }

}
