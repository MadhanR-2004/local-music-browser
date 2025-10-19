🎯 Project Overview
Create a fully local, offline-first Android music player application using Java, Material 3 Expressive Design, and ExoPlayer. The app should feel premium, responsive, and visually engaging while maintaining excellent performance for large music libraries (10,000+ songs).

🎨 Visual Design Language - Material 3 Expressive
Design Principles

Bold & Dynamic: Use large, expressive typography with variable font weights
Spatial Depth: Implement layered surfaces with elevation, shadows, and blur effects
Fluid Motion: Smooth, choreographed animations with easing curves
Rich Surfaces: Gradient overlays, frosted glass effects (blur), and dynamic color extraction from album art
Generous Spacing: Breathable layouts with ample whitespace
Rounded Corners: Use large corner radius (16-24dp) for cards and surfaces

Color System

Dynamic Theming: Extract colors from currently playing album art to create immersive experiences
System Colors (Android 12+): Leverage Material You dynamic color palette
Custom Themes: Allow users to override with custom primary, secondary, and tertiary colors
Color Roles:

Primary: Main brand color, FABs, active states
Secondary: Chips, less prominent buttons
Tertiary: Accents, special highlights
Surface variants: Different elevation levels
Containers: Background for cards, chips


Gradient Overlays: Use subtle gradients on hero sections, Now Playing background

Typography

Headline Large: 32sp - Screen titles, Now Playing song title
Headline Medium: 28sp - Section headers
Title Large: 22sp - Prominent list items
Body Large: 16sp - Song titles in lists
Body Medium: 14sp - Artist names, metadata
Label Small: 11sp - Timestamps, extra info

Component Styling

Cards: Elevated with 8dp elevation, 16dp corner radius, subtle shadows
Buttons:

FAB: Large (56dp) with icon, 16dp corner radius
Extended FAB: Pill-shaped, icon + text
Text buttons: No background, primary color text
Filled tonal buttons: Surface container background


Bottom Sheets: Full-screen or modal with drag handle, rounded top corners (28dp)
Dialogs: Centered or full-screen with smooth entrance animations
Lists: Use Material 3 list items with leading icons/images, generous padding (16-20dp vertical)


📱 Screen Structure & Navigation
Navigation Architecture
Bottom Navigation Bar (persistent across app):

Home 🏠 - Daily mixes, recommendations, quick access
Search 🔍 - Universal search interface
Library 📚 - Tabbed music collection
Settings ⚙️ - App configuration (optional 4th tab or access via menu)

App Flow
Launch → First Run Check
├── New User → Folder Selection Onboarding → Scanning Progress → Home
└── Returning User → Home (with cached library)

Bottom Nav Navigation:
Home ↔ Search ↔ Library ↔ Settings

From Any Screen:
├── Tap song → Mini Player expands
├── Mini Player tap → Now Playing (full screen)
├── Song long-press → Context Menu (Add to playlist, favorite, share, etc.)
└── Album/Artist tap → Detail Screen → Song list

🏠 Home Screen Design
Purpose
Personalized discovery hub with algorithmically generated mixes and quick access to recent activity.
Layout Components
1. App Bar (Collapsing Toolbar)

Height: 200dp when expanded, 64dp collapsed
Background: Gradient overlay (primary → transparent) or blur effect
Content:

App logo/name (large, bold)
Profile icon (top-right) - future expansion
Notification bell (optional)


Scroll Behavior: Collapses smoothly as user scrolls down

2. Daily Mixes Section

Title: "Daily Mixes" - Headline Medium style
Layout: Horizontal scrolling carousel
Cards:

Size: 180dp × 220dp
Design:

Top: Collage of 4 album arts (2×2 grid) or single dominant album art
Gradient overlay from bottom (primary color, 60% opacity)
Text overlay: "Mix 1", "Rock Mix", "Chill Vibes" (white, bold)
Subtitle: "Based on Artist X, Y, Z" (white, 70% opacity)


Corner radius: 20dp
Elevation: 4dp, increases to 8dp on press
Tap action: Navigate to Mix detail screen → Play mix


Mix Generation Logic:

Mix 1-3: Generated from most played genres/artists
Discover Mix: New/rarely played songs
Recently Played Mix: Songs from last 7 days
Time-based: Morning Motivation, Evening Wind Down



3. Recently Played

Title: "Recently Played"
Layout: Horizontal scrolling list
Items:

Album art thumbnail (120dp square, rounded corners 12dp)
Song title below (Body Medium, 2 lines max, ellipsis)
Artist name (Body Small, secondary color)
Play count badge (optional)


Show: Last 20 played songs, chronologically

4. Quick Actions Section

Title: "Quick Access"
Layout: 2-column grid or horizontal chips
Buttons:

Shuffle All 🎲: Large chip/button, primary color
Liked Songs ❤️: Navigate to favorites
Playlists 📝: Quick access to playlist screen
Sleep Timer ⏰: Open sleep timer dialog



5. Discover Section (Optional)

Title: "You Might Like"
Content: Songs algorithmically picked from:

Similar genre to favorites
Same artist as top played
Similar BPM/mood


Layout: Vertical list (5-10 songs) with album art, title, artist

Interaction Patterns

Pull to Refresh: Re-scan library for new songs, regenerate mixes
Scroll Performance: Lazy loading, image caching for smooth scrolling
Empty State: First-time users see onboarding prompt to select folder


🔍 Search Screen Design
Purpose
Universal search hub with instant results across all media types and metadata fields.
Layout Components
1. Search Bar

Position: Fixed at top (always visible)
Design:

Large rounded search field (56dp height, 28dp corner radius)
Leading icon: Search magnifying glass
Placeholder text: "Search songs, artists, albums, genres..."
Trailing icon: Voice search (optional), Clear text (when typing)
Background: Surface variant color, elevated 2dp


Behavior:

Auto-focus on screen load
Real-time search (debounced 300ms after last keystroke)
Show search history chips below when empty



2. Search Filters (Chips Row)

Position: Below search bar, horizontal scrolling
Chips:

All (default selected)
Songs 🎵
Albums 💿
Artists 👤
Playlists 📝
Genres 🎸


Design: Filter chips (outlined when unselected, filled when selected)
Behavior: Tap to filter results instantly

3. Search History (Empty State)

Show When: Search bar is focused but empty
Content:

Recent searches (up to 10)
Each item: Text + Close icon to remove
"Clear History" button at bottom


Design: Simple list items with leading search icon

4. Search Results

Layout: Sectioned vertical scroll list
Sections (show only non-empty):
a. Top Result (Most relevant single item)

Large card (edge-to-edge)
Album art (left, 80dp)
Title, Artist, Album info (right)
Play button (FAB mini, overlay on album art)

b. Songs Section

