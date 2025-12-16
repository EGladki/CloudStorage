package com.gladkiei.cloudstorage.models;

import com.gladkiei.cloudstorage.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Resource {
    private String path;
    private String name;
    private Type type;
}
