package hajalibayram.hnotes_android.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

        mUrlStr = getIntent().getStringExtra("PARAMS");


        mWebView = (WebView) findViewById(R.id.web_view);

        getData();

    }

    private void getData() {
        mWebView.loadUrl(mUrlStr);

        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
            }
        });
    }

}
