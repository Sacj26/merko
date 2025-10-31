package merko.merko.Config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorPage = "error/error";
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                errorPage = "error/404";
            }
            else if(statusCode == HttpStatus.FORBIDDEN.value()) {
                errorPage = "error/403";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorPage = "error/500";
            }
            
            model.addAttribute("codigo", statusCode);
        }
        
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (message != null) {
            model.addAttribute("mensaje", message);
        }
        
        return errorPage;
    }
}