document.addEventListener('DOMContentLoaded', () => {
  const inputNombre = document.getElementById('filtro-nombre');
  const inputPrecio = document.getElementById('filtro-precio');
  const catalogo = document.getElementById('catalogo');

  function filtrarProductos() {
    const filtroNombre = inputNombre.value.toLowerCase();
    const filtroPrecio = parseFloat(inputPrecio.value);

    const cards = catalogo.querySelectorAll('.card-producto');

    cards.forEach(card => {
      const nombre = card.getAttribute('data-nombre').toLowerCase();
      const precio = parseFloat(card.getAttribute('data-precio'));

      const cumpleNombre = nombre.includes(filtroNombre);
      const cumplePrecio = isNaN(filtroPrecio) || precio <= filtroPrecio;

      if (cumpleNombre && cumplePrecio) {
        card.style.display = 'block';
      } else {
        card.style.display = 'none';
      }
    });
  }


  inputNombre.addEventListener('input', filtrarProductos);
  inputPrecio.addEventListener('input', filtrarProductos);
});

document.addEventListener('DOMContentLoaded', () => {
  const catalogo = document.getElementById('catalogo');
  const btnIzquierda = document.getElementById('btn-izquierda');
  const btnDerecha = document.getElementById('btn-derecha');

  const scrollAmount = 300; // pixeles a desplazar al click

  btnIzquierda.addEventListener('click', () => {
    catalogo.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
  });

  btnDerecha.addEventListener('click', () => {
    catalogo.scrollBy({ left: scrollAmount, behavior: 'smooth' });
  });
});

