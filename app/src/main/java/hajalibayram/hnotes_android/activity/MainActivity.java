package hajalibayram.hnotes_android.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import hajalibayram.hnotes_android.R;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private Toolbar toolbar;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private int mUserChooseTask;
    private Uri mFileUri;
    private Context mContext;
    private boolean isLogged;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    private TextView mAuth;
    private String title;

    private AsyncHttpClient mClient;
    private JsonParser mParser;
    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initVars();
    }

    private void initVars() {
        mPrefs = mContext.getSharedPreferences("LocalPreference", Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
        isLogged = mPrefs.getBoolean("is_logged", false);

        mClient = new AsyncHttpClient(false, 80, 443);
        mParser = new JsonParser();
        mGson = new Gson();

        mAuth = (TextView) findViewById(R.id.auth_btn);


        findViewById(R.id.auth_btn_lay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLogged) {
                    startActivity(new Intent(mContext, AuthActivity.class));
                    finish();
                } else {
                    final BottomSheetDialog bsd = new BottomSheetDialog(mContext);
                    View sheetView = getLayoutInflater().inflate(R.layout.view_bottom_sheet_log_out, null);

                    sheetView.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mEditor.putBoolean("is_logged", false).apply();

                            finish();
                            startActivity(getIntent());
                        }
                    });
                    sheetView.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bsd.cancel();
                        }
                    });

                    bsd.setContentView(sheetView);
                    bsd.show();
                }
            }
        });


        findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserChooseTask = 0;
                checkPermission();
            }
        });
        findViewById(R.id.browse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserChooseTask = 1;
                checkPermission();
            }
        });
        findViewById(R.id.history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, HistoryActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.setText(isLogged ? mPrefs.getString("name", "User") : getString(R.string.login));
        ((ImageView) findViewById(R.id.auth_btn_icon)).setImageResource(isLogged ? R.drawable.ic_logout : R.drawable.ic_login);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(mContext, R.string.permission_needed, Toast.LENGTH_LONG).show();
            }

            requestPermissions(new String[]{Manifest.permission.CAMERA
                    , Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            Log.e("Tag", "2.0.0");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            Log.e("Tag", "Granted");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_FILE) {
            if (resultCode == RESULT_OK) {
                final BottomSheetDialog bsd = new BottomSheetDialog(mContext);
                bsd.setCancelable(false);

                final View sheetView = getLayoutInflater().inflate(R.layout.view_bottom_sheet_naming, null);
                title = null;
                sheetView.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        title = ((EditText) sheetView.findViewById(R.id.title_et)).getText().length() <= 0
                                ? "ADFasdf"
                                : ((EditText) sheetView.findViewById(R.id.title_et)).getText().toString();

                        uploadImage(bsd);

                    }
                });
                bsd.setContentView(sheetView);
                bsd.show();
            }
        } else if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                if (mFileUri != null) {
                    final BottomSheetDialog bsd = new BottomSheetDialog(mContext);
                    bsd.setCancelable(false);

                    Log.d("TAG", mFileUri.toString());
                    final View sheetView = getLayoutInflater().inflate(R.layout.view_bottom_sheet_naming, null);
                    title = null;
                    sheetView.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            title = ((EditText) sheetView.findViewById(R.id.title_et)).getText().length() <= 0
                                    ? "ADFasdf"
                                    : ((EditText) sheetView.findViewById(R.id.title_et)).getText().toString();

                            uploadImage(bsd);

                        }
                    });
                    bsd.setContentView(sheetView);
                    bsd.show();

                } else
                    Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFileUri = savedInstanceState.getParcelable("file_uri");
    }

    private void uploadImage(final BottomSheetDialog bsd) {
        if (mFileUri != null) {

            File uploadPhoto = new File(mFileUri.getPath());
            RequestParams params = new RequestParams();

            File[] uploadPhotos = {uploadPhoto};


            try {
                String url = "http://www.hnotes.org/api/notes";
                Log.d("ASDA", mFileUri.toString());

                params.put("image", new File(mFileUri.getPath()));

                params.setHttpEntityIsRepeatable(true);
                params.setForceMultipartEntityContentType(true);
                params.put("title", title);
                Log.d("ASDA", params.toString());
//                            params.put("user_id", isLogged ? mPrefs.getString("user_id", "") : "");
//                            params.put("image", isLogged ? mPrefs.getString("api_token", "") : "");
//
                mClient.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        JsonParser parser = new JsonParser();
                        JsonObject response = parser.parse(new String(responseBody)).getAsJsonObject();

                        if (response.get("status").getAsInt() == 200) {
                            JsonArray responseData = response.get("data").getAsJsonArray();

                            startActivity(new Intent(mContext, MainActivity.class));
                            bsd.cancel();

                        } else {
                            JsonObject errorObj = response.get("error").getAsJsonObject();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(mContext, getString(R.string.error), Toast.LENGTH_LONG).show();

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                System.out.println("uri" + grantResults.length + " " + mUserChooseTask);
                if (grantResults.length == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    if (mUserChooseTask == REQUEST_CAMERA)
                        takePictureIntent();
                    else if (mUserChooseTask == SELECT_FILE)
                        galleryIntent();
                } else {
                    Toast.makeText(mContext, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void takePictureIntent() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(mContext, R.string.error, Toast.LENGTH_SHORT).show();
                }
                if (photoFile != null) {
                    mFileUri = FileProvider.getUriForFile(mContext,
                            "hajalibayram.hnotes_android.provider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
                    startActivityForResult(takePictureIntent, REQUEST_CAMERA);
                }
            }
        } catch (Exception ignored) {
            Toast.makeText(mContext, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select_file"), SELECT_FILE);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.e("Tag", "TAKE PIC4.0");

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", mFileUri);
    }

}
