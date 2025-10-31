(function(){
  const denseToggle = document.getElementById('toggleDenseProvProductos');
  const table = document.querySelector('.table-admin');

  // Densidad compacta con persistencia
  const STORAGE_KEY = 'proveedorVerTableDensity';
  const saved = localStorage.getItem(STORAGE_KEY);
  if (table && saved === 'dense') {
    table.classList.add('dense');
    if (denseToggle) denseToggle.checked = true;
  }
  if (denseToggle && table) {
    denseToggle.addEventListener('change', () => {
      table.classList.toggle('dense', denseToggle.checked);
      localStorage.setItem(STORAGE_KEY, denseToggle.checked ? 'dense' : 'normal');
    });
  }
})();
