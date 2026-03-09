# Manual Test Procedure: Add/Delete Deck Flows

Use this procedure to manually verify add and delete deck flows on Android and iOS.

## Prerequisites

- **Android**: Emulator or physical device; app built and installed
- **iOS**: Simulator or physical device; app built in Xcode
- A `.deck.json` file (e.g. copy from `shared/src/commonMain/composeResources/files/en-de-german-basics.deck.json` or create one)

## Add Deck Flows

### 1. Import deck (from file)

1. Launch the app.
2. Open the menu (⋮) → **Import deck**.
3. **Android**: File picker opens → select a `.deck.json` file.
4. **iOS**: Document picker opens → select a `.deck.json` file (e.g. from Files app or AirDrop).
5. **Verify**: A new language combination or deck appears in the dropdown; the deck can be selected and practiced.

### 2. Add deck (from bundled)

1. Launch the app.
2. Open the menu (⋮) → **Add a deck**.
3. In the dialog, select a deck not yet loaded (e.g. German Basics if not loaded).
4. **Verify**: The deck appears in the dropdown; it can be selected and practiced.

### 3. Restore default decks

1. Launch the app with some decks loaded.
2. Open the menu (⋮) → **Restore default decks**.
3. Confirm in the dialog.
4. **Verify**: All decks are removed and replaced with the four default decks (French Basics, French Past Tense, French Conversation, Spanish Basics).

## Delete Deck Flows

### 4. Delete a deck

1. Launch the app with at least two decks (e.g. French Basics and Spanish Basics).
2. Open the menu (⋮) → **Delete deck**.
3. Select a deck from the dropdown (e.g. French Basics).
4. Tap **Delete**.
5. **Verify**: The deck is removed; it no longer appears in the deck dropdown. The language combination remains if other decks exist for it.

### 5. Delete last deck in a language set

1. Ensure only one deck exists for a given language (e.g. only Spanish Basics).
2. Open the menu (⋮) → **Delete deck**.
3. Select that deck → **Delete**.
4. **Verify**: The deck and its language combination are removed from the list.

## Automated Tests

- **Android**: `./gradlew :androidApp:testDebugUnitTest` — runs `DeckRepositoryTest` (JVM) and shared `DeckRepositoryTest` (androidUnitTest).
- **iOS**: `./gradlew :shared:iosSimulatorArm64Test` — runs shared `DeckRepositoryTest` (iosSimulatorArm64) and `CardSelectorTest`. Requires macOS.

CI runs both; see `.github/workflows/android.yml` and `.github/workflows/ios.yml`.
