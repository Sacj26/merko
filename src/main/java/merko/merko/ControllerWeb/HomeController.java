package merko.merko.ControllerWeb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la página de inicio
 */
@Controller
public class HomeController {

    /**
     * Redirige la raíz "/" a la página de productos públicos
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/publico/productos";
    }
}