Header: "Songs" + count badge
List items: Album art (56dp), Title, Artist, Duration
Show top 5, "See All" button if more

c. Albums Section

Header: "Albums" + count
Grid layout: 2 columns, album art + album name
Show top 4, "See All" button

d. Artists Section

Header: "Artists" + count
Circular avatars (80dp) with artist name below
Horizontal scroll or grid

e. Playlists Section

Header: "Playlists" + count
Playlist covers + names

f. Genres Section

Header: "Genres" + count
Colorful genre chips/cards



5. No Results State

Illustration: Empty state graphic (musical notes, broken record)
Message: "No results found for '[query]'"
Suggestions:

Check spelling
Try different keywords
Browse library instead (button)



Search Algorithm

Fields Searched:

Song title (highest weight)
Artist name
Album name
Genre
Filename (fallback)


Fuzzy Matching: Allow minor typos, partial matches
Ranking: Prioritize exact matches, then partial, then fuzzy
Performance: Index all searchable text in Room database with FTS (Full-Text Search) tables


📚 Library Screen Design
Purpose
Complete music collection organized by type with tabbed navigation.
Layout Components
1. App Bar

Title: "Library" (Headline Medium)
Actions:

Sort menu (top-right): Alphabetical, Recently Added, Most Played
Grid/List view toggle (for Songs/Albums)


Background: Surface color
Elevation: 0dp (flat), elevates on scroll

2. Tab Layout

Position: Below app bar, fixed (scrolls with tabs, not content)
Tabs:

Songs 🎵
Albums 💿
Artists 👤
Playlists 📝
Liked ❤️


Design:

Material 3 scrollable tabs
Active tab: Primary color indicator (3dp thick, full width of text)
Inactive tabs: On-surface color (60% opacity)
Smooth indicator animation when switching


Behavior: Swipe between tabs using ViewPager2

3. Tab Content: Songs

Layout Options:

List View (default): Vertical list

Album art thumbnail (56dp, rounded 8dp)
Song title (Body Large, 1 line, ellipsis)
Artist · Album (Body Small, secondary color, 1 line)
Duration (trailing, Body Small)
More menu icon (vertical 3 dots)


Grid View: 2-column grid showing larger album art with title below


Sections:

Fast scroll alphabetical index (right side)
Section headers (letters, dates) if sorted by alphabet/date


Actions:

Tap song: Play immediately + add to queue
Long press: Multi-select mode (checkboxes appear)
More menu: Add to playlist, Favorite, Share, Song info, Go to artist/album



4. Tab Content: Albums

Layout: 2-column grid (phones) or 3-column (tablets)
Cards:

Album art (square, full width of column)
Album title (Body Medium, 2 lines, ellipsis, centered)
Artist name (Body Small, secondary color, centered)
Song count badge (optional, top-right corner overlay)


Tap Action: Navigate to Album Detail Screen
Long Press: Select album → Add all to queue, Add to playlist
Sort Options: A-Z, Recently Added, Most Played, Year

5. Tab Content: Artists

Layout: List or grid
List View:

Circular avatar (64dp) - generated from artist name or first album art
Artist name (Title Large)
Song count (Body Small, secondary color)
Chevron right icon (trailing)


Grid View: 2-column with circular avatars + names
Tap Action: Navigate to Artist Detail Screen
Sort: A-Z, Most Played, Song Count

6. Tab Content: Playlists

Layout: Vertical list or grid
Items:

System Playlists (top, cannot delete):

Recently Played
Most Played
Recently Added


User Playlists: Below system playlists


Card Design:

Playlist cover: 4-image collage (2×2 grid of album arts) or custom image
Playlist name (Title Medium)
Song count (Body Small)
Creator/Date info


Actions:

Tap: Open playlist detail
Long press: Rename, Delete, Share


FAB: Create New Playlist (bottom-right, primary color)

7. Tab Content: Liked Songs

Layout: Same as Songs tab (list or grid)
Content: All songs marked as favorite
Header: Total count + "Shuffle All" button
Empty State:

Heart icon illustration
"No liked songs yet"
"Tap ❤️ on any song to add it here"


Actions: Same as Songs tab + unlike option

Interaction Patterns

Pull to Refresh: Re-scan library, update metadata
Fast Scroll: Draggable alphabetical scrollbar for Songs/Artists
Search Integration: Search bar appears when scrolling up or via search icon
Multi-Select Mode:

Triggered by long press
Top bar changes to show: Select All, Clear, Count of selected
Actions: Add to queue, Add to playlist, Delete, Share




🎵 Now Playing Screen Design
Purpose
Immersive, full-screen playback experience with album art, controls, lyrics, and queue management.
Layout Components
1. Background Treatment

Dynamic: Blurred and darkened version of album art (full screen)
Gradient Overlay: Vertical gradient from transparent (top 30%) → dark scrim (bottom 70%)
Alternative: Solid background with dominant color extracted from album art
Parallax Effect: Background shifts slightly when swiping or scrolling

2. Top Bar

Position: Overlaid on background, top edge
Content:

Back/Down arrow (left): Return to previous screen
Current queue name or "Playing from [Album/Playlist/Artist]" (center)
More menu (right): Share, Song info, Sleep timer, Add to playlist


Design: Semi-transparent background (scrim 40%), white icons

3. Album Art Display

Position: Upper third of screen, centered
Size:

Portrait: 70% of screen width (max 320dp)
Landscape: 40% of screen height


Design:

Square with rounded corners (24dp)
Elevation: 16dp with soft shadow
Animated: Gentle breathing animation (scale 1.0 → 1.02 loop)
Rotation effect (optional): Slow rotation when playing (vinyl simulation)


Gestures:

Swipe left/right: Change track
Tap: Toggle mini-controls overlay
Double-tap: Like/unlike song



4. Song Information

Position: Below album art
Content:

Song title (Headline Large, white, bold, 2 lines max, marquee if overflow)
Artist name (Title Medium, white 80% opacity, tappable → Artist Detail)
Album name (Body Medium, white 60% opacity, tappable → Album Detail)


Design: Center-aligned, generous padding

5. Like Button

Position: Right of song title or below artist name
Design: Heart icon, outline when unliked, filled + primary color when liked
Size: 32dp
Animation: Scale + color change on tap

6. Progress Bar & Timestamps

Position: Below song info
Components:

Elapsed time (left, Body Small, white 70%)
SeekBar:

Track: White 20% opacity
Active track (progress): Primary color or white
Thumb: Circular, 16dp, primary color, shows on touch


Total duration (right, Body Small, white 70%)


Behavior:

Smooth progress update every 100ms
Draggable for seeking
Haptic feedback on touch



7. Playback Controls

Position: Below progress bar
Layout: Centered horizontal row
Buttons (left to right):

