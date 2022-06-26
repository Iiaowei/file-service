package com.lw.storage.service;

import cn.hutool.core.util.IdUtil;
import com.lw.storage.ZipCompressDto;
import com.lw.storage.document.FileStorageDocument;
import com.lw.storage.repository.FileStorageRepository;
import com.lw.storage.support.BizResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    public void setFileStorageRepository(FileStorageRepository fileStorageRepository) {
        this.fileStorageRepository = fileStorageRepository;
    }

    public Mono<BizResponse<Map<String, Object>>> upload(Flux<FilePart> fileParts) {
        return fileParts.flatMap(this::storageFile)
                .collectList()
                .flatMap(urls -> Mono.just(BizResponse.ok(Map.of("urls", urls))));
    }

    public Mono<Void> download(Mono<String> mono) {
        return mono.flatMap((url) -> {
            String tmpPath = getTempPath();
            final Mono<byte[]> single = DataBufferUtils.read(Paths.get(tmpPath + url), DefaultDataBufferFactory.sharedInstance, 1024)
                    .flatMap((a) -> Mono.just(a.asByteBuffer().array()))
                    .single();
            return Mono.zip(single, Mono.just(url));
        }).map((tuple2 -> {
            final byte[] bytes = tuple2.getT1();
            final String url = tuple2.getT2();
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers((httpHeaders -> {
                        ContentDisposition contentDisposition = ContentDisposition.attachment()
                                .filename(url)
                                .build();
                        httpHeaders.setContentDisposition(contentDisposition);
                    }))
                    .body(BodyInserters.fromValue(bytes));
        })).then();
    }

    public Mono<Void> batchDownload(ZipCompressDto zipCompressDto) {
        return Flux.zip(
                Mono.just(zipCompressDto.getName()),
                Flux.just(zipCompressDto.getPaths().toArray(new String[0]))
        ).flatMap(tuple2 -> {
            final String zipName = tuple2.getT1();
            final String path = tuple2.getT2();

            ZipOutputStream zipOutputStream = null;
            try {
                zipOutputStream = new ZipOutputStream(new FileOutputStream(getTempPath() + zipName + ".zip"), StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            final Mono<byte[]> single = DataBufferUtils.read(Paths.get(getTempPath() + path), DefaultDataBufferFactory.sharedInstance, 1024)
                    .flatMap((a) -> Mono.just(a.asByteBuffer().array()))
                    .single();

            assert zipOutputStream != null;
            return Mono.zip(Mono.just(zipOutputStream), single, Mono.just(zipName), Mono.just(new ZipEntry(path)));
        }).flatMap(tuple2 -> {
            final ZipOutputStream zipOutputStream = tuple2.getT1();
            final byte[] bytes = tuple2.getT2();
            final String zipName = tuple2.getT3();
            final ZipEntry zipEntry = tuple2.getT4();

            return Mono.fromSupplier(() -> {
                try {
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return zipName;
            });
        }).last().publish(this::download).then();
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
                    FileStorageDocument storageDocument = new FileStorageDocument();
                    storageDocument.setId(simpleUUID);
                    storageDocument.setName(pattern.matcher(filename).matches() ? filename : simpleUUID);
                    storageDocument.setPath(tmpPath);
                    fileStorageRepository.save(storageDocument);
                    return simpleUUID;
                }));
    }

    public String getTempPath() {
        return tempPath + dateTimeFormatter.format(LocalDate.now()) + "\\";
    }
}
