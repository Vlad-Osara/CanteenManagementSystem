package com.canteenbackend.api.transaction.service;

import com.canteenbackend.api.transaction.dto.TransactionDTO;
import com.canteenbackend.api.transaction.mapper.TransactionMapper;
import com.canteenbackend.api.transaction.model.Transaction;
import com.canteenbackend.api.transaction.repository.TransactionJpaRepository;
import com.canteenbackend.api.transaction.request.TransactionGetRequest;
import com.canteenbackend.api.transaction.request.TransactionStoreRequest;
import com.canteenbackend.api.transaction.request.TransactionUpdateRequest;
import com.canteenbackend.helper.base.construct.RestfullService;
import com.canteenbackend.helper.base.repository.BaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService extends RestfullService<TransactionDTO, TransactionGetRequest, TransactionStoreRequest, TransactionUpdateRequest> {
    private final BaseRepository<Transaction, UUID, TransactionJpaRepository> baseRepository;
    private final TransactionMapper transactionMapper;


    @Override
    public Page<TransactionDTO> getAll(TransactionGetRequest transactionGetRequest) {
        Pageable pageable = transactionGetRequest.toPageable();
        Page<Transaction> transactions = baseRepository.getAll(pageable);
        return transactions.map(transactionMapper::toTransactionDTO);
    }

    @Override
    public TransactionDTO get(UUID id) {
        return transactionMapper.toTransactionDTO(baseRepository.get(id));
    }

    @Override
    public TransactionDTO store(TransactionStoreRequest transactionStoreRequest) {
        return null;
    }

    @Override
    public TransactionDTO update(UUID id, TransactionUpdateRequest transactionUpdateRequest) {
        return null;
    }

    @Override
    public TransactionDTO destroy(UUID id) {
        return null;
    }
}
