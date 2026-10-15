import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/** Tests PlaylistManager. */
public class PlaylistManagerTest
{
    private PlaylistManager m;

    private static Song song(String t, String a, int secs)
    {
        return new Song(t, a, "Pop", secs);
    }

    private static PodcastEpisode ep(String t, String p, int secs)
    {
        return new PodcastEpisode(t, p, "Show", "About", secs);
    }

    private static AudiobookChapter ch(String t, String a, String n)
    {
        return new AudiobookChapter(t, a, n, "Book", "Fantasy", "Desc", 900);
    }

    @Before
    public void setUp()
    {
        m = new PlaylistManager();
    }

    // ---- add / size ----
    @Test
    public void addSongGrowsOnlySongs()
    {
        assertTrue(m.addSong(song("Hello", "Adele", 295)));
        assertEquals(1, m.size(MediaType.SONG));
        assertEquals(0, m.size(MediaType.EPISODE));
        assertEquals(0, m.size(MediaType.CHAPTER));
    }

    @Test
    public void addNullThrowsAndChangesNothing()
    {
        assertThrows(IllegalArgumentException.class, () -> m.addSong(null));
        assertThrows(IllegalArgumentException.class, () -> m.addEpisode(null));
        assertThrows(IllegalArgumentException.class, () -> m.addChapter(null));
        assertEquals(0, m.size(MediaType.SONG));
    }

    @Test
    public void addEpisodeAndChapterGoToTheirOwnLists()
    {
        m.addEpisode(ep("Ep", "Joe", 60));
        m.addChapter(ch("One", "Tolkien", "Smith"));
        assertEquals(1, m.size(MediaType.EPISODE));
        assertEquals(1, m.size(MediaType.CHAPTER));
        assertEquals(0, m.size(MediaType.SONG));
    }

    @Test
    public void sizeIsZeroForNewManagerAndTwoAfterTwoAdds()
    {
        for (MediaType t : MediaType.values())
        {
            assertEquals(0, m.size(t));
        }
        m.addSong(song("A", "x", 60));
        m.addSong(song("B", "x", 60));
        assertEquals(2, m.size(MediaType.SONG));
    }

    @Test
    public void sizeNullThrows()
    {
        assertThrows(IllegalArgumentException.class, () -> m.size(null));
    }

    // ---- isDuplicate ----
    @Test
    public void isDuplicateIgnoresCaseAndSpaces()
    {
        m.addSong(song("Hello", "Adele", 295));
        assertTrue(m.isDuplicate(song("  hello ", "ADELE", 100)));
        assertFalse(m.isDuplicate(song("Hello", "Someone Else", 295)));
    }

    @Test
    public void isDuplicateWorksForEpisodesAndChapters()
    {
        m.addEpisode(ep("Ep", "Joe", 60));
        m.addChapter(ch("One", "Tolkien", "Smith"));
        assertTrue(m.isDuplicate(ep("ep", "JOE", 99)));
        assertTrue(m.isDuplicate(ch("one", "tolkien", "Other")));
        assertFalse(m.isDuplicate(ep("Ep", "Sam", 60)));
        assertFalse(m.isDuplicate(ch("One", "Other", "Smith")));
    }

    @Test
    public void isDuplicateNullThrows()
    {
        assertThrows(IllegalArgumentException.class,
            () -> m.isDuplicate((Song) null));
        assertThrows(IllegalArgumentException.class,
            () -> m.isDuplicate((PodcastEpisode) null));
        assertThrows(IllegalArgumentException.class,
            () -> m.isDuplicate((AudiobookChapter) null));
    }

    // ---- deleteAt ----
    @Test
    public void deleteAtRemovesChosenPosition()
    {
        m.addSong(song("A", "x", 60));
        m.addSong(song("B", "x", 60));
        m.addSong(song("C", "x", 60));
        assertEquals("B", m.deleteAt(MediaType.SONG, 2).getTitle());
        assertEquals(2, m.size(MediaType.SONG));
        assertEquals("A", m.searchSongs("A").remove().getTitle());
        assertEquals(1, m.searchSongs("C").size());
        assertEquals(0, m.searchSongs("B").size());
    }

    @Test
    public void deleteAtFirstAndLastKeepsOrderAndChainUsable()
    {
        m.addSong(song("A", "x", 60));
        m.addSong(song("B", "x", 60));
        m.addSong(song("C", "x", 60));
        m.deleteAt(MediaType.SONG, 1);
        m.deleteAt(MediaType.SONG, 1);
        assertEquals("C", m.deleteAt(MediaType.SONG, 1).getTitle());
        assertEquals(0, m.size(MediaType.SONG));
        m.addSong(song("D", "x", 60));
        assertEquals(1, m.size(MediaType.SONG));
    }

    @Test
    public void deleteAtRemovesTheChosenDuplicateNotTheFirst()
    {
        m.addSong(new Song("Same", "Artist", "Pop", 100));
        m.addSong(new Song("Same", "Artist", "Rock", 200));
        m.deleteAt(MediaType.SONG, 2);
        assertEquals("Pop", m.searchSongs("same").get(0).getGenre());
    }

