package com.example.wefly_app.entity;

import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Table
@Entity
@Where(clause = "deleted_date is null")
public class Airline extends AbstractDate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Column(unique = true)
    private String code;
    @Column(name = "discount_child", precision = 5, scale = 2)
    private BigDecimal discountChild;
    @Column(name = "discount_infant", precision = 5, scale = 2)
    private BigDecimal discountInfant;
    @Column(name = "business_multiplier", precision = 5, scale = 2)
    private BigDecimal businessMultiplier;
}