Shuffle 🔀: Toggle (active = primary color, inactive = white 60%)
Previous ⏮️: Large (48dp), white icon
Play/Pause ▶️⏸️: Extra large (72dp), circular FAB, primary color background, white icon
Next ⏭️: Large (48dp), white icon
Repeat 🔁: Toggle (Off / Repeat All / Repeat One), color-coded


Spacing: 24dp between buttons, 32dp between Play/Pause and adjacent buttons
Animation:

Play/Pause: Morphing icon animation
All buttons: Ripple effect, scale on press



8. Secondary Actions Bar

Position: Below playback controls
Buttons (icon buttons, 40dp each):

Lyrics 📝: Open lyrics sheet
Sleep Timer ⏰: Open timer dialog
Equalizer 🎚️: Open EQ settings
Queue 📋: Open queue bottom sheet
Share ↗️: Share song/playlist


Design: White icons, 60% opacity, increase to 100% on active/hover
Layout: Evenly distributed horizontal row or two rows on smaller screens

9. Lyrics Button (Alternative Position)

Style: Floating chip at bottom or dedicated icon in secondary actions
Text: "Show Lyrics"
Action: Opens bottom sheet or full-screen lyrics view

10. Visualization (Optional)

Position: Behind or around album art
Types:

Animated waveform (real-time audio analysis)
Particle effects synced to beat
Pulsating circle around album art


Control: Toggle in settings, disabled by default for performance

Screen States
Playing State

Play button → Pause icon
Album art animates (breathing/rotation)
Progress bar moves smoothly

Paused State

Pause button → Play icon
Animations stop
Screen dims slightly after 5 seconds (optional)

Loading State

Circular progress indicator over album art
Controls disabled temporarily

Error State

Toast message: "Failed to play song"
Skip to next song automatically or retry prompt

Gestures & Interactions
Swipe Gestures

Left Swipe: Next track
Right Swipe: Previous track (or restart current if >5 seconds played)
Down Swipe: Minimize to mini-player
Visual Feedback: Album art slides in direction of swipe

Tap Interactions

Album Art Tap: Toggle visibility of lyrics overlay (if enabled)
Artist/Album Name Tap: Navigate to respective detail screen
Double-Tap Album Art: Like/unlike song (heart animation)

Animations & Transitions

Enter Animation: Slide up from bottom (mini-player expands)
Exit Animation: Slide down to mini-player
Track Change: Fade out/in album art, crossfade song info
Button Press: Ripple effect + subtle scale (0.95)


🎤 Lyrics Screen Design
Purpose
Display synchronized or static lyrics with auto-scrolling and user interaction.
Access Methods

Tap "Lyrics" button on Now Playing screen
Swipe up on Now Playing screen (alternative gesture)

Display Format
Full-Screen Lyrics View

Background:

Continuation of Now Playing background (blurred album art)
Darker scrim (60% opacity) for text readability


Layout: Bottom sheet or full-screen modal

Lyrics Display Area

Content:

With .lrc (synced):

Current line: Center-aligned, Headline Small, white, bold
Previous lines: Above, Body Medium, white 40%, smaller
Next lines: Below, Body Medium, white 40%, smaller
Smooth auto-scroll to center current line


Without sync (plain text):

All lyrics displayed in scrollable view
Current position estimated by playback time percentage
Manual scroll enabled




Highlighting:

Current line background: Subtle highlight (primary color 10% opacity)
Current line text: White 100%, slightly larger font
Fade in/out transition when line changes



Lyrics Header

Position: Top, collapsing or fixed
Content:

Back/Close button (left)
Song title + artist (center, Body Large)
More menu (right): Report error, Search lyrics source


Background: Transparent or gradient scrim

Lyrics Footer

Position: Bottom, floating above mini-player
Content:

Compact playback controls: Previous, Play/Pause, Next
Progress indicator (thin line)
Like button


Background: Blurred surface (frosted glass effect)

Interaction Features
Auto-Scroll Toggle

Button: Icon button in header or floating at bottom-right
States:

Auto-scroll ON (default): Lyrics scroll automatically
Auto-scroll OFF: User can manually scroll, auto-scroll resumes when reaching current line



Manual Seeking

Behavior: Tap any lyric line to jump to that timestamp
Visual Feedback: Ripple effect, line highlights briefly
Only Works With: Synced .lrc files

Share Lyrics

Action: Share current lyric line or entire lyrics as text/image
Format: Text or stylized image with album art background

Lyrics Source Logic
Priority Order

Local .lrc file: Check same folder as audio file, same name as audio file with .lrc extension
Embedded lyrics: Extract from ID3/metadata tags
Online fetch: Query lyrics API with song title + artist
Cache: Save fetched lyrics locally for offline access

Online Lyrics APIs (Implementation Options)

Lyrics.ovh: Free API, no auth required
Musixmatch: Requires API key, robust catalog
Genius: Good for annotations but requires scraping
Fallback: Display "Lyrics not available" with option to contribute

Lyrics Sync Algorithm (for non-synced)

Estimate line timing based on:

Total song duration
Number of lines
Average syllable count per line


Smooth scrolling even without perfect sync

Empty State

Icon: Microphone crossed out or musical note
Message: "No lyrics available for this song"
Action Button: "Search Online" → Manual lyrics fetch attempt

Error Handling

Parsing Error: "Lyrics file corrupted, displaying raw text"
Network Error: "Unable to fetch lyrics, check connection"
Not Found: "Lyrics not found for this song"


🎛️ Mini-Player Design
Purpose
Persistent, compact playback control accessible from any screen, always docked at bottom.
Position & Behavior

Location: Fixed at bottom of screen, above bottom navigation bar
Visibility: Visible when a song is playing/paused, hidden when no active playback
Interaction:

Tap anywhere: Expand to full Now Playing screen
Swipe down: Dismiss (stop playback)
Swipe left/right: Previous/Next track



Layout Components (Horizontal)
1. Album Art Thumbnail

Size: 56dp × 56dp
Position: Far left
Design:

Rounded corners (8dp)
Slight elevation (4dp)
Shadow for depth


Animation: Gentle pulsing when playing

2. Song Info (Center)

Content:

Song title (Body Medium, 1 line, ellipsis, primary text color)
Artist name (Body Small, 1 line, ellipsis, secondary text color)


Alignment: Left-aligned, centered vertically
Width: Flexible, takes remaining space between album art and controls

3. Playback Controls (Right)

Buttons (horizontal):

Play/Pause: Icon button (40dp), primary color icon
Next: Icon button (40dp), on-surface color icon
Close: Icon button (optional, 32dp), removes mini-player


Spacing: 8dp between buttons
Animation: Play/Pause morphs between icons

4. Progress Indicator

Position: Top edge of mini-player
Design:

Thin line (2dp height)
Color: Primary color
Smooth progress animation
Spans full width of mini-player



Styling

Background:

