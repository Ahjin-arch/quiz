package com.example.quiz2;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quiz2.MainActivity;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_sheet_results);

        TextView scoreTextView = findViewById(R.id.score_text_view);
        TextView resultsTextView = findViewById(R.id.results_container);
        Button restartButton = findViewById(R.id.restart_button);

        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL", 0);
        String results = getIntent().getStringExtra("RESULTS");

        // Mostrar puntaje
        scoreTextView.setText("Puntaje: " + score + "/" + total);

        // Procesar resultados con colores
        SpannableString spannable = new SpannableString(results);
        String[] resultBlocks = results.split("\n\n"); // Separar por preguntas

        int currentIndex = 0;
        for (String block : resultBlocks) {
            String[] lines = block.split("\n");
            if (lines.length >= 3) { // Asegurarse de que hay al menos pregunta, tu respuesta, correcta
                String userAnswerLine = lines[1]; // "Tu respuesta: [respuesta]"
                String correctAnswerLine = lines[2]; // "Correcta: [respuesta]"

                // Extraer respuestas
                String userAnswer = userAnswerLine.replace("Tu respuesta: ", "").trim();
                String correctAnswer = correctAnswerLine.replace("Correcta: ", "").trim();

                // Encontrar posición de "Correcta: [respuesta]"
                int correctStart = results.indexOf(correctAnswerLine, currentIndex);
                int correctEnd = correctStart + correctAnswerLine.length();

                // Aplicar color verde a "Correcta: [respuesta]"
                ForegroundColorSpan greenSpan = new ForegroundColorSpan(getResources().getColor(R.color.correct));
                spannable.setSpan(greenSpan, correctStart, correctEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Si la respuesta del usuario es incorrecta, colorear "Tu respuesta: [respuesta]" en rojo
                if (!userAnswer.equals(correctAnswer)) {
                    int userAnswerStart = results.indexOf(userAnswerLine, currentIndex);
                    int userAnswerEnd = userAnswerStart + userAnswerLine.length();
                    ForegroundColorSpan redSpan = new ForegroundColorSpan(getResources().getColor(R.color.incorrect));
                    spannable.setSpan(redSpan, userAnswerStart, userAnswerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                currentIndex = correctEnd;
            }
        }

        // Mostrar resultados con formato
        resultsTextView.setText(spannable);

        // Configurar botón de reinicio
        restartButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}