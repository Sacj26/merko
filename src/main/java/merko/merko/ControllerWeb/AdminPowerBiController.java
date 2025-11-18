package merko.merko.ControllerWeb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPowerBiController {

    @GetMapping("/admin/dashboard/powerbi")
    public String powerbi() {
        return "admin/dashboard/powerbi";
    }
}