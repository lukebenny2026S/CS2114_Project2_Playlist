/**
 * A song. The creator for the duplicate rule is the artist.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class Song extends MediaItem
{
    private final String artist; // performer
    private final String genre;  // style

    /**
     * Creates a song.
     *
     * @param title the song title
     * @param artist the artist
     * @param genre the genre
     * @param durationSeconds length in seconds
     * @throws IllegalArgumentException if any field is invalid
     */
    public Song(String title, String artist, String genre,
        int durationSeconds)
    {
        super(title, durationSeconds);
        this.artist = requireText(artist, "artist");
        this.genre = requireText(genre, "genre");
    }

    /** @return the artist */
    public String getArtist()
    {
        return artist;
    }

    /** @return the genre */
    public String getGenre()
    {
        return genre;
    }

    @Override
    public String getCreator()
    {
        return artist;
    }

    @Override
    protected String[] searchableFields()
    {
        return new String[] {getTitle(), artist, genre};
    }

    @Override
    public String describe()
    {
        return getTitle() + " - " + artist + " (" + genre + ") ["
            + formatDuration(getDuration()) + "]";
    }
}
