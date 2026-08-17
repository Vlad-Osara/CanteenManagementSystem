package com.canteenbackend.api.transaction.repository;

import com.canteenbackend.api.transaction.model.Transaction;
import com.canteenbackend.helper.base.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepository extends BaseRepository<Transaction, UUID, TransactionJpaRepository> {
    public TransactionRepository(TransactionJpaRepository repository) {
        super(repository, Transaction.class);
    }

    public Optional<Transaction> findById(UUID id){
        return repository.findById(id);
    }
}
