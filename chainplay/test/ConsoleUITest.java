import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

/** Tests ConsoleUI and Main with scripted input. */
public class ConsoleUITest
{
    private PlaylistManager manager;
    private ByteArrayOutputStream buffer;

    @Before
    public void setUp()
    {
        manager = new PlaylistManager();
    }

    private String run(String input)
    {
        buffer = new ByteArrayOutputStream();
        ConsoleUI ui = new ConsoleUI(new Scanner(input),
            new PrintStream(buffer), manager, new InputValidator());
        ui.run();
        return buffer.toString();
    }

    private static int count(String text, String part)
    {
        return text.split(java.util.regex.Pattern.quote(part), -1).length - 1;
    }

    @Test
    public void runAddsSongShowsPlaylistAndExits()
    {
        String out = run("1\nHello\nAdele\nPop\n4:55\nn\n6\n7\n");
        assertTrue(out.contains("Hello - Adele (Pop) [4:55]"));
        assertTrue(out.contains("Goodbye!"));
        assertEquals(1, manager.size(MediaType.SONG));
    }

    @Test
    public void runAddsEpisodeAndChapter()
    {
        run("2\nEp\nJoe\nShow\nChat\n10:00\nn\n"
            + "3\nOne\nTolkien\nSmith\nThe Hobbit\nFantasy\nStart\n25:00\nn\n7\n");
        assertEquals(1, manager.size(MediaType.EPISODE));
        assertEquals(1, manager.size(MediaType.CHAPTER));
    }

    @Test
    public void runBadMenuChoicesPrintErrorsThenExitWithPlaylistUnchanged()
    {
        String out = run("x\n9\n7\n");
        assertEquals(2, count(out, "Please enter a whole number from 1 to 7."));
        assertEquals(0, manager.size(MediaType.SONG));
    }

    @Test
    public void runBadFieldsAreReprompted()
    {
        String out = run("1\n\nHello\nAdele\nPop\n3:75\n4:55\nn\n7\n");
        assertTrue(out.contains("Text cannot be empty."));
        assertTrue(out.contains("m:ss"));
        assertEquals(1, manager.size(MediaType.SONG));
    }

    @Test
    public void runEndOfInputMidPromptSaysGoodbye()
    {
        String out = run("1\nHel");
        assertTrue(out.contains("Goodbye!"));
        assertEquals(0, manager.size(MediaType.SONG));
    }

    @Test
    public void runEndOfInputAtMenuSaysGoodbye()
    {
        assertTrue(run("").contains("Goodbye!"));
    }

    @Test
    public void runDeleteRemovesChosenPosition()
    {
        manager.addSong(new Song("First", "A", "Pop", 100));
        manager.addSong(new Song("Second", "B", "Pop", 100));
        String out = run("4\n1\n1\n7\n");
        assertEquals(1, manager.size(MediaType.SONG));
        assertTrue(out.contains("Second"));
        assertTrue(out.contains("Deleted: First"));
    }

    @Test
    public void runDeleteBadPositionRepromptsAndListChangesOnlyOnValid()
    {
        manager.addSong(new Song("First", "A", "Pop", 100));
        manager.addSong(new Song("Second", "B", "Pop", 100));
        String out = run("4\n1\n0\n5\n2\n7\n");
        assertEquals(2, count(out, "Position must be a whole number"));
        assertEquals(1, manager.size(MediaType.SONG));
        assertTrue(out.contains("Deleted: Second"));
    }

    @Test
    public void runDeleteFromEmptyListDoesNotAskForPosition()
    {
        String out = run("4\n1\n7\n");
        assertTrue(out.contains("nothing to delete"));
        assertFalse(out.contains("Position to delete"));
    }

    @Test
    public void runDuplicateAnsweredYesIsAdded()
    {
        run("1\nHello\nAdele\nPop\n4:55\ny\nhello\nADELE\nPop\n4:55\ny\nn\n7\n");
        assertEquals(2, manager.size(MediaType.SONG));
    }

    @Test
    public void runDuplicateAnsweredMaybeRepromptsThenNoCancels()
    {
        String out = run("1\nHello\nAdele\nPop\n4:55\ny\nHello\nAdele\nPop\n"
            + "4:55\nmaybe\nn\nn\n7\n");
        assertTrue(out.contains("Please answer y or n."));
        assertEquals(1, manager.size(MediaType.SONG));
    }

    @Test
    public void runSearchByTextAndByDuration()
    {
        manager.addSong(new Song("Hello", "Adele", "Pop", 295));
        manager.addSong(new Song("Short", "Band", "Rock", 120));
        String out = run("5\n1\n1\nhel\n5\n1\n2\n3:00\n5\n1\n1\nzzz\n7\n");
        assertTrue(out.contains("1. Hello"));
        assertTrue(out.contains("1. Short"));
        assertTrue(out.contains("No matches found."));
    }

    @Test
    public void runSearchEpisodesAndChapters()
    {
        manager.addEpisode(new PodcastEpisode("Ep", "Joe", "Show", "d", 60));
        manager.addChapter(new AudiobookChapter("One", "T", "S", "B", "G",
            "d", 60));
        String out = run("5\n2\n1\njoe\n5\n3\n2\n1:30\n7\n");
        assertTrue(out.contains("1. Ep"));
        assertTrue(out.contains("1. One"));
    }

    @Test
    public void promptUntilValidAcceptsFirstValidAnswerSilently()
    {
        buffer = new ByteArrayOutputStream();
        ConsoleUI ui = new ConsoleUI(new Scanner("3\n"),
            new PrintStream(buffer), manager, new InputValidator());
        InputValidator v = new InputValidator();
        int n = ui.promptUntilValid("? ", s -> v.parseMenuChoice(s, 1, 7));
        assertEquals(3, n);
        assertEquals("? ", buffer.toString());
    }

    @Test
    public void promptUntilValidExplainsThreeBadAnswersBeforeFourth()
    {
        buffer = new ByteArrayOutputStream();
        ConsoleUI ui = new ConsoleUI(new Scanner("a\nb\n0\n3\n"),
            new PrintStream(buffer), manager, new InputValidator());
        InputValidator v = new InputValidator();
        int n = ui.promptUntilValid("? ", s -> v.parseMenuChoice(s, 1, 7));
        assertEquals(3, n);
        assertEquals(3, count(buffer.toString(), "Please enter"));
    }

    @Test
    public void mainShowsMenuAndExitsCleanlyOnEndOfInput()
    {
        java.io.InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try
        {
            System.setIn(new java.io.ByteArrayInputStream(new byte[0]));
            System.setOut(new PrintStream(captured));
            Main.main(new String[0]);
        }
        finally
        {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        assertTrue(captured.toString().contains("ChainPlay"));
        assertTrue(captured.toString().contains("Goodbye!"));
    }
}
