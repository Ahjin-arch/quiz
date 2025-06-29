package com.example.quiz2;
import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private LinearLayout questionsContainer;
    private Button submitButton, btnHistorialView;
    private List<Question> allQuestions = new ArrayList<>();
    private List<Question> selectedQuestions = new ArrayList<>();
    private List<RadioGroup> radioGroups = new ArrayList<>();
    private Set<Integer> usedQuestionIds = new HashSet<>();
    private TextView timerTextView;
    private Button startTimerButton;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private static final int NUMERO_DE_PREGUNTAS = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        questionsContainer = findViewById(R.id.questions_container);
        submitButton = findViewById(R.id.submit_button);
        btnHistorialView=findViewById(R.id.btnHistorialView);
        timerTextView = findViewById(R.id.timerTextView);
        startTimerButton = findViewById(R.id.startTimerButton);

        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.APPEARING);
        transition.enableTransitionType(LayoutTransition.DISAPPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        questionsContainer.setLayoutTransition(transition);



        btnHistorialView.setOnClickListener(v -> {
            startActivity(new Intent(this, HistorialActivity.class));
        });
        startTimerButton.setOnClickListener(v -> {
            startTime = System.currentTimeMillis();
            timerHandler.post(timerRunnable);
        });


        // Cargar preguntas desde JSON
        loadQuestionsFromJson();

        // Seleccionar 5 preguntas aleatorias
        selectRandomQuestions();

        // Mostrar preguntas en la interfaz
        displayQuestions();

        // Configurar acción del botón Enviar
        submitButton.setOnClickListener(v -> submitAnswers());
    }
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = System.currentTimeMillis() - startTime;
            int seconds = (int) (elapsed / 1000) % 60;
            int minutes = (int) (elapsed / 60000);
            timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };
    private void loadQuestionsFromJson() {
        try {
            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int id = obj.getInt("id");
                String type = obj.getString("type");
                String question = obj.getString("question");
                boolean answerBoolean = obj.getBoolean("answerBoolean");
                String answerString = obj.getString("answerString");
                String explanation = obj.getString("explanation");
                JSONArray optionsArray = obj.getJSONArray("options");
                List<String> options = new ArrayList<>();
                for (int j = 0; j < optionsArray.length(); j++) {
                    options.add(optionsArray.getString(j));
                }
                allQuestions.add(new Question(id, type, question, answerBoolean, answerString, explanation, options));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar preguntas", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectRandomQuestions() {
        selectedQuestions.clear();
        loadUsedIdsFromPrefs();

        // Filtrar preguntas disponibles que no se hayan usado
        List<Question> availableQuestions = new ArrayList<>();
        for (Question q : allQuestions) {
            if (!usedQuestionIds.contains(q.getId())) {
                availableQuestions.add(q);
            }
        }

        // ¿Hay suficientes preguntas únicas para continuar?
        if (availableQuestions.size() < NUMERO_DE_PREGUNTAS) {
            showResetDialog();
            return;
        }

        Collections.shuffle(availableQuestions);

        for (int i = 0; i < NUMERO_DE_PREGUNTAS; i++) {
            Question question = availableQuestions.get(i);
            selectedQuestions.add(question);
            usedQuestionIds.add(question.getId());
        }

        saveUsedIdsToPrefs();
    }
    private void showResetDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Reiniciar cuestionario?")
                .setMessage("deseas reiniciar el cuestionario?")
                .setPositiveButton("Reiniciar", (dialog, which) -> {
                    usedQuestionIds.clear();
                    saveUsedIdsToPrefs();
                    selectRandomQuestions();
                    displayQuestions();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }



    private void displayQuestions() {
        questionsContainer.removeAllViews();
        radioGroups.clear();

        for (int i = 0; i < selectedQuestions.size(); i++) {
            Question question = selectedQuestions.get(i);

            // Crear contenedor para la pregunta
            LinearLayout questionLayout = new LinearLayout(this);
            questionLayout.setOrientation(LinearLayout.VERTICAL);
            questionLayout.setPadding(16, 16, 16, 16);

            // Añadir texto de la pregunta
            TextView questionText = new TextView(this);
            questionText.setText(question.getId() + ". " + question.getQuestion());
            questionText.setTextSize(18);

            // Crear RadioGroup para las opciones
            RadioGroup radioGroup = new RadioGroup(this);
            radioGroup.setId(View.generateViewId());
            radioGroup.setTag(i); // Para identificar la pregunta

            if (question.getType().equals("true_false")) {
                RadioButton trueButton = new RadioButton(this);
                trueButton.setText(R.string.true_option);
                trueButton.setTag("true");
                RadioButton falseButton = new RadioButton(this);
                falseButton.setText(R.string.false_option);
                falseButton.setTag("false");
                radioGroup.addView(trueButton);
                radioGroup.addView(falseButton);
            } else {
                for (String option : question.getOptions()) {
                    RadioButton optionButton = new RadioButton(this);
                    optionButton.setText(option);
                    optionButton.setTag(option.charAt(0)); // 'a', 'b', 'c', 'd'
                    radioGroup.addView(optionButton);
                }
            }

            // Añadir elementos al contenedor
            questionLayout.addView(questionText);
            questionLayout.addView(radioGroup);
            questionsContainer.addView(questionLayout);
            radioGroups.add(radioGroup);
        }
    }

    private void submitAnswers() {
        int score = 0;
        StringBuilder results = new StringBuilder();

        for (int i = 0; i < selectedQuestions.size(); i++) {
            Question question = selectedQuestions.get(i);
            RadioGroup radioGroup = radioGroups.get(i);
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(this, "Por favor, responde todas las preguntas", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedButton = findViewById(selectedId);
            String userAnswer = selectedButton.getTag().toString();
            boolean isCorrect = false;

            if (question.getType().equals("true_false")) {
                boolean userBoolean = userAnswer.equals("true");
                isCorrect = userBoolean == question.getAnswerBoolean();
            } else {
                isCorrect = userAnswer.equals(question.getAnswerString());
            }

            if (isCorrect) {
                score++;
            }

            results.append(question.getId()).append(". ").append(question.getQuestion()).append("\n");
            results.append("Tu respuesta: ").append(userAnswer).append("\n");
            results.append("Correcta: ").append(question.getType().equals("true_false") ? question.getAnswerBoolean() : question.getAnswerString()).append("\n");
            results.append("Explicación: ").append(question.getExplanation()).append("\n\n");
        }

        // Iniciar ResultsActivity
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL", selectedQuestions.size());
        intent.putExtra("RESULTS", results.toString());

        //guardas un historial
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        String resumen = "Fecha: " + fecha + "\nPuntaje: " + score + "/" + selectedQuestions.size() + "\n\n" + results.toString();
        SharedPreferences prefs = getSharedPreferences("HistorialPrefs", MODE_PRIVATE);
        Set<String> historial = new HashSet<>(prefs.getStringSet("historial", new HashSet<>()));
        historial.add(resumen);
        prefs.edit().putStringSet("historial", historial).apply();


        startActivity(intent);
    }

    private void saveUsedIdsToPrefs() {
        SharedPreferences prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("used_ids", TextUtils.join(",", usedQuestionIds))
                .apply();
    }
    private void loadUsedIdsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE);
        String ids = prefs.getString("used_ids", "");
        if (!ids.isEmpty()) {
            String[] split = ids.split(",");
            for (String id : split) {
                usedQuestionIds.add(Integer.parseInt(id));
            }
        }
    }


}