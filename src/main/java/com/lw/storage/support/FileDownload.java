package com.lw.storage.support;

import com.lw.storage.document.FileStorageDocument;
import com.lw.storage.repository.FileStorageReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.nio.file.Paths;
import java.time.LocalDate;

@Component
public class FileDownload {

    private FileStorageReactiveRepository fileStorageReactiveRepository;
    @Value("${file.upload.path}")
    private String uploadPath;

    @Autowired
    public void setFileStorageRepository(FileStorageReactiveRepository fileStorageReactiveRepository) {
        this.fileStorageReactiveRepository = fileStorageReactiveRepository;
    }

    public Mono<Tuple2<byte[], FileStorageDocument>> readBytes(String path) {
        final Mono<byte[]> single = DataBufferUtils.read(Paths.get(uploadPath + FileUpload.dateTimeFormatter.format(LocalDate.now()) + "\\" + path), DefaultDataBufferFactory.sharedInstance, 1024)
                .flatMap((a) -> Mono.just(a.asByteBuffer().array()))
                .last();
        return Mono.zip(single, fileStorageReactiveRepository.findById(path));
    }

    public Mono<ResponseEntity<byte[]>> createResponse(Tuple2<byte[], FileStorageDocument> tuple2) {
        final byte[] bytes = tuple2.getT1();
        final FileStorageDocument document = tuple2.getT2();

        return Mono.just(ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(Filename.create(document.getName())::setHeaders)
                .body(bytes));
    }

    record Filename(String filename) {
        private void setHeaders(HttpHeaders httpHeaders) {
            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename(filename)
                    .build();
            httpHeaders.setContentDisposition(contentDisposition);
        }

        public static Filename create(String filename) {
            return new Filename(filename);
        }
    }
}
