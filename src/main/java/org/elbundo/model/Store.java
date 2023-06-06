package org.elbundo.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Store {
    private long id;
    private String title;
    private String inNal;
    private String timeWork;
    private String phone;
}
