package com.lw.storage.support;

import cn.hutool.core.util.IdUtil;
import com.lw.storage.document.FileStorageDocument;
import com.lw.storage.repository.FileStorageReactiveRepository;
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

    @Value("${file.upload.path}")
    private String uploadPath;
    private FileStorageReactiveRepository fileStorageReactiveRepository;

    @Autowired
    public void setFileStorageRepository(FileStorageReactiveRepository fileStorageReactiveRepository) {
        this.fileStorageReactiveRepository = fileStorageReactiveRepository;
    }

    public Mono<FileStorageDocument> storageFile(FilePart fp) {
        File tmpFolder = Paths.get(uploadPath + dateTimeFormatter.format(LocalDate.now())).toFile();

        if (!tmpFolder.exists() && tmpFolder.mkdirs()) {
            System.out.println("文件夹创建成功!");
        }

        FileStorage fileStorage = new FileStorage(IdUtil.simpleUUID(),
                uploadPath,
                fp.filename(), fileStorageReactiveRepository);

        return fp.transferTo(new File(fileStorage.getFilepath()))
                .then(fileStorage.storage());
    }

    public Mono<BizResponse<UploadResponse>> createResponse(FileStorageDocument document) {
        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.setFileId(document.getId());
        return Mono.just(BizResponse.ok(uploadResponse));
    }

    static class FileStorage {
        private final String id;
        private final String uploadPath;
        private final String filepath;
        private final String filename;
        private final FileStorageReactiveRepository fileStorageReactiveRepository;

        public FileStorage(String id, String filepath, String filename, FileStorageReactiveRepository fileStorageReactiveRepository) {
            this.id = id;
            this.uploadPath = filepath.endsWith(File.separator) ? filepath : filepath + File.separator;
            this.filename = filename;
            this.filepath = this.uploadPath + dateTimeFormatter.format(LocalDate.now()) + File.separator + this.id;
            this.fileStorageReactiveRepository = fileStorageReactiveRepository;
        }

        public String getFilepath() {
            return filepath;
        }

        public Mono<FileStorageDocument> storage() {
            FileStorageDocument storageDocument = new FileStorageDocument();
            storageDocument.setId(id);
            storageDocument.setName(pattern.matcher(filename).matches() ? filename : id);
            storageDocument.setPath(uploadPath);
            return fileStorageReactiveRepository.save(storageDocument);
        }
    }
}
