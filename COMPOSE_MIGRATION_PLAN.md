# Jetpack Compose Migration Plan

## 🎯 Goal
Migrate entire music player app from XML/Java to Jetpack Compose/Kotlin with Material 3 Expressive design, implementing ALL features from requirements.md.

## ✅ Completed

### Phase 1: Setup
- [x] Add Jetpack Compose dependencies
- [x] Add Kotlin plugin
- [x] Enable Compose build features
- [x] Add Material 3 Expressive color palette
- [x] Fix multi-folder selection bug (Java implementation)
- [x] Add scanning progress system

## 📋 Remaining Tasks (Current Context Window)

### Phase 2: Foundation (Priority: Critical)
1. **Create Material 3 Theme System**
   - Typography (Expressive scale)
   - Shapes (Large corner radii)
   - Theme.kt with dynamic colors support
   
2. **Create Compose MainActivity**
   - Replace existing Java MainActivity
   - Bottom navigation with Compose
   - Navigation graph setup
   
3. **Core UI Components**
   - ExpressiveCard composable
   - ExpressiveButton composable
   - AnimatedBottomBar composable
   - MiniPlayer composable (persistent)

### Phase 3: Main Screens (Next Context Window)
4. **Home Screen** (Compose)
   - Collapsing toolbar
   - Daily Mixes horizontal carousel
   - Recently Played section
   - Quick Actions chips
   
5. **Search Screen** (Compose)
   - Search bar with filters
   - Real-time results
   - Categorized results (Songs/Albums/Artists/etc.)
   
6. **Library Screen** (Compose)
   - Tab layout (Songs/Albums/Artists/Playlists/Liked)
   - List/Grid toggle
   - Fast scroll
   
7. **Now Playing Screen** (Compose)
   - Full-screen immersive UI
   - Album art with animations
   - Playback controls
   - Swipe gestures
   
8. **Mini Player** (Compose)
   - Bottom-docked player
   - Swipe to expand
   - Progress indicator

### Phase 4: Detail Screens
9. **Album Detail** (Compose)
10. **Artist Detail** (Compose)
11. **Playlist Detail** (Compose)
12. **Queue Management** (Bottom Sheet)
13. **Lyrics Screen** (Auto-scroll)
14. **Settings Screen** (Compose)

### Phase 5: Advanced Features
15. **Folder Selection** (Compose with multi-select)
16. **Dynamic Theming** (Album art adaptive colors)
17. **Animations**
    - Shared element transitions
    - Choreographed motion
    - Material motion system
18. **Gestures**
    - Swipe to change track
    - Pull to refresh
    - Drag to reorder

## 🎨 Material 3 Expressive Design Principles

### Typography Scale
```kotlin
HeadlineLarge: 32sp, bold
HeadlineMedium: 28sp, semibold  
TitleLarge: 22sp, medium
BodyLarge: 16sp, regular
BodyMedium: 14sp, regular
LabelSmall: 11sp, medium
```

### Shape System
```kotlin
Small: 12dp corner radius
Medium: 16dp corner radius
Large: 20dp corner radius
ExtraLarge: 28dp corner radius
```

### Elevation & Shadows
```kotlin
Level0: 0dp (flush)
Level1: 1dp (subtle)
Level2: 3dp (raised)
Level3: 6dp (elevated)
Level4: 8dp (prominent)
Level5: 12dp (floating)
```

### Animations
- Duration: 300-500ms
- Easing: EaseInOutCubic
- Choreography: Stagger by 50ms
- Spring animations for interactive elements

## 🔄 Migration Strategy

### Hybrid Approach (Recommended)
1. Keep existing Java/XML code running
2. Create new Compose screens alongside
3. Add ComposeBridge for gradual migration
4. Use ComposeView to embed Compose in XML
5. Eventually replace MainActivity with Compose version

### File Structure
```
app/src/main/java/com/example/myapplication/
├── ui/
│   ├── theme/          # Compose theme system
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   ├── Shape.kt
│   │   └── Theme.kt
│   ├── screens/        # Compose screens
│   │   ├── home/
│   │   ├── search/
│   │   ├── library/
│   │   ├── nowplaying/
│   │   ├── settings/
│   │   └── ...
│   ├── components/     # Reusable composables
│   │   ├── ExpressiveCard.kt
│   │   ├── ExpressiveButton.kt
│   │   ├── MiniPlayer.kt
│   │   └── ...
│   └── navigation/     # Navigation setup
│       └── NavGraph.kt
├── data/               # Existing (keep)
├── media/              # Existing (keep)
└── MainActivity.kt     # NEW Compose version
```

## 🎯 Success Criteria

- [ ] All screens implemented in Compose
- [ ] Material 3 Expressive design applied throughout
- [ ] All animations smooth (60fps)
- [ ] Dynamic theming from album art working
- [ ] Folder selection with multi-select working
- [ ] All requirements.md features implemented
- [ ] No XML layouts for UI (except legacy components)
- [ ] Proper state management (ViewModel + StateFlow)
- [ ] Smooth transitions between screens

## ⚠️ Important Notes

### This is a MASSIVE undertaking
- **Estimated time**: 20-30 hours of development
- **Context windows needed**: 4-6 windows
- **Files to create/modify**: 50+ files
- **Learning curve**: Compose is different from XML

### What User Needs to Know
1. **App will be broken during migration** - Gradle sync will fail until all Kotlin files are created
2. **Test incrementally** - Test each screen as it's built
3. **Backup your work** - Use Git to commit frequently
4. **Be patient** - This is a full rewrite, not a quick fix

## 🚀 Current Status

**Context Window 1** (This window):
- ✅ Dependencies added
- ✅ Kotlin enabled
- ✅ Compose enabled
- ✅ Color palette created
- 🔄 Next: Create Type.kt, Shape.kt, Theme.kt

**You are here** ⬇️

The migration will continue across multiple context windows. Each window will complete 2-3 major screens or components.

## 📝 Testing Strategy

### Manual Testing Checklist
- [ ] Build succeeds
- [ ] App launches
- [ ] Navigation works
- [ ] Each screen displays correctly
- [ ] Animations are smooth
- [ ] Music playback works
- [ ] Folder selection works
- [ ] Scanning progress shows correctly
- [ ] Theme switching works
- [ ] Dynamic colors work

### Performance Testing
- [ ] Smooth 60fps scrolling in lists
- [ ] No jank during screen transitions
- [ ] Album art loading doesn't block UI
- [ ] Metadata scanning in background doesn't freeze UI

## 💡 Recommendations

### For This Context Window
1. Create core theme system (Type, Shape, Theme)
2. Create 2-3 reusable composables (Card, Button, TopBar)
3. Create minimal MainActivity with bottom nav
4. Test that it builds and runs

### For Next Context Windows
- Window 2: Home + Search screens
- Window 3: Library + Now Playing screens
- Window 4: Detail screens + Queue
- Window 5: Settings + Folder selection
- Window 6: Animations + polish

## 🎵 Let's Build an Amazing Music Player! 🚀




