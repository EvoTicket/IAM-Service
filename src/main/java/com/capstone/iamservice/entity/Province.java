package com.capstone.iamservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "provinces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province {

    @Id
    private Integer code;

    private String name;

    @JsonProperty("division_type")
    private String divisionType;

    private String codename;

    @JsonProperty("phone_code")
    private Integer phoneCode;
}
