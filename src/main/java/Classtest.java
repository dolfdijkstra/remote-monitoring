import java.lang.reflect.Array;

import junit.framework.Assert;
import junit.framework.TestCase;

public class Classtest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testClass() throws ClassNotFoundException {
        
//        Assert.assertTrue(Character.digit('1', 16) != -1);
//        Assert.assertTrue(Character.digit('a', 16) != -1);
//        Assert.assertTrue(Character.digit('k', 16) != -1);
        Class<?> x = Array.newInstance(String.class, 0).getClass();
        System.out.println(x.getName());
        Class<?> y=Class.forName("[Ljava.lang.String;");
        System.out.println(y.getName());
        Assert.assertEquals(x,y);
        

    }
}
