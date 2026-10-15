# ChainPlay - Deliverable 3 (Prototype + Tests)

A console playlist manager for songs, podcast episodes, and audiobook
chapters. All storage is the provided generic `LinkedChain<T>`.

## Layout

```
chainplay/
  src/   LinkedChain, MediaItem, Song, PodcastEpisode, AudiobookChapter,
         MediaType, InputValidator, PlaylistManager, ConsoleUI, Main
  test/  LinkedChainTest, MediaItemTest, InputValidatorTest,
         PlaylistManagerTest, ConsoleUITest   (JUnit 4, 80 tests)
```

`chainplay/src/main/java/` holds a teammate's in-progress rewrite of the item
classes (it adds an album field). It is not part of this build; see
"What doesn't work yet".

## How to compile and run

All commands are run from inside the `chainplay` folder.

```
cd chainplay
```

Compile and run the program:

```
javac -d out src/*.java
java -cp out Main
```

Compile and run the tests (needs `junit-4.13.2.jar` and
`hamcrest-core-1.3.jar` in the `chainplay` folder):

```
javac -d out -cp junit-4.13.2.jar:hamcrest-core-1.3.jar src/*.java test/*.java
java -cp out:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore LinkedChainTest MediaItemTest InputValidatorTest PlaylistManagerTest ConsoleUITest
```

On Windows, use `;` instead of `:` between the jar names.

In Eclipse: create a Java project on the `chainplay` folder, exclude
`src/main` from the build path, add the JUnit 4 library, then right-click
`test` and choose Run As > JUnit Test. Run the app with Run As > Java
Application on `Main.java`.

## How the prototype uses LinkedChain

* `PlaylistManager` holds `LinkedChain<Song>`, `LinkedChain<PodcastEpisode>`
  and `LinkedChain<AudiobookChapter>`: three element types, one generic class,
  no type-specific copies, no casts, no raw types.
* Search results are also `LinkedChain`s of the same type.
* The manager only calls public methods: `addToEnd`, `add`, `get`, `size`,
  `isEmpty`, `contains`, `clear`, `toArray`. All playlist logic (positions,
  duplicates, searching, formatting) is in the app classes.
* `LinkedChainTest` also stores `String` and `Integer` in the same class.

## Where validation lives

| Bad input | Caught by | What happens |
|---|---|---|
| Menu choice (letters, decimals, 0, 8, overflow) | `InputValidator.parseMenuChoice` | `ConsoleUI` prints the error and asks again |
| Position out of range | `InputValidator.parsePosition`; `PlaylistManager.deleteAt` re-checks | Re-prompt; manager throws `IllegalArgumentException` and the list is unchanged |
| Text (blank, >100 chars, tab) | `InputValidator.parseText`; item constructors re-check | Re-prompt; constructors throw `IllegalArgumentException` |
| Duration (`3:75`, `0:00`, `601:00`, `-1:20`) | `InputValidator.parseDuration`; `MediaItem` constructor re-checks | Re-prompt / exception |
| Yes/no (`maybe`, blank) | `InputValidator.parseYesNo` | Re-prompt |
| Duplicate item | `PlaylistManager.isDuplicate` | `ConsoleUI` asks to confirm; "n" leaves the list unchanged |
| Empty list on delete | `PlaylistManager.size` | `ConsoleUI` prints "nothing to delete" without asking for a position |
| No search matches | `PlaylistManager.search*` | Empty chain, printed as "No matches found." |
| Null add / null search query | `LinkedChain.add` and the manager | `IllegalArgumentException`, size unchanged |
| End of input (Ctrl+D) | `ConsoleUI.promptUntilValid` | Prints "Goodbye!" and returns |

## Reflection

### What changed from our specification?

The specification contradicted itself in places, so building it forced
decisions. The main ones:

* **Three typed chains instead of one `LinkedChain<MediaItem>`.** Class Design
  described one chain holding all three kinds, but the Generic Contract
  (`LinkedChain<Song>`, `<PodcastEpisode>`, `<AudiobookChapter>`) and the Test
  Plan (`size(SONG)`, "the other two lists empty", numbering per section) assume
  separate lists. Separate chains also removed the need for `instanceof` and
  casts. We added a small `MediaType` enum so the UI and `deleteAt` can say
  which list they mean.
