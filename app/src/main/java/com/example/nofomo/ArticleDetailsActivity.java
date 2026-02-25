package com.example.nofomo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ArticleDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_details);

        // קישור לרכיבים
        ImageView imageView = findViewById(R.id.detailImage);
        TextView titleView = findViewById(R.id.detailTitle);
        TextView summaryView = findViewById(R.id.detailSummary);
        Button btnBack = findViewById(R.id.btnBack);

        // קבלת הנתונים שנשלחו מהמסך הקודם
        String title = getIntent().getStringExtra("title");
        String summary = getIntent().getStringExtra("summary");
        int image = getIntent().getIntExtra("image", 0);

        // הצגת הנתונים
        titleView.setText(title);
        summaryView.setText(summary);
        imageView.setImageResource(image);

        // כפתור חזרה
        btnBack.setOnClickListener(v -> finish());
    }
}