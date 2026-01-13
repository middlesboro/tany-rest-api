package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.blog.Blog;
import sk.tany.rest.api.domain.blog.BlogRepository;
import sk.tany.rest.api.dto.BlogDto;
import sk.tany.rest.api.mapper.BlogMapper;
import sk.tany.rest.api.service.admin.BlogAdminService;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlogAdminServiceImpl implements BlogAdminService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final ImageService imageService;

    @Override
    public Page<BlogDto> findAll(Pageable pageable) {
        return blogRepository.findAll(pageable).map(blogMapper::toDto);
    }

    @Override
    public Optional<BlogDto> findById(String id) {
        return blogRepository.findById(id).map(blogMapper::toDto);
    }

    @Override
    public BlogDto save(BlogDto blogDto) {
        Blog blog = blogMapper.toEntity(blogDto);
        Blog savedBlog = blogRepository.save(blog);
        return blogMapper.toDto(savedBlog);
    }

    @Override
    public BlogDto update(String id, BlogDto blogDto) {
        Blog existingBlog = blogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        blogMapper.updateEntityFromDto(blogDto, existingBlog);
        existingBlog.setId(id); // Ensure ID doesn't change

        Blog updatedBlog = blogRepository.save(existingBlog);
        return blogMapper.toDto(updatedBlog);
    }

    @Override
    public void deleteById(String id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        if (blog.getImage() != null) {
            imageService.delete(blog.getImage());
        }

        blogRepository.deleteById(id);
    }

    @Override
    public BlogDto uploadImage(String id, MultipartFile file) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        if (blog.getImage() != null) {
            imageService.delete(blog.getImage());
        }

        String imageUrl = imageService.upload(file, ImageKitType.BLOG);
        blog.setImage(imageUrl);
        Blog updatedBlog = blogRepository.save(blog);
        return blogMapper.toDto(updatedBlog);
    }

    @Override
    public void deleteImage(String id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        if (blog.getImage() != null) {
            imageService.delete(blog.getImage());
            blog.setImage(null);
            blogRepository.save(blog);
        }
    }
}