* **Search API.** The spec listed `filterArtist`, `filterAlbum`, `filterGenre`,
  `filterPodcaster`, ... (one per field) returning arrays. We built
  `searchSongs/Episodes/Chapters(query)`, which match the query against every
  field via `MediaItem.matches`, plus `search*ByDuration(maxSeconds)`. These
  return a new `LinkedChain` of the same type, as the Test Plan said, and
  share one private generic `filter` helper.
* **Printing.** `displayPlaylist()` became `formatPlaylist()` and
  `formatList(chain)` returning text, because the spec says the manager "does
  not read input or print". Only `ConsoleUI` prints.
* **Immutable items.** Data & State says item fields are private and final, so
  we dropped every setter listed in the Method Signatures table.
* **Constructor order.** `Song(title, artist, genre, seconds)` follows the Test
  Plan. `PodcastEpisode(title, podcaster, podcastTitle, description, seconds)`
  and `AudiobookChapter(title, author, narrator, bookTitle, genre,
  description, seconds)` put the creator second so all three read the same way.
* **Album support removed for now.** The diagram's `album` field and the
  `addAlbum/addPodcast/addAudiobook/removeAlbum/...` collection methods are not
  built. The Test Plan never covered them and the spec itself said we needed to
  confirm how they would work. `currentIndex` in `PlaylistManager` was also
  dropped because nothing uses it yet.
* **Where validation lives.** Same layers as specified, with one difference:
  `InputValidator` returns a small `Result<T>` (value or error message) rather
  than throwing, so the UI can re-prompt without try/catch. The item
  constructors and manager still throw `IllegalArgumentException`, so bad
  input cannot get into a chain even if the UI is bypassed.
* **Delete by position.** The chain has no remove-by-index (see next section),
  and `remove(T)` removes the *first equal* item, which is wrong when duplicates
  differ in other fields. `deleteAt` copies the chain with `toArray`, calls
  `clear`, and re-adds everything except the chosen item. It is O(n) and uses
  only the public API. A test checks that the right duplicate is removed.
* **Tests.** We kept the Test Plan's cases (one normal + one bad input per
  method) but renamed them to match the final method names. They use
  **JUnit 4** (4.13.2). Moving to JUnit 5 means changing the imports and
  `@Before` to `@BeforeEach`; the logic does not change.

### What changed in the LinkedChain?

**Nothing. We used the provided class unchanged.** The spec described a
different class from the one we were given (Iterable, `filter(Predicate)`,
`remove(int)`, a `tail` field, `size` tracked as a field). The provided class
has `get(int)`, `toArray`, `remove(T)`, and `addToEnd` (which walks the chain,
O(n), since there is no tail). Everything the app needed could be built on top
of those, so we kept the chain storage-only. Consequences: `addToEnd` is O(n),
so adding N items is O(N²), which is fine for playlist sizes but would be the
first thing to change (add a tail, or an `Iterable`) if it mattered.

### What happened to our unknowns?

* *How collection features (albums, whole podcasts/books) would work:* decided
  to defer them (see above); **still open.**
* *How to delete by position when the chain removes by value:* **figured
  out** (rebuild from a `toArray` snapshot).
* *How to filter a chain without putting application logic in it:* **figured
  out** (a generic private `filter` in the manager using `toArray` plus a
  `Predicate`).
* *How to test the console without a real keyboard:* **figured out**
  (`ConsoleUI` takes a `Scanner` and `PrintStream`; tests pass scripted input).

### What doesn't work yet?

* Album/collection add and remove, the GUI, shuffle, and the doubly linked
  stretch goal are not built.
* The `Song` album field from the diagram is not in this build. A teammate's
  version with an album field is in `src/main/java/`, but the manager, UI, and
  tests have not been switched over to it yet.
* `pom.xml` is set up for JUnit 5 and the `src/main/java` folder, so
  `mvn test` does not run our tests yet. Use the `javac` commands above or
  Eclipse.
* Every text field is required (including descriptions), so adding a chapter
  takes seven prompts. There is no way to skip a field or edit an item.
