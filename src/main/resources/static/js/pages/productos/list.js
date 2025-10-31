(function(){
  const form = document.getElementById('productosFilterForm');
  const sizeSelect = form ? form.querySelector('select[name="size"]') : null;
  const denseToggle = document.getElementById('toggleDense');
  const table = document.querySelector('.table-admin');
  const container = document.querySelector('.table-container.table-scroll');

  // Auto-submit al cambiar tamaño de página, y reinicia a la primera página
  if (form && sizeSelect) {
    sizeSelect.addEventListener('change', () => {
      // Asegura parámetro page=0 para volver a la primera página
      let pageHidden = form.querySelector('input[name="page"]');
      if (!pageHidden) {
        pageHidden = document.createElement('input');
        pageHidden.type = 'hidden';
        pageHidden.name = 'page';
        form.appendChild(pageHidden);
      }
      pageHidden.value = '0';
      form.submit();
    });
  }

  // Densidad compacta con persistencia en localStorage (tabla principal)
  const STORAGE_KEY = 'productosTableDensity';
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

  // Densidad compacta para tabla de historial de compras
  const denseToggleCompras = document.getElementById('toggleDenseComprasHistory');
  const comprasTable = document.getElementById('comprasHistoryTable');
  const STORAGE_KEY_COMPRAS = 'productosComprasHistoryDensity';
  const savedCompras = localStorage.getItem(STORAGE_KEY_COMPRAS);
  
  if (comprasTable && savedCompras === 'dense') {
    comprasTable.classList.add('dense');
    if (denseToggleCompras) denseToggleCompras.checked = true;
  }
  if (denseToggleCompras && comprasTable) {
    denseToggleCompras.addEventListener('change', () => {
      comprasTable.classList.toggle('dense', denseToggleCompras.checked);
      localStorage.setItem(STORAGE_KEY_COMPRAS, denseToggleCompras.checked ? 'dense' : 'normal');
    });
  }
})();
