package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.domain.supplier.Supplier;
import sk.tany.rest.api.domain.supplier.SupplierRepository;
import sk.tany.rest.api.dto.SupplierDto;
import sk.tany.rest.api.mapper.ISkladMapper;
import sk.tany.rest.api.mapper.SupplierMapper;
import sk.tany.rest.api.service.isklad.ISkladService;

import static org.mockito.Mockito.*;

class SupplierAdminServiceISkladTest {

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private SupplierMapper supplierMapper;
    @Mock
    private ISkladService iskladService;
    @Mock
    private ISkladProperties iskladProperties;
    @Mock
    private ISkladMapper iskladMapper;

    @InjectMocks
    private SupplierAdminServiceImpl supplierAdminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_shouldCallISkladService_whenEnabled_and_IsNew() {
        SupplierDto dto = new SupplierDto();
        dto.setId(null); // Explicitly null for new
        Supplier entity = new Supplier();
        Supplier savedEntity = new Supplier();

        when(supplierMapper.toEntity(dto)).thenReturn(entity);
        when(supplierRepository.save(entity)).thenReturn(savedEntity);
        when(supplierMapper.toDto(savedEntity)).thenReturn(dto);
        when(iskladProperties.isEnabled()).thenReturn(true);
        when(iskladMapper.toCreateSupplierRequest(dto)).thenReturn(sk.tany.rest.api.dto.isklad.CreateSupplierRequest.builder().build());

        supplierAdminService.save(dto);

        verify(iskladService, times(1)).createSupplier(any());
    }

    @Test
    void save_shouldNotCallISkladService_whenDisabled() {
        SupplierDto dto = new SupplierDto();
        dto.setId(null);
        Supplier entity = new Supplier();
        Supplier savedEntity = new Supplier();

        when(supplierMapper.toEntity(dto)).thenReturn(entity);
        when(supplierRepository.save(entity)).thenReturn(savedEntity);
        when(supplierMapper.toDto(savedEntity)).thenReturn(dto);
        when(iskladProperties.isEnabled()).thenReturn(false);

        supplierAdminService.save(dto);

        verify(iskladService, never()).createSupplier(any());
    }

    @Test
    void save_shouldNotCallISkladService_whenUpdate() {
        SupplierDto dto = new SupplierDto();
        dto.setId("existing-id"); // Existing ID
        Supplier entity = new Supplier();
        Supplier savedEntity = new Supplier();

        when(supplierMapper.toEntity(dto)).thenReturn(entity);
        when(supplierRepository.save(entity)).thenReturn(savedEntity);
        when(supplierMapper.toDto(savedEntity)).thenReturn(dto);
        when(iskladProperties.isEnabled()).thenReturn(true); // Enabled but is update

        supplierAdminService.save(dto);

        verify(iskladService, never()).createSupplier(any());
    }
}
