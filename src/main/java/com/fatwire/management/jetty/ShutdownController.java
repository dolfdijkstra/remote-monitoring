package com.fatwire.management.jetty;

import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ShutdownController implements Controller {

    private CountDownLatch latch;

    /**
     * @param latch the latch to set
     */
    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public long getLastModified(HttpServletRequest req) {
        return -1;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        response.getWriter().write("down down down");
        latch.countDown();
        return null;
    }

}
