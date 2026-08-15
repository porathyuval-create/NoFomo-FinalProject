package com.example.nofomo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * המסך הראשי - פיד הכתבות.
 * כל הנתונים נטענים מ-Firestore, אין נתונים קבועים בקוד.
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mAnalytics;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private NewsAdapter adapter;
    private List<NewsArticle> newsList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mAnalytics = FirebaseAnalytics.getInstance(this);

        // בקרת גישה: משתמש שאינו מחובר מוחזר למסך ההתחברות
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        FirebaseCrashlytics.getInstance().setUserId(mAuth.getCurrentUser().getUid());

        progressBar = findViewById(R.id.mainProgress);
        Button btnAddArticle = findViewById(R.id.btnAddArticle);
        Button btnProfile = findViewById(R.id.btnProfile);

        RecyclerView recyclerView = findViewById(R.id.newsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsList = new ArrayList<>();
        adapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(adapter);

        btnAddArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddArticleActivity.class));
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // רענון הפיד בכל חזרה למסך, כדי שכתבה חדשה תופיע מיד.
        // הבדיקה מונעת טעינה כשהמסך בדרך להיסגר או כשאין משתמש מחובר.
        if (isFinishing() || mAuth.getCurrentUser() == null || progressBar == null) {
            return;
        }
        loadArticles();
    }

    /**
     * טוען את כל הכתבות מ-Firestore, מהחדשה לישנה.
     */
    private void loadArticles() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("articles")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        progressBar.setVisibility(View.GONE);
                        newsList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            NewsArticle article = new NewsArticle(
                                    doc.getString("title"),
                                    doc.getString("summary"),
                                    doc.getString("imageBase64")
                            );
                            newsList.add(article);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Toast.makeText(MainActivity.this,
                                "Failed to load articles", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}