package com.example.leavemgmt.controller;

import com.example.leavemgmt.dto.EmployeeDTO;
import com.example.leavemgmt.entity.LeaveRequest;
import com.example.leavemgmt.repository.EmployeeRepository;
import com.example.leavemgmt.service.EmployeeService;
import com.example.leavemgmt.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final LeaveRequestService leaveRequestService;
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeService employeeService,
                               LeaveRequestService leaveRequestService,
                               EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.leaveRequestService = leaveRequestService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<EmployeeDTO.Response> register(@Valid @RequestBody EmployeeDTO.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<EmployeeDTO.Response> login(@Valid @RequestBody EmployeeDTO.LoginRequest request) {
        return ResponseEntity.ok(employeeService.login(request));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO.Response>> getAll() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO.Response> update(@PathVariable Long id,
                                                        @RequestBody EmployeeDTO.UpdateRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Dashboard stats for a specific employee
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Long>> getEmployeeStats(@PathVariable Long id) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) leaveRequestService.getByEmployee(id).size());
        stats.put("pending", leaveRequestService.countByEmployeeAndStatus(id, LeaveRequest.Status.PENDING));
        stats.put("approved", leaveRequestService.countByEmployeeAndStatus(id, LeaveRequest.Status.APPROVED));
        stats.put("rejected", leaveRequestService.countByEmployeeAndStatus(id, LeaveRequest.Status.REJECTED));
        return ResponseEntity.ok(stats);
    }

    // Dashboard stats for admin
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Long>> getAdminStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalEmployees", employeeRepository.countByRole(com.example.leavemgmt.entity.Employee.Role.EMPLOYEE));
        stats.put("totalLeaves", leaveRequestService.countAll());
        stats.put("pending", leaveRequestService.countByStatus(LeaveRequest.Status.PENDING));
        stats.put("approved", leaveRequestService.countByStatus(LeaveRequest.Status.APPROVED));
        stats.put("rejected", leaveRequestService.countByStatus(LeaveRequest.Status.REJECTED));
        return ResponseEntity.ok(stats);
    }
}
