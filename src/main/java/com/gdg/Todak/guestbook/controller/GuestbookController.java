package com.gdg.Todak.guestbook.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.guestbook.controller.dto.AddGuestbookRequest;
import com.gdg.Todak.guestbook.controller.dto.AddGuestbookResponse;
import com.gdg.Todak.guestbook.controller.dto.DeleteGuestbookRequest;
import com.gdg.Todak.guestbook.controller.dto.GetGuestbookResponse;
import com.gdg.Todak.guestbook.service.GuestbookService;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/guestbook")
@RequiredArgsConstructor
@RestController
@Tag(name = "방명록", description = "방명록 관련 API")
public class GuestbookController {

    private final GuestbookService guestbookService;

    @GetMapping
    @Operation(summary = "내 방명록 조회", description = "본인에게 작성된 방명록을 조회합니다. 작성한 방명록은 24시간동안만 유지됩니다.")
    public ApiResponse<List<GetGuestbookResponse>> getNyGuestbook(@Parameter(hidden = true) @Login AuthenticateUser user) {
        return ApiResponse.ok(guestbookService.getMyGuestbook(user));
    }

    @GetMapping("/{friendId}")
    @Operation(summary = "친구 방명록 조회", description = "친구에게 작성된 방명록을 조회합니다. 작성한 방명록은 24시간동안만 유지됩니다.")
    public ApiResponse<List<GetGuestbookResponse>> getFriendGuestbook(@Parameter(hidden = true) @Login AuthenticateUser user, @PathVariable("friendId") String friendId) {
        return ApiResponse.ok(guestbookService.getFriendGuestbook(user, friendId));
    }

    @PostMapping
    @Operation(summary = "방명록 작성", description = "userId에 해당하는 사람에게 방명록을 작성합니다. 작성된 방명록은 24시간동안만 유지됩니다.")
    public ApiResponse<AddGuestbookResponse> addGuestbook(@Parameter(hidden = true) @Login AuthenticateUser user, @RequestBody AddGuestbookRequest request) {
        return ApiResponse.ok(guestbookService.addGuestbook(user, request));
    }

    @DeleteMapping
    @Operation(summary = "방명록 삭제", description = "수동으로 방명록을 삭제합니다. (자동으로 24시간후에 삭제됨)")
    public ApiResponse<String> deleteGuestbook(@Parameter(hidden = true) @Login AuthenticateUser user, @RequestBody DeleteGuestbookRequest request) {
        return ApiResponse.ok(guestbookService.deleteGuestbook(user, request));
    }
}
