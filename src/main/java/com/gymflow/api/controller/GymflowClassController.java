package com.gymflow.api.controller;

import com.gymflow.api.controller.dto.ClassSessionDto;
import com.gymflow.api.controller.dto.GymflowClassDto;
import com.gymflow.api.controller.mapper.GymflowClassDtoMapper;
import com.gymflow.api.service.GymflowClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
public class GymflowClassController {

    private final GymflowClassService gymflowClassService;
    private final GymflowClassDtoMapper mapper;

    @GetMapping
    public ResponseEntity<List<GymflowClassDto>> getClasses() {
        List<GymflowClassDto> classes = gymflowClassService.getClasses().stream().map(mapper::fromCore).toList();
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/{classId}/sessions")
    public ResponseEntity<List<ClassSessionDto>> getSessions(@PathVariable UUID classId) {
        List<ClassSessionDto> sessions = gymflowClassService.getClassSessions(classId).stream().map(mapper::fromCore).toList();
        return ResponseEntity.ok(sessions);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{classId}")
    public ResponseEntity<Void> updateClass(@PathVariable UUID classId, @RequestBody GymflowClassDto classDto) {
        gymflowClassService.updateClass(classId, mapper.toCore(classDto));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Void> createClass(@RequestBody GymflowClassDto classDto) {
        gymflowClassService.createClass(mapper.toCore(classDto));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
