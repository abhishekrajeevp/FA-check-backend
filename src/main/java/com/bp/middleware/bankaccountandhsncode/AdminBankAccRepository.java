package com.bp.middleware.bankaccountandhsncode;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminBankAccRepository extends JpaRepository<AdminBankAccount, Integer>{

	AdminBankAccount findByAccountNumber(String accountNumber);

	AdminBankAccount findByStatus(boolean b);

	
}
