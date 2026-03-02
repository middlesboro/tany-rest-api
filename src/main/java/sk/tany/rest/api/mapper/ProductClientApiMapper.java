package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.dto.ProductLabelDto;
import sk.tany.rest.api.dto.client.product.get.ProductClientGetResponse;
import sk.tany.rest.api.dto.client.product.label.ProductLabelClientDto;
import sk.tany.rest.api.dto.client.product.list.ProductClientListResponse;

@Mapper(componentModel = "spring")
public interface ProductClientApiMapper {
    ProductClientGetResponse toGetResponse(ProductClientDto dto);
    ProductClientListResponse toListResponse(ProductClientDto dto);
    ProductLabelClientDto toLabelClientDto(ProductLabelDto dto);

    default sk.tany.rest.api.dto.client.product.ProductDto toProductDto(ProductClientDto dto) {
        if (dto == null) return null;
        sk.tany.rest.api.dto.client.product.ProductDto productDto = new sk.tany.rest.api.dto.client.product.ProductDto();
        productDto.setId(dto.getId());
        productDto.setTitle(dto.getTitle());
        productDto.setPrice(dto.getPrice());
        productDto.setDiscountPrice(dto.getDiscountPrice());
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            productDto.setImage(dto.getImages().getFirst());
        }
        return productDto;
    }

    default sk.tany.rest.api.dto.client.product.ProductDto toProductDto(sk.tany.rest.api.domain.product.Product product) {
        if (product == null) return null;
        sk.tany.rest.api.dto.client.product.ProductDto productDto = new sk.tany.rest.api.dto.client.product.ProductDto();
        productDto.setId(product.getId());
        productDto.setTitle(product.getTitle());
        productDto.setPrice(product.getPrice());
        productDto.setDiscountPrice(product.getDiscountPrice());
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            productDto.setImage(product.getImages().getFirst());
        }
        return productDto;
    }
}
