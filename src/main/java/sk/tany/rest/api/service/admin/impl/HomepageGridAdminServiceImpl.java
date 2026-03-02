package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.homepage.HomepageGridRepository;
import sk.tany.rest.api.dto.admin.homepage.HomepageGridAdminDto;
import sk.tany.rest.api.dto.admin.homepage.patch.HomepageGridPatchRequest;
import sk.tany.rest.api.exception.HomepageGridException;
import sk.tany.rest.api.mapper.HomepageGridMapper;
import sk.tany.rest.api.service.admin.HomepageGridAdminService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HomepageGridAdminServiceImpl implements HomepageGridAdminService {

    private final HomepageGridRepository homepageGridRepository;
    private final HomepageGridMapper homepageGridMapper;

    @Override
    public Page<HomepageGridAdminDto> findAll(Pageable pageable) {
        return homepageGridRepository.findAll(pageable).map(homepageGridMapper::toAdminDto);
    }

    @Override
    public Optional<HomepageGridAdminDto> findById(String id) {
        return homepageGridRepository.findById(id).map(homepageGridMapper::toAdminDto);
    }

    @Override
    public HomepageGridAdminDto save(HomepageGridAdminDto dto) {
        var entity = homepageGridMapper.toEntity(dto);
        var saved = homepageGridRepository.save(entity);
        return homepageGridMapper.toAdminDto(saved);
    }

    @Override
    public HomepageGridAdminDto update(String id, HomepageGridAdminDto dto) {
        var existing = homepageGridRepository.findById(id).orElseThrow(() -> new HomepageGridException.NotFound("HomepageGrid not found"));
        dto.setId(id);
        homepageGridMapper.updateEntityFromDto(dto, existing);
        var saved = homepageGridRepository.save(existing);
        return homepageGridMapper.toAdminDto(saved);
    }

    @Override
    public HomepageGridAdminDto patch(String id, HomepageGridPatchRequest dto) {
        var existing = homepageGridRepository.findById(id).orElseThrow(() -> new HomepageGridException.NotFound("HomepageGrid not found"));
        homepageGridMapper.updateEntityFromPatch(dto, existing);
        var saved = homepageGridRepository.save(existing);
        return homepageGridMapper.toAdminDto(saved);
    }

    @Override
    public void deleteById(String id) {
        homepageGridRepository.deleteById(id);
    }
}
