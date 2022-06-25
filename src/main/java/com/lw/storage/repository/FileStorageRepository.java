package com.lw.storage.repository;

import com.lw.storage.document.FileStorageDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileStorageRepository extends ReactiveMongoRepository<FileStorageDocument, String> {
}
