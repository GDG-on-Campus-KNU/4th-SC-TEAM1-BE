package com.gdg.Todak.member.controller.request;

import org.springframework.web.multipart.MultipartFile;

public record ProfileRequest(
        MultipartFile file
) {
}
