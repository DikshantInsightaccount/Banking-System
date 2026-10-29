package com.bankingsystem.bankingsystem;

import com.bankingsystem.bankingsystem.entity.Employee;
import com.bankingsystem.bankingsystem.services.EmployeeServiceImp;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    EmployeeServiceImp employeeService;

    // -------------------- Register --------------------

    @Test
    void testRegisterEmployee_success() throws Exception {
        Employee saved = new Employee();
        saved.setEmployeeid(1L);

        when(employeeService.registerEmployee(any(Employee.class))).thenReturn(saved);

        mvc.perform(post("/api/employees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullname\":\"A\",\"designation\":\"DEV\",\"branch\":\"HYD\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employeeid").value(1));

        verify(employeeService).registerEmployee(any(Employee.class));
    }

    // -------------------- Get --------------------

    @Test
    void testGetEmployeeById_success() throws Exception {
        Employee employee = new Employee();
        employee.setEmployeeid(1L);

        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mvc.perform(get("/api/employees/{employeeId}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employeeid").value(1));

        verify(employeeService).getEmployeeById(1L);
    }

    @Test
    void testGetEmployeeByUserId_success() throws Exception {
        Employee employee = new Employee();
        employee.setEmployeeid(2L);

        when(employeeService.getEmployeeByUserId(101L)).thenReturn(employee);

        mvc.perform(get("/api/employees/by-user/{userId}", 101))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employeeid").value(2));

        verify(employeeService).getEmployeeByUserId(101L);
    }

    @Test
    void testGetAllEmployees_empty() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mvc.perform(get("/api/employees/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(employeeService).getAllEmployees();
    }

    @Test
    void testGetAllEmployees_nonEmpty() throws Exception {
        Employee e1 = new Employee();
        e1.setEmployeeid(1L);

        Employee e2 = new Employee();
        e2.setEmployeeid(2L);

        when(employeeService.getAllEmployees()).thenReturn(List.of(e1, e2));

        mvc.perform(get("/api/employees/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employeeid").value(1))
                .andExpect(jsonPath("$[1].employeeid").value(2));

        verify(employeeService).getAllEmployees();
    }

    // -------------------- Update --------------------

    @Test
    void testUpdateEmployee_success() throws Exception {
        Employee updatedReturn = new Employee();
        updatedReturn.setEmployeeid(10L);

        // Stub with matchers (OK)
        when(employeeService.updateEmployee(any(Long.class), any(Employee.class)))
                .thenReturn(updatedReturn);

        mvc.perform(put("/api/employees/update/{employeeId}", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullname\":\"New\",\"designation\":\"SDE\",\"branch\":\"BLR\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employeeid").value(10));

        // ✅ No eq() here → use captors instead (Sonar-friendly)
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employeeService).updateEmployee(idCaptor.capture(), employeeCaptor.capture());

        assertEquals(10L, idCaptor.getValue());
    }

    // -------------------- Delete --------------------

    @Test
    void testDeleteEmployee_success() throws Exception {
        doNothing().when(employeeService).deleteEmployee(10L);

        mvc.perform(delete("/api/employees/delete/{employeeId}", 10))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee deleted successfully"));

        verify(employeeService).deleteEmployee(10L);
    }

    // -------------------- Exists --------------------

    @Test
    void testExistsByUserId_true() throws Exception {
        when(employeeService.existsByUserId(5L)).thenReturn(true);

        mvc.perform(get("/api/employees/exists/user")
                        .param("userId", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(employeeService).existsByUserId(5L);
    }

    @Test
    void testExistsByUserId_false() throws Exception {
        when(employeeService.existsByUserId(6L)).thenReturn(false);

        mvc.perform(get("/api/employees/exists/user")
                        .param("userId", "6"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(employeeService).existsByUserId(6L);
    }
}