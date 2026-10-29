package com.bankingsystem.bankingsystem.repositories;

import com.bankingsystem.bankingsystem.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUser_Userid(Long userId);

    boolean existsByUser_Userid(Long userId);
}