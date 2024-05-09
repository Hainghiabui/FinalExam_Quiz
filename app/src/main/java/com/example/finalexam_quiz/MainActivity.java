package com.example.finalexam_quiz;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView questionTextView;
    private Button optionButton1, optionButton2, optionButton3, optionButton4;
    AppDatabase db;
    private int currentIndex = 0;
    private Button backButton, nextButton;
    private int score = 0;
    private List<Question> questions = new ArrayList<>();
    private Button currentButton = null;
    private ArrayList<Button> buttons = new ArrayList<>(); // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getDatabase(getApplicationContext());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        try {
            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            Log.d("debug", json);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        questionTextView = findViewById(R.id.questionTextView);
        optionButton1 = findViewById(R.id.optionButton1);
        optionButton2 = findViewById(R.id.optionButton2);
        optionButton3 = findViewById(R.id.optionButton3);
        optionButton4 = findViewById(R.id.optionButton4);
        optionButton1.setOnClickListener(optionListener);
        optionButton2.setOnClickListener(optionListener);
        optionButton3.setOnClickListener(optionListener);
        optionButton4.setOnClickListener(optionListener);

        GridLayout questionsGridLayout = findViewById(R.id.questionsGridLayout);

        new Thread(() -> {
            questions.addAll(db.questionDao().getAll());
            runOnUiThread(() -> {

                for (int i = 0; i < questions.size(); i++) {
                    Button button = new Button(MainActivity.this);
                    button.setText(String.valueOf(i + 1));
                    button.setTextColor(Color.WHITE);
                    final int questionIndex = i;
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);


                    button.setLayoutParams(params);
                    button.setBackground(ContextCompat.getDrawable(this, R.drawable.button_states));

                    button.setOnClickListener(v -> {
                        // Reset color of previously selected button
                        if (currentButton != null) {
                            currentButton.setSelected(false);

                        }
                        // Change color of the selected button
                        button.setSelected(true);

                        // Keep track of the current selected button
                        currentButton = button;

                        currentIndex = questionIndex;
                        showQuestion(questions.get(currentIndex));
                    });
                    buttons.add(button); // Add this line

                    questionsGridLayout.addView(button);
                }
                showQuestion(questions.get(0));
            });
        }).start();

        backButton = findViewById(R.id.backButton);
        nextButton = findViewById(R.id.nextButton);
        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            currentIndex = 0;
            score = 0;
            showQuestion(questions.get(currentIndex));
            homeButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.VISIBLE);
        });
        nextButton.setOnClickListener(v -> {
            if (currentIndex < questions.size() - 1) {
                currentIndex++;
                showQuestion(questions.get(currentIndex));
            } else {
                questionTextView.setText("Quiz Finished! Your score: " + score);
                optionButton1.setVisibility(View.GONE);
                optionButton2.setVisibility(View.GONE);
                optionButton3.setVisibility(View.GONE);
                optionButton4.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                homeButton.setVisibility(View.VISIBLE);
            }
        });
        backButton.setOnClickListener(v -> {
            currentIndex = currentIndex -  1;
            showQuestion(questions.get(currentIndex));
        });
    }

    View.OnClickListener optionListener = view -> {
        Button b = (Button) view;
        String answer = b.getText().toString();
        if (answer.equals(questions.get(currentIndex).answer)) {
            score++;
        }

        buttons.get(currentIndex).setBackgroundResource(R.drawable.button_answered_state);
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion(questions.get(currentIndex));
        } else {
            questionTextView.setText("Quiz Finished! Your score: " + score);
            optionButton1.setVisibility(View.GONE);
            optionButton2.setVisibility(View.GONE);
            optionButton3.setVisibility(View.GONE);
            optionButton4.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);
        }

    };

    private void showQuestion(Question question) {
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> options = new Gson().fromJson(question.options, type);
        optionButton1.setVisibility(View.VISIBLE);
        optionButton2.setVisibility(View.VISIBLE);
        optionButton3.setVisibility(View.VISIBLE);
        optionButton4.setVisibility(View.VISIBLE);
        if (currentButton != null) {
            currentButton.setSelected(false);
        }

        // Change color of the current button
        currentButton = buttons.get(currentIndex);
        currentButton.setSelected(true);
        String questionWithIndex = (currentIndex + 1) + ". " + question.content;
        questionTextView.setText(questionWithIndex);

        optionButton1.setText(options.get(0));
        optionButton2.setText(options.get(1));
        optionButton3.setText(options.get(2));
        optionButton4.setText(options.get(3));

        if (currentIndex > 0) {
            backButton.setVisibility(View.VISIBLE);
        }else{
            backButton.setVisibility(View.GONE);
        }


    }
}