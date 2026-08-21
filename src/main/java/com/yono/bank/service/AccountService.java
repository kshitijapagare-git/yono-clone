package com.yono.bank.service;

import com.yono.bank.entity.Account;
import com.yono.bank.exception.ResourceNotFoundException;
import com.yono.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account create(Account account) {
        return accountRepository.save(account);
    }

    public Account get(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    public List<Account> list() {
        return accountRepository.findAll();
    }

    public Account update(Long id, Account update) {
        Account existing = get(id);
        existing.setCustomer(update.getCustomer());
        existing.setAccountNumber(update.getAccountNumber());
        existing.setType(update.getType());
        existing.setBalance(update.getBalance());
        existing.setStatus(update.getStatus());
        return accountRepository.save(existing);
    }

    public void delete(Long id) {
        Account existing = get(id);
        accountRepository.delete(existing);
    }
}
