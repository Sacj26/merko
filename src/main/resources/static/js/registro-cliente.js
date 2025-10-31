function validarFormulario() {
    const correo = document.getElementById('correo').value;
    const confirmarCorreo = document.getElementById('confirmarCorreo').value;
    const password = document.getElementById('password').value;
    const confirmarPassword = document.getElementById('confirmarPassword').value;

    if (correo !== confirmarCorreo) {
        alert('Los correos no coinciden.');
        return false;
    }

    if (password !== confirmarPassword) {
        alert('Las contraseñas no coinciden.');
        return false;
    }

    return true;
}
