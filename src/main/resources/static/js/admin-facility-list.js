document.addEventListener("DOMContentLoaded", function() {
    fetchFacilities();
    fetchFacilityStats();

    // Setup filter listeners
    const filters = document.querySelectorAll('.filter-tab');
    filters.forEach(tab => {
        tab.addEventListener('click', function() {
            filters.forEach(t => {
                t.classList.remove('active', 'bg-white', 'shadow-sm', 'text-gray-900');
                t.classList.add('text-gray-500', 'hover:text-gray-900');
            });
            this.classList.remove('text-gray-500', 'hover:text-gray-900');
            this.classList.add('active', 'bg-white', 'shadow-sm', 'text-gray-900');
            const status = this.getAttribute('data-status');
            fetchFacilities(status === 'ALL' ? null : status);
        });
    });
});

function fetchFacilityStats() {
    fetch('/api/admin/facilities/stats')
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch stats");
            return response.json();
        })
        .then(stats => {
            // Update Stats Cards
            if (document.getElementById('statTotalRequests')) document.getElementById('statTotalRequests').textContent = stats.totalRequests;
            if (document.getElementById('statPending')) document.getElementById('statPending').textContent = stats.pendingReview;
            if (document.getElementById('statApproved')) document.getElementById('statApproved').textContent = stats.approved;
            if (document.getElementById('statRejected')) document.getElementById('statRejected').textContent = stats.rejected;

            if (document.getElementById('heroPendingCount')) document.getElementById('heroPendingCount').textContent = stats.pendingReview;
            if (document.getElementById('queueActionCount')) document.getElementById('queueActionCount').textContent = `${stats.pendingReview} Action${stats.pendingReview !== 1 ? 's' : ''}`;


            // Update Doughnut Chart
            if (document.getElementById('chartTotalFacilities')) document.getElementById('chartTotalFacilities').textContent = stats.totalRequests;
            
            const pctApproved = stats.totalRequests > 0 ? (stats.approved / stats.totalRequests) * 100 : 0;
            const pctPending = stats.totalRequests > 0 ? (stats.pendingReview / stats.totalRequests) * 100 : 0;
            const pctRejected = stats.totalRequests > 0 ? (stats.rejected / stats.totalRequests) * 100 : 0;

            if (document.getElementById('chartApprovedPct')) document.getElementById('chartApprovedPct').textContent = Math.round(pctApproved) + '%';
            if (document.getElementById('chartPendingPct')) document.getElementById('chartPendingPct').textContent = Math.round(pctPending) + '%';
            if (document.getElementById('chartRejectedPct')) document.getElementById('chartRejectedPct').textContent = Math.round(pctRejected) + '%';

            if (document.getElementById('chartApprovedLabel')) document.getElementById('chartApprovedLabel').textContent = `Approved (${stats.approved})`;
            if (document.getElementById('chartPendingLabel')) document.getElementById('chartPendingLabel').textContent = `Pending (${stats.pendingReview})`;
            if (document.getElementById('chartRejectedLabel')) document.getElementById('chartRejectedLabel').textContent = `Rejected (${stats.rejected})`;

            // Update Doughnut Chart conic gradient dynamically
            const doughnut = document.getElementById('doughnutChart');
            if (doughnut && stats.totalRequests > 0) {
                const pendingStart = pctApproved;
                const rejectedStart = pctApproved + pctPending;
                doughnut.style.background = `conic-gradient(#10b981 0% ${pendingStart}%, #f59e0b ${pendingStart}% ${rejectedStart}%, #ef4444 ${rejectedStart}% 100%)`;
            } else if (doughnut) {
                 doughnut.style.background = `conic-gradient(#e5e7eb 0% 100%)`;
            }

            // Update Progress Bars
            const container = document.getElementById('sportDistributionContainer');
            if (container && stats.sportDistribution) {
                container.innerHTML = stats.sportDistribution.map((sport, index) => {
                    const colors = ['bg-blue-500', 'bg-green-500', 'bg-yellow-500', 'bg-purple-500', 'bg-pink-500'];
                    const colorClass = colors[index % colors.length];
                    return `
                        <div>
                            <div class="flex justify-between items-end mb-2">
                                <div class="flex items-center gap-2"><div class="w-2 h-2 rounded-full ${colorClass}"></div><span class="text-sm font-bold text-gray-700">${sport.sportName}</span></div>
                                <span class="text-sm font-bold text-gray-900">${sport.courtCount} Courts (${sport.percentage}%)</span>
                            </div>
                            <div class="w-full bg-gray-100 rounded-full h-2.5">
                                <div class="${colorClass} h-2.5 rounded-full" style="width: ${sport.percentage}%"></div>
                            </div>
                        </div>
                    `;
                }).join('');
            }

            // Update density info text
            if (document.getElementById('densityInfoText')) {
                document.getElementById('densityInfoText').textContent = `Density is calculated based on a total of ${stats.totalActiveCourts} active sports courts belonging to ${stats.totalActiveFacilities} partner facilities.`;
            }
        })
        .catch(err => console.error(err));
}

