package com.mvc.mock_project.controller.api;

import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.SportAttributeDTO;
import com.mvc.mock_project.dto.response.SportDTO;
import com.mvc.mock_project.service.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/sports")
@RequiredArgsConstructor
public class PublicSportApiController {

    private final SportService sportService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SportDTO>>> getAllActiveSports() {
        List<SportDTO> sports = sportService.getAllActiveSports();
        return ResponseEntity.ok(ApiResponse.success("Success", sports));
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<ApiResponse<List<SportAttributeDTO>>> getSportAttributes(@PathVariable Integer id) {
        List<SportAttributeDTO> attributes = sportService.getSportAttributes(id);
        return ResponseEntity.ok(ApiResponse.success("Success", attributes));
    }
}
