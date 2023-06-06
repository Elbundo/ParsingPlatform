package org.elbundo.model;

import lombok.Builder;
import lombok.Data;
import org.elbundo.core.parsers.Result;

import java.util.List;

@Data
@Builder
public class Product implements Result {
    private String id;
    private String title;
    private String desc;
    private String img;
    private String brand;
    private long price;
    private long calories;
    private long fats;
    private long proteins;
    private long carbohydrates;
    private List<Store> stores;
}
