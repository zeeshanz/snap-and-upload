package com.jixte.snapcropupload;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;

import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 *
 * Created by Zeeshan A Zakaria on 2016-04-06.
 * www.zeeshanz.com
 *
 */
public class PhotoActivity extends AppCompatActivity implements PhotoActivityView {
    static int IMAGE_FROM_CAMERA = 0, IMAGE_FROM_GALLERY = 1;
    String mItem, mCurrentImagePath, mImageFolder, mUrl;
    int mCropWidth, mCropHeight, mImageType;
    boolean mOverwriteImage;
    Button mGoBackButton;
    IPhotoActivityPresenter mPresenter;
    boolean mMoveToInternalStorage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_upload);
        mGoBackButton = findViewById(R.id.b_go_back);
        mPresenter = new PhotoActivityPresenterImpl(this);
        verifyPermissions(this);
        setupVariables();
        selectImageSourceDialog();
        mPresenter.setServerUrl(mUrl);
        mGoBackButton.setOnClickListener(view -> mPresenter.goBackButtonPresses());
    }

    /**
     *
     */
    private void setupVariables() {
        Intent intent = getIntent();
        mImageFolder = intent.getStringExtra(Constants.IMAGE_FOLDER);
        mCropWidth = intent.getIntExtra(Constants.CROP_WIDTH, 0);
        mCropHeight = intent.getIntExtra(Constants.CROP_HEIGHT, 0);
        mImageType = intent.getIntExtra(Constants.IMAGE_TYPE, 0);
        mUrl = intent.getStringExtra(Constants.URL);
        mOverwriteImage = intent.getBooleanExtra(Constants.OVERWRITE_IMAGE, true);
        mMoveToInternalStorage = (mImageFolder == null || mImageFolder.isEmpty());
        mImageFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                        + ""
                        + ((mImageFolder == null || mImageFolder.isEmpty()) ? "" : "/" + mImageFolder)
                        + "/";
    }

    /**
     *
     */
    private void selectImageSourceDialog() {
        final CharSequence[] items = { Constants.TAKE_PHOTO, Constants.CHOOSE_FROM_LIBRARY, Constants.CANCEL };

        AlertDialog.Builder builder = new AlertDialog.Builder(PhotoActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, (dialog, item) -> {
            mItem = items[item].toString();

            switch (mItem) {
                case Constants.TAKE_PHOTO:
                    mPresenter.takePhotoOptionClicked();
                    break;

                case Constants.CHOOSE_FROM_LIBRARY:
                    mPresenter.openGalleryOptionClicked();
                    break;

                case Constants.CANCEL:
                    mPresenter.cancelOptionClicked(dialog);
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Log.i(Constants.TAG, "resultCode: " + resultCode);

            if (requestCode == IMAGE_FROM_GALLERY) {
                try {
                    String sourceFile = getImageLocation(data);
                    String destFile = Utils.createImageFile("temp", mImageFolder, mImageType, mOverwriteImage).getAbsolutePath();
                    boolean success = Utils.copyFile(sourceFile, destFile);
                    if (!success) {
                        return;
                    }
                    mCurrentImagePath = destFile;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mCropHeight > 0 && mCropWidth > 0) {
                if(mPresenter.processImage() != null) {
                    mPresenter.imageProcessingComplete(mCurrentImagePath);
                }
            }
        } else {
            Log.e(Constants.TAG, "resultCode: " + resultCode);
        }
    }

    /**
     * Capture and save the image
     *
     */
    @Override
    public String rescaleCropAndSave() {
        String finalImagePath = Utils.processImage(mCurrentImagePath, mCropWidth, mCropHeight, mImageFolder, mImageType, mOverwriteImage);
        if (finalImagePath == null) return null;
        else {
            mCurrentImagePath = finalImagePath;
            return finalImagePath;
        }
    }

    /**
     * Get image location when selected from the gallery
     *
     * @param data  the image data
     * @return      the image location
     */
    private String getImageLocation(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        return cursor.getString(columnIndex);
    }

    @Override
    public void returnImagePath(String imagePath) {

        if (mMoveToInternalStorage) {
            String newPath;
            if (mOverwriteImage)
                newPath = getFilesDir().getAbsolutePath() + "/image.jpg";
            else {
                String filename = imagePath.substring(imagePath.lastIndexOf("/"));
                newPath = getFilesDir().getAbsolutePath() + filename;
            }

            Utils.copyFile(imagePath, newPath);
            File file = new File(imagePath);
            if (file.delete()) {
                Log.v(Constants.TAG, "deleted: " + imagePath);
            }
            imagePath = newPath;
            mPresenter.uploadImageToServer(imagePath);
        }

        Intent intent = new Intent();
        intent.setAction("com.jixte.snapcropupload.MESSAGE_RECEIVED");
        intent.putExtra(Constants.IMAGE_PATH, imagePath);
        Log.d(Constants.TAG, "Broadcasting image path: " + imagePath);
        sendBroadcast(intent);
        finish();
    }

    /**
     *
     * @param activity  the activity
     */
    public void verifyPermissions(AppCompatActivity activity) {
        // Check for camera and storage permissions
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        if ((ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                )
            ActivityCompat.requestPermissions(activity, PERMISSIONS, 1);
    }

    @Override
    public void startCamera() {
        File initialFile = null;
        try {
            initialFile = Utils.createImageFile("temp", mImageFolder, mImageType, mOverwriteImage);
            mCurrentImagePath = initialFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (initialFile != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
//                Uri uri = FileProvider.getUriForFile(PhotoActivity.this, BuildConfig.APPLICATION_ID + ".provider", initialFile);
                Uri uri = Utils.getImageContentUri(this, initialFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, IMAGE_FROM_CAMERA);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), IMAGE_FROM_GALLERY);
    }

    @Override
    public void cancel(DialogInterface dialog) {
        dialog.dismiss();
        finish();
    }

    @Override
    public void finishActivity() {
        finish();
    }
}