package com.lw.storage.controller;

import cn.hutool.core.io.resource.BytesResource;
import cn.hutool.core.util.IdUtil;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author liaowei
 * @version V1.0
 * @date 2022/6/24 10:26:55
 */
@RestController
@RequestMapping("/file")
public class FileStorageController {
    private static final String tempPath = "E:\\temp\\";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Pattern pattern = Pattern.compile("([^[\\/:\\*\\?\"<>\\|]]+)");

    @PostMapping("/upload")
    public Mono<Map<String, Object>> upload(@RequestPart("files") Flux<FilePart> fileParts) {
        return fileParts.flatMap(fp -> {

            String tmpPath = getTempPath();

            File tmpFolder = Paths.get(tmpPath).toFile();

            if (!tmpFolder.exists() && tmpFolder.mkdirs()) {
                System.out.println("文件夹创建成功!");
            }

            String simpleUUID = IdUtil.simpleUUID();

            if (!pattern.matcher(fp.filename()).matches()) {
                return Flux.error(IllegalArgumentException::new);
            }
            return fp.transferTo(new File(tmpPath + simpleUUID)).then(Mono.just(simpleUUID));
        }).collectList().flatMap(urls -> {
            Map<String, Object> result = new HashMap<>();
            result.put("urls", urls);
            return Mono.just(result);
        });
    }


    @GetMapping("/download/{url}")
    public ServerResponse download(@PathVariable("url") String url) {
        String tmpPath = getTempPath();

        BytesResource bytesResource;
        try (FileInputStream fileInputStream = new FileInputStream(tmpPath + url)) {
            bytesResource = new BytesResource(fileInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ServerResponse.ok()
                .headers((httpHeaders -> {
                    ContentDisposition contentDisposition = ContentDisposition.attachment()
                            .filename(url)
                            .build();
                    httpHeaders.setContentDisposition(contentDisposition);
                    httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                }))
                .body(BodyInserters.fromValue(bytesResource.readBytes()))
                .block();
    }

    @PostMapping("/zip/{name}")
    public ServerResponse download(@RequestBody List<String> paths, @PathVariable("name") String name) {
        String zipPath = getTempPath() + IdUtil.simpleUUID();

        try (FileOutputStream fileOutputStream = new FileOutputStream(zipPath);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream, StandardCharsets.UTF_8)) {

            for (String path : paths) {
                File file = new File(getTempPath() + path);
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    zipOutputStream.write(fileInputStream.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BytesResource bytesResource;
        try (FileInputStream fileInputStream = new FileInputStream(zipPath)) {
            bytesResource = new BytesResource(fileInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ServerResponse.ok()
                .headers((httpHeaders -> {
                    ContentDisposition contentDisposition = ContentDisposition.attachment()
                            .filename(name)
                            .build();
                    httpHeaders.setContentDisposition(contentDisposition);
                    httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                }))
                .body(BodyInserters.fromValue(bytesResource.readBytes()))
                .block();
    }

    public String getTempPath() {
        return tempPath + dateTimeFormatter.format(LocalDate.now()) + "\\";
    }
}
