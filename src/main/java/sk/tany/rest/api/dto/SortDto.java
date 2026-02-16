package sk.tany.rest.api.dto;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class SortDto {
    private boolean sorted;
    private boolean unsorted;
    private boolean empty;

    public static SortDto from(Sort sort) {
        SortDto dto = new SortDto();
        dto.setSorted(sort.isSorted());
        dto.setUnsorted(sort.isUnsorted());
        dto.setEmpty(sort.isEmpty());
        return dto;
    }
}
