package com.fatwire.management.jetty;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface View {

    public void render(Map model, HttpServletRequest request,
            HttpServletResponse response) throws Exception;
}
