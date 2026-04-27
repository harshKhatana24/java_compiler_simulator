package com.algorithmavengers.compiler.controller;


import com.algorithmavengers.compiler.model.CompilerResult;
import com.algorithmavengers.compiler.service.CompilerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

@RestController
@CrossOrigin
public class CompilerController {

    @Autowired
    CompilerService compilerService;

    // POST /compile with text body
    @PostMapping("/compile")
    public ResponseEntity<CompilerResult> compile(@RequestBody String sourceCode) {
        return ResponseEntity.ok(compilerService.compile(sourceCode));
    }

    // POST /compile/file — upload a file
    @PostMapping("/compile/file")
    public ResponseEntity<CompilerResult> compileFile(@RequestParam("file") MultipartFile file) {
        try {
            String code = new String(file.getBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(compilerService.compile(code));
        } catch (Exception e) {
            CompilerResult r = new CompilerResult();
            r.errorMessage = "File read error: " + e.getMessage();
            return ResponseEntity.badRequest().body(r);
        }
    }
}