Surface color (elevated)
Elevation: 8dp
Corner radius: Top corners 16dp (if floating) or 0dp (if docked)
Optional: Frosted glass effect (blur underlying content)


Padding: 8dp all sides
Height: 72dp total (64dp content + 8dp progress bar)

Animations

Enter: Slide up from bottom (300ms, ease-out)
Exit: Slide down to bottom (200ms, ease-in)
Expand to Now Playing:

Mini-player scales up and morphs into Now Playing screen
Album art expands from thumbnail to large
Song info transitions smoothly
Duration: 400ms, Material motion curve


Swipe Gestures:

Horizontal swipe: Album art slides, new art slides in from opposite side
Vertical swipe: Mini-player follows finger, dismiss if swiped >50% down



States

Playing: Play icon shows, progress bar animates
Paused: Pause icon shows, progress bar stops
Loading: Indeterminate progress bar
Error: Red tint, error icon, toast message


📋 Queue Management Screen
Access Methods

Tap "Queue" button on Now Playing screen
Long-press mini-player (alternative gesture)
Swipe up from mini-player (if gesture enabled)

Display Format
Bottom Sheet Modal

Height: 70% of screen height, draggable handle at top
Background: Surface color with elevation, rounded top corners (28dp)
Sections:
1. Now Playing Header

Position: Top of sheet, fixed
Content:

"Now Playing" title (Title Medium)
Clear queue button (text button, right side)


Current Song Card:

Album art (48dp, rounded 8dp)
Song title + artist (Body Large/Medium)
"Now Playing" indicator (animated sound wave icon or pulsing dot)
Background: Primary container color, elevated



2. Up Next Header

Content: "Up Next" + count badge (e.g., "Up Next · 12 songs")
Actions:

Shuffle (icon button)
Clear up next (text button)



3. Queue List

Layout: Vertical scrollable list
Items:

Drag handle (left, 3 horizontal lines icon, on-surface color)
Album art thumbnail (48dp)
Song title (Body Medium, 1 line)
Artist name (Body Small, secondary color)
Duration (trailing)
Remove from queue button (X icon, far right)


Reordering:

Long-press or drag handle to enter reorder mode
Drag item to new position
Visual feedback: Lift item with shadow, shift other items
Haptic feedback on reorder


Actions:

Tap: Jump to that song immediately
Swipe left: Remove from queue
Long-press: Show context menu (Move to top, Remove, Go to album)



4. Playback History (Optional Section)

Toggle: "Show History" expandable section below queue
Content: Previously played songs (up to 50)
Action: Tap to replay song



Interaction Patterns

Drag Handle: Pull down to dismiss sheet, pull up to expand full-screen
Background Dim: Scrim overlay (50% opacity) behind sheet
Smooth Animations: Spring animation when adding/removing items

Empty State (No Queue)

Icon: Empty playlist icon
Message: "No upcoming songs"
Action: "Browse Library" button


💖 Favorites System Design
Implementation
Favorite Button

Icon: Heart outline (unliked), filled heart (liked)
Color: Secondary text color (unliked), primary color or red (liked)
Size: 24dp (standard), 32dp (Now Playing screen)
Location:

Song list items (trailing position)
Now Playing screen (near song title)
Album/Artist detail screens


Animation:

Tap: Scale up (1.2) → Scale down (0.9) → Normal (1.0), 300ms
Color: Fade from outline to filled, pulsing ring effect



Storage

Database: Room entity FavoriteSongs with song ID and timestamp
Sync: Update UI immediately, persist to DB asynchronously
Retrieval: Query favorites by timestamp (recently liked first) or alphabetically

Liked Songs Tab

Location: Library → Liked tab (5th tab)
Content: All favorited songs
Features:

Song count in tab badge
Shuffle all button in header
Sort options (Recently Liked, A-Z, Artist)
Multi-select to unfavorite




🎲 Daily Mixes & Playlists
Daily Mixes Generation
Algorithm Logic

Frequency: Regenerate daily at midnight or when app opens after 24 hours
Mix Types:
1. Genre-Based Mixes (Mix 1-3)

Analyze user's most played songs' genres
Create mixes for top 3 genres
Include 25-50 songs per mix
Name: "[Genre] Mix" (e.g., "Rock Mix", "Pop Mix")
Cover: Collage of album arts from songs in mix

2. Artist-Based Mix (Mix 4)

Top 3-5 most played artists
Include lesser-known tracks from those artists
Name: "Artist Mix" or "[Top Artist] & More"

3. Mood/Time-Based Mixes

Morning Mix: Energetic songs, higher BPM (120+)
Evening Mix: Relaxing songs, lower BPM (<100)
Logic: Analyze listening patterns by time of day

4. Discovery Mix

Rarely played or never played songs
Similar genre/artist to favorites
Name: "Discover Weekly"

5. Recently Added

Songs added to library in last 30 days
Name: "Recently Added"



Storage & Caching

Store mix definitions in Room DB (mix ID, song IDs, generated date)
Cache for offline access
Update incrementally when new songs added

User Playlists
Creation Flow

User t
RetryClaude does not have the ability to run the code it generates yet.MRContinueaps FAB on Playlists tab
2. Dialog appears: "Create New Playlist"

Text field: Playlist name (required)
Optional: Add description
Optional: Choose cover image (from gallery or auto-generate from songs)


Confirm → Empty playlist created
Prompt: "Add songs now?" → Navigate to song selection screen

Playlist Management
Playlist Detail Screen:

Header Section:

Large playlist cover (200dp square, centered)
Playlist name (Headline Medium, editable on tap)
Creator info: "Created by You · [Date]"
Song count + total duration
Description (Body Medium, expandable)


Action Buttons Row:

Play All (primary button, filled)
Shuffle (secondary button, outlined)
Edit (icon button): Rename, change cover, description
Share (icon button): Share playlist (export as M3U or text)
More (icon button): Delete, Duplicate, Add to queue


Song List:

Drag handles for reordering
Album art thumbnail + title + artist
Remove from playlist button (swipe or button)
Long-press: Multi-select mode


Add Songs FAB: Bottom-right, opens song selection screen

Adding Songs to Playlist:

Selection Screen:

Search bar at top
All songs list with checkboxes
Filter by: Songs, Albums, Artists
Multi-select enabled by default
Bottom bar: "Add [count] songs" button
Tap album/artist: Select all songs from that item


Context Menu Method:

Long-press any song → "Add to Playlist"
Bottom sheet with playlist list
"Create New Playlist" option at top



Playlist Cover Generation:

Auto-Generated:

Mosaic of first 4 song album arts (2×2 grid)
Gradient overlay with playlist name
Updates dynamically as songs added/removed


Custom:

Choose from gallery
Crop to square
Apply filters (optional)



System Playlists (Non-Editable)
1. Recently Played

