package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.blog.BlogRepository;
import sk.tany.rest.api.dto.BlogDto;
import sk.tany.rest.api.mapper.BlogMapper;
import sk.tany.rest.api.service.client.BlogClientService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogClientServiceImpl implements BlogClientService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;

    @Override
    public List<BlogDto> getAll() {
        return blogRepository.findAllByVisibleTrue().stream()
                .map(blogMapper::toDto)
                .toList();
    }

    @Override
    public java.util.Optional<BlogDto> getBlog(String id) {
        return blogRepository.findById(id)
                .map(blogMapper::toDto);
    }
}
