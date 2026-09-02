package com.harmonia.payment.repo;
import com.harmonia.payment.domain.ProcessedStripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProcessedStripeEventRepository extends JpaRepository<ProcessedStripeEvent, String> { }
