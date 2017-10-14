package hajalibayram.hnotes_android.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hajalibayram.hnotes_android.R;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private int mUserChooseTask;
    private Uri mFileUri;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initVars();
    }

    private void initVars() {
        findViewById(R.id.take_snap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserChooseTask = 0;
                checkPermission();
            }
        });
        findViewById(R.id.choose_from_library).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserChooseTask = 1;
                checkPermission();
            }
        });
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
//                cropImage(data.getData());
                Log.e("Tag", data.getData().toString());
            }
        } else if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                if (mFileUri != null)
//                    cropImage(mFileUri);
                    Log.e("Tag", data.getData().toString());
                else
                    Toast.makeText(mContext, getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
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


    public void onHistory(View view) {
        startActivity(new Intent(mContext, HistoryActivity.class));
    }
}
