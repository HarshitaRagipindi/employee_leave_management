package com.example.leavemgmt.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Data
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDays;
    private String reason;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDate appliedDate = LocalDate.now();
    private String managerComment;

    public enum LeaveType {
        CASUAL, SICK, EARNED
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
