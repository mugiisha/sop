package compliance_reporting_service.compliance_reporting_service;

import compliance_reporting_service.compliance_reporting_service.service.UserInfoClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import userService.getUserInfoRequest;
import userService.getUserInfoResponse;
import userService.userServiceGrpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserInfoClientServiceTest {

    @Mock
    private userServiceGrpc.userServiceBlockingStub userInfoServiceBlockingStub;

    @InjectMocks
    private UserInfoClientService userInfoClientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserInfo() {
        // Arrange
        String userId = "user-123";
        getUserInfoResponse mockResponse = getUserInfoResponse.newBuilder()
                .setSuccess(true)
                .setName("John Doe")
                .build();

        when(userInfoServiceBlockingStub.getUserInfo(any(getUserInfoRequest.class)))
                .thenReturn(mockResponse);

        // Act
        getUserInfoResponse response = userInfoClientService.getUserInfo(userId);

        // Assert
        assertEquals(true, response.getSuccess());
        assertEquals("John Doe", response.getName());


        // Verify that the gRPC stub was called once
        verify(userInfoServiceBlockingStub, times(1)).getUserInfo(any(getUserInfoRequest.class));
    }

    @Test
    void testGetUserInfoFailure() {
        // Arrange
        String userId = "user-404";
        getUserInfoResponse mockResponse = getUserInfoResponse.newBuilder()
                .setSuccess(false)
                .setErrorMessage("User not found")
                .build();

        when(userInfoServiceBlockingStub.getUserInfo(any(getUserInfoRequest.class)))
                .thenReturn(mockResponse);

        // Act
        getUserInfoResponse response = userInfoClientService.getUserInfo(userId);

        // Assert
        assertEquals(false, response.getSuccess());
        assertEquals("User not found", response.getErrorMessage());

        // Verify that the gRPC stub was called once
        verify(userInfoServiceBlockingStub, times(1)).getUserInfo(any(getUserInfoRequest.class));
    }
}
