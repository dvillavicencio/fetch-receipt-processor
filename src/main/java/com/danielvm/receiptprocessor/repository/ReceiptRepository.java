package com.danielvm.receiptprocessor.repository;

import com.danielvm.receiptprocessor.entity.ReceiptEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends CrudRepository<ReceiptEntity, Long> {

}
