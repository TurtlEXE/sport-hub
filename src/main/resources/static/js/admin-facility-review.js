document.addEventListener("DOMContentLoaded", function() {
    if (typeof facilityId !== 'undefined' && facilityId > 0) {
        fetchFacilityDetail(facilityId);
    } else {
        document.getElementById('facilityName').innerHTML = '<div class="text-red-500">Invalid Facility ID</div>';
    }
});

let currentFacilityData = null;
let currentImageIndex = 0;
let currentSportIndex = 0;

function fetchFacilityDetail(id) {
    fetch(`/api/admin/facilities/${id}`)
        .then(response => {
            if (!response.ok) throw new Error("Network response was not ok");
            return response.json();
        })
        .then(data => {
            currentFacilityData = data;
            renderFacilityInfo(data);
            renderGallery(data);
            if (data.sports && data.sports.length > 0) {
                renderSports(data.sports);
            }
            setupActions(data);
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('facilityName').innerHTML = `<div class="text-red-500">Error loading data: ${error.message}</div>`;
        });
}

function renderFacilityInfo(data) {
    document.getElementById('facilityName').textContent = data.name || 'Unnamed Facility';
    
    const statusEl = document.getElementById('facilityStatus');
    if (data.approvalStatus === 'PENDING') {
        statusEl.textContent = 'PENDING APPROVAL';
        statusEl.className = 'px-3 py-1.5 rounded-full text-xs font-black uppercase tracking-widest border border-yellow-200 bg-yellow-50 text-yellow-700';
    } else if (data.approvalStatus === 'APPROVED') {
        statusEl.textContent = 'OPEN / APPROVED';
        statusEl.className = 'px-3 py-1.5 rounded-full text-xs font-black uppercase tracking-widest border border-green-200 bg-green-50 text-green-700';
    } else {
        statusEl.textContent = 'REJECTED';
        statusEl.className = 'px-3 py-1.5 rounded-full text-xs font-black uppercase tracking-widest border border-red-200 bg-red-50 text-red-700';
    }

    document.getElementById('facilityAddress').textContent = data.fullAddress || data.address || 'No address provided';
    document.getElementById('facilityDescription').textContent = data.description || 'No description provided';
    document.getElementById('facilityTime').textContent = `${(data.openTime || '00:00').substring(0,5)} - ${(data.closeTime || '23:59').substring(0,5)}`;
    document.getElementById('facilityCoordinates').textContent = `${data.latitude || '-'}, ${data.longitude || '-'}`;
    document.getElementById('facilitySportCount').textContent = (data.sports ? data.sports.length : 0);

    document.getElementById('ownerBusiness').textContent = data.businessName || 'N/A';
    document.getElementById('ownerName').textContent = data.ownerName || 'N/A';
    document.getElementById('ownerPhone').textContent = data.ownerPhone || 'N/A';
    document.getElementById('ownerEmail').textContent = data.ownerEmail || 'N/A';
}

function renderGallery(data) {
    const images = data.galleryImages || [];
    const container = document.getElementById('galleryContainer');
    const imgEl = document.getElementById('mainGalleryImage');
    const placeholder = document.getElementById('noImagesPlaceholder');
    const dotsContainer = document.getElementById('galleryDots');
    const statusEl = document.getElementById('galleryStatus');

    if (images.length === 0) {
        imgEl.classList.add('hidden');
        placeholder.classList.remove('hidden');
        statusEl.textContent = `Showing image 0 of 0 (Thumbnail & Gallery max 20 images)`;
        return;
    }

    placeholder.classList.add('hidden');
    imgEl.classList.remove('hidden');

    function updateImage() {
        imgEl.classList.add('opacity-0');
        
        setTimeout(() => {
            const currentImg = images[currentImageIndex];
            
            const thumbTag = document.getElementById('thumbnailTag');
            if (thumbTag) {
                if (currentImg.isThumbnail) {
                    thumbTag.classList.remove('hidden');
                } else {
                    thumbTag.classList.add('hidden');
                }
            }
            
            statusEl.textContent = `Showing image ${currentImageIndex + 1} of ${images.length} (Thumbnail & Gallery max 20 images)`;
            
            dotsContainer.innerHTML = images.map((_, idx) => `
                <div class="w-2 h-2 rounded-full cursor-pointer transition-all duration-300 ${idx === currentImageIndex ? 'bg-white scale-125' : 'bg-white/50 hover:bg-white/80'}" onclick="setGalleryImage(${idx})"></div>
            `).join('');

            imgEl.onload = () => {
                imgEl.classList.remove('opacity-0');
            };
            imgEl.src = currentImg.imageUrl;
        }, 150);
    }

    document.getElementById('prevImage').onclick = () => {
        currentImageIndex = (currentImageIndex - 1 + images.length) % images.length;
        updateImage();
    };

    document.getElementById('nextImage').onclick = () => {
        currentImageIndex = (currentImageIndex + 1) % images.length;
        updateImage();
    };

    window.setGalleryImage = (idx) => {
        currentImageIndex = idx;
        updateImage();
    };

    updateImage();
}

