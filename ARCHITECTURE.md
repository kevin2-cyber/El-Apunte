# El Apunte — Project Structure

A lightweight, offline-first note-taking app for Android built in Java.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Database | Room (SQLite) |
| Architecture | MVVM |
| UI | XML Views + Data Binding |
| Async | `LiveData`, `ExecutorService` |
| DI | Hilt |
| Navigation | Jetpack Navigation Component |

---

## Architecture Overview

The app follows **MVVM** (Model–View–ViewModel) with a Repository pattern. All data is stored locally on-device via Room — there is no backend connection.

```
View (Fragments)
    │  observes LiveData
    ▼
ViewModel
    │  calls
    ▼
Repository
    │  queries
    ▼
Room DAO → SQLite Database
```

---

## Directory Layout

```
app/src/main/
├── java/com/kimikevin/elapunte/
│   ├── ElApunteApplication.java   # @HiltAndroidApp entry point
│   ├── MainActivity.java          # Single activity host
│   ├── di/
│   │   └── AppModule.java         # Hilt DI: Room database + DAO providers
│   ├── model/
│   │   ├── NoteDatabase.java      # Room database definition (v10)
│   │   ├── dao/
│   │   │   └── NoteDao.java       # CRUD queries
│   │   ├── entity/
│   │   │   └── Note.java          # Room entity + data-binding observable
│   │   └── repository/
│   │       └── NoteRepository.java # Single source of truth for notes
│   ├── view/
│   │   ├── NoteListFragment.java  # Main notes list screen
│   │   ├── EditNoteFragment.java  # Create / edit a note
│   │   ├── ThemeBottomSheet.java  # Light / dark / system theme picker
│   │   └── adapter/
│   │       └── NoteAdapter.java   # RecyclerView adapter with DiffUtil
│   ├── viewmodel/
│   │   ├── NoteViewModel.java     # Note CRUD state, exposes LiveData
│   │   └── SplashViewModel.java   # Splash screen loading flag
│   └── util/
│       ├── AppConstants.java      # Shared string keys and log tags
│       ├── Converters.java        # Room type converter: UUID ↔ String
│       └── TimeAgoUtil.java       # Relative timestamp formatter
└── res/
    ├── layout/
    │   ├── activity_main.xml           # NavHostFragment container
    │   ├── fragment_note_list.xml      # Notes list UI
    │   ├── fragment_edit_note.xml      # Edit/create note UI
    │   ├── note_item.xml               # RecyclerView row
    │   └── theme_bottom_sheet_layout.xml
    ├── navigation/
    │   └── nav_graph.xml          # startDestination: noteListFragment
    └── values/ …                  # Strings, colors, themes, dark mode
```

---

## Key Components

### Note Entity (`Note.java`)

```java
@Entity(tableName = "note_table")
public class Note extends BaseObservable {
    @PrimaryKey
    private UUID id;          // auto-generated on construction

    private String title;
    private String content;
    private String formattedDate;
    private long   timestamp;

    @Bindable public String getTitle()   { return title; }
    @Bindable public String getContent() { return content; }
    @Bindable public String getDisplayDate() {
        return TimeAgoUtil.formatChatTimestamp(timestamp);
    }
}
```

### DAO (`NoteDao.java`)

```java
@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Update void update(Note note);
    @Delete void delete(Note note);

    @Query("SELECT * FROM note_table ORDER BY timestamp DESC")
    LiveData<List<Note>> getAllNotes();
}
```

### Repository (`NoteRepository.java`)

```java
public class NoteRepository {
    private final NoteDao noteDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public NoteRepository(NoteDao noteDao) { this.noteDao = noteDao; }

    public LiveData<List<Note>> getAllNotes()   { return noteDao.getAllNotes(); }

    public void insertNote(Note note) { executor.execute(() -> { /* set timestamp, insert */ }); }
    public void updateNote(Note note) { executor.execute(() -> { /* set timestamp, upsert */ }); }
    public void deleteNote(Note note) { executor.execute(() -> noteDao.delete(note)); }
}
```

### ViewModel (`NoteViewModel.java`)

```java
@HiltViewModel
public class NoteViewModel extends ViewModel {
    private final LiveData<List<Note>> allNotes;

    @Inject
    public NoteViewModel(NoteRepository repository) {
        allNotes = repository.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() { return allNotes; }
    public void insertNote(Note note) { … }
    public void updateNote(Note note) { … }
    public void deleteNote(Note note) { … }
}
```

---

## Database

Starting at version **1** with a clean schema. `fallbackToDestructiveMigration()` is set so any stale data from older installs is wiped on upgrade. Future schema changes should bump the version number and add an explicit `Migration` to `AppModule`.

---

## Navigation

```
noteListFragment  ──(FAB / tap note)──▶  editNoteFragment
```

`startDestination` is `noteListFragment`. No login or registration screens.

---

## Features

- Create, edit, and delete notes
- Search and filter notes by title or content
- Sort order toggle (newest first / oldest first)
- Dark / Light / System theme via bottom sheet
- Splash screen with Jetpack SplashScreen API
- Edge-to-edge layout
