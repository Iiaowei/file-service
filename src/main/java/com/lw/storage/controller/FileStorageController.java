package com.lw.storage.controller;

import com.lw.storage.service.FileStorageService;
import com.lw.storage.support.BizResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
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
    public ServerResponse download(@PathVariable("url") String url) {
        return fileStorageService.download(url);
    }

    @PostMapping("/zip")
    public ServerResponse download(@RequestBody List<String> paths) {
        return fileStorageService.batchDownload(paths);
    }
}
