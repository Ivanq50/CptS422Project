package org.cpts422.carrentalapp.web;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void RuntimeErrorSetsErrorAttributeAndReturnsErrorView() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Model model = new ExtendedModelMap();

        String view = handler.handleRuntime(new RuntimeException("Exception"), model);

        assertEquals("error", view);
        assertEquals("Exception", model.getAttribute("error"));
    }

    @Test
    void RuntimeNullMessageIsAllowed() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Model model = new ExtendedModelMap();

        RuntimeException ex = new RuntimeException() { @Override public String getMessage() { return null; } };
        String view = handler.handleRuntime(ex, model);

        assertEquals("error", view);
        assertNull(model.getAttribute("error"));
    }
}
