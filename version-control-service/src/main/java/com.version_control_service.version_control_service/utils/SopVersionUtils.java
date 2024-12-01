package com.version_control_service.version_control_service.utils;

import com.version_control_service.version_control_service.model.SopVersionModel;

public class SopVersionUtils {
    public static void setSopId(SopVersionModel existingSop, SopVersionModel updatedSop) {
        if (updatedSop.getSopId() != null) {
            existingSop.setSopId(updatedSop.getSopId());
        }
    }
}
