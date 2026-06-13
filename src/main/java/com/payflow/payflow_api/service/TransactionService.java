package com.payflow.payflow_api.service;


import com.payflow.payflow_api.entity.Transaction;
import com.payflow.payflow_api.entity.User;
import com.payflow.payflow_api.repository.TransactionRepository;
import com.payflow.payflow_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    // Spring Boot scans for @Repository beans and injects them here automatically.
    // At startup, it creates a proxy implementation of TransactionRepository and wires it into this field.

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction sendMoney(Transaction transaction) {
        // Find sender and receiver
        User sender = userRepository.findByUpiId(transaction.getSenderUpiId());
        User receiver = userRepository.findByUpiId(transaction.getReceiverUpiId());

        if (sender == null || receiver == null) {
            throw new RuntimeException("Sender or receiver not found");
        }

        if (sender.getBalance() < transaction.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        // Update balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        receiver.setBalance(receiver.getBalance() + transaction.getAmount());

        userRepository.save(sender);
        userRepository.save(receiver);

        // Add timestamp
        transaction.setTimestamp(LocalDateTime.now());

        

        return transactionRepository.save(transaction);
    }
}

