package com.example.objectdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private Button snapBtn;
    private Button detectBtn;
    private Button selectBtn;
    private ImageView imageView;
    private Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        snapBtn = findViewById(R.id.snapBtn);
        detectBtn = findViewById(R.id.detectBtn);
        selectBtn = findViewById(R.id.selectBtn);
        imageView = findViewById(R.id.imageView);
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectImage();
            }
        });
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    static final int REQUEST_IMAGE_SELECT = 2;

    private void selectImage(){
        Intent selectImage = new Intent(Intent.ACTION_GET_CONTENT);
        selectImage.setType("image/*");
        startActivityForResult(Intent.createChooser(selectImage,"pick an image"),REQUEST_IMAGE_SELECT);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
        else if(requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK){
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                imageBitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(imageBitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void detectImage(){



        FirebaseVisionObjectDetectorOptions options =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()  // Optional
                        .build();

        FirebaseVisionObjectDetector objectDetector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);

        objectDetector.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionObject>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionObject> detectedObjects) {

                                Paint myRectPaint = new Paint();
                                myRectPaint.setStrokeWidth(5);
                                myRectPaint.setColor(Color.RED);
                                myRectPaint.setStyle(Paint.Style.STROKE);

                                Bitmap tempBitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.RGB_565);
                                Canvas tempCanvas = new Canvas(tempBitmap);
                                tempCanvas.drawBitmap(imageBitmap, 0, 0, null);

                                for (FirebaseVisionObject obj : detectedObjects) {
                                    Integer id = obj.getTrackingId();
                                    Rect bounds = obj.getBoundingBox();
                                    tempCanvas.drawRect(bounds, myRectPaint);


                                }
                                imageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });


    }


}