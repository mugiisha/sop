package com.sop_content_service.sop_content_service.util;

import com.sop_content_service.sop_content_service.model.SopModel;

import java.util.List;

public class SopModelUtils {

    public static void updateDocumentUrls(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getDocumentUrls() != null) {
            existingSop.setDocumentUrls(updatedSop.getDocumentUrls());
        }
    }

    public static void updateCoverUrl(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getCoverUrl() != null) {
            existingSop.setCoverUrl(updatedSop.getCoverUrl());
        }
    }

    public static void updateCreatedAt(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getCreatedAt() != null) {
            existingSop.setCreatedAt(updatedSop.getCreatedAt());
        }
    }

    public static void updateUpdatedAt(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getUpdatedAt() != null) {
            existingSop.setUpdatedAt(updatedSop.getUpdatedAt());
        }
    }
}
