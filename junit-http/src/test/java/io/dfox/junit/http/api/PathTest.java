package io.dfox.junit.http.api;

import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PathTest {
    
    private void assertPath(final String path, final String grouping, final Optional<String> name) {
        Optional<Path> a = Path.parse(path);
        
        assertTrue(a.isPresent());
        assertEquals(a.get().getGrouping(), grouping);
        assertEquals(a.get().getName(), name);
    }
    
    @Test
    public void testParse() {
        assertFalse(Path.parse("").isPresent());
        assertFalse(Path.parse("/").isPresent());
        assertFalse(Path.parse("//").isPresent());
        assertFalse(Path.parse("/foo/bar/baz").isPresent());
        
        assertPath("foo", "foo", Optional.empty());
        assertPath("/foo", "foo", Optional.empty());
        assertPath("/foo/", "foo", Optional.empty());
        assertPath("/foo/  ", "foo", Optional.empty());
        assertPath("/foo/bar", "foo", Optional.of("bar"));
        assertPath("/foo/bar/", "foo", Optional.of("bar"));
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