Last 100 played songs
Ordered chronologically (newest first)
Icon: Clock or history icon
Auto-updates after each playback

2. Most Played

Top 50 songs by play count
Regenerates weekly
Icon: Fire or trending icon

3. Recently Added

Songs added in last 30 days
Ordered by date added (newest first)
Icon: Plus or new icon
Updates when library scanned

4. All Songs Playlist

Every song in library
Quick access to shuffle all
Icon: Music note icon


🎨 Theme System Design
Theme Options
1. Dynamic Colors (Material You)

Availability: Android 12+ (API 31+)
Behavior:

Extract colors from system wallpaper
Apply to: Primary, Secondary, Tertiary, Surface, Background
Updates automatically when wallpaper changes


Implementation: Use DynamicColors.applyToActivitiesIfAvailable()

2. Now Playing Adaptive Theme

Behavior:

Extract dominant colors from currently playing album art
Apply to: Primary color, gradients, backgrounds
Smooth transition (500ms) when song changes


Scope: Affects Now Playing screen only OR entire app (user preference)
Algorithm:

Use Palette API to extract vibrant, muted, dark, light colors
Choose highest population color for primary
Generate complementary colors for secondary/tertiary


Toggle: Settings → "Adaptive theme from album art" (ON/OFF)

3. Custom Color Themes

User-Defined Colors:

Primary color picker
Accent color picker
Background preference: Dark, Light, True Black (AMOLED)


Presets:

Ocean Blue: Blues and teals
Sunset Orange: Oranges and reds
Forest Green: Greens and browns
Royal Purple: Purples and magentas
Cherry Red: Reds and pinks
Midnight Black: Grays and blacks (AMOLED-friendly)


Color Picker UI:

Circular hue selector
Brightness/saturation slider
Live preview of UI elements
Hex code input field



4. Dark/Light Mode

Options:

System Default (follow system setting)
Always Dark
Always Light
Auto (Light 6am-6pm, Dark 6pm-6am)


Implementation:

AppCompatDelegate.setDefaultNightMode()
Separate color resources for dark/light (colors-night.xml)



Theme Application Scope
Affected Elements:

App Bar: Background uses surface color, text uses on-surface
Bottom Navigation: Background, active tab color (primary)
FABs: Primary color background, on-primary text/icon
Cards: Surface variant, elevation creates depth
Buttons: Primary/Secondary/Tertiary colors based on button type
Progress Bars: Primary color
Mini-Player: Surface elevated, primary accent
Now Playing: Dynamic if enabled, otherwise primary
Tab Indicators: Primary color
Selection: Primary container for selected items
Ripple Effects: Primary color at 20% opacity

Settings Screen Implementation
Theme Section:
Settings → Appearance

├─ Color Source
│  ○ Dynamic Colors (System) [Android 12+]
│  ○ Album Art Adaptive
│  ○ Custom Colors
│
├─ Custom Colors [if selected]
│  ├─ Primary Color [Color chip + picker]
│  ├─ Accent Color [Color chip + picker]
│  └─ Presets [Grid of preset options]
│
├─ Dark Mode
│  ○ System Default
│  ○ Always Dark
│  ○ Always Light
│  ○ Auto (Time-based)
│
├─ True Black (AMOLED) [Toggle]
│  └─ Pure black backgrounds for power saving
│
└─ Apply Album Colors To
   ○ Now Playing Only
   ○ Entire App
