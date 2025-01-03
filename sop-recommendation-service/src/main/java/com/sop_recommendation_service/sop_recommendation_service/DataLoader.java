package com.sop_recommendation_service.sop_recommendation_service;
import com.sop_recommendation_service.sop_recommendation_service.models.SOP;
import com.sop_recommendation_service.sop_recommendation_service.repository.SOPRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class DataLoader {

    @Bean
    @Profile("dev")  // Only run in dev profile
    CommandLineRunner loadData(SOPRepository sopRepository) {
        return args -> {
            // First clear existing data
            sopRepository.deleteAll()
                    .thenMany(Flux.fromIterable(generateSampleSOPs()))
                    .flatMap(sopRepository::save)
                    .doOnComplete(() -> System.out.println("Sample SOPs loaded successfully!"))
                    .doOnError(e -> System.err.println("Error loading sample SOPs: " + e.getMessage()))
                    .subscribe();
        };
    }

    private List<SOP> generateSampleSOPs() {
        return Arrays.asList(
                // IT Department SOPs
                createSOP(
                        "IT Security Protocol",
                        "IT",
                        "Security",
                        """
                        1. Purpose
                        This SOP establishes security protocols for IT systems.
        
                        2. Scope
                        Applies to all IT staff and system administrators.
        
                        3. Procedures
                        - Daily system checks
                        - Weekly security audits
                        - Monthly penetration testing
                        """,
                        Arrays.asList("security", "IT", "protocols"),
                        "ACTIVE"
                ),

                createSOP(
                        "Network Maintenance Procedure",
                        "IT",
                        "Infrastructure",
                        """
                        1. Purpose
                        To maintain network infrastructure effectively.
        
                        2. Scope
                        Network administrators and IT support staff.
        
                        3. Procedures
                        - Regular network monitoring
                        - Bandwidth optimization
                        - Hardware maintenance schedule
                        """,
                        Arrays.asList("network", "maintenance", "infrastructure"),
                        "ACTIVE"
                ),

                // HR Department SOPs
                createSOP(
                        "Employee Onboarding Process",
                        "HR",
                        "Recruitment",
                        """
                        1. Purpose
                        Standardize the onboarding process for new employees.
        
                        2. Scope
                        All HR staff involved in employee onboarding.
        
                        3. Procedures
                        - Documentation collection
                        - System access setup
                        - Training schedule
                        """,
                        Arrays.asList("onboarding", "recruitment", "HR"),
                        "ACTIVE"
                ),

                // Finance Department SOPs
                createSOP(
                        "Expense Approval Workflow",
                        "Finance",
                        "Accounting",
                        """
                        1. Purpose
                        Define the expense approval process.
        
                        2. Scope
                        All finance staff and department managers.
        
                        3. Procedures
                        - Expense submission guidelines
                        - Approval hierarchy
                        - Reimbursement process
                        """,
                        Arrays.asList("expenses", "approval", "finance"),
                        "ACTIVE"
                ),

                // Customer Service SOPs
                createSOP(
                        "Customer Complaint Resolution",
                        "Customer Service",
                        "Support",
                        """
                        1. Purpose
                        Standardize customer complaint handling process.
        
                        2. Scope
                        All customer service representatives.
        
                        3. Procedures
                        - Complaint logging
                        - Resolution steps
                        - Escalation protocol
                        """,
                        Arrays.asList("customer service", "complaints", "resolution"),
                        "ACTIVE"
                ),

                // Operations Department SOPs
                createSOP(
                        "Warehouse Safety Guidelines",
                        "Operations",
                        "Safety",
                        """
                        1. Purpose
                        Ensure safe warehouse operations.
        
                        2. Scope
                        All warehouse staff and supervisors.
        
                        3. Procedures
                        - Safety equipment usage
                        - Emergency protocols
                        - Incident reporting
                        """,
                        Arrays.asList("safety", "warehouse", "operations"),
                        "ACTIVE"
                )
        );
    }

    private SOP createSOP(String title, String department, String category, String content, List<String> tags, String status) {
        SOP sop = new SOP();
        sop.setId(UUID.randomUUID().toString());
        sop.setTitle(title);
        sop.setDepartment(department);
        sop.setCategory(category);
        sop.setContent(content);
        sop.setTags(tags);
        sop.setStatus(status);
        sop.setCreatedAt(LocalDateTime.now());
        sop.setUpdatedAt(LocalDateTime.now());
        sop.setVersion(1);
        sop.setCreatedBy("system");
        sop.setViewCount(0);
        return sop;
    }
}
