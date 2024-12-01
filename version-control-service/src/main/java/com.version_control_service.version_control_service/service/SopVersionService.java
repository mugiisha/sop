package com.version_control_service.version_control_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.version_control_service.version_control_service.config.CloudinaryConfig;
import com.version_control_service.version_control_service.dto.ApiResponse;
import com.version_control_service.version_control_service.exception.SopNotFoundException;
import com.version_control_service.version_control_service.model.SopVersionModel;
import com.version_control_service.version_control_service.repository.SopVersionRepository;
import com.version_control_service.version_control_service.utils.SopVersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;

@Service
public class SopVersionService {

    @Autowired
    private SopVersionRepository sopVersionRepository;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Autowired
    private Cloudinary cloudinary;

    private static final Logger logger = LoggerFactory.getLogger(SopVersionService.class);



}

