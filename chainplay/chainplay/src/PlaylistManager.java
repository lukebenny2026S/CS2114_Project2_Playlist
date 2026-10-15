import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * All playlist logic. Owns one LinkedChain per item type (songs, podcast
 * episodes, audiobook chapters), converts 1-based positions to chain
 * operations, checks duplicates, runs searches, and formats text.
 * It never reads input or prints. It talks to the chains only through
 * their public methods.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class PlaylistManager
{
    private final LinkedChain<Song> songs = new LinkedChain<Song>();
    private final LinkedChain<PodcastEpisode> episodes =
        new LinkedChain<PodcastEpisode>();
    private final LinkedChain<AudiobookChapter> chapters =
        new LinkedChain<AudiobookChapter>();

    /**
     * Adds a song to the end of the song list.
     *
     * @param song the song
     * @return true if added
     * @throws IllegalArgumentException if song is null
     */
    public boolean addSong(Song song)
    {
        return songs.addToEnd(song);
    }

    /**
     * Adds an episode to the end of the episode list.
     *
     * @param episode the episode
     * @return true if added
     * @throws IllegalArgumentException if episode is null
     */
    public boolean addEpisode(PodcastEpisode episode)
    {
        return episodes.addToEnd(episode);
    }

    /**
     * Adds a chapter to the end of the chapter list.
     *
     * @param chapter the chapter
     * @return true if added
     * @throws IllegalArgumentException if chapter is null
     */
    public boolean addChapter(AudiobookChapter chapter)
    {
        return chapters.addToEnd(chapter);
    }

    /**
     * Tests whether an equal song (same title and artist) is stored.
     *
     * @param song the song to look for
     * @return true if a duplicate exists
     * @throws IllegalArgumentException if song is null
     */
    public boolean isDuplicate(Song song)
    {
        requireNonNull(song);
        return songs.contains(song);
    }

    /**
     * Tests whether an equal episode (same title and podcaster) is stored.
     *
     * @param episode the episode to look for
     * @return true if a duplicate exists
     * @throws IllegalArgumentException if episode is null
     */
    public boolean isDuplicate(PodcastEpisode episode)
    {
        requireNonNull(episode);
        return episodes.contains(episode);
    }

    /**
     * Tests whether an equal chapter (same title and author) is stored.
     *
     * @param chapter the chapter to look for
     * @return true if a duplicate exists
     * @throws IllegalArgumentException if chapter is null
     */
    public boolean isDuplicate(AudiobookChapter chapter)
    {
        requireNonNull(chapter);
        return chapters.contains(chapter);
    }

    /**
     * Gets how many items of one type are stored.
     *
     * @param type the type
     * @return the count
     * @throws IllegalArgumentException if type is null
     */
    public int size(MediaType type)
    {
        requireNonNull(type);
        switch (type)
        {
            case SONG:
                return songs.size();
            case EPISODE:
                return episodes.size();
            default:
                return chapters.size();
        }
    }

    /**
     * Deletes the item at a 1-based position within one type's list.
     * The list is unchanged if the position is invalid.
     *
     * @param type the list to delete from
     * @param position 1-based position
     * @return the removed item
     * @throws IllegalArgumentException if type is null or position is
     *         outside 1..size
     */
    public MediaItem deleteAt(MediaType type, int position)
    {
        requireNonNull(type);
        switch (type)
        {
            case SONG:
                return removeAt(songs, Song[]::new, position);
            case EPISODE:
                return removeAt(episodes, PodcastEpisode[]::new, position);
            default:
                return removeAt(chapters, AudiobookChapter[]::new, position);
        }
    }

    /**
     * Finds songs with a field containing the query (ignoring case).
     *
     * @param query the text to find
     * @return a new chain of matches in playlist order; empty if none
     * @throws IllegalArgumentException if query is null
     */
    public LinkedChain<Song> searchSongs(String query)
    {
        requireNonNull(query);
        return filter(songs, Song[]::new, s -> s.matches(query));
    }

    /**
     * Finds episodes with a field containing the query (ignoring case).
     *
     * @param query the text to find
     * @return a new chain of matches in playlist order; empty if none
     * @throws IllegalArgumentException if query is null
     */
    public LinkedChain<PodcastEpisode> searchEpisodes(String query)
    {
        requireNonNull(query);
        return filter(episodes, PodcastEpisode[]::new, e -> e.matches(query));
    }

    /**
     * Finds chapters with a field containing the query (ignoring case).
     *
     * @param query the text to find
     * @return a new chain of matches in playlist order; empty if none
     * @throws IllegalArgumentException if query is null
     */
    public LinkedChain<AudiobookChapter> searchChapters(String query)
    {
        requireNonNull(query);
        return filter(chapters, AudiobookChapter[]::new,
            c -> c.matches(query));
    }

    /**
     * Finds songs no longer than a limit.
     *
     * @param maxSeconds the limit in seconds (at least 1)
     * @return a new chain of matches in playlist order
     * @throws IllegalArgumentException if maxSeconds is below 1
     */
    public LinkedChain<Song> searchSongsByDuration(int maxSeconds)
    {
        requireLimit(maxSeconds);
        return filter(songs, Song[]::new, s -> s.getDuration() <= maxSeconds);
    }

    /**
     * Finds episodes no longer than a limit.
     *
     * @param maxSeconds the limit in seconds (at least 1)
     * @return a new chain of matches in playlist order
     * @throws IllegalArgumentException if maxSeconds is below 1
     */
    public LinkedChain<PodcastEpisode> searchEpisodesByDuration(
        int maxSeconds)
    {
        requireLimit(maxSeconds);
        return filter(episodes, PodcastEpisode[]::new,
            e -> e.getDuration() <= maxSeconds);
    }

    /**
     * Finds chapters no longer than a limit.
     *
     * @param maxSeconds the limit in seconds (at least 1)
     * @return a new chain of matches in playlist order
     * @throws IllegalArgumentException if maxSeconds is below 1
     */
    public LinkedChain<AudiobookChapter> searchChaptersByDuration(
        int maxSeconds)
    {
        requireLimit(maxSeconds);
        return filter(chapters, AudiobookChapter[]::new,
            c -> c.getDuration() <= maxSeconds);
    }

    /**
     * Formats the whole playlist: three sections, numbered from 1, with
     * "(empty)" for a section with no items.
     *
     * @return the text
     */
    public String formatPlaylist()
    {
        return "Songs:\n" + formatSection(songs)
            + "Podcast episodes:\n" + formatSection(episodes)
            + "Audiobook chapters:\n" + formatSection(chapters);
    }

    /**
     * Formats search results as numbered lines.
     *
     * @param <T> the item type
     * @param items the results
     * @return numbered lines, or "No matches found." if empty
     * @throws IllegalArgumentException if items is null
     */
    public <T extends MediaItem> String formatList(LinkedChain<T> items)
    {
        requireNonNull(items);
        if (items.isEmpty())
        {
            return "No matches found.\n";
        }
        return numbered(items);
    }

    // ---- private helpers -------------------------------------------

    private <T extends MediaItem> String formatSection(LinkedChain<T> items)
    {
        return items.isEmpty() ? "  (empty)\n" : numbered(items);
    }

    private <T extends MediaItem> String numbered(LinkedChain<T> items)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++)
        {
            sb.append("  ").append(i + 1).append(". ")
                .append(items.get(i).describe()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Builds a new chain of the entries that pass the test, keeping order.
     * Walks a snapshot array backwards and adds at the front, so each add
     * is O(1) and the whole filter is O(n).
     */
    private <T> LinkedChain<T> filter(LinkedChain<T> source,
        IntFunction<T[]> arrayMaker, Predicate<? super T> test)
    {
        T[] snapshot = source.toArray(arrayMaker.apply(source.size()));
        LinkedChain<T> result = new LinkedChain<T>();
        for (int i = snapshot.length - 1; i >= 0; i--)
        {
            if (test.test(snapshot[i]))
            {
                result.add(snapshot[i]);
            }
        }
        return result;
    }

    /**
     * Removes the entry at a 1-based position. The provided chain only
     * removes by value (the first equal entry), which could take out a
     * different duplicate, so this rebuilds the chain without the chosen
     * entry. The position is validated before anything changes.
     */
    private <T> T removeAt(LinkedChain<T> chain, IntFunction<T[]> arrayMaker,
        int position)
    {
        if (position < 1 || position > chain.size())
        {
            throw new IllegalArgumentException("Position " + position
                + " is not between 1 and " + chain.size());
        }
        T[] snapshot = chain.toArray(arrayMaker.apply(chain.size()));
        T removed = snapshot[position - 1];
        chain.clear();
        for (int i = snapshot.length - 1; i >= 0; i--)
        {
            if (i != position - 1)
            {
                chain.add(snapshot[i]);
            }
        }
        return removed;
    }

    private void requireNonNull(Object value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("Argument must not be null");
        }
    }

    private void requireLimit(int maxSeconds)
    {
        if (maxSeconds < 1)
        {
            throw new IllegalArgumentException(
                "Duration limit must be at least 1 second");
        }
    }
}
