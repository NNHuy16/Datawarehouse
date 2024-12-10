package org.example.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Config {

    Integer id;
    String fileName;
    String sourcePath;
    String fileLocation;
    String backupPath;
    String warehouseProcedure;
    String version;
    Boolean isActive;
    Timestamp insertDate;
    Timestamp updateDate;
}
