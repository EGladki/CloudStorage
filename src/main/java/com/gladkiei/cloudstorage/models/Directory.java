package com.gladkiei.cloudstorage.models;

import com.gladkiei.cloudstorage.enums.Type;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@ToString
public class Directory extends Resource {
    public Directory(String path, String name, Type type) {
        super(path, name, type);
    }
}
