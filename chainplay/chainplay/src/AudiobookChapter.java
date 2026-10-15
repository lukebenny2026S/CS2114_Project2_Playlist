/**
 * One chapter of an audiobook. The creator for the duplicate rule is the
 * author.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class AudiobookChapter extends MediaItem
{
    private final String bookTitle;   // book name
    private final String author;      // writer
    private final String narrator;    // reader
    private final String genre;       // style
    private final String description; // short summary

    /**
     * Creates a chapter.
     *
     * @param title the chapter title
     * @param author the author
     * @param narrator the narrator
     * @param bookTitle the book name
     * @param genre the genre
     * @param description a short summary
     * @param durationSeconds length in seconds
     * @throws IllegalArgumentException if any field is invalid
     */
    public AudiobookChapter(String title, String author, String narrator,
        String bookTitle, String genre, String description,
        int durationSeconds)
    {
        super(title, durationSeconds);
        this.author = requireText(author, "author");
        this.narrator = requireText(narrator, "narrator");
        this.bookTitle = requireText(bookTitle, "bookTitle");
        this.genre = requireText(genre, "genre");
        this.description = requireText(description, "description");
    }

    /** @return the book name */
    public String getBookTitle()
    {
        return bookTitle;
    }

    /** @return the author */
    public String getAuthor()
    {
        return author;
    }

    /** @return the narrator */
    public String getNarrator()
    {
        return narrator;
    }

    /** @return the genre */
    public String getGenre()
    {
        return genre;
    }

    /** @return the summary */
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
    protected String[] searchableFields()
    {
        return new String[] {getTitle(), author, narrator, bookTitle,
            genre, description};
    }

    @Override
    public String describe()
    {
        return getTitle() + " - " + bookTitle + " by " + author
            + ", read by " + narrator + " ["
            + formatDuration(getDuration()) + "]";
    }
}
