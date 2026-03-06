package sk.tany.rest.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    @NotBlank
    private String street;
    @NotBlank
    private String city;
    @NotBlank
    private String zip;
    @NotBlank
    private String country;
}
