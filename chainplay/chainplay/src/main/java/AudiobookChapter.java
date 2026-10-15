/**
 * An audiobook chapter. The inherited title is the chapter title; the
 * author is the creator for the duplicate rule.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class AudiobookChapter extends MediaItem
{
    private final String bookTitle;
    private final String author;
    private final String narrator;
    private final String genre;
    private final String description;

    /**
     * Creates an audiobook chapter.
     *
     * @param title the chapter title
     * @param bookTitle the book the chapter belongs to
     * @param author the author
     * @param narrator the narrator
     * @param genre the genre
     * @param description a short description
     * @param durationSeconds the length in seconds, 1 to 36,000
     * @throws IllegalArgumentException if any value is invalid
     */
    public AudiobookChapter(String title, String bookTitle, String author,
        String narrator, String genre, String description,
        int durationSeconds)
    {
        super(title, durationSeconds);
        this.bookTitle = requireText(bookTitle, "Book title");
        this.author = requireText(author, "Author");
        this.narrator = requireText(narrator, "Narrator");
        this.genre = requireText(genre, "Genre");
        this.description = requireText(description, "Description");
    }

    /**
     * Gets the book title.
     *
     * @return the book title
     */
    public String getBookTitle()
    {
        return bookTitle;
    }

    /**
     * Gets the author.
     *
     * @return the author
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Gets the narrator.
     *
     * @return the narrator
     */
    public String getNarrator()
    {
        return narrator;
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

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getCreator()
    {
        return author;
    }

    @Override
    public String describe()
    {
        return getTitle() + " - " + bookTitle + " by " + author
            + ", narrated by " + narrator + " [" + formatDuration() + "]";
    }

    @Override
    protected String[] getSearchableFields()
    {
        return new String[] {getTitle(), bookTitle, author, narrator, genre,
            description};
    }
}
