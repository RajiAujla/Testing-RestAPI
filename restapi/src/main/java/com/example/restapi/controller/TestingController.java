package com.example.restapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiOperation;

@RestController
public class TestingController {

    @ApiOperation(value = "Testing")
    @RequestMapping(value = "/testing", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Boolean testing() {
        return true;
    }

}
