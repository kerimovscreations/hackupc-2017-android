package hajalibayram.hnotes_android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import hajalibayram.hnotes_android.R;
import hajalibayram.hnotes_android.adapter.HistoryAdapter;
import hajalibayram.hnotes_android.model.HistoryItem;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class HistoryActivity extends AppCompatActivity {

    private Context mContext;

    private RecyclerView mRv;
    private HistoryItem mItem;
    private HistoryAdapter mAdapter;
    private ArrayList<HistoryItem> mList;
    private SwipeRefreshLayout mSwipeRLayout;
    private Realm mRealm;

    private AsyncHttpClient mClient;
    private JsonParser mParser;
    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mContext = this;

        Realm.init(mContext);
        try {
            mRealm = Realm.getDefaultInstance();
        } catch (Exception e) {
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            mRealm = Realm.getInstance(config);
        }

        mClient = new AsyncHttpClient(false, 80, 443);
        mParser = new JsonParser();
        mGson = new Gson();

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
                        .putExtra("URL", mList.get(position).getContent()));
            }

            @Override
            public void onDeleteClick(View itemView, int position) {

            }

            @Override
            public void onShareClick(View itemView, int position) {
                share(mList.get(position).getImg_url());
            }
        });


        findViewById(R.id.history_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//        RealmResults<ShoppingProductOption> objectList = mRealm.where(ShoppingProductOption.class).equalTo("branch_id", mList.get(position).getBranch_id()).findAll();

    }


    private void getData() {
        mList.clear();
//        mList.add(new HistoryItem("Get ducky", "31 nov 2017", "https://octodex.github.com/images/daftpunktocat-thomas.gif"));
//        mList.add(new HistoryItem("Han Solo sucks", "29 feb 2018", "https://octodex.github.com/images/stormtroopocat.png"));
//        mList.add(new HistoryItem("There could be your advertisement", "32 mar 2017", "https://octodex.github.com/images/femalecodertocat.png"));
        SharedPreferences mPrefs = mContext.getSharedPreferences("LocalPreference", Context.MODE_PRIVATE);
        Log.e("PREF", mPrefs.getString("user_id", "0"));
        Log.e("PREF", mPrefs.getString("api_token", "0"));
        String url = "http://www.hnotes.org/api/notes?user_id=" + mPrefs.getString("user_id", "0")
                + "&api_token=" + mPrefs.getString("api_token", "0");
        mClient.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    Log.d("RESPONSE", new String(responseBody));

                    JsonObject response = mParser.parse(new String(responseBody)).getAsJsonObject();

                    if (response.get("status").getAsInt() == 200) {
                        for (JsonElement item : response.get("data").getAsJsonObject().get("notes").getAsJsonArray()) {
                            String url = "http://www.hnotes.org/api/uploads/images/"+                                    item.getAsJsonObject().get("image_url").getAsString();

                            mList.add(new HistoryItem(item.getAsJsonObject().get("title").getAsString(),
                                    item.getAsJsonObject().get("created_at").getAsString(),
                                    url,
                                    item.getAsJsonObject().get("content").getAsString()));

                        }
//                        startActivity(new Intent(mContext, MainActivity.class));
                    } else {
//                                                JsonObject errorObj = response.get("error").getAsJsonObject();
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(mContext, R.string.error, Toast.LENGTH_LONG).show();

//                                    String[] byteValues = responseBody.toString().substring(1, responseBody.toString().length() - 1).split(",");


                Log.d("RESPONSE", String.valueOf(statusCode));

            }
        });
        mAdapter.notifyDataSetChanged();
        mSwipeRLayout.setRefreshing(false);
    }

    public void share(String text) {
        String mimeType = "text/plain";
        String title = "Select one";

        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setChooserTitle(title)
                .setType(mimeType)
                .setText(text)
                .getIntent();
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }
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
