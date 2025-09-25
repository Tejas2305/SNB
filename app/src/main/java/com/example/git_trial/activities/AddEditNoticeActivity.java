package com.example.git_trial.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.example.git_trial.R;
import com.example.git_trial.auth.AuthService;
import com.example.git_trial.database.NoticeDatabase;
import com.example.git_trial.model.Notice;
import com.example.git_trial.model.NoticeCategory;
import com.example.git_trial.model.User;

public class AddEditNoticeActivity extends AppCompatActivity {

    public static final String EXTRA_NOTICE = "extra_notice";
    public static final String EXTRA_EDIT_MODE = "extra_edit_mode";

    private TextInputEditText etTitle, etDescription, etSubject;
    private AutoCompleteTextView spinnerCategory, spinnerDepartment;
    private Slider sliderPriority;
    private MaterialButton btnSave, btnCancel, btnAttachFile;
    private MaterialToolbar toolbar;

    private AuthService authService;
    private NoticeDatabase noticeDatabase;
    private User currentUser;
    private Notice editingNotice;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_notice);

        authService = AuthService.getInstance(this);
        noticeDatabase = new NoticeDatabase(this);
        currentUser = authService.getCurrentUser();

        if (currentUser == null || !authService.canManageNotices()) {
            finish();
            return;
        }

        // Check if we're editing an existing notice
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra(EXTRA_EDIT_MODE, false);
        if (isEditMode) {
            editingNotice = (Notice) intent.getSerializableExtra(EXTRA_NOTICE);
        }

        initializeViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();
        
        if (isEditMode && editingNotice != null) {
            populateFields();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etSubject = findViewById(R.id.etSubject);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        sliderPriority = findViewById(R.id.sliderPriority);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnAttachFile = findViewById(R.id.btnAttachFile);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Edit Notice" : "Add Notice");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        // Category spinner
        String[] categories = {"Common", "Department", "Annual", "Subject-Specific"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(categoryAdapter);
        
        // Set default based on user role
        if (authService.isTeacher()) {
            spinnerCategory.setText("Department", false);
        } else {
            spinnerCategory.setText("Common", false);
        }

        // Department spinner
        String[] departments = {"All", "Computer Science", "Information Technology", "Electronics", "Mechanical", "Civil"};
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, departments);
        spinnerDepartment.setAdapter(deptAdapter);
        
        // Set user's department as default
        spinnerDepartment.setText(currentUser.getDepartment(), false);

        // Category change listener to show/hide subject field
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String category = categories[position];
            etSubject.setEnabled("Subject-Specific".equals(category));
            if (!"Subject-Specific".equals(category)) {
                etSubject.setText("");
            }
        });
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveNotice());
        btnCancel.setOnClickListener(v -> onBackPressed());
        btnAttachFile.setOnClickListener(v -> {
            // TODO: Implement file attachment
            Toast.makeText(this, "File attachment feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void populateFields() {
        etTitle.setText(editingNotice.getTitle());
        etDescription.setText(editingNotice.getDescription());
        spinnerCategory.setText(editingNotice.getCategory().getDisplayName(), false);
        
        if (editingNotice.getDepartment() != null) {
            spinnerDepartment.setText(editingNotice.getDepartment(), false);
        }
        
        if (editingNotice.getSubject() != null) {
            etSubject.setText(editingNotice.getSubject());
            etSubject.setEnabled(true);
        }
        
        sliderPriority.setValue(editingNotice.getPriority());
    }

    private void saveNotice() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String categoryStr = spinnerCategory.getText().toString();
        String department = spinnerDepartment.getText().toString();
        String subject = etSubject.getText().toString().trim();
        int priority = (int) sliderPriority.getValue();

        // Validation
        if (title.isEmpty()) {
            etTitle.setError("Please enter a title");
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Please enter a description");
            return;
        }

        if (categoryStr.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (department.isEmpty()) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Subject-Specific".equals(categoryStr) && subject.isEmpty()) {
            etSubject.setError("Please enter a subject for subject-specific notices");
            return;
        }

        // Convert category string to enum
        NoticeCategory category;
        switch (categoryStr) {
            case "Department":
                category = NoticeCategory.DEPARTMENT;
                break;
            case "Annual":
                category = NoticeCategory.ANNUAL;
                break;
            case "Subject-Specific":
                category = NoticeCategory.SUBJECT_SPECIFIC;
                break;
            default:
                category = NoticeCategory.COMMON;
        }

        // Show loading state
        btnSave.setEnabled(false);
        btnSave.setText(isEditMode ? "Updating..." : "Saving...");

        // Create or update notice
        new Thread(() -> {
            boolean success;
            
            if (isEditMode) {
                // Update existing notice
                editingNotice.setTitle(title);
                editingNotice.setDescription(description);
                editingNotice.setCategory(category);
                editingNotice.setDepartment(department);
                editingNotice.setSubject("Subject-Specific".equals(categoryStr) ? subject : null);
                editingNotice.setPriority(priority);
                editingNotice.updateTimestamp();
                
                success = noticeDatabase.updateNotice(editingNotice);
            } else {
                // Create new notice
                Notice newNotice = new Notice(title, description, category, currentUser.getUserId(), currentUser.getFullName());
                newNotice.setDepartment(department);
                newNotice.setSubject("Subject-Specific".equals(categoryStr) ? subject : null);
                newNotice.setPriority(priority);
                
                success = noticeDatabase.addNotice(newNotice);
            }

            runOnUiThread(() -> {
                btnSave.setEnabled(true);
                btnSave.setText(isEditMode ? "Update Notice" : "Save Notice");

                if (success) {
                    Toast.makeText(this, isEditMode ? "Notice updated successfully!" : "Notice created successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to " + (isEditMode ? "update" : "save") + " notice. Please try again.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    @Override
    public void onBackPressed() {
        // Check if there are unsaved changes
        if (hasUnsavedChanges()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to go back?")
                    .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        if (!isEditMode) {
            // New notice - check if any field has content
            return !etTitle.getText().toString().trim().isEmpty() ||
                   !etDescription.getText().toString().trim().isEmpty();
        } else {
            // Editing - check if any field has changed
            return !editingNotice.getTitle().equals(etTitle.getText().toString().trim()) ||
                   !editingNotice.getDescription().equals(etDescription.getText().toString().trim());
        }
    }
}