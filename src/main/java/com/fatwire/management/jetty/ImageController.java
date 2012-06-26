package com.fatwire.management.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.TimeZone;

import javax.management.ObjectName;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatwire.management.JMXMetric;
import com.fatwire.management.dao.HostDao;
import com.fatwire.management.dao.JMXMetricDao;
import com.fatwire.management.domain.Host;

public class ImageController implements Controller {
    private static final Logger log = LoggerFactory
            .getLogger(ImageController.class);

    final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private URI dir;

    private HostDao hostDao;

    private JMXMetricDao metricDao;

    //MetricGrapher grapher;

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        log.debug("request uri: " +request.getRequestURI());
        //we should move to rrd/template/period.png

        URI u2 = getImageUri(request);

        if (!u2.getPath().startsWith(dir.getPath())) {
            log.debug("u2 path: "+u2.getPath());
            log.debug("dir path: " + dir.getPath());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            File f = new File(u2);
            if (f.exists()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("image/png");
                response.setHeader("Cache-Control", "must-revalidate");

                response.setDateHeader("Last-Modified", f.lastModified());
                InputStream in = new FileInputStream(f);
                ServletOutputStream out = response.getOutputStream();
                try {
                    copy(out, in);
                } finally {

                    try {
                        in.close();
                    } catch (Exception e) {
                        //ignore
                    }

                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        return null;
    }

    private URI getImageUri(HttpServletRequest request) {

        int start = request.getContextPath().length() + 1;

        URI u = URI.create(request.getRequestURI().substring(start))
                .normalize();
        if (log.isDebugEnabled()) {
            log.debug(u.toASCIIString());
        }
        /*
        String[] part = u.getPath().split("/");
        if (part.length == 4) {
            //part[0]=host_port
            //part[1]=domain
            //part[2]=objectName
            //part[3]=templateHash.png
            Host host = discoverHost(part[0]);
            JMXMetric m = discoverMetric(host, ObjectName.getInstance(part[1] + ":" + part[2]));
            String hash = part[3].substring(0, part[3].length()-4);
            for (URL template : m.getGraphTemplateUrls()) {
                String graphName2 = m.getObjName().getDomain() + "/"
                        + m.getObjName().toString() + "/" + template.getPath();
                String digest = DigestUtils.md5Hex(graphName2);
                if(hash.equals(digest)){
                    // this is the one we need
                    //no see if we need to render it
                }
                

            }

        }
        */

        URI u2 = dir.resolve(request.getRequestURI().substring(start));
        log.debug(u2.toASCIIString());

        return u2.normalize();
    }

    private JMXMetric discoverMetric(Host host, ObjectName objectName) {
        return metricDao.findMetric(host, objectName);
    }

    private Host discoverHost(String host) {
        int t = host.lastIndexOf("_");
        return this.hostDao.findHost(host.substring(0, t), Integer
                .parseInt(host.substring(t + 1)));
    }

    class RenderData {
        JMXMetric metric;

        URI template;
    }

    private void copy(ServletOutputStream out, InputStream in)
            throws IOException {

        byte[] b = new byte[8 * 1024];
        int c = 0;
        while ((c = in.read(b)) != -1) {
            out.write(b, 0, c);
        }
        out.flush();
    }

    public long getLastModified(HttpServletRequest req) {
        File f = new File(getImageUri(req));
        if (f.exists()) {
            Calendar c = Calendar.getInstance(GMT);
            c.setTimeInMillis(f.lastModified());
            //log.info(f.lastModified() +" "+c.getTimeInMillis());

            return c.getTimeInMillis();
        }

        return -1;
    }

    /**
     * @param dir the dir to set
     */
    public void setDir(URI dir) {
        log.debug("setting dir: " + dir.toASCIIString());
        this.dir = dir.normalize();
    }

    /**
     * @param hostDao the hostDao to set
     */
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
