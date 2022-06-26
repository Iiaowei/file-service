package com.lw.storage.support;

import com.lw.storage.document.FileStorageDocument;
import com.lw.storage.repository.FileStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.nio.file.Paths;

@Component
public class FileDownload {

    private FileStorageRepository fileStorageRepository;

    @Autowired
    public void setFileStorageRepository(FileStorageRepository fileStorageRepository) {
        this.fileStorageRepository = fileStorageRepository;
    }

    public Mono<Tuple2<byte[], FileStorageDocument>> readBytes(String path) {
        final Mono<byte[]> single = DataBufferUtils.read(Paths.get(path), DefaultDataBufferFactory.sharedInstance, 1024)
                .flatMap((a) -> Mono.just(a.asByteBuffer().array()))
                .single();

        return Mono.zip(single, fileStorageRepository.findById(path));
    }

    public Mono<ServerResponse> createResponse(Tuple2<byte[], FileStorageDocument> tuple2) {
        final byte[] bytes = tuple2.getT1();
        final FileStorageDocument document = tuple2.getT2();

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(Filename.create(document.getName())::setHeaders)
                .body(BodyInserters.fromValue(bytes));
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
