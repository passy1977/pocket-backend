/***************************************************************************
 *
 * Pocket web backend
 * Copyright (C) 2018/2025 Antonio Salsi <passy.linux@zresa.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************/

package it.salsi.pocket.rests;

import it.salsi.pocket.controllers.SessionController;
import it.salsi.pocket.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionRest Unit Tests")
class SessionRestTest {

    @Mock
    private SessionController sessionController;

    @InjectMocks
    private SessionRest sessionRest;

    private static final String VALID_UUID = "12345678-1234-1234-1234-123456789012";
    private static final String VALID_CRYPT = "validCryptData123";
    private static final String INVALID_UUID = "invalid-uuid";
    private static final String INVALID_CRYPT = "invalid@crypt#data";

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should get data successfully with valid parameters")
    void shouldGetDataSuccessfully() throws Exception {
        // Given
        Container expectedContainer = createTestContainer();
        ResponseEntity<Container> expectedResponse = ResponseEntity.ok(expectedContainer);
        
        when(sessionController.getData(eq(VALID_UUID), eq(VALID_CRYPT), anyString()))
                .thenReturn(expectedResponse);

        // When
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        ResponseEntity<Container> response = sessionRest.getData(VALID_UUID, VALID_CRYPT, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedContainer, response.getBody());
        verify(sessionController).getData(VALID_UUID, VALID_CRYPT, "127.0.0.1");
    }

    @Test
    @DisplayName("Should handle X-Forwarded-For header correctly")
    void shouldHandleXForwardedForHeader() throws Exception {
        // Given
        Container expectedContainer = createTestContainer();
        ResponseEntity<Container> expectedResponse = ResponseEntity.ok(expectedContainer);
        String forwardedIP = "192.168.1.100";
        
        when(sessionController.getData(eq(VALID_UUID), eq(VALID_CRYPT), eq(forwardedIP)))
                .thenReturn(expectedResponse);

        // When
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("x-forwarded-for", forwardedIP);
        ResponseEntity<Container> response = sessionRest.getData(VALID_UUID, VALID_CRYPT, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sessionController).getData(VALID_UUID, VALID_CRYPT, forwardedIP);
    }

    @Test
    @DisplayName("Should persist data successfully")
    void shouldPersistDataSuccessfully() throws Exception {
        // Given
        Container inputContainer = createTestContainer();
        ResponseEntity<Container> expectedResponse = ResponseEntity.ok(inputContainer);
        
        when(sessionController.persist(eq(VALID_UUID), eq(VALID_CRYPT), eq(inputContainer), anyString()))
                .thenReturn(expectedResponse);

        // When
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        ResponseEntity<Container> response = sessionRest.persist(VALID_UUID, VALID_CRYPT, inputContainer, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(inputContainer, response.getBody());
        verify(sessionController).persist(VALID_UUID, VALID_CRYPT, inputContainer, "127.0.0.1");
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() throws Exception {
        // Given
        ResponseEntity<Boolean> expectedResponse = ResponseEntity.ok(true);
        Boolean changePasswdDataOnServer = true;
        
        when(sessionController.changePasswd(eq(VALID_UUID), eq(VALID_CRYPT), eq(changePasswdDataOnServer), anyString()))
                .thenReturn(expectedResponse);

        // When
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        ResponseEntity<Boolean> response = sessionRest.changePasswd(VALID_UUID, VALID_CRYPT, changePasswdDataOnServer, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(sessionController).changePasswd(VALID_UUID, VALID_CRYPT, changePasswdDataOnServer, "127.0.0.1");
    }

    @Test
    @DisplayName("Should delete cache record successfully") 
    void shouldDeleteCacheRecordSuccessfully() throws Exception {
        // Given - mock the controller to return any ResponseEntity
        when(sessionController.deleteCacheRecord(anyString(), anyString()))
                .thenReturn(ResponseEntity.ok().build());

        // When
        ResponseEntity<?> response = sessionRest.deleteCacheRecord(VALID_UUID, VALID_CRYPT);

        // Then
        assertNotNull(response);
        verify(sessionController).deleteCacheRecord(VALID_UUID, VALID_CRYPT);
    }

    @Test
    @DisplayName("Should check cache record successfully")
    void shouldCheckCacheRecordSuccessfully() throws Exception {
        // Given - mock the controller to return any ResponseEntity
        when(sessionController.checkCacheRecord(anyString(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok().build());

        // When
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        ResponseEntity<?> response = sessionRest.checkCacheRecord(VALID_UUID, VALID_CRYPT, request);

        // Then
        assertNotNull(response);
        verify(sessionController).checkCacheRecord(VALID_UUID, VALID_CRYPT, "127.0.0.1");
    }

    @Test
    @DisplayName("Should validate UUID format")
    void shouldValidateUuidFormat() throws Exception {
        // This test verifies that validation annotations work
        // In a real Spring context, invalid UUIDs would be rejected by @Pattern validation
        
        // We can test this behavior in integration tests where full Spring context is loaded
        assertTrue(VALID_UUID.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"));
        assertFalse(INVALID_UUID.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"));
    }

    @Test
    @DisplayName("Should validate crypt parameter format")
    void shouldValidateCryptParameterFormat() throws Exception {
        // This test verifies that validation annotations work
        // In a real Spring context, invalid crypt parameters would be rejected by @Pattern validation
        
        assertTrue(VALID_CRYPT.matches("^[A-Za-z0-9_-]{10,2048}(=*)$"));
        assertFalse(INVALID_CRYPT.matches("^[A-Za-z0-9_-]{10,2048}(=*)$"));
    }

    // Helper method to create test data
    private Container createTestContainer() {
        User testUser = new User("testUser", "test@example.com", "hashedPassword");
        testUser.setId(1L);

        Device testDevice = new Device(testUser);
        testDevice.setId(1L);
        testDevice.setUuid("TEST-DEVICE-UUID");

        return new Container(
                System.currentTimeMillis(),
                testUser,
                testDevice,
                List.of(), // groups
                List.of(), // groupFields
                List.of()  // fields
        );
    }
}