function fetchFacilities(status = null, page = 0, size = 10) {
    let url = `/api/admin/facilities?page=${page}&size=${size}`;
    if (status) {
        url += `&status=${status}`;
    }

    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error("Network response was not ok");
            return response.json();
        })
        .then(data => {
            renderFacilities(data.content); // Spring Data Page returns content array
            renderPagination(data);
        })
        .catch(error => {
            console.error('Error fetching facilities:', error);
            const tbody = document.querySelector('tbody') || createTable();
            tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-4 text-center text-red-500">Lỗi khi tải dữ liệu: ${error.message}</td></tr>`;
        });
}

function renderFacilities(facilities) {
    const tbody = document.getElementById('facilityTableBody');
    if (!tbody) return;

    if (!facilities || facilities.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-8 text-center text-gray-500">No facilities found.</td></tr>`;
        return;
    }

    tbody.innerHTML = facilities.map(f => {
        const dateObj = f.createdAt ? new Date(f.createdAt) : new Date();
        const formattedDate = dateObj.toLocaleDateString('en-GB'); // dd/MM/yyyy

        let actionBtn = `<a href="/admin/facilities/${f.facilityId}" class="inline-flex items-center justify-center bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-xl text-sm font-bold shadow-sm transition">Review Application <svg class="w-4 h-4 ml-2 opacity-70" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg></a>`;
        
        if(f.approvalStatus !== 'PENDING') {
             actionBtn = `<a href="/admin/facilities/${f.facilityId}" class="inline-flex items-center justify-center bg-gray-100 hover:bg-gray-200 text-gray-700 px-5 py-2.5 rounded-xl text-sm font-bold transition">View Details</a>`;
        }

        // We mock Sport Type since the API DTO currently doesn't include specific sports in the listing
        const sportMock = "Multiple Sports"; 

        return `
            <tr class="hover:bg-gray-50/50 transition border-b border-gray-100 last:border-0">
                <td class="px-6 py-5 whitespace-nowrap">
                    <div class="flex items-center gap-4">
                        <div class="flex-shrink-0 h-14 w-14 rounded-2xl bg-gray-100 shadow-inner overflow-hidden border border-gray-100">
                            ${f.thumbnailUrl ? `<img src="${f.thumbnailUrl}" class="h-full w-full object-cover" alt="">` : `<div class="h-full w-full flex items-center justify-center text-gray-300"><svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg></div>`}
                        </div>
                        <div>
                            <h6 class="text-sm font-bold text-gray-900 mb-0.5">${f.name}</h6>
                            <div class="flex items-center text-xs text-gray-500">
                                <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                                <span class="truncate max-w-[200px]">${f.fullAddress || f.address || 'No address'}</span>
                            </div>
                        </div>
                    </div>
                </td>
                <td class="px-6 py-5 whitespace-nowrap">
                    <div class="flex flex-col">
                        <div class="flex items-center gap-1.5 mb-1">
                            <svg class="w-3.5 h-3.5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                            <span class="text-sm font-bold text-gray-900">${f.ownerName || 'Not updated'}</span>
                        </div>
                        <div class="flex items-center gap-1.5 pl-5 text-xs text-gray-500">
                            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path></svg>
                            ${f.ownerPhone || 'No Phone'}
                        </div>
                    </div>
                </td>
                <td class="px-6 py-5 whitespace-nowrap">
                    <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-600 border border-blue-100">${sportMock}</span>
                </td>
                <td class="px-6 py-5 whitespace-nowrap">
                    <div class="flex items-center text-sm text-gray-600">
                        <svg class="w-4 h-4 mr-1.5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                        ${formattedDate}
                    </div>
                </td>
                <td class="px-6 py-5 whitespace-nowrap text-right">
                    ${actionBtn}
                </td>
            </tr>
        `;
    }).join('');
}

function renderPagination(pageData) {
    // Basic pagination logic placeholder
}
