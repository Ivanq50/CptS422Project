package org.cpts422.carrentalapp.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    @Test
    void homeSignedOutSetsSignedInFalse() {
        HomeController controller = new HomeController();
        Model model = new ExtendedModelMap();
        MockHttpSession session = new MockHttpSession();

        String view = controller.home(model, session);

        assertEquals("home", view);
        assertEquals(false, model.getAttribute("signedIn"));
        assertNull(model.getAttribute("username"));
    }

    @Test
    void homeSignedInSetsSignedInTrueWithUsername() {
        HomeController controller = new HomeController();
        Model model = new ExtendedModelMap();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "alice");

        String view = controller.home(model, session);

        assertEquals("home", view);
        assertEquals(true, model.getAttribute("signedIn"));
        assertEquals("alice", model.getAttribute("username"));
    }
}
