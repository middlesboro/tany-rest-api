package sk.tany.rest.api.service.admin.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.domain.filter.FilterParameterRepository;
import sk.tany.rest.api.domain.filter.FilterParameterValueRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FilterParameterValueAdminServiceImplTest {

    @Mock
    private FilterParameterValueRepository repository;

    @Mock
    private FilterParameterRepository filterParameterRepository;

    @InjectMocks
    private FilterParameterValueAdminServiceImpl service;

    @Test
    void deleteById_shouldRemoveValueIdFromParameters() {
        String valueId = "value123";
        FilterParameter param1 = new FilterParameter();
        param1.setId("param1");
        param1.setFilterParameterValueIds(new ArrayList<>(Collections.singletonList(valueId)));

        FilterParameter param2 = new FilterParameter();
        param2.setId("param2");
        param2.setFilterParameterValueIds(new ArrayList<>(List.of("otherValue", valueId)));

        FilterParameter param3 = new FilterParameter();
        param3.setId("param3");
        param3.setFilterParameterValueIds(new ArrayList<>(Collections.singletonList("otherValue")));

        when(filterParameterRepository.findAllByFilterParameterValueIdsContaining(valueId)).thenReturn(List.of(param1, param2));

        service.deleteById(valueId);

        // Verify that save was called for param1 with empty list or list without valueId
        verify(filterParameterRepository).save(argThat(argument ->
            argument.getId().equals("param1") && !argument.getFilterParameterValueIds().contains(valueId)
        ));

        // Verify that save was called for param2 with list without valueId
        verify(filterParameterRepository).save(argThat(argument ->
            argument.getId().equals("param2") && !argument.getFilterParameterValueIds().contains(valueId) && argument.getFilterParameterValueIds().contains("otherValue")
        ));

        // Verify that save was NOT called for param3
        verify(filterParameterRepository, never()).save(argThat(argument ->
            argument.getId().equals("param3")
        ));

        // Verify deleteById was called on the value repository
        verify(repository).deleteById(valueId);
    }
}
