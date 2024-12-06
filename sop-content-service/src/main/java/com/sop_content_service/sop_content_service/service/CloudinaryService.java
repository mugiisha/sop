//package com.sop_content_service.sop_content_service.service;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Service
//public class CloudinaryService {
//
//    @Autowired
//    private Cloudinary cloudinary;
//
//    public Map<?, ?> uploadFile(MultipartFile file) throws IOException {
//        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
//    }
//}
