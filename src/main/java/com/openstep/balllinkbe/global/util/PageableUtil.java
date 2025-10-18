package com.openstep.balllinkbe.global.util;

import org.springframework.data.domain.*;

public class PageableUtil {
    public static Pageable from(int page, int size, String sort, String defaultField) {
        if (sort == null || sort.isBlank()) return PageRequest.of(page, size);
        String[] sp = sort.split(",");
        String field = sp[0] != null ? sp[0] : defaultField;
        boolean asc = sp.length >= 2 && "asc".equalsIgnoreCase(sp[1]);
        return PageRequest.of(page, size, asc ? Sort.by(field).ascending() : Sort.by(field).descending());
    }
}
