package org.example.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Log {

    Integer id;

    @EqualsAndHashCode.Exclude
    Process process;

    String message;

    Timestamp insertDate;

    String level;
}
