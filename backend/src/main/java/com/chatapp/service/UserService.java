package com.chatapp.service;

import com.chatapp.dto.ChangePasswordRequest;
import com.chatapp.dto.UpdateProfileRequest;
import com.chatapp.dto.UserDto;
import com.chatapp.exception.ApiException;
import com.chatapp.model.ChatRequest;
import com.chatapp.model.RequestStatus;
import com.chatapp.model.User;
import com.chatapp.repository.ChatRequestRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** User lookup, discovery (search + Online/LIVE filters), public profiles and settings. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ChatRequestRepository requestRepository;
    private final PresenceService presenceService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ChatRequestRepository requestRepository,
                       PresenceService presenceService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.presenceService = presenceService;
        this.passwordEncoder = passwordEncoder;
    }

    public User getByUsername(String username) {
        if (username == null) {
            throw ApiException.notFound("User not found");
        }
        return userRepository.findByUsername(username.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    /** Public view (no email) with live presence information. */
    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getUsername(), null, user.getBio(),
                user.getCreatedAt(), user.getLastSeen(),
                presenceService.isOnline(user.getUsername()), presenceService.isLive(user.getUsername()),
                null, null);
    }

    /** Own profile: includes the email address. */
    public UserDto toSelfDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getUsername(), user.getEmail(), user.getBio(),
                user.getCreatedAt(), user.getLastSeen(),
                presenceService.isOnline(user.getUsername()), presenceService.isLive(user.getUsername()),
                "SELF", null);
    }

    /**
     * User discovery.
     * @param search  matches username or display name (case-insensitive); blank = everyone
     * @param filter  all | online | live | offline
     * Sorted LIVE first, then online, then alphabetically.
     */
    @Transactional(readOnly = true)
    public List<UserDto> list(String search, String filter) {
        String q = search == null ? "" : search.trim();
        List<User> users = q.isEmpty() ? userRepository.findAllByOrderByUsernameAsc() : userRepository.search(q);
        String f = filter == null ? "all" : filter.trim().toLowerCase(Locale.ROOT);
        return users.stream()
                .map(this::toDto)
                .filter(u -> switch (f) {
                    case "online" -> u.online();
                    case "live" -> u.live();
                    case "offline" -> !u.online();
                    default -> true;
                })
                .sorted(Comparator.comparing(UserDto::live).reversed()
                        .thenComparing(Comparator.comparing(UserDto::online).reversed())
                        .thenComparing(UserDto::username))
                .toList();
    }

    /**
     * Public profile of another user, plus how the viewer is related to them:
     * SELF, NONE, OUTGOING_PENDING, INCOMING_PENDING or ACCEPTED (with the request id).
     */
    @Transactional(readOnly = true)
    public UserDto profile(String username, String viewerUsername) {
        User target = getByUsername(username);
        User viewer = getByUsername(viewerUsername);
        if (target.getId().equals(viewer.getId())) {
            return toSelfDto(target);
        }
        UserDto base = toDto(target);
        String relation = "NONE";
        Long requestId = null;

        List<ChatRequest> accepted = requestRepository.findBetween(viewer, target, RequestStatus.ACCEPTED);
        List<ChatRequest> pending = requestRepository.findBetween(viewer, target, RequestStatus.PENDING);
        if (!accepted.isEmpty()) {
            relation = "ACCEPTED";
            requestId = accepted.get(0).getId();
        } else if (!pending.isEmpty()) {
            ChatRequest r = pending.get(0);
            relation = r.getSender().getId().equals(viewer.getId()) ? "OUTGOING_PENDING" : "INCOMING_PENDING";
            requestId = r.getId();
        }
        return new UserDto(base.id(), base.name(), base.username(), null, base.bio(), base.createdAt(),
                base.lastSeen(), base.online(), base.live(), relation, requestId);
    }

    @Transactional
    public UserDto updateProfile(String username, UpdateProfileRequest req) {
        User user = getByUsername(username);
        user.setName(req.name().trim());
        String bio = req.bio() == null ? null : req.bio().trim();
        user.setBio(bio == null || bio.isEmpty() ? null : bio);
        return toSelfDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest req) {
        User user = getByUsername(username);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect", "currentPassword");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }
}
