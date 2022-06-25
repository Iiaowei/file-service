package com.lw.storage.document;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
public class FileStorageDocument {
    private String id;
    private String name;
    private String path;
    @CreatedDate
    private LocalDateTime createTime;
    @CreatedBy
    private String createUser;
    @LastModifiedDate
    private LocalDateTime modifiedTime;
    @LastModifiedBy
    private String modifiedUser;
}
