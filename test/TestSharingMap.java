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
        System.out.println(a);
    }

    public void testSharingNavMapSharesBaseTree(){
        String s = "string"; // placeholder to pause on
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
    }
}
