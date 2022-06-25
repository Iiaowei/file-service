package com.lw.storage.service;

import cn.hutool.core.io.resource.BytesResource;
import cn.hutool.core.util.IdUtil;
import com.lw.storage.document.FileStorageDocument;
import com.lw.storage.repository.FileStorageRepository;
import com.lw.storage.support.BizResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileStorageService {

    private FileStorageRepository fileStorageRepository;

    private static final String tempPath = "E:\\temp\\";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern pattern = Pattern.compile("([^[\\/:\\*\\?\"<>\\|]]+)");

//    @Autowired
//    public void setFileStorageRepository(FileStorageRepository fileStorageRepository) {
//        this.fileStorageRepository = fileStorageRepository;
//    }

    public Mono<BizResponse<Map<String, Object>>> upload(Flux<FilePart> fileParts) {
        return fileParts.flatMap(this::storageFile)
                .collectList()
                .flatMap(urls -> Mono.just(BizResponse.ok(Map.of("urls", urls))));
    }

    public ServerResponse download(String url) {
        String tmpPath = getTempPath();

        BytesResource bytesResource;
        try (FileInputStream fileInputStream = new FileInputStream(tmpPath + url)) {
            bytesResource = new BytesResource(fileInputStream.readAllBytes());
        } catch (IOException e) {
            return ServerResponse.notFound()
                    .build()
                    .block();
        }

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers((httpHeaders -> {
                    ContentDisposition contentDisposition = ContentDisposition.attachment()
                            .filename(url)
                            .build();
                    httpHeaders.setContentDisposition(contentDisposition);
                }))
                .body(BodyInserters.fromValue(bytesResource.readBytes()))
                .block();
    }

    public ServerResponse batchDownload(List<String> paths) {
        String simpleUUID = IdUtil.simpleUUID();
        String zipPath = getTempPath() + simpleUUID;

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
            return ServerResponse.notFound()
                    .build()
                    .block();
        }
        return download(simpleUUID);
    }

    private Mono<String> storageFile(FilePart fp) {
        String tmpPath = getTempPath();
        File tmpFolder = Paths.get(tmpPath).toFile();

        if (!tmpFolder.exists() && tmpFolder.mkdirs()) {
            System.out.println("文件夹创建成功!");
        }


        String simpleUUID = IdUtil.simpleUUID();
        String filename = fp.filename();
        String filePath = tmpPath + simpleUUID;

        return fp.transferTo(new File(filePath))
                .then(Mono.fromSupplier(() -> {
//                    FileStorageDocument storageDocument = new FileStorageDocument();
//                    storageDocument.setId(simpleUUID);
//                    storageDocument.setName(pattern.matcher(filename).matches() ? filename : simpleUUID);
//                    storageDocument.setPath(tmpPath);
//                    fileStorageRepository.save(storageDocument);
                    return simpleUUID;
                }));
    }

    public String getTempPath() {
        return tempPath + dateTimeFormatter.format(LocalDate.now()) + "\\";
    }
}
