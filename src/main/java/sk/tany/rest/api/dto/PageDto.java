package sk.tany.rest.api.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageDto<T> {
    private List<T> content;
    private PageableDto pageable;
    private boolean last;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private SortDto sort;
    private boolean first;
    private int numberOfElements;
    private boolean empty;

    public static <T> PageDto<T> from(Page<T> page) {
        PageDto<T> dto = new PageDto<>();
        dto.setContent(page.getContent());
        dto.setPageable(PageableDto.from(page.getPageable()));
        dto.setLast(page.isLast());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setSize(page.getSize());
        dto.setNumber(page.getNumber());
        dto.setSort(SortDto.from(page.getSort()));
        dto.setFirst(page.isFirst());
        dto.setNumberOfElements(page.getNumberOfElements());
        dto.setEmpty(page.isEmpty());
        return dto;
    }
}
