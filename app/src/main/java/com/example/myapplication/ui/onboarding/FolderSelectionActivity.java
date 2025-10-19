package com.example.myapplication.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FolderSelectionActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private List<FolderItem> folders = new ArrayList<>();
    private static final int REQUEST_CODE = 200;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_selection);
        
        recyclerView = findViewById(R.id.folders_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new FolderAdapter();
        recyclerView.setAdapter(adapter);
        
        MaterialButton acceptButton = findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(v -> saveAndFinish());
        
        // Scan for common music folders
        scanForMusicFolders();
    }
    
    private void scanForMusicFolders() {
        // Get common folder paths
        folders.clear();
        
        // Add common Android music folders
        folders.add(new FolderItem("Documents", "/storage/emulated/0/Documents", true));
        folders.add(new FolderItem("Download", "/storage/emulated/0/Download", true));
        folders.add(new FolderItem("Music", "/storage/emulated/0/Music", true));
        folders.add(new FolderItem("MyAudioEditor", "/storage/emulated/0/MyAudioEditor", true));
        folders.add(new FolderItem("WhatsApp Audio", "/storage/emulated/0/WhatsApp/Media/WhatsApp Audio", true));
        folders.add(new FolderItem("files (online-audio-converter.com)", "/storage/emulated/0/files (online-audio-converter.com)", true));
        folders.add(new FolderItem("records", "/storage/emulated/0/records", true));
        
        adapter.notifyDataSetChanged();
    }
    
    private void saveAndFinish() {
        Set<String> selectedPaths = new HashSet<>();
        int count = 0;
        for (FolderItem item : folders) {
            if (item.isSelected) {
                selectedPaths.add(item.path);
                count++;
                android.util.Log.d("FolderSelection", "Selected folder: " + item.path);
            }
        }
        
        if (!selectedPaths.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().putStringSet("music_folder_paths", selectedPaths).apply();
            android.util.Log.d("FolderSelection", "Saved " + count + " folders to SharedPreferences");
            
            // Show progress and start scanning
            Intent resultIntent = new Intent();
            resultIntent.putExtra("folder_count", selectedPaths.size());
            setResult(RESULT_OK, resultIntent);
        } else {
            android.util.Log.w("FolderSelection", "No folders selected!");
        }
        
        finish();
    }
    
    class FolderItem {
        String name;
        String path;
        boolean isSelected;
        
        FolderItem(String name, String path, boolean selected) {
            this.name = name;
            this.path = path;
            this.isSelected = selected;
        }
    }
    
    class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_folder_selection, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FolderItem item = folders.get(position);
            holder.folderName.setText(item.name);
            holder.checkBox.setChecked(item.isSelected);
            
            holder.card.setOnClickListener(v -> {
                item.isSelected = !item.isSelected;
                holder.checkBox.setChecked(item.isSelected);
            });
            
            holder.checkBox.setOnClickListener(v -> {
                item.isSelected = holder.checkBox.isChecked();
            });
        }
        
        @Override
        public int getItemCount() {
            return folders.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            ImageView folderIcon;
            TextView folderName;
            CheckBox checkBox;
            
            ViewHolder(View view) {
                super(view);
                card = (MaterialCardView) view;
                folderIcon = view.findViewById(R.id.folder_icon);
                folderName = view.findViewById(R.id.folder_name);
                checkBox = view.findViewById(R.id.folder_checkbox);
            }
        }
    }
}

