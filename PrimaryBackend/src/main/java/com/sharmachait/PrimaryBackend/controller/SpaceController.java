package com.sharmachait.PrimaryBackend.controller;

import com.sharmachait.PrimaryBackend.models.dto.SpaceDto;
import com.sharmachait.PrimaryBackend.models.entity.Space;
import com.sharmachait.PrimaryBackend.repository.SpaceRepository;
import com.sharmachait.PrimaryBackend.service.space.SpaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/space")
public class SpaceController {
    private final SpaceService spaceService;
    private final SpaceRepository spaceRepository;
    @PostMapping
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> postSpace(@RequestBody @Valid SpaceDto spaceDto, @RequestHeader("Authorization") String authHeader) {
        try{
            Space space = spaceService.save(authHeader, spaceDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(space);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @GetMapping("/all")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getSpaces(@RequestHeader("Authorization") String authHeader) {
        try{
            List<Space> spaces = spaceService.findByUserId(authHeader);
            return ResponseEntity.status(HttpStatus.OK).body(spaces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{spaceId}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteSpace(@PathVariable String spaceId, @RequestHeader("Authorization") String authHeader) {
        try{
            spaceService.deleteById(authHeader, spaceId);
            return ResponseEntity.status(HttpStatus.OK).body("Space Deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
//
//    @GetMapping("/{spaceId}")
////    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public ResponseEntity<?> getSpace(@PathVariable String spaceId) {
//
//    }
//
//    @PostMapping("/element/{spaceId}")
////    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public ResponseEntity<?> postSpaceElement(@RequestBody SpaceElementDto spaceElementDto, @PathVariable String spaceId) {
//
//    }
//
//    @DeleteMapping("/element/{spaceId}")
////    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
//    public ResponseEntity<?> deleteSpaceElement(@RequestBody SpaceElementDto spaceElementDto, @PathVariable String spaceId) {
//
//    }
}
