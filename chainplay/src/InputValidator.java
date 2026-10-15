/**
 * Turns raw console text into safe values or a clear error message.
 * Holds no state (only constants), so it can check any input in any order
 * and never touches the playlist.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class InputValidator
{
    /** Longest allowed text field. */
    public static final int MAX_TEXT_LENGTH = 100;
    /** Shortest allowed duration in seconds. */
    public static final int MIN_DURATION_SECONDS = 1;
    /** Longest allowed duration in seconds (10 hours). */
    public static final int MAX_DURATION_SECONDS = 36000;

    /**
     * The outcome of parsing: either a value or an error message.
     *
     * @param <T> the type of the parsed value
     */
    public static class Result<T>
    {
        private final T value;       // parsed value, or null if invalid
        private final String error;  // explanation, or null if valid

        private Result(T value, String error)
        {
            this.value = value;
            this.error = error;
        }

        /**
         * Builds a successful result.
         *
         * @param <T> value type
         * @param value the parsed value
         * @return a valid result
         */
        public static <T> Result<T> ok(T value)
        {
            return new Result<T>(value, null);
        }

        /**
         * Builds a failed result.
         *
         * @param <T> value type
         * @param message why the input was rejected
         * @return an invalid result
         */
        public static <T> Result<T> fail(String message)
        {
            return new Result<T>(null, message);
        }

        /** @return true if the input was accepted */
        public boolean isValid()
        {
            return error == null;
        }

        /** @return the parsed value, or null if invalid */
        public T getValue()
        {
            return value;
        }

        /** @return the error message, or null if valid */
        public String getError()
        {
            return error;
        }
    }

    /**
     * Trims text and collapses runs of whitespace to single spaces.
     *
     * @param raw the raw text; may be null
     * @return the normalized text, or "" for null
     */
    public static String normalizeText(String raw)
    {
        if (raw == null)
        {
            return "";
        }
        return raw.trim().replaceAll("\\s+", " ");
    }

    /**
     * Checks a text field (title, artist, genre, ...).
     *
     * @param raw the raw input
     * @return the normalized text, or an error message
     */
    public Result<String> parseText(String raw)
    {
        if (raw != null)
        {
            for (int i = 0; i < raw.length(); i++)
            {
                if (Character.isISOControl(raw.charAt(i)))
                {
                    return Result.fail("Text must not contain control "
                        + "characters such as tabs.");
                }
            }
        }
        String text = normalizeText(raw);
        if (text.isEmpty())
        {
            return Result.fail("Text cannot be empty.");
        }
        if (text.length() > MAX_TEXT_LENGTH)
        {
            return Result.fail("Text must be at most " + MAX_TEXT_LENGTH
                + " characters.");
        }
        return Result.ok(text);
    }

    /**
     * Checks a menu choice.
     *
     * @param raw the raw input
     * @param min lowest valid choice
     * @param max highest valid choice
     * @return the choice, or an error message
     */
    public Result<Integer> parseMenuChoice(String raw, int min, int max)
    {
        Integer n = parseWholeNumber(raw);
        if (n == null || n < min || n > max)
        {
            return Result.fail("Please enter a whole number from " + min
                + " to " + max + ".");
        }
        return Result.ok(n);
    }

    /**
     * Checks a 1-based position.
     *
     * @param raw the raw input
     * @param size number of items in the selected list
     * @return the position, or an error message
     */
    public Result<Integer> parsePosition(String raw, int size)
    {
        Integer n = parseWholeNumber(raw);
        if (n == null || n < 1 || n > size)
        {
            return Result.fail("Position must be a whole number from 1 to "
                + size + ".");
        }
        return Result.ok(n);
    }

    /**
     * Checks a duration written as m:ss and converts it to seconds.
     *
     * @param raw the raw input
     * @return the seconds, or an error message
     */
    public Result<Integer> parseDuration(String raw)
    {
        String msg = "Duration must be in m:ss format (for example 3:45) "
            + "between 0:01 and 600:00.";
        String text = normalizeText(raw);
        if (!text.matches("\\d{1,4}:[0-5]\\d"))
        {
            return Result.fail(msg);
        }
        int colon = text.indexOf(':');
        int seconds = Integer.parseInt(text.substring(0, colon)) * 60
            + Integer.parseInt(text.substring(colon + 1));
        if (seconds < MIN_DURATION_SECONDS || seconds > MAX_DURATION_SECONDS)
        {
            return Result.fail(msg);
        }
        return Result.ok(seconds);
    }

    /**
     * Checks a yes/no answer.
     *
     * @param raw the raw input
     * @return true for yes, false for no, or an error message
     */
    public Result<Boolean> parseYesNo(String raw)
    {
        String t = normalizeText(raw).toLowerCase();
        if (t.equals("y") || t.equals("yes"))
        {
            return Result.ok(Boolean.TRUE);
        }
        if (t.equals("n") || t.equals("no"))
        {
            return Result.ok(Boolean.FALSE);
        }
        return Result.fail("Please answer y or n.");
    }

    /**
     * Parses a whole number, or returns null if it is not one.
     * Catches overflow (such as 99999999999) instead of crashing.
     */
    private Integer parseWholeNumber(String raw)
    {
        String t = normalizeText(raw);
        if (!t.matches("\\d{1,9}"))
        {
            return null;
        }
        return Integer.valueOf(t);
    }
}
