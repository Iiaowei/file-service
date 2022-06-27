package com.lw.storage.service;

import com.lw.storage.ZipCompressDto;
import com.lw.storage.repository.FileStorageReactiveRepository;
import com.lw.storage.support.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FileStorageService {

    private FileDownload fileDownload;
    private FileUpload fileUpload;
    @Value("${file.upload.path}")
    private String uploadPath;
    private FileStorageReactiveRepository fileStorageReactiveRepository;

    @Autowired
    public void setFileStorageReactiveRepository(FileStorageReactiveRepository fileStorageReactiveRepository) {
        this.fileStorageReactiveRepository = fileStorageReactiveRepository;
    }

    @Autowired
    public void setFileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    @Autowired
    public void setFileDownload(FileDownload fileDownload) {
        this.fileDownload = fileDownload;
    }

    public Mono<BizResponse<UploadResponse>> upload(Mono<FilePart> mono) {
        return mono.flatMap(fileUpload::storageFile)
                .flatMap(fileUpload::createResponse);
    }

    public Mono<ResponseEntity<byte[]>> download(Mono<String> mono) {
        return mono.flatMap(fileDownload::readBytes)
                .flatMap(fileDownload::createResponse);
    }

    public Mono<ResponseEntity<byte[]>> batchDownload(ZipCompressDto zipCompressDto) {

        try (FileCompress fileCompress = new FileCompress(zipCompressDto, uploadPath, fileStorageReactiveRepository)) {
            fileCompress.write();
            return this.download(Mono.just(fileCompress.getName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