```

### Theme Priority Logic
```
1. User selects Custom Colors → Override all
2. User selects Album Art Adaptive → Apply to specified scope
3. User selects Dynamic Colors → System colors applied
4. Fallback → Default Material 3 baseline colors
```

### Accessibility Considerations
- **Contrast Ratio**: Ensure 4.5:1 minimum for text
- **Color Blindness**: Test with deuteranopia/protanopia simulators
- **Override Option**: Force high-contrast mode in accessibility settings
- **Validation**: Warn user if custom colors have poor contrast

---

## 🔔 Background Playback & Notifications

### Foreground Service

**Implementation Requirements**:
- **Service Type**: MediaBrowserService (for Android Auto compatibility)
- **Notification Channel**: "Music Playback" (importance: LOW, no sound/vibration)
- **Sticky Service**: Service persists even when app is closed
- **Lifecycle**: Start on first playback, stop when user dismisses notification OR 5 minutes after pause

**Service Responsibilities**:
1. Manage ExoPlayer instance
2. Handle audio focus (duck/pause on interruption)
3. Respond to media button events (headset controls)
4. Update notification with current song info
5. Maintain queue and playback state
6. Cache current position for resume on app reopen

### Notification Design

**Expanded View** (when notification is expanded):
- **Layout**: MediaStyle notification
- **Components**:
  - **Album Art**: Large (64dp square) on left
  - **Song Title**: Large text, 2 lines max
  - **Artist Name**: Small text, 1 line
  - **Progress Bar**: Thin, below text (optional, Android 13+)
  - **Control Buttons** (5 buttons):
    1. Previous track
    2. Rewind 10s (or Shuffle toggle)
    3. Play/Pause (large, emphasized)
    4. Fast-forward 10s (or Repeat toggle)
    5. Next track
  - **Close Button**: Small X icon (dismisses notification, stops playback)
  
**Collapsed View** (when notification is collapsed):
- **Components**:
  - Album art thumbnail (48dp)
  - Song title (1 line)
  - Artist name (1 line, secondary text)
  - Play/Pause button only
  
**Actions**:
- Tap notification body: Open app to Now Playing screen
- Tap control buttons: Immediate playback control
- Swipe away: Stop playback, close notification

**Notification Behavior**:
- **When Playing**: Persistent, cannot be swiped away
- **When Paused**: Can be swiped away after 5 minutes (configurable)
- **On Low Battery**: Reduce notification update frequency (1s → 5s)

### Lock Screen Controls

**Implementation**:
- **MediaSession**: Register MediaSessionCompat with metadata
- **Metadata**:
  - Title, Artist, Album
  - Album art bitmap
  - Duration
  - Current position
- **Actions**: Play/Pause, Previous, Next, Seek
  
**Lock Screen Display** (varies by device):
- Album art as background (blurred)
- Song info overlay
- Playback controls (standard Android media controls)
- Progress bar (on some devices)

**Always-On Display (AOD)**:
- On supported devices, show minimal song info + controls
- Album art thumbnail
- Tap to wake and expand controls

### Media Button Handling

**Headset/Bluetooth Controls**:
- **Play/Pause**: Single click
- **Next Track**: Double click
- **Previous Track**: Triple click
- **Voice Assistant**: Long press (if supported)

**Response to Audio Events**:
1. **Phone Call Incoming**: Pause playback, resume after call ends
2. **Another App Plays Audio**: Duck volume (reduce to 20%) OR pause (user setting)
3. **Alarm Sounds**: Pause playback
4. **Notification Sound**: Duck briefly (2 seconds)
5. **Unplug Headphones**: Pause immediately
6. **Bluetooth Disconnect**: Pause immediately
7. **Audio Jack Plug In**: Resume if was playing before unplug (user setting)

**Audio Focus**:
- **Request**: AUDIOFOCUS_GAIN for music playback
- **Loss Transient**: Pause, resume when focus regained
- **Loss**: Pause and don't resume (another app took permanent focus)

---

## ⚙️ Settings Screen Design

### Layout Structure

**Scrollable List** with categorized sections:

---

### **1. Library Section**

**Music Folder**
- Primary text: "Music Folder"
- Secondary text: Current folder path (truncated, e.g., "Internal Storage/Music")
- Icon: Folder icon
- Action: Tap → Folder selection dialog (DocumentTree picker)
- Long-press: Show full path in dialog

**Scan Library**
- Primary text: "Scan Library"
- Secondary text: "Last scanned: [timestamp]"
- Icon: Refresh icon
- Action: Tap → Trigger rescan (progress dialog)
- Show: "X new songs found" after scan completes

**Excluded Folders**
- Primary text: "Excluded Folders"
- Secondary text: "Choose folders to ignore"
- Icon: Block icon
- Action: Opens list of excluded folders, FAB to add new
- Use case: Ignore ringtones, podcasts, audiobooks folders

**File Types**
- Primary text: "Supported Formats"
- Secondary text: "MP3, FLAC, WAV, M4A, OGG"
- Icon: File icon
- Action: Informational only OR allow enabling/disabling formats

---

### **2. Appearance Section**

**Theme**
- Primary text: "Theme"
- Secondary text: Current selection (e.g., "Dynamic Colors")
- Icon: Palette icon
- Action: Opens theme selection screen (detailed above)

**Dark Mode**
- Primary text: "Dark Mode"
- Secondary text: Current setting (e.g., "System Default")
- Icon: Moon icon
- Action: Radio button dialog (System/Always Dark/Always Light/Auto)

**True Black (AMOLED)**
- Primary text: "True Black"
- Secondary text: "Pure black backgrounds for OLED screens"
- Icon: Smartphone icon
- Toggle: Switch on right side

**Album Art in Lists**
- Primary text: "Show Album Art"
- Secondary text: "Display thumbnails in song lists"
- Toggle: ON/OFF
- Impact: Performance improvement when OFF for large libraries

**Animations**
- Primary text: "Animations"
- Secondary text: "Enable smooth transitions and effects"
- Toggle: ON/OFF
- Impact: Reduces motion for accessibility or battery saving

---

### **3. Playback Section**

**Audio Quality**
- Primary text: "Audio Quality"
- Secondary text: "High (Original)"
- Icon: High quality icon
- Options: Original / High / Medium / Low (for potential future streaming)

**Gapless Playback**
- Primary text: "Gapless Playback"
- Secondary text: "Seamless transition between songs"
- Toggle: ON/OFF
- Impact: Remove silence between tracks

**Crossfade**
- Primary text: "Crossfade"
- Secondary text: "0 seconds" (or current value)
- Icon: Transition icon
- Action: Opens slider (0-12 seconds)
- Behavior: Overlap song endings/beginnings

**Equalizer**
- Primary text: "Equalizer"
- Secondary text: "Adjust audio frequencies"
- Icon: Equalizer icon
- Action: Opens equalizer screen (if supported)
- Options: Presets (Rock, Pop, Jazz, Classical, Custom) + manual sliders

**Resume on Headphones**
- Primary text: "Resume on Connect"
- Secondary text: "Auto-play when headphones plugged in"
- Toggle: ON/OFF

**Pause on Disconnect**
- Primary text: "Pause on Disconnect"
- Secondary text: "Auto-pause when headphones unplugged"
- Toggle: ON (default, non-optional for UX)

**Audio Focus**
- Primary text: "Audio Focus Behavior"
- Secondary text: "Pause" (or "Duck Volume")
- Options: Pause / Duck Volume / Ignore
- Context: What happens when another app plays audio

---

### **4. Lyrics Section**

**Show Lyrics**
- Primary text: "Show Lyrics"
- Secondary text: "Display lyrics when available"
- Toggle: ON/OFF

**Auto-Fetch Lyrics**
- Primary text: "Auto-Fetch Online"
- Secondary text: "Download lyrics if not found locally"
- Toggle: ON/OFF
- Dependency: Requires internet permission

**Lyrics Source Priority**
- Primary text: "Lyrics Source Priority"
- Secondary text: "Local → Embedded → Online"
- Action: Opens reorderable list to change priority

**Clear Lyrics Cache**
- Primary text: "Clear Lyrics Cache"
- Secondary text: "X MB cached"
- Icon: Delete icon
- Action: Confirmation dialog → Clear cached online lyrics

---

### **5. Advanced Section**

**Sleep Timer**
- Primary text: "Sleep Timer"
- Secondary text: "Not set" (or time remaining)
- Icon: Timer icon
- Action: Opens sleep timer dialog
- Options: 5, 10, 15, 30, 45, 60 minutes / End of song / Custom

**Backup Playlists**
- Primary text: "Backup Playlists"
- Secondary text: "Export playlists to storage"
- Icon: Upload icon
- Action: Export all playlists as .m3u files to Downloads folder
- Confirmation: "Playlists backed up to Downloads"

**Restore Playlists**
- Primary text: "Restore Playlists"
- Secondary text: "Import playlists from storage"
- Icon: Download icon
- Action: File picker → Select .m3u files → Import

**Clear Play History**
- Primary text: "Clear Play History"
- Secondary text: "Delete recently played and play counts"
- Icon: History icon
- Action: Confirmation dialog → Clear all history

**Clear Cache**
- Primary text: "Clear Cache"
- Secondary text: "X MB cached (images, lyrics)"
- Icon: Delete icon
- Action: Confirmation dialog → Clear cache

**Reset App**
- Primary text: "Reset App"
- Secondary text: "Clear all data and settings"
- Icon: Warning icon (red)
- Action: Confirmation dialog with password/warning → Reset to factory state

---

### **6. About Section**

**Version**
- Primary text: "Version"
- Secondary text: "1.0.0 (Build 1)"
- Icon: Info icon
- Action: Tap 7 times → Enable developer mode (optional easter egg)

**Developer**
- Primary text: "Developer"
- Secondary text: "Your Name/Studio"
- Icon: Code icon
- Action: Opens link to website/GitHub

**Licenses**
- Primary text: "Open Source Licenses"
- Secondary text: "View third-party licenses"
- Icon: Document icon
- Action: Opens scrollable list of dependencies + licenses

**Privacy Policy**
- Primary text: "Privacy Policy"
- Secondary text: "Your data stays on your device"
- Icon: Shield icon
- Action: Opens policy document

**Rate App**
- Primary text: "Rate on Play Store"
- Secondary text: "Support the development"
- Icon: Star icon
- Action: Opens Play Store listing

**Share App**
- Primary text: "Share App"
- Secondary text: "Tell your friends"
- Icon: Share icon
- Action: Share link to Play Store

---

### Settings UI Design

**Visual Style**:
- List items with Material 3 components
- Leading icons (24dp, on-surface color variant)
- Two-line list items (title + subtitle)
- Trailing widgets: Switches, chevrons, or info icons
- Dividers between sections (not between items)
- Section headers: Small caps, primary color, 12sp

**Interaction**:
- Ripple effect on tap
- Immediate toggle response (switches)
- Confirmation dialogs for destructive actions (red accent)
- Progress indicators for long operations (scan, backup)

---

## 🎵 Album Detail Screen Design

### Access
- Tap album card from Library → Albums tab
- Tap album name from song detail/Now Playing

### Layout Components

#### **1. Hero Section**
- **Album Art**: 
  - Large (300dp square), centered
  - Rounded corners (16dp)
  - Elevation (12dp)
  - Parallax scroll effect (moves slower than content)
- **Background**: 
  - Blurred + darkened version of album art (full-width)
  - Gradient scrim overlay (top: transparent 30%, bottom: dark 70%)
- **Height**: Collapses on scroll (400dp → 180dp)

#### **2. Album Info Section**
- **Position**: Below album art, overlapping slightly (translucent card)
- **Content**:
  - Album name (Headline Medium, bold, 2 lines max)
  - Artist name (Title Medium, tappable → Artist Detail)
  - Year (Body Small, secondary color)
  - Song count + total duration (Body Small, secondary color)
  - Genre tags (chips, optional)
- **Background**: Surface color with elevation

#### **3. Action Buttons**
- **Layout**: Horizontal row, below album info
- **Buttons**:
  1. **Play All** (filled button, primary color, large)
  2. **Shuffle** (outlined button, secondary)
  3. **Add to Queue** (icon button)
  4. **Share** (icon button)
  5. **More** (icon button): Add to playlist, Download (if streaming)

#### **4. Song List**
- **Header**: "Songs" (Title Small)
- **List Items**:
  - Track number (leading, 32dp width, centered)
  - Song title (Body Large, 2 lines max)
  - Duration (trailing, Body Small)
  - More menu (3-dot icon, trailing)
  - No album art (redundant in this context)
- **Dividers**: Subtle lines between songs
- **Tap**: Play song + load album as queue
- **Long-press**: Multi-select mode

#### **5. More by Artist Section** (Optional)
- **Title**: "More by [Artist Name]"
- **Content**: Horizontal scrolling list of other albums by same artist
- **Cards**: Album art + album name
- **Action**: Tap → Navigate to that album

### Scroll Behavior
- **Parallax**: Album art scales down and fades as user scrolls
- **Collapsing Toolbar**: Title appears in toolbar when hero section collapses
- **Sticky Action Buttons**: Buttons stick below toolbar when scrolling

---

## 🎤 Artist Detail Screen Design

### Access
- Tap artist item from Library → Artists tab
- Tap artist name from song/album detail

### Layout Components

#### **1. Hero Section**
- **Artist Avatar**: 
  - Large circular image (200dp diameter)
  - Generated from artist name (first letter, colorful background) OR first album art
  - Centered at top
- **Background**: Gradient or solid color based on avatar
- **Artist Name**: Large, bold, centered below avatar

#### **2. Statistics Bar**
- **Content**:
  - Song count (e.g., "47 songs")
  - Album count (e.g., "5 albums")
  - Total play time (e.g., "3.2 hours")
- **Layout**: Horizontal, evenly spaced, Body Small
- **Icons**: Small leading icons for each stat

#### **3. Action Buttons**
- **Layout**: Horizontal row
- **Buttons**:
  1. **Shuffle All** (filled button, primary)
  2. **Play All** (outlined button)
  3. **Share** (icon button)

#### **4. Tabs**
- **Tab 1: Songs**
  - All songs by artist
  - Sorted by album → track number
  - OR sorted by most played
  - List format with album art, title, album name, duration
  
- **Tab 2: Albums**
  - Grid of albums by artist
  - Sorted by year (newest first) or alphabetically
  - Tap → Navigate to Album Detail
  
- **Tab 3: Similar Artists** (Optional, future feature)
  - Algorithmically determined similar artists
  - Based on genre, user listening patterns
  - Circular avatars with names

### Scroll Behavior
- Hero section collapses
- Artist name appears in toolbar
- Tabs stick below toolbar

---

## 🔍 Search Implementation Details

### Indexing Strategy

**Database Structure**:
- **Songs Table**: id, title, artist, album, genre, path, duration, play_count
- **FTS (Full-Text Search) Table**: Indexed title, artist, album, genre
- **Triggers**: Auto-update FTS table when songs added/modified

**Indexing Process**:
- Index created during library scan
- Updates incrementally when new songs added
- Rebuild index on demand (Settings → Advanced)

**Search Query Processing**:
1. User types query
2. Debounce 300ms (wait for typing pause)
3. Query FTS table with MATCH operator
4. Rank results by relevance (title match > artist > album > genre)
5. Apply current filter (Songs/Albums/Artists)
6. Limit results (50 per category for performance)
7. Update UI asynchronously

**Fuzzy Matching**:
- Allow 1-character substitution for queries >4 characters
- Use Levenshtein distance algorithm
- Example: "bettles" matches "beatles"

### Search Performance Optimization
- Cache recent searches (last 20)
- Prefetch common queries on app start
- Use coroutines for async operations
- Cancel previous search when new query entered
- Pagination for large result sets (load more on scroll)

---

## 📊 Playback Analytics & Daily Mixes Algorithm

### Data Collection

**Events Tracked**:
- Song played: Song ID, timestamp, duration listened, completion percentage
- Song skipped: Song ID, timestamp, position skipped at
- Song liked/unliked: Song ID, timestamp
- Playlist created/modified: Playlist ID, songs added/removed
- Search queries: Query text, result clicked

**Storage**:
- Room database table: PlaybackHistory
- Fields: song_id, timestamp, duration_listened, completion_percentage
- Retention: Keep last 90 days, purge older
- Privacy: All data local, never transmitted

### Analytics Processing

**Metrics Calculated**:
- Play count per song
- Average completion rate (songs played >80% = liked indicator)
- Listening patterns by time of day
- Genre distribution (percentage of plays per genre)
- Artist affinity (plays per artist)
- Skip rate (songs skipped <30% = dislike indicator)

**Daily Mix Algorithm**:
```
1. Query PlaybackHistory for last 30 days
2. Aggregate by genre:
   - Count plays per genre
   - Weight recent plays higher (decay function)
