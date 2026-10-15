import static org.junit.Assert.*;

import org.junit.Test;

/** Tests MediaItem, Song, PodcastEpisode and AudiobookChapter. */
public class MediaItemTest
{
    private static Song song(String t, String a)
    {
        return new Song(t, a, "Pop", 295);
    }

    private static PodcastEpisode ep(String t, String p)
    {
        return new PodcastEpisode(t, p, "Show", "About stuff", 600);
    }

    private static AudiobookChapter ch(String t, String a)
    {
        return new AudiobookChapter(t, a, "Smith", "Book", "Fantasy",
            "A chapter", 1200);
    }

    // ---- Song ----
    @Test
    public void songStoresValidValues()
    {
        Song s = new Song("Hello", "Adele", "Pop", 295);
        assertEquals("Hello", s.getTitle());
        assertEquals("Adele", s.getArtist());
        assertEquals("Adele", s.getCreator());
        assertEquals("Pop", s.getGenre());
        assertEquals(295, s.getDuration());
        assertEquals("Hello - Adele (Pop) [4:55]", s.describe());
        assertEquals(s.describe(), s.toString());
    }

    @Test
    public void songRejectsBadValues()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new Song("  ", "Adele", "Pop", 295));
        assertThrows(IllegalArgumentException.class,
            () -> new Song("Hello", null, "Pop", 295));
        assertThrows(IllegalArgumentException.class,
            () -> new Song("Hello", "Adele", "", 295));
        assertThrows(IllegalArgumentException.class,
            () -> new Song("Hello", "Adele", "Pop", 0));
        assertThrows(IllegalArgumentException.class,
            () -> new Song("Hello", "Adele", "Pop", 36001));
        assertThrows(IllegalArgumentException.class,
            () -> new Song(new String(new char[101]).replace('\0', 'x'),
                "Adele", "Pop", 295));
    }

    // ---- PodcastEpisode ----
    @Test
    public void episodeStoresValidValues()
    {
        PodcastEpisode e = new PodcastEpisode("Ep 1", "Joe", "The Show",
            "Chat", 600);
        assertEquals("Ep 1", e.getTitle());
        assertEquals("Joe", e.getPodcaster());
        assertEquals("Joe", e.getCreator());
        assertEquals("The Show", e.getPodcastTitle());
        assertEquals("Chat", e.getDescription());
        assertEquals("Ep 1 - The Show with Joe [10:00]", e.describe());
    }

    @Test
    public void episodeRejectsBadValues()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new PodcastEpisode(null, "Joe", "Show", "d", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new PodcastEpisode("Ep", " ", "Show", "d", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new PodcastEpisode("Ep", "Joe", null, "d", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new PodcastEpisode("Ep", "Joe", "Show", "", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new PodcastEpisode("Ep", "Joe", "Show", "d", -3));
    }

    // ---- AudiobookChapter ----
    @Test
    public void chapterStoresValidValues()
    {
        AudiobookChapter c = new AudiobookChapter("One", "Tolkien", "Smith",
            "The Hobbit", "Fantasy", "Start", 1500);
        assertEquals("One", c.getTitle());
        assertEquals("Tolkien", c.getAuthor());
        assertEquals("Tolkien", c.getCreator());
        assertEquals("Smith", c.getNarrator());
        assertEquals("The Hobbit", c.getBookTitle());
        assertEquals("Fantasy", c.getGenre());
        assertEquals("Start", c.getDescription());
        assertEquals("One - The Hobbit by Tolkien, read by Smith [25:00]",
            c.describe());
    }

    @Test
    public void chapterRejectsBadValues()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new AudiobookChapter("", "A", "N", "B", "G", "D", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new AudiobookChapter("T", null, "N", "B", "G", "D", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new AudiobookChapter("T", "A", " ", "B", "G", "D", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new AudiobookChapter("T", "A", "N", "", "G", "D", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new AudiobookChapter("T", "A", "N", "B", null, "D", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new AudiobookChapter("T", "A", "N", "B", "G", "", 60));
        assertThrows(IllegalArgumentException.class,
            () -> new AudiobookChapter("T", "A", "N", "B", "G", "D", 0));
    }

    // ---- equals / hashCode ----
    @Test
    public void equalsIgnoresCaseAndExtraSpaces()
    {
        Song a = song("Hello", "Adele");
        Song b = song("  hello ", "ADELE");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equalsRejectsDifferentCreatorNullAndOtherKind()
    {
        assertNotEquals(song("Hello", "Adele"), song("Hello", "Someone"));
        assertNotEquals(song("Hello", "Adele"), null);
        assertNotEquals(song("X", "Joe"), ep("X", "Joe"));
    }

    // ---- matches ----
    @Test
    public void matchesFindsTextInEveryField()
    {
        Song s = new Song("Hello", "Adele", "Pop", 295);
        assertTrue(s.matches("hel"));
        assertTrue(s.matches("ADEL"));
        assertTrue(s.matches("pop"));
        PodcastEpisode e = new PodcastEpisode("Ep", "Joe", "Show",
            "About cats", 60);
        assertTrue(e.matches("joe"));
        assertTrue(e.matches("show"));
        assertTrue(e.matches("cats"));
        AudiobookChapter c = ch("One", "Tolkien");
        assertTrue(c.matches("smith"));
        assertTrue(c.matches("book"));
        assertTrue(c.matches("fantasy"));
    }

    @Test
    public void matchesRejectsMissingTextAndNull()
    {
        assertFalse(song("Hello", "Adele").matches("zzz"));
        assertFalse(ep("Ep", "Joe").matches("zzz"));
        assertFalse(ch("One", "Tolkien").matches("zzz"));
        assertThrows(IllegalArgumentException.class,
            () -> song("Hello", "Adele").matches(null));
    }

    // ---- formatDuration ----
    @Test
    public void formatDurationFormatsMinutesAndSeconds()
    {
        assertEquals("3:45", MediaItem.formatDuration(225));
        assertEquals("0:05", MediaItem.formatDuration(5));
        assertEquals("600:00", MediaItem.formatDuration(36000));
    }

    @Test
    public void formatDurationRejectsNonPositive()
    {
        assertThrows(IllegalArgumentException.class,
            () -> MediaItem.formatDuration(0));
        assertThrows(IllegalArgumentException.class,
            () -> MediaItem.formatDuration(-4));
    }
}
