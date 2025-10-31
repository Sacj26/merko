document.addEventListener('DOMContentLoaded', () => {
  const denseToggle = document.getElementById('toggleDenseCompras');
  const table = document.querySelector('.table-admin');
  const pageSizeSelector = document.getElementById('pageSizeSelector');

  // Densidad compacta con persistencia
  const STORAGE_KEY = 'comprasTableDensity';
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

  // Selector de tamaño de página
  if (pageSizeSelector) {
    pageSizeSelector.addEventListener('change', () => {
      localStorage.setItem('comprasPageSize', pageSizeSelector.value);
      console.log('Tamaño de página cambiado a:', pageSizeSelector.value);
    });
    
    const savedSize = localStorage.getItem('comprasPageSize');
    if (savedSize) {
      pageSizeSelector.value = savedSize;
    }
  }

  // Filtros existentes
  document.getElementById('filterProveedor')?.addEventListener('change', function() {
    // TODO: Implementar filtrado por proveedor
  });

  document.getElementById('filterFecha')?.addEventListener('change', function() {
    // TODO: Implementar filtrado por fecha
  });
});