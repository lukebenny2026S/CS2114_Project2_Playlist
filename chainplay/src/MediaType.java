/**
 * The three kinds of item ChainPlay stores; used to pick a section.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public enum MediaType
{
    /** A song. */
    SONG("song"),
    /** A podcast episode. */
    EPISODE("podcast episode"),
    /** An audiobook chapter. */
    CHAPTER("audiobook chapter");

    private final String label; // lower-case name for messages

    MediaType(String label)
    {
        this.label = label;
    }

    /** @return a lower-case name for messages */
    public String getLabel()
    {
        return label;
    }
}
