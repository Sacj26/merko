document.addEventListener('DOMContentLoaded', () => {
  // Usar el input correcto del slider de precio
  const inputPrecio = document.getElementById('priceRange');
  const priceValue = document.getElementById('priceValue');
  const productGrid = document.getElementById('productGrid');

  // Función para actualizar el valor mostrado del precio
  function actualizarPrecioMostrado() {
    if (inputPrecio && priceValue) {
      const valor = parseInt(inputPrecio.value);
      priceValue.textContent = '$' + valor.toLocaleString('es-CO');
    }
  }

  // Función para filtrar productos por precio
  function filtrarProductos() {
    if (!inputPrecio || !productGrid) return;
    
    const filtroPrecio = parseFloat(inputPrecio.value);
    const cards = productGrid.querySelectorAll('.card-producto');

    cards.forEach(card => {
      const precio = parseFloat(card.getAttribute('data-precio'));
      
      // Mostrar solo si el precio es menor o igual al filtro
      if (!isNaN(precio) && precio <= filtroPrecio) {
        card.style.display = 'block';
      } else {
        card.style.display = 'none';
      }
    });
  }

  // Eventos
  if (inputPrecio) {
    inputPrecio.addEventListener('input', () => {
      actualizarPrecioMostrado();
      filtrarProductos();
    });
    
    // Inicializar valor mostrado
    actualizarPrecioMostrado();
  }
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

