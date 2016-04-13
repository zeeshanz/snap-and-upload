package com.jixte.snapcropuploadsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.jixte.snapcropupload.ISnapCropUploadListener;
import com.jixte.snapcropupload.SnapCropUpload;

import java.io.File;

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

        ivImage = (ImageView) findViewById(R.id.photo_holder);
        mButton = (Button) findViewById(R.id.button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener = new SnapCropUpload();
                mListener.setListener(MainActivity.this, null, 800, 800, 0, null, true, new ISnapCropUploadListener() {
                            @Override
                            public void onReceiveImagePath(String imagePath) {
                                Log.v(TAG, "Image path received: " + imagePath);
                                displayImage(imagePath);
                            }
                        }
                );
            }
        });
    }

    /**
     *  Display image
     */
    public void displayImage(String imagePath) {
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