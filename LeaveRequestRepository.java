package com.example.leavemgmt.repository;

import com.example.leavemgmt.entity.LeaveRequest;
import com.example.leavemgmt.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByStatus(LeaveRequest.Status status);

       List<LeaveRequest> findByEmployeeRole(Employee.Role role);

       List<LeaveRequest> findByStatusAndEmployeeRole(LeaveRequest.Status status, Employee.Role role);

    long countByStatus(LeaveRequest.Status status);

    long countByEmployeeIdAndStatus(Long employeeId, LeaveRequest.Status status);

    // Check for overlapping leave dates for the same employee (excluding REJECTED)
    @Query("SELECT COUNT(lr) > 0 FROM LeaveRequest lr WHERE lr.employee.id = :empId " +
           "AND lr.status != 'REJECTED' " +
           "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    boolean hasOverlappingLeave(@Param("empId") Long empId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}
