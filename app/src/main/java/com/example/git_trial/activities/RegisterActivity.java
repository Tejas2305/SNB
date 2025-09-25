package com.example.git_trial.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.git_trial.R;
import com.example.git_trial.auth.AuthService;
import com.example.git_trial.model.User;
import com.example.git_trial.model.UserRole;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;
    private AutoCompleteTextView spinnerRole, spinnerDepartment;
    private MaterialButton btnRegister, btnBack;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = AuthService.getInstance(this);
        initializeViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupSpinners() {
        // Role spinner
        String[] roles = {"Student", "Teacher"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setText("Student", false);

        // Department spinner
        String[] departments = {"Computer Science", "Information Technology", "Electronics", "Mechanical", "Civil"};
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, departments);
        spinnerDepartment.setAdapter(deptAdapter);
        spinnerDepartment.setText("Computer Science", false);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> performRegistration());
        btnBack.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String roleStr = spinnerRole.getText().toString();
        String department = spinnerDepartment.getText().toString();

        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Please enter your full name");
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("Please enter username");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Please enter email");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            etEmail.setError("Please enter a valid email");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter password");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        if (roleStr.isEmpty()) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        if (department.isEmpty()) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert role string to enum
        UserRole role = roleStr.equals("Teacher") ? UserRole.TEACHER : UserRole.STUDENT;

        // Create user object
        User newUser = new User(username, email, password, role, fullName, department);

        // Show loading state
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        // Simulate network delay and perform registration
        new Thread(() -> {
            boolean success = authService.register(newUser);

            runOnUiThread(() -> {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");

                if (success) {
                    Toast.makeText(this, "Account created successfully! Please login.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Registration failed. Username or email may already exist.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}