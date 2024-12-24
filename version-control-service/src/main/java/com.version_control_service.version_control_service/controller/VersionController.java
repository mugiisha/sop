package com.version_control_service.version_control_service.controller;

import com.version_control_service.version_control_service.dto.RevertVersionDto;
import com.version_control_service.version_control_service.model.Version;
import com.version_control_service.version_control_service.service.SopVersionService;
import com.version_control_service.version_control_service.utils.Response;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/versions")
public class VersionController {
    private final SopVersionService sopVersionService;

    @Autowired
    public VersionController(SopVersionService sopVersionService) {
        this.sopVersionService = sopVersionService;
    }

    @PutMapping("/{sopId}")
    public Response<Version> revertVersionsBySopId(@PathVariable String sopId,
                                                      @Valid @RequestBody RevertVersionDto revertVersionDto) {
        Version version = sopVersionService.revertSopVersion(sopId, revertVersionDto.getVersionNumber());
        return new Response<>(true, "Versions reverted successfully", version);
    }

    @GetMapping("/{sopId}")
    public Response<List<Version>> getVersionsBySopId(@PathVariable String sopId) {
        List<Version> versions = sopVersionService.getSopVersionsById(sopId);
        return new Response<>(true, "Versions fetched successfully", versions);
    }

    @GetMapping("/{sopId}/compare")
    public Response<List<Version>> compareSopVersions(@RequestParam(name = "firstVersion") Float firstVersion,
                                                      @RequestParam(name = "secondVersion") Float secondVersion,
                                                      @PathVariable String sopId) {
        List<Version> versions = sopVersionService.compareSopVersions(sopId, firstVersion, secondVersion);
        return new Response<>(true, "Versions to compare fetched successfully", versions);
    }
}