* Playlists are in memory only; nothing is saved between runs.
* Getters have no bad-input case of their own (there is no input to a getter);
  bad values are covered by the constructor tests.
* No known failing tests: all 80 pass.

## Next Steps

### Finish the MVP (in this order)

1. Re-read the spec against the code with the whole team and update the
   specification so it matches the three-chain design (Riya).
2. Merge the `src/main/java` item classes (album field) into `src`, update
   `PlaylistManager`, `ConsoleUI`, and the tests to the new constructors, and
   fix `pom.xml` so `mvn test` runs the suite (Abhinav).
3. Decide what "add an album/collection" means, then implement
   `addAlbum`-style methods in `PlaylistManager` using `addToEnd` (Abhinav).
4. Let the user skip optional fields (descriptions), and show a clearer
   prompt for each field (Aidan).
5. Add a UI path for the collection methods and print the playlist after
   each change (Aidan).
6. Final pass on Javadoc, style, and edge cases (Luke).

### Stretch goals

* **Committing to:** shuffle (it only needs `get(int)` and a random index, and
  fits in `PlaylistManager`) (Abhinav), and a simple GUI that replaces
  `ConsoleUI` and calls the same manager methods (Aidan).
* **Dropping:** the doubly linked list. The provided chain would have to be
  rewritten, and nothing in the app needs backwards traversal.

### New tests we will need

* Album field: `getAlbum` normal case, and null/blank album rejected by the
  `Song` constructor.
* Collection add/remove: normal case, empty array, null array, array with a
  null element (list must stay unchanged).
* Shuffle: normal case (same items, same size), empty list, one item.
* Optional-field prompts: blank accepted for optional fields, still rejected
  for required ones.
* GUI: manager-level tests stay as they are; add one scripted check that each
  button calls the right manager method.

### Who does what

| Item | Owner |
|---|---|
| Spec/README updates, design consistency | Riya |
| Album merge, collection methods, shuffle, manager tests | Abhinav |
| Optional fields, UI changes, GUI, UI tests | Aidan |
| Test plan upkeep, Javadoc/style, final review | Luke |

## Attribution

| File | Team member(s) | How |
|---|---|---|
| LinkedChain.java | CS 2114 staff | Provided; used unchanged |
| MediaItem.java | [Riya] | Wrote, GenAI-assisted; reviewed by [Luke] |
| Song.java | [Riya] | Wrote, GenAI-assisted; reviewed by [Luke] |
| PodcastEpisode.java | [Riya] | Wrote, GenAI-assisted; reviewed by [Luke] |
| AudiobookChapter.java | [Riya] | Wrote, GenAI-assisted; reviewed by [Luke] |
| MediaType.java | [Luke] | Wrote |
| InputValidator.java | [Luke] | Wrote, GenAI-assisted; reviewed by [Riya] |
| PlaylistManager.java | [Luke] | Wrote, GenAI-assisted; reviewed by [Riya] |
| ConsoleUI.java | [Luke] | Wrote, GenAI-assisted; reviewed by [Riya] |
| Main.java | [Abhinav] | Wrote |
| LinkedChainTest.java | [Abhinav] | Wrote tests from the spec's test plan; ran and verified |
| MediaItemTest.java | [Abhninav] | Wrote, GenAI-assisted edge cases; reviewed each case |
| InputValidatorTest.java | [Abhinav] | Wrote, GenAI-assisted edge cases; reviewed each case |
| PlaylistManagerTest.java | [Aidan]| Wrote, GenAI-assisted edge cases; reviewed each case |
| ConsoleUITest.java | [Aidan] | Wrote, GenAI-assisted edge cases; reviewed each case |
| README.md | [Aidan] | Drafted with GenAI assistance; revised by [Abhinav] |

### How we used GenAI

We used Claude to help draft code from our specification, to suggest edge-case tests (empty lists, removing something that isn't there, bad menu input, end of input), and to help write this README. Every suggested test was read by a team member, and we kept only the ones that checked behavior our spec actually promised. We ran the full suite ourselves (80 tests, all passing) and can explain what each test checks and why it exists. GenAI did not write the LinkedChain, which was provided and left unchanged.
