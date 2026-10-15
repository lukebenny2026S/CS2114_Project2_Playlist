/**
 * A podcast episode. The inherited title is the episode title; the
 * podcaster is the creator for the duplicate rule.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class PodcastEpisode extends MediaItem
{
    private final String podcastTitle;
    private final String podcaster;
    private final String description;

    /**
     * Creates a podcast episode.
     *
     * @param title the episode title
     * @param podcastTitle the podcast the episode belongs to
     * @param podcaster the person or group who makes the podcast
     * @param description a short description
     * @param durationSeconds the length in seconds, 1 to 36,000
     * @throws IllegalArgumentException if any value is invalid
     */
    public PodcastEpisode(String title, String podcastTitle, String podcaster,
        String description, int durationSeconds)
    {
        super(title, durationSeconds);
        this.podcastTitle = requireText(podcastTitle, "Podcast title");
        this.podcaster = requireText(podcaster, "Podcaster");
        this.description = requireText(description, "Description");
    }

    /**
     * Gets the podcast title.
     *
     * @return the podcast title
     */
    public String getPodcastTitle()
    {
        return podcastTitle;
    }

    /**
     * Gets the podcaster.
     *
     * @return the podcaster
     */
    public String getPodcaster()
    {
        return podcaster;
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
        return podcaster;
    }

    @Override
    public String describe()
    {
        return getTitle() + " - " + podcastTitle + " with " + podcaster
            + " [" + formatDuration() + "]";
    }

    @Override
    protected String[] getSearchableFields()
    {
        return new String[] {getTitle(), podcastTitle, podcaster, description};
    }
}
