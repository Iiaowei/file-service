package com.lw.storage.controller;

import com.lw.storage.ZipCompressDto;
import com.lw.storage.service.FileStorageService;
import com.lw.storage.support.BizResponse;
import com.lw.storage.support.UploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author liaowei
 * @version V1.0
 * @date 2022/6/24 10:26:55
 */
@RestController
@RequestMapping("/file")
public class FileStorageController {

    private FileStorageService fileStorageService;

    @Autowired
    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public Mono<BizResponse<UploadResponse>> upload(@RequestPart("file") Mono<FilePart> mono) {
        return fileStorageService.upload(mono);
    }


    @GetMapping("/download/{url}")
    public Mono<ResponseEntity<byte[]>> download(@PathVariable("url") String url) {
        return fileStorageService.download(Mono.just(url));
    }

    @PostMapping("/zip")
    public Mono<ResponseEntity<byte[]>> zipDownload(@RequestBody ZipCompressDto zipCompressDto) {
        return fileStorageService.batchDownload(zipCompressDto);
    }
}
