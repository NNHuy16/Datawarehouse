package org.example.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.entity.Process;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Log {

    Integer id;

    @EqualsAndHashCode.Exclude
    Process process;  // Giữ lại trường process nếu cần lưu đối tượng

    String message;

    Timestamp insertDate;

    String level;

    // Thay đổi kiểu processId thành Integer để phù hợp với kiểu id của Process
    Integer processId;

    // Setter và getter cho processId
    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public Integer getProcessId() {
        return this.processId;
    }
}
