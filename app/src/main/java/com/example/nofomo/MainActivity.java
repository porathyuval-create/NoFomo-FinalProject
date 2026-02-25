package com.example.nofomo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // מחבר את הקוד לעיצוב ה-XML של המסך הראשי [cite: 127]
        setContentView(R.layout.activity_main);

        // 1. קישור לרכיב ה-RecyclerView מה-XML [cite: 15, 128]
        RecyclerView recyclerView = findViewById(R.id.newsRecyclerView);

        // 2. הגדרת אופן התצוגה כרשימה אנכית [cite: 187]
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. יצירת רשימת כתבות (נתונים דינמיים לפי דרישות הפרויקט) [cite: 14, 15]
        List<NewsArticle> newsList = new ArrayList<>();

        // הוספת כתבות עם תוכן מורחב ואייקונים שונים מה-drawable [cite: 16, 38]
        // שים לב: אם נתת שמות אחרים לאייקונים, שנה את ic_tech/ic_sports/ic_movie לשמות שבחרת

        newsList.add(new NewsArticle(
                "Tech Giants Merge",
                "In a historic move, two of the world's biggest tech companies have officially merged. This 50-billion-dollar deal aims to create a new AI powerhouse. Experts predict this will change how we use smartphones forever.",
                R.drawable.ic_tech)); // אייקון טכנולוגיה

        newsList.add(new NewsArticle(
                "Local Sports Win",
                "The home team defied all odds last night, winning the national championship in the final seconds of the game. Fans are currently celebrating in the streets. This is their first title in over a decade.",
                R.drawable.ic_sports)); // אייקון ספורט

        newsList.add(new NewsArticle(
                "New Movie Release",
                "The summer's biggest blockbuster is finally hitting theaters this weekend. Critics are already calling it a masterpiece with groundbreaking visual effects. Tickets are selling out fast across the country.",
                R.drawable.ic_movie)); // אייקון סרטים

        // 4. יצירת ה-Adapter וחיבורו ל-RecyclerView [cite: 15, 37]
        NewsAdapter adapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(adapter);
    }
}