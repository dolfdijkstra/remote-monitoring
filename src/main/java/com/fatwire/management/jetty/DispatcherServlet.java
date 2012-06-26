package com.fatwire.management.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherServlet extends HttpServlet {
    private static final Logger log = LoggerFactory
            .getLogger(DispatcherServlet.class);

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Controller controller;

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        try {
            long t1 = log.isDebugEnabled() ? System.nanoTime() : 0L;
            log.trace("handling request {}", request.getRequestURI());

            ModelAndView mav = controller.handleRequest(request, response);
            long t2 = log.isDebugEnabled() ? System.nanoTime() : 0L;
            log.debug("handleRequest took {} us", (t2 - t1) / 1000L);

            if (mav != null) {
                View view = mav.getView();
                if (view != null) {
                    long t3 = log.isDebugEnabled() ? System.nanoTime() : 0L;
                    view.render(mav.getModel(), request, response);
                    long t4 = log.isDebugEnabled() ? System.nanoTime() : 0L;
                    log.debug("render took {} us", (t4 - t3) / 1000L);

                }
            }
            response.flushBuffer();
            long t5 = log.isDebugEnabled() ? System.nanoTime() : 0L;
            log.debug("doGet took {} us for {}", (t5 - t1) / 1000L, request
                    .getRequestURI());

        } catch (ServletException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#getLastModified(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected long getLastModified(HttpServletRequest req) {

        return controller.getLastModified(req);
    }

    /**
     * @return the controller
     */
    public Controller getController() {
        return controller;
    }

    /**
     * @param controller the controller to set
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

}
