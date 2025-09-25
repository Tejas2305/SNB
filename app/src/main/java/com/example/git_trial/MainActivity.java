package com.example.git_trial;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.git_trial.auth.AuthService;
import com.example.git_trial.model.User;
import com.example.git_trial.activities.DashboardActivity;
import com.example.git_trial.activities.RegisterActivity;

public class MainActivity extends AppCompatActivity {
    
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin, btnAdminLogin;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Check if user is already logged in
        authService = AuthService.getInstance(this);
        if (authService.isLoggedIn()) {
            navigateToDashboard();
            return;
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
    }
    
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        btnAdminLogin.setOnClickListener(v -> showAdminLoginDialog());
        findViewById(R.id.tvRegister).setOnClickListener(v -> openRegistration());
    }
    
    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (username.isEmpty()) {
            etUsername.setError("Please enter username or email");
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Please enter password");
            return;
        }
        
        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");
        
        // Simulate network delay and perform authentication
        new Thread(() -> {
            User user = authService.login(username, password);
            
            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");
                
                if (user != null) {
                    Toast.makeText(this, "Welcome, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
                    etPassword.setText("");
                    etPassword.requestFocus();
                }
            });
        }).start();
    }
    
    private void showAdminLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Login")
                .setMessage("Use admin credentials:\nUsername: admin, Password: admin123\nor\nUsername: hod_cs, Password: hod123")
                .setPositiveButton("OK", (dialog, which) -> {
                    etUsername.setText("admin");
                    etPassword.setText("admin123");
                    etUsername.requestFocus();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void openRegistration() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}