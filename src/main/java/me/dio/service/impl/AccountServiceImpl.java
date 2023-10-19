package me.dio.service.impl;

import me.dio.domain.model.Account;
import me.dio.domain.repository.AccountRepository;
import me.dio.service.AccountService;
import me.dio.service.exception.BusinessException;
import me.dio.service.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Optional.ofNullable;

@Service
public class AccountServiceImpl implements AccountService {

    /**
     * ID de usuário utilizado na Santander Dev Week 2023.
     * Por isso, vamos criar algumas regras para mantê-lo integro.
     */
    private static final Long UNCHANGEABLE_ACCOUNT_ID = 1L;

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return this.accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Account findById(Long id) {
        return this.accountRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Account create(Account accountToCreate) {
        ofNullable(accountToCreate).orElseThrow(() -> new BusinessException("Account to create must not be null."));
        ofNullable(accountToCreate.getAgency()).orElseThrow(() -> new BusinessException("Account agencey must not be null."));
        ofNullable(accountToCreate.getNumber()).orElseThrow(() -> new BusinessException("Account number must not be null."));
        ofNullable(accountToCreate.getBalance()).orElseThrow(() -> new BusinessException("Account balance must not be null."));


        this.validateChangeableId(accountToCreate.getId(), "created");
        if (accountRepository.existsByAccountNumber(accountToCreate.getNumber())) {
            throw new BusinessException("This account number already exists.");
        }
        if (accountRepository.existsByCardNumber(accountToCreate.getAgency())) {
            throw new BusinessException("This agency number already exists.");
        }
        return this.accountRepository.save(accountToCreate);
    }

    @Transactional
    public Account update(Long id, Account accountToUpdate) {
        this.validateChangeableId(id, "updated");
        Account dbAccount = this.findById(id);
        if (!dbAccount.getId().equals(accountToUpdate.getId())) {
            throw new BusinessException("Update IDs must be the same.");
        }

        dbAccount.setAgency(accountToUpdate.getAgency());
        dbAccount.setBalance(accountToUpdate.getBalance());
        dbAccount.setNumber(accountToUpdate.getNumber());

        return this.accountRepository.save(dbAccount);
    }

    @Transactional
    public void delete(Long id) {
        this.validateChangeableId(id, "deleted");
        Account dbAccount = this.findById(id);
        this.accountRepository.delete(dbAccount);
    }

    private void validateChangeableId(Long id, String operation) {
        if (UNCHANGEABLE_ACCOUNT_ID.equals(id)) {
            throw new BusinessException("Account with ID %d can not be %s.".formatted(UNCHANGEABLE_ACCOUNT_ID, operation));
        }
    }
}

