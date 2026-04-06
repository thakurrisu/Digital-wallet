package com.example.digitalwallet.transaction.service;

import com.example.digitalwallet.common.exception.ErrorCode;
import com.example.digitalwallet.common.exception.WalletException;
import com.example.digitalwallet.transaction.dto.DepositRequest;
import com.example.digitalwallet.transaction.dto.TransactionResponse;
import com.example.digitalwallet.transaction.dto.TransferRequest;
import com.example.digitalwallet.transaction.dto.WithdrawRequest;
import com.example.digitalwallet.transaction.model.Transaction;
import com.example.digitalwallet.transaction.model.TransactionStatus;
import com.example.digitalwallet.transaction.model.TransactionType;
import com.example.digitalwallet.transaction.repo.TransactiontRepository;
import com.example.digitalwallet.wallet.dto.WalletResponse;
import com.example.digitalwallet.wallet.model.Wallet;
import com.example.digitalwallet.wallet.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService{

    private final TransactiontRepository transactionRepo;

    private final WalletService walletService;

    public TransactionServiceImpl(TransactiontRepository transactionRepo,WalletService walletService) {
        this.transactionRepo = transactionRepo;
        this.walletService=walletService;
    }

    @Override
    public TransactionResponse deposit(UUID userId, DepositRequest depReq) {
        log.info("Deposit request received for userId={}, amount={}, refId={}", userId, depReq.getAmount(), depReq.getReferenceId());
        //check if ref id exist
        String refId = depReq.getReferenceId();
        Optional<Transaction> existingTransaction = transactionRepo.findByReferenceId(refId);
        if(existingTransaction.isPresent())
        {
            log.info("Duplicate deposit detected for refId={}, returning existing transaction", refId);
            return TransactionResponse.fromTransaction(existingTransaction.get());
        }
        //get wallet
        Wallet wallet = walletService.getActiveWalletByUserId(userId);

        //get snapshot before
        BigDecimal balance_before = wallet.getBalance();
        //deposit
        walletService.credit(wallet.getId(),depReq.getAmount());

        //TO DO throw exception and see what happens

        // TODO Phase 5:
        // Replace mathematical balanceAfter with:
        // entityManager.flush() + entityManager.clear()
        // then refetch wallet for accurate snapshot
        // under concurrent transactions
       // Wallet updatedWallet = walletService.getActiveWalletByUserId(userId);
        BigDecimal balance_after = balance_before.add(depReq.getAmount());
        Transaction transaction =buildTransaction(wallet,depReq.getAmount(),TransactionStatus.PENDING,balance_before,balance_after,
                TransactionType.DEPOSIT,refId,"Deposit",null);
        transactionRepo.save(transaction);
        log.info("Deposit successful for userId={}, walletId={}, amount={}, balanceBefore={}, balanceAfter={}", userId, wallet.getId(), depReq.getAmount(), balance_before, balance_after);
        return TransactionResponse.fromTransaction(transaction);
    }


    @Override
    @Transactional(readOnly = true)
    public TransactionResponse transfer(UUID userId, TransferRequest depReq) {
        log.info("Transfer request received for userId={}, amount={}", userId, depReq.getAmount());
        String refId = depReq.getReferenceId();
        BigDecimal amount = depReq.getAmount();
        //idemcheck
        var existingTransaction = transactionRepo.findByReferenceId(refId);
        if(existingTransaction.isPresent()){
            log.info("Duplicate transfer detected for refId={}, returning existing transaction", refId);
            return TransactionResponse.fromTransaction(existingTransaction.get());
        }
        //credit from - sender wallet
        //credit from account
        Wallet senderWallet = walletService.getActiveWalletByUserId(userId);
        BigDecimal senderWallet_Before = senderWallet.getBalance();

        Wallet receiverWallet = walletService.getActiveWalletById(depReq.getReceiverWalletId());
        BigDecimal receiverWallet_Before = receiverWallet.getBalance();

        if(senderWallet.getId().equals(receiverWallet.getId())){
            log.info("Self-transfer attempted by userId={}, walletId={}", userId, senderWallet.getId());
            throw new WalletException(ErrorCode.SELF_TRANSFER);
        }
        log.info("Initiating transfer: senderWalletId={}, receiverWalletId={}, amount={}", senderWallet.getId(), receiverWallet.getId(), amount);
        walletService.debit(senderWallet.getId(),amount);
        walletService.credit(depReq.getReceiverWalletId(),depReq.getAmount());

        BigDecimal senderWallet_After = receiverWallet_Before.subtract(depReq.getAmount());
        BigDecimal receiverWallet_After = receiverWallet_Before.add(depReq.getAmount());

        Transaction transaction_out =buildTransaction(senderWallet,depReq.getAmount(),TransactionStatus.PENDING,senderWallet_Before,senderWallet_After,
                TransactionType.TRANSFER_OUT,refId,"Credit",depReq.getReceiverWalletId());

        Transaction transaction_in =buildTransaction(receiverWallet,depReq.getAmount(),TransactionStatus.PENDING,receiverWallet_Before,receiverWallet_After,
                TransactionType.TRANSFER_IN,refId,"Deposit",senderWallet.getId());
        transactionRepo.save(transaction_in);
        transactionRepo.save(transaction_out);
        log.info("Transfer successful: senderWalletId={} ({}->{}), receiverWalletId={} ({}->{}), amount={}, refId={}", senderWallet.getId(), senderWallet_Before, senderWallet_After, receiverWallet.getId(), receiverWallet_Before, receiverWallet_After, amount, refId);

        return TransactionResponse.fromTransaction(transaction_out);
    }

    @Override
    public TransactionResponse withdraw(UUID userId, WithdrawRequest depReq) {
        log.info("Withdraw request received for userId={}, amount={}, refId={}", userId, depReq.getAmount(), depReq.getReferenceId());
        String refId = depReq.getReferenceId();
        var existingTransaction = transactionRepo.findByReferenceId(refId);
        if(existingTransaction.isPresent()) {
            log.info("Duplicate withdraw detected for refId={}, returning existing transaction", refId);
            return TransactionResponse.fromTransaction(existingTransaction.get());
        }
        //get wallet
        Wallet wallet = walletService.getActiveWalletByUserId(userId);

        //get snapshot before
        BigDecimal balance_before = wallet.getBalance();
        //debit
        walletService.debit(wallet.getId(),depReq.getAmount());

        //TO DO : throw exception and see what happens

        //get sanpshot after
       // Wallet updatedWallet = walletService.getActiveWalletByUserId(userId);
        BigDecimal balance_after = balance_before.subtract(depReq.getAmount());
        Transaction transaction =buildTransaction(wallet,depReq.getAmount(),TransactionStatus.PENDING,balance_before,balance_after,
                TransactionType.WITHDRAW,refId,"credit",null);
        transactionRepo.save(transaction);
        log.info("Withdraw successful for userId={}, walletId={}, amount={}, balanceBefore={}, balanceAfter={}", userId, wallet.getId(), depReq.getAmount(), balance_before, balance_after);
        return TransactionResponse.fromTransaction(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID userId, UUID transactionId) {
        log.info("Fetching transaction by id={} for userId={}", transactionId, userId);
        WalletResponse wallet = walletService.getWalletByUserId(userId);
        Transaction transaction = transactionRepo.findByIdAndWalletId(userId,wallet.getId())
                        .orElseThrow(()->
                                    new WalletException(ErrorCode.TRANSACTION_NOT_FOUND,"Transaction not found for transaction Id : "  + transactionId)
                        );
       return TransactionResponse.fromTransaction(transaction);// return transactionRepo.findByIdAndWalletId();
    }

    @Override
    public Page<TransactionResponse> getTransactionHistory(UUID userId , Pageable pageable) {
        log.info("Fetching transaction history for userId={}", userId);
        WalletResponse wallet = walletService.getWalletByUserId(userId);
        Page<Transaction> pagedTransaction =  transactionRepo.findByWalletIdOrderByCreatedAtDesc(wallet.getId(),pageable);
        return pagedTransaction.map(transaction -> TransactionResponse.fromTransaction(transaction));
    }

    private Transaction buildTransaction(Wallet wallet , BigDecimal amount,TransactionStatus transactionStatus
            ,BigDecimal balance_before,BigDecimal balance_after,TransactionType transactionType,String refId,String desc,UUID relatedWalletId)
    {
        Transaction transaction =  Transaction.builder()
                . wallet(wallet)
                .amount(amount)
                .transactionStatus(transactionStatus)
                .transactionType(transactionType)
                .balanceBefore(balance_before)
                .balanceAfter(balance_after)
                .currency(wallet.getCurrency())
                .referenceId(refId)
                .description(desc)
                .relatedWalletId(relatedWalletId)
                .build();
        return transaction;
    }
}
