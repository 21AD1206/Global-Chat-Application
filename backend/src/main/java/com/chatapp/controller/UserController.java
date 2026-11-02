package com.chatapp.controller;

import com.chatapp.dto.ChangePasswordRequest;
import com.chatapp.dto.UpdateProfileRequest;
import com.chatapp.dto.UserDto;
import com.chatapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/** User discovery, profiles and account settings. All endpoints require login. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/users?search=ali&filter=online   (filter: all | online | live | offline) */
    @GetMapping
    public List<UserDto> list(@RequestParam(required = false) String search,
                              @RequestParam(required = false, defaultValue = "all") String filter) {
        return userService.list(search, filter);
    }

    /** GET /api/users/me  -> own profile including email */
    @GetMapping("/me")
    public UserDto me(Principal principal) {
        return userService.toSelfDto(userService.getByUsername(principal.getName()));
    }

    /** PUT /api/users/me  { name, bio } */
    @PutMapping("/me")
    public UserDto updateMe(Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.getName(), request);
    }

    /** PUT /api/users/me/password  { currentPassword, newPassword } */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/users/{username}  -> public profile + relation to the viewer */
    @GetMapping("/{username}")
    public UserDto profile(@PathVariable String username, Principal principal) {
        return userService.profile(username, principal.getName());
    }
}
