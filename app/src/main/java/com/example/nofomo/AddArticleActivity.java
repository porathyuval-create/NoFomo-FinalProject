package com.example.nofomo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * מסך יצירת כתבה חדשה.
 * המסך משתמש במצלמת המכשיר לצילום תמונה, וכותב את הכתבה ל-Firestore.
 */
public class AddArticleActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private ImageView imagePreview;
    private EditText titleInput, summaryInput;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mAnalytics;

    private String encodedImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAnalytics = FirebaseAnalytics.getInstance(this);

        // אם אין משתמש מחובר, אין מי שיפרסם את הכתבה
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Bundle screenBundle = new Bundle();
        screenBundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "AddArticleActivity");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, screenBundle);

        imagePreview = findViewById(R.id.articleImagePreview);
        titleInput = findViewById(R.id.titleInput);
        summaryInput = findViewById(R.id.summaryInput);
        progressBar = findViewById(R.id.addProgress);

        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        Button btnPublish = findViewById(R.id.btnPublish);

        // שימוש ביכולת של המכשיר - מצלמה
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishArticle();
            }
        });
    }

    private void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(this, "Camera is not available on this device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.get("data") != null) {
                Bitmap photo = (Bitmap) extras.get("data");
                imagePreview.setImageBitmap(photo);
                encodedImage = encodeImage(photo);
            }
        }
    }

    /**
     * ממיר את התמונה למחרוזת כדי שניתן יהיה לשמור אותה כשדה במסמך ב-Firestore.
     */
    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void publishArticle() {
        final String title = titleInput.getText().toString().trim();
        final String summary = summaryInput.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(summary)) {
            Toast.makeText(this, "Please fill in title and summary", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> article = new HashMap<>();
        article.put("title", title);
        article.put("summary", summary);
        article.put("imageBase64", encodedImage);
        article.put("authorEmail", mAuth.getCurrentUser().getEmail());
        article.put("authorId", mAuth.getCurrentUser().getUid());
        article.put("timestamp", System.currentTimeMillis());

        db.collection("articles")
                .add(article)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressBar.setVisibility(View.GONE);

                        Bundle bundle = new Bundle();
                        bundle.putString("article_title", title);
                        mAnalytics.logEvent("article_published", bundle);

                        Toast.makeText(AddArticleActivity.this,
                                "Article published", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Toast.makeText(AddArticleActivity.this,
                                "Failed to publish article", Toast.LENGTH_LONG).show();
                    }
                });
    }
}