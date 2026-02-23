package sk.tany.rest.api.dto;

import lombok.Data;
import org.springframework.data.domain.Pageable;

@Data
public class PageableDto {
    private SortDto sort;
    private int pageNumber;
    private int pageSize;
    private long offset;
    private boolean paged;
    private boolean unpaged;

    public static PageableDto from(Pageable pageable) {
        PageableDto dto = new PageableDto();
        if (pageable.isPaged()) {
            dto.setPageNumber(pageable.getPageNumber());
            dto.setPageSize(pageable.getPageSize());
            dto.setOffset(pageable.getOffset());
        }
        dto.setSort(SortDto.from(pageable.getSort()));
        dto.setPaged(pageable.isPaged());
        dto.setUnpaged(pageable.isUnpaged());
        return dto;
    }
}
