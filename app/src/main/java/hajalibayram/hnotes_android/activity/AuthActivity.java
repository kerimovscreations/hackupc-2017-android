package hajalibayram.hnotes_android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;
import hajalibayram.hnotes_android.R;

public class AuthActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    //    private GoogleApiClient mGoogleApiClient;
    private boolean mIsFacebook;
    private Context mContext;
//    private static final int GOOGLE_SIGN_IN_CODE = 464;

    private AsyncHttpClient mClient;
    private JsonParser mParser;
    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_auth);
        mContext = this;

        mClient = new AsyncHttpClient(false, 80, 443);
        mParser = new JsonParser();
        mGson = new Gson();


        findViewById(R.id.auth_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(mContext, MainActivity.class));
            }
        });

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

            }
        };

        findViewById(R.id.facebook_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsFacebook = true;
                onFacebookLogin();
            }
        });

        /**
         * Google Login
         */
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//                        Toast.makeText(mContext, getString(R.string.error), Toast.LENGTH_SHORT).show();
//                    }
//                } /* OnConnectionFailedListener */)
//                .addApi(com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();

//        findViewById(R.id.google_login_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mIsFacebook = false;
//                onGoogleLogin();
//            }
//        });
    }

//    private void onGoogleLogin() {
//        Intent signInIntent = com.google.android.gms.auth.api.Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE);
//    }

//    private void handleGoogleSignInResult(GoogleSignInResult result) {
//        if (result.isSuccess()) {
//            // Signed in successfully, show authenticated UI.
//            GoogleSignInAccount acct = result.getSignInAccount();
//
//            if (acct != null) {
//                String personId = acct.getId();
//                String personGivenName = acct.getGivenName();
//                String personEmail = acct.getEmail();
//
//
//            }
//
//        } else {
//            // Signed out, show unauthenticated UI.
//        }
//    }

    private void onFacebookLogin() {
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        // Callback registration
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                final AccessToken accessToken = loginResult.getAccessToken();
//                AccessToken.getCurrentAccessToken().getToken();


                GraphRequest request = GraphRequest.newMeRequest(accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                JsonParser jsonParser = new JsonParser();
                                JsonObject gsonObject = (JsonObject) jsonParser.parse(object.toString());
//                                Log.d("TAGG",gsonObject.getAsString());


                                mContext.getSharedPreferences("LocalPreference", MODE_PRIVATE).edit().putString("name", (object.toString().contains("name") && gsonObject.get("name").getAsString() != null)
                                        ? gsonObject.get("name").getAsString()
                                        : "").apply();
                                mContext.getSharedPreferences("LocalPreference", MODE_PRIVATE).edit().putBoolean("is_logged", true).apply();

                                String url = "http://www.hnotes.org/api/login/facebook";
                                RequestParams params = new RequestParams();
                                params.put("access_token", AccessToken.getCurrentAccessToken().getToken());

                                mClient.post(url, params, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        try {
                                            JsonObject response = mParser.parse(new String(responseBody)).getAsJsonObject();
                                            Log.d("RESPONSE", new String(responseBody));

                                            if (response.get("status").getAsInt() == 200) {


                                                String user_id = (response.toString().contains("id")
                                                        && response.get("data").getAsJsonObject().get("user").getAsJsonObject().get("id").getAsString() != null)
                                                        ? response.get("data").getAsJsonObject().get("user").getAsJsonObject().get("id").getAsString()
                                                        : "";

                                                String api_token = (response.toString().contains("api_token") && response.get("data").getAsJsonObject().get("user").getAsJsonObject().get("api_token").getAsString() != null)
                                                        ? response.get("data").getAsJsonObject().get("user").getAsJsonObject().get("api_token").getAsString()
                                                        : "";

                                                SharedPreferences prefs = mContext.getSharedPreferences("LocalPreference", MODE_PRIVATE);
                                                prefs.edit().putString("user_id", user_id).apply();
                                                prefs.edit().putString("api_token", api_token).apply();
                                                Log.d("ASAP", prefs.getAll().toString());


                                                startActivity(new Intent(mContext, MainActivity.class));

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
                                    }
                                });
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(mContext, "Login cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(mContext, getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mIsFacebook) //{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
//        } else if (requestCode == GOOGLE_SIGN_IN_CODE) {
//            GoogleSignInResult result = com.google.android.gms.auth.api.Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            handleGoogleSignInResult(result);
//        }
    }

}
