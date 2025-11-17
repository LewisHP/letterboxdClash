package com.lewis.letterboxdClash.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle SPA (Single Page Application) routing.
 * Forwards all non-API routes to index.html for React Router to handle.
 */
@Controller
public class SpaController {

    /**
     * Forward all requests that don't match /api/** to index.html
     * This allows React Router to handle client-side routing
     */
    @RequestMapping(value = {"/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
