document.addEventListener("DOMContentLoaded", () => {
    const formularios = document.querySelectorAll(".form-agregar-carrito");

    formularios.forEach(form => {
        form.addEventListener("submit", (e) => {
            const cantidadInput = form.querySelector("input[name='cantidad']");
            const cantidad = parseInt(cantidadInput.value);

            if (isNaN(cantidad) || cantidad < 1) {
                e.preventDefault();
                alert("La cantidad debe ser al menos 1.");
                return;
            }

            // Mostrar animación temporal en el botón
            const boton = form.querySelector("button");
            const textoOriginal = boton.textContent;
            boton.textContent = "Agregado ✔";
            boton.disabled = true;
            boton.style.backgroundColor = "#4CAF50";

            setTimeout(() => {
                boton.textContent = textoOriginal;
                boton.disabled = false;
                boton.style.backgroundColor = ""; // o el color original
            }, 1500);
        });
    });
});
