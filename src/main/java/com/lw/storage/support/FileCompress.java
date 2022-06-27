package com.lw.storage.support;

import com.lw.storage.ZipCompressDto;
import com.lw.storage.exception.FileDownlaodException;
import com.lw.storage.repository.FileStorageReactiveRepository;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class FileCompress implements AutoCloseable {
    private String filepath;
    private String uploadPath;
    private String name;
    private List<String> paths;
    private FileStorageReactiveRepository fileStorageReactiveRepository;
    private ZipOutputStream zipOutputStream;
    public FileCompress(ZipCompressDto zipCompressDto, String uploadPath, FileStorageReactiveRepository fileStorageReactiveRepository) {
        String name = zipCompressDto.getName();
        name = name.endsWith(".zip") ? name : name + ".zip";
        this.name = name.endsWith(".zip") ? name : name + ".zip";
        this.paths = zipCompressDto.getPaths();
        this.uploadPath = uploadPath;
        this.filepath = uploadPath + FileUpload.dateTimeFormatter.format(LocalDate.now()) + File.separator + this.name;
        try {
            this.zipOutputStream = new ZipOutputStream(new FileOutputStream(this.filepath), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.fileStorageReactiveRepository = fileStorageReactiveRepository;
    }


    public void write() {
//        fileStorageReactiveRepository.findAllById(paths).flatMap(document -> {
//            String path = document.getPath();
//            String name = document.getName();
//            try {
//                InputStream inputStream = Files.newInputStream(Paths.get(path + name));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        for (String path : paths) {
//            CompletableFuture<FileStorageDocument> completableFuture = fileStorageReactiveRepository.findById(path).toFuture();
//            completableFuture.thenAcceptAsync((document) -> {
//                try (InputStream inputStream = Files.newInputStream(Paths.get(uploadPath + FileUpload.dateTimeFormatter.format(LocalDate.now()) + File.separator + path))) {
//                    zipOutputStream.putNextEntry(new ZipEntry(document.getName()));
//                    zipOutputStream.write(inputStream.readAllBytes());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }


    }

    public String getName() {
        return name;
    }

    @Override
    public void close() throws Exception {
        zipOutputStream.close();
    }
}
