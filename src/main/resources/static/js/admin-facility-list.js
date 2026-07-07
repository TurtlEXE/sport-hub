document.addEventListener("DOMContentLoaded", function() {
    fetchFacilities();

    // Setup filter listeners if they exist (they don't in the provided HTML but just in case)
    const filters = document.querySelectorAll('.filter-tab');
    filters.forEach(tab => {
        tab.addEventListener('click', function() {
            filters.forEach(t => t.classList.remove('active', 'border-blue-600', 'text-blue-600'));
            this.classList.add('active', 'border-blue-600', 'text-blue-600');
            const status = this.getAttribute('data-status');
            fetchFacilities(status === 'ALL' ? null : status);
        });
    });
});

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
    // If the HTML structure is missing a tbody, let's create a basic table
    let tableContainer = document.querySelector('.bg-white.rounded-lg.shadow-sm.border.border-gray-200.overflow-hidden');
    if (tableContainer && !tableContainer.querySelector('table')) {
        tableContainer.innerHTML = `
            <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Cơ Sở</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Chủ Sân</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng Thái</th>
                        <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Thao Tác</th>
                    </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200"></tbody>
            </table>
        `;
    }

    const tbody = document.querySelector('tbody');
    if (!tbody) return;

    if (!facilities || facilities.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="px-6 py-8 text-center text-gray-500">Không có dữ liệu cơ sở nào.</td></tr>`;
        return;
    }

    tbody.innerHTML = facilities.map(f => {
        let statusClass = "bg-gray-100 text-gray-800";
        let statusText = f.approvalStatus;
        if (f.approvalStatus === 'PENDING') {
            statusClass = "bg-yellow-100 text-yellow-800";
            statusText = "Chờ duyệt";
        } else if (f.approvalStatus === 'APPROVED') {
            statusClass = "bg-green-100 text-green-800";
            statusText = "Đã duyệt";
        } else if (f.approvalStatus === 'REJECTED') {
            statusClass = "bg-red-100 text-red-800";
            statusText = "Từ chối";
        }

        return `
            <tr>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                        <div class="flex-shrink-0 h-10 w-10 bg-gray-200 rounded-md overflow-hidden">
                            ${f.thumbnailUrl ? `<img src="${f.thumbnailUrl}" class="h-10 w-10 object-cover" alt="">` : `<div class="h-full w-full flex items-center justify-center text-gray-400">No Img</div>`}
                        </div>
                        <div class="ml-4">
                            <div class="text-sm font-medium text-gray-900">${f.name}</div>
                            <div class="text-sm text-gray-500 truncate max-w-xs">${f.address}</div>
                        </div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm text-gray-900">${f.ownerName || 'Chưa cập nhật'}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${statusClass}">
                        ${statusText}
                    </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <a href="/admin/facilities/${f.facilityId}" class="text-blue-600 hover:text-blue-900">Chi tiết & Duyệt</a>
                </td>
            </tr>
        `;
    }).join('');
}

function renderPagination(pageData) {
    // Basic pagination logic placeholder
}
