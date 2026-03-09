package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.supplier.SupplierInvoice;
import sk.tany.rest.api.domain.supplier.SupplierInvoiceRepository;
import sk.tany.rest.api.dto.SupplierInvoiceAdminDto;
import sk.tany.rest.api.mapper.SupplierInvoiceAdminMapper;
import sk.tany.rest.api.service.admin.SupplierInvoiceAdminService;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierInvoiceAdminServiceImpl implements SupplierInvoiceAdminService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final SupplierInvoiceRepository repository;
    private final SupplierInvoiceAdminMapper mapper;

    @Override
    public Page<SupplierInvoiceAdminDto> list(Pageable pageable, String query, LocalDate createDateFrom, LocalDate createDateTo) {
        Page<SupplierInvoice> page;

        if (query != null && !query.trim().isEmpty()) {
            page = repository.findBySupplierNameContainingIgnoreCaseOrInvoiceNumberContainingIgnoreCaseOrSupplierVatIdentifierContainingIgnoreCase(
                query, query, query, pageable
            );
        } else if (createDateFrom != null && createDateTo != null) {
            Instant fromInstant = createDateFrom.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant toInstant = createDateTo.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            page = repository.findByCreateDateBetween(fromInstant, toInstant, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        List<SupplierInvoiceAdminDto> dtoList = page.getContent().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    public SupplierInvoiceAdminDto get(String id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("SupplierInvoice not found with id: " + id));
    }

    @Override
    public SupplierInvoiceAdminDto create(SupplierInvoiceAdminDto dto) {
        SupplierInvoice entity = mapper.toEntity(dto);
        SupplierInvoice saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public SupplierInvoiceAdminDto update(String id, SupplierInvoiceAdminDto dto) {
        SupplierInvoice entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SupplierInvoice not found with id: " + id));
        mapper.updateEntity(dto, entity);
        SupplierInvoice saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public byte[] exportCsv(LocalDate createDateFrom, LocalDate createDateTo) {
        List<SupplierInvoice> invoices;
        if (createDateFrom != null && createDateTo != null) {
            invoices = repository.findByCreateDateBetween(createDateFrom, createDateTo);
        } else {
            invoices = repository.findAll();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter pw = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // UTF-8 BOM
            baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // Header
            pw.println("Partner;Cislo faktury;Variabilny symbol;Cena bez DPH;DPH suma;Cena s DPH;Datum vystavenia;Datum zdanitelneho plnenia;IC DPH");

            // Data
            for (SupplierInvoice inv : invoices) {
                pw.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s%n",
                        escapeCsv(inv.getSupplierName()),
                        escapeCsv(inv.getInvoiceNumber()),
                        escapeCsv(inv.getPaymentReference()),
                        inv.getPriceWithoutVat() != null ? inv.getPriceWithoutVat().toString().replace(".", ",") : "",
                        inv.getVatValue() != null ? inv.getVatValue().toString().replace(".", ",") : "",
                        inv.getPriceWithVat() != null ? inv.getPriceWithVat().toString().replace(".", ",") : "",
                        inv.getDateCreated() != null ? inv.getDateCreated().format(FORMATTER) : "",
                        inv.getTaxDate() != null ? inv.getTaxDate().format(FORMATTER) : "",
                        escapeCsv(inv.getSupplierVatIdentifier())
                );
            }
            pw.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate CSV export for supplier invoices", e);
            throw new RuntimeException("Failed to generate CSV export", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
