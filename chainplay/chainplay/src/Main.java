import java.util.Scanner;

/**
 * Entry point: wires the objects together and starts the console UI.
 *
 * @author ChainPlay team
 * @version October 2026
 */
public class Main
{
    /**
     * Starts ChainPlay.
     *
     * @param args unused
     */
    public static void main(String[] args)
    {
        new ConsoleUI(new Scanner(System.in), System.out,
            new PlaylistManager(), new InputValidator()).run();
    }
}
