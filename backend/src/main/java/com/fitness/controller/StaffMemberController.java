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
     GET /api/staff/members?keyword=
     * Tra cứu danh sách học viên theo từ khóa (trả về List<MemberLookupResponse>)
     */
    @GetMapping
    public ResponseEntity<List<MemberLookupResponse>> searchMembers(
            @RequestParam(required = false) String keyword) {
        List<MemberLookupResponse> members = staffMemberService.searchMembers(keyword);
        return ResponseEntity.ok(members);
    }
    
    /**
     * GET /api/staff/members/{id}/membership
     * Kiểm tra chi tiết thẻ tập của 1 học viên cụ thể (trả về 1 MemberLookupResponse)
     */
    @GetMapping("/{id}/membership")
    public ResponseEntity<MemberLookupResponse> getMemberMembership(@PathVariable Long id) {
        MemberLookupResponse membership = staffMemberService.getMemberMembership(id);
        return ResponseEntity.ok(membership);
    }
}