function renderSports(sports) {
    const tabsContainer = document.getElementById('sportTabs');
    const statusContainer = document.getElementById('sportStatusContainer');
    const activeStatusEl = document.getElementById('sportActiveStatus');
    
    tabsContainer.innerHTML = sports.map((sport, idx) => `
        <button class="px-5 py-2.5 rounded-full text-sm font-bold transition-colors ${idx === currentSportIndex ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}" onclick="selectSport(${idx})">
            ${sport.sportName}
        </button>
    `).join('');

    const activeSport = sports[currentSportIndex];
    
    statusContainer.classList.remove('hidden');
    if (activeSport.isActive) {
        activeStatusEl.innerHTML = `<div class="w-2 h-2 bg-green-500 rounded-full"></div>Active`;
        activeStatusEl.className = 'px-2 py-0.5 rounded border border-green-200 bg-green-50 text-green-700 flex items-center gap-1.5';
    } else {
        activeStatusEl.innerHTML = `<div class="w-2 h-2 bg-red-500 rounded-full"></div>Inactive`;
        activeStatusEl.className = 'px-2 py-0.5 rounded border border-red-200 bg-red-50 text-red-700 flex items-center gap-1.5';
    }

    renderSportContent(activeSport);
}

window.selectSport = (idx) => {
    currentSportIndex = idx;
    if (currentFacilityData && currentFacilityData.sports) {
        renderSports(currentFacilityData.sports);
    }
};

function renderSportContent(sport) {
    const content = document.getElementById('sportContent');
    
    let courtsHtml = '';
    if (sport.courts && sport.courts.length > 0) {
        courtsHtml = sport.courts.map(court => {
            const surface = court.attributes?.find(a => a.attributeCode === 'SURFACE_TYPE')?.value || 'N/A';
            const roof = court.attributes?.find(a => a.attributeCode === 'ROOF_TYPE')?.value || 'N/A';
            const courtStatus = court.isActive ? 
                `<span class="px-2 py-0.5 bg-green-100 text-green-700 text-[10px] font-black uppercase rounded">Active</span>` : 
                `<span class="px-2 py-0.5 bg-red-100 text-red-700 text-[10px] font-black uppercase rounded">Inactive</span>`;
            
            return `
            <div class="bg-gray-50 border border-gray-100 rounded-2xl p-6">
                <div class="flex justify-between items-start mb-3">
                    <h5 class="font-bold text-gray-900">${court.courtName}</h5>
                    ${courtStatus}
                </div>
                <p class="text-sm text-gray-500 mb-6">${court.description || 'No description provided.'}</p>
                
                <div class="space-y-3">
                    <div class="flex items-center text-sm">
                        <span class="w-24 text-gray-400 font-bold">Surface Type:</span>
                        <span class="font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded">${surface}</span>
                    </div>
                    <div class="flex items-center text-sm">
                        <span class="w-24 text-gray-400 font-bold">Roof Type:</span>
                        <span class="font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded">${roof}</span>
                    </div>
                </div>
            </div>`;
        }).join('');
    } else {
        courtsHtml = '<p class="text-sm text-gray-500">No courts configured yet.</p>';
    }

    let pricesHtml = '';
    if (sport.priceRules && sport.priceRules.length > 0) {
        pricesHtml = sport.priceRules.map(rule => {
            const formattedPrice = new Intl.NumberFormat('en-US').format(rule.pricePerSlot) + ' VND';
            const dayTypeLabels = {
                'WEEKDAY': 'WEEKDAY',
                'WEEKEND': 'WEEKEND',
                'HOLIDAY': 'HOLIDAY'
            };
            const label = dayTypeLabels[rule.dayType] || rule.dayType;
            
            return `
            <div class="grid grid-cols-4 items-center gap-6 p-4 hover:bg-gray-50 transition border-b border-gray-50 last:border-0">
                <div>
                    <span class="text-sm font-bold text-blue-600 bg-blue-50 px-3 py-1 rounded-full">${label}</span>
                </div>
                <div class="flex items-center gap-2 text-sm font-bold text-gray-700">
                    <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                    ${rule.startTime.substring(0,5)} - ${rule.endTime.substring(0,5)}
                </div>
                <div class="text-lg font-black text-blue-600">${formattedPrice}</div>
                <div class="text-xs text-gray-400 italic">Always effective (Default)</div>
            </div>`;
        }).join('');
    } else {
        pricesHtml = '<p class="text-sm text-gray-500 p-4">No price rules configured yet.</p>';
    }

    content.innerHTML = `
        <div class="space-y-10">
            <!-- A. Config -->
            <div>
                <h4 class="text-base font-bold text-gray-900 mb-4">A. Sport Configuration (${sport.sportName}) <span class="text-sm font-normal text-gray-400 italic">(Booking requirements)</span></h4>
                <div class="grid grid-cols-3 gap-6">
                    <div>
                        <label class="block text-[10px] font-black text-gray-500 uppercase tracking-widest mb-2">Sport Name</label>
                        <div class="w-full bg-gray-50 border border-gray-100 text-gray-900 text-sm font-bold rounded-xl px-4 py-3">${sport.sportName}</div>
                    </div>
                    <div>
                        <label class="block text-[10px] font-black text-gray-500 uppercase tracking-widest mb-2">Min Duration</label>
                        <div class="w-full bg-gray-50 border border-gray-100 text-gray-900 text-sm font-bold rounded-xl px-4 py-3">${sport.minDurationMinutes} minutes</div>
                    </div>
                    <div>
                        <label class="block text-[10px] font-black text-gray-500 uppercase tracking-widest mb-2">Slot Step</label>
                        <div class="w-full bg-gray-50 border border-gray-100 text-gray-900 text-sm font-bold rounded-xl px-4 py-3">${sport.slotStepMinutes} minutes</div>
                    </div>
                </div>
            </div>

            <!-- B. Courts -->
            <div>
                <h4 class="text-base font-bold text-gray-900 mb-4">B. Courts List <span class="text-sm font-normal text-gray-400 italic">(${sport.courts ? sport.courts.length : 0} courts)</span></h4>
                <div class="grid grid-cols-2 gap-6">
                    ${courtsHtml}
                </div>
            </div>

            <!-- C. Pricing -->
            <div>
                <h4 class="text-base font-bold text-gray-900 mb-4">C. Dynamic Pricing Table <span class="text-sm font-normal text-gray-400 italic">(Pricing must match slot step: ${sport.slotStepMinutes} minutes)</span></h4>
                <div class="border border-gray-100 rounded-2xl overflow-hidden">
                    <div class="grid grid-cols-4 gap-6 p-4 bg-gray-50/80 border-b border-gray-100 text-[10px] font-black text-gray-400 uppercase tracking-widest">
                        <div>Day Type</div>
                        <div>Time Frame</div>
                        <div>Price per Slot (${sport.slotStepMinutes} mins)</div>
                        <div>Effective Date Range (Optional)</div>
                    </div>
                    ${pricesHtml}
                </div>
            </div>
        </div>
    `;
}

