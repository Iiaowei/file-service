package com.lw.storage.support;

import com.lw.storage.ZipCompressDto;
import com.lw.storage.exception.FileDownlaodException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FileCompress {
    @Value("file.upload.path")
    private String uploadPath;

    public Flux<Tuple2<String, String>> buildFlux(ZipCompressDto zipCompressDto) {
        return Flux.zip(
                Mono.just(zipCompressDto.getName()),
                Flux.just(zipCompressDto.getPaths().toArray(new String[0]))
        );
    }

    public Mono<Tuple4<ZipOutputStream, byte[], String, ZipEntry>> readBytes(Tuple2<String, String> tuple2) {
        final String zipName = tuple2.getT1();
        final String path = tuple2.getT2();

        String filepath = uploadPath.endsWith(File.pathSeparator) ? uploadPath : uploadPath + File.pathSeparator;
        ZipOutputStream zipOutputStream;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(filepath + zipName + ".zip"), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            return Mono.error(FileDownlaodException::new);
        }
        final Mono<byte[]> single = DataBufferUtils.read(Paths.get(filepath + path), DefaultDataBufferFactory.sharedInstance, 1024)
                .flatMap((a) -> Mono.just(a.asByteBuffer().array()))
                .single();

        return Mono.zip(Mono.just(zipOutputStream), single, Mono.just(zipName), Mono.just(new ZipEntry(path)));
    }

    public Mono<String> transferTo(Tuple4<ZipOutputStream, byte[], String, ZipEntry> tuple4) {
        final ZipOutputStream zipOutputStream = tuple4.getT1();
        final byte[] bytes = tuple4.getT2();
        final String zipName = tuple4.getT3();
        final ZipEntry zipEntry = tuple4.getT4();

        return Mono.fromSupplier(() -> {
            try {
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return zipName;
        });
    }
}
