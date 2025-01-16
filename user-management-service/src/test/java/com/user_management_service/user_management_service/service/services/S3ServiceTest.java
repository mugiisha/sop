package com.user_management_service.user_management_service.service.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.user_management_service.user_management_service.services.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private AmazonS3 s3Client;

    private S3Service s3Service;

    @Captor
    private ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String FILE_KEY = "test/file-key.jpg";
    private static final String FILE_CONTENT_TYPE = "image/jpeg";
    private static final String FILE_URL = "https://test-bucket.s3.amazonaws.com/test/file-key.jpg";

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client);
        ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET_NAME);
    }

    @Test
    void uploadFile_Success() throws IOException {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        MultipartFile file = new MockMultipartFile(
                "file",
                "test-file.jpg",
                FILE_CONTENT_TYPE,
                fileContent
        );

        when(s3Client.getUrl(eq(BUCKET_NAME), eq(FILE_KEY)))
                .thenReturn(new URL(FILE_URL));

        // Act
        String resultUrl = s3Service.uploadFile(file, FILE_KEY);

        // Assert
        verify(s3Client).putObject(putObjectRequestCaptor.capture());
        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();

        assertEquals(BUCKET_NAME, capturedRequest.getBucketName());
        assertEquals(FILE_KEY, capturedRequest.getKey());
        assertNotNull(capturedRequest.getInputStream());

        ObjectMetadata metadata = capturedRequest.getMetadata();
        assertEquals(FILE_CONTENT_TYPE, metadata.getContentType());
        assertEquals(fileContent.length, metadata.getContentLength());

        assertEquals(FILE_URL, resultUrl);
    }

    @Test
    void uploadFile_WithEmptyFile_ThrowsException() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty-file.jpg",
                FILE_CONTENT_TYPE,
                new byte[0]
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                s3Service.uploadFile(emptyFile, FILE_KEY)
        );

        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void uploadFile_WhenS3ClientThrowsException_PropagatesException() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file",
                "test-file.jpg",
                FILE_CONTENT_TYPE,
                "test content".getBytes()
        );

        doThrow(new RuntimeException("S3 Error"))
                .when(s3Client).putObject(any(PutObjectRequest.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                s3Service.uploadFile(file, FILE_KEY)
        );
    }

    @Test
    void uploadFile_WithNullFile_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                s3Service.uploadFile(null, FILE_KEY)
        );

        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void uploadFile_WithNullKey_ThrowsException() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file",
                "test-file.jpg",
                FILE_CONTENT_TYPE,
                "test content".getBytes()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                s3Service.uploadFile(file, null)
        );

        verify(s3Client, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void deleteFile_Success() {
        // Act
        s3Service.deleteFile(FILE_URL);

        // Assert
        verify(s3Client).deleteObject(eq(BUCKET_NAME), eq("test/file-key.jpg"));
    }

    @Test
    void deleteFile_WithInvalidUrl_ThrowsException() {
        // Arrange
        String invalidUrl = "invalid-url";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                s3Service.deleteFile(invalidUrl)
        );

        verify(s3Client, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void deleteFile_WhenS3ClientThrowsException_PropagatesException() {
        // Arrange
        doThrow(new RuntimeException("S3 Error"))
                .when(s3Client).deleteObject(anyString(), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                s3Service.deleteFile(FILE_URL)
        );
    }

    @Test
    void deleteFile_WithNullUrl_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                s3Service.deleteFile(null)
        );

        verify(s3Client, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void deleteFile_ExtractsCorrectKeyFromComplexUrl() {
        // Arrange
        String complexUrl = "https://test-bucket.s3.amazonaws.com/path/to/file/with/multiple/segments.jpg";
        String expectedKey = "path/to/file/with/multiple/segments.jpg";

        // Act
        s3Service.deleteFile(complexUrl);

        // Assert
        verify(s3Client).deleteObject(BUCKET_NAME, expectedKey);
    }
}