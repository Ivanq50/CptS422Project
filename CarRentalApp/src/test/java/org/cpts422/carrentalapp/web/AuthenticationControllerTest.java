package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.service.AuthenticationService;
import org.cpts422.carrentalapp.web.datatransfers.LoginForm;
import org.cpts422.carrentalapp.web.datatransfers.RegistrationForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    AuthenticationService auth;

    @Mock
    BindingResult bindingResult;

    @InjectMocks
    AuthenticationController controller;

    @Test
    void loginPageAddsForm() {
        Model model = new ExtendedModelMap();
        String view = controller.loginPage(model);

        assertEquals("login", view);
        assertTrue(model.containsAttribute("form"));
        assertTrue(model.getAttribute("form") instanceof LoginForm);
    }

    @Test
    void loginPageKeepsExistingForm() {
        Model model = new ExtendedModelMap();
        model.addAttribute("form", new LoginForm());

        String view = controller.loginPage(model);

        assertEquals("login", view);
        assertTrue(model.containsAttribute("form"));
    }

    @Test
    void loginBindingErrorsReturnsLogin() {
        LoginForm form = new LoginForm();
        form.setUsername(""); // Invalid
        form.setPassword("short"); // invalid

        when(bindingResult.hasErrors()).thenReturn(true);

        HttpSession session = new MockHttpSession();
        String view = controller.login(form, bindingResult, session);

        assertEquals("login", view);
        verify(bindingResult).hasErrors();
        verifyNoInteractions(auth);
    }

    @Test
    void loginBadCredentialsReturnsLoginError() {
        LoginForm form = new LoginForm();
        form.setUsername("user1");
        form.setPassword("wrong-password");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(auth.authenticate("user1", "wrong-password"))
                .thenThrow(new IllegalArgumentException("Invalid username or password"));

        HttpSession session = new MockHttpSession();
        String view = controller.login(form, bindingResult, session);

        assertEquals("login", view);
        verify(bindingResult).hasErrors();
        verify(auth).authenticate("user1", "wrong-password");
        verify(bindingResult).rejectValue(eq("password"), eq("invalid"), anyString());
    }

    @Test
    void loginSuccessSetsSessionAndRedirects() throws Exception {
        LoginForm form = new LoginForm();
        form.setUsername("alice");
        form.setPassword("correct-password");

        when(bindingResult.hasErrors()).thenReturn(false);

        AppUser u = new AppUser();
        u.setUsername("alice");

        // Inject ID
        java.lang.reflect.Field f = AppUser.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(u, 100L);

        when(auth.authenticate("alice", "correct-password")).thenReturn(u);

        MockHttpSession session = new MockHttpSession();
        String view = controller.login(form, bindingResult, session);

        assertEquals("redirect:/", view);
        assertEquals("alice", session.getAttribute("username"));
        assertEquals(100L, session.getAttribute("userId"));
        verify(auth).authenticate("alice", "correct-password");
    }

    @Test
    void logoutInvalidatesSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "x");
        session.setAttribute("userId", 1L);

        String view = controller.logout(session);

        assertEquals("redirect:/", view);
        assertTrue(session.isInvalid());
    }

    @Test
    void registerPageAddsForm() {
        Model model = new ExtendedModelMap();

        String view = controller.registerPage(model);

        assertEquals("register", view);
        assertTrue(model.containsAttribute("form"));
        assertTrue(model.getAttribute("form") instanceof RegistrationForm);
    }

    @Test
    void registerPageKeepsExistingForm() {
        Model model = new ExtendedModelMap();
        model.addAttribute("form", new RegistrationForm());

        String view = controller.registerPage(model);

        assertEquals("register", view);
        assertTrue(model.containsAttribute("form"));
    }

    @Test
    void registerWithErrorsReturnsRegister() {
        RegistrationForm form = new RegistrationForm();
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.register(form, bindingResult);

        assertEquals("register", view);
        verify(bindingResult).hasErrors();
        verifyNoInteractions(auth);
    }

    @Test
    void registerDuplicateUsernameReturnsError() {
        RegistrationForm form = new RegistrationForm();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(auth.register(form)).thenThrow(new org.cpts422.carrentalapp.service.error.DuplicateUsernameException("taken"));

        String view = controller.register(form, bindingResult);

        assertEquals("register", view);
        verify(bindingResult).rejectValue(eq("username"), eq("duplicate"), anyString());
    }

    @Test
    void registerDuplicateDriversLicenseReturnsError() {
        RegistrationForm form = new RegistrationForm();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(auth.register(form)).thenThrow(new org.cpts422.carrentalapp.service.error.DriversLicenseTakenException("dl-taken"));

        String view = controller.register(form, bindingResult);

        assertEquals("register", view);
        verify(bindingResult).rejectValue(eq("driversLicenseNumber"), eq("duplicate"), anyString());
    }

    @Test
    void registerRuntimeErrorMessageReturnsRegister() {
        RegistrationForm form = new RegistrationForm();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(auth.register(form)).thenThrow(new RuntimeException("Test"));

        String view = controller.register(form, bindingResult);

        assertEquals("register", view);
        verify(bindingResult).reject(eq("registration"), anyString());
    }

    @Test
    void registerRuntimeNullErrorDisplaysDefaultMessage() {
        RegistrationForm form = new RegistrationForm();
        when(bindingResult.hasErrors()).thenReturn(false);
        RuntimeException ex = new RuntimeException() { @Override public String getMessage() { return null; } };
        when(auth.register(form)).thenThrow(ex);

        String view = controller.register(form, bindingResult);

        assertEquals("register", view);
        verify(bindingResult).reject(eq("registration"), anyString());
    }

    @Test
    void registerSuccessful() {
        RegistrationForm form = new RegistrationForm();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(auth.register(form)).thenReturn(new AppUser());

        String view = controller.register(form, bindingResult);

        assertEquals("register_successful", view);
    }

    @Test
    void membershipTypesReturnsValues() {
        assertArrayEquals(
                org.cpts422.carrentalapp.model.MembershipType.values(),
                controller.membershipTypes()
        );
    }
}
