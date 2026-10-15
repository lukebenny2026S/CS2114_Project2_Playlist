import static org.junit.Assert.*;

import org.junit.Test;

/** Tests InputValidator. */
public class InputValidatorTest
{
    private final InputValidator v = new InputValidator();

    @Test
    public void normalizeTextTrimsAndCollapses()
    {
        assertEquals("The Wall", InputValidator.normalizeText("  The   Wall "));
    }

    @Test
    public void normalizeTextNullGivesEmpty()
    {
        assertEquals("", InputValidator.normalizeText(null));
    }

    @Test
    public void parseTextAcceptsValid()
    {
        assertEquals("Blue Moon", v.parseText("  Blue  Moon ").getValue());
        assertTrue(v.parseText("Beyoncé (Live)!").isValid());
    }

    @Test
    public void parseTextRejectsBad()
    {
        assertFalse(v.parseText("   ").isValid());
        assertFalse(v.parseText(null).isValid());
        assertFalse(v.parseText(new String(new char[101]).replace('\0', 'x'))
            .isValid());
        assertFalse(v.parseText("a\tb").isValid());
        assertNotNull(v.parseText("").getError());
    }

    @Test
    public void parseMenuChoiceAcceptsValid()
    {
        assertEquals(Integer.valueOf(3),
            v.parseMenuChoice(" 3 ", 1, 7).getValue());
    }

    @Test
    public void parseMenuChoiceRejectsBad()
    {
        for (String bad : new String[] {"abc", "2.5", "-1", "0", "8", "1 2",
            "", "99999999999"})
        {
            InputValidator.Result<Integer> r = v.parseMenuChoice(bad, 1, 7);
            assertFalse(bad, r.isValid());
            assertNotNull(r.getError());
        }
    }

    @Test
    public void parsePositionAcceptsValid()
    {
        assertEquals(Integer.valueOf(4), v.parsePosition("4", 4).getValue());
    }

    @Test
    public void parsePositionRejectsBad()
    {
        for (String bad : new String[] {"5", "0", "-2", "1 2"})
        {
            assertFalse(bad, v.parsePosition(bad, 4).isValid());
        }
        assertTrue(v.parsePosition("5", 4).getError().contains("1 to 4"));
    }

    @Test
    public void parseDurationAcceptsValid()
    {
        assertEquals(Integer.valueOf(225), v.parseDuration("3:45").getValue());
        assertEquals(Integer.valueOf(36000),
            v.parseDuration("600:00").getValue());
    }

    @Test
    public void parseDurationRejectsBad()
    {
        for (String bad : new String[] {"3:75", "3:5", "345", "0:00",
            "601:00", "3.5", "-1:20", "", "abc"})
        {
            InputValidator.Result<Integer> r = v.parseDuration(bad);
            assertFalse(bad, r.isValid());
            assertTrue(r.getError().contains("m:ss"));
        }
    }

    @Test
    public void parseYesNoAcceptsValid()
    {
        assertTrue(v.parseYesNo("Y").getValue());
        assertTrue(v.parseYesNo("yes").getValue());
        assertFalse(v.parseYesNo("  no ").getValue());
        assertFalse(v.parseYesNo("n").getValue());
    }

    @Test
    public void parseYesNoRejectsBad()
    {
        assertFalse(v.parseYesNo("maybe").isValid());
        assertFalse(v.parseYesNo("").isValid());
        assertFalse(v.parseYesNo(null).isValid());
    }

    @Test
    public void resultFactoriesBuildValidAndInvalid()
    {
        assertTrue(InputValidator.Result.ok("x").isValid());
        InputValidator.Result<String> bad = InputValidator.Result.fail("no");
        assertFalse(bad.isValid());
        assertNull(bad.getValue());
        assertEquals("no", bad.getError());
    }
}
