package com.lw.storage.service;

import com.lw.storage.ZipCompressDto;
import com.lw.storage.support.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FileStorageService {

    private FileDownload fileDownload;
    private FileUpload fileUpload;
    private FileCompress fileCompress;

    @Autowired
    public void setFileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    @Autowired
    public void setFileDownload(FileDownload fileDownload) {
        this.fileDownload = fileDownload;
    }

    @Autowired
    public void setFileCompress(FileCompress fileCompress) {
        this.fileCompress = fileCompress;
    }

    public Mono<BizResponse<UploadResponse>> upload(Mono<FilePart> mono) {
        return mono.flatMap(fileUpload::storageFile)
                .flatMap(fileUpload::createResponse);
    }

    public Mono<Void> download(Mono<String> mono) {
        return mono.flatMap(fileDownload::readBytes)
                .map(fileDownload::createResponse)
                .then();
    }

    public Mono<Void> batchDownload(ZipCompressDto zipCompressDto) {
        return fileCompress.buildFlux(zipCompressDto)
                .flatMap(fileCompress::readBytes)
                .flatMap(fileCompress::transferTo)
                .last().publish(this::download).then();
    }
}
