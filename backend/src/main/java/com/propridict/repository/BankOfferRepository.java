package com.propridict.repository;

import com.propridict.model.BankOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankOfferRepository extends JpaRepository<BankOffer, Long> {

    List<BankOffer> findByLoanType(String loanType);

    List<BankOffer> findByLoanTypeAndCustomerIn(String loanType, List<String> customerTypes);
}
