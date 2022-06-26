package com.lw.storage;

import lombok.Data;

import java.util.List;

@Data
public class ZipCompressDto {
    private String name;
    private List<String> paths;
}
