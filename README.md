# 📝 El Apunte - Simple Note-Taking App (Java)

![App Screenshot](/screenshots/empty_note.jpeg)  
*A lightweight, fast, and beautiful note-taking app for Android, built with Java.*

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.java.com)  
[![Android](https://img.shields.io/badge/Android-API_24+-green.svg)](https://developer.android.com)  
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](https://opensource.org/licenses/MIT)

---

## ✨ Features
- 📌 **Create, edit, and delete** notes effortlessly.
- 🔍 **Search & filter** notes by title or content.
- 🌙 **Dark/Light theme** with system-wide support.
- 🎨 **Material Design 3** with smooth animations.
- 📅 **Timestamps** for tracking note history.

---

## 🛠 Tech Stack
- **Language:** Java
- **Database:** Room (SQLite)
- **Architecture:** MVVM
- **UI:** XML Views + Data Binding
- **Async:** `LiveData`, `ExecutorService`

---

## 📸 Screenshots
| Notes List                                                | Edit Note                                           | Dark Mode                                                |  
|-----------------------------------------------------------|-----------------------------------------------------|----------------------------------------------------------|  
| <img src="/screenshots/note_list_light.jpeg" width="200"> | <img src="/screenshots/edit_note.jpeg" width="200"> | <img src="/screenshots/note_list_dark.jpeg" width="200"> |  


---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Flamingo or newer)
- Android SDK 33+

### Installation
1. **Clone the repository**
   ```bash
   git clone https://github.com/kevin2-cyber/el-apunte.git
   
2. **Open in Android Studio**
   File → Open → Select the cloned directory.
3. **Run the App**
   Click the ▶️ button or Shift + F10(Windows).

## 🏗 Project Structure

```📂 el-apunte/
├── 📂 app/
│   ├── 📂 src/main/
│   │   ├── 📂 java/com/kimikevin/el_apunte/
│   │   │   ├── 📁 model/      # Data layer (Entity, DAO, Repository)
│   │   │   ├── 📁 view/       # Activities & UI
│   │   │   ├── 📁 viewmodel/  # Business logic
│   │   │   └── 📁 util/       # Utilities (DiffUtil, TimeAgo)
│   │   └── 📂 res/            # Layouts, strings, drawables
├── 📂 gradle/
└── build.gradle
```

## 🧩 Key Code Components
### 1. Note Entity (`Note.java`)  
```java
@Entity(tableName = "note_table")
public class Note extends BaseObservable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String content;
    private String formattedDate;

    // Getters and Setters with @Bindable
    @Bindable
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }
    // ... other getters/setters
}
```
### 2. Room Database Access (`NoteDao.java`)
```java
@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);
    
    @Update
    void update(Note note);
    
    @Delete
    void delete(Note note);
    
    @Query("SELECT * FROM note_table ORDER BY formattedDate DESC")
    LiveData<List<Note>> getAllNotes();
}
```
### 3. ViewModel (`NoteViewModel.java`)
```java
public class NoteViewModel extends AndroidViewModel {
    private NoteRepository repository;
    private LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public void insert(Note note) {
        repository.insertNote(note);
    }
    
    // Other CRUD operations...
}
```
### 4. Repository (`NoteRepository.java`)
```java
public class NoteRepository {
    private NoteDao noteDao;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public NoteRepository(Application application) {
        NoteDatabase db = NoteDatabase.getInstance(application);
        noteDao = db.getNoteDao();
    }

    public void insertNote(Note note) {
        executor.execute(() -> noteDao.insert(note));
    }
}
```

### 5. RecyclerView Adapter (`NoteAdapter.java`)
```java
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private OnItemClickListener listener;

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private final NoteItemBinding binding;
        
        public NoteViewHolder(NoteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onNoteClick(notes.get(position));
                }
            });
        }
        
        public void bind(Note note) {
            binding.setNote(note);
            binding.executePendingBindings();
        }
    }

    // onCreateViewHolder and onBindViewHolder implementations...
}
```

### 6. Edit Activity Handler (`EditActivity.java`)
```java
public class EditClickHandler {
    private final Context context;

    public void onSubmitButtonClicked(View view) {
        String title = note.getTitle() != null ? note.getTitle().trim() : "";
        String content = note.getContent() != null ? note.getContent().trim() : "";
        
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(context, "Note cannot be empty", Toast.LENGTH_SHORT).show();
        }
        
        // Save logic...
    }
}
```
## 🤝 Contributing
#### Pull requests are welcome! For major changes, open an issue first.
1. Fork the project.
2. Create a branch (`git checkout -b feature/awesome-feature`)
3. Commit changes (`git commit -m 'Add awesome feature'`)
4. Push to the branch (`git push origin feature/awesome-feature`)
5. Open a PR.

## 📄 License
### MIT License. See LICENSE for details.