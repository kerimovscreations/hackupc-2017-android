package hajalibayram.hnotes_android.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;

import hajalibayram.hnotes_android.R;

public class DocumentActivity extends AppCompatActivity {

    //web_view

    private Context mContext;
    private String mUrlStr;
    private WebView mWebView;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        mContext = this;

        initVars();
    }

    private void initVars() {
//        isLogged = Auth.isLogged(mContext);
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(getString(R.string.loading));

        mUrlStr = getIntent().getStringExtra("URL");


        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.loadData(mUrlStr, "text/html; charset=utf-8", "UTF-8");

        findViewById(R.id.doc_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(mUrlStr);
            }
        });
        findViewById(R.id.doc_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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


}