function setupActions(data) {
    const actionContainer = document.getElementById('actionContainer');
    const statusContainer = document.getElementById('statusContainer');
    const rejectModal = document.getElementById('rejectModal');
    
    if (data.approvalStatus === 'PENDING') {
        actionContainer.classList.remove('hidden');
        statusContainer.classList.add('hidden');
        
        document.getElementById('btnApprove').onclick = () => {
            if(confirm("Are you sure you want to approve this facility? It will be publicly active.")) {
                fetch(`/api/admin/facilities/${data.facilityId}/approve`, {
                    method: 'POST'
                }).then(res => {
                    if(res.ok) window.location.reload();
                    else alert("An error occurred during approval.");
                });
            }
        };

        document.getElementById('btnReject').onclick = () => {
            rejectModal.classList.remove('hidden');
        };

        document.getElementById('btnCloseRejectModal').onclick = () => {
            rejectModal.classList.add('hidden');
        };

        document.getElementById('btnCancelReject').onclick = () => {
            rejectModal.classList.add('hidden');
        };

        document.getElementById('btnConfirmReject').onclick = () => {
            const reason = document.getElementById('rejectionReason').value.trim();
            if(!reason) {
                alert("Please provide a rejection reason.");
                return;
            }
            
            fetch(`/api/admin/facilities/${data.facilityId}/reject`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ reason: reason })
            }).then(res => {
                if(res.ok) window.location.reload();
                else alert("An error occurred.");
            });
        };
    } else {
        actionContainer.classList.add('hidden');
        statusContainer.classList.remove('hidden');
        
        if (data.approvalStatus === 'APPROVED') {
            statusContainer.innerHTML = `
                <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-100 mb-4">
                    <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                </div>
                <h3 class="text-xl font-bold text-gray-900 mb-2">Facility Approved</h3>
                <p class="text-gray-500">This facility is approved and active on the SportHub platform.</p>
            `;
        } else {
            statusContainer.innerHTML = `
                <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-100 mb-4">
                    <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </div>
                <h3 class="text-xl font-bold text-gray-900 mb-2">Facility Rejected</h3>
                <p class="text-gray-500 mb-4">This facility's request has been rejected.</p>
                <div class="bg-gray-50 border border-gray-200 rounded-xl p-4 inline-block text-left max-w-lg w-full">
                    <p class="text-[10px] font-black uppercase text-gray-400 tracking-wider mb-2">Rejection Reason:</p>
                    <p class="text-sm font-medium text-gray-800">${data.rejectionReason || 'No detailed reason provided.'}</p>
                </div>
            `;
        }
    }
}
