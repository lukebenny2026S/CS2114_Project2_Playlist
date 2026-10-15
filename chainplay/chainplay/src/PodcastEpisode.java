/**
 * A podcast episode. The creator for the duplicate rule is the podcaster.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class PodcastEpisode extends MediaItem
{
    private final String podcastTitle; // show name
    private final String podcaster;    // host
    private final String description;  // short summary

    /**
     * Creates an episode.
     *
     * @param title the episode title
     * @param podcaster the host
     * @param podcastTitle the show name
     * @param description a short summary
     * @param durationSeconds length in seconds
     * @throws IllegalArgumentException if any field is invalid
     */
    public PodcastEpisode(String title, String podcaster,
        String podcastTitle, String description, int durationSeconds)
    {
        super(title, durationSeconds);
        this.podcaster = requireText(podcaster, "podcaster");
        this.podcastTitle = requireText(podcastTitle, "podcastTitle");
        this.description = requireText(description, "description");
    }

    /** @return the show name */
    public String getPodcastTitle()
    {
        return podcastTitle;
    }

    /** @return the host */
    public String getPodcaster()
    {
        return podcaster;
    }

    /** @return the summary */
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getCreator()
    {
        return podcaster;
    }

    @Override
    protected String[] searchableFields()
    {
        return new String[] {getTitle(), podcaster, podcastTitle,
            description};
    }

    @Override
    public String describe()
    {
        return getTitle() + " - " + podcastTitle + " with " + podcaster
            + " [" + formatDuration(getDuration()) + "]";
    }
}
