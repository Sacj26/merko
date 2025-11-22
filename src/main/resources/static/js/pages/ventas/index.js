document.addEventListener('DOMContentLoaded', () => {
  const denseToggle = document.getElementById('toggleDenseVentas');
  const table = document.querySelector('.table-admin');
  const pageSizeSelector = document.getElementById('pageSizeSelector');

  // Densidad compacta con persistencia
  const STORAGE_KEY = 'ventasTableDensity';
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
      localStorage.setItem('ventasPageSize', pageSizeSelector.value);
      console.log('Tamaño de página cambiado a:', pageSizeSelector.value);
    });
    
    const savedSize = localStorage.getItem('ventasPageSize');
    if (savedSize) {
      pageSizeSelector.value = savedSize;
    }
  }

  // Filtros dinámicos
  const filterCliente = document.getElementById('filterCliente');
  const filterSucursal = document.getElementById('filterSucursal');
  const filterFecha = document.getElementById('filterFecha');
  const filterForm = document.querySelector('.filters-modern');

  // Auto-submit al cambiar filtros
  if (filterCliente) {
    filterCliente.addEventListener('change', () => {
      if (filterForm) filterForm.submit();
    });
  }

  if (filterSucursal) {
    filterSucursal.addEventListener('change', () => {
      if (filterForm) filterForm.submit();
    });
  }

  if (filterFecha) {
    filterFecha.addEventListener('change', () => {
      if (filterForm) filterForm.submit();
    });
  }

  // Feedback visual
  const showFilteringFeedback = () => {
    const btn = filterForm?.querySelector('button[type="submit"]');
    if (btn) {
      const icon = btn.querySelector('i');
      const originalClass = icon.className;
      icon.className = 'fas fa-spinner fa-spin';
      setTimeout(() => { icon.className = originalClass; }, 500);
    }
  };

  if (filterForm) {
    filterForm.addEventListener('submit', showFilteringFeedback);
  }
});