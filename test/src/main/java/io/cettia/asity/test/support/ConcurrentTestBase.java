package io.cettia.asity.test.support;

import net.jodah.concurrentunit.Waiter;

import org.junit.After;
import org.junit.Before;

/**
 * @author Donghwan Kim
 */
public abstract class ConcurrentTestBase {

    private Waiter waiter;

    @Before
    public void setUp() throws Exception {
        waiter = new Waiter();
    }

    @After
    public void tearDown() throws Exception {
        waiter = null;
    }

    /**
     * @see Waiter#assertEquals(Object, Object)
     */
    public void assertEquals(Object expected, Object actual) {
        waiter.assertEquals(expected, actual);
    }

    /**
     * @see Waiter#assertTrue(boolean)
     */
    public void assertFalse(boolean b) {
        waiter.assertFalse(b);
    }

    /**
     * @see Waiter#assertNotNull(Object)
     */
    public void assertNotNull(Object object) {
        waiter.assertNotNull(object);
    }

    /**
     * @see Waiter#assertNull(Object)
     */
    public void assertNull(Object x) {
        waiter.assertNull(x);
    }

    /**
     * @see Waiter#assertTrue(boolean)
     */
    public void assertTrue(boolean b) {
        waiter.assertTrue(b);
    }

    /**
     * @see Waiter#fail()
     */
    public void fail() {
        waiter.fail(new AssertionError());
    }

    /**
     * @see Waiter#fail(String)
     */
    public void fail(String reason) {
        waiter.fail(new AssertionError(reason));
    }

    /**
     * @see Waiter#fail(Throwable)
     */
    public void fail(Throwable reason) {
        waiter.fail(reason);
    }

    /**
     * @see Waiter#await()
     */
    protected void await() throws Throwable {
        waiter.await();
    }

    /**
     * @see Waiter#resume()
     */
    protected void resume() {
        waiter.resume();
    }

}
