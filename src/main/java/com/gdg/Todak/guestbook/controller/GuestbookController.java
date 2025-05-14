package com.gdg.Todak.guestbook.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.guestbook.controller.dto.AddGuestbookRequest;
import com.gdg.Todak.guestbook.controller.dto.AddGuestbookResponse;
import com.gdg.Todak.guestbook.controller.dto.DeleteGuestbookRequest;
import com.gdg.Todak.guestbook.controller.dto.GetGuestbookResponse;
import com.gdg.Todak.guestbook.service.GuestbookService;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/guestbook")
@RequiredArgsConstructor
@RestController
public class GuestbookController {

    private final GuestbookService guestbookService;

    @GetMapping
    public ApiResponse<List<GetGuestbookResponse>> getGuestbook(@Login AuthenticateUser user) {
        return ApiResponse.ok(guestbookService.getGuestbook(user));
    }

    @PostMapping
    public ApiResponse<AddGuestbookResponse> addGuestbook(@Login AuthenticateUser user, @RequestBody AddGuestbookRequest request) {
        return ApiResponse.ok(guestbookService.addGuestbook(user, request));
    }

    @DeleteMapping
    public ApiResponse<String> deleteGuestbook(@Login AuthenticateUser user, @RequestBody DeleteGuestbookRequest request) {
        return ApiResponse.ok(guestbookService.deleteGuestbook(user, request));
    }
}
