document.addEventListener('DOMContentLoaded', () => {
  const denseToggle = document.getElementById('toggleDenseCompras');
  const table = document.querySelector('.table-admin');
  const pageSizeSelector = document.getElementById('pageSizeSelector');
  const filterBranch = document.getElementById('filterBranch');
  const filterFecha = document.getElementById('filterFecha');
  const filterForm = document.querySelector('.filters-modern');

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

  // Filtros dinámicos - Auto-submit cuando cambian los valores
  const filterProveedor = document.getElementById('filterProveedor');
  
  // Cuando cambia el proveedor, limpiar sucursal y recargar
  if (filterProveedor) {
    filterProveedor.addEventListener('change', () => {
      if (filterBranch) {
        filterBranch.value = ''; // Limpiar sucursal seleccionada
      }
      if (filterForm) {
        filterForm.submit();
      }
    });
  }

  if (filterBranch) {
    filterBranch.addEventListener('change', () => {
      if (filterForm) {
        filterForm.submit();
      }
    });
  }

  if (filterFecha) {
    filterFecha.addEventListener('change', () => {
      if (filterForm) {
        filterForm.submit();
      }
    });
  }

  // Guardar estado de filtros en localStorage
  const saveFilterState = () => {
    const filters = {
      proveedorId: filterProveedor?.value || '',
      branchId: filterBranch?.value || '',
      fecha: filterFecha?.value || ''
    };
    localStorage.setItem('comprasFilters', JSON.stringify(filters));
  };

  // Restaurar estado de filtros
  const restoreFilterState = () => {
    try {
      const saved = localStorage.getItem('comprasFilters');
      if (saved) {
        const filters = JSON.parse(saved);
        if (filterProveedor && !filterProveedor.value) filterProveedor.value = filters.proveedorId || '';
        if (filterBranch && !filterBranch.value) filterBranch.value = filters.branchId || '';
        if (filterFecha && !filterFecha.value) filterFecha.value = filters.fecha || '';
      }
    } catch (e) {
      console.error('Error restaurando filtros:', e);
    }
  };

  // Guardar al cambiar
  filterProveedor?.addEventListener('change', saveFilterState);
  filterBranch?.addEventListener('change', saveFilterState);
  filterFecha?.addEventListener('change', saveFilterState);

  // Feedback visual al filtrar
  const showFilteringFeedback = () => {
    const btn = filterForm?.querySelector('button[type="submit"]');
    if (btn) {
      const icon = btn.querySelector('i');
      const originalClass = icon.className;
      icon.className = 'fas fa-spinner fa-spin';
      setTimeout(() => {
        icon.className = originalClass;
      }, 500);
    }
  };

  if (filterForm) {
    filterForm.addEventListener('submit', showFilteringFeedback);
  }
});