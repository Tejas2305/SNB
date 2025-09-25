package com.example.git_trial.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.example.git_trial.MainActivity;
import com.example.git_trial.R;
import com.example.git_trial.auth.AuthService;
import com.example.git_trial.database.NoticeDatabase;
import com.example.git_trial.model.Notice;
import com.example.git_trial.model.NoticeCategory;
import com.example.git_trial.model.User;
import com.example.git_trial.adapters.NoticeAdapter;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvRoleInfo;
    private TabLayout tabLayout;
    private RecyclerView recyclerNotices;
    private FloatingActionButton fabAddNotice;
    
    private AuthService authService;
    private NoticeDatabase noticeDatabase;
    private User currentUser;
    private NoticeAdapter noticeAdapter;
    private List<Notice> noticeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        authService = AuthService.getInstance(this);
        noticeDatabase = new NoticeDatabase(this);
        currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupUserInterface();
        setupRecyclerView();
        setupTabLayout();
        loadNotices();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRoleInfo = findViewById(R.id.tvRoleInfo);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerNotices = findViewById(R.id.recyclerNotices);
        fabAddNotice = findViewById(R.id.fabAddNotice);
    }

    private void setupUserInterface() {
        // Set welcome message
        tvWelcome.setText("Welcome, " + currentUser.getFullName() + "!");
        tvRoleInfo.setText(currentUser.getRole().toString() + " • " + currentUser.getDepartment());

        // Show/hide FAB based on user role
        if (authService.canManageNotices()) {
            fabAddNotice.show();
            fabAddNotice.setOnClickListener(v -> openAddNoticeActivity());
        } else {
            fabAddNotice.hide();
        }

        // Set toolbar title based on role
        if (getSupportActionBar() != null) {
            String title = "Smart Notice Board";
            if (authService.isAdmin()) {
                title += " - Admin";
            } else if (authService.isTeacher()) {
                title += " - Teacher";
            }
            getSupportActionBar().setTitle(title);
        }
    }

    private void setupRecyclerView() {
        noticeList = new ArrayList<>();
        noticeAdapter = new NoticeAdapter(this, noticeList, currentUser);
        recyclerNotices.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotices.setAdapter(noticeAdapter);
    }

    private void setupTabLayout() {
        // Add tabs based on user role
        tabLayout.addTab(tabLayout.newTab().setText("All Notices"));
        tabLayout.addTab(tabLayout.newTab().setText("Common"));
        tabLayout.addTab(tabLayout.newTab().setText("Department"));
        tabLayout.addTab(tabLayout.newTab().setText("Annual"));
        
        if (authService.isStudent() || authService.isTeacher()) {
            tabLayout.addTab(tabLayout.newTab().setText("Subject"));
        }
        
        if (authService.canManageNotices()) {
            tabLayout.addTab(tabLayout.newTab().setText("My Notices"));
        }
        
        if (authService.isAdmin()) {
            tabLayout.addTab(tabLayout.newTab().setText("Archived"));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterNoticesByTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadNotices() {
        List<Notice> allNotices = noticeDatabase.getNoticesForUser(currentUser);
        noticeList.clear();
        noticeList.addAll(allNotices);
        noticeAdapter.notifyDataSetChanged();
    }

    private void filterNoticesByTab(int position) {
        List<Notice> filteredNotices = new ArrayList<>();
        
        switch (position) {
            case 0: // All Notices
                filteredNotices = noticeDatabase.getNoticesForUser(currentUser);
                break;
            case 1: // Common
                filteredNotices = noticeDatabase.getNoticesByCategory(NoticeCategory.COMMON);
                break;
            case 2: // Department
                filteredNotices = noticeDatabase.getNoticesByCategory(NoticeCategory.DEPARTMENT);
                break;
            case 3: // Annual
                filteredNotices = noticeDatabase.getNoticesByCategory(NoticeCategory.ANNUAL);
                break;
            case 4: // Subject or My Notices (depends on role)
                if (authService.canManageNotices() && tabLayout.getTabAt(4).getText().equals("My Notices")) {
                    filteredNotices = noticeDatabase.getNoticesByCreator(currentUser.getUserId());
                } else {
                    filteredNotices = noticeDatabase.getNoticesByCategory(NoticeCategory.SUBJECT_SPECIFIC);
                }
                break;
            case 5: // Archived (admin only) or My Notices (teacher)
                if (authService.isAdmin() && tabLayout.getTabAt(5).getText().equals("Archived")) {
                    filteredNotices = noticeDatabase.getArchivedNotices();
                } else if (tabLayout.getTabAt(5).getText().equals("My Notices")) {
                    filteredNotices = noticeDatabase.getNoticesByCreator(currentUser.getUserId());
                }
                break;
        }
        
        noticeList.clear();
        noticeList.addAll(filteredNotices);
        noticeAdapter.notifyDataSetChanged();
    }

    private void openAddNoticeActivity() {
        Intent intent = new Intent(this, AddEditNoticeActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh notices after adding/editing
            loadNotices();
            Toast.makeText(this, "Notice updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        
        // Hide admin options if not admin
        MenuItem adminItem = menu.findItem(R.id.action_admin_panel);
        if (adminItem != null) {
            adminItem.setVisible(authService.isAdmin());
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_search) {
            // TODO: Implement search functionality
            Toast.makeText(this, "Search functionality coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_admin_panel) {
            if (authService.isAdmin()) {
                // TODO: Open admin panel
                Toast.makeText(this, "Admin panel coming soon!", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_profile) {
            // TODO: Open profile activity
            Toast.makeText(this, "Profile management coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    authService.logout();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notices when returning to dashboard
        loadNotices();
    }
}