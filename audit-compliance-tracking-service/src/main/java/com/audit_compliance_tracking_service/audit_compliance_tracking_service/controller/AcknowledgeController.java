package com.audit_compliance_tracking_service.audit_compliance_tracking_service.controller;

import com.audit_compliance_tracking_service.audit_compliance_tracking_service.dto.ApiResponse;
import com.audit_compliance_tracking_service.audit_compliance_tracking_service.model.AcknowledgeModel;
import com.audit_compliance_tracking_service.audit_compliance_tracking_service.service.AcknowledgeService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/acknowledge")
public class AcknowledgeController {
    private static final Logger log = LoggerFactory.getLogger(AcknowledgeController.class);

    @Autowired
    private AcknowledgeService acknowledgeService;

    /**
     * Acknowledge an SOP.
     * @param sopId SOP ID.
     * @return Acknowledged SOP response.
     */
    @PostMapping("/{sopId}")
    public ResponseEntity<ApiResponse<AcknowledgeModel>> acknowledgeSOP(
            @PathVariable String sopId,
            HttpServletRequest request) {
        return acknowledgeService.acknowledgeSOP(sopId, request);
    }

    /**
     * Get all acknowledgements.
     * @return List of all acknowledgements.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AcknowledgeModel>>> getAllAcknowledged() {
        return acknowledgeService.getAllAcknowledged();
    }
}
