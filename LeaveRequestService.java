package com.example.leavemgmt.service;

import com.example.leavemgmt.dto.LeaveRequestDTO;
import com.example.leavemgmt.entity.Employee;
import com.example.leavemgmt.entity.LeaveRequest;
import com.example.leavemgmt.exception.BadRequestException;
import com.example.leavemgmt.exception.ResourceNotFoundException;
import com.example.leavemgmt.repository.EmployeeRepository;
import com.example.leavemgmt.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRepo;
    private final EmployeeRepository employeeRepo;

    public LeaveRequestService(LeaveRequestRepository leaveRepo, EmployeeRepository employeeRepo) {
        this.leaveRepo = leaveRepo;
        this.employeeRepo = employeeRepo;
    }

    public LeaveRequestDTO.Response apply(LeaveRequestDTO.ApplyRequest request) {
        Employee employee = findEmployee(request.getEmployeeId());

        // Rule 1: start date cannot be after end date
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        // Rule 2: calculate number of days
        int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        // Rule 3: check leave balance
        int balance = getBalance(employee, request.getLeaveType());
        if (days > balance) {
            throw new BadRequestException("Insufficient leave balance. Available: " + balance + " days");
        }

        // Rule 4: check for overlapping leave
        if (leaveRepo.hasOverlappingLeave(request.getEmployeeId(), request.getStartDate(), request.getEndDate())) {
            throw new BadRequestException("You already have a leave request for overlapping dates");
        }

        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(employee);
        lr.setLeaveType(request.getLeaveType());
        lr.setStartDate(request.getStartDate());
        lr.setEndDate(request.getEndDate());
        lr.setNumberOfDays(days);
        lr.setReason(request.getReason());

        return LeaveRequestDTO.Response.from(leaveRepo.save(lr));
    }

    public List<LeaveRequestDTO.Response> getAll() {
        return leaveRepo.findAll().stream().map(LeaveRequestDTO.Response::from).collect(Collectors.toList());
    }

    public List<LeaveRequestDTO.Response> getByStatus(String status) {
        LeaveRequest.Status s = LeaveRequest.Status.valueOf(status.toUpperCase());
        return leaveRepo.findByStatus(s).stream().map(LeaveRequestDTO.Response::from).collect(Collectors.toList());
    }

    public List<LeaveRequestDTO.Response> getManagerLeaves(String status) {
        List<LeaveRequest> leaves = status == null || status.isBlank()
                ? leaveRepo.findByEmployeeRole(Employee.Role.EMPLOYEE)
                : leaveRepo.findByStatusAndEmployeeRole(
                        LeaveRequest.Status.valueOf(status.toUpperCase()), Employee.Role.EMPLOYEE);
        return leaves.stream().map(LeaveRequestDTO.Response::from).collect(Collectors.toList());
    }

    public LeaveRequestDTO.Response getById(Long id) {
        return LeaveRequestDTO.Response.from(findLeave(id));
    }

    public List<LeaveRequestDTO.Response> getByEmployee(Long employeeId) {
        findEmployee(employeeId); // validate exists
        return leaveRepo.findByEmployeeId(employeeId).stream()
                .map(LeaveRequestDTO.Response::from).collect(Collectors.toList());
    }

    public LeaveRequestDTO.Response approve(Long id, LeaveRequestDTO.ActionRequest request) {
        LeaveRequest lr = findLeave(id);
        if (lr.getStatus() != LeaveRequest.Status.PENDING) {
            throw new BadRequestException("Only pending requests can be approved");
        }

        // Deduct leave balance from employee
        Employee employee = lr.getEmployee();
        deductBalance(employee, lr.getLeaveType(), lr.getNumberOfDays());
        employeeRepo.save(employee);

        lr.setStatus(LeaveRequest.Status.APPROVED);
        lr.setManagerComment(request.getManagerComment());
        return LeaveRequestDTO.Response.from(leaveRepo.save(lr));
    }

    public LeaveRequestDTO.Response managerApprove(Long id, LeaveRequestDTO.ActionRequest request) {
        LeaveRequest lr = findLeave(id);
        ensureEmployeeRequest(lr);
        return approve(id, request);
    }

    public LeaveRequestDTO.Response reject(Long id, LeaveRequestDTO.ActionRequest request) {
        LeaveRequest lr = findLeave(id);
        if (lr.getStatus() != LeaveRequest.Status.PENDING) {
            throw new BadRequestException("Only pending requests can be rejected");
        }
        lr.setStatus(LeaveRequest.Status.REJECTED);
        lr.setManagerComment(request.getManagerComment());
        return LeaveRequestDTO.Response.from(leaveRepo.save(lr));
    }

    public LeaveRequestDTO.Response managerReject(Long id, LeaveRequestDTO.ActionRequest request) {
        LeaveRequest lr = findLeave(id);
        ensureEmployeeRequest(lr);
        return reject(id, request);
    }

    public void cancel(Long id) {
        LeaveRequest lr = findLeave(id);
        if (lr.getStatus() != LeaveRequest.Status.PENDING) {
            throw new BadRequestException("Only pending requests can be cancelled");
        }
        leaveRepo.deleteById(id);
    }

    public long countAll() { return leaveRepo.count(); }
    public long countByStatus(LeaveRequest.Status status) { return leaveRepo.countByStatus(status); }
    public long countByEmployeeAndStatus(Long empId, LeaveRequest.Status status) {
        return leaveRepo.countByEmployeeIdAndStatus(empId, status);
    }

    private Employee findEmployee(Long id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private LeaveRequest findLeave(Long id) {
        return leaveRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
    }

    private void ensureEmployeeRequest(LeaveRequest leave) {
        if (leave.getEmployee().getRole() != Employee.Role.EMPLOYEE) {
            throw new BadRequestException("Managers can only review employee leave requests");
        }
    }

    private int getBalance(Employee e, LeaveRequest.LeaveType type) {
        return switch (type) {
            case CASUAL -> e.getCasualLeave();
            case SICK -> e.getSickLeave();
            case EARNED -> e.getEarnedLeave();
        };
    }

    private void deductBalance(Employee e, LeaveRequest.LeaveType type, int days) {
        switch (type) {
            case CASUAL -> e.setCasualLeave(e.getCasualLeave() - days);
            case SICK -> e.setSickLeave(e.getSickLeave() - days);
            case EARNED -> e.setEarnedLeave(e.getEarnedLeave() - days);
        }
    }
}
