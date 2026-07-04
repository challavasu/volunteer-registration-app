package com.volunteer.registration.repository;

import com.volunteer.registration.model.EmailTemplate;
import com.volunteer.registration.model.EmailTemplate.EmailTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByTemplateType(EmailTemplateType templateType);

    Optional<EmailTemplate> findByTemplateTypeAndEnabledTrue(EmailTemplateType templateType);
}
