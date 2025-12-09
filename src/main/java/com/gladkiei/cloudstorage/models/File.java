package com.gladkiei.cloudstorage.models;

import com.gladkiei.cloudstorage.enums.Type;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@ToString
public class File extends Resource {
    private Long size;

    public File(String path, String name, Long size ,Type type) {
        super(path, name, type);
        this.size = size;
    }
}
