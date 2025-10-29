package org.cpts422.carrentalapp.web;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock AccountService accounts;
    @InjectMocks AccountController controller;

    @Test
    void accountRedirectsToLoginWhenSignedOut() {
        Model model = new ExtendedModelMap();
        MockHttpSession session = new MockHttpSession(); // No UserID

        String view = controller.account(model, session);

        assertEquals("redirect:/login", view);
        verifyNoInteractions(accounts);
    }

    @Test
    void accountShowsAccountWhenSignedIn() {
        Model model = new ExtendedModelMap();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 5L);

        AppUser u = new AppUser();
        when(accounts.getById(5L)).thenReturn(u);

        String view = controller.account(model, session);

        assertEquals("account", view);
        assertSame(u, model.getAttribute("user"));
        verify(accounts).getById(5L);
    }

    @Test
    void addFundsRedirectsToLoginWhenSignedOut() {
        MockHttpSession session = new MockHttpSession(); // no userId
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.addFunds(session, 12.34, ra);

        assertEquals("redirect:/login", view);
        assertTrue(((RedirectAttributesModelMap) ra).getFlashAttributes().isEmpty());
        verifyNoInteractions(accounts);
    }

    @Test
    void addFundSuccessfullySetsMessageAndRedirects() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 7L);
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        doNothing().when(accounts).addFunds(7L, 10.0);

        String view = controller.addFunds(session, 10.0, ra);

        assertEquals("redirect:/account", view);
        assertEquals("Funds added.", ra.getFlashAttributes().get("msg"));
        assertFalse(ra.getFlashAttributes().containsKey("error"));
        verify(accounts).addFunds(7L, 10.0);
    }

    @Test
    void addFundsFailureThrowsErrorAndRedirects() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 7L);
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        doThrow(new RuntimeException("Exception")).when(accounts).addFunds(7L, 20.0);

        String view = controller.addFunds(session, 20.0, ra);

        assertEquals("redirect:/account", view);
        assertEquals("Exception", ra.getFlashAttributes().get("error"));
        assertFalse(ra.getFlashAttributes().containsKey("msg"));
        verify(accounts).addFunds(7L, 20.0);
    }
}
