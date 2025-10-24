package io.github.asyncbtd.sap.core.service;

import io.github.asyncbtd.sap.web.dto.RegisterRequest;

public interface UserService {
    void registerUser(RegisterRequest registerReq);
}
