document.addEventListener("DOMContentLoaded", function() {
    fetchMyFacilities();

    // Setup filter listeners
    const filters = document.querySelectorAll('.filter-btn');
    filters.forEach(tab => {
        tab.addEventListener('click', function() {
            filters.forEach(t => {
                t.classList.remove('active', 'bg-blue-600', 'text-white');
                t.classList.add('text-gray-500');
            });
            this.classList.remove('text-gray-500');
            this.classList.add('active', 'bg-blue-600', 'text-white');
            
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
            // Handle ApiResponse wrapper if present
            allFacilities = data.data ? data.data : data;
            updateStatsAndCounts(allFacilities);
            renderFacilities(allFacilities);
        })
        .catch(error => {
            console.error('Error fetching facilities:', error);
            const grid = document.getElementById('facilityGrid');
            if (grid) grid.innerHTML = `<div class="col-span-full text-center text-red-500 py-8">Lỗi tải dữ liệu: ${error.message}</div>`;
        });
}

function updateStatsAndCounts(facilities) {
    let total = facilities.length;
    let approved = 0;
    let pending = 0;
    let rejected = 0;
    let sportsCount = 0;
    let courtsCount = 0;

    facilities.forEach(f => {
        if (f.approvalStatus === 'APPROVED') approved++;
        else if (f.approvalStatus === 'PENDING') pending++;
        else if (f.approvalStatus === 'REJECTED') rejected++;
        
        sportsCount += (f.totalSports || 0);
        courtsCount += (f.totalCourts || 0);
    });

    // Update Stats Row
    if(document.getElementById('statTotalFacilities')) document.getElementById('statTotalFacilities').innerText = total;
    if(document.getElementById('statApproved')) document.getElementById('statApproved').innerText = approved;
    if(document.getElementById('statPending')) document.getElementById('statPending').innerText = pending;
    if(document.getElementById('statSports')) document.getElementById('statSports').innerText = sportsCount;
    if(document.getElementById('statCourts')) document.getElementById('statCourts').innerText = courtsCount;

    // Update Filter Tab Counts
    if(document.getElementById('countAll')) document.getElementById('countAll').innerText = total;
    if(document.getElementById('countApproved')) document.getElementById('countApproved').innerText = approved;
    if(document.getElementById('countPending')) document.getElementById('countPending').innerText = pending;
    if(document.getElementById('countRejected')) document.getElementById('countRejected').innerText = rejected;
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
            statusBadge = `<div class="absolute top-3 left-3 bg-yellow-500 text-white text-[10px] uppercase font-bold px-3 py-1.5 rounded-full flex items-center shadow-md">
                <svg class="w-3.5 h-3.5 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                ${i18n.filterPending}
            </div>`;
        } else if (f.approvalStatus === 'APPROVED') {
            statusBadge = `<div class="absolute top-3 left-3 bg-green-500 text-white text-[10px] uppercase font-bold px-3 py-1.5 rounded-full flex items-center shadow-md">
                <svg class="w-3.5 h-3.5 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                ${i18n.filterApproved}
            </div>`;
        } else if (f.approvalStatus === 'REJECTED') {
            statusBadge = `<div class="absolute top-3 left-3 bg-red-600 text-white text-[10px] uppercase font-bold px-3 py-1.5 rounded-full flex items-center shadow-md">
                <svg class="w-3.5 h-3.5 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                ${i18n.filterRejected}
            </div>`;
        }

        let activeToggle = '';
        if (f.isActive !== false) {
            activeToggle = `<div class="absolute top-3 right-3 bg-black/40 backdrop-blur-sm text-white text-[10px] font-bold px-3 py-1.5 rounded-full flex items-center gap-2">
                ${i18n.statusActive}
                <div class="w-6 h-3.5 bg-green-400 rounded-full flex items-center p-0.5"><div class="w-2.5 h-2.5 bg-white rounded-full ml-auto"></div></div>
            </div>`;
        } else {
            activeToggle = `<div class="absolute top-3 right-3 bg-black/60 backdrop-blur-sm text-white text-[10px] font-bold px-3 py-1.5 rounded-full flex items-center gap-2">
                ${i18n.statusInactive}
                <div class="w-6 h-3.5 bg-gray-500 rounded-full flex items-center p-0.5"><div class="w-2.5 h-2.5 bg-white rounded-full"></div></div>
            </div>`;
        }

        const date = new Date(f.createdAt).toLocaleDateString('vi-VN');
        const img = f.thumbnailUrl || 'https://placehold.co/600x400?text=Sport+Facility';
        
        let tagsHtml = '';
        if (f.sportNames && f.sportNames.length > 0) {
            tagsHtml = f.sportNames.map(name => `<span class="px-3 py-1 bg-blue-50 text-blue-600 text-[10px] font-bold rounded-full">${name}</span>`).join('');
        } else {
            tagsHtml = `<span class="px-3 py-1 bg-gray-50 text-gray-500 text-[10px] font-bold rounded-full">Chưa có môn nào</span>`;
        }

        let rejectionHtml = '';
        if (f.approvalStatus === 'REJECTED' && f.rejectionReason) {
            rejectionHtml = `<div class="mt-4 pt-3 border-t border-red-100 flex items-start text-red-600 text-xs font-medium">
                <svg class="w-4 h-4 mr-1.5 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                <span>${i18n.rejectionReason} ${f.rejectionReason}</span>
            </div>`;
        }

        return `
            <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-lg transition-all duration-300 relative group cursor-pointer flex flex-col h-full" onclick="window.location.href='/owner/facilities/${f.facilityId}'">
                
                <div class="h-48 w-full bg-gray-200 relative overflow-hidden flex-shrink-0">
                    <img src="${img}" alt="${f.name}" class="w-full h-full object-cover group-hover:scale-105 transition duration-500" onerror="this.src='https://placehold.co/600x400?text=No+Image'">
                    <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent"></div>
                    ${statusBadge}
                    ${activeToggle}
                    <div class="absolute bottom-3 right-3 text-white text-xs flex items-center font-medium opacity-90">
                        <svg class="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                        ${i18n.createdDate} ${date}
                    </div>
                </div>
                
                <div class="p-5 flex-1 flex flex-col">
                    <h3 class="text-lg font-bold text-gray-900 mb-1 line-clamp-1 group-hover:text-blue-600 transition-colors">${f.name}</h3>
                    <p class="text-xs text-gray-500 mb-5 line-clamp-1 flex items-center">
                        <svg class="w-3.5 h-3.5 mr-1 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                        ${f.fullAddress || f.address || 'Chưa cập nhật địa chỉ'}
                    </p>
                    
                    <div class="grid grid-cols-2 gap-4 mb-5">
                        <div>
                            <p class="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">${i18n.totalCourts}</p>
                            <p class="font-bold text-gray-900">${f.totalCourts || 0} <span class="text-sm font-medium text-gray-500 ml-0.5" style="text-transform:none;">${i18n.courtsLabel}</span></p>
                        </div>
                        <div>
                            <p class="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">${i18n.totalSports}</p>
                            <p class="font-bold text-gray-900">${f.totalSports || 0} <span class="text-sm font-medium text-gray-500 ml-0.5" style="text-transform:none;">${i18n.businessSports}</span></p>
                        </div>
                    </div>
                    
                    <div class="mt-auto pt-4 border-t border-gray-100 flex items-center justify-between">
                        <div class="flex gap-2">
                            ${tagsHtml}
                        </div>
                        <span class="text-xs font-semibold text-blue-600 group-hover:text-blue-800 transition-colors">
                            ${i18n.manageDetail}
                        </span>
                    </div>

                    ${rejectionHtml}
                </div>
            </div>
        `;
    }).join('');
}
