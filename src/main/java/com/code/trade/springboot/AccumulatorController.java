package com.code.trade.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Bala
 */

@RestController
@RequestMapping("accumulator")
public class AccumulatorController {

    @Autowired
    AccumulatorService accumulatorService;

    @GetMapping("/add")
    @ResponseBody
    public int add(@RequestParam String number){
        System.out.println("Add method initialized.");
        return accumulatorService.add(number);
    }

    @GetMapping("/test")
    @ResponseBody
    public String add(){
        System.out.println("Welcome to OMS App.");
        return "Welcome to OMS App.";
    }
}
