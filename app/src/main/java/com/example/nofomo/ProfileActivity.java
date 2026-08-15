package com.example.nofomo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseAnalytics mAnalytics;

    private NewsAdapter adapter;
    private List<NewsArticle> myArticles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle screenBundle = new Bundle();
        screenBundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "ProfileActivity");
        mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, screenBundle);

        TextView profileEmail = findViewById(R.id.profileEmail);
        TextView profileMethod = findViewById(R.id.profileMethod);
        Button btnSignOut = findViewById(R.id.btnSignOut);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            profileEmail.setText(user.getEmail());
            String provider = user.getProviderData()
                    .get(user.getProviderData().size() - 1).getProviderId();
            if (provider.contains("google")) {
                profileMethod.setText("Signed in with Google");
            } else {
                profileMethod.setText("Signed in with Email");
            }
        }

        RecyclerView recyclerView = findViewById(R.id.myArticlesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myArticles = new ArrayList<>();
        adapter = new NewsAdapter(myArticles);
        recyclerView.setAdapter(adapter);

        loadMyArticles();

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    // טוען מ-Firestore רק את הכתבות שהמשתמש הנוכחי פרסם
    private void loadMyArticles() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("articles")
                .whereEqualTo("authorId", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        myArticles.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            NewsArticle article = new NewsArticle(
                                    doc.getString("title"),
                                    doc.getString("summary"),
                                    doc.getString("imageBase64")
                            );
                            myArticles.add(article);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        Toast.makeText(ProfileActivity.this,
                                "Failed to load your articles", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        client.signOut();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}