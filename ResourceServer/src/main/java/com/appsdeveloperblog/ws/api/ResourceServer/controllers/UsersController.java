package com.appsdeveloperblog.ws.api.ResourceServer.controllers;

import com.appsdeveloperblog.ws.api.ResourceServer.response.User;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UsersController {

    @GetMapping("/status/check")
    public String statusCheck() {
        return "Working...";
    }

    // Allows more complex expressions, is evaluated before the method is executed
    // can check roles and parameters properties
    @PreAuthorize("hasRole('developer') or #id == #jwt.subject")
    //@Secured("ROLE_developer") // allows the operation only to users with developer role
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return "Deleted user with id " + id + " and JWT subject " + jwt.getSubject();
    }

    @PostAuthorize("returnObject.id == #jwt.subject")
    @GetMapping("/{id}")
    public User getUser(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        String hardcodedJwtSubject = "5557c002-50be-40e6-b4ff-27be46a9320a";
        return new User(hardcodedJwtSubject, "Alvaro", "Pedraza");
    }
}
