import java.util.Locale;

/**
 * The shared parent of every playable item. Holds the title and duration,
 * validates them, and defines equality using the duplicate rule: same
 * normalized title and creator (case and extra spaces are ignored).
 *
 * Instances are immutable so equals and hashCode stay stable while an item
 * sits in a LinkedChain.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public abstract class MediaItem
{
    /** The longest text value any field may hold. */
    public static final int MAX_TEXT_LENGTH = 100;
    /** The shortest allowed duration in seconds. */
    public static final int MIN_DURATION_SECONDS = 1;
    /** The longest allowed duration in seconds (10 hours). */
    public static final int MAX_DURATION_SECONDS = 36000;

    private final String title;           // normalized, never blank
    private final int durationSeconds;    // 1 to 36,000

    /**
     * Creates a media item.
     *
     * @param title the title; must be non-blank and at most 100 characters
     * @param durationSeconds the length in seconds, 1 to 36,000
     * @throws IllegalArgumentException if either value is invalid
     */
    protected MediaItem(String title, int durationSeconds)
    {
        this.title = requireText(title, "Title");
        this.durationSeconds = requireDuration(durationSeconds);
    }

    /**
     * Trims a string and collapses runs of whitespace to one space.
     *
     * @param value the text, possibly null
     * @return the normalized text, or "" for null
     */
    public static String normalize(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Normalizes and checks a text field.
     *
     * @param value the raw text
     * @param fieldName the field name used in the error message
     * @return the normalized text
     * @throws IllegalArgumentException if null, blank, or too long
     */
    protected static String requireText(String value, String fieldName)
    {
        if (value == null)
        {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String normalized = normalize(value);
        if (normalized.isEmpty())
        {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (normalized.length() > MAX_TEXT_LENGTH)
        {
            throw new IllegalArgumentException(fieldName + " must be at most "
                + MAX_TEXT_LENGTH + " characters");
        }
        return normalized;
    }

    /**
     * Checks a duration.
     *
     * @param seconds the duration in seconds
     * @return the same value
     * @throws IllegalArgumentException if outside 1 to 36,000
     */
    protected static int requireDuration(int seconds)
    {
        if (seconds < MIN_DURATION_SECONDS || seconds > MAX_DURATION_SECONDS)
        {
            throw new IllegalArgumentException("Duration must be from "
                + MIN_DURATION_SECONDS + " to " + MAX_DURATION_SECONDS
                + " seconds");
        }
        return seconds;
    }

    /**
     * Formats seconds as m:ss, for example 225 becomes "3:45".
     *
     * @param seconds the duration, 1 to 36,000
     * @return the formatted duration
     * @throws IllegalArgumentException if seconds is out of range
     */
    public static String formatDuration(int seconds)
    {
        requireDuration(seconds);
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return minutes + ":" + (remainder < 10 ? "0" : "") + remainder;
    }

    /**
     * Gets the title.
     *
     * @return the normalized title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Gets the duration.
     *
     * @return the duration in seconds
     */
    public int getDuration()
    {
        return durationSeconds;
    }

    /**
     * Formats this item's duration as m:ss.
     *
     * @return the formatted duration
     */
    public String formatDuration()
    {
        return formatDuration(durationSeconds);
    }

    /**
     * Gets the creator used by the duplicate rule (artist, podcaster, or
     * author).
     *
     * @return the creator's name
     */
    public abstract String getCreator();

    /**
     * Builds a one-line description for display.
     *
     * @return the description
     */
    public abstract String describe();

    /**
     * Lists every text field a search should look at, title included.
     *
     * @return the searchable text fields
     */
    protected abstract String[] getSearchableFields();

    /**
     * Tests whether the query appears (ignoring case) in any text field.
     *
     * @param query the search text; must not be null or blank
     * @return true if any field contains the query
     * @throws IllegalArgumentException if the query is null or blank
     */
    public boolean matches(String query)
    {
        if (query == null || normalize(query).isEmpty())
        {
            throw new IllegalArgumentException("Search text must not be blank");
        }
        String wanted = normalize(query).toLowerCase(Locale.ROOT);
        for (String field : getSearchableFields())
        {
            if (field.toLowerCase(Locale.ROOT).contains(wanted))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Two items are equal when they are the same kind of item with the same
     * normalized title and creator, ignoring case.
     *
     * @param other the object to compare
     * @return true if the items are duplicates of each other
     */
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
        MediaItem that = (MediaItem)other;
        return duplicateKey().equals(that.duplicateKey());
    }

    /**
     * Hash code consistent with equals.
     *
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        return duplicateKey().hashCode();
    }

    /**
     * Returns the one-line description.
     *
     * @return the description
     */
    @Override
    public String toString()
    {
        return describe();
    }

    private String duplicateKey()
    {
        return title.toLowerCase(Locale.ROOT) + "|"
            + getCreator().toLowerCase(Locale.ROOT);
    }
}
