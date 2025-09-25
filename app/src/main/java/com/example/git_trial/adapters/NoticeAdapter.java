package com.example.git_trial.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.git_trial.R;
import com.example.git_trial.model.Notice;
import com.example.git_trial.model.NoticeCategory;
import com.example.git_trial.model.User;
import com.example.git_trial.model.UserRole;
import com.example.git_trial.utils.DateUtils;
import java.util.List;

/**
 * Adapter for displaying notices in RecyclerView
 */
public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {
    
    private Context context;
    private List<Notice> notices;
    private User currentUser;
    private OnNoticeClickListener clickListener;
    
    public interface OnNoticeClickListener {
        void onNoticeClick(Notice notice, int position);
        void onNoticeMenuClick(Notice notice, int position, View anchorView);
    }
    
    public NoticeAdapter(Context context, List<Notice> notices, User currentUser) {
        this.context = context;
        this.notices = notices;
        this.currentUser = currentUser;
    }
    
    public void setOnNoticeClickListener(OnNoticeClickListener listener) {
        this.clickListener = listener;
    }
    
    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = notices.get(position);
        holder.bind(notice, position);
    }
    
    @Override
    public int getItemCount() {
        return notices.size();
    }
    
    public class NoticeViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvCategory, tvDate, tvTitle, tvDescription, tvAuthor, tvSubjectDept, tvAttachmentCount;
        private View viewPriority;
        private ImageView ivMenu;
        private LinearLayout layoutAttachments;
        
        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvSubjectDept = itemView.findViewById(R.id.tvSubjectDept);
            tvAttachmentCount = itemView.findViewById(R.id.tvAttachmentCount);
            viewPriority = itemView.findViewById(R.id.viewPriority);
            ivMenu = itemView.findViewById(R.id.ivMenu);
            layoutAttachments = itemView.findViewById(R.id.layoutAttachments);
        }
        
        public void bind(Notice notice, int position) {
            // Set title and description
            tvTitle.setText(notice.getTitle());
            tvDescription.setText(notice.getDescription());
            
            // Set category with appropriate color
            tvCategory.setText(notice.getCategory().getDisplayName().toUpperCase());
            setCategoryColor(notice.getCategory());
            
            // Set date
            tvDate.setText(DateUtils.getTimeAgo(notice.getCreatedAt()));
            
            // Set author
            tvAuthor.setText("Created by " + notice.getCreatedByName());
            
            // Set priority indicator
            setPriorityColor(notice.getPriority());
            
            // Set subject/department info
            setSubjectDeptInfo(notice);
            
            // Set attachments info
            setAttachmentsInfo(notice);
            
            // Show menu for notice creators and admins
            showMenuIfAllowed(notice);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNoticeClick(notice, position);
                }
            });
            
            ivMenu.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNoticeMenuClick(notice, position, v);
                }
            });
        }
        
        private void setCategoryColor(NoticeCategory category) {
            int colorRes;
            switch (category) {
                case COMMON:
                    colorRes = R.color.common_notices;
                    break;
                case DEPARTMENT:
                    colorRes = R.color.dept_notices;
                    break;
                case ANNUAL:
                    colorRes = R.color.annual_notices;
                    break;
                case SUBJECT_SPECIFIC:
                    colorRes = R.color.subject_notices;
                    break;
                default:
                    colorRes = R.color.primary_blue;
            }
            
            int color = ContextCompat.getColor(context, colorRes);
            tvCategory.setBackgroundTintList(ColorStateList.valueOf(color));
        }
        
        private void setPriorityColor(int priority) {
            int colorRes;
            if (priority >= 5) {
                colorRes = R.color.error_red; // High priority
            } else if (priority >= 4) {
                colorRes = R.color.warning_orange; // Medium-high priority
            } else if (priority >= 3) {
                colorRes = R.color.success_green; // Medium priority
            } else {
                colorRes = R.color.text_secondary; // Low priority
            }
            
            int color = ContextCompat.getColor(context, colorRes);
            viewPriority.setBackgroundTintList(ColorStateList.valueOf(color));
        }
        
        private void setSubjectDeptInfo(Notice notice) {
            String info = "";
            if (notice.getCategory() == NoticeCategory.SUBJECT_SPECIFIC && notice.getSubject() != null) {
                info = notice.getSubject();
            } else if (notice.getDepartment() != null && !notice.getDepartment().equals("All")) {
                info = notice.getDepartment();
            } else {
                info = "All Departments";
            }
            tvSubjectDept.setText(info);
        }
        
        private void setAttachmentsInfo(Notice notice) {
            if (notice.getAttachments() != null && !notice.getAttachments().isEmpty()) {
                layoutAttachments.setVisibility(View.VISIBLE);
                int count = notice.getAttachments().size();
                tvAttachmentCount.setText(count + (count == 1 ? " attachment" : " attachments"));
            } else {
                layoutAttachments.setVisibility(View.GONE);
            }
        }
        
        private void showMenuIfAllowed(Notice notice) {
            // Show menu if user is admin or the creator of the notice
            boolean canManage = currentUser.getRole() == UserRole.ADMIN || 
                               notice.getCreatedBy().equals(currentUser.getUserId());
            ivMenu.setVisibility(canManage ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * Update the notice list
     */
    public void updateNotices(List<Notice> newNotices) {
        this.notices.clear();
        this.notices.addAll(newNotices);
        notifyDataSetChanged();
    }
    
    /**
     * Remove a notice from the list
     */
    public void removeNotice(int position) {
        if (position >= 0 && position < notices.size()) {
            notices.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notices.size());
        }
    }
    
    /**
     * Update a specific notice
     */
    public void updateNotice(Notice updatedNotice, int position) {
        if (position >= 0 && position < notices.size()) {
            notices.set(position, updatedNotice);
            notifyItemChanged(position);
        }
    }
}