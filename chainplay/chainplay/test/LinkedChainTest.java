import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/** Tests the provided LinkedChain on its own, using simple Strings. */
public class LinkedChainTest
{
    private LinkedChain<String> chain;

    @Before
    public void setUp()
    {
        chain = new LinkedChain<String>();
    }

    @Test
    public void addKeepsDuplicatesAtFront()
    {
        chain.add("A");
        chain.add("A");
        assertEquals(2, chain.size());
        chain.add("B");
        assertEquals("B", chain.get(0));
    }

    @Test
    public void addNullThrowsAndKeepsSize()
    {
        chain.add("A");
        assertThrows(IllegalArgumentException.class, () -> chain.add(null));
        assertEquals(1, chain.size());
    }

    @Test
    public void addToEndKeepsInsertionOrder()
    {
        chain.addToEnd("A");
        chain.addToEnd("B");
        assertEquals("[A, B]", chain.toString());
    }

    @Test
    public void addToEndNullThrows()
    {
        assertThrows(IllegalArgumentException.class,
            () -> chain.addToEnd(null));
        assertEquals(0, chain.size());
    }

    @Test
    public void removeFirstReturnsHead()
    {
        chain.addToEnd("A");
        chain.addToEnd("B");
        assertEquals("A", chain.remove());
        assertEquals(1, chain.size());
    }

    @Test
    public void removeFirstOnEmptyReturnsNull()
    {
        assertNull(chain.remove());
    }

    @Test
    public void removeByValueRemovesFirstMatch()
    {
        chain.addToEnd("A");
        chain.addToEnd("B");
        chain.addToEnd("A");
        assertTrue(chain.remove("A"));
        assertEquals("[B, A]", chain.toString());
    }

    @Test
    public void removeByValueMissingOrNullChangesNothing()
    {
        chain.addToEnd("A");
        assertFalse(chain.remove("Z"));
        assertFalse(chain.remove((String) null));
        assertEquals(1, chain.size());
    }

    @Test
    public void getReturnsEntryAtIndex()
    {
        chain.addToEnd("A");
        chain.addToEnd("B");
        chain.addToEnd("C");
        assertEquals("B", chain.get(1));
    }

    @Test
    public void getBadIndexThrows()
    {
        chain.addToEnd("A");
        assertThrows(IndexOutOfBoundsException.class, () -> chain.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> chain.get(-1));
        assertThrows(IndexOutOfBoundsException.class,
            () -> new LinkedChain<String>().get(0));
    }

    @Test
    public void containsAndCountUseEquals()
    {
        chain.addToEnd("A");
        chain.addToEnd("B");
        chain.addToEnd("A");
        assertTrue(chain.contains("A"));
        assertEquals(2, chain.count("A"));
    }

    @Test
    public void containsAndCountAbsentOrNull()
    {
        chain.addToEnd("A");
        assertFalse(chain.contains("Z"));
        assertEquals(0, chain.count("Z"));
        assertFalse(chain.contains(null));
        assertEquals(0, chain.count(null));
    }

    @Test
    public void sizeAndIsEmpty()
    {
        assertEquals(0, chain.size());
        assertTrue(chain.isEmpty());
        chain.add("A");
        assertEquals(1, chain.size());
        assertFalse(chain.isEmpty());
    }

    @Test
    public void clearEmptiesAndStillAccepts()
    {
        chain.add("A");
        chain.clear();
        assertTrue(chain.isEmpty());
        chain.add("B");
        assertEquals(1, chain.size());
        new LinkedChain<String>().clear(); // empty chain: no failure
    }

    @Test
    public void toArrayCopiesInOrder()
    {
        chain.addToEnd("A");
        chain.addToEnd("B");
        assertArrayEquals(new String[] {"A", "B"},
            chain.toArray(new String[2]));
    }

    @Test
    public void toArrayBadArgumentThrows()
    {
        chain.add("A");
        assertThrows(IllegalArgumentException.class,
            () -> chain.toArray(null));
        assertThrows(IllegalArgumentException.class,
            () -> chain.toArray(new String[0]));
    }

    @Test
    public void toStringFormats()
    {
        assertEquals("[]", chain.toString());
        chain.addToEnd("A");
        chain.addToEnd("B");
        assertEquals("[A, B]", chain.toString());
    }

    @Test
    public void storesDifferentTypesThroughSameClass()
    {
        LinkedChain<Integer> numbers = new LinkedChain<Integer>();
        numbers.add(5);
        assertEquals(Integer.valueOf(5), numbers.get(0));
        chain.add("x");
        assertEquals("x", chain.get(0));
    }
}
