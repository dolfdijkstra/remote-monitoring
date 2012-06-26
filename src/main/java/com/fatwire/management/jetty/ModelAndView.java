package com.fatwire.management.jetty;

import java.util.Map;

public class ModelAndView {
    
    private final Map model;
    
    private final View view;

    /**
     * @param model
     * @param view
     */
    public ModelAndView(Map model, View view) {
        super();
        this.model = model;
        this.view = view;
    }

    /**
     * @return the model
     */
    public Map getModel() {
        return model;
    }

    /**
     * @return the view
     */
    public View getView() {
        return view;
    }

}
