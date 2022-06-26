package com.lw.storage.support;

import cn.hutool.core.util.IdUtil;
import com.lw.storage.document.FileStorageDocument;
import com.lw.storage.repository.FileStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Component
public class FileUpload {
    public static final Pattern pattern = Pattern.compile("([^[\\/:\\*\\?\"<>\\|]]+)");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("file.upload.path")
    private String uploadPath;
    private FileStorageRepository fileStorageRepository;

    @Autowired
    public void setFileStorageRepository(FileStorageRepository fileStorageRepository) {
        this.fileStorageRepository = fileStorageRepository;
    }

    public Mono<String> storageFile(FilePart fp) {
        File tmpFolder = Paths.get(uploadPath).toFile();

        if (!tmpFolder.exists() && tmpFolder.mkdirs()) {
            System.out.println("文件夹创建成功!");
        }

        FileStorage fileStorage = new FileStorage(IdUtil.simpleUUID(),
                uploadPath + dateTimeFormatter.format(LocalDate.now()),
                fp.filename(), fileStorageRepository);

        return fp.transferTo(new File(fileStorage.getFilepath()))
                .then(Mono.fromSupplier(fileStorage::storage));
    }

    public Mono<BizResponse<UploadResponse>> createResponse(String fileId) {
        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.setFileId(fileId);
        return Mono.just(BizResponse.ok(uploadResponse));
    }

    static class FileStorage {
        private final String id;
        private final String uploadPath;
        private final String filepath;
        private final String filename;
        private final FileStorageRepository fileStorageRepository;

        public FileStorage(String id, String filepath, String filename, FileStorageRepository fileStorageRepository) {
            this.id = id;
            this.uploadPath = filepath.endsWith(File.pathSeparator) ? filepath : filepath + File.pathSeparator;
            this.filename = filename;
            this.filepath = this.uploadPath + this.filename;
            this.fileStorageRepository = fileStorageRepository;
        }

        public String getFilepath() {
            return filepath;
        }

        public String storage() {
            FileStorageDocument storageDocument = new FileStorageDocument();
            storageDocument.setId(id);
            storageDocument.setName(pattern.matcher(filename).matches() ? filename : id);
            storageDocument.setPath(uploadPath);
            fileStorageRepository.save(storageDocument);
            return id;
        }
    }
}
