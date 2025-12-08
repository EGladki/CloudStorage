package com.gladkiei.cloudstorage.models;

import com.gladkiei.cloudstorage.enums.Type;
import lombok.*;
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
