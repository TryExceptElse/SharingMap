import com.github.io.tryexceptelse.data.SharingNavMap;
import junit.framework.TestCase;

public class TestSharingMap extends TestCase{
    public SharingNavMap<Double, String> a = null;
    public SharingNavMap<Double, String> b = null;
    public SharingNavMap<Double, String> c = null;
    public String s = "string";

    @Override
    protected void setUp(){
        // create three copies
        a = new SharingNavMap<>();
        // add values
        a.put(10., "stringA");
        a.put(20., "stringB");
        a.put(30., "stringC");
        a.put(40., "stringD");
        a.put(50., "stringE");
        b = new SharingNavMap<>(a); // copy of original
        c = new SharingNavMap<>(b); // copy of copy
    }

    public void testOriginalRetainsAllValuesAfterCopyRemovesElements(){
        b.remove(20.);
        assertTrue(a.containsKey(10.));
        assertTrue(a.containsKey(20.));
        assertTrue(a.containsKey(30.));
        assertTrue(a.containsKey(40.));
        assertTrue(a.containsKey(50.));
    }

    public void testCopiesDoNotContainRemovedValues() {
        b.remove(20.);
        c.remove(40.);
        assertFalse(b.containsKey(20.));
        assertFalse(c.containsKey(40.));
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println("pass");
    }
}
