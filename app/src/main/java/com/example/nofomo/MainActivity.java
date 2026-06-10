package com.example.nofomo;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mAnalytics;
    private FirebaseFirestore db;
    private NewsAdapter adapter;
    private List<NewsArticle> newsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Analytics
        mAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        // Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        FirebaseCrashlytics.getInstance().log("MainActivity opened");

        // RecyclerView setup
        RecyclerView recyclerView = findViewById(R.id.newsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsList = new ArrayList<>();
        adapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(adapter);

        // Firebase Firestore
        db = FirebaseFirestore.getInstance();
        db.collection("articles")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String title = document.getString("title");
                            String summary = document.getString("summary");
                            newsList.add(new NewsArticle(title, summary, R.drawable.ic_tech));
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("Firestore", "Articles loaded: " + newsList.size());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Log.e("Firestore", "Error loading articles", e);
                    }
                });
    }
}