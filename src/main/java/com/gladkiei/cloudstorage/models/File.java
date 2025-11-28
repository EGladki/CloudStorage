package com.gladkiei.cloudstorage.models;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class File {

    private String path;

    private String name;

    private byte size;
}
