package com.bankingsystem.bankingsystem;

import com.bankingsystem.bankingsystem.entity.Employee;
import com.bankingsystem.bankingsystem.entity.User;
import com.bankingsystem.bankingsystem.exceptions.ResourceNotFoundException;
import com.bankingsystem.bankingsystem.repositories.EmployeeRepository;
import com.bankingsystem.bankingsystem.services.EmployeeServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServicesTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServices employeeServices;

    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validEmployee = buildValidEmployee();
    }

    private Employee buildValidEmployee() {
        User user = new User();
        user.setUserid(200L);

        Employee e = new Employee();
        e.setUser(user);
        e.setFullname("Employee One");
        e.setDesignation("Clerk");
        e.setBranch("HYD");
        return e;
    }

    // -------------------- registerEmployee --------------------

    @Test
    @DisplayName("registerEmployee: throws if user is null")
    void registerEmployee_userNull_throws() {
        validEmployee.setUser(null);
        assertThrows(IllegalArgumentException.class,
                () -> employeeServices.registerEmployee(validEmployee));
        verifyNoInteractions(employeeRepository);
    }

    @Test
    @DisplayName("registerEmployee: throws if userId is null")
    void registerEmployee_userIdNull_throws() {
        validEmployee.getUser().setUserid(null);
        assertThrows(IllegalArgumentException.class,
                () -> employeeServices.registerEmployee(validEmployee));
        verifyNoInteractions(employeeRepository);
    }

    @Test
    @DisplayName("registerEmployee: throws if employee already exists for userId")
    void registerEmployee_alreadyExists_throws() {
        when(employeeRepository.existsByUser_Userid(200L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> employeeServices.registerEmployee(validEmployee));

        verify(employeeRepository).existsByUser_Userid(200L);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerEmployee: saves if valid and not existing")
    void registerEmployee_success_saves() {
        when(employeeRepository.existsByUser_Userid(200L)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeServices.registerEmployee(validEmployee);

        assertNotNull(result);
        verify(employeeRepository).save(validEmployee);
    }

    // -------------------- getEmployeeById --------------------

    @Test
    @DisplayName("getEmployeeById: returns employee if found")
    void getEmployeeById_found() {
        validEmployee.setEmployeeid(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));

        Employee result = employeeServices.getEmployeeById(1L);
        assertEquals(1L, result.getEmployeeid());
    }

    @Test
    @DisplayName("getEmployeeById: throws if not found")
    void getEmployeeById_notFound_throws() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> employeeServices.getEmployeeById(99L));
    }

    // -------------------- getEmployeeByUserId --------------------

    @Test
    @DisplayName("getEmployeeByUserId: throws if not found")
    void getEmployeeByUserId_notFound_throws() {
        when(employeeRepository.findByUser_Userid(200L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> employeeServices.getEmployeeByUserId(200L));
    }

    // -------------------- getAllEmployees --------------------

    @Test
    @DisplayName("getAllEmployees: returns list")
    void getAllEmployees_returnsList() {
        when(employeeRepository.findAll()).thenReturn(List.of(validEmployee));
        assertEquals(1, employeeServices.getAllEmployees().size());
        verify(employeeRepository).findAll();
    }

    // -------------------- updateEmployee --------------------

    @Test
    @DisplayName("updateEmployee: updates only non-null fields")
    void updateEmployee_updatesNonNull() {
        validEmployee.setEmployeeid(10L);

        Employee updated = new Employee();
        updated.setFullname("New Emp Name");
        updated.setDesignation(null); // should not override
        updated.setBranch("BLR");

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeServices.updateEmployee(10L, updated);

        assertEquals("New Emp Name", result.getFullname());
        assertEquals("Clerk", result.getDesignation()); // unchanged
        assertEquals("BLR", result.getBranch());
        verify(employeeRepository).save(validEmployee);
    }

    // -------------------- deleteEmployee --------------------

    @Test
    @DisplayName("deleteEmployee: deletes after fetching")
    void deleteEmployee_deletes() {
        validEmployee.setEmployeeid(50L);
        when(employeeRepository.findById(50L)).thenReturn(Optional.of(validEmployee));

        employeeServices.deleteEmployee(50L);

        verify(employeeRepository).delete(validEmployee);
    }

    // -------------------- existsByUserId --------------------

    @Test
    @DisplayName("existsByUserId: delegates to repository")
    void existsByUserId_delegates() {
        when(employeeRepository.existsByUser_Userid(200L)).thenReturn(true);
        assertTrue(employeeServices.existsByUserId(200L));
        verify(employeeRepository).existsByUser_Userid(200L);
    }
}