3. Identify top 3 genres
4. For each genre:
   - Select most played songs (50%)
   - Add similar songs from library (30%)
   - Add discovery songs (rarely played, same genre) (20%)
5. Shuffle within mix
6. Generate mix metadata:
   - Name: "[Genre] Mix"
   - Cover: Mosaic of top 4 songs' album art
7. Store mix definition in DB
8. Regenerate daily OR when significant library changes
```

**Discovery Algorithm**:
```
1. Identify user preferences:
   - Favorite genres (from liked songs)
   - Favorite artists
   - BPM range (from most played songs)
2. Query library for songs matching:
   - Same genres BUT play_count < 3
   - Related artists (same genre)
3. Score songs:
   - +10 points: Same genre as favorite
   - +5 points: Related artist
   - +3 points: Similar BPM
   - -5 points: Recently added (likely already heard)
4. Select top 50 scored songs
5. Shuffle and create "Discover Mix"
```

---

## 🎧 Audio Playback Implementation Details

### ExoPlayer Configuration

**Player Instance**:
- Singleton pattern (one player for entire app)
- Background thread for operations
- Audio offload enabled (hardware decoding when available)

**Supported Formats**:
- MP3 (MPEG Audio Layer III)
- FLAC (Free Lossless Audio Codec)
- WAV (Waveform Audio File Format)
- M4A/AAC (MPEG-4 Audio)
- OGG Vorbis
- Opus

**Audio Attributes**:
- Content type: CONTENT_TYPE_MUSIC
- Usage: USAGE_MEDIA
- Handle audio focus: AUDIOFOCUS_GAIN

**Playback Features**:
- **Gapless Playback**: Enabled by default, use ConcatenatingMediaSource
- **Crossfade**: Custom renderer to overlap track endings (user-configurable 0-12s)
- **Replay Gain**: Normalize volume across tracks (if metadata available)
- **Skip Silence**: Automatically skip silent portions (user toggle)

### Queue Management

**Queue Structure**:
- Current song: MediaItem with metadata
- Up next: List<MediaItem>, ordered
- History: List<MediaItem>, circular buffer (last 50)

**Queue Operations**:
- Add to queue (end)
- Add next (after current)
- Move to position
- Remove from queue
- Clear queue
- Shuffle queue (Fisher-Yates algorithm)

**Playback Modes**:
- **Normal**: Play queue sequentially, stop at end
- **Repeat All**: Loop entire queue
- **Repeat One**: Loop current song
- **Shuffle**: Randomize order, preserve history to prevent repeats

**Shuffle Algorithm**:
```
1. Store original queue order
2. Shuffle using Fisher-Yates
3. Track played songs in session
4. When queue exhausted:
   - Reshuffle unplayed songs
   - Avoid repeating last 5 songs
