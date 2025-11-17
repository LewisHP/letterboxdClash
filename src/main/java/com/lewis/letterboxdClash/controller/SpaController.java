package com.lewis.letterboxdClash.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle SPA (Single Page Application) routing.
 * Handles 404 errors and forwards to index.html for React Router to handle.
 * This allows client-side routing to work when users refresh or navigate directly to routes.
 */
@Controller
public class SpaController implements ErrorController {

    /**
     * Forward 404 errors to index.html so React Router can handle the route.
     * API routes are excluded as they're handled by RestControllers.
     */
    @RequestMapping("/error")
    public String handleError() {
        return "forward:/index.html";
    }
}
