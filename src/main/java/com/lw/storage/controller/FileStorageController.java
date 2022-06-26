package com.lw.storage.controller;

import com.lw.storage.ZipCompressDto;
import com.lw.storage.service.FileStorageService;
import com.lw.storage.support.BizResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

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
    public Mono<BizResponse<Map<String, Object>>> upload(@RequestPart("files") Flux<FilePart> fileParts) {
        return fileStorageService.upload(fileParts);
    }


    @GetMapping("/download/{url}")
    public Mono<Void> download(@PathVariable("url") Mono<String> mono) {
        return fileStorageService.download(mono);
    }

    @PostMapping("/zip")
    public Mono<Void> zipDownload(@RequestBody ZipCompressDto zipCompressDto) {
        return fileStorageService.batchDownload(zipCompressDto);
    }
}
