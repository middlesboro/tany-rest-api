package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.domain.wishlist.Wishlist;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminDto;

@Mapper(componentModel = "spring")
public interface WishlistMapper {
    WishlistAdminDto toAdminDto(Wishlist wishlist);
}
