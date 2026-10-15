/**
 * A song. The title is inherited; the artist is the creator for the
 * duplicate rule.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class Song extends MediaItem
{
    private final String artist;
    private final String albumTitle;
    private final String genre;

    /**
     * Creates a song.
     *
     * @param title the song title
     * @param artist the artist
     * @param albumTitle the album the song is from
     * @param genre the genre
     * @param durationSeconds the length in seconds, 1 to 36,000
     * @throws IllegalArgumentException if any value is invalid
     */
    public Song(String title, String artist, String albumTitle, String genre,
        int durationSeconds)
    {
        super(title, durationSeconds);
        this.artist = requireText(artist, "Artist");
        this.albumTitle = requireText(albumTitle, "Album");
        this.genre = requireText(genre, "Genre");
    }

    /**
     * Gets the artist.
     *
     * @return the artist
     */
    public String getArtist()
    {
        return artist;
    }

    /**
     * Gets the album title.
     *
     * @return the album title
     */
    public String getAlbum()
    {
        return albumTitle;
    }

    /**
     * Gets the genre.
     *
     * @return the genre
     */
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
    public String describe()
    {
        return getTitle() + " - " + artist + " (" + albumTitle + ", "
            + genre + ") [" + formatDuration() + "]";
    }

    @Override
    protected String[] getSearchableFields()
    {
        return new String[] {getTitle(), artist, albumTitle, genre};
    }
}
