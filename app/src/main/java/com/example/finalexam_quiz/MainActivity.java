package com.example.finalexam_quiz;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
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
    private ArrayList<String> selectedAnswers = new ArrayList<>();
    private ArrayList<String> correctAnswers = new ArrayList<>();

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
        SharedPreferences sharedPreferences = getSharedPreferences("OptionPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        questionTextView = findViewById(R.id.questionTextView);
        optionButton1 = findViewById(R.id.optionButton1);
        optionButton2 = findViewById(R.id.optionButton2);
        optionButton3 = findViewById(R.id.optionButton3);
        optionButton4 = findViewById(R.id.optionButton4);
//        optionButton1.setOnClickListener(optionListener);
//        optionButton2.setOnClickListener(optionListener);
//        optionButton3.setOnClickListener(optionListener);
//        optionButton4.setOnClickListener(optionListener);

        View.OnClickListener optionClickListener = v -> onOptionSelected(v, currentIndex);

        optionButton1.setOnClickListener(optionClickListener);
        optionButton2.setOnClickListener(optionClickListener);
        optionButton3.setOnClickListener(optionClickListener);
        optionButton4.setOnClickListener(optionClickListener);

        optionButton1.setBackgroundColor(Color.parseColor("#D6EBFD"));
        optionButton2.setBackgroundColor(Color.parseColor("#D6EBFD"));
        optionButton3.setBackgroundColor(Color.parseColor("#D6EBFD"));
        optionButton4.setBackgroundColor(Color.parseColor("#D6EBFD"));
        optionButton1.setTextColor(Color.BLACK);
        optionButton2.setTextColor(Color.BLACK);
        optionButton3.setTextColor(Color.BLACK);
        optionButton4.setTextColor(Color.BLACK);

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
                if (!questions.isEmpty()) {
                    showQuestion(questions.get(0));
                }
            });
        }).start();

        backButton = findViewById(R.id.backButton);
        nextButton = findViewById(R.id.nextButton);
        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            // Clear SharedPreferences
            editor.clear();
            editor.apply();

            // Reset currentIndex and score
            currentIndex = 0;
            score = 0;

            // Show the first question
            showQuestion(questions.get(currentIndex));

            // Reset the background and text color of the option buttons
            optionButton1.setBackgroundColor(Color.parseColor("#D6EBFD"));
            optionButton2.setBackgroundColor(Color.parseColor("#D6EBFD"));
            optionButton3.setBackgroundColor(Color.parseColor("#D6EBFD"));
            optionButton4.setBackgroundColor(Color.parseColor("#D6EBFD"));

            // Reset the state of the question buttons
            for (Button button : buttons) {
                button.setSelected(false);
                button.setBackgroundResource(R.drawable.button_states);
            }

            homeButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.VISIBLE);
        });
        nextButton.setOnClickListener(v -> {
            if (currentIndex < questions.size() - 1) {
                currentIndex++;
                showQuestion(questions.get(currentIndex));
            } else {
                questionTextView.setText("Quiz Finished! Your score: " + calculateScore() + "/" + questions.size());
                optionButton1.setVisibility(View.GONE);
                optionButton2.setVisibility(View.GONE);
                optionButton3.setVisibility(View.GONE);
                optionButton4.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                //hide the buttons of questions
                for (Button button : buttons) {
                    button.setVisibility(View.GONE);
                }
                homeButton.setVisibility(View.VISIBLE);
            }
        });
        backButton.setOnClickListener(v -> {
            currentIndex = currentIndex -  1;
            showQuestion(questions.get(currentIndex));
        });
    }

    private void onOptionSelected(View view, int questionNumber) {
        Button option1Button = findViewById(R.id.optionButton1);
        Button option2Button = findViewById(R.id.optionButton2);
        Button option3Button = findViewById(R.id.optionButton3);
        Button option4Button = findViewById(R.id.optionButton4);
        // Reset background color for all buttons
        option1Button.setBackgroundColor(Color.parseColor("#D6EBFD"));
        option2Button.setBackgroundColor(Color.parseColor("#D6EBFD"));
        option3Button.setBackgroundColor(Color.parseColor("#D6EBFD"));
        option4Button.setBackgroundColor(Color.parseColor("#D6EBFD"));

        Button selectedOption = (Button) view;

        selectedOption.setBackgroundColor(Color.parseColor("#6a5be2"));

        SharedPreferences sharedPreferences = getSharedPreferences("OptionPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastSelectedOptionState" + questionNumber, selectedOption.getText().toString());
        editor.apply();

        buttons.get(currentIndex).setBackgroundResource(R.drawable.button_answered_state);
        String selectedAnswer = selectedOption.getText().toString();
        selectedAnswers.add(selectedAnswer);
    }


    View.OnClickListener optionListener = view -> {
        Button b = (Button) view;
        String answer = b.getText().toString();
        if (answer.equals(questions.get(currentIndex).answer)) {
            score++;
        }

        buttons.get(currentIndex).setBackgroundResource(R.drawable.button_answered_state);
//        if (currentIndex < questions.size() - 1) {
//            currentIndex++;
//            showQuestion(questions.get(currentIndex));
//        } else {
//            questionTextView.setText("Quiz Finished! Your score: " + score);
//            optionButton1.setVisibility(View.GONE);
//            optionButton2.setVisibility(View.GONE);
//            optionButton3.setVisibility(View.GONE);
//            optionButton4.setVisibility(View.GONE);
//            nextButton.setVisibility(View.GONE);
//            backButton.setVisibility(View.GONE);
//        }
    };

    private void showQuestion(Question question) {
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> options = new Gson().fromJson(question.options, type);
        correctAnswers.add(question.answer);
        optionButton1.setVisibility(View.VISIBLE);
        optionButton2.setVisibility(View.VISIBLE);
        optionButton3.setVisibility(View.VISIBLE);
        optionButton4.setVisibility(View.VISIBLE);
        optionButton1.setBackgroundColor(Color.parseColor("#D6EBFD"));
        optionButton2.setBackgroundColor(Color.parseColor("#D6EBFD"));
        optionButton3.setBackgroundColor(Color.parseColor("#D6EBFD"));
        optionButton4.setBackgroundColor(Color.parseColor("#D6EBFD"));
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
        SharedPreferences sharedPreferences = getSharedPreferences("OptionPrefs", Context.MODE_PRIVATE);
        String lastSelectedOptionState = sharedPreferences.getString("lastSelectedOptionState" + currentIndex, null);

        if (lastSelectedOptionState != null) {
            if (optionButton1.getText().toString().equals(lastSelectedOptionState)) {
                optionButton1.setBackgroundColor(Color.parseColor("#6a5be2"));
            } else if (optionButton2.getText().toString().equals(lastSelectedOptionState)) {
                optionButton2.setBackgroundColor(Color.parseColor("#6a5be2"));
            } else if (optionButton3.getText().toString().equals(lastSelectedOptionState)) {
                optionButton3.setBackgroundColor(Color.parseColor("#6a5be2"));
            } else if (optionButton4.getText().toString().equals(lastSelectedOptionState)) {
                optionButton4.setBackgroundColor(Color.parseColor("#6a5be2"));
            }
        }
        if (currentIndex > 0) {
            backButton.setVisibility(View.VISIBLE);
        }else{
            backButton.setVisibility(View.GONE);
        }
    }
    private int calculateScore() {
        score = 0; // Reset the score
        for (int i = 0; i < correctAnswers.size(); i++) {
            if (i < selectedAnswers.size() && correctAnswers.get(i).equals(selectedAnswers.get(i))) {
                score++;
            }
        }
        return score;
    }
}