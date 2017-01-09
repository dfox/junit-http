package io.dfox.junit.http.api;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PathTest {

    private void assertPath(final String path, final String grouping, final String name) {
        Path a = Path.parse(path);

        assertNotNull(a);
        assertEquals(a.getGrouping(), grouping);
        assertEquals(a.getName(), name);
    }

    @Test
    public void testParse() {
        assertNull(Path.parse(""));
        assertNull(Path.parse("/"));
        assertNull(Path.parse("//"));
        assertNull(Path.parse("/foo/bar/baz"));

        assertPath("foo", "foo", null);
        assertPath("/foo", "foo", null);
        assertPath("/foo/", "foo", null);
        assertPath("/foo/  ", "foo", null);
        assertPath("/foo/bar", "foo", "bar");
        assertPath("/foo/bar/", "foo", "bar");
    }

    @Test
    public void testEqualsAndHashCode() {
        Path a = new Path("foo", "bar");
        Path b = new Path("foo", "bar");
        Path c = new Path("foo", "baz");
        Path d = new Path("baz", "bar");
        Path e = new Path("biz", "bam");

        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals(d));
        assertFalse(a.equals(e));

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
        assertNotEquals(a.hashCode(), d.hashCode());
        assertNotEquals(a.hashCode(), e.hashCode());
    }
}
