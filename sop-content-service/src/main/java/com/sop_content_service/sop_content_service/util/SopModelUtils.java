package com.sop_content_service.sop_content_service.util;

import com.sop_content_service.sop_content_service.model.SopModel;

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

    public static void updateVisibility(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getVisibility() != null) {
            existingSop.setVisibility(updatedSop.getVisibility());
        }
    }

    public static void updateAuthor(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getAuthor() != null) {
            existingSop.setAuthor(updatedSop.getAuthor());
        }
    }

    public static void updateReviewer(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getReviewer() != null) {
            existingSop.setReviewer(updatedSop.getReviewer());
        }
    }

    public static void updateApprover(SopModel existingSop, SopModel updatedSop) {
        if (updatedSop.getApprover() != null) {
            existingSop.setApprover(updatedSop.getApprover());
        }
    }
}
