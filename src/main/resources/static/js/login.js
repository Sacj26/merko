document.addEventListener('DOMContentLoaded', () => {
    console.log('Formulario cargado');

    const form = document.querySelector('form');
    form.addEventListener('submit', (e) => {
        const email = form.querySelector('input[type="email"]').value;
        const pass = form.querySelector('input[type="password"]').value;

        if (!email || !pass) {
            e.preventDefault();
            alert('Completa todos los campos');
        }
    });
});
