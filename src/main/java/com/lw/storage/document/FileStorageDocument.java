package com.lw.storage.document;

import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(value = "file-storage")
public class FileStorageDocument {
    @Id
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
