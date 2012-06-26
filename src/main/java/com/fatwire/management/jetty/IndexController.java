package com.fatwire.management.jetty;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.dao.HostDao;
import com.fatwire.management.dao.JMXMetricDao;
import com.fatwire.management.domain.Host;

public class IndexController implements Controller {
    private static final Logger log = LoggerFactory
            .getLogger(IndexController.class);

    private HostDao hostDao;

    private JMXMetricDao metricDao;

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        log.debug(request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "must-revalidate");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();

        writer.println("<html><head><title>JMX Beans</title></head><body>");

        writer.println("<h1><a href=\"/\">Graphs from mbeans</a></h1>");
        for (Host host : hostDao.findHosts()) {
            writer.println("<div class=\"host\"><a href=\"host/"
                    + host.getAddress().getHostAddress() + "_"
                    + host.getJmxPort() + "\">"
                    + host.getAddress().getHostName() + "_" + host.getJmxPort()
                    + "</a><br/>");
            writer.println("<select name=\"metrics\" multiple=\"mulitple\" >");
            for (JMXMetric m : this.metricDao.queryMetrics(host)) {

                writer.println("<option value=\"" + m.getObjName() + "\">"
                        + m.getObjName() + "</option><br/>");
            }
            writer.println("</select>");
            writer.println("</div>");
        }

        writer.println("</body></html>");
        return null;
    }

    @Override
    public long getLastModified(HttpServletRequest req) {

        return -1;
    }

    public void setHostDao(HostDao hostDao) {
        this.hostDao = hostDao;

    }

    /**
     * @param metricDao the metricDao to set
     */
    public void setMetricDao(JMXMetricDao metricDao) {
        this.metricDao = metricDao;
    }

}
