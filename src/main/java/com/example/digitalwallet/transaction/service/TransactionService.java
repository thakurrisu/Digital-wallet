package com.example.digitalwallet.transaction.service;

import com.example.digitalwallet.transaction.dto.DepositRequest;
import com.example.digitalwallet.transaction.dto.TransactionResponse;
import com.example.digitalwallet.transaction.dto.TransferRequest;
import com.example.digitalwallet.transaction.dto.WithdrawRequest;
import com.example.digitalwallet.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {

    TransactionResponse deposit(UUID userId , DepositRequest depReq);

    TransactionResponse transfer(UUID userId , TransferRequest depReq);

    TransactionResponse withdraw(UUID userId , WithdrawRequest depReq);

    TransactionResponse getTransactionById(UUID userId , UUID transactionId);

    Page<TransactionResponse> getTransactionHistory(UUID userId , Pageable pageable);
}
