package quotestamp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import quotestamp.service.DataService;

@Controller
public class IndexController {

    @Autowired private DataService data;

    @GetMapping(path="/")
    public String index() {
        return "index";
    }

    @GetMapping(path="/results")
    public String getResults(@RequestParam("search")String search) {
        return "results";
    }
}