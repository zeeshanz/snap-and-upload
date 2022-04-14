package com.jixte.snapcropuploadsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.jixte.snapcropupload.SnapCropUpload;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

/**
 *
 * Created by Zeeshan A Zakaria on 2016-04-08.
 * www.zeeshanz.com
 */

public class MainActivity extends AppCompatActivity {

    SnapCropUpload mListener;
    ImageView ivImage;
    Button mButton;
    static final String TAG = "SNU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivImage = findViewById(R.id.photo_holder);
        mButton = findViewById(R.id.button);

        mButton.setOnClickListener(view -> {
            mListener = new SnapCropUpload();
            try {
                mListener.setListener(MainActivity.this, null, 800, 800, 0, null, true, imageUri -> {
                    Log.v(TAG, "Image path received: " + imageUri);
                    Uri tnail = SnapCropUpload.makeThumbnail(MainActivity.this, imageUri, 225);
                    if (tnail != null)
                        displayImage(imageUri);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     *  Display image
     */
    public void displayImage(Uri imageUri) {
        String imagePath = imageUri.getPath();
        Bitmap image = BitmapFactory.decodeFile(imagePath.replace("file:", ""));
        if (image != null)
            ivImage.setImageBitmap(image);
        else {
            File tempImage = new File(imagePath).getAbsoluteFile();
            if (tempImage.delete()) {
                Log.e(TAG, "image is null, temporary file deleted");
                Toast.makeText(this, "The image file you selected is corrupted", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}