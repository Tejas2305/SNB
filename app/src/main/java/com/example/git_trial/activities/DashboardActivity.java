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

import com.google.android.material.appbar.MaterialToolbar;
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

public class DashboardActivity extends AppCompatActivity implements NoticeAdapter.OnNoticeClickListener {

    private TextView tvWelcome, tvRoleInfo;
    private TabLayout tabLayout;
    private RecyclerView recyclerNotices;
    private FloatingActionButton fabAddNotice;
    private MaterialToolbar toolbar;
    
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
        setupToolbar();
        setupUserInterface();
        setupRecyclerView();
        setupTabLayout();
        loadNotices();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRoleInfo = findViewById(R.id.tvRoleInfo);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerNotices = findViewById(R.id.recyclerNotices);
        fabAddNotice = findViewById(R.id.fabAddNotice);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
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
    }

    private void setupRecyclerView() {
        noticeList = new ArrayList<>();
        noticeAdapter = new NoticeAdapter(this, noticeList, currentUser);
        noticeAdapter.setOnNoticeClickListener(this);
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
        
        noticeAdapter.updateNotices(filteredNotices);
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
    public void onNoticeClick(Notice notice, int position) {
        // TODO: Open notice details activity
        Toast.makeText(this, "Notice clicked: " + notice.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNoticeMenuClick(Notice notice, int position, android.view.View anchorView) {
        showNoticeMenu(notice, position, anchorView);
    }

    private void showNoticeMenu(Notice notice, int position, android.view.View anchorView) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.notice_item_menu, popup.getMenu());
        
        // Show/hide menu items based on permissions
        android.view.Menu menu = popup.getMenu();
        boolean canEdit = currentUser.getUserId().equals(notice.getCreatedBy()) || authService.isAdmin();
        menu.findItem(R.id.action_edit).setVisible(canEdit);
        menu.findItem(R.id.action_delete).setVisible(canEdit);
        menu.findItem(R.id.action_archive).setVisible(authService.isAdmin());
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_view) {
                viewNoticeDetails(notice);
                return true;
            } else if (id == R.id.action_edit) {
                editNotice(notice);
                return true;
            } else if (id == R.id.action_delete) {
                deleteNotice(notice, position);
                return true;
            } else if (id == R.id.action_archive) {
                archiveNotice(notice, position);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void viewNoticeDetails(Notice notice) {
        // Create a dialog to show full notice details
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_notice_details, null);
        
        // Populate dialog views
        android.widget.TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        android.widget.TextView tvDescription = dialogView.findViewById(R.id.tvDialogDescription);
        android.widget.TextView tvCategory = dialogView.findViewById(R.id.tvDialogCategory);
        android.widget.TextView tvAuthor = dialogView.findViewById(R.id.tvDialogAuthor);
        android.widget.TextView tvDate = dialogView.findViewById(R.id.tvDialogDate);
        android.widget.TextView tvDepartment = dialogView.findViewById(R.id.tvDialogDepartment);
        android.widget.TextView tvSubject = dialogView.findViewById(R.id.tvDialogSubject);
        
        tvTitle.setText(notice.getTitle());
        tvDescription.setText(notice.getDescription());
        tvCategory.setText(notice.getCategory().getDisplayName());
        tvAuthor.setText(notice.getCreatedByName());
        tvDate.setText(com.example.git_trial.utils.DateUtils.formatDateTime(notice.getCreatedAt()));
        tvDepartment.setText(notice.getDepartment() != null ? notice.getDepartment() : "All");
        
        if (notice.getSubject() != null && !notice.getSubject().isEmpty()) {
            tvSubject.setText(notice.getSubject());
            tvSubject.setVisibility(android.view.View.VISIBLE);
        } else {
            tvSubject.setVisibility(android.view.View.GONE);
        }
        
        builder.setView(dialogView)
               .setPositiveButton("Close", null)
               .show();
    }

    private void editNotice(Notice notice) {
        Intent intent = new Intent(this, AddEditNoticeActivity.class);
        intent.putExtra(AddEditNoticeActivity.EXTRA_NOTICE, notice);
        intent.putExtra(AddEditNoticeActivity.EXTRA_EDIT_MODE, true);
        startActivityForResult(intent, 100);
    }

    private void deleteNotice(Notice notice, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Notice")
                .setMessage("Are you sure you want to delete \"" + notice.getTitle() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = noticeDatabase.deleteNotice(notice.getNoticeId());
                    if (success) {
                        noticeAdapter.removeNotice(position);
                        Toast.makeText(this, "Notice deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete notice", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void archiveNotice(Notice notice, int position) {
        boolean newArchiveState = !notice.isArchived();
        String action = newArchiveState ? "archive" : "unarchive";
        
        new AlertDialog.Builder(this)
                .setTitle((newArchiveState ? "Archive" : "Unarchive") + " Notice")
                .setMessage("Are you sure you want to " + action + " \"" + notice.getTitle() + "\"?")
                .setPositiveButton(newArchiveState ? "Archive" : "Unarchive", (dialog, which) -> {
                    boolean success = noticeDatabase.archiveNotice(notice.getNoticeId(), newArchiveState);
                    if (success) {
                        notice.setArchived(newArchiveState);
                        noticeAdapter.updateNotice(notice, position);
                        Toast.makeText(this, "Notice " + action + "d successfully", Toast.LENGTH_SHORT).show();
                        
                        // Refresh the list to reflect changes
                        loadNotices();
                    } else {
                        Toast.makeText(this, "Failed to " + action + " notice", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
            showSearchDialog();
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

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_search, null);
        
        com.google.android.material.textfield.TextInputEditText etSearch = dialogView.findViewById(R.id.etSearch);
        
        AlertDialog dialog = builder.setTitle("Search Notices")
                .setView(dialogView)
                .setPositiveButton("Search", null)
                .setNegativeButton("Cancel", null)
                .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                    dialog.dismiss();
                } else {
                    etSearch.setError("Please enter a search term");
                }
            });
        });
        
        dialog.show();
    }

    private void performSearch(String query) {
        List<Notice> searchResults = noticeDatabase.searchNotices(query, currentUser);
        noticeAdapter.updateNotices(searchResults);
        
        // Update tab selection to show we're in search mode
        tabLayout.clearOnTabSelectedListeners();
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setText(tab.getText() + (i == 0 ? " (Search: \"" + query + "\")" : ""));
            }
        }
        
        // Re-add the listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Reset search when switching tabs
                resetTabTitles();
                filterNoticesByTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        Toast.makeText(this, "Found " + searchResults.size() + " results for \"" + query + "\"", Toast.LENGTH_SHORT).show();
    }

    private void resetTabTitles() {
        tabLayout.clearOnTabSelectedListeners();
        
        // Reset all tab titles
        String[] originalTitles = {"All Notices", "Common", "Department", "Annual"};
        for (int i = 0; i < Math.min(originalTitles.length, tabLayout.getTabCount()); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setText(originalTitles[i]);
            }
        }
        
        // Handle dynamic tabs based on user role
        int nextIndex = originalTitles.length;
        if (authService.isStudent() || authService.isTeacher()) {
            TabLayout.Tab subjectTab = tabLayout.getTabAt(nextIndex);
            if (subjectTab != null) {
                subjectTab.setText("Subject");
                nextIndex++;
            }
        }
        
        if (authService.canManageNotices()) {
            TabLayout.Tab myNoticesTab = tabLayout.getTabAt(nextIndex);
            if (myNoticesTab != null) {
                myNoticesTab.setText("My Notices");
                nextIndex++;
            }
        }
        
        if (authService.isAdmin()) {
            TabLayout.Tab archivedTab = tabLayout.getTabAt(nextIndex);
            if (archivedTab != null) {
                archivedTab.setText("Archived");
            }
        }
        
        // Re-add the listener
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
}