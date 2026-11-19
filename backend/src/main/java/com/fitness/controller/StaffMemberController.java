package com.fitness.controller;

import com.fitness.dto.MemberLookupResponse;
import com.fitness.service.StaffMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/members")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffMemberController {

    private final StaffMemberService staffMemberService;

    /**
     * GET /api/staff/members?keyword=
     */
    @GetMapping
    public ResponseEntity<List<MemberLookupResponse>> searchMembers(
            @RequestParam(required = false) String keyword) {
        List<MemberLookupResponse> members = staffMemberService.searchMembers(keyword);
        return ResponseEntity.ok(members);
    }
}