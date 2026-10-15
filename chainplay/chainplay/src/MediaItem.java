/**
 * Shared parent of every playable item. Immutable: fields are private and
 * final so equals and hashCode stay stable while an item is in a chain.
 * Two items are duplicates when they are the same kind and have the same
 * normalized title and creator (case and extra spaces ignored).
 *
 * @author ChainPlay team
 * @version October 2026
 */
public abstract class MediaItem
{
    private final String title;          // normalized, never blank
    private final int durationSeconds;   // 1 to 36,000

    /**
     * Creates an item after validating its fields.
     *
     * @param title the title
     * @param durationSeconds the length in seconds
     * @throws IllegalArgumentException if either is invalid
     */
    protected MediaItem(String title, int durationSeconds)
    {
        this.title = requireText(title, "title");
        if (durationSeconds < InputValidator.MIN_DURATION_SECONDS
            || durationSeconds > InputValidator.MAX_DURATION_SECONDS)
        {
            throw new IllegalArgumentException("Duration must be between "
                + InputValidator.MIN_DURATION_SECONDS + " and "
                + InputValidator.MAX_DURATION_SECONDS + " seconds");
        }
        this.durationSeconds = durationSeconds;
    }

    /**
     * Normalizes a text field and rejects null, blank, or too-long values.
     *
     * @param value the raw text
     * @param fieldName name used in the error message
     * @return the normalized text
     * @throws IllegalArgumentException if the text is invalid
     */
    protected static String requireText(String value, String fieldName)
    {
        String text = InputValidator.normalizeText(value);
        if (text.isEmpty())
        {
            throw new IllegalArgumentException(fieldName
                + " must not be null or blank");
        }
        if (text.length() > InputValidator.MAX_TEXT_LENGTH)
        {
            throw new IllegalArgumentException(fieldName
                + " must be at most " + InputValidator.MAX_TEXT_LENGTH
                + " characters");
        }
        return text;
    }

    /** @return the title */
    public String getTitle()
    {
        return title;
    }

    /** @return the duration in seconds */
    public int getDuration()
    {
        return durationSeconds;
    }

    /** @return the creator used by the duplicate rule */
    public abstract String getCreator();

    /** @return every text field, used by matches() */
    protected abstract String[] searchableFields();

    /** @return a one-line description for display */
    public abstract String describe();

    /**
     * Tests whether the query appears in any field of this item,
     * ignoring case.
     *
     * @param query the text to look for
     * @return true if some field contains the query
     * @throws IllegalArgumentException if query is null
     */
    public boolean matches(String query)
    {
        if (query == null)
        {
            throw new IllegalArgumentException("Query must not be null");
        }
        String q = InputValidator.normalizeText(query).toLowerCase();
        for (String field : searchableFields())
        {
            if (field.toLowerCase().contains(q))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Formats seconds as m:ss.
     *
     * @param seconds a positive number of seconds
     * @return text such as "3:45"
     * @throws IllegalArgumentException if seconds is not positive
     */
    public static String formatDuration(int seconds)
    {
        if (seconds <= 0)
        {
            throw new IllegalArgumentException(
                "Duration must be positive: " + seconds);
        }
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || getClass() != other.getClass())
        {
            return false;
        }
        MediaItem item = (MediaItem) other;
        return title.equalsIgnoreCase(item.title)
            && getCreator().equalsIgnoreCase(item.getCreator());
    }

    @Override
    public int hashCode()
    {
        return java.util.Objects.hash(getClass(), title.toLowerCase(),
            getCreator().toLowerCase());
    }

    @Override
    public String toString()
    {
        return describe();
    }
}
