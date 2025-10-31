(function(){
  const ventasCtx = document.getElementById('ventasDiariasChart');
  if (ventasCtx) {
      fetch('/admin/dashboard/api/ventas-diarias?dias=30')
          .then(r => r.json())
          .then(data => {
              const labels = data.map(d => d.fecha);
              const totals = data.map(d => d.total);
              const hasData = totals.some(v => v > 0);
              // eslint-disable-next-line no-undef
              new Chart(ventasCtx, {
                  type: 'line',
                  data: {
                      labels,
                      datasets: [{
                          label: 'Total',
                          data: totals,
                          borderColor: '#2563eb',
                          backgroundColor: 'rgba(37, 99, 235, 0.15)',
                          tension: 0.25,
                          fill: true
                      }]
                  },
                  options: { responsive: true, maintainAspectRatio: false }
              });
              if (!hasData) document.getElementById('ventasEmpty').style.display='block';
          })
          .catch(console.error);
  }

  const topCtx = document.getElementById('topProductosChart');
  if (topCtx) {
      fetch('/admin/dashboard/api/top-productos?dias=30&n=5')
          .then(r => r.json())
          .then(data => {
              const labels = data.map(d => d.nombre);
              const totals = data.map(d => d.cantidad);
              const hasData = totals.some(v => v > 0);
              // eslint-disable-next-line no-undef
              new Chart(topCtx, {
                  type: 'bar',
                  data: {
                      labels,
                      datasets: [{
                          label: 'Unidades',
                          data: totals,
                          backgroundColor: '#16a34a'
                      }]
                  },
                  options: { responsive: true, maintainAspectRatio: false }
              });
              if (!hasData) document.getElementById('topVentasEmpty').style.display='block';
          })
          .catch(console.error);
  }

  const comprasCtx = document.getElementById('comprasDiariasChart');
  if (comprasCtx) {
      fetch('/admin/dashboard/api/compras-diarias?dias=30')
          .then(r => r.json())
          .then(data => {
              const labels = data.map(d => d.fecha);
              const totals = data.map(d => d.total);
              const hasData = totals.some(v => v > 0);
              if (hasData) {
                  // eslint-disable-next-line no-undef
                  new Chart(comprasCtx, {
                      type: 'line',
                      data: {
                          labels,
                          datasets: [{
                              label: 'Total compras',
                              data: totals,
                              borderColor: '#ea580c',
                              backgroundColor: 'rgba(234, 88, 12, 0.15)',
                              tension: 0.25,
                              fill: true
                          }]
                      },
                      options: { responsive: true, maintainAspectRatio: false }
                  });
              } else {
                  comprasCtx.style.display = 'none';
                  document.getElementById('comprasEmpty').style.display='block';
              }
          })
          .catch(err => {
              console.error('Error al cargar compras-diarias:', err);
          });
  }

  const topComprasCtx = document.getElementById('topProductosComprasChart');
  if (topComprasCtx) {
      fetch('/admin/dashboard/api/top-productos-compras?dias=30&n=5')
          .then(r => r.json())
          .then(data => {
              const labels = data.map(d => d.nombre);
              const totals = data.map(d => d.cantidad);
              const hasData = totals.some(v => v > 0);
              if (hasData) {
                  // eslint-disable-next-line no-undef
                  new Chart(topComprasCtx, {
                      type: 'bar',
                      data: {
                          labels,
                          datasets: [{
                              label: 'Unidades compradas',
                              data: totals,
                              backgroundColor: '#0ea5e9'
                          }]
                      },
                      options: { responsive: true, maintainAspectRatio: false }
                  });
              } else {
                  topComprasCtx.style.display = 'none';
                  document.getElementById('topComprasEmpty').style.display='block';
              }
          })
          .catch(err => {
              console.error('Error al cargar top-productos-compras:', err);
          });
  }
})();
