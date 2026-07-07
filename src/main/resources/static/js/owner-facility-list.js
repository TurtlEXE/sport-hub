document.addEventListener("DOMContentLoaded", function() {
    fetchMyFacilities();

    // Setup filter listeners
    const filters = document.querySelectorAll('.filter-tab');
    filters.forEach(tab => {
        tab.addEventListener('click', function() {
            filters.forEach(t => t.classList.remove('active', 'border-blue-600', 'text-blue-600'));
            this.classList.add('active', 'border-blue-600', 'text-blue-600');
            const status = this.getAttribute('data-status');
            filterFacilities(status);
        });
    });
});

let allFacilities = [];

function fetchMyFacilities() {
    fetch('/api/owner/facilities')
        .then(response => {
            if (!response.ok) throw new Error("Network response was not ok");
            return response.json();
        })
        .then(data => {
            allFacilities = data;
            renderFacilities(allFacilities);
        })
        .catch(error => {
            console.error('Error fetching facilities:', error);
            const grid = document.getElementById('facilityGrid');
            if (grid) grid.innerHTML = `<div class="col-span-full text-center text-red-500 py-8">Lỗi tải dữ liệu: ${error.message}</div>`;
        });
}

function filterFacilities(status) {
    if (status === 'ALL' || !status) {
        renderFacilities(allFacilities);
    } else {
        const filtered = allFacilities.filter(f => f.approvalStatus === status);
        renderFacilities(filtered);
    }
}

function renderFacilities(facilities) {
    const grid = document.getElementById('facilityGrid');
    const emptyState = document.getElementById('emptyState');
    
    if (!grid) return;

    if (!facilities || facilities.length === 0) {
        grid.innerHTML = '';
        grid.classList.add('hidden');
        if (emptyState) emptyState.classList.remove('hidden');
        return;
    }

    grid.classList.remove('hidden');
    if (emptyState) emptyState.classList.add('hidden');

    grid.innerHTML = facilities.map(f => {
        let statusBadge = '';
        if (f.approvalStatus === 'PENDING') {
            statusBadge = `<span class="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs font-semibold rounded-full absolute top-3 right-3 shadow-sm">Chờ duyệt</span>`;
        } else if (f.approvalStatus === 'APPROVED') {
            statusBadge = `<span class="px-2 py-1 bg-green-100 text-green-800 text-xs font-semibold rounded-full absolute top-3 right-3 shadow-sm">Đã duyệt</span>`;
        } else if (f.approvalStatus === 'REJECTED') {
            statusBadge = `<span class="px-2 py-1 bg-red-100 text-red-800 text-xs font-semibold rounded-full absolute top-3 right-3 shadow-sm">Bị từ chối</span>`;
        }

        const date = new Date(f.createdAt).toLocaleDateString('vi-VN');
        const img = f.thumbnailUrl || '/images/default-facility.jpg';

        return `
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition duration-200 relative group cursor-pointer" onclick="window.location.href='/owner/facilities/${f.facilityId}'">
                ${statusBadge}
                <div class="h-48 w-full bg-gray-200 overflow-hidden">
                    <img src="${img}" alt="${f.name}" class="w-full h-full object-cover group-hover:scale-105 transition duration-300" onerror="this.src='https://placehold.co/600x400?text=No+Image'">
                </div>
                <div class="p-5">
                    <h3 class="text-lg font-bold text-gray-900 mb-1 truncate">${f.name}</h3>
                    <p class="text-sm text-gray-500 mb-4 truncate flex items-center">
                        <svg class="w-4 h-4 mr-1 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                        ${f.address}
                    </p>
                    
                    <div class="flex justify-between items-center pt-4 border-t border-gray-100">
                        <div class="text-sm font-medium text-gray-700">
                            <span class="text-blue-600 font-bold">${f.totalCourts || 0}</span> Sân • <span class="text-blue-600 font-bold">${f.totalSports || 0}</span> Môn
                        </div>
                        <div class="text-xs text-gray-400">
                            Tạo: ${date}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}
