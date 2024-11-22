package com.sop_content_service.sop_content_service.util;

import com.sop_content_service.sop_content_service.model.SopModel;

import java.util.List;

public class SopModelUtils {

    public static void updateTitle(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getTitle() != null) {
            existingSop.setTitle(updatedSop.getTitle());
        }
    }

    public static void updateDescription(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getDescription() != null) {
            existingSop.setDescription(updatedSop.getDescription());
        }
    }

    public static void updateNewSection(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getNewSection() != null) {
            existingSop.setNewSection(updatedSop.getNewSection());
        }
    }

    public static void updateCode(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getCode() != null) {
            existingSop.setCode(updatedSop.getCode());
        }
    }

    public static void updateDocumentUrl(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getDocumentUrl() != null) {
            existingSop.setDocumentUrl(updatedSop.getDocumentUrl());
        }
    }

    public static void updateImageUrl(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getImageUrl() != null) {
            existingSop.setImageUrl(updatedSop.getImageUrl());
        }
    }

    public static void updateStatus(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getStatus() != null) {
            existingSop.setStatus(updatedSop.getStatus());
        }
    }

    public static void updateVersion(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getVersion() != null) {
            existingSop.setVersion(updatedSop.getVersion());
        }
    }

    public static void updateVisibility(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getVisibility() != null) {
            existingSop.setVisibility(updatedSop.getVisibility());
        }
    }

    public static void updateAuthors(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getAuthors() != null) {
            existingSop.setAuthors(updatedSop.getAuthors());
        }
    }

    public static void updateReviewers(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getReviewers() != null) {
            existingSop.setReviewers(updatedSop.getReviewers());
        }
    }

    public static void updateApprovers(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getApprovers() != null) {
            existingSop.setApprovers(updatedSop.getApprovers());
        }
    }

    public static void updateCategory(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getCategory() != null) {
            existingSop.setCategory(updatedSop.getCategory());
        }
    }
}