    @Test
    public void deleteAtBadPositionThrowsAndKeepsList()
    {
        m.addSong(song("A", "x", 60));
        m.addSong(song("B", "x", 60));
        assertThrows(IllegalArgumentException.class,
            () -> m.deleteAt(MediaType.SONG, 0));
        assertThrows(IllegalArgumentException.class,
            () -> m.deleteAt(MediaType.SONG, 3));
        assertThrows(IllegalArgumentException.class,
            () -> m.deleteAt(MediaType.EPISODE, 1));
        assertThrows(IllegalArgumentException.class,
            () -> m.deleteAt(null, 1));
        assertEquals(2, m.size(MediaType.SONG));
    }

    @Test
    public void deleteAtWorksForEpisodesAndChapters()
    {
        m.addEpisode(ep("Ep", "Joe", 60));
        m.addChapter(ch("One", "Tolkien", "Smith"));
        m.deleteAt(MediaType.EPISODE, 1);
        m.deleteAt(MediaType.CHAPTER, 1);
        assertEquals(0, m.size(MediaType.EPISODE));
        assertEquals(0, m.size(MediaType.CHAPTER));
    }

    // ---- searches ----
    @Test
    public void searchSongsReturnsMatchesInOrder()
    {
        m.addSong(song("Hello", "Adele", 295));
        m.addSong(song("Other", "Band", 100));
        m.addSong(song("Hellfire", "Band", 100));
        LinkedChain<Song> r = m.searchSongs("hel");
        assertEquals(2, r.size());
        assertEquals("Hello", r.get(0).getTitle());
        assertEquals("Hellfire", r.get(1).getTitle());
    }

    @Test
    public void searchSongsNoMatchIsEmptyAndNullThrows()
    {
        m.addSong(song("Hello", "Adele", 295));
        assertTrue(m.searchSongs("zzz").isEmpty());
        assertThrows(IllegalArgumentException.class,
            () -> m.searchSongs(null));
    }

    @Test
    public void searchEpisodesAndChapters()
    {
        m.addEpisode(ep("Ep", "Joe Rogan", 60));
        m.addChapter(ch("One", "Tolkien", "Mr Smith"));
        assertEquals(1, m.searchEpisodes("joe").size());
        assertTrue(m.searchEpisodes("zzz").isEmpty());
        assertEquals(1, m.searchChapters("smith").size());
        assertTrue(m.searchChapters("zzz").isEmpty());
        assertThrows(IllegalArgumentException.class,
            () -> m.searchEpisodes(null));
        assertThrows(IllegalArgumentException.class,
            () -> m.searchChapters(null));
    }

    @Test
    public void durationSearchesReturnItemsAtOrUnderLimit()
    {
        m.addSong(song("A", "x", 300));
        m.addSong(song("B", "x", 180));
        m.addSong(song("C", "x", 240));
        LinkedChain<Song> r = m.searchSongsByDuration(240);
        assertEquals(2, r.size());
        assertEquals("B", r.get(0).getTitle());
        assertEquals("C", r.get(1).getTitle());
        m.addEpisode(ep("E1", "Joe", 100));
        m.addEpisode(ep("E2", "Joe", 500));
        assertEquals(1, m.searchEpisodesByDuration(240).size());
        m.addChapter(ch("One", "T", "S"));
        assertEquals(1, m.searchChaptersByDuration(900).size());
        assertEquals(0, m.searchChaptersByDuration(899).size());
    }

    @Test
    public void durationSearchesRejectBadLimits()
    {
        assertThrows(IllegalArgumentException.class,
            () -> m.searchSongsByDuration(0));
        assertThrows(IllegalArgumentException.class,
            () -> m.searchEpisodesByDuration(-5));
        assertThrows(IllegalArgumentException.class,
            () -> m.searchChaptersByDuration(0));
    }

    // ---- formatting ----
    @Test
    public void formatPlaylistNumbersItemsAndMarksEmptySections()
    {
        m.addSong(song("A", "x", 60));
        m.addSong(song("B", "x", 60));
        String text = m.formatPlaylist();
        assertTrue(text.contains("1. A - x"));
        assertTrue(text.contains("2. B - x"));
        assertEquals(2, text.split("\\(empty\\)", -1).length - 1);
    }

    @Test
    public void formatPlaylistEmptyShowsThreeEmptySections()
    {
        String text = m.formatPlaylist();
        assertEquals(3, text.split("\\(empty\\)", -1).length - 1);
    }

    @Test
    public void formatListNumbersResultsAndHandlesEmptyOrNull()
    {
        m.addSong(song("A", "x", 60));
        m.addSong(song("B", "x", 60));
        String text = m.formatList(m.searchSongs("x"));
        assertTrue(text.contains("1."));
        assertTrue(text.contains("2."));
        assertTrue(m.formatList(m.searchSongs("zzz"))
            .contains("No matches found."));
        assertThrows(IllegalArgumentException.class,
            () -> m.formatList((LinkedChain<Song>) null));
    }
}
