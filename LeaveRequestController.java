package com.example.leavemgmt.controller;

import com.example.leavemgmt.dto.LeaveRequestDTO;
import com.example.leavemgmt.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestDTO.Response> apply(@Valid @RequestBody LeaveRequestDTO.ApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.apply(request));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestDTO.Response>> getAll(
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(leaveRequestService.getByStatus(status));
        }
        return ResponseEntity.ok(leaveRequestService.getAll());
    }

    @GetMapping("/manager")
    public ResponseEntity<List<LeaveRequestDTO.Response>> getManagerLeaves(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(leaveRequestService.getManagerLeaves(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequestDTO.Response>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.getByEmployee(employeeId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestDTO.Response> approve(@PathVariable Long id,
                                                             @RequestBody LeaveRequestDTO.ActionRequest request) {
        return ResponseEntity.ok(leaveRequestService.approve(id, request));
    }

    @PutMapping("/{id}/manager-approve")
    public ResponseEntity<LeaveRequestDTO.Response> managerApprove(
            @PathVariable Long id, @RequestBody LeaveRequestDTO.ActionRequest request) {
        return ResponseEntity.ok(leaveRequestService.managerApprove(id, request));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestDTO.Response> reject(@PathVariable Long id,
                                                            @RequestBody LeaveRequestDTO.ActionRequest request) {
        return ResponseEntity.ok(leaveRequestService.reject(id, request));
    }

    @PutMapping("/{id}/manager-reject")
    public ResponseEntity<LeaveRequestDTO.Response> managerReject(
            @PathVariable Long id, @RequestBody LeaveRequestDTO.ActionRequest request) {
        return ResponseEntity.ok(leaveRequestService.managerReject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        leaveRequestService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
