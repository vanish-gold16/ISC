package org.example.isc.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping
    public String getManagement(){
        return "private/admin/management";
    }

}
