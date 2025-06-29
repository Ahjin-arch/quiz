package com.example.quiz2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistorialActivity extends AppCompatActivity {

    private ListView historialListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        historialListView = findViewById(R.id.historialListView);
        Button btnLimpiarHistorial = findViewById(R.id.btnCleanHistorial);
        Button btnCompartirHistorial = findViewById(R.id.btnCompartirHistorial);


        btnLimpiarHistorial.setOnClickListener(v -> showDeleteDialog());



        btnCompartirHistorial.setOnClickListener(v -> shareHistorial());

        SharedPreferences prefs = getSharedPreferences("HistorialPrefs", MODE_PRIVATE);
        Set<String> historialSet = prefs.getStringSet("historial", new HashSet<>());
        List<String> historialList = new ArrayList<>(historialSet);
        Collections.sort(historialList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historialList);
        historialListView.setAdapter(adapter);
    }

    private void showDeleteDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Eliminar el Historial?")
                .setMessage("estas seguro de eliminar el Historial?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deletehistorial();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void deletehistorial(){
        SharedPreferences prefs = getSharedPreferences("HistorialPrefs", MODE_PRIVATE);
        prefs.edit().remove("historial").apply();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        historialListView.setAdapter(adapter);
        Toast.makeText(this, "Historial eliminado", Toast.LENGTH_SHORT).show();
    }
    private void shareHistorial(){
        exportHistorial();
        File archivo = new File(getExternalFilesDir(null), "historial_Redes.txt");
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Historial de cuestionarios");
        intent.putExtra(Intent.EXTRA_TEXT, "Adjunto el historial de cuestionarios completados.");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Compartir historial con..."));
    }
    private void exportHistorial(){
        SharedPreferences prefs = getSharedPreferences("HistorialPrefs", MODE_PRIVATE);
        Set<String> historialSet = prefs.getStringSet("historial", new HashSet<>());
        StringBuilder contenido = new StringBuilder();

        for (String entrada : historialSet) {
            contenido.append(entrada).append("\n\n");
        }

        try {
            File archivo = new File(getExternalFilesDir(null), "historial_Redes.txt");
            FileOutputStream fos = new FileOutputStream(archivo);
            fos.write(contenido.toString().getBytes());
            fos.close();

            Toast.makeText(this, "Historial exportado a:\n" + archivo.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al exportar historial", Toast.LENGTH_SHORT).show();
        }
    }
}
