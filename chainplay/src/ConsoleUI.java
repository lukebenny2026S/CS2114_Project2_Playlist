import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Function;

/**
 * The only class that reads the console or prints. Runs the menu loop,
 * re-prompts through the InputValidator until input is valid, asks the
 * duplicate confirmation, calls the PlaylistManager, and prints the
 * playlist after every change. End of input (Ctrl+D) exits cleanly.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class ConsoleUI
{
    private static final int MENU_MAX = 7;
    private static final String MENU =
        "\n=== ChainPlay ===\n"
        + "1. Add song\n2. Add podcast episode\n3. Add audiobook chapter\n"
        + "4. Delete item\n5. Search\n6. Show playlist\n7. Exit\n";

    private final Scanner in;
    private final PrintStream out;
    private final PlaylistManager manager;
    private final InputValidator validator;

    /** Thrown internally when input runs out. */
    private static class EndOfInput extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Creates the UI. Input and output are passed in so tests can script
     * them.
     *
     * @param in where input comes from
     * @param out where messages go
     * @param manager the playlist logic
     * @param validator the input checker
     */
    public ConsoleUI(Scanner in, PrintStream out, PlaylistManager manager,
        InputValidator validator)
    {
        this.in = in;
        this.out = out;
        this.manager = manager;
        this.validator = validator;
    }

    /**
     * Runs the menu loop until the user exits or input ends.
     */
    public void run()
    {
        try
        {
            boolean running = true;
            while (running)
            {
                out.print(MENU);
                int choice = promptUntilValid("Choose 1-" + MENU_MAX + ": ",
                    s -> validator.parseMenuChoice(s, 1, MENU_MAX));
                switch (choice)
                {
                    case 1:
                        addSongs();
                        break;
                    case 2:
                        addEpisodes();
                        break;
                    case 3:
                        addChapters();
                        break;
                    case 4:
                        deleteItem();
                        break;
                    case 5:
                        search();
                        break;
                    case 6:
                        out.print(manager.formatPlaylist());
                        break;
                    default:
                        out.println("Goodbye!");
                        running = false;
                }
            }
        }
        catch (EndOfInput e)
        {
            out.println();
            out.println("Goodbye!");
        }
    }

    /**
     * Asks until the parser accepts the answer, explaining each bad one.
     *
     * @param <T> the parsed type
     * @param prompt the question
     * @param parser turns raw text into a result
     * @return the accepted value
     */
    public <T> T promptUntilValid(String prompt,
        Function<String, InputValidator.Result<T>> parser)
    {
        while (true)
        {
            out.print(prompt);
            if (!in.hasNextLine())
            {
                throw new EndOfInput();
            }
            InputValidator.Result<T> result = parser.apply(in.nextLine());
            if (result.isValid())
            {
                return result.getValue();
            }
            out.println(result.getError());
        }
    }

    // ---- menu actions ------------------------------------------------

    private String askText(String label)
    {
        return promptUntilValid(label + ": ", validator::parseText);
    }

    private int askDuration()
    {
        return promptUntilValid("Duration (m:ss): ",
            validator::parseDuration);
    }

    private boolean askYesNo(String question)
    {
        return promptUntilValid(question + " (y/n): ",
            validator::parseYesNo);
    }

    private void addSongs()
    {
        do
        {
            Song song = new Song(askText("Title"), askText("Artist"),
                askText("Genre"), askDuration());
            if (!manager.isDuplicate(song) || askYesNo(
                "That song is already in the playlist. Add it anyway?"))
            {
                manager.addSong(song);
                out.print(manager.formatPlaylist());
            }
        }
        while (askYesNo("Add another song?"));
    }

    private void addEpisodes()
    {
        do
        {
            PodcastEpisode ep = new PodcastEpisode(askText("Episode title"),
                askText("Podcaster"), askText("Podcast title"),
                askText("Description"), askDuration());
            if (!manager.isDuplicate(ep) || askYesNo(
                "That episode is already in the playlist. Add it anyway?"))
            {
                manager.addEpisode(ep);
                out.print(manager.formatPlaylist());
            }
        }
        while (askYesNo("Add another episode?"));
    }

    private void addChapters()
    {
        do
        {
            AudiobookChapter ch = new AudiobookChapter(
                askText("Chapter title"), askText("Author"),
                askText("Narrator"), askText("Book title"),
                askText("Genre"), askText("Description"), askDuration());
            if (!manager.isDuplicate(ch) || askYesNo(
                "That chapter is already in the playlist. Add it anyway?"))
            {
                manager.addChapter(ch);
                out.print(manager.formatPlaylist());
            }
        }
        while (askYesNo("Add another chapter?"));
    }

    private MediaType askType(String action)
    {
        int n = promptUntilValid(action + ": 1. Songs  2. Podcast episodes  "
            + "3. Audiobook chapters: ",
            s -> validator.parseMenuChoice(s, 1, 3));
        return MediaType.values()[n - 1];
    }

    private void deleteItem()
    {
        MediaType type = askType("Delete from");
        int size = manager.size(type);
        if (size == 0)
        {
            out.println("There is nothing to delete in the "
                + type.getLabel() + " list.");
            return;
        }
        out.print(manager.formatPlaylist());
        int position = promptUntilValid("Position to delete: ",
            s -> validator.parsePosition(s, size));
        MediaItem removed = manager.deleteAt(type, position);
        out.println("Deleted: " + removed.describe());
        out.print(manager.formatPlaylist());
    }

    private void search()
    {
        MediaType type = askType("Search in");
        int mode = promptUntilValid("Search by 1. text  2. max duration: ",
            s -> validator.parseMenuChoice(s, 1, 2));
        if (mode == 1)
        {
            String query = askText("Search text");
            switch (type)
            {
                case SONG:
                    out.print(manager.formatList(manager.searchSongs(query)));
                    break;
                case EPISODE:
                    out.print(manager.formatList(
                        manager.searchEpisodes(query)));
                    break;
                default:
                    out.print(manager.formatList(
                        manager.searchChapters(query)));
            }
        }
        else
        {
            int max = askDuration();
            switch (type)
            {
                case SONG:
                    out.print(manager.formatList(
                        manager.searchSongsByDuration(max)));
                    break;
                case EPISODE:
                    out.print(manager.formatList(
                        manager.searchEpisodesByDuration(max)));
                    break;
                default:
                    out.print(manager.formatList(
                        manager.searchChaptersByDuration(max)));
            }
        }
    }
}
