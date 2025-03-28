package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.ChangePasswordServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangePasswordRequest {

    private String oldPassword;
    private String newPassword;
    private String newPasswordCheck;

    @Builder
    public ChangePasswordRequest(String oldPassword, String newPassword, String newPasswordCheck) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.newPasswordCheck = newPasswordCheck;
    }

    public ChangePasswordServiceRequest toServiceRequest() {
        return ChangePasswordServiceRequest.builder()
                .oldPassword(oldPassword)
                .newPassword(newPassword)
                .newPasswordCheck(newPasswordCheck)
                .build();
    }
}
