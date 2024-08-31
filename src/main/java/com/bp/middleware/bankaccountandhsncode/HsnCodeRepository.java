package com.bp.middleware.bankaccountandhsncode;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HsnCodeRepository extends JpaRepository<HsnCode, Integer>{

	HsnCode findByHsnNumber(String hsnNumber);

	HsnCode findByStatus(boolean b);

}
