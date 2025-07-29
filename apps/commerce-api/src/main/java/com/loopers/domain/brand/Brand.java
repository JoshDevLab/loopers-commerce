package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "brands")
@Entity
public class Brand extends BaseEntity {
    private String name;
    private String description;
    private String imageUrl;

    private Brand(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public static Brand create(String name, String description, String imageUrl) {
        return new Brand(name, description, imageUrl);
    }
}
