package com.openstep.balllinkbe.features.auth.dto.request;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String name;
}