5. Un-shuffle: Restore original order
Metadata Extraction
Libraries Used:

Android MediaMetadataRetriever (basic)
JAudiotagger (advanced, for ID3v2, FLAC tags)

Extracted Fields:

Title (fallback: filename without extension)
Artist (fallback: "Unknown Artist")
Album (fallback: "Unknown Album")
Album Artist
Genre
Year
Track number
Disc number
Duration
Album art (embedded)
Lyrics (embedded or .lrc file)
Bitrate
Sample rate

Album Art Handling:

Extract from metadata
Cache as Bitmap (scaled to 512px)
Store in app cache directory
Generate placeholder if missing (colored gradient with first letter)


🔐 Permissions & First Launch Flow
Required Permissions
Android 10 and Below:

READ_EXTERNAL_STORAGE: Read audio files
WRITE_EXTERNAL_STORAGE: Save playlists, lyrics cache (if needed)

Android 11+:

READ_MEDIA_AUDIO: Read audio files (scoped storage)
Or ACTION_OPEN_DOCUMENT_TREE: User-granted folder access

Optional Permissions:

INTERNET: Fetch online lyrics, album art
WAKE_LOCK: Keep CPU awake during playback
FOREGROUND_SERVICE: Background playback
POST_NOTIFICATIONS (Android 13+): Show playback notification

First Launch Onboarding
Flow:
Screen 1: Welcome

App logo (large, animated entrance)
Title: "Welcome to [App Name]"
Subtitle: "Your personal music player"
Features list (3-4 bullet points with icons):

🎵 Play local music files
📝 Synced lyrics support
🎨 Beautiful Material You design
💖 Smart playlists & favorites


Button: "Get Started" (filled, primary color)

Screen 2: Permissions

Icon: Folder with checkmark
Title: "Access Your Music"
Description: "We need permission to find music files on your device. Your data stays private and local."
Button: "Grant Permission" → Request READ_MEDIA_AUDIO
Skip option: "Skip for Now" (text button) → App functional but no music

Screen 3: Folder Selection

Title: "Choose Music Folder"
Description: "Select where your music is stored. You can change this later in settings."
Large folder icon with tap target
Button: "Select Folder" → ACTION_OPEN_DOCUMENT_TREE
Common suggestions:

Internal Storage/Music
Internal Storage/Downloads
SD Card/Music


Selected path preview (editable)

Screen 4: Scanning Progress

Animated scanning illustration (files flying into folder)
Title: "Scanning Your Library"
Progress bar (determinate if possible)
Status text: "Found X songs, Y albums, Z artists..."
Cannot skip (but can cancel → return to folder selection)

Screen 5: Complete

Success icon (animated checkmark)
Title: "All Set!"
Summary: "Found X songs in Y albums"
Button: "Start Listening" → Navigate to Home screen
Auto-navigate after 2 seconds if user doesn't interact

Onboarding Design

Use ViewPager2 for horizontal swipe navigation
Page indicator dots at bottom
Skip button (top-right) on all except last screen
Back button (top-left) to previous screen
Smooth transitions between screens
Save progress: User can exit and resume onboarding


🎛️ Additional Features Detail
Sleep Timer
Access:

Now Playing → More menu → Sleep Timer
Settings → Sleep Timer
Quick tile (Android notification shade)

Interface:

Bottom sheet dialog
Preset buttons (chips):

5 min, 10 min, 15 min
30 min, 45 min, 1 hour
End of current song
End of album/playlist
Custom (number picker)


Active timer display: "Playback will stop in 23:45"
Cancel button
Extend button (+5 min, +10 min)

Behavior:

Countdown timer in notification
Fade out volume over last 10 seconds
Pause playback at end
Close notification (if setting enabled)
Show toast: "Sleep timer ended"

Equalizer
Implementation:

Use system Equalizer API (android.media.audiofx.Equalizer)
Check if device supports: hasSystemFeature(AUDIO_LOW_LATENCY)
Fallback: "Equalizer not supported on this device"