package com.workruit.quiz.controllers;


import com.workruit.quiz.controllers.dto.ApiResponse;
import com.workruit.quiz.controllers.dto.Message;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.QuestionAnswerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@Slf4j
public class UploadQuestionController {

    @Autowired private QuestionAnswerService questionAnswerService;

    @PostMapping(value = "/questions/upload-csv", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseBody
    public ResponseEntity uploadCSV(@RequestParam("file") @RequestPart MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(questionAnswerService.createUploadJob(file, userDetailsDTO.getId()));
            apiResponse.setMessage("Questions uploaded");
            apiResponse.setMsg(Message.builder().description("Questions uploaded successfully").title("Success").build());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }


    @GetMapping(value = "/questions/upload-status")
    @ResponseBody
    public ResponseEntity uploadStatus(@RequestParam("jobId") Long jobId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setData(questionAnswerService.getJobStatus(jobId,userDetailsDTO.getId()));
            apiResponse.setMessage("Get questions upload status");
            apiResponse.setMsg(Message.builder().description("Get status").title("Success").build());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }

    }
}
