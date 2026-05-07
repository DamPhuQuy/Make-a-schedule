# Appointment Creation Flow - Implementation Guide

Tài liệu này mô tả chi tiết từng thao tác trong flow tạo appointment và mapping với code thực tế.

## Table of Contents
1. [Validation Phase](#validation-phase)
2. [Group Meeting Match Flow](#group-meeting-match-flow)
3. [Time Conflict Flow](#time-conflict-flow)
4. [No Conflict Flow](#no-conflict-flow)

---

## Validation Phase

### 1. User enters appointment details
**Frontend**: User nhập thông tin appointment vào form

### 2. Frontend → Controller: validateAppointment(request, user)
**Endpoint**: `POST /api/appointments/validate`

**File**: `AppointmentController.java:46-50`
```java
@PostMapping("/validate")
public AppointmentConflictResponse validateAppointment(
    @Valid @RequestBody ValidateAppointmentRequest request,
    @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return appointmentUseCase.validateAppointment(request, currentUser);
}
```

### 3. Controller → Service: validateAppointment(request, user)
**File**: `AppointmentService.java:55-72`
```java
@Override
@Transactional(readOnly = true)
public AppointmentConflictResponse validateAppointment(
    ValidateAppointmentRequest request, 
    UserDetailsImpl currentUser) {
    User user = getUserOrThrow(currentUser.getId());
    validateAppointmentBasicFields(request.getName(), request.getStartTime(), request.getEndTime());

    var groupMeetingMatch = findMatchingGroupMeeting(request);
    if (groupMeetingMatch.isPresent()) {
        return buildGroupMeetingMatchResponse(groupMeetingMatch.get());
    }

    var overlappingAppointment = findOverlappingAppointment(user.getId(), request.getStartTime(), request.getEndTime());
    if (overlappingAppointment.isPresent()) {
        return buildTimeOverlapResponse(overlappingAppointment.get());
    }

    return buildNoConflictResponse();
}
```

**Các bước xử lý**:

#### 3.1. Get User
**File**: `AppointmentService.java:304-307`
```java
private User getUserOrThrow(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));
}
```

#### 3.2. Validate Basic Fields
**File**: `AppointmentService.java:74-84`
```java
private void validateAppointmentBasicFields(String name, Instant startTime, Instant endTime) {
    if (name == null || name.trim().isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment name cannot be empty");
    }
    if (startTime == null || endTime == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end times are required");
    }
    if (!endTime.isAfter(startTime)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
    }
}
```

---

## Group Meeting Match Flow

### 4. Service → GroupMeetingRepository: findMatchingGroupMeeting(name, time)
**File**: `AppointmentService.java:86-91`
```java
private Optional<GroupMeeting> findMatchingGroupMeeting(ValidateAppointmentRequest request) {
    return groupMeetingRepository.findByName(request.getName()).stream()
        .filter(gm -> gm.getTimeSlot().getStartTime().equals(request.getStartTime())
                   && gm.getTimeSlot().getEndTime().equals(request.getEndTime()))
        .findFirst();
}
```

**Repository method**: `GroupMeetingRepository.findByName(String name)`

### 5. Build GROUP_MEETING_MATCH Response
**File**: `AppointmentService.java:99-115`
```java
private AppointmentConflictResponse buildGroupMeetingMatchResponse(GroupMeeting match) {
    List<String> participantEmails = match.getParticipants().stream()
        .map(p -> p.getUser().getEmail())
        .toList();

    return AppointmentConflictResponse.builder()
        .conflictType(AppointmentConflictResponse.ConflictType.GROUP_MEETING_MATCH)
        .message("A group meeting with the same name and time already exists. Would you like to join it?")
        .details(AppointmentConflictResponse.ConflictDetails.builder()
            .matchingGroupMeetingId(match.getId())
            .matchingGroupMeetingName(match.getName())
            .matchingGroupStartTime(match.getTimeSlot().getStartTime())
            .matchingGroupEndTime(match.getTimeSlot().getEndTime())
            .participants(participantEmails)
            .build())
        .build();
}
```

### 6. User confirms join → Frontend → Controller: createAppointment(forceJoin=true)
**Endpoint**: `POST /api/appointments`

**File**: `AppointmentController.java:52-56`
```java
@PostMapping
public AppointmentResponse createAppointment(
    @Valid @RequestBody CreateAppointmentRequest request,
    @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return appointmentUseCase.createAppointment(request, currentUser);
}
```

### 7. Handle Force Join
**File**: `AppointmentService.java:207-209` (trong createAppointment method)
```java
if (request.isForceJoin()) {
    return handleForceJoin(request, user);
}
```

**File**: `AppointmentService.java:222-226`
```java
private AppointmentResponse handleForceJoin(CreateAppointmentRequest request, User user) {
    GroupMeeting gm = findMatchingGroupMeetingOrThrow(request);
    addUserToGroupMeetingIfNeeded(gm, user);
    return mapGroupToResponse(gm);
}
```

### 8. Find Matching Group Meeting
**File**: `AppointmentService.java:228-234`
```java
private GroupMeeting findMatchingGroupMeetingOrThrow(CreateAppointmentRequest request) {
    return groupMeetingRepository.findByName(request.getName()).stream()
        .filter(gm -> gm.getTimeSlot().getStartTime().equals(request.getStartTime())
                   && gm.getTimeSlot().getEndTime().equals(request.getEndTime()))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group meeting not found"));
}
```

### 9. Add User to Group Meeting
**File**: `AppointmentService.java:236-247`
```java
private void addUserToGroupMeetingIfNeeded(GroupMeeting gm, User user) {
    boolean isAlreadyParticipant = gm.getParticipants().stream()
        .anyMatch(p -> p.getUser().getId().equals(user.getId()));

    if (!isAlreadyParticipant) {
        GroupMeetingParticipant participant = new GroupMeetingParticipant();
        participant.setGroupMeeting(gm);
        participant.setUser(user);
        gm.getParticipants().add(participant);
        groupMeetingRepository.save(gm);
    }
}
```

**Note**: Nếu user đã là participant thì skip việc thêm lại

### 10. Create Reminder (Optional)
**File**: `AppointmentService.java:217` (called after joining group meeting)
```java
createReminderIfNeeded(appointment.getId(), request.getReminderMinutes());
```

**Note**: Reminder được tạo cho group meeting ID, và sẽ gửi email cho tất cả participants khi đến hạn

---

## Time Conflict Flow

### 10. Service → AppointmentRepository: findOverlappingAppointments(userId, time)
**File**: `AppointmentService.java:93-97`
```java
private Optional<Appointment> findOverlappingAppointment(
        Long userId, Instant startTime, Instant endTime) {
    return appointmentRepository.findOverlappingAppointmentsForUser(userId, startTime, endTime).stream()
        .findFirst();
}
```

**Repository method**: `AppointmentRepository.findOverlappingAppointmentsForUser(Long userId, Instant startTime, Instant endTime)`

### 11. Build TIME_OVERLAP Response
**File**: `AppointmentService.java:117-130`
```java
private AppointmentConflictResponse buildTimeOverlapResponse(Appointment conflict) {
    String conflictType = conflict instanceof GroupMeeting ? "group meeting" : "appointment";
    return AppointmentConflictResponse.builder()
        .conflictType(AppointmentConflictResponse.ConflictType.TIME_OVERLAP)
        .message("You already have a " + conflictType + " at this time. Would you like to choose another time or replace it?")
        .details(AppointmentConflictResponse.ConflictDetails.builder()
            .conflictingAppointmentId(conflict.getId())
            .conflictingAppointmentName(conflict.getName())
            .conflictingStartTime(conflict.getTimeSlot().getStartTime())
            .conflictingEndTime(conflict.getTimeSlot().getEndTime())
            .build())
        .build();
}
```

### 12. User chooses Replace → Frontend → Controller: createAppointment(forceReplace=true)
**Endpoint**: `POST /api/appointments`

### 13. Handle Force Replace
**File**: `AppointmentService.java:211-212` (trong createAppointment method)
```java
checkConflictsIfNeeded(request, user.getId());
handleForceReplaceIfNeeded(request, user.getId());
```

**File**: `AppointmentService.java:261-270`
```java
private void handleForceReplaceIfNeeded(CreateAppointmentRequest request, Long userId) {
    if (request.isForceReplace()) {
        List<Appointment> overlapping =
            appointmentRepository.findOverlappingAppointmentsForUser(userId, request.getStartTime(), request.getEndTime());

        for (Appointment overlap : overlapping) {
            deleteOrRemoveUserFromAppointment(overlap, userId);
        }
    }
}
```

**Note**: Recheck để tránh race condition

### 14. Delete or Remove User from Overlapping Appointments
**File**: `AppointmentService.java:272-278`
```java
private void deleteOrRemoveUserFromAppointment(Appointment appointment, Long userId) {
    if (appointment instanceof GroupMeeting) {
        removeUserFromGroupMeeting((GroupMeeting) appointment, userId);
    } else if (appointment instanceof PersonalAppointment) {
        personalAppointmentRepository.delete((PersonalAppointment) appointment);
    }
}
```

#### 14.1. Remove User from Group Meeting
**File**: `AppointmentService.java:280-287`
```java
private void removeUserFromGroupMeeting(GroupMeeting gm, Long userId) {
    gm.getParticipants().removeIf(p -> p.getUser().getId().equals(userId));
    if (gm.getParticipants().isEmpty()) {
        groupMeetingRepository.delete(gm);
    } else {
        groupMeetingRepository.save(gm);
    }
}
```

**Note**: Nếu không còn participant nào thì xóa group meeting

### 15. Save New Appointment
**File**: `AppointmentService.java:214-215`
```java
PersonalAppointment appointment = createPersonalAppointment(request, user);
appointment = personalAppointmentRepository.save(appointment);
```

**File**: `AppointmentService.java:289-296`
```java
private PersonalAppointment createPersonalAppointment(CreateAppointmentRequest request, User user) {
    PersonalAppointment appointment = new PersonalAppointment();
    appointment.setName(request.getName());
    appointment.setLocation(request.getLocation());
    appointment.setTimeSlot(new TimeSlot(request.getStartTime(), request.getEndTime()));
    appointment.setOwner(user);
    return appointment;
}
```

### 16. Create Reminder (Optional)
**File**: `AppointmentService.java:217`
```java
createReminderIfNeeded(appointment.getId(), request.getReminderMinutes());
```

**File**: `AppointmentService.java:298-302`
```java
private void createReminderIfNeeded(Long appointmentId, Integer reminderMinutes) {
    if (reminderMinutes != null) {
        createReminderUseCase.execute(appointmentId, reminderMinutes);
    }
}
```

**CreateReminderUseCase implementation**: `CreateReminderService.java:15-23`
```java
@Override
public Reminder execute(Long appointmentId, Integer minutesBefore) {
    Reminder reminder = Reminder.builder()
            .appointmentId(appointmentId)
            .minutesBefore(minutesBefore)
            .build();
    return reminderRepository.save(reminder);
}
```

---

## No Conflict Flow

### 17. Build No Conflict Response
**File**: `AppointmentService.java:132-137`
```java
private AppointmentConflictResponse buildNoConflictResponse() {
    return AppointmentConflictResponse.builder()
        .conflictType(AppointmentConflictResponse.ConflictType.NONE)
        .message("No conflicts found. You can proceed with creating the appointment.")
        .build();
}
```

### 18. User clicks create → Frontend → Controller: createAppointment()
**Endpoint**: `POST /api/appointments`

### 19. Check Conflicts
**File**: `AppointmentService.java:249-259`
```java
private void checkConflictsIfNeeded(CreateAppointmentRequest request, Long userId) {
    if (!request.isForceReplace()) {
        List<Appointment> overlapping =
            appointmentRepository.findOverlappingAppointmentsForUser(userId, request.getStartTime(), request.getEndTime());

        if (!overlapping.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Time conflict with existing appointment. Please choose another time or use forceReplace.");
        }
    }
}
```

**Note**: Nếu không có forceReplace flag và có conflict thì throw exception

### 20. Save Appointment
**File**: `AppointmentService.java:214-215`
```java
PersonalAppointment appointment = createPersonalAppointment(request, user);
appointment = personalAppointmentRepository.save(appointment);
```

### 21. Create Reminder (Optional)
**File**: `AppointmentService.java:217`
```java
createReminderIfNeeded(appointment.getId(), request.getReminderMinutes());
```

---

## Additional Flow: Create Group Meeting

### Endpoint: POST /api/appointments/group
**File**: `AppointmentController.java:58-62`
```java
@PostMapping("/group")
public AppointmentResponse createGroupMeeting(
    @Valid @RequestBody CreateAppointmentRequest request,
    @AuthenticationPrincipal UserDetailsImpl currentUser) {
    return appointmentUseCase.createGroupMeeting(request, currentUser);
}
```

### Implementation
**File**: `AppointmentService.java:310-325`
```java
@Override
@Transactional
public AppointmentResponse createGroupMeeting(CreateAppointmentRequest request, UserDetailsImpl currentUser) {
    User user = getUserOrThrow(currentUser.getId());
    validateAppointmentBasicFields(request.getName(), request.getStartTime(), request.getEndTime());

    checkConflictsIfNeeded(request, user.getId());
    handleForceReplaceIfNeeded(request, user.getId());

    GroupMeeting gm = createGroupMeetingWithCreator(request, user);
    addParticipantsToGroupMeeting(gm, request.getParticipantUsernames(), user.getEmail());

    gm = groupMeetingRepository.save(gm);

    createReminderIfNeeded(gm.getId(), request.getReminderMinutes());

    return mapGroupToResponse(gm);
}
```

**Note**: Reminder được tạo sau khi save group meeting để có ID

### Create Group Meeting with Creator
**File**: `AppointmentService.java:325-337`
```java
private GroupMeeting createGroupMeetingWithCreator(CreateAppointmentRequest request, User creator) {
    GroupMeeting gm = new GroupMeeting();
    gm.setName(request.getName());
    gm.setTimeSlot(new TimeSlot(request.getStartTime(), request.getEndTime()));
    gm.setParticipants(new HashSet<>());

    GroupMeetingParticipant creatorParticipant = new GroupMeetingParticipant();
    creatorParticipant.setGroupMeeting(gm);
    creatorParticipant.setUser(creator);
    gm.getParticipants().add(creatorParticipant);

    return gm;
}
```

### Add Participants
**File**: `AppointmentService.java:339-355`
```java
private void addParticipantsToGroupMeeting(GroupMeeting gm, List<String> participantEmails, String creatorEmail) {
    if (participantEmails == null || participantEmails.isEmpty()) {
        return;
    }

    for (String email : participantEmails) {
        String trimmedEmail = email.trim();
        if (!trimmedEmail.isEmpty() && !trimmedEmail.equals(creatorEmail)) {
            userRepository.findByEmail(trimmedEmail).ifPresent(participantUser -> {
                GroupMeetingParticipant participant = new GroupMeetingParticipant();
                participant.setGroupMeeting(gm);
                participant.setUser(participantUser);
                gm.getParticipants().add(participant);
            });
        }
    }
}
```

**Note**: 
- Skip nếu email trống hoặc trùng với creator
- Chỉ thêm user nếu tồn tại trong database

---

## Response Mapping

### Personal Appointment Response
**File**: `AppointmentService.java:398-414`
```java
private AppointmentResponse mapToResponse(PersonalAppointment param) {
    List<Reminder> reminders = reminderRepository.findByAppointmentId(param.getId());
    Integer reminderMinutes = reminders.isEmpty() ? null : reminders.get(0).getMinutesBefore();

    return AppointmentResponse.builder()
        .id(param.getId())
        .name(param.getName())
        .location(param.getLocation())
        .startTime(param.getTimeSlot().getStartTime())
        .endTime(param.getTimeSlot().getEndTime())
        .reminderMinutes(reminderMinutes)
        .isGroupMeeting(false)
        .ownerUsername(param.getOwner().getEmail())
        .appointmentType(com.schedule.app.domain.model.AppointmentType.NORMAL_APPOINTMENT)
        .participants(null)
        .build();
}
```

### Group Meeting Response
**File**: `AppointmentService.java:419-438`
```java
private AppointmentResponse mapGroupToResponse(GroupMeeting param) {
    List<String> participantEmails = param.getParticipants().stream()
        .map(p -> p.getUser().getEmail())
        .toList();

    List<Reminder> reminders = reminderRepository.findByAppointmentId(param.getId());
    Integer reminderMinutes = reminders.isEmpty() ? null : reminders.get(0).getMinutesBefore();

    return AppointmentResponse.builder()
        .id(param.getId())
        .name(param.getName())
        .location("Multiple")
        .startTime(param.getTimeSlot().getStartTime())
        .endTime(param.getTimeSlot().getEndTime())
        .reminderMinutes(reminderMinutes)
        .isGroupMeeting(true)
        .ownerUsername("Group")
        .appointmentType(com.schedule.app.domain.model.AppointmentType.GROUP_MEETING)
        .participants(participantEmails)
        .build();
}
```

**Note**: Group meeting giờ đây cũng query và hiển thị reminderMinutes giống như personal appointment

---

## Reminder Sending Flow (Scheduled Task)

Phần này mô tả cách reminder được gửi tự động bởi scheduled task.

### 1. Scheduled Task Execution
**File**: `ReminderService.java:33-53`
```java
@Scheduled(fixedRate = 60000)
@Transactional
public void scanAndSendReminders() {
    Instant now = Instant.now();
    List<Reminder> dueReminders = reminderRepository.findAllDueWithDynamicTime(now);

    log.info("[ReminderService] Found {} reminders to process", dueReminders.size());

    for (Reminder reminder : dueReminders) {
        appointmentRepository.findById(reminder.getAppointmentId()).ifPresent(appointment -> {
            try {
                sendReminderForAppointment(appointment);
                log.info("[ReminderService] Sent reminder for appointment '{}'", appointment.getName());
                reminderRepository.delete(reminder);
            } catch (Exception e) {
                log.error("[ReminderService] Failed to send email for appointment id={}: {}",
                        reminder.getAppointmentId(), e.getMessage());
            }
        });
    }
}
```

**Trigger**: Chạy mỗi 60 giây (60000ms)

**Repository method**: `ReminderRepository.findAllDueWithDynamicTime(Instant now)` - Query tất cả reminders đến hạn

### 2. Send Reminder Based on Appointment Type
**File**: `ReminderService.java:48-56`
```java
private void sendReminderForAppointment(Appointment appointment) {
    if (appointment instanceof PersonalAppointment personalAppointment) {
        emailService.sendReminderEmail(personalAppointment.getOwner().getEmail(), personalAppointment);
    } else if (appointment instanceof GroupMeeting groupMeeting) {
        emailService.sendGroupMeetingReminder(groupMeeting);
    }
}
```

**Logic**: Sử dụng `instanceof` để phân biệt loại appointment và gọi method tương ứng

### 3. Send Personal Appointment Reminder
**File**: `EmailService.java:16-30`
```java
public void sendReminderEmail(String toEmail, PersonalAppointment appointment) {
    ZonedDateTime startTime = appointment.getTimeSlot().getStartTime()
            .atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
    String formattedTime = startTime.format(formatter);

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject("Reminder: " + appointment.getName());
    message.setText("Hello,\n\nThis is a reminder for your upcoming appointment: " + appointment.getName() +
            "\nLocation: " + appointment.getLocation() +
            "\nStart Time: " + formattedTime +
            "\n\nBest regards,\nMake-a-schedule App");

    mailSender.send(message);
}
```

**Recipient**: Owner của personal appointment

### 4. Send Group Meeting Reminder
**File**: `EmailService.java:32-49`
```java
public void sendGroupMeetingReminder(GroupMeeting groupMeeting) {
    ZonedDateTime startTime = groupMeeting.getTimeSlot().getStartTime()
            .atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
    String formattedTime = startTime.format(formatter);

    for (GroupMeetingParticipant participant : groupMeeting.getParticipants()) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(participant.getUser().getEmail());
        message.setSubject("Reminder: " + groupMeeting.getName());
        message.setText("Hello,\n\nThis is a reminder for your upcoming group meeting: " + groupMeeting.getName() +
                "\nStart Time: " + formattedTime +
                "\n\nBest regards,\nMake-a-schedule App");

        mailSender.send(message);
    }
}
```

**Recipients**: Tất cả participants trong group meeting

**Note**: Mỗi participant nhận một email riêng

### 5. Delete Reminder After Sending
**File**: `ReminderService.java:46`
```java
reminderRepository.delete(reminder);
```

**Note**: Reminder được xóa sau khi gửi thành công để tránh gửi lại

---

## Summary of Key Files

| File | Purpose |
|------|---------|
| `AppointmentController.java` | REST API endpoints |
| `AppointmentService.java` | Business logic implementation |
| `CreateReminderService.java` | Reminder creation logic |
| `ReminderService.java` | Scheduled task for sending reminders |
| `EmailService.java` | Email sending logic for reminders |
| `AppointmentRepository.java` | Base appointment repository |
| `PersonalAppointmentRepository.java` | Personal appointment repository |
| `GroupMeetingRepository.java` | Group meeting repository |
| `ReminderRepository.java` | Reminder repository |
| `UserRepository.java` | User repository |

---

## Transaction Boundaries

### Read-Only Transactions
- `validateAppointment()` - Line 56
- `getAllAppointments()` - Line 140
- `getAppointmentById()` - Line 170

### Write Transactions
- `createAppointment()` - Line 202
- `createGroupMeeting()` - Line 310
- `deleteAppointment()` - Line 358

**Note**: Tất cả write operations đều được wrap trong `@Transactional` để đảm bảo data consistency.
