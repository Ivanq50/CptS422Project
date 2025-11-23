package org.cpts422.carrentalapp.Integration;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.model.VehicleType;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.repo.RentalRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RentingVehicleIntegrationtest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RentalRepository rentalRepository;

    private AppUser testUser;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        // 1. Create a Test User
        testUser = new AppUser();
        testUser.setUsername("integrationUser");
        testUser.setPasswordHash("hashedPassword"); // In real app, this should be encoded
        testUser.setAge(30);
        testUser.setDriversLicenseNumber("DL-INT-1");
        testUser.setDriversLicenseExpiry(LocalDate.now().plusYears(5));
        testUser.setMembershipType(MembershipType.STANDARD);
        testUser.setWalletBalance(500.0); // Sufficient funds
        testUser = appUserRepository.save(testUser);

        // 2. Create a Test Vehicle
        testVehicle = new Vehicle();
        testVehicle.setMake("Toyota");
        testVehicle.setModel("Camry");
        testVehicle.setDailyRate(50.0);
        testVehicle.setVehicleType(VehicleType.SEDAN);
        testVehicle.setAvailable(true);
        testVehicle = vehicleRepository.save(testVehicle);
    }

    @AfterEach
    void tearDown() {
        rentalRepository.deleteAll();
        vehicleRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration: Cart Controller -> Rental Service -> Repositories (Successful Checkout)")
    void testRentVehicleProcess() throws Exception {
        // This test simulates the full flow of adding a car to cart and checking out

        // Set up session with userId for authentication
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", testUser.getId());

        // Step 1: Add the vehicle to the session Cart
        // Endpoint: POST /cart/add-rent with vehicleId and days parameters
        mockMvc.perform(post("/cart/add-rent")
                        .session(session)
                        .param("vehicleId", String.valueOf(testVehicle.getId()))
                        .param("days", "3")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection()); // Usually redirects back to cart

        // Step 2: Perform Checkout
        // Endpoint: POST /cart/checkout-rent
        mockMvc.perform(post("/cart/checkout-rent")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection()); // Should redirect to success page or rental history

        // Step 3: Verify Database State (Data Layer Integration)

        // A. Verify Vehicle is now marked as UNAVAILABLE
        Vehicle updatedVehicle = vehicleRepository.findById(testVehicle.getId()).orElseThrow();
        assertFalse(updatedVehicle.isAvailable(), "Vehicle should be marked unavailable after rental");

        // B. Verify Rental Record exists
        long rentalCount = rentalRepository.count();
        assertEquals(1, rentalCount, "There should be exactly one rental record in the database");

        var rental = rentalRepository.findAll().get(0);
        assertEquals(testVehicle.getId(), rental.getVehicle().getId(), "Rental record should match the correct vehicle");
        assertEquals(testUser.getUsername(), rental.getUser().getUsername(), "Rental record should belong to the test user");
